package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("capital_flow")
public class CapitalFlow extends BaseEntity {

    /**
     * 流水号
     */
    @TableField("flow_no")
    private String flowNo;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 交易所 ID
     */
    @TableField("exchange_id")
    private Long exchangeId;

    /**
     * 流水类型：DEPOSIT-入金 WITHDRAW-出钱 TRADE-交易分配 - 分配
     */
    @TableField("flow_type")
    private String flowType;

    /**
     * 变动金额（正数为收入，负数为支出）
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 变动后余额
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /**
     * 关联业务单号（订单号、交易流水号等）
     */
    @TableField("ref_no")
    private String refNo;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private LocalDateTime operateTime;

    /**
     * 操作人 ID（管理员操作时填写）
     */
    @TableField("operator_id")
    private Long operatorId;
}
