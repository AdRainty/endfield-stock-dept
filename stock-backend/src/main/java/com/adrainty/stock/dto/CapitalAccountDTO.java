package com.adrainty.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资金账户 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
public class CapitalAccountDTO {
    
    /**
     * 交易所 ID
     */
    private Long exchangeId;
    
    /**
     * 交易所名称
     */
    private String exchangeName;
    
    /**
     * 可用资金
     */
    private BigDecimal available;
    
    /**
     * 冻结资金
     */
    private BigDecimal frozen;
    
    /**
     * 总资产（可用 + 持仓市值）
     */
    private BigDecimal totalAsset;
    
    /**
     * 持仓市值
     */
    private BigDecimal positionValue;
    
    /**
     * 总盈亏
     */
    private BigDecimal totalProfitLoss;
}
