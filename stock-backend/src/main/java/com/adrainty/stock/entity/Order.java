package com.adrainty.stock.entity;

import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.enums.OrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 委托订单实体类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@Entity
@Table(name = "order_book")
public class Order extends BaseEntity {
    
    /**
     * 订单号（业务主键）
     */
    @Column(name = "order_no", unique = true, length = 32, nullable = false)
    private String orderNo;
    
    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 交易所 ID
     */
    @Column(name = "exchange_id", nullable = false)
    private Long exchangeId;
    
    /**
     * 品种代码
     */
    @Column(name = "instrument_code", length = 20, nullable = false)
    private String instrumentCode;
    
    /**
     * 订单类型：BUY-买入 SELL-卖出
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 10, nullable = false)
    private OrderType orderType;
    
    /**
     * 委托价格
     */
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    /**
     * 委托数量
     */
    @Column(name = "quantity", precision = 20, scale = 2, nullable = false)
    private BigDecimal quantity;
    
    /**
     * 已成交数量
     */
    @Column(name = "filled_quantity", precision = 20, scale = 2)
    private BigDecimal filledQuantity = BigDecimal.ZERO;
    
    /**
     * 未成交数量
     */
    @Column(name = "unfilled_quantity", precision = 20, scale = 2)
    private BigDecimal unfilledQuantity;
    
    /**
     * 成交金额
     */
    @Column(name = "filled_amount", precision = 20, scale = 2)
    private BigDecimal filledAmount = BigDecimal.ZERO;
    
    /**
     * 订单状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    /**
     * 委托时间
     */
    @Column(name = "order_time")
    private LocalDateTime orderTime;
    
    /**
     * 成交时间
     */
    @Column(name = "filled_time")
    private LocalDateTime filledTime;
    
    /**
     * 撤单时间
     */
    @Column(name = "cancelled_time")
    private LocalDateTime cancelledTime;
    
    /**
     * 撤单原因
     */
    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;
}
