package com.adrainty.stock.entity;

import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.enums.OrderType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("order_book")
public class Order extends BaseEntity {

    /**
     * 订单号（业务主键）
     */
    @TableField("order_no")
    private String orderNo;

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
     * 订单类型：BUY-买入 SELL-卖出
     */
    @TableField("order_type")
    private OrderType orderType;

    /**
     * 委托价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 委托数量
     */
    @TableField("quantity")
    private BigDecimal quantity;

    /**
     * 已成交数量
     */
    @TableField("filled_quantity")
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    /**
     * 未成交数量
     */
    @TableField("unfilled_quantity")
    private BigDecimal unfilledQuantity;

    /**
     * 成交金额
     */
    @TableField("filled_amount")
    private BigDecimal filledAmount = BigDecimal.ZERO;

    /**
     * 订单状态
     */
    @TableField("status")
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * 委托时间
     */
    @TableField("order_time")
    private LocalDateTime orderTime;

    /**
     * 成交时间
     */
    @TableField("filled_time")
    private LocalDateTime filledTime;

    /**
     * 撤单时间
     */
    @TableField("cancelled_time")
    private LocalDateTime cancelledTime;

    /**
     * 撤单原因
     */
    @TableField("cancel_reason")
    private String cancelReason;
}
