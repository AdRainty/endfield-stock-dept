package com.adrainty.stock.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
public class LoginRequest {
    
    /**
     * 微信登录 code
     */
    private String code;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
}
