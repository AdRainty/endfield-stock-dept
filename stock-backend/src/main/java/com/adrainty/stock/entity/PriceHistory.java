package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格历史实体类（用于 K 线数据）
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@TableName("price_history")
public class PriceHistory extends BaseEntity {

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
     * K 线周期：1m-1 分钟 5m-5 分钟 15m-15 分钟 1h-1 小时 1d-1 天
     */
    @TableField("period")
    private String period;

    /**
     * 交易时间
     */
    @TableField("trade_time")
    private LocalDateTime tradeTime;

    /**
     * 开盘价
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
     * 收盘价
     */
    @TableField("close_price")
    private BigDecimal closePrice;

    /**
     * 成交量
     */
    @TableField("volume")
    private BigDecimal volume;

    /**
     * 成交额
     */
    @TableField("turnover")
    private BigDecimal turnover;
}
