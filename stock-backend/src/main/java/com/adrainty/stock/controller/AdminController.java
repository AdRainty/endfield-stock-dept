package com.adrainty.stock.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.adrainty.stock.dto.AllocateRequest;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Tag(name = "管理员管理", description = "用户管理、交易所管理、品种管理相关接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 分配原能
     */
    @Operation(summary = "分配原能", description = "向用户分配调度券原能")
    @PostMapping("/allocate")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> allocate(
            @RequestBody AllocateRequest request) {
        Long adminUserId = StpUtil.getLoginIdAsLong();
        adminService.allocateCapital(adminUserId, request.getTargetUserId(), request.getExchangeId(),
                request.getAmount(), request.getReason());
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("分配成功"));
    }

    /**
     * 获取用户列表
     */
    @Operation(summary = "获取用户列表", description = "获取所有用户列表")
    @GetMapping("/users")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<Map<String, Object>>>> getUserList() {
        List<Map<String, Object>> users = adminService.getUserList();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(users));
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情", description = "获取指定用户的详细信息")
    @GetMapping("/user/{userId}")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Map<String, Object>>> getUserDetail(
            @PathVariable Long userId) {
        Map<String, Object> detail = adminService.getUserDetail(userId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(detail));
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PostMapping("/user/{userId}/status")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        adminService.updateUserStatus(userId, status);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("更新成功"));
    }

    /**
     * 获取分配记录
     */
    @Operation(summary = "获取分配记录", description = "获取原能分配记录列表")
    @GetMapping("/allocations")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<Map<String, Object>>>> getAllocationRecords(
            @RequestParam(required = false) Long userId) {
        List<Map<String, Object>> records = adminService.getAllocationRecords(userId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(records));
    }

    /**
     * 获取统计数据
     */
    @Operation(summary = "获取统计数据", description = "获取平台运营统计数据")
    @GetMapping("/statistics")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Map<String, Object>>> getStatistics() {
        Map<String, Object> stats = adminService.getStatistics();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(stats));
    }

    // ==================== 交易所管理 ====================

    /**
     * 获取交易所列表
     */
    @Operation(summary = "获取交易所列表", description = "获取所有交易所列表")
    @GetMapping("/exchanges")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<Map<String, Object>>>> getExchangeList() {
        List<Map<String, Object>> exchanges = adminService.getExchangeList();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(exchanges));
    }

    /**
     * 添加交易所
     */
    @Operation(summary = "添加交易所", description = "添加新的交易所")
    @PostMapping("/exchange")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> addExchange(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam(required = false) String description) {
        adminService.addExchange(name, code, description);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("添加成功"));
    }

    /**
     * 更新交易所
     */
    @Operation(summary = "更新交易所", description = "更新交易所信息")
    @PostMapping("/exchange/{exchangeId}")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> updateExchange(
            @PathVariable Long exchangeId,
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam(required = false) String description) {
        adminService.updateExchange(exchangeId, name, code, description);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("更新成功"));
    }

    /**
     * 更新交易所状态
     */
    @Operation(summary = "更新交易所状态", description = "启用或禁用交易所")
    @PostMapping("/exchange/{exchangeId}/status")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> updateExchangeStatus(
            @PathVariable Long exchangeId,
            @RequestParam Integer status) {
        adminService.updateExchangeStatus(exchangeId, status);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("更新成功"));
    }

    // ==================== 品种管理 ====================

    /**
     * 获取品种列表
     */
    @Operation(summary = "获取品种列表", description = "获取所有交易品种列表")
    @GetMapping("/instruments")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<Map<String, Object>>>> getInstrumentList() {
        List<Map<String, Object>> instruments = adminService.getInstrumentList();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(instruments));
    }

    /**
     * 添加品种
     */
    @Operation(summary = "添加品种", description = "添加新的交易品种")
    @PostMapping("/instrument")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> addInstrument(
            @RequestParam Long exchangeId,
            @RequestParam String instrumentCode,
            @RequestParam String name,
            @RequestParam String type) {
        adminService.addInstrument(exchangeId, instrumentCode, name, type);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("添加成功"));
    }

    /**
     * 更新品种
     */
    @Operation(summary = "更新品种", description = "更新交易品种信息")
    @PostMapping("/instrument/{instrumentId}")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> updateInstrument(
            @PathVariable Long instrumentId,
            @RequestParam Long exchangeId,
            @RequestParam String instrumentCode,
            @RequestParam String name,
            @RequestParam String type) {
        adminService.updateInstrument(instrumentId, exchangeId, instrumentCode, name, type);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("更新成功"));
    }

    /**
     * 更新品种状态
     */
    @Operation(summary = "更新品种状态", description = "启用或禁用交易品种")
    @PostMapping("/instrument/{instrumentId}/status")
    @SaCheckRole("ADMIN")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> updateInstrumentStatus(
            @PathVariable Long instrumentId,
            @RequestParam Integer status) {
        adminService.updateInstrumentStatus(instrumentId, status);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("更新成功"));
    }
}
