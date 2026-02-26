package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.Instrument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 调度券品种 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface InstrumentMapper extends BaseMapper<Instrument> {

    /**
     * 根据品种代码查找品种
     *
     * @param instrumentCode 品种代码
     * @return 品种对象
     */
    @Select("SELECT * FROM instrument WHERE instrument_code = #{instrumentCode}")
    Instrument findByInstrumentCode(@Param("instrumentCode") String instrumentCode);

    /**
     * 检查品种代码是否存在
     *
     * @param instrumentCode 品种代码
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM instrument WHERE instrument_code = #{instrumentCode}")
    boolean existsByInstrumentCode(@Param("instrumentCode") String instrumentCode);

    /**
     * 根据交易所 ID 查找所有品种
     *
     * @param exchangeId 交易所 ID
     * @return 品种列表
     */
    @Select("SELECT * FROM instrument WHERE exchange_id = #{exchangeId}")
    List<Instrument> findByExchangeId(@Param("exchangeId") Long exchangeId);

    /**
     * 根据交易所 ID 和状态查找品种
     *
     * @param exchangeId 交易所 ID
     * @param status 状态
     * @return 品种列表
     */
    @Select("SELECT * FROM instrument WHERE exchange_id = #{exchangeId} AND status = #{status}")
    List<Instrument> findByExchangeIdAndStatus(@Param("exchangeId") Long exchangeId, @Param("status") Integer status);
}
