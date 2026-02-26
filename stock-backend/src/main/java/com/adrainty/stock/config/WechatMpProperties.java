package com.adrainty.stock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信配置属性
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.mp")
public class WechatMpProperties {

    /**
     * 微信公众号 AppID
     */
    private String appId;

    /**
     * 微信公众号 AppSecret
     */
    private String appSecret;

    /**
     * 微信公众号 Token
     */
    private String token;

    /**
     * 微信公众号 EncodingAESKey
     */
    private String aesKey;

    /**
     * OAuth2 回调地址
     */
    private String redirectUri;

    /**
     * Redis 前缀
     */
    private String redisPrefix = "wx:";
}
