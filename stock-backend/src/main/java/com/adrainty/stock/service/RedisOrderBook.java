package com.adrainty.stock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的订单簿实现
 * 使用 Redis ZSet 存储买卖订单
 *
 * @author adrainty
 * @since 2026-02-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOrderBook {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String ORDER_BOOK_PREFIX = "orderbook:";
    private static final String ORDER_SUFFIX = ":orders";
    private static final String BIDS_SUFFIX = ":bids";
    private static final String ASKS_SUFFIX = ":asks";
    private static final String LOCK_SUFFIX = ":lock";

    /**
     * 获取订单簿 Key
     */
    private String getOrderBookKey(Long exchangeId, String instrumentCode) {
        return ORDER_BOOK_PREFIX + exchangeId + "_" + instrumentCode;
    }

    /**
     * 获取买单 ZSet Key
     */
    private String getBidsKey(String orderBookKey) {
        return orderBookKey + BIDS_SUFFIX;
    }

    /**
     * 获取卖单 ZSet Key
     */
    private String getAsksKey(String orderBookKey) {
        return orderBookKey + ASKS_SUFFIX;
    }

    /**
     * 获取订单 ZSet Key
     */
    private String getOrdersKey(String orderBookKey) {
        return orderBookKey + ORDER_SUFFIX;
    }

    /**
     * 获取分布式锁
     */
    private RLock getLock(String orderBookKey) {
        return redissonClient.getLock(orderBookKey + LOCK_SUFFIX);
    }

    /**
     * 添加买单
     * score = 价格 * 10000 + 时间戳（价格高优先，同价格时间早优先）
     */
    public void addBid(Long exchangeId, String instrumentCode, BigDecimal price,
                       BigDecimal quantity, Long orderId) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String bidsKey = getBidsKey(key);
        String ordersKey = getOrdersKey(key);
        RLock lock = getLock(key);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 计算 score：价格越高分数越高，同价格时间越早分数越高
                // 使用 Long.MAX_VALUE - timestamp 来保证同价格时时间早的分数高
                double score = calculateBidScore(price, System.currentTimeMillis());

                // 添加到买单 ZSet
                redisTemplate.opsForZSet().add(bidsKey, String.valueOf(orderId), score);

                // 存储订单详情
                OrderDetail detail = new OrderDetail(orderId, price, quantity);
                String json = objectMapper.writeValueAsString(detail);
                redisTemplate.opsForHash().put(ordersKey, String.valueOf(orderId), json);

                log.debug("添加买单：orderId={}, price={}, quantity={}", orderId, price, quantity);
            }
        } catch (Exception e) {
            log.error("添加买单失败：orderId={}, price={}, quantity={}", orderId, price, quantity, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 添加卖单
     * score = 价格 * 10000 + 时间戳（价格低优先，同价格时间早优先）
     */
    public void addAsk(Long exchangeId, String instrumentCode, BigDecimal price,
                       BigDecimal quantity, Long orderId) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String asksKey = getAsksKey(key);
        String ordersKey = getOrdersKey(key);
        RLock lock = getLock(key);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 计算 score：价格越低分数越低（越靠前），同价格时间越早分数越低
                double score = calculateAskScore(price, System.currentTimeMillis());

                // 添加到卖单 ZSet
                redisTemplate.opsForZSet().add(asksKey, String.valueOf(orderId), score);

                // 存储订单详情
                OrderDetail detail = new OrderDetail(orderId, price, quantity);
                String json = objectMapper.writeValueAsString(detail);
                redisTemplate.opsForHash().put(ordersKey, String.valueOf(orderId), json);

                log.debug("添加卖单：orderId={}, price={}, quantity={}", orderId, price, quantity);
            }
        } catch (Exception e) {
            log.error("添加卖单失败：orderId={}, price={}, quantity={}", orderId, price, quantity, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 计算买单 score
     * 价格越高越靠前，同价格时间越早越靠前
     */
    private double calculateBidScore(BigDecimal price, long timestamp) {
        // 价格 * 10000 + (Long.MAX_VALUE - timestamp) / 1000000
        // 这样价格高的在前，同价格时间早的在前
        return price.multiply(BigDecimal.valueOf(10000)).doubleValue() +
               (Double.MAX_VALUE - timestamp) / 1000000.0;
    }

    /**
     * 计算卖单 score
     * 价格越低越靠前，同价格时间越早越靠前
     */
    private double calculateAskScore(BigDecimal price, long timestamp) {
        // 价格 * 10000 + timestamp / 1000000
        // 这样价格低的在前，同价格时间早的在前
        return price.multiply(BigDecimal.valueOf(10000)).doubleValue() +
               timestamp / 1000000.0;
    }

    /**
     * 获取最佳买单（最高价）
     */
    public Long getBestBidOrderId(Long exchangeId, String instrumentCode) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String bidsKey = getBidsKey(key);

        Set<Object> results = redisTemplate.opsForZSet().range(bidsKey, 0, 0);
        if (results == null || results.isEmpty()) {
            return null;
        }

        return Long.valueOf(results.iterator().next().toString());
    }

    /**
     * 获取最佳卖单（最低价）
     */
    public Long getBestAskOrderId(Long exchangeId, String instrumentCode) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String asksKey = getAsksKey(key);

        Set<Object> results = redisTemplate.opsForZSet().range(asksKey, 0, 0);
        if (results == null || results.isEmpty()) {
            return null;
        }

        return Long.valueOf(results.iterator().next().toString());
    }

    /**
     * 获取订单详情
     */
    public OrderDetail getOrderDetail(Long exchangeId, String instrumentCode, Long orderId) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String ordersKey = getOrdersKey(key);

        Object json = redisTemplate.opsForHash().get(ordersKey, String.valueOf(orderId));
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
     * 移除订单
     */
    public void removeOrder(Long exchangeId, String instrumentCode, Long orderId) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String bidsKey = getBidsKey(key);
        String asksKey = getAsksKey(key);
        String ordersKey = getOrdersKey(key);
        RLock lock = getLock(key);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 从买卖 ZSet 中移除
                redisTemplate.opsForZSet().remove(bidsKey, String.valueOf(orderId));
                redisTemplate.opsForZSet().remove(asksKey, String.valueOf(orderId));
                // 从订单详情中移除
                redisTemplate.opsForHash().delete(ordersKey, String.valueOf(orderId));

                log.debug("移除订单：orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("移除订单失败：orderId={}", orderId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 更新订单数量
     */
    public void updateOrderQuantity(Long exchangeId, String instrumentCode,
                                     Long orderId, BigDecimal newQuantity) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String ordersKey = getOrdersKey(key);
        RLock lock = getLock(key);

        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                OrderDetail detail = getOrderDetail(exchangeId, instrumentCode, orderId);
                if (detail != null) {
                    detail.setQuantity(newQuantity);
                    String json = objectMapper.writeValueAsString(detail);
                    redisTemplate.opsForHash().put(ordersKey, String.valueOf(orderId), json);
                }
            }
        } catch (Exception e) {
            log.error("更新订单数量失败：orderId={}, newQuantity={}", orderId, newQuantity, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取前 N 档买单
     */
    public List<PriceLevel> getPriceLevels(Long exchangeId, String instrumentCode,
                                           String side, int depth) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String zSetKey = side.equals("bid") ? getBidsKey(key) : getAsksKey(key);
        String ordersKey = getOrdersKey(key);

        List<PriceLevel> levels = new ArrayList<>();

        Set<Object> orderIds;
        if (side.equals("bid")) {
            // 买单：从高到低取（分数高的在前）
            orderIds = redisTemplate.opsForZSet().reverseRange(zSetKey, 0, depth - 1);
        } else {
            // 卖单：从低到高取（分数低的在前）
            orderIds = redisTemplate.opsForZSet().range(zSetKey, 0, depth - 1);
        }

        if (orderIds == null || orderIds.isEmpty()) {
            return levels;
        }

        // 按价格聚合
        Map<BigDecimal, BigDecimal> priceQuantityMap = new LinkedHashMap<>();

        for (Object orderIdObj : orderIds) {
            Long orderId = Long.valueOf(orderIdObj.toString());
            OrderDetail detail = getOrderDetail(exchangeId, instrumentCode, orderId);
            if (detail != null) {
                BigDecimal price = detail.getPrice();
                BigDecimal quantity = detail.getQuantity();
                priceQuantityMap.merge(price, quantity, BigDecimal::add);
            }
        }

        // 转换为 PriceLevel 列表
        for (Map.Entry<BigDecimal, BigDecimal> entry : priceQuantityMap.entrySet()) {
            levels.add(new PriceLevel(entry.getKey(), entry.getValue()));
        }

        return levels;
    }

    /**
     * 查找可撮合的买单（价格 >= askPrice）
     */
    public List<OrderDetail> findMatchableBids(Long exchangeId, String instrumentCode,
                                                BigDecimal askPrice) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String bidsKey = getBidsKey(key);

        List<OrderDetail> matchableOrders = new ArrayList<>();

        // 获取所有分数 >= askPriceScore 的订单（价格 >= askPrice 的买单）
        double minScore = calculateBidScore(askPrice, Long.MAX_VALUE);
        Set<Object> results = redisTemplate.opsForZSet().rangeByScore(bidsKey, minScore, Double.MAX_VALUE);

        if (results != null) {
            for (Object orderIdObj : results) {
                Long orderId = Long.valueOf(orderIdObj.toString());
                OrderDetail detail = getOrderDetail(exchangeId, instrumentCode, orderId);
                if (detail != null && detail.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    matchableOrders.add(detail);
                }
            }
        }

        return matchableOrders;
    }

    /**
     * 查找可撮合的卖单（价格 <= bidPrice）
     */
    public List<OrderDetail> findMatchableAsks(Long exchangeId, String instrumentCode,
                                                BigDecimal bidPrice) {
        String key = getOrderBookKey(exchangeId, instrumentCode);
        String asksKey = getAsksKey(key);

        List<OrderDetail> matchableOrders = new ArrayList<>();

        // 获取所有分数 <= askPriceScore 的订单（价格 <= bidPrice 的卖单）
        double maxScore = calculateAskScore(bidPrice, Long.MAX_VALUE);
        Set<Object> results = redisTemplate.opsForZSet().rangeByScore(asksKey, 0, maxScore);

        if (results != null) {
            for (Object orderIdObj : results) {
                Long orderId = Long.valueOf(orderIdObj.toString());
                OrderDetail detail = getOrderDetail(exchangeId, instrumentCode, orderId);
                if (detail != null && detail.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    matchableOrders.add(detail);
                }
            }
        }

        return matchableOrders;
    }

    /**
     * 订单详情
     */
    @Data
    @AllArgsConstructor
    public static class OrderDetail {
        private Long orderId;
        private BigDecimal price;
        private BigDecimal quantity;
    }

    /**
     * 价格档位
     */
    @Data
    @AllArgsConstructor
    public static class PriceLevel {
        private BigDecimal price;
        private BigDecimal quantity;
    }
}
