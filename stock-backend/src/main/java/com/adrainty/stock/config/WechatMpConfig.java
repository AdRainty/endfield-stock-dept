package com.adrainty.stock.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信配置类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WechatMpConfig {

    private final WechatMpProperties properties;

    @Bean
    public WxMpService wxMpService() {
        log.info("初始化微信服务，appId={}", properties.getAppId());

        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(properties.getAppId());
        config.setSecret(properties.getAppSecret());
        config.setToken(properties.getToken());
        config.setAesKey(properties.getAesKey());

        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(config);

        return wxMpService;
    }
}
