package com.adrainty.stock.repository;

import com.adrainty.stock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据微信 OpenID 查找用户
     * 
     * @param openid 微信 OpenID
     * @return 用户对象
     */
    Optional<User> findByWechatOpenid(String openid);
    
    /**
     * 检查微信 OpenID 是否存在
     * 
     * @param openid 微信 OpenID
     * @return 是否存在
     */
    boolean existsByWechatOpenid(String openid);
}
