package com.adrainty.stock.service;

import com.adrainty.stock.dto.OrderDTO;
import com.adrainty.stock.dto.PlaceOrderRequest;
import com.adrainty.stock.enums.OrderStatus;

import java.util.List;

/**
 * 订单服务接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public interface OrderService {
    
    /**
     * 下单
     * 
     * @param userId 用户 ID
     * @param request 下单请求
     * @return 订单 DTO
     */
    OrderDTO placeOrder(Long userId, PlaceOrderRequest request);
    
    /**
     * 撤单
     * 
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean cancelOrder(Long userId, String orderNo);
    
    /**
     * 获取用户订单列表
     * 
     * @param userId 用户 ID
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    List<OrderDTO> getUserOrders(Long userId, String status);
    
    /**
     * 获取订单详情
     *
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @return 订单 DTO
     */
    OrderDTO getOrderDetail(Long userId, String orderNo);

    /**
     * 批量撤单（用于日终清算）
     *
     * @param date 日期
     * @param status 订单状态
     * @param reason 撤单原因
     * @return 撤单数量
     */
    int batchCancelOrders(java.time.LocalDate date, OrderStatus status, String reason);
}
