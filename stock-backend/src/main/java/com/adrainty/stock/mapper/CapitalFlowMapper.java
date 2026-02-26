package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.CapitalFlow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资金流水 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface CapitalFlowMapper extends BaseMapper<CapitalFlow> {

    /**
     * 根据用户 ID 查找资金流水
     *
     * @param userId 用户 ID
     * @return 资金流水列表
     */
    @Select("SELECT * FROM capital_flow WHERE user_id = #{userId} ORDER BY operate_time DESC")
    List<CapitalFlow> findByUserIdOrderByOperateTimeDesc(@Param("userId") Long userId);

    /**
     * 根据用户 ID 和交易所 ID 查找资金流水
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @return 资金流水列表
     */
    @Select("SELECT * FROM capital_flow WHERE user_id = #{userId} AND exchange_id = #{exchangeId} ORDER BY operate_time DESC")
    List<CapitalFlow> findByUserIdAndExchangeIdOrderByOperateTimeDesc(
        @Param("userId") Long userId, @Param("exchangeId") Long exchangeId);

    /**
     * 根据流水号查找资金流水
     *
     * @param flowNo 流水号
     * @return 资金流水对象
     */
    @Select("SELECT * FROM capital_flow WHERE flow_no = #{flowNo}")
    CapitalFlow findByFlowNo(@Param("flowNo") String flowNo);
}
