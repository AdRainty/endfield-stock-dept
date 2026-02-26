package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据微信 OpenID 查找用户
     *
     * @param openid 微信 OpenID
     * @return 用户对象
     */
    @Select("SELECT * FROM sys_user WHERE wechat_openid = #{openid}")
    User findByWechatOpenid(@Param("openid") String openid);

    /**
     * 检查微信 OpenID 是否存在
     *
     * @param openid 微信 OpenID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE wechat_openid = #{openid}")
    boolean existsByWechatOpenid(@Param("openid") String openid);
}
