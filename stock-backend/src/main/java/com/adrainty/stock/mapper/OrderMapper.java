package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.Order;
import com.adrainty.stock.enums.OrderStatus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 委托订单 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单号查找订单
     *
     * @param orderNo 订单号
     * @return 订单对象
     */
    @Select("SELECT * FROM order_book WHERE order_no = #{orderNo}")
    Order findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据用户 ID 查找订单列表
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    @Select("SELECT * FROM order_book WHERE user_id = #{userId} ORDER BY order_time DESC")
    List<Order> findByUserIdOrderByOrderTimeDesc(@Param("userId") Long userId);

    /**
     * 根据用户 ID 和状态查找订单
     *
     * @param userId 用户 ID
     * @param status 订单状态
     * @return 订单列表
     */
    @Select("SELECT * FROM order_book WHERE user_id = #{userId} AND status = #{status} ORDER BY order_time DESC")
    List<Order> findByUserIdAndStatusOrderByOrderTimeDesc(@Param("userId") Long userId, @Param("status") OrderStatus status);

    /**
     * 根据用户 ID、交易所 ID 和品种代码查找待成交订单
     *
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param status 订单状态
     * @return 订单列表
     */
    @Select("SELECT * FROM order_book WHERE user_id = #{userId} AND exchange_id = #{exchangeId} AND instrument_code = #{instrumentCode} AND status = #{status}")
    List<Order> findByUserIdAndExchangeIdAndInstrumentCodeAndStatus(
        @Param("userId") Long userId, @Param("exchangeId") Long exchangeId,
        @Param("instrumentCode") String instrumentCode, @Param("status") OrderStatus status);
}
