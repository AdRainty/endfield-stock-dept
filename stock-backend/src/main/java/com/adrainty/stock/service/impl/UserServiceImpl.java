package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.mapper.UserMapper;
import com.adrainty.stock.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Override
    @Transactional
    public LoginResponse wxLogin(String openid) {
        boolean newUser = false;
        User user = userMapper.findByWechatOpenid(openid);

        return LoginResponse.builder()
                .userId(user.getId())
                .openid(user.getWechatOpenid())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole().getCode())
                .newUser(newUser)
                .build();
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
