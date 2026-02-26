package com.adrainty.stock.entity;

import com.adrainty.stock.enums.OrderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录实体类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@Entity
@Table(name = "trade_record")
public class TradeRecord extends BaseEntity {
    
    /**
     * 交易流水号
     */
    @Column(name = "trade_no", unique = true, length = 32, nullable = false)
    private String tradeNo;
    
    /**
     * 订单号
     */
    @Column(name = "order_no", length = 32, nullable = false)
    private String orderNo;
    
    /**
     * 买方用户 ID
     */
    @Column(name = "buyer_user_id", nullable = false)
    private Long buyerUserId;
    
    /**
     * 卖方用户 ID
     */
    @Column(name = "seller_user_id", nullable = false)
    private Long sellerUserId;
    
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
     * 交易类型（从买方角度）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 10, nullable = false)
    private OrderType orderType;
    
    /**
     * 成交价格
     */
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    /**
     * 成交数量
     */
    @Column(name = "quantity", precision = 20, scale = 2, nullable = false)
    private BigDecimal quantity;
    
    /**
     * 成交金额
     */
    @Column(name = "amount", precision = 20, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * 手续费
     */
    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;
    
    /**
     * 交易时间
     */
    @Column(name = "trade_time")
    private LocalDateTime tradeTime;
}
