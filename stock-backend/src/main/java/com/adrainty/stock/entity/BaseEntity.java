package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 实体基类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
