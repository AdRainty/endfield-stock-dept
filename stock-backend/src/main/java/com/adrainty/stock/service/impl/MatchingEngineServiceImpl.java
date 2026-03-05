package com.adrainty.stock.service;

import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.Order;
import com.adrainty.stock.entity.TradeRecord;
import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.enums.OrderType;
import com.adrainty.stock.mapper.InstrumentMapper;
import com.adrainty.stock.mapper.OrderMapper;
import com.adrainty.stock.mapper.TradeRecordMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 撮合引擎服务实现
 * 基于 Redis ZSet 实现买卖订单队列，参考 A 股成交规则：
 * - 价格优先：较高价格买入申报优先成交，较低价格卖出申报优先成交
 * - 时间优先：相同价位申报时，先申报者优先成交
 *
 * 使用两个独立的 ZSet：
 * - 买单队列：score = 价格 * 1000000 + (MAX_TIME - 时间戳)，价格越高、时间越早分数越高
 * - 卖单队列：score = 价格 * 1000000 + 时间戳，价格越低、时间越早分数越低
 *
 * @author adrainty
 * @since 2026-03-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineServiceImpl implements MatchingEngineService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;
    private final TradeRecordMapper tradeRecordMapper;
    private final InstrumentMapper instrumentMapper;
    private final CapitalService capitalService;
    private final PositionService positionService;

    // Redis Key 前缀
    private static final String BID_QUEUE_PREFIX = "matching:bid:";
    private static final String ASK_QUEUE_PREFIX = "matching:ask:";
    private static final String ORDER_DETAIL_PREFIX = "matching:order:";
    private static final String MATCH_LOCK_PREFIX = "match:lock:";

    // 交易时间配置（默认 A 股时间）
    private static final LocalTime MORNING_OPEN = LocalTime.of(9, 30);
    private static final LocalTime MORNING_CLOSE = LocalTime.of(11, 30);
    private static final LocalTime AFTERNOON_OPEN = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_CLOSE = LocalTime.of(15, 0);

    // 提前启动时间（分钟）
    private static final int PRE_START_MINUTES = 2;

    // 撮合间隔（毫秒）
    private static final int MATCH_INTERVAL_MS = 100;

    // 时间格式化器
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // 存储每个品种的撮合线程
    private final Map<String, ScheduledFuture<?>> matchingTasks = new ConcurrentHashMap<>();

    // 线程池
    private final ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler();

    /**
     * 启动所有交易品种的撮合引擎
     */
    @Override
    public void startAllMatchingEngines() {
        // 获取所有交易所
        List<Instrument> instruments = instrumentMapper.findAll();

        for (Instrument instrument : instruments) {
            startMatchingEngine(instrument.getExchangeId(), instrument.getInstrumentCode());
        }

        log.info("已启动 {} 个品种撮合引擎", instruments.size());
    }

    /**
     * 停止所有撮合引擎
     */
    @Override
    public void stopAllMatchingEngines() {
        for (String key : matchingTasks.keySet()) {
            ScheduledFuture<?> future = matchingTasks.get(key);
            if (future != null) {
                future.cancel(false);
            }
        }
        matchingTasks.clear();
        scheduler.shutdown();
        log.info("已停止所有撮合引擎");
    }

    /**
     * 启动指定品种的撮合引擎
     */
    @Override
    public void startMatchingEngine(Long exchangeId, String instrumentCode) {
        String taskKey = exchangeId + "_" + instrumentCode;

        // 如果已有运行中的线程，先停止
        stopMatchingEngine(exchangeId, instrumentCode);

        // 计算启动延迟（提前 2 分钟启动）
        long delayMs = calculatePreStartDelay();

        // 启动撮合线程
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            () -> runMatchingCycle(exchangeId, instrumentCode),
            delayMs,
            MATCH_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        matchingTasks.put(taskKey, future);
        log.info("启动撮合引擎：exchangeId={}, instrumentCode={}, 延迟启动={}ms",
            exchangeId, instrumentCode, delayMs);
    }

    /**
     * 停止指定品种的撮合引擎
     */
    @Override
    public void stopMatchingEngine(Long exchangeId, String instrumentCode) {
        String taskKey = exchangeId + "_" + instrumentCode;
        ScheduledFuture<?> future = matchingTasks.remove(taskKey);
        if (future != null) {
            future.cancel(false);
        }
        log.debug("停止撮合引擎：exchangeId={}, instrumentCode={}", exchangeId, instrumentCode);
    }

    /**
     * 计算提前启动的延迟时间
     * 返回距离下一个开盘前 2 分钟的时间（毫秒）
     */
    private long calculatePreStartDelay() {
        LocalTime now = LocalTime.now();

        // 计算早盘开盘前 2 分钟
        LocalTime morningPreStart = MORNING_OPEN.minusMinutes(PRE_START_MINUTES);

        // 计算午盘开盘前 2 分钟
        LocalTime afternoonPreStart = AFTERNOON_OPEN.minusMinutes(PRE_START_MINUTES);

        if (now.isBefore(morningPreStart)) {
            // 当前时间在早盘预启动之前
            return Duration.between(now, morningPreStart).toMillis();
        } else if (now.isBefore(MORNING_OPEN)) {
            // 当前时间在早盘预启动和开盘之间，立即启动
            return 0;
        } else if (now.isBefore(afternoonPreStart)) {
            // 当前时间在午盘预启动之前
            return Duration.between(now, afternoonPreStart).toMillis();
        } else if (now.isBefore(AFTERNOON_OPEN)) {
            // 当前时间在午盘预启动和开盘之间，立即启动
            return 0;
        } else {
            // 已经过了午盘开盘时间，如果还在交易时间内则立即启动
            if (now.isBefore(AFTERNOON_CLOSE)) {
                return 0;
            }
            // 已收市，等待明天早盘
            long millisUntilTomorrow = Duration.between(now, LocalTime.MAX).toMillis() +
                Duration.between(LocalTime.MIDNIGHT, morningPreStart).toMillis();
            return millisUntilTomorrow;
        }
    }

    /**
     * 执行撮合周期
     */
    private void runMatchingCycle(Long exchangeId, String instrumentCode) {
        // 检查是否在交易时间内（或接近开盘）
        if (!shouldRunMatching(exchangeId)) {
            return;
        }

        String lockKey = MATCH_LOCK_PREFIX + exchangeId + "_" + instrumentCode;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(50, TimeUnit.MILLISECONDS)) {
                try {
                    matchOrders(exchangeId, instrumentCode);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("撮合线程被中断：exchangeId={}, instrumentCode={}", exchangeId, instrumentCode);
        } catch (Exception e) {
            log.error("撮合执行失败：exchangeId={}, instrumentCode={}", exchangeId, instrumentCode, e);
        }
    }

    /**
     * 判断是否应该执行撮合
     */
    private boolean shouldRunMatching(Long exchangeId) {
        // 如果交易所配置了交易时间，则检查
        // 这里使用默认的 A 股交易时间
        LocalTime now = LocalTime.now();

        // 早盘时段（含预启动时间）
        LocalTime morningPreStart = MORNING_OPEN.minusMinutes(PRE_START_MINUTES);
        boolean inMorningSession = (now.isAfter(morningPreStart) && now.isBefore(MORNING_CLOSE)) ||
                                   (now.equals(morningPreStart) || now.equals(MORNING_CLOSE));

        // 午盘时段
        LocalTime afternoonPreStart = AFTERNOON_OPEN.minusMinutes(PRE_START_MINUTES);
        boolean inAfternoonSession = (now.isAfter(afternoonPreStart) && now.isBefore(AFTERNOON_CLOSE)) ||
                                     (now.equals(afternoonPreStart) || now.equals(AFTERNOON_CLOSE));

        return inMorningSession || inAfternoonSession;
    }

    /**
     * 执行撮合逻辑
     */
    private void matchOrders(Long exchangeId, String instrumentCode) {
        String bidKey = BID_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;
        String askKey = ASK_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;

        // 获取最佳买单（最高价）
        Set<Object> bestBids = redisTemplate.opsForZSet().reverseRange(bidKey, 0, 0);
        // 获取最佳卖单（最低价）
        Set<Object> bestAsks = redisTemplate.opsForZSet().range(askKey, 0, 0);

        if (bestBids == null || bestBids.isEmpty() || bestAsks == null || bestAsks.isEmpty()) {
            return;
        }

        Long bestBidOrderId = Long.valueOf(bestBids.iterator().next().toString());
        Long bestAskOrderId = Long.valueOf(bestAsks.iterator().next().toString());

        // 获取订单详情
        OrderDetail bidOrder = getOrderDetail(exchangeId, instrumentCode, bestBidOrderId);
        OrderDetail askOrder = getOrderDetail(exchangeId, instrumentCode, bestAskOrderId);

        if (bidOrder == null || askOrder == null) {
            return;
        }

        // 检查是否可撮合：买价 >= 卖价
        if (bidOrder.getPrice().compareTo(askOrder.getPrice()) >= 0) {
            // 成交价格取卖价（A 股规则）
            BigDecimal matchPrice = askOrder.getPrice();

            // 撮合数量
            BigDecimal matchQty = bidOrder.getQuantity().min(askOrder.getQuantity());

            if (matchQty.compareTo(BigDecimal.ZERO) > 0) {
                // 执行撮合
                executeMatch(exchangeId, instrumentCode, bidOrder, askOrder, matchPrice, matchQty);
            }
        }
    }

    /**
     * 执行撮合交易
     */
    private void executeMatch(Long exchangeId, String instrumentCode,
                              OrderDetail bidOrder, OrderDetail askOrder,
                              BigDecimal matchPrice, BigDecimal matchQty) {

        log.info("撮合成交：buyOrderId={}, sellOrderId={}, price={}, qty={}",
            bidOrder.getOrderId(), askOrder.getOrderId(), matchPrice, matchQty);

        // 更新买单数量
        BigDecimal bidRemainingQty = bidOrder.getQuantity().subtract(matchQty);
        if (bidRemainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            // 买单完全成交
            updateOrderStatus(bidOrder.getOrderId(), matchPrice, matchQty, OrderStatus.FILLED);
            removeOrderFromQueue(exchangeId, instrumentCode, bidOrder.getOrderId(), "bid");
        } else {
            // 买单部分成交
            updateOrderStatus(bidOrder.getOrderId(), matchPrice, matchQty, OrderStatus.PARTIALLY_FILLED);
            updateOrderQuantityInQueue(exchangeId, instrumentCode, bidOrder.getOrderId(), bidRemainingQty);
        }

        // 更新卖单数量
        BigDecimal askRemainingQty = askOrder.getQuantity().subtract(matchQty);
        if (askRemainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            // 卖单完全成交
            updateOrderStatus(askOrder.getOrderId(), matchPrice, matchQty, OrderStatus.FILLED);
            removeOrderFromQueue(exchangeId, instrumentCode, askOrder.getOrderId(), "ask");
        } else {
            // 卖单部分成交
            updateOrderStatus(askOrder.getOrderId(), matchPrice, matchQty, OrderStatus.PARTIALLY_FILLED);
            updateOrderQuantityInQueue(exchangeId, instrumentCode, askOrder.getOrderId(), askRemainingQty);
        }

        // 创建成交记录
        createTradeRecord(exchangeId, instrumentCode, bidOrder.getOrderId(), askOrder.getOrderId(),
            matchPrice, matchQty);

        // 通知买卖双方（可以通过 WebSocket 或事件）
        notifyTrade(exchangeId, instrumentCode, bidOrder.getOrderId(), askOrder.getOrderId(),
            matchPrice, matchQty);
    }

    /**
     * 添加买单到队列
     * score = 价格 * 1000000 + (MAX_TIME - 时间戳)
     * 价格越高分数越高，同价格时间越早分数越高
     */
    @Override
    public void addBidOrder(Long exchangeId, String instrumentCode, BigDecimal price,
                            BigDecimal quantity, Long orderId, Long timestamp) {
        String key = BID_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;
        String detailKey = ORDER_DETAIL_PREFIX + exchangeId + "_" + instrumentCode;

        // 计算 score：价格优先，时间优先
        double score = calculateBidScore(price, timestamp);

        // 添加到买单 ZSet
        redisTemplate.opsForZSet().add(key, String.valueOf(orderId), score);

        // 存储订单详情
        OrderDetail detail = new OrderDetail(orderId, price, quantity, timestamp);
        try {
            String json = objectMapper.writeValueAsString(detail);
            redisTemplate.opsForHash().put(detailKey, String.valueOf(orderId), json);
        } catch (JsonProcessingException e) {
            log.error("存储订单详情失败：orderId={}", orderId, e);
        }

        log.debug("添加买单：orderId={}, price={}, quantity={}, timestamp={}", orderId, price, quantity, timestamp);
    }

    /**
     * 添加卖单到队列
     * score = 价格 * 1000000 + 时间戳
     * 价格越低分数越低（越靠前），同价格时间越早分数越低
     */
    @Override
    public void addAskOrder(Long exchangeId, String instrumentCode, BigDecimal price,
                            BigDecimal quantity, Long orderId, Long timestamp) {
        String key = ASK_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;
        String detailKey = ORDER_DETAIL_PREFIX + exchangeId + "_" + instrumentCode;

        // 计算 score：价格优先，时间优先
        double score = calculateAskScore(price, timestamp);

        // 添加到卖单 ZSet
        redisTemplate.opsForZSet().add(key, String.valueOf(orderId), score);

        // 存储订单详情
        OrderDetail detail = new OrderDetail(orderId, price, quantity, timestamp);
        try {
            String json = objectMapper.writeValueAsString(detail);
            redisTemplate.opsForHash().put(detailKey, String.valueOf(orderId), json);
        } catch (JsonProcessingException e) {
            log.error("存储订单详情失败：orderId={}", orderId, e);
        }

        log.debug("添加卖单：orderId={}, price={}, quantity={}, timestamp={}", orderId, price, quantity, timestamp);
    }

    /**
     * 移除订单
     */
    @Override
    public void removeOrder(Long exchangeId, String instrumentCode, Long orderId) {
        String bidKey = BID_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;
        String askKey = ASK_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;
        String detailKey = ORDER_DETAIL_PREFIX + exchangeId + "_" + instrumentCode;

        // 从买卖队列中移除
        redisTemplate.opsForZSet().remove(bidKey, String.valueOf(orderId));
        redisTemplate.opsForZSet().remove(askKey, String.valueOf(orderId));

        // 从订单详情中移除
        redisTemplate.opsForHash().delete(detailKey, String.valueOf(orderId));

        log.debug("移除订单：orderId={}", orderId);
    }

    /**
     * 获取最佳买单价格
     */
    @Override
    public BigDecimal getBestBidPrice(Long exchangeId, String instrumentCode) {
        String key = BID_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;
        Set<Object> results = redisTemplate.opsForZSet().reverseRange(key, 0, 0);

        if (results == null || results.isEmpty()) {
            return null;
        }

        Long orderId = Long.valueOf(results.iterator().next().toString());
        OrderDetail detail = getOrderDetail(exchangeId, instrumentCode, orderId);
        return detail != null ? detail.getPrice() : null;
    }

    /**
     * 获取最佳卖单价格
     */
    @Override
    public BigDecimal getBestAskPrice(Long exchangeId, String instrumentCode) {
        String key = ASK_QUEUE_PREFIX + exchangeId + "_" + instrumentCode;
        Set<Object> results = redisTemplate.opsForZSet().range(key, 0, 0);

        if (results == null || results.isEmpty()) {
            return null;
        }

        Long orderId = Long.valueOf(results.iterator().next().toString());
        OrderDetail detail = getOrderDetail(exchangeId, instrumentCode, orderId);
        return detail != null ? detail.getPrice() : null;
    }

    /**
     * 检查是否在交易时间段内
     */
    @Override
    public boolean isWithinTradingHours(Long exchangeId) {
        LocalTime now = LocalTime.now();

        // 检查是否在早盘或午盘交易时间内
        boolean inMorning = !now.isBefore(MORNING_OPEN) && !now.isAfter(MORNING_CLOSE);
        boolean inAfternoon = !now.isBefore(AFTERNOON_OPEN) && !now.isAfter(AFTERNOON_CLOSE);

        return inMorning || inAfternoon;
    }

    /**
     * 获取距离开盘的时间（秒）
     */
    @Override
    public long getSecondsUntilMarketOpen(Long exchangeId) {
        LocalTime now = LocalTime.now();

        // 如果当前在交易时间内，返回 0
        if (isWithinTradingHours(exchangeId)) {
            return 0;
        }

        // 计算距离下一个开盘时间
        if (now.isBefore(MORNING_OPEN)) {
            return Duration.between(now, MORNING_OPEN).getSeconds();
        } else if (now.isBefore(AFTERNOON_OPEN)) {
            return Duration.between(now, AFTERNOON_OPEN).getSeconds();
        } else {
            // 已收市，返回距离明天开盘的时间
            return Duration.between(now, LocalTime.MAX).getSeconds() +
                Duration.between(LocalTime.MIDNIGHT, MORNING_OPEN).getSeconds();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算买单 score
     */
    private double calculateBidScore(BigDecimal price, long timestamp) {
        // 价格 * 1000000 + (Long.MAX_VALUE - timestamp) / 1000000
        // 价格越高分数越高，同价格时间越早（timestamp 越小）分数越高
        return price.multiply(BigDecimal.valueOf(1000000)).doubleValue() +
               (Double.MAX_VALUE - timestamp) / 1000000.0;
    }

    /**
     * 计算卖单 score
     */
    private double calculateAskScore(BigDecimal price, long timestamp) {
        // 价格 * 1000000 + timestamp / 1000000
        // 价格越低分数越低，同价格时间越早（timestamp 越小）分数越低
        return price.multiply(BigDecimal.valueOf(1000000)).doubleValue() +
               timestamp / 1000000.0;
    }

    /**
     * 获取订单详情
     */
    private OrderDetail getOrderDetail(Long exchangeId, String instrumentCode, Long orderId) {
        String detailKey = ORDER_DETAIL_PREFIX + exchangeId + "_" + instrumentCode;
        Object json = redisTemplate.opsForHash().get(detailKey, String.valueOf(orderId));

        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json.toString(), OrderDetail.class);
        } catch (JsonProcessingException e) {
            log.error("解析订单详情失败：orderId={}", orderId, e);
            return null;
        }
    }

    /**
     * 从队列中移除订单
     */
    private void removeOrderFromQueue(Long exchangeId, String instrumentCode,
                                       Long orderId, String side) {
        String key = ("bid".equals(side) ? BID_QUEUE_PREFIX : ASK_QUEUE_PREFIX)
                     + exchangeId + "_" + instrumentCode;
        redisTemplate.opsForZSet().remove(key, String.valueOf(orderId));
    }

    /**
     * 更新队列中的订单数量
     */
    private void updateOrderQuantityInQueue(Long exchangeId, String instrumentCode,
                                             Long orderId, BigDecimal newQuantity) {
        String detailKey = ORDER_DETAIL_PREFIX + exchangeId + "_" + instrumentCode;
        OrderDetail detail = getOrderDetail(exchangeId, instrumentCode, orderId);

        if (detail != null) {
            detail.setQuantity(newQuantity);
            try {
                String json = objectMapper.writeValueAsString(detail);
                redisTemplate.opsForHash().put(detailKey, String.valueOf(orderId), json);
            } catch (JsonProcessingException e) {
                log.error("更新订单详情失败：orderId={}", orderId, e);
            }
        }
    }

    /**
     * 更新订单状态
     */
    private void updateOrderStatus(Long orderId, BigDecimal price, BigDecimal quantity, OrderStatus status) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            BigDecimal filledAmount = (order.getFilledAmount() != null ? order.getFilledAmount() : BigDecimal.ZERO)
                    .add(price.multiply(quantity));
            order.setFilledQuantity(order.getFilledQuantity().add(quantity));
            order.setFilledAmount(filledAmount);
            order.setUnfilledQuantity(order.getUnfilledQuantity().subtract(quantity));
            order.setStatus(status);

            if (status == OrderStatus.FILLED) {
                order.setFilledTime(java.time.LocalDateTime.now());
            }

            orderMapper.updateById(order);

            // 处理资金和持仓
            handleTradeSettlement(order, price, quantity);
        }
    }

    /**
     * 处理交易结算
     */
    private void handleTradeSettlement(Order order, BigDecimal price, BigDecimal quantity) {
        if (order.getOrderType() == OrderType.BUY) {
            // 买单成交，扣除资金
            BigDecimal matchAmount = price.multiply(quantity);
            capitalService.deductCapital(order.getUserId(), order.getExchangeId(),
                matchAmount, order.getOrderNo(), "委托成交");

            // 增加持仓
            positionService.increasePosition(order.getUserId(), order.getExchangeId(),
                order.getInstrumentCode(), quantity, price);

            // 如果是完全成交，解冻剩余冻结资金
            if (order.getStatus() == OrderStatus.FILLED) {
                BigDecimal totalFrozen = order.getPrice().multiply(order.getQuantity());
                BigDecimal remainingFrozen = totalFrozen.subtract(order.getFilledAmount());
                if (remainingFrozen.compareTo(BigDecimal.ZERO) > 0) {
                    capitalService.unfreezeCapital(order.getUserId(), order.getExchangeId(),
                        remainingFrozen, order.getOrderNo());
                }
            }
        } else {
            // 卖单成交，减少持仓
            positionService.decreasePosition(order.getUserId(), order.getExchangeId(),
                order.getInstrumentCode(), quantity, price);
        }
    }

    /**
     * 创建成交记录
     */
    private void createTradeRecord(Long exchangeId, String instrumentCode,
                                   Long buyOrderId, Long sellOrderId,
                                   BigDecimal price, BigDecimal quantity) {
        Order buyOrder = orderMapper.selectById(buyOrderId);
        Order sellOrder = orderMapper.selectById(sellOrderId);

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
        record.setTradeTime(java.time.LocalDateTime.now());

        tradeRecordMapper.insert(record);
    }

    /**
     * 通知交易结果（可扩展为 WebSocket 推送）
     */
    private void notifyTrade(Long exchangeId, String instrumentCode,
                             Long buyOrderId, Long sellOrderId,
                             BigDecimal price, BigDecimal quantity) {
        // TODO: 实现 WebSocket 推送通知
        log.info("交易通知：exchangeId={}, instrumentCode={}, buyOrderId={}, sellOrderId={}, price={}, qty={}",
            exchangeId, instrumentCode, buyOrderId, sellOrderId, price, quantity);
    }

    /**
     * 订单详情类
     */
    @Data
    public static class OrderDetail {
        private Long orderId;
        private BigDecimal price;
        private BigDecimal quantity;
        private Long timestamp;

        public OrderDetail() {}

        public OrderDetail(Long orderId, BigDecimal price, BigDecimal quantity, Long timestamp) {
            this.orderId = orderId;
            this.price = price;
            this.quantity = quantity;
            this.timestamp = timestamp;
        }
    }

    /**
     * 简单的调度器实现
     */
    private static class ScheduledExecutorScheduler {
        private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r, "matching-engine");
                t.setDaemon(true);
                return t;
            }
        );

        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                       long initialDelay,
                                                       long period,
                                                       TimeUnit unit) {
            return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
        }

        public void shutdown() {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
