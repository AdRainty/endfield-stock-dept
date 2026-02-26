package com.adrainty.stock.dto;

import com.adrainty.stock.enums.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下单请求 DTO
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Data
public class PlaceOrderRequest {
    
    /**
     * 交易所 ID
     */
    @NotNull(message = "交易所 ID 不能为空")
    private Long exchangeId;
    
    /**
     * 品种代码
     */
    @NotNull(message = "品种代码不能为空")
    private String instrumentCode;
    
    /**
     * 订单类型（买入/卖出）
     */
    @NotNull(message = "订单类型不能为空")
    private OrderType orderType;
    
    /**
     * 委托价格
     */
    @NotNull(message = "委托价格不能为空")
    @DecimalMin(value = "0.01", message = "委托价格必须大于 0")
    private BigDecimal price;
    
    /**
     * 委托数量
     */
    @NotNull(message = "委托数量不能为空")
    @Min(value = 1, message = "委托数量必须大于等于 1")
    private BigDecimal quantity;
}
