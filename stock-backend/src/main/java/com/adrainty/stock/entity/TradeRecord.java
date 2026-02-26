package com.adrainty.stock.entity;

import com.adrainty.stock.enums.OrderType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("trade_record")
public class TradeRecord extends BaseEntity {

    /**
     * 交易流水号
     */
    @TableField("trade_no")
    private String tradeNo;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 买方用户 ID
     */
    @TableField("buyer_user_id")
    private Long buyerUserId;

    /**
     * 卖方用户 ID
     */
    @TableField("seller_user_id")
    private Long sellerUserId;

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
     * 交易类型（从买方角度）
     */
    @TableField("order_type")
    private OrderType orderType;

    /**
     * 成交价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 成交数量
     */
    @TableField("quantity")
    private BigDecimal quantity;

    /**
     * 成交金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 手续费
     */
    @TableField("fee")
    private BigDecimal fee = BigDecimal.ZERO;

    /**
     * 交易时间
     */
    @TableField("trade_time")
    private LocalDateTime tradeTime;
}
