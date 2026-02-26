package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.PriceHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格历史 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface PriceHistoryMapper extends BaseMapper<PriceHistory> {

    /**
     * 根据交易所、品种和周期查找价格历史
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param period 周期
     * @return 价格历史列表
     */
    @Select("SELECT * FROM price_history WHERE exchange_id = #{exchangeId} AND instrument_code = #{instrumentCode} AND period = #{period} ORDER BY trade_time DESC")
    List<PriceHistory> findByExchangeIdAndInstrumentCodeAndPeriodOrderByTradeTimeDesc(
        @Param("exchangeId") Long exchangeId, @Param("instrumentCode") String instrumentCode, @Param("period") String period);

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
    @Select("SELECT * FROM price_history WHERE exchange_id = #{exchangeId} AND instrument_code = #{instrumentCode} AND period = #{period} AND trade_time BETWEEN #{startTime} AND #{endTime} ORDER BY trade_time ASC")
    List<PriceHistory> findByExchangeIdAndInstrumentCodeAndPeriodAndTradeTimeBetweenOrderByTradeTimeAsc(
        @Param("exchangeId") Long exchangeId, @Param("instrumentCode") String instrumentCode,
        @Param("period") String period, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
