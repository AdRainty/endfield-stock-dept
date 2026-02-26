package com.adrainty.stock.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final int code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }
    
    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }
}
