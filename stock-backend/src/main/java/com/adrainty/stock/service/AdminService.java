package com.adrainty.stock.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
public interface AdminService {

    /**
     * 分配资金给用户
     *
     * @param adminUserId 管理员 ID
     * @param targetUserId 目标用户 ID
     * @param exchangeId 交易所 ID
     * @param amount 分配金额
     * @param reason 分配原因
     */
    void allocateCapital(Long adminUserId, Long targetUserId, Long exchangeId, BigDecimal amount, String reason);

    /**
     * 获取用户列表
     *
     * @return 用户列表
     */
    List<Map<String, Object>> getUserList();

    /**
     * 获取用户详情（包含资金信息）
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    Map<String, Object> getUserDetail(Long userId);

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getStatistics();

    /**
     * 更新用户状态
     *
     * @param userId 用户 ID
     * @param status 状态
     */
    void updateUserStatus(Long userId, Integer status);

    // ==================== 交易所管理 ====================

    /**
     * 获取交易所列表
     *
     * @return 交易所列表
     */
    List<Map<String, Object>> getExchangeList();

    /**
     * 添加交易所
     *
     * @param name 交易所名称
     * @param code 交易所代码
     * @param description 描述
     */
    void addExchange(String name, String code, String description);

    /**
     * 更新交易所
     *
     * @param exchangeId 交易所 ID
     * @param name 交易所名称
     * @param code 交易所代码
     * @param description 描述
     */
    void updateExchange(Long exchangeId, String name, String code, String description);

    /**
     * 更新交易所状态
     *
     * @param exchangeId 交易所 ID
     * @param status 状态
     */
    void updateExchangeStatus(Long exchangeId, Integer status);

    // ==================== 品种管理 ====================

    /**
     * 获取品种列表
     *
     * @return 品种列表
     */
    List<Map<String, Object>> getInstrumentList();

    /**
     * 添加品种
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param name 品种名称
     * @param type 品种类型
     */
    void addInstrument(Long exchangeId, String instrumentCode, String name, String type);

    /**
     * 更新品种
     *
     * @param instrumentId 品种 ID
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param name 品种名称
     * @param type 品种类型
     */
    void updateInstrument(Long instrumentId, Long exchangeId, String instrumentCode, String name, String type);

    /**
     * 更新品种状态
     *
     * @param instrumentId 品种 ID
     * @param status 状态
     */
    void updateInstrumentStatus(Long instrumentId, Integer status);
}
