package com.adrainty.stock.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "instrument")
public class Instrument extends BaseEntity {
    
    /**
     * 品种代码
     */
    @Column(name = "instrument_code", unique = true, length = 20)
    private String instrumentCode;
    
    /**
     * 品种名称
     */
    @Column(name = "name", length = 50)
    private String name;
    
    /**
     * 所属交易所 ID
     */
    @Column(name = "exchange_id")
    private Long exchangeId;
    
    /**
     * 当前价格
     */
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;
    
    /**
     * 昨日收盘价
     */
    @Column(name = "prev_close_price", precision = 10, scale = 2)
    private BigDecimal prevClosePrice;
    
    /**
     * 今日开盘价
     */
    @Column(name = "open_price", precision = 10, scale = 2)
    private BigDecimal openPrice;
    
    /**
     * 最高价
     */
    @Column(name = "high_price", precision = 10, scale = 2)
    private BigDecimal highPrice;
    
    /**
     * 最低价
     */
    @Column(name = "low_price", precision = 10, scale = 2)
    private BigDecimal lowPrice;
    
    /**
     * 涨跌幅 (%)
     */
    @Column(name = "change_percent", precision = 6, scale = 4)
    private BigDecimal changePercent;
    
    /**
     * 涨跌额
     */
    @Column(name = "change_amount", precision = 10, scale = 2)
    private BigDecimal changeAmount;
    
    /**
     * 成交量
     */
    @Column(name = "volume")
    private Long volume = 0L;
    
    /**
     * 成交额
     */
    @Column(name = "turnover", precision = 20, scale = 2)
    private BigDecimal turnover;
    
    /**
     * 状态：1-交易中 0-休市
     */
    @Column(name = "status")
    private Integer status = 1;
}
