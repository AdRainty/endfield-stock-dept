package com.adrainty.stock.entity;

import com.adrainty.stock.enums.ExchangeCode;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 交易所实体类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Getter
@Setter
@TableName("exchange")
public class Exchange extends BaseEntity {

    /**
     * 交易所代码
     */
    @TableField("exchange_code")
    private ExchangeCode exchangeCode;

    /**
     * 交易所名称
     */
    @TableField("name")
    private String name;

    /**
     * 交易所描述
     */
    @TableField("description")
    private String description;

    /**
     * 交易所状态：1-正常 0-维护
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 交易时间开始 (HH:mm 格式)
     */
    @TableField("trading_start")
    private String tradingStart = "00:00";

    /**
     * 交易时间结束 (HH:mm 格式)
     */
    @TableField("trading_end")
    private String tradingEnd = "23:59";
}
