package com.adrainty.stock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员分配记录实体类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@Entity
@Table(name = "allocation_record")
public class AllocationRecord extends BaseEntity {
    
    /**
     * 分配单号
     */
    @Column(name = "allocation_no", unique = true, length = 32, nullable = false)
    private String allocationNo;
    
    /**
     * 目标用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 交易所 ID
     */
    @Column(name = "exchange_id", nullable = false)
    private Long exchangeId;
    
    /**
     * 分配金额
     */
    @Column(name = "amount", precision = 20, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * 分配后余额
     */
    @Column(name = "balance_after", precision = 20, scale = 2, nullable = false)
    private BigDecimal balanceAfter;
    
    /**
     * 分配原因
     */
    @Column(name = "reason", length = 255)
    private String reason;
    
    /**
     * 操作管理员 ID
     */
    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;
    
    /**
     * 操作时间
     */
    @Column(name = "operate_time")
    private LocalDateTime operateTime;
}
