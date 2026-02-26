package com.adrainty.stock.service;

import com.adrainty.stock.dto.CapitalAccountDTO;

import java.math.BigDecimal;

/**
 * 资金服务接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public interface CapitalService {
    
    /**
     * 获取用户资金账户
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @return 资金账户信息
     */
    CapitalAccountDTO getAccount(Long userId, Long exchangeId);
    
    /**
     * 冻结资金
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param amount 金额
     * @param refNo 关联单号
     * @return 是否成功
     */
    boolean freezeCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo);
    
    /**
     * 解冻资金
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param amount 金额
     * @param refNo 关联单号
     * @return 是否成功
     */
    boolean unfreezeCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo);
    
    /**
     * 扣除资金
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param amount 金额
     * @param refNo 关联单号
     * @param remark 备注
     * @return 是否成功
     */
    boolean deductCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo, String remark);
    
    /**
     * 增加资金
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param amount 金额
     * @param refNo 关联单号
     * @param remark 备注
     * @return 是否成功
     */
    boolean addCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo, String remark);
    
    /**
     * 初始化用户资金
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param initialAmount 初始金额
     */
    void initCapital(Long userId, Long exchangeId, BigDecimal initialAmount);

    /**
     * 获取可用资金（内部使用）
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @return 可用资金
     */
    BigDecimal getAvailableCapital(Long userId, Long exchangeId);
}
