package com.adrainty.stock.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "sys_role")
public class Role extends BaseEntity {
    
    /**
     * 角色代码
     */
    @Column(name = "role_code", unique = true, length = 20)
    private String roleCode;
    
    /**
     * 角色名称
     */
    @Column(name = "role_name", length = 50)
    private String roleName;
    
    /**
     * 角色描述
     */
    @Column(name = "description", length = 255)
    private String description;
    
    /**
     * 角色状态：1-正常 0-禁用
     */
    @Column(name = "status")
    private Integer status = 1;
}
