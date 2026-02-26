package com.adrainty.stock.enums;

/**
 * 订单类型枚举
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public enum OrderType {
    
    /**
     * 买入
     */
    BUY("BUY", "买入"),
    
    /**
     * 卖出
     */
    SELL("SELL", "卖出");
    
    private final String code;
    private final String name;
    
    OrderType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
}
