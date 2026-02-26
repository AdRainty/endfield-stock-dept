package com.adrainty.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 持仓 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
public class PositionDTO {
    
    /**
     * 持仓 ID
     */
    private Long id;
    
    /**
     * 交易所 ID
     */
    private Long exchangeId;
    
    /**
     * 交易所名称
     */
    private String exchangeName;
    
    /**
     * 品种代码
     */
    private String instrumentCode;
    
    /**
     * 品种名称
     */
    private String instrumentName;
    
    /**
     * 持仓数量
     */
    private BigDecimal quantity;
    
    /**
     * 可用数量
     */
    private BigDecimal availableQuantity;
    
    /**
     * 冻结数量
     */
    private BigDecimal frozenQuantity;
    
    /**
     * 持仓成本价
     */
    private BigDecimal costPrice;
    
    /**
     * 持仓成本总额
     */
    private BigDecimal costAmount;
    
    /**
     * 最新价
     */
    private BigDecimal latestPrice;
    
    /**
     * 持仓盈亏
     */
    private BigDecimal profitLoss;
    
    /**
     * 盈亏比例 (%)
     */
    private BigDecimal profitLossRate;
    
    /**
     * 持仓市值
     */
    private BigDecimal marketValue;
}
