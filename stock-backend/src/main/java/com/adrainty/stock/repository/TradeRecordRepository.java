package com.adrainty.stock.repository;

import com.adrainty.stock.entity.TradeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 交易记录数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface TradeRecordRepository extends JpaRepository<TradeRecord, Long> {
    
    /**
     * 根据用户 ID 查找交易记录（作为买方）
     * 
     * @param userId 用户 ID
     * @return 交易记录列表
     */
    List<TradeRecord> findByBuyerUserIdOrderByTradeTimeDesc(Long userId);
    
    /**
     * 根据用户 ID 查找交易记录（作为卖方）
     * 
     * @param userId 用户 ID
     * @return 交易记录列表
     */
    List<TradeRecord> findBySellerUserIdOrderByTradeTimeDesc(Long userId);
    
    /**
     * 根据交易所 ID 和品种代码查找交易记录
     * 
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 交易记录列表
     */
    List<TradeRecord> findByExchangeIdAndInstrumentCodeOrderByTradeTimeDesc(
        Long exchangeId, String instrumentCode);
}
