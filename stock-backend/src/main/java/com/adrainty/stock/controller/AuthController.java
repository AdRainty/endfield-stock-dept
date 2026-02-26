package com.adrainty.stock.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.UserService;
import com.adrainty.stock.util.WechatUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Tag(name = "认证管理", description = "用户登录、注册相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final WechatUtil wechatUtil;

    /**
     * 获取微信二维码
     */
    @Operation(summary = "获取微信二维码", description = "生成微信登录二维码场景")
    @PostMapping("/wx-qrcode")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Map<String, String>>> getWxQrCode() {
        String sceneStr = wechatUtil.generateQrCodeScene();
        WechatUtil.WxQrCodeScene qrScene = wechatUtil.checkQrCodeStatus(sceneStr);
        Map<String, String> result = new HashMap<>();
        result.put("scene", sceneStr);
        result.put("qrCodeUrl", qrScene.getQrCodeUrl());
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
    }

    /**
     * 检查二维码状态
     */
    @Operation(summary = "检查二维码状态", description = "轮询检查二维码扫码状态")
    @GetMapping("/wx-qrcode/{scene}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Object>> checkQrCodeStatus(
            @PathVariable String scene) {
        WechatUtil.WxQrCodeScene qrScene = wechatUtil.checkQrCodeStatus(scene);
        if (qrScene == null) {
            return ResponseEntity.badRequest().body(
                    GlobalExceptionHandler.ApiResult.error(400, "无效的二维码场景"));
        }

        if ("SUCCESS".equals(qrScene.getStatus())) {
            // 用户已扫码，进行登录
            LoginResponse loginResponse = userService.wxLogin(
                    qrScene.getOpenid()
            );
            // 生成 Sa-Token（角色由 StpInterface 自动获取）
            StpUtil.login(loginResponse.getUserId());
            String token = StpUtil.getTokenValue();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "SUCCESS");
            result.put("user", loginResponse);
            result.put("token", token);
            return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
        }

        Map<String, String> result = new HashMap<>();
        result.put("status", qrScene.getStatus());
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "登出当前用户")
    @PostMapping("/logout")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<String>> logout() {
        StpUtil.logout();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success("退出成功"));
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    @GetMapping("/user-info")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Map<String, Object>>> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole().getCode());
        result.put("status", user.getStatus());
        result.put("createdAt", user.getCreatedAt());
        result.put("availableCapital", user.getAvailableCapital());
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
    }
}
