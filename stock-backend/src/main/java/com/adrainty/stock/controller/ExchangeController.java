package com.adrainty.stock.controller;

import com.adrainty.stock.dto.InstrumentDTO;
import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.ExchangeService;
import com.adrainty.stock.service.InstrumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易所控制器
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Tag(name = "交易所管理", description = "交易所和行情相关接口")
@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {
    
    private final ExchangeService exchangeService;
    private final InstrumentService instrumentService;
    
    /**
     * 获取所有交易所
     */
    @Operation(summary = "获取所有交易所", description = "获取所有正常运营的交易所列表")
    @GetMapping("/list")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<Map<String, Object>>>> listExchanges() {
        List<Exchange> exchanges = exchangeService.getAllExchanges();
        List<Map<String, Object>> result = exchanges.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("code", e.getExchangeCode());
            map.put("name", e.getName());
            map.put("description", e.getDescription());
            map.put("status", e.getStatus());
            map.put("tradingStart", e.getTradingStart());
            map.put("tradingEnd", e.getTradingEnd());
            return map;
        }).toList();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
    }
    
    /**
     * 获取交易所下的品种
     */
    @Operation(summary = "获取交易所品种", description = "获取指定交易所下的所有调度券品种")
    @GetMapping("/{exchangeId}/instruments")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<InstrumentDTO>>> listInstruments(
            @PathVariable Long exchangeId) {
        List<InstrumentDTO> instruments = instrumentService.getByExchangeId(exchangeId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(instruments));
    }
    
    /**
     * 获取品种详情
     */
    @Operation(summary = "获取品种详情", description = "获取指定品种的详细行情信息")
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
}
