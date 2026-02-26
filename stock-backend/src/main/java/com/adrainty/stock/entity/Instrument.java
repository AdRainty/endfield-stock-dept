package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 调度券品种实体类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@TableName("instrument")
public class Instrument extends BaseEntity {

    /**
     * 品种代码
     */
    @TableField("instrument_code")
    private String instrumentCode;

    /**
     * 品种名称
     */
    @TableField("name")
    private String name;

    /**
     * 所属交易所 ID
     */
    @TableField("exchange_id")
    private Long exchangeId;

    /**
     * 当前价格
     */
    @TableField("current_price")
    private BigDecimal currentPrice;

    /**
     * 昨日收盘价
     */
    @TableField("prev_close_price")
    private BigDecimal prevClosePrice;

    /**
     * 今日开盘价
     */
    @TableField("open_price")
    private BigDecimal openPrice;

    /**
     * 最高价
     */
    @TableField("high_price")
    private BigDecimal highPrice;

    /**
     * 最低价
     */
    @TableField("low_price")
    private BigDecimal lowPrice;

    /**
     * 涨跌幅 (%)
     */
    @TableField("change_percent")
    private BigDecimal changePercent;

    /**
     * 涨跌额
     */
    @TableField("change_amount")
    private BigDecimal changeAmount;

    /**
     * 成交量
     */
    @TableField("volume")
    private Long volume = 0L;

    /**
     * 成交额
     */
    @TableField("turnover")
    private BigDecimal turnover;

    /**
     * 状态：1-交易中 0-休市
     */
    @TableField("status")
    private Integer status = 1;
}
