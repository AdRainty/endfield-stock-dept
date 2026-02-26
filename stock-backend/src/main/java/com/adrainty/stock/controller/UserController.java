package com.adrainty.stock.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.adrainty.stock.dto.LeaderboardDTO;
import com.adrainty.stock.dto.UserDTO;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author adrainty
 * @since 2026-02-27
 */
@Tag(name = "用户管理", description = "用户信息、排行榜相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<UserDTO>> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserDTO user = userService.getUserInfo(userId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(user));
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新用户昵称和头像")
    @PostMapping("/profile")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> updateProfile(
            @RequestBody Map<String, String> request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, request.get("nickname"), request.get("avatar"));
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("更新成功"));
    }

    /**
     * 获取用户统计
     */
    @Operation(summary = "获取用户统计", description = "获取用户的投资统计数据")
    @GetMapping("/stats")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Map<String, Object>>> getUserStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> stats = userService.getUserStats(userId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(stats));
    }

    /**
     * 获取排行榜
     */
    @Operation(summary = "获取排行榜", description = "获取收益排行榜（日榜/总榜）")
    @GetMapping("/leaderboard")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<LeaderboardDTO>>> getLeaderboard(
            @RequestParam String type) {
        List<LeaderboardDTO> leaderboard = userService.getLeaderboard(type);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(leaderboard));
    }
}
