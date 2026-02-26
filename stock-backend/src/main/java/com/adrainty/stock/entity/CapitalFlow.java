package com.adrainty.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水实体类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@Entity
@Table(name = "capital_flow")
public class CapitalFlow extends BaseEntity {
    
    /**
     * 流水号
     */
    @Column(name = "flow_no", unique = true, length = 32, nullable = false)
    private String flowNo;
    
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
     * 流水类型：DEPOSIT-入金 WITHDRAW-出钱 TRADE-交易分配-分配
     */
    @Column(name = "flow_type", length = 20, nullable = false)
    private String flowType;
    
    /**
     * 变动金额（正数为收入，负数为支出）
     */
    @Column(name = "amount", precision = 20, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * 变动后余额
     */
    @Column(name = "balance_after", precision = 20, scale = 2, nullable = false)
    private BigDecimal balanceAfter;
    
    /**
     * 关联业务单号（订单号、交易流水号等）
     */
    @Column(name = "ref_no", length = 32)
    private String refNo;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 255)
    private String remark;
    
    /**
     * 操作时间
     */
    @Column(name = "operate_time")
    private LocalDateTime operateTime;
    
    /**
     * 操作人 ID（管理员操作时填写）
     */
    @Column(name = "operator_id")
    private Long operatorId;
}
