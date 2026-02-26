package com.adrainty.stock.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.adrainty.stock.dto.LoginRequest;
import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.service.UserService;
import com.adrainty.stock.util.WechatUtil;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
        String scene = wechatUtil.generateQrCodeScene();
        Map<String, String> result = new HashMap<>();
        result.put("scene", scene);
        result.put("qrCodeBase64", wechatUtil.checkQrCodeStatus(scene).getQrCodeBase64());
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
                qrScene.getOpenid(),
                qrScene.getNickname(),
                qrScene.getAvatar(),
                null
            );
            // 生成 Sa-Token
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
     * 微信 code 登录（备用登录方式）
     */
    @Operation(summary = "微信 code 登录", description = "使用微信登录 code 进行登录")
    @PostMapping("/wx-login")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Map<String, Object>>> wxLogin(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 根据 code 获取 access_token 和 openid
            String code = request.getCode();
            Map<String, String> tokenInfo = wechatUtil.getAccessTokenByCode(code);
            String openid = tokenInfo.get("openid");
            String accessToken = tokenInfo.get("access_token");

            if (openid == null || accessToken == null) {
                return ResponseEntity.badRequest().body(
                    GlobalExceptionHandler.ApiResult.error(400, "微信授权失败，请重试"));
            }

            // 获取用户信息
            JSONObject userInfo = wechatUtil.getUserInfo(accessToken, openid);
            String nickname = userInfo.getString("nickname");
            String avatar = userInfo.getString("headimgurl");

            // 登录/注册
            String clientIp = httpRequest.getRemoteAddr();
            LoginResponse loginResponse = userService.wxLogin(
                openid,
                nickname,
                avatar,
                clientIp
            );

            // 生成 Sa-Token
            StpUtil.login(loginResponse.getUserId());
            String token = StpUtil.getTokenValue();

            Map<String, Object> result = new HashMap<>();
            result.put("user", loginResponse);
            result.put("token", token);
            return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return ResponseEntity.badRequest().body(
                GlobalExceptionHandler.ApiResult.error(500, "登录失败：" + e.getMessage()));
        }
    }
    
    /**
     * 微信回调接口
     * 微信开放平台会携带 code 和 state 参数回调此接口
     */
    @Operation(hidden = true)
    @GetMapping("/wx-callback")
    public String wxCallback(
            @RequestParam String code,
            @RequestParam String state) {
        log.info("微信回调，code={}, state={}", code, state);

        WechatUtil.WxQrCodeScene scene = wechatUtil.handleWxCallback(code, state);

        if (scene != null) {
            // 用户已扫码授权，前端轮询会检测到状态并登录
            return "<html><head><title>微信授权成功</title></head><body>" +
                   "<h1>微信授权成功</h1><p>请在电脑上继续操作</p>" +
                   "<script>window.close()</script>" +
                   "</body></html>";
        }

        return "<html><head><title>授权失败</title></head><body>" +
               "<h1>授权失败</h1><p>请重新扫码</p>" +
               "</body></html>";
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
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(result));
    }
}
