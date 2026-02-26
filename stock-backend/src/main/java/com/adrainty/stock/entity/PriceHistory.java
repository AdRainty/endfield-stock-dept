package com.adrainty.stock.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "price_history",
    uniqueConstraints = @UniqueConstraint(columnNames = {"exchange_id", "instrument_code", "trade_time"}))
public class PriceHistory extends BaseEntity {
    
    /**
     * 交易所 ID
     */
    @Column(name = "exchange_id", nullable = false)
    private Long exchangeId;
    
    /**
     * 品种代码
     */
    @Column(name = "instrument_code", length = 20, nullable = false)
    private String instrumentCode;
    
    /**
     * K 线周期：1m-1 分钟 5m-5 分钟 15m-15 分钟 1h-1 小时 1d-1 天
     */
    @Column(name = "period", length = 10, nullable = false)
    private String period;
    
    /**
     * 交易时间
     */
    @Column(name = "trade_time", nullable = false)
    private LocalDateTime tradeTime;
    
    /**
     * 开盘价
     */
    @Column(name = "open_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal openPrice;
    
    /**
     * 最高价
     */
    @Column(name = "high_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal highPrice;
    
    /**
     * 最低价
     */
    @Column(name = "low_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal lowPrice;
    
    /**
     * 收盘价
     */
    @Column(name = "close_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal closePrice;
    
    /**
     * 成交量
     */
    @Column(name = "volume", precision = 20, scale = 2)
    private BigDecimal volume;
    
    /**
     * 成交额
     */
    @Column(name = "turnover", precision = 20, scale = 2)
    private BigDecimal turnover;
}
