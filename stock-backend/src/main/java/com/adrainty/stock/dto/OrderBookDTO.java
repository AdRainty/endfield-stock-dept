package com.adrainty.stock.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 档口订单 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
public class OrderBookDTO {
    
    /**
     * 交易所 ID
     */
    private Long exchangeId;
    
    /**
     * 品种代码
     */
    private String instrumentCode;
    
    /**
     * 买一档口
     */
    private List<PriceLevel> bids;
    
    /**
     * 卖一档口
     */
    private List<PriceLevel> asks;
    
    /**
     * 最新价
     */
    private BigDecimal latestPrice;
    
    /**
     * 涨跌幅
     */
    private BigDecimal changePercent;
    
    /**
     * 价格档位
     */
    @Data
    public static class PriceLevel {
        /**
         * 价格
         */
        private BigDecimal price;
        
        /**
         * 数量
         */
        private BigDecimal quantity;
        
        /**
         * 订单数
         */
        private Integer orderCount;
        
        public PriceLevel() {}
        
        public PriceLevel(BigDecimal price, BigDecimal quantity) {
            this.price = price;
            this.quantity = quantity;
            this.orderCount = 1;
        }
    }
}
