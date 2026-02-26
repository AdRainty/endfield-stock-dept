package com.adrainty.stock.enums;

/**
 * 用户角色枚举
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public enum UserRole {
    
    /**
     * 普通用户 - 可交易、查看行情和持仓
     */
    USER("USER", "普通用户"),
    
    /**
     * 管理员 - 可分配原能、管理用户
     */
    ADMIN("ADMIN", "管理员");
    
    private final String code;
    private final String name;
    
    UserRole(String code, String name) {
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
