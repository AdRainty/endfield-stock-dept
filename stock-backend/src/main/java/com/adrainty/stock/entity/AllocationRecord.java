package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("allocation_record")
public class AllocationRecord extends BaseEntity {

    /**
     * 分配单号
     */
    @TableField("allocation_no")
    private String allocationNo;

    /**
     * 目标用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 交易所 ID
     */
    @TableField("exchange_id")
    private Long exchangeId;

    /**
     * 分配金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 分配后余额
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /**
     * 分配原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 操作管理员 ID
     */
    @TableField("admin_user_id")
    private Long adminUserId;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private LocalDateTime operateTime;
}
