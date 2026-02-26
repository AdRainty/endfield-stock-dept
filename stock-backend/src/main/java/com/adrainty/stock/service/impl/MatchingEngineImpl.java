package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.OrderBookDTO;
import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.Order;
import com.adrainty.stock.entity.TradeRecord;
import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.enums.OrderType;
import com.adrainty.stock.mapper.InstrumentMapper;
import com.adrainty.stock.mapper.OrderMapper;
import com.adrainty.stock.mapper.TradeRecordMapper;
import com.adrainty.stock.service.CapitalService;
import com.adrainty.stock.service.OrderBookService;
import com.adrainty.stock.service.RedisOrderBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 撮合引擎实现类（基于 Redis）
 * 使用 Redis ZSet 实现买卖订单栈
 *
 * @author adrainty
 * @since 2026-02-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineImpl implements OrderBookService {

    private final OrderMapper orderMapper;
    private final TradeRecordMapper tradeRecordMapper;
    private final InstrumentMapper instrumentMapper;
    private final RedisOrderBook redisOrderBook;
    private final RedissonClient redissonClient;
    private final CapitalService capitalService;

    private static final String MATCH_LOCK_PREFIX = "match:lock:";

    @Override
    public OrderBookDTO getOrderBook(Long exchangeId, String instrumentCode) {
        return buildOrderBookDTO(exchangeId, instrumentCode);
    }

    @Override
    public BigDecimal addBidOrder(Long exchangeId, String instrumentCode, BigDecimal price,
                                  BigDecimal quantity, Long orderId) {
        String lockKey = MATCH_LOCK_PREFIX + exchangeId + "_" + instrumentCode;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                BigDecimal matchedQty = matchBidOrder(exchangeId, instrumentCode, price, quantity, orderId);

                // 剩余未成交数量加入买单栈
                BigDecimal remainingQty = quantity.subtract(matchedQty);
                if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
                    redisOrderBook.addBid(exchangeId, instrumentCode, price, remainingQty, orderId);
                }

                return matchedQty;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("买单撮合被中断：orderId={}", orderId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal addAskOrder(Long exchangeId, String instrumentCode, BigDecimal price,
                                  BigDecimal quantity, Long orderId) {
        String lockKey = MATCH_LOCK_PREFIX + exchangeId + "_" + instrumentCode;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                BigDecimal matchedQty = matchAskOrder(exchangeId, instrumentCode, price, quantity, orderId);

                // 剩余未成交数量加入卖单栈
                BigDecimal remainingQty = quantity.subtract(matchedQty);
                if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
                    redisOrderBook.addAsk(exchangeId, instrumentCode, price, remainingQty, orderId);
                }

                return matchedQty;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("卖单撮合被中断：orderId={}", orderId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return BigDecimal.ZERO;
    }

    @Override
    public void removeOrder(Long exchangeId, String instrumentCode, Long orderId, BigDecimal quantity) {
        redisOrderBook.removeOrder(exchangeId, instrumentCode, orderId);
    }

    @Override
    public List<OrderBookDTO> getAllOrderBooks() {
        // TODO: 需要实现获取所有品种的档口
        return new ArrayList<>();
    }

    /**
     * 撮合买单
     *
     * @return 成交数量
     */
    private BigDecimal matchBidOrder(Long exchangeId, String instrumentCode,
                                     BigDecimal bidPrice, BigDecimal bidQuantity, Long orderId) {
        BigDecimal matchedQty = BigDecimal.ZERO;
        BigDecimal remainingQty = bidQuantity;

        // 查找可撮合的卖单（价格 <= 买单价格）
        List<RedisOrderBook.OrderDetail> matchableAsks =
                redisOrderBook.findMatchableAsks(exchangeId, instrumentCode, bidPrice);

        for (RedisOrderBook.OrderDetail ask : matchableAsks) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;

            // 撮合数量
            BigDecimal matchQty = remainingQty.min(ask.getQuantity());

            // 创建成交记录
            createTradeRecord(exchangeId, instrumentCode, ask.getOrderId(), orderId,
                    ask.getPrice(), matchQty);

            // 更新卖单数量
            BigDecimal askRemainingQty = ask.getQuantity().subtract(matchQty);
            if (askRemainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                // 卖单完全成交，更新订单状态
                updateOrderFilled(ask.getOrderId(), ask.getPrice(), ask.getQuantity(),
                        OrderStatus.FILLED);
                redisOrderBook.removeOrder(exchangeId, instrumentCode, ask.getOrderId());
            } else {
                // 卖单部分成交
                updateOrderFilled(ask.getOrderId(), ask.getPrice(), matchQty,
                        OrderStatus.PARTIALLY_FILLED);
                redisOrderBook.updateOrderQuantity(exchangeId, instrumentCode,
                        ask.getOrderId(), askRemainingQty);
            }

            matchedQty = matchedQty.add(matchQty);
            remainingQty = remainingQty.subtract(matchQty);

            log.info("买单撮合：buyOrderId={}, sellOrderId={}, matchPrice={}, matchQty={}, remainingQty={}",
                    orderId, ask.getOrderId(), ask.getPrice(), matchQty, remainingQty);
        }

        // 更新买单状态
        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            updateOrderUnfilled(orderId, remainingQty, OrderStatus.PENDING);
        }

        return matchedQty;
    }

    /**
     * 撮合卖单
     *
     * @return 成交数量
     */
    private BigDecimal matchAskOrder(Long exchangeId, String instrumentCode,
                                     BigDecimal askPrice, BigDecimal askQuantity, Long orderId) {
        BigDecimal matchedQty = BigDecimal.ZERO;
        BigDecimal remainingQty = askQuantity;

        // 查找可撮合的买单（价格 >= 卖单价格）
        List<RedisOrderBook.OrderDetail> matchableBids =
                redisOrderBook.findMatchableBids(exchangeId, instrumentCode, askPrice);

        for (RedisOrderBook.OrderDetail bid : matchableBids) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;

            // 撮合数量
            BigDecimal matchQty = remainingQty.min(bid.getQuantity());

            // 创建成交记录
            createTradeRecord(exchangeId, instrumentCode, orderId, bid.getOrderId(),
                    bid.getPrice(), matchQty);

            // 更新买单数量
            BigDecimal bidRemainingQty = bid.getQuantity().subtract(matchQty);
            if (bidRemainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                // 买单完全成交，更新订单状态
                updateOrderFilled(bid.getOrderId(), bid.getPrice(), bid.getQuantity(),
                        OrderStatus.FILLED);
                redisOrderBook.removeOrder(exchangeId, instrumentCode, bid.getOrderId());
            } else {
                // 买单部分成交
                updateOrderFilled(bid.getOrderId(), bid.getPrice(), matchQty,
                        OrderStatus.PARTIALLY_FILLED);
                redisOrderBook.updateOrderQuantity(exchangeId, instrumentCode,
                        bid.getOrderId(), bidRemainingQty);
            }

            matchedQty = matchedQty.add(matchQty);
            remainingQty = remainingQty.subtract(matchQty);

            log.info("卖单撮合：sellOrderId={}, buyOrderId={}, matchPrice={}, matchQty={}, remainingQty={}",
                    orderId, bid.getOrderId(), bid.getPrice(), matchQty, remainingQty);
        }

        // 更新卖单状态
        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            updateOrderUnfilled(orderId, remainingQty, OrderStatus.PENDING);
        }

        return matchedQty;
    }

    /**
     * 创建成交记录
     */
    private void createTradeRecord(Long exchangeId, String instrumentCode,
                                   Long sellOrderId, Long buyOrderId,
                                   BigDecimal price, BigDecimal quantity) {
        // 获取买卖双方用户 ID
        com.adrainty.stock.entity.Order buyOrder = orderMapper.selectById(buyOrderId);
        com.adrainty.stock.entity.Order sellOrder = orderMapper.selectById(sellOrderId);

        TradeRecord record = new TradeRecord();
        record.setTradeNo("TRD_" + System.currentTimeMillis() + "_" + buyOrderId);
        record.setOrderNo(buyOrder != null ? buyOrder.getOrderNo() : "");
        record.setBuyerUserId(buyOrder != null ? buyOrder.getUserId() : null);
        record.setSellerUserId(sellOrder != null ? sellOrder.getUserId() : null);
        record.setExchangeId(exchangeId);
        record.setInstrumentCode(instrumentCode);
        record.setOrderType(OrderType.BUY);
        record.setPrice(price);
        record.setQuantity(quantity);
        record.setAmount(price.multiply(quantity));
        record.setTradeTime(LocalDateTime.now());

        tradeRecordMapper.insert(record);
    }

    /**
     * 更新订单为已成交状态（买单成交时扣除冻结资金）
     */
    private void updateOrderFilled(Long orderId, BigDecimal price, BigDecimal quantity, OrderStatus status) {
        com.adrainty.stock.entity.Order order = orderMapper.selectById(orderId);
        if (order != null) {
            BigDecimal filledAmount = (order.getFilledAmount() != null ? order.getFilledAmount() : BigDecimal.ZERO)
                    .add(price.multiply(quantity));
            order.setFilledQuantity(order.getFilledQuantity().add(quantity));
            order.setFilledAmount(filledAmount);
            order.setStatus(status);
            if (status == OrderStatus.FILLED) {
                order.setFilledTime(LocalDateTime.now());
            }
            orderMapper.updateById(order);

            // 买单成交后，扣除已成交部分的冻结资金
            if (order.getOrderType() == OrderType.BUY) {
                BigDecimal matchAmount = price.multiply(quantity);
                capitalService.deductCapital(order.getUserId(), order.getExchangeId(),
                    matchAmount, order.getOrderNo(), "委托成交");
                // 解冻剩余冻结资金（如果是部分成交或完全成交）
                if (status == OrderStatus.FILLED) {
                    BigDecimal remainingFrozen = order.getPrice().multiply(order.getQuantity())
                        .subtract(filledAmount);
                    if (remainingFrozen.compareTo(BigDecimal.ZERO) > 0) {
                        capitalService.unfreezeCapital(order.getUserId(), order.getExchangeId(),
                            remainingFrozen, order.getOrderNo());
                    }
                }
            }
        }
    }

    /**
     * 更新订单未成交数量
     */
    private void updateOrderUnfilled(Long orderId, BigDecimal unfilledQuantity, OrderStatus status) {
        com.adrainty.stock.entity.Order order = orderMapper.selectById(orderId);
        if (order != null) {
            order.setUnfilledQuantity(unfilledQuantity);
            order.setStatus(status);
            orderMapper.updateById(order);
        }
    }

    /**
     * 构建档口 DTO
     */
    private OrderBookDTO buildOrderBookDTO(Long exchangeId, String instrumentCode) {
        OrderBookDTO dto = new OrderBookDTO();
        dto.setExchangeId(exchangeId);
        dto.setInstrumentCode(instrumentCode);

        // 获取最新价
        Instrument instrument = instrumentMapper.findByInstrumentCode(instrumentCode);
        if (instrument != null) {
            dto.setLatestPrice(instrument.getCurrentPrice());
            dto.setChangePercent(instrument.getChangePercent());
        }

        // 获取买卖档口（前 5 档）
        List<RedisOrderBook.PriceLevel> bidLevels = redisOrderBook.getPriceLevels(
                exchangeId, instrumentCode, "bid", 5);
        List<RedisOrderBook.PriceLevel> askLevels = redisOrderBook.getPriceLevels(
                exchangeId, instrumentCode, "ask", 5);

        List<OrderBookDTO.PriceLevel> bids = bidLevels.stream()
                .map(l -> new OrderBookDTO.PriceLevel(l.getPrice(), l.getQuantity()))
                .toList();
        List<OrderBookDTO.PriceLevel> asks = askLevels.stream()
                .map(l -> new OrderBookDTO.PriceLevel(l.getPrice(), l.getQuantity()))
                .toList();

        dto.setBids(bids);
        dto.setAsks(asks);

        return dto;
    }
}
