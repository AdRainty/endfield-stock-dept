package com.adrainty.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 微信 OpenID
     */
    private String openid;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 访问 Token
     */
    private String token;
    
    /**
     * 是否为新用户
     */
    private boolean newUser;
}
