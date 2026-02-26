package com.adrainty.stock.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 订单簿数据结构
 * 维护买卖档口数据
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
public class OrderBook {
    
    /**
     * 买单档口：价格 -> 数量（价格从高到低）
     */
    private final NavigableMap<BigDecimal, OrderLevel> bids = 
        new ConcurrentSkipListMap<>(Collections.reverseOrder());
    
    /**
     * 卖单档口：价格 -> 数量（价格从低到高）
     */
    private final NavigableMap<BigDecimal, OrderLevel> asks = 
        new ConcurrentSkipListMap<>();
    
    /**
     * 订单映射：订单 ID -> (价格，数量)
     */
    private final Map<Long, PriceQuantity> orderMap = new ConcurrentHashMap<>();
    
    /**
     * 交易所 ID
     */
    private final Long exchangeId;
    
    /**
     * 品种代码
     */
    private final String instrumentCode;
    
    public OrderBook(Long exchangeId, String instrumentCode) {
        this.exchangeId = exchangeId;
        this.instrumentCode = instrumentCode;
    }
    
    /**
     * 添加买单
     */
    public synchronized void addBid(BigDecimal price, BigDecimal quantity, Long orderId) {
        OrderLevel level = bids.computeIfAbsent(price, k -> new OrderLevel(price));
        level.addQuantity(quantity);
        orderMap.put(orderId, new PriceQuantity(price, quantity));
    }
    
    /**
     * 添加卖单
     */
    public synchronized void addAsk(BigDecimal price, BigDecimal quantity, Long orderId) {
        OrderLevel level = asks.computeIfAbsent(price, k -> new OrderLevel(price));
        level.addQuantity(quantity);
        orderMap.put(orderId, new PriceQuantity(price, quantity));
    }
    
    /**
     * 移除订单
     */
    public synchronized void removeOrder(Long orderId, BigDecimal quantity) {
        PriceQuantity pq = orderMap.get(orderId);
        if (pq == null) return;
        
        BigDecimal price = pq.price;
        BigDecimal remainingQuantity = pq.quantity.subtract(quantity);
        
        if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            orderMap.remove(orderId);
            if (bids.containsKey(price)) {
                OrderLevel level = bids.get(price);
                level.reduceQuantity(pq.quantity);
                if (level.getQuantity().compareTo(BigDecimal.ZERO) <= 0) bids.remove(price);
            }
            if (asks.containsKey(price)) {
                OrderLevel level = asks.get(price);
                level.reduceQuantity(pq.quantity);
                if (level.getQuantity().compareTo(BigDecimal.ZERO) <= 0) asks.remove(price);
            }
        } else {
            orderMap.put(orderId, new PriceQuantity(price, remainingQuantity));
            if (bids.containsKey(price)) bids.get(price).reduceQuantity(quantity);
            if (asks.containsKey(price)) asks.get(price).reduceQuantity(quantity);
        }
    }
    
    /**
     * 获取最佳买价
     */
    public synchronized BigDecimal getBestBid() {
        return bids.isEmpty() ? null : bids.firstKey();
    }
    
    /**
     * 获取最佳卖价
     */
    public synchronized BigDecimal getBestAsk() {
        return asks.isEmpty() ? null : asks.firstKey();
    }
    
    /**
     * 获取前 N 档买单
     */
    public synchronized List<OrderLevel> getBids(int depth) {
        return new ArrayList<>(bids.values()).stream().limit(depth).toList();
    }
    
    /**
     * 获取前 N 档卖单
     */
    public synchronized List<OrderLevel> getAsks(int depth) {
        return new ArrayList<>(asks.values()).stream().limit(depth).toList();
    }
    
    /**
     * 查找可以撮合的买单（价格 >= 卖价）
     */
    public synchronized NavigableMap<BigDecimal, OrderLevel> findMatchableBids(BigDecimal askPrice) {
        return bids.subMap(askPrice, true, bids.lastKey(), true);
    }
    
    /**
     * 查找可以撮合的卖单（价格 <= 买价）
     */
    public synchronized NavigableMap<BigDecimal, OrderLevel> findMatchableAsks(BigDecimal bidPrice) {
        return asks.subMap(asks.firstKey(), true, bidPrice, true);
    }
    
    /**
     * 订单档位
     */
    @Data
    public static class OrderLevel {
        private final BigDecimal price;
        private BigDecimal quantity = BigDecimal.ZERO;
        private int orderCount = 1;
        
        public OrderLevel(BigDecimal price) {
            this.price = price;
        }
        
        public synchronized void addQuantity(BigDecimal qty) {
            this.quantity = this.quantity.add(qty);
            this.orderCount++;
        }
        
        public synchronized void reduceQuantity(BigDecimal qty) {
            this.quantity = this.quantity.subtract(qty);
        }
    }
    
    /**
     * 价格 - 数量对
     */
    @Data
    @AllArgsConstructor
    public static class PriceQuantity {
        private final BigDecimal price;
        private final BigDecimal quantity;
    }
}
