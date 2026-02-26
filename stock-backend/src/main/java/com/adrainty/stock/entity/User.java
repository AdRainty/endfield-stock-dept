package com.adrainty.stock.entity;

import com.adrainty.stock.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class User extends BaseEntity {
    
    /**
     * 微信 OpenID
     */
    @Column(name = "wechat_openid", unique = true)
    private String wechatOpenid;
    
    /**
     * 用户昵称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    /**
     * 用户头像
     */
    @Column(name = "avatar", length = 255)
    private String avatar;
    
    /**
     * 用户角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private UserRole role = UserRole.USER;
    
    /**
     * 账户状态：1-正常 0-禁用
     */
    @Column(name = "status")
    private Integer status = 1;
    
    /**
     * 创建时来源 IP
     */
    @Column(name = "register_ip", length = 50)
    private String registerIp;
    
    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * 最后登录 IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;
}
