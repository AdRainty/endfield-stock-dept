package com.adrainty.stock.repository;

import com.adrainty.stock.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格历史数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    
    /**
     * 根据交易所、品种和周期查找价格历史
     * 
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param period 周期
     * @return 价格历史列表
     */
    List<PriceHistory> findByExchangeIdAndInstrumentCodeAndPeriodOrderByTradeTimeDesc(
        Long exchangeId, String instrumentCode, String period);
    
    /**
     * 根据交易所、品种、周期和时间范围查找价格历史
     * 
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param period 周期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 价格历史列表
     */
    List<PriceHistory> findByExchangeIdAndInstrumentCodeAndPeriodAndTradeTimeBetweenOrderByTradeTimeAsc(
        Long exchangeId, String instrumentCode, String period, LocalDateTime startTime, LocalDateTime endTime);
}
