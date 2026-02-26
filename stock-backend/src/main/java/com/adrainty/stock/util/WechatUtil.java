package com.adrainty.stock.util;

import com.adrainty.stock.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serial;
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
    private final RedisTemplate<String, Object> redisTemplate;

    private final WxMpMessageRouter wxMpMessageRouter;

    public WechatUtil(WxMpService wxMpService,
                      RedisTemplate<String, Object> redisTemplate,
                      WxMpMessageRouter wxMpMessageRouter) {
        this.wxMpService = wxMpService;
        this.redisTemplate = redisTemplate;
        this.wxMpMessageRouter = wxMpMessageRouter;
    }

    private static final String REDIS_QR_PREFIX = "wx:qrcode:";
    private static final long QR_EXPIRE_MINUTES = 5;

    /**
     * 微信公众号验证
     */
    public static final String SIGNATURE = "signature";
    public static final String TIMESTAMP = "timestamp";
    public static final String NONCE = "nonce";
    public static final String ECHO_STR = "echostr";
    public static final String MSG_SIGNATURE = "msg_signature";
    public static final String ENCRYPTED_TYPE = "encrypt_type";
    public static final String ENCRYPTED_TYPE_RAW = "raw";


    /**
     * 生成微信二维码登录场景（临时二维码）
     *
     * @return 场景字符串
     */
    public String generateQrCodeScene() {
        try {
            // 生成临时二维码 scene_id
            String sceneStr = UUID.randomUUID().toString().replace("-", "");

            // 获取二维码服务
            var qrcodeService = wxMpService.getQrcodeService();

            // 使用 WxJava 生成临时二维码 ticket
            WxMpQrCodeTicket qrCodeTicket = qrcodeService.qrCodeCreateTmpTicket(sceneStr, (int) QR_EXPIRE_MINUTES * 60);
            String ticket = qrCodeTicket.getTicket();
            String redisKey = REDIS_QR_PREFIX + sceneStr;

            // 获取二维码图片 URL（这是微信官方的二维码图片）
            String pictureUrl = qrcodeService.qrCodePictureUrl(ticket);

            log.info("生成微信二维码，sceneId={}, ticket={}, pictureUrl={}", sceneStr, ticket, pictureUrl);

            // 存储场景信息到 Redis
            WxQrCodeScene scene = new WxQrCodeScene();
            scene.setScene(sceneStr);
            scene.setTicket(ticket);
            scene.setQrCodeUrl(pictureUrl);  // 直接返回微信官方的二维码图片 URL
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
     * @return 用户信息
     */
    public String handleWxCallback(HttpServletRequest request) {
        String timestamp = request.getParameter(WechatUtil.TIMESTAMP);
        String nonce = request.getParameter(WechatUtil.NONCE);
        String encryptType = StringUtils.defaultIfBlank(request.getParameter(ENCRYPTED_TYPE), ENCRYPTED_TYPE_RAW);
        WxMpConfigStorage wxMpConfigStorage = wxMpService.getWxMpConfigStorage();
        WxMpXmlMessage inMessage;

        try {
            if (ENCRYPTED_TYPE_RAW.equals(encryptType)) {
                inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
            } else {
                String msgSignature = request.getParameter(MSG_SIGNATURE);
                inMessage = WxMpXmlMessage.fromEncryptedXml(
                        request.getInputStream(), wxMpConfigStorage, timestamp, nonce, msgSignature);
            }
        } catch (IOException e) {
            log.error("Parse encrypted xml error: ", e);
            throw new BusinessException("第三方服务错误");
        }

        WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
        if (outMessage == null) {
            //为null，说明路由配置有问题，需要注意
            log.error("Route error: outMessage is null");
            throw new BusinessException("微信服务器配置错误");
        }
        return ENCRYPTED_TYPE_RAW.equals(encryptType) ?
                outMessage.toXml() :
                outMessage.toEncryptedXml(wxMpConfigStorage);
    }

    /**
     * 检查微信签名
     *
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param signature 签名
     */
    public void checkSignature(String timestamp, String nonce, String signature) {
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            throw new BusinessException("微信签名验证失败");
        }
    }

    /**
     * 二维码场景数据
     */
    @Data
    public static class WxQrCodeScene implements java.io.Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 场景字符串（state）
         */
        private String scene;

        /**
         * 二维码 ticket
         */
        private String ticket;

        /**
         * 二维码 URL（微信 OAuth2.0 授权链接）
         */
        private String qrCodeUrl;

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
