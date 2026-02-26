package com.adrainty.stock.enums;

/**
 * 交易所代码枚举
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public enum ExchangeCode {
    
    /**
     * 四号谷底交易所
     */
    VALLEY("VALLEY", "四号谷底"),
    
    /**
     * 武陵交易所
     */
    WULING("WULING", "武陵");
    
    private final String code;
    private final String name;
    
    ExchangeCode(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据代码获取枚举
     * 
     * @param code 交易所代码
     * @return 交易所枚举
     */
    public static ExchangeCode fromCode(String code) {
        for (ExchangeCode e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown exchange code: " + code);
    }
}
