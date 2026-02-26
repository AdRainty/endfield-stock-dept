package com.adrainty.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分配原能请求 DTO
 *
 * @author adrainty
 * @since 2026-02-27
 */
@Data
public class AllocateRequest {

    /**
     * 目标用户 ID
     */
    private Long targetUserId;

    /**
     * 交易所 ID
     */
    private Long exchangeId;

    /**
     * 分配金额
     */
    private BigDecimal amount;

    /**
     * 分配原因
     */
    private String reason;
}
