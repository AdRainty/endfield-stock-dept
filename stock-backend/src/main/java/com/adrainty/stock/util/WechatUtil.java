package com.adrainty.stock.util;

import com.adrainty.stock.config.WechatMpProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 微信工具类（基于 WxJava SDK）
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Component
public class WechatUtil {

    private final WxMpService wxMpService;
    private final WechatMpProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    public WechatUtil(WxMpService wxMpService, WechatMpProperties properties, RedisTemplate<String, Object> redisTemplate) {
        this.wxMpService = wxMpService;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    private static final String REDIS_QR_PREFIX = "wx:qrcode:";
    private static final long QR_EXPIRE_MINUTES = 5;
    private static final long CODE_EXPIRE_MINUTES = 10;

    /**
     * 生成微信二维码登录场景（临时二维码）
     *
     * @return 场景字符串
     */
    public String generateQrCodeScene() {
        try {
            // 生成临时二维码 scene_id
            String sceneStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            int sceneId = Math.abs(sceneStr.hashCode());
            String redisKey = REDIS_QR_PREFIX + sceneStr;

            // 使用 WxJava 生成临时二维码
            WxMpQrCodeTicket ticket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(sceneId, (int) QR_EXPIRE_MINUTES * 60);

            // 生成微信授权 URL（用于前端 QRCode 组件）
            String authorizeUrl = wxMpService.oauth2buildAuthorizationUrl(
                properties.getRedirectUri(),
                "snsapi_userinfo",
                sceneStr
            );

            log.info("生成微信二维码，sceneId={}, ticket={}, url={}", sceneId, ticket.getTicket(), authorizeUrl);

            // 存储场景信息到 Redis
            WxQrCodeScene scene = new WxQrCodeScene();
            scene.setScene(sceneStr);
            scene.setSceneId(sceneId);
            scene.setTicket(ticket.getTicket());
            scene.setQrCodeUrl(authorizeUrl);
            scene.setStatus("WAIT");
            scene.setExpireTime(System.currentTimeMillis() + QR_EXPIRE_MINUTES * 60 * 1000);

            redisTemplate.opsForValue().set(redisKey, scene, QR_EXPIRE_MINUTES, TimeUnit.MINUTES);

            log.info("生成微信二维码场景：{}, 过期时间：{} 分钟", sceneStr, QR_EXPIRE_MINUTES);
            return sceneStr;
        } catch (WxErrorException e) {
            log.error("生成微信二维码失败：{}", e.getError().getErrorMsg(), e);
            throw new RuntimeException("生成微信二维码失败：" + e.getError().getErrorMsg(), e);
        }
    }

    /**
     * 检查二维码状态
     *
     * @param scene 场景字符串
     * @return 二维码场景数据
     */
    public WxQrCodeScene checkQrCodeStatus(String scene) {
        String redisKey = REDIS_QR_PREFIX + scene;
        Object obj = redisTemplate.opsForValue().get(redisKey);

        if (obj == null) {
            return null;
        }

        WxQrCodeScene wxScene = (WxQrCodeScene) obj;

        // 检查是否过期
        if (System.currentTimeMillis() > wxScene.getExpireTime()) {
            wxScene.setStatus("EXPIRED");
            redisTemplate.delete(redisKey);
        }

        return wxScene;
    }

    /**
     * 微信回调接口处理
     * 微信会携带 code 和 state 参数回调此接口
     *
     * @param code 微信授权码
     * @param state 场景字符串
     * @return 用户信息
     */
    public WxQrCodeScene handleWxCallback(String code, String state) {
        try {
            log.info("微信回调，code={}, state={}", code, state);

            // 使用 code 换取用户信息
            WxMpUser user = wxMpService.getOauth2Service().getUserInfo(null, code);
            String openid = user.getOpenId();

            if (openid == null) {
                log.error("获取 openid 失败");
                return null;
            }

            // 查询场景信息
            String redisKey = REDIS_QR_PREFIX + state;
            WxQrCodeScene scene = (WxQrCodeScene) redisTemplate.opsForValue().get(redisKey);

            if (scene != null) {
                scene.setStatus("SUCCESS");
                scene.setOpenid(openid);
                scene.setNickname(user.getNickname());
                scene.setAvatar(user.getHeadImgUrl());

                // 延长过期时间，给用户登录的时间
                redisTemplate.opsForValue().set(redisKey, scene, 10, TimeUnit.MINUTES);

                log.info("微信扫码成功，openid={}, nickname={}", openid, scene.getNickname());
                return scene;
            }

            return null;
        } catch (WxErrorException e) {
            log.error("处理微信回调失败：{}", e.getError().getErrorMsg(), e);
            return null;
        }
    }

    /**
     * 使用 code 换取 access_token 和 openid（供备用登录接口使用）
     *
     * @param code 微信授权码
     * @return access_token 和 openid
     */
    public Map<String, String> getAccessTokenByCode(String code) {
        try {
            String accessToken = wxMpService.getOauth2Service().getAccessToken(code);
            WxMpUser user = wxMpService.getOauth2Service().getUserInfo(accessToken, code);

            Map<String, String> result = new HashMap<>();
            result.put("access_token", accessToken);
            result.put("openid", user.getOpenId());
            result.put("nickname", user.getNickname());
            result.put("avatar", user.getHeadImgUrl());

            log.info("获取 access_token 成功，openid={}", user.getOpenId());
            return result;
        } catch (WxErrorException e) {
            log.error("获取 access_token 失败：{}", e.getError().getErrorMsg(), e);
            return new HashMap<>();
        }
    }

    /**
     * 模拟扫码授权（开发环境使用）
     */
    public void mockScanAuthorize(String scene, String openid, String nickname, String avatar) {
        String redisKey = REDIS_QR_PREFIX + scene;
        WxQrCodeScene wxScene = (WxQrCodeScene) redisTemplate.opsForValue().get(redisKey);

        if (wxScene != null) {
            wxScene.setStatus("SUCCESS");
            wxScene.setOpenid(openid);
            wxScene.setNickname(nickname);
            wxScene.setAvatar(avatar);

            redisTemplate.opsForValue().set(redisKey, wxScene, 10, TimeUnit.MINUTES);
            log.info("模拟扫码授权：scene={}, openid={}", scene, openid);
        }
    }

    /**
     * 二维码场景数据
     */
    @Data
    public static class WxQrCodeScene implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 场景字符串（state）
         */
        private String scene;

        /**
         * 场景 ID（scene_id）
         */
        private Integer sceneId;

        /**
         * 二维码 ticket
         */
        private String ticket;

        /**
         * 二维码 URL（微信 OAuth2.0 授权链接）
         */
        private String qrCodeUrl;

        /**
         * 二维码图片 base64（可选）
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
