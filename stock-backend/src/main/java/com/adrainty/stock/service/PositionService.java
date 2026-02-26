package com.adrainty.stock.service;

import com.adrainty.stock.dto.PositionDTO;

import java.util.List;

/**
 * 持仓服务接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
public interface PositionService {

    /**
     * 获取用户持仓列表
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @return 持仓列表
     */
    List<PositionDTO> getUserPositions(Long userId, Long exchangeId);

    /**
     * 获取用户所有交易所的持仓列表
     *
     * @param userId 用户 ID
     * @return 持仓列表
     */
    List<PositionDTO> getAllUserPositions(Long userId);

    /**
     * 获取用户单个持仓
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 持仓 DTO
     */
    PositionDTO getPosition(Long userId, Long exchangeId, String instrumentCode);

    /**
     * 初始化用户持仓
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     */
    void initPosition(Long userId, Long exchangeId);
}
