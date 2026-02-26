package com.adrainty.stock.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.adrainty.stock.dto.CapitalAccountDTO;
import com.adrainty.stock.dto.OrderBookDTO;
import com.adrainty.stock.dto.OrderDTO;
import com.adrainty.stock.dto.PlaceOrderRequest;
import com.adrainty.stock.dto.PositionDTO;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.CapitalService;
import com.adrainty.stock.service.impl.CapitalServiceImpl;
import com.adrainty.stock.service.OrderBookService;
import com.adrainty.stock.service.OrderService;
import com.adrainty.stock.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易控制器
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Tag(name = "交易管理", description = "交易、持仓、资金相关接口")
@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {
    
    private final OrderService orderService;
    private final OrderBookService orderBookService;
    private final CapitalServiceImpl capitalService;
    private final PositionService positionService;
    
    /**
     * 获取资金账户
     */
    @Operation(summary = "获取资金账户", description = "获取用户在指定交易所的资金账户信息")
    @GetMapping("/account/{exchangeId}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<CapitalAccountDTO>> getAccount(
            @PathVariable Long exchangeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        CapitalAccountDTO account = capitalService.getAccount(userId, exchangeId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(account));
    }
    
    /**
     * 获取持仓列表
     */
    @Operation(summary = "获取持仓列表", description = "获取用户在指定交易所的持仓列表")
    @GetMapping("/position/{exchangeId}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<PositionDTO>>> getPositions(
            @PathVariable Long exchangeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<PositionDTO> positions = positionService.getUserPositions(userId, exchangeId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(positions));
    }
    
    /**
     * 获取档口数据
     */
    @Operation(summary = "获取档口数据", description = "获取指定品种的买卖档口数据")
    @GetMapping("/orderbook/{exchangeId}/{instrumentCode}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<OrderBookDTO>> getOrderBook(
            @PathVariable Long exchangeId,
            @PathVariable String instrumentCode) {
        OrderBookDTO orderBook = orderBookService.getOrderBook(exchangeId, instrumentCode);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(orderBook));
    }
    
    /**
     * 下单
     */
    @Operation(summary = "下单", description = "提交买入或卖出委托")
    @PostMapping("/order")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<OrderDTO>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        OrderDTO order = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(order));
    }
    
    /**
     * 撤单
     */
    @Operation(summary = "撤单", description = "撤销未成交的委托")
    @PostMapping("/order/{orderNo}/cancel")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Boolean>> cancelOrder(
            @PathVariable String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = orderService.cancelOrder(userId, orderNo);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
    }
    
    /**
     * 获取订单列表
     */
    @Operation(summary = "获取订单列表", description = "获取用户的委托订单列表")
    @GetMapping("/orders")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<OrderDTO>>> getOrders(
            @RequestParam(required = false) String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<OrderDTO> orders = orderService.getUserOrders(userId, status);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(orders));
    }
    
    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情", description = "获取指定订单的详细信息")
    @GetMapping("/order/{orderNo}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<OrderDTO>> getOrderDetail(
            @PathVariable String orderNo) {
        Long userId = StpUtil.getLoginIdAsLong();
        OrderDTO order = orderService.getOrderDetail(userId, orderNo);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(order));
    }
}
