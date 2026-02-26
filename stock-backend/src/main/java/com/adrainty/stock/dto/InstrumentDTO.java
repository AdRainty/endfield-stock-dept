package com.adrainty.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 品种行情 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
public class InstrumentDTO {
    
    /**
     * 品种 ID
     */
    private Long id;
    
    /**
     * 品种代码
     */
    private String instrumentCode;
    
    /**
     * 品种名称
     */
    private String name;
    
    /**
     * 交易所 ID
     */
    private Long exchangeId;
    
    /**
     * 交易所名称
     */
    private String exchangeName;
    
    /**
     * 当前价格
     */
    private BigDecimal currentPrice;
    
    /**
     * 昨日收盘价
     */
    private BigDecimal prevClosePrice;
    
    /**
     * 今日开盘价
     */
    private BigDecimal openPrice;
    
    /**
     * 最高价
     */
    private BigDecimal highPrice;
    
    /**
     * 最低价
     */
    private BigDecimal lowPrice;
    
    /**
     * 涨跌幅 (%)
     */
    private BigDecimal changePercent;
    
    /**
     * 涨跌额
     */
    private BigDecimal changeAmount;
    
    /**
     * 成交量
     */
    private Long volume;
    
    /**
     * 成交额
     */
    private BigDecimal turnover;
    
    /**
     * 状态
     */
    private Integer status;
}
