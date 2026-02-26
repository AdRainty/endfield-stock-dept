package com.adrainty.stock.service;

import com.adrainty.stock.dto.OrderBookDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 档口订单服务接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public interface OrderBookService {
    
    /**
     * 获取档口数据
     * 
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 档口数据
     */
    OrderBookDTO getOrderBook(Long exchangeId, String instrumentCode);
    
    /**
     * 添加买单到档口
     * 
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param price 价格
     * @param quantity 数量
     * @param orderId 订单 ID
     * @return 成交数量
     */
    BigDecimal addBidOrder(Long exchangeId, String instrumentCode, BigDecimal price, BigDecimal quantity, Long orderId);
    
    /**
     * 添加卖单到档口
     * 
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param price 价格
     * @param quantity 数量
     * @param orderId 订单 ID
     * @return 成交数量
     */
    BigDecimal addAskOrder(Long exchangeId, String instrumentCode, BigDecimal price, BigDecimal quantity, Long orderId);
    
    /**
     * 从档口移除订单
     * 
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param orderId 订单 ID
     * @param quantity 数量
     */
    void removeOrder(Long exchangeId, String instrumentCode, Long orderId, BigDecimal quantity);
    
    /**
     * 获取所有档口
     * 
     * @return 所有品种的档口数据
     */
    List<OrderBookDTO> getAllOrderBooks();
}
