package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.enums.UserRole;
import com.adrainty.stock.repository.UserRepository;
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
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public LoginResponse wxLogin(String openid, String nickname, String avatar, String registerIp) {
        boolean newUser = false;
        User user = userRepository.findByWechatOpenid(openid).orElse(null);
        
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
            userRepository.save(user);
            log.info("新用户注册：openid={}, nickname={}", openid, user.getNickname());
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
    
    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @Override
    public User findByOpenid(String openid) {
        return userRepository.findByWechatOpenid(openid).orElse(null);
    }
    
    @Override
    @Transactional
    public void updateLoginInfo(User user, String loginIp) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        userRepository.save(user);
    }
}
