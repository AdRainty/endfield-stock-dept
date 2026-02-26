package com.adrainty.stock.entity;

import com.adrainty.stock.enums.UserRole;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@TableName("sys_user")
public class User extends BaseEntity {

    /**
     * 微信 OpenID
     */
    @TableField("wechat_openid")
    private String wechatOpenid;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 用户头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 用户角色
     */
    @TableField("role")
    private UserRole role = UserRole.USER;

    /**
     * 账户状态：1-正常 0-禁用
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 创建时来源 IP
     */
    @TableField("register_ip")
    private String registerIp;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录 IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;
}
