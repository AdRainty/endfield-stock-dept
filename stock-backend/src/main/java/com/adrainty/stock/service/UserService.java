package com.adrainty.stock.service;

import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.entity.User;

/**
 * 用户服务接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public interface UserService {
    
    /**
     * 微信登录/注册
     * 
     * @param openid 微信 OpenID
     * @param nickname 昵称
     * @param avatar 头像
     * @param registerIp 注册 IP
     * @return 登录响应
     */
    LoginResponse wxLogin(String openid, String nickname, String avatar, String registerIp);
    
    /**
     * 根据 ID 查找用户
     * 
     * @param id 用户 ID
     * @return 用户对象
     */
    User findById(Long id);
    
    /**
     * 根据 OpenID 查找用户
     * 
     * @param openid 微信 OpenID
     * @return 用户对象
     */
    User findByOpenid(String openid);
    
    /**
     * 更新用户登录信息
     * 
     * @param user 用户对象
     * @param loginIp 登录 IP
     */
    void updateLoginInfo(User user, String loginIp);
}
