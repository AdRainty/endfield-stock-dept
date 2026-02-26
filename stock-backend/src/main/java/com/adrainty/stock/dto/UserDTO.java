package com.adrainty.stock.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户信息 DTO
 *
 * @author adrainty
 * @since 2026-02-27
 */
@Data
public class UserDTO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 微信 OpenID
     */
    private String wechatOpenid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 可用资金
     */
    private BigDecimal availableCapital;

    /**
     * 锁定资金
     */
    private BigDecimal lockedCapital;

    /**
     * 总资金
     */
    private BigDecimal totalCapital;
}
