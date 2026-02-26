package com.adrainty.stock.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Tag(name = "管理员管理", description = "用户管理、原能分配相关接口")
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
            @RequestParam Long targetUserId,
            @RequestParam Long exchangeId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "管理员分配") String reason) {
        Long adminUserId = StpUtil.getLoginIdAsLong();
        adminService.allocateCapital(adminUserId, targetUserId, exchangeId, amount, reason);
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
}
