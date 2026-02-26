package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("user_position")
public class UserPosition extends BaseEntity {

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
     * 品种代码
     */
    @TableField("instrument_code")
    private String instrumentCode;

    /**
     * 持仓数量
     */
    @TableField("quantity")
    private BigDecimal quantity = BigDecimal.ZERO;

    /**
     * 可用数量（未冻结的部分）
     */
    @TableField("available_quantity")
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    /**
     * 冻结数量（委托中的部分）
     */
    @TableField("frozen_quantity")
    private BigDecimal frozenQuantity = BigDecimal.ZERO;

    /**
     * 持仓成本价
     */
    @TableField("cost_price")
    private BigDecimal costPrice;

    /**
     * 持仓成本总额
     */
    @TableField("cost_amount")
    private BigDecimal costAmount = BigDecimal.ZERO;

    /**
     * 最新价
     */
    @TableField("latest_price")
    private BigDecimal latestPrice;

    /**
     * 持仓盈亏
     */
    @TableField("profit_loss")
    private BigDecimal profitLoss = BigDecimal.ZERO;

    /**
     * 盈亏比例 (%)
     */
    @TableField("profit_loss_rate")
    private BigDecimal profitLossRate;
}
