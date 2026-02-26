package com.adrainty.stock.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
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
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一条 match 规则
            SaInterceptor
                .create(handle)
                // 排除路径：登录接口、微信回调、API 文档、静态资源
                .excludePathPatterns(
                    "/auth/**",
                    "/wechat/**",
                    "/doc.html",
                    "/webjars/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/h2-console/**"
                )
                // 其他路径需要登录
                .addCheck(r -> StpUtil.checkLogin())
                .run();
        })).addPathPatterns("/**");
    }
}
