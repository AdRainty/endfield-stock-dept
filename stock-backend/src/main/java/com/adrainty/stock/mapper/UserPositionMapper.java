package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.UserPosition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户持仓 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface UserPositionMapper extends BaseMapper<UserPosition> {

    /**
     * 根据用户 ID 和交易所 ID 查找所有持仓
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @return 持仓列表
     */
    @Select("SELECT * FROM user_position WHERE user_id = #{userId} AND exchange_id = #{exchangeId}")
    List<UserPosition> findByUserIdAndExchangeId(@Param("userId") Long userId, @Param("exchangeId") Long exchangeId);

    /**
     * 根据用户 ID、交易所 ID 和品种代码查找持仓
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 持仓对象
     */
    @Select("SELECT * FROM user_position WHERE user_id = #{userId} AND exchange_id = #{exchangeId} AND instrument_code = #{instrumentCode}")
    UserPosition findByUserIdAndExchangeIdAndInstrumentCode(
        @Param("userId") Long userId, @Param("exchangeId") Long exchangeId, @Param("instrumentCode") String instrumentCode);

    /**
     * 根据用户 ID 查找所有持仓
     *
     * @param userId 用户 ID
     * @return 持仓列表
     */
    @Select("SELECT * FROM user_position WHERE user_id = #{userId}")
    List<UserPosition> findByUserId(@Param("userId") Long userId);
}
