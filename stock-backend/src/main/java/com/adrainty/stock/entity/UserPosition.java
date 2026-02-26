package com.adrainty.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 用户持仓实体类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@Entity
@Table(name = "user_position", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exchange_id", "instrument_code"}))
public class UserPosition extends BaseEntity {
    
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
     * 持仓数量
     */
    @Column(name = "quantity", precision = 20, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;
    
    /**
     * 可用数量（未冻结的部分）
     */
    @Column(name = "available_quantity", precision = 20, scale = 2)
    private BigDecimal availableQuantity = BigDecimal.ZERO;
    
    /**
     * 冻结数量（委托中的部分）
     */
    @Column(name = "frozen_quantity", precision = 20, scale = 2)
    private BigDecimal frozenQuantity = BigDecimal.ZERO;
    
    /**
     * 持仓成本价
     */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;
    
    /**
     * 持仓成本总额
     */
    @Column(name = "cost_amount", precision = 20, scale = 2)
    private BigDecimal costAmount = BigDecimal.ZERO;
    
    /**
     * 最新价
     */
    @Column(name = "latest_price", precision = 10, scale = 2)
    private BigDecimal latestPrice;
    
    /**
     * 持仓盈亏
     */
    @Column(name = "profit_loss", precision = 20, scale = 2)
    private BigDecimal profitLoss = BigDecimal.ZERO;
    
    /**
     * 盈亏比例 (%)
     */
    @Column(name = "profit_loss_rate", precision = 6, scale = 4)
    private BigDecimal profitLossRate;
}
