package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.enums.ExchangeCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交易所 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface ExchangeMapper extends BaseMapper<Exchange> {

    /**
     * 根据交易所代码查找交易所
     *
     * @param exchangeCode 交易所代码
     * @return 交易所对象
     */
    @Select("SELECT * FROM exchange WHERE exchange_code = #{exchangeCode}")
    Exchange findByExchangeCode(@Param("exchangeCode") ExchangeCode exchangeCode);

    /**
     * 检查交易所代码是否存在
     *
     * @param exchangeCode 交易所代码
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM exchange WHERE exchange_code = #{exchangeCode}")
    boolean existsByExchangeCode(@Param("exchangeCode") ExchangeCode exchangeCode);

    /**
     * 查找所有正常状态的交易所
     *
     * @param status 状态
     * @return 交易所列表
     */
    @Select("SELECT * FROM exchange WHERE status = #{status}")
    List<Exchange> findByStatus(@Param("status") Integer status);
}
