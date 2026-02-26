package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.enums.UserRole;
import com.adrainty.stock.mapper.ExchangeMapper;
import com.adrainty.stock.mapper.UserMapper;
import com.adrainty.stock.service.CapitalService;
import com.adrainty.stock.service.PositionService;
import com.adrainty.stock.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ExchangeMapper exchangeMapper;
    private final CapitalService capitalService;
    private final PositionService positionService;

    @Value("${app.initial-capital:100000}")
    private BigDecimal initialCapital;

    @Override
    @Transactional
    public LoginResponse wxLogin(String openid, String nickname, String avatar, String registerIp) {
        boolean newUser = false;
        User user = userMapper.findByWechatOpenid(openid);

        if (user == null) {
            // 新用户注册
            newUser = true;
            user = new User();
            user.setWechatOpenid(openid);
            user.setNickname(nickname != null ? nickname : "用户" + openid.substring(0, 8));
            user.setAvatar(avatar);
            user.setRole(UserRole.USER);
            user.setStatus(1);
            user.setRegisterIp(registerIp);
            userMapper.insert(user);
            log.info("新用户注册：openid={}, nickname={}", openid, user.getNickname());

            // 初始化资金和持仓
            initUserResources(user.getId());
        } else {
            // 老用户登录
            log.info("用户登录：openid={}, nickname={}", openid, user.getNickname());
        }

        return LoginResponse.builder()
            .userId(user.getId())
            .openid(user.getWechatOpenid())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            .role(user.getRole().getCode())
            .newUser(newUser)
            .build();
    }

    /**
     * 初始化用户资源（资金和持仓）
     */
    private void initUserResources(Long userId) {
        // 获取所有交易所
        List<Exchange> exchanges = exchangeMapper.findByStatus(1);

        for (Exchange exchange : exchanges) {
            // 初始化资金（每个交易所 10W）
            capitalService.initCapital(userId, exchange.getId(), initialCapital);

            // 初始化持仓记录
            positionService.initPosition(userId, exchange.getId());
        }

        log.info("初始化用户资源完成：userId={}", userId);
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User findByOpenid(String openid) {
        return userMapper.findByWechatOpenid(openid);
    }

    @Override
    @Transactional
    public void updateLoginInfo(User user, String loginIp) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        userMapper.updateById(user);
    }
}
