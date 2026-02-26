package com.adrainty.stock.enums;

/**
 * 订单状态枚举
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public enum OrderStatus {
    
    /**
     * 待成交
     */
    PENDING("PENDING", "待成交"),
    
    /**
     * 部分成交
     */
    PARTIALLY_FILLED("PARTIALLY_FILLED", "部分成交"),
    
    /**
     * 已成交
     */
    FILLED("FILLED", "已成交"),
    
    /**
     * 已撤单
     */
    CANCELLED("CANCELLED", "已撤单");
    
    private final String code;
    private final String name;
    
    OrderStatus(String code, String name) {
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
