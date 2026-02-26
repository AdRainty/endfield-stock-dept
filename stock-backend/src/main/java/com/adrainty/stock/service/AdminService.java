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
     * 分配原能给用户
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
     * 获取用户详情
     * 
     * @param userId 用户 ID
     * @return 用户详情
     */
    Map<String, Object> getUserDetail(Long userId);
    
    /**
     * 获取分配记录
     * 
     * @param userId 用户 ID（可选）
     * @return 分配记录列表
     */
    List<Map<String, Object>> getAllocationRecords(Long userId);
    
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
}
