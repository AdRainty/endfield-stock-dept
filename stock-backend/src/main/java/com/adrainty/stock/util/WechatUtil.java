package com.adrainty.stock.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信工具类（模拟实现，实际项目需要对接微信 API）
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Component
public class WechatUtil {
    
    @Value("${app.wechat.app-id:}")
    private String appId;
    
    @Value("${app.wechat.app-secret:}")
    private String appSecret;
    
    // 模拟的二维码场景数据
    private static final Map<String, WxQrCodeScene> QR_CODE_SCENES = new ConcurrentHashMap<>();
    
    /**
     * 生成二维码场景
     * 
     * @return 场景字符串
     */
    public String generateQrCodeScene() {
        String sceneStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        WxQrCodeScene scene = new WxQrCodeScene();
        scene.setScene(sceneStr);
        scene.setQrCodeUrl("https://api.weixin.qq.com/cgi-bin/qrcode/create?ticket=" + sceneStr);
        // 实际应该生成二维码图片的 base64
        scene.setQrCodeBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        scene.setStatus("WAIT"); // WAIT-等待扫码 SUCCESS-已授权
        scene.setExpireTime(System.currentTimeMillis() + 5 * 60 * 1000); // 5 分钟过期
        QR_CODE_SCENES.put(sceneStr, scene);
        log.info("生成二维码场景：{}", sceneStr);
        return sceneStr;
    }
    
    /**
     * 模拟用户扫码授权（用于测试）
     * 
     * @param scene 场景字符串
     * @param openid 微信 OpenID
     * @param nickname 昵称
     * @param avatar 头像
     */
    public void mockScanAuthorize(String scene, String openid, String nickname, String avatar) {
        WxQrCodeScene wxQrCodeScene = QR_CODE_SCENES.get(scene);
        if (wxQrCodeScene != null) {
            wxQrCodeScene.setStatus("SUCCESS");
            wxQrCodeScene.setOpenid(openid);
            wxQrCodeScene.setNickname(nickname);
            wxQrCodeScene.setAvatar(avatar);
            log.info("模拟扫码授权：scene={}, openid={}", scene, openid);
        }
    }
    
    /**
     * 检查二维码状态
     * 
     * @param scene 场景字符串
     * @return 二维码场景数据
     */
    public WxQrCodeScene checkQrCodeStatus(String scene) {
        WxQrCodeScene wxQrCodeScene = QR_CODE_SCENES.get(scene);
        if (wxQrCodeScene == null) {
            return null;
        }
        // 检查是否过期
        if (System.currentTimeMillis() > wxQrCodeScene.getExpireTime()) {
            wxQrCodeScene.setStatus("EXPIRED");
        }
        return wxQrCodeScene;
    }
    
    /**
     * 根据 code 获取 OpenID（模拟实现）
     * 
     * @param code 微信登录 code
     * @return OpenID 信息
     */
    public Map<String, String> getOpenIdByCode(String code) {
        // 实际实现需要调用微信 API: https://api.weixin.qq.com/sns/jscode2session
        Map<String, String> result = new HashMap<>();
        // 模拟返回
        result.put("openid", "mock_openid_" + code);
        result.put("session_key", "mock_session_key_" + code);
        log.info("模拟获取 OpenID: code={}, openid={}", code, result.get("openid"));
        return result;
    }
    
    /**
     * 二维码场景数据
     */
    @lombok.Data
    public static class WxQrCodeScene {
        /**
         * 场景字符串
         */
        private String scene;
        
        /**
         * 二维码 URL
         */
        private String qrCodeUrl;
        
        /**
         * 二维码 base64 图片
         */
        private String qrCodeBase64;
        
        /**
         * 状态：WAIT-等待扫码 SUCCESS-已授权 EXPIRED-已过期
         */
        private String status;
        
        /**
         * 过期时间
         */
        private Long expireTime;
        
        /**
         * 微信 OpenID（扫码后填充）
         */
        private String openid;
        
        /**
         * 昵称（扫码后填充）
         */
        private String nickname;
        
        /**
         * 头像（扫码后填充）
         */
        private String avatar;
    }
}
