package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.TradeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易记录 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface TradeRecordMapper extends BaseMapper<TradeRecord> {

    /**
     * 根据用户 ID 查找交易记录（作为买方）
     *
     * @param userId 用户 ID
     * @return 交易记录列表
     */
    @Select("SELECT * FROM trade_record WHERE buyer_user_id = #{userId} ORDER BY trade_time DESC")
    List<TradeRecord> findByBuyerUserIdOrderByTradeTimeDesc(@Param("userId") Long userId);

    /**
     * 根据用户 ID 查找交易记录（作为卖方）
     *
     * @param userId 用户 ID
     * @return 交易记录列表
     */
    @Select("SELECT * FROM trade_record WHERE seller_user_id = #{userId} ORDER BY trade_time DESC")
    List<TradeRecord> findBySellerUserIdOrderByTradeTimeDesc(@Param("userId") Long userId);

    /**
     * 根据交易所 ID 和品种代码查找交易记录
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 交易记录列表
     */
    @Select("SELECT * FROM trade_record WHERE exchange_id = #{exchangeId} AND instrument_code = #{instrumentCode} ORDER BY trade_time DESC")
    List<TradeRecord> findByExchangeIdAndInstrumentCodeOrderByTradeTimeDesc(
        @Param("exchangeId") Long exchangeId, @Param("instrumentCode") String instrumentCode);
}
