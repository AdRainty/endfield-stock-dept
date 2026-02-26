package com.adrainty.stock.controller;

import com.adrainty.stock.dto.InstrumentDTO;
import com.adrainty.stock.dto.OrderBookDTO;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.InstrumentService;
import com.adrainty.stock.service.OrderBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行情控制器
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Tag(name = "行情管理", description = "实时行情相关接口")
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {
    
    private final InstrumentService instrumentService;
    private final OrderBookService orderBookService;
    
    /**
     * 获取所有品种行情
     */
    @Operation(summary = "获取所有品种行情", description = "获取全市场所有品种的实时行情")
    @GetMapping("/instruments")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<InstrumentDTO>>> getAllInstruments() {
        List<InstrumentDTO> instruments = instrumentService.getAllInstruments();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(instruments));
    }
    
    /**
     * 获取交易所品种行情
     */
    @Operation(summary = "获取交易所品种行情", description = "获取指定交易所的品种行情列表")
    @GetMapping("/instruments/{exchangeId}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<InstrumentDTO>>> getInstrumentsByExchange(
            @PathVariable Long exchangeId) {
        List<InstrumentDTO> instruments = instrumentService.getByExchangeId(exchangeId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(instruments));
    }
    
    /**
     * 获取品种详情
     */
    @Operation(summary = "获取品种详情", description = "获取单个品种的详细行情信息")
    @GetMapping("/instrument/{instrumentCode}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<InstrumentDTO>> getInstrument(
            @PathVariable String instrumentCode) {
        InstrumentDTO instrument = instrumentService.getByCode(instrumentCode);
        if (instrument == null) {
            return ResponseEntity.badRequest().body(
                GlobalExceptionHandler.ApiResult.error(400, "品种不存在"));
        }
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(instrument));
    }
    
    /**
     * 获取档口数据
     */
    @Operation(summary = "获取档口数据", description = "获取指定品种的买卖五档数据")
    @GetMapping("/orderbook/{exchangeId}/{instrumentCode}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<OrderBookDTO>> getOrderBook(
            @PathVariable Long exchangeId,
            @PathVariable String instrumentCode) {
        OrderBookDTO orderBook = orderBookService.getOrderBook(exchangeId, instrumentCode);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(orderBook));
    }
    
    /**
     * 获取全市场档口
     */
    @Operation(summary = "获取全市场档口", description = "获取所有品种档口数据")
    @GetMapping("/orderbooks")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<OrderBookDTO>>> getAllOrderBooks() {
        List<OrderBookDTO> orderBooks = orderBookService.getAllOrderBooks();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(orderBooks));
    }
}
