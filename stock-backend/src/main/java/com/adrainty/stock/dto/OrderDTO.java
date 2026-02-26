package com.adrainty.stock.dto;

import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.enums.OrderType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
public class OrderDTO {
    
    /**
     * 订单 ID
     */
    private Long id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 交易所 ID
     */
    private Long exchangeId;
    
    /**
     * 交易所名称
     */
    private String exchangeName;
    
    /**
     * 品种代码
     */
    private String instrumentCode;
    
    /**
     * 品种名称
     */
    private String instrumentName;
    
    /**
     * 订单类型
     */
    private OrderType orderType;
    
    /**
     * 委托价格
     */
    private BigDecimal price;
    
    /**
     * 委托数量
     */
    private BigDecimal quantity;
    
    /**
     * 已成交数量
     */
    private BigDecimal filledQuantity;
    
    /**
     * 未成交数量
     */
    private BigDecimal unfilledQuantity;
    
    /**
     * 成交金额
     */
    private BigDecimal filledAmount;
    
    /**
     * 订单状态
     */
    private OrderStatus status;
    
    /**
     * 委托时间
     */
    private LocalDateTime orderTime;
    
    /**
     * 成交时间
     */
    private LocalDateTime filledTime;
}
