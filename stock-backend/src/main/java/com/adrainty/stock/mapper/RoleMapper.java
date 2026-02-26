package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色代码查找角色
     *
     * @param roleCode 角色代码
     * @return 角色对象
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    Role findByRoleCode(@Param("roleCode") String roleCode);
}
