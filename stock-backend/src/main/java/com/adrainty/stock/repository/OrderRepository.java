package com.adrainty.stock.repository;

import com.adrainty.stock.entity.Order;
import com.adrainty.stock.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 委托订单数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 根据订单号查找订单
     * 
     * @param orderNo 订单号
     * @return 订单对象
     */
    java.util.Optional<Order> findByOrderNo(String orderNo);
    
    /**
     * 根据用户 ID 查找订单列表
     * 
     * @param userId 用户 ID
     * @return 订单列表
     */
    List<Order> findByUserIdOrderByOrderTimeDesc(Long userId);
    
    /**
     * 根据用户 ID 和状态查找订单
     * 
     * @param userId 用户 ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByUserIdAndStatusOrderByOrderTimeDesc(Long userId, OrderStatus status);
    
    /**
     * 根据用户 ID、交易所 ID 和品种代码查找待成交订单
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByUserIdAndExchangeIdAndInstrumentCodeAndStatus(
        Long userId, Long exchangeId, String instrumentCode, OrderStatus status);
}
