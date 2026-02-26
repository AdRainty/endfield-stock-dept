package com.adrainty.stock.config;

import com.adrainty.stock.wechat.CustomWxMpMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpRedissonConfigImpl;
import org.redisson.api.RedissonClient;
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

    @Bean(name = "wxMpConfigStorage")
    public WxMpConfigStorage wxMpConfigStorage(RedissonClient redissonClient) {
        log.info("初始化微信服务，appId={}", properties.getAppId());

        WxMpRedissonConfigImpl storage = new WxMpRedissonConfigImpl(redissonClient, properties.getRedisPrefix());
        storage.setAppId(properties.getAppId());
        storage.setSecret(properties.getAppSecret());
        storage.setToken(properties.getToken());
        storage.setAesKey(properties.getAesKey());
        return storage;
    }

    @Bean(name = "wxMpService")
    public WxMpService wxMpService(WxMpConfigStorage wxMpConfigStorage) {
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage);
        return wxMpService;
    }

    @Bean(name = "wxMpMessageRouter")
    public WxMpMessageRouter wxMpMessageRouter(WxMpService wxMpService, CustomWxMpMessageHandler customHandler) {
        WxMpMessageRouter wxMpMessageRouter = new WxMpMessageRouter(wxMpService);
        wxMpMessageRouter
                .rule()
                .async(false)
                .handler(customHandler)
                .end();
        return wxMpMessageRouter;
    }


}
