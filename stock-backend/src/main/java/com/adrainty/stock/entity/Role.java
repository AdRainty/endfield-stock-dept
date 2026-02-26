package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色实体类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@TableName("sys_role")
public class Role extends BaseEntity {

    /**
     * 角色代码
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 角色状态：1-正常 0-禁用
     */
    @TableField("status")
    private Integer status = 1;
}
