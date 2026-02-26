package com.adrainty.stock.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 微信工具类（真实调用微信 API）
 *
 * 微信开放平台文档：
 * - 网站应用微信登录：https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html
 * - 获取 access_token：https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/Official_Accounts/Third_party_authorization_process.html
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

    @Value("${app.wechat.redirect-uri:http://localhost:8081/api/auth/wx-callback}")
    private String redirectUri;

    private final RedisTemplate<String, Object> redisTemplate;

    public WechatUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String WX_OAUTH2_URL = "https://open.weixin.qq.com/connect/qrconnect";
    private static final String WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String WX_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo";

    private static final String REDIS_QR_PREFIX = "wx:qrcode:";
    private static final long QR_EXPIRE_MINUTES = 5;

    /**
     * 生成微信二维码登录场景
     *
     * @return 场景字符串
     */
    public String generateQrCodeScene() {
        String sceneStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String redisKey = REDIS_QR_PREFIX + sceneStr;

        // 生成微信 OAuth2.0 二维码 URL
        String qrCodeUrl = String.format(
            "%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
            WX_OAUTH2_URL,
            appId,
            encodeURIComponent(redirectUri),
            sceneStr
        );

        // 使用第三方工具生成二维码图片（这里使用联众二维码 API）
        String qrCodeBase64 = generateQrCodeImage(qrCodeUrl);

        // 存储场景信息到 Redis
        WxQrCodeScene scene = new WxQrCodeScene();
        scene.setScene(sceneStr);
        scene.setQrCodeUrl(qrCodeUrl);
        scene.setQrCodeBase64(qrCodeBase64);
        scene.setStatus("WAIT");
        scene.setExpireTime(System.currentTimeMillis() + QR_EXPIRE_MINUTES * 60 * 1000);

        redisTemplate.opsForValue().set(redisKey, scene, QR_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("生成微信二维码场景：{}, 过期时间：{} 分钟", sceneStr, QR_EXPIRE_MINUTES);
        return sceneStr;
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

            // 使用 code 换取 access_token 和 openid
            Map<String, String> tokenInfo = getAccessTokenByCode(code);
            String accessToken = tokenInfo.get("access_token");
            String openid = tokenInfo.get("openid");

            if (openid == null) {
                log.error("获取 openid 失败：{}", tokenInfo.get("errmsg"));
                return null;
            }

            // 获取用户信息
            JSONObject userInfo = getUserInfo(accessToken, openid);

            // 更新 Redis 中的场景状态
            String redisKey = REDIS_QR_PREFIX + state;
            WxQrCodeScene scene = (WxQrCodeScene) redisTemplate.opsForValue().get(redisKey);

            if (scene != null) {
                scene.setStatus("SUCCESS");
                scene.setOpenid(openid);
                scene.setNickname(userInfo.getString("nickname"));
                scene.setAvatar(userInfo.getString("headimgurl"));

                // 延长过期时间，给用户登录的时间
                redisTemplate.opsForValue().set(redisKey, scene, 10, TimeUnit.MINUTES);

                log.info("微信扫码成功，openid={}, nickname={}", openid, scene.getNickname());
                return scene;
            }

            return null;
        } catch (Exception e) {
            log.error("处理微信回调失败", e);
            return null;
        }
    }

    /**
     * 使用 code 换取 access_token 和 openid
     *
     * @param code 微信授权码
     * @return access_token 和 openid
     */
    private Map<String, String> getAccessTokenByCode(String code) {
        String url = String.format(
            "%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
            WX_ACCESS_TOKEN_URL,
            appId,
            appSecret,
            code
        );

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            String response = httpClient.execute(httpGet, res -> {
                if (res.getCode() == 200) {
                    return EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8);
                }
                return null;
            });

            if (response != null) {
                JSONObject json = JSON.parseObject(response);
                Map<String, String> result = new HashMap<>();
                result.put("access_token", json.getString("access_token"));
                result.put("openid", json.getString("openid"));
                result.put("refresh_token", json.getString("refresh_token"));
                log.info("获取 access_token 成功，openid={}", json.getString("openid"));
                return result;
            }
        } catch (Exception e) {
            log.error("获取 access_token 失败", e);
        }

        return new HashMap<>();
    }

    /**
     * 获取用户信息
     *
     * @param accessToken access_token
     * @param openid 用户 openid
     * @return 用户信息 JSON
     */
    private JSONObject getUserInfo(String accessToken, String openid) {
        String url = String.format(
            "%s?access_token=%s&openid=%s&lang=zh_CN",
            WX_USER_INFO_URL,
            accessToken,
            openid
        );

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            String response = httpClient.execute(httpGet, res -> {
                if (res.getCode() == 200) {
                    return EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8);
                }
                return null;
            });

            if (response != null) {
                JSONObject json = JSON.parseObject(response);
                if (json.containsKey("errmsg")) {
                    log.error("获取用户信息失败：{}", json.getString("errmsg"));
                }
                return json;
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
        }

        return new JSONObject();
    }

    /**
     * 生成二维码图片（使用第三方 API）
     *
     * @param content 二维码内容
     * @return base64 图片
     */
    private String generateQrCodeImage(String content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String apiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodeURIComponent(content);
            HttpGet httpGet = new HttpGet(apiUrl);

            byte[] imageBytes = httpClient.execute(httpGet, res -> {
                if (res.getCode() == 200) {
                    return res.getEntity().getContent().readAllBytes();
                }
                return null;
            });

            if (imageBytes != null) {
                String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                return "data:image/png;base64," + base64;
            }
        } catch (Exception e) {
            log.error("生成二维码失败", e);
        }

        // 降级方案：返回空图片
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    }

    /**
     * URL 编码
     */
    private String encodeURIComponent(String str) {
        try {
            return java.net.URLEncoder.encode(str, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        } catch (Exception e) {
            return str;
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
         * 场景字符串
         */
        private String scene;

        /**
         * 二维码 URL（微信 OAuth2.0 链接）
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
