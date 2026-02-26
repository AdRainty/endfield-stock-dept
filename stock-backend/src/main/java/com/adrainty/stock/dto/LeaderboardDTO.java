package com.adrainty.stock.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 排行榜用户 DTO
 *
 * @author adrainty
 * @since 2026-02-27
 */
@Data
public class LeaderboardDTO {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 收益金额
     */
    private BigDecimal profitLoss;

    /**
     * 收益率 (%)
     */
    private BigDecimal returnRate;
}
