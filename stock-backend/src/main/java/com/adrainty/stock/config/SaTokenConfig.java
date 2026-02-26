package com.adrainty.stock.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * Sa-Token 使用 Redis 进行 token 存储
     */
    @Bean
    @Primary
    public SaTokenDao saTokenDao(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        return new SaTokenDaoRedisJackson(redisTemplate);
    }

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/auth/**",
                "/wechat/**",
                "/doc.html",
                "/webjars/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/h2-console/**"
            );
    }
}
