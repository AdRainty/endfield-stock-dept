package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.OrderBookDTO;
import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.Order;
import com.adrainty.stock.entity.TradeRecord;
import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.enums.OrderType;
import com.adrainty.stock.repository.InstrumentRepository;
import com.adrainty.stock.repository.OrderRepository;
import com.adrainty.stock.repository.TradeRecordRepository;
import com.adrainty.stock.service.OrderBook;
import com.adrainty.stock.service.OrderBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 撮合引擎实现类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineImpl implements OrderBookService {
    
    private final OrderRepository orderRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final InstrumentRepository instrumentRepository;
    
    // 每个品种一个订单簿：key = exchangeId_instrumentCode
    private static final Map<String, OrderBook> ORDER_BOOKS = new ConcurrentHashMap<>();
    
    @Override
    public OrderBookDTO getOrderBook(Long exchangeId, String instrumentCode) {
        OrderBook orderBook = getOrderBook(exchangeId, instrumentCode);
        return buildOrderBookDTO(orderBook, exchangeId, instrumentCode);
    }
    
    @Override
    public BigDecimal addBidOrder(Long exchangeId, String instrumentCode, BigDecimal price, 
                                   BigDecimal quantity, Long orderId) {
        OrderBook orderBook = getOrderBook(exchangeId, instrumentCode);
        return matchAndAddBid(orderBook, price, quantity, orderId);
    }
    
    @Override
    public BigDecimal addAskOrder(Long exchangeId, String instrumentCode, BigDecimal price, 
                                   BigDecimal quantity, Long orderId) {
        OrderBook orderBook = getOrderBook(exchangeId, instrumentCode);
        return matchAndAddAsk(orderBook, price, quantity, orderId);
    }
    
    @Override
    public void removeOrder(Long exchangeId, String instrumentCode, Long orderId, BigDecimal quantity) {
        OrderBook orderBook = getOrderBook(exchangeId, instrumentCode);
        orderBook.removeOrder(orderId, quantity);
    }
    
    @Override
    public List<OrderBookDTO> getAllOrderBooks() {
        List<OrderBookDTO> result = new ArrayList<>();
        for (Map.Entry<String, OrderBook> entry : ORDER_BOOKS.entrySet()) {
            String[] parts = entry.getKey().split("_");
            if (parts.length == 2) {
                Long exchangeId = Long.parseLong(parts[0]);
                String instrumentCode = parts[1];
                result.add(buildOrderBookDTO(entry.getValue(), exchangeId, instrumentCode));
            }
        }
        return result;
    }
    
    /**
     * 获取或创建订单簿
     */
    private OrderBook getOrderBook(Long exchangeId, String instrumentCode) {
        String key = exchangeId + "_" + instrumentCode;
        return ORDER_BOOKS.computeIfAbsent(key, k -> new OrderBook(exchangeId, instrumentCode));
    }
    
    /**
     * 撮合并添加买单
     */
    private BigDecimal matchAndAddBid(OrderBook orderBook, BigDecimal price, 
                                       BigDecimal quantity, Long orderId) {
        BigDecimal remainingQty = quantity;
        
        // 查找可以撮合的卖单（价格 <= 买单价格）
        var matchableAsks = orderBook.findMatchableAsks(price);
        
        for (var askEntry : matchableAsks) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
            
            BigDecimal askPrice = askEntry.getKey();
            OrderBook.OrderLevel askLevel = askEntry.getValue();
            
            // 撮合数量
            BigDecimal matchQty = remainingQty.min(askLevel.getQuantity());
            
            // 更新卖单档口
            askLevel.reduceQuantity(matchQty);
            if (askLevel.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                orderBook.removeOrder(getFirstOrderIdForPrice(orderBook, askPrice), matchQty);
            }
            
            // 更新剩余数量
            remainingQty = remainingQty.subtract(matchQty);
            
            log.info("买单撮合：orderId={}, matchPrice={}, matchQty={}, remainingQty={}", 
                orderId, askPrice, matchQty, remainingQty);
        }
        
        // 如果有剩余，加入买单档口
        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            orderBook.addBid(price, remainingQty, orderId);
        }
        
        return quantity.subtract(remainingQty);
    }
    
    /**
     * 撮合并添加卖单
     */
    private BigDecimal matchAndAddAsk(OrderBook orderBook, BigDecimal price, 
                                       BigDecimal quantity, Long orderId) {
        BigDecimal remainingQty = quantity;
        
        // 查找可以撮合的买单（价格 >= 卖单价格）
        var matchableBids = orderBook.findMatchableBids(price);
        
        for (var bidEntry : matchableBids) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
            
            BigDecimal bidPrice = bidEntry.getKey();
            OrderBook.OrderLevel bidLevel = bidEntry.getValue();
            
            // 撮合数量
            BigDecimal matchQty = remainingQty.min(bidLevel.getQuantity());
            
            // 更新买单档口
            bidLevel.reduceQuantity(matchQty);
            if (bidLevel.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                orderBook.removeOrder(getFirstOrderIdForPrice(orderBook, bidPrice), matchQty);
            }
            
            // 更新剩余数量
            remainingQty = remainingQty.subtract(matchQty);
            
            log.info("卖单撮合：orderId={}, matchPrice={}, matchQty={}, remainingQty={}", 
                orderId, bidPrice, matchQty, remainingQty);
        }
        
        // 如果有剩余，加入卖单档口
        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            orderBook.addAsk(price, remainingQty, orderId);
        }
        
        return quantity.subtract(remainingQty);
    }
    
    /**
     * 获取订单簿中某价格的第一个订单 ID（简化实现）
     */
    private Long getFirstOrderIdForPrice(OrderBook orderBook, BigDecimal price) {
        // 简化实现，实际应该维护价格到订单 ID 列表的映射
        return 0L;
    }
    
    /**
     * 构建档口 DTO
     */
    private OrderBookDTO buildOrderBookDTO(OrderBook orderBook, Long exchangeId, String instrumentCode) {
        OrderBookDTO dto = new OrderBookDTO();
        dto.setExchangeId(exchangeId);
        dto.setInstrumentCode(instrumentCode);
        
        // 获取最新价
        Instrument instrument = instrumentRepository.findByInstrumentCode(instrumentCode).orElse(null);
        if (instrument != null) {
            dto.setLatestPrice(instrument.getCurrentPrice());
            dto.setChangePercent(instrument.getChangePercent());
        }
        
        // 获取买卖档口
        List<OrderBookDTO.PriceLevel> bids = orderBook.getBids(5).stream()
            .map(l -> new OrderBookDTO.PriceLevel(l.getPrice(), l.getQuantity()))
            .toList();
        List<OrderBookDTO.PriceLevel> asks = orderBook.getAsks(5).stream()
            .map(l -> new OrderBookDTO.PriceLevel(l.getPrice(), l.getQuantity()))
            .toList();
        
        dto.setBids(bids);
        dto.setAsks(asks);
        
        return dto;
    }
}
