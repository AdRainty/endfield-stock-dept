package com.adrainty.stock.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 配置类
 * 实现 StpInterface 接口，提供角色和权限获取逻辑
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer, StpInterface {

    private final UserMapper userMapper;

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

    /**
     * 返回一个账号所拥有的角色码集合
     *
     * @param loginId 账号 ID
     * @param loginType 账号类型
     * @return 角色码集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();

        Long userId;
        try {
            userId = Long.parseLong(loginId.toString());
        } catch (NumberFormatException e) {
            return roleList;
        }

        User user = userMapper.selectById(userId);
        if (user != null && user.getRole() != null) {
            roleList.add(user.getRole().getCode());
        }

        return roleList;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     *
     * @param loginId 账号 ID
     * @param loginType 账号类型
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 暂不实现具体权限，只使用角色控制
        return new ArrayList<>();
    }
}
