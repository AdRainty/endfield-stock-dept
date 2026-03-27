package com.adrainty.stock.service;

import com.adrainty.stock.dto.LeaderboardDTO;
import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.dto.UserDTO;
import com.adrainty.stock.entity.User;

import java.util.List;
import java.util.Map;

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
     * @return 登录响应
     */
    LoginResponse wxLogin(String openid);

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
     * 注册用户
     *
     * @param openid 微信 OpenID
     * @return 用户对象
     */
    User register(String openid);

    /**
     * 更新用户登录信息
     *
     * @param user 用户对象
     * @param loginIp 登录 IP
     */
    void updateLoginInfo(User user, String loginIp);

    /**
     * 获取用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息 DTO
     */
    UserDTO getUserInfo(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户 ID
     * @param nickname 昵称
     * @param avatar 头像
     */
    void updateProfile(Long userId, String nickname, String avatar);

    /**
     * 获取用户统计
     *
     * @param userId 用户 ID
     * @return 统计数据
     */
    Map<String, Object> getUserStats(Long userId);

    /**
     * 获取排行榜
     *
     * @param type 类型：daily-日榜，total-总榜
     * @return 排行榜列表
     */
    List<LeaderboardDTO> getLeaderboard(String type);
}
