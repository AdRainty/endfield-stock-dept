package com.adrainty.stock.repository;

import com.adrainty.stock.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色代码查找角色
     * 
     * @param roleCode 角色代码
     * @return 角色对象
     */
    Optional<Role> findByRoleCode(String roleCode);
}
