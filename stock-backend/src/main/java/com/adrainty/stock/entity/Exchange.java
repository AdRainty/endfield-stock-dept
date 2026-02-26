package com.adrainty.stock.entity;

import com.adrainty.stock.enums.ExchangeCode;
import jakarta.persistence.*;
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
@Entity
@Table(name = "exchange")
public class Exchange extends BaseEntity {
    
    /**
     * 交易所代码
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_code", unique = true, length = 20)
    private ExchangeCode exchangeCode;
    
    /**
     * 交易所名称
     */
    @Column(name = "name", length = 50)
    private String name;
    
    /**
     * 交易所描述
     */
    @Column(name = "description", length = 255)
    private String description;
    
    /**
     * 交易所状态：1-正常 0-维护
     */
    @Column(name = "status")
    private Integer status = 1;
    
    /**
     * 交易时间开始 (HH:mm 格式)
     */
    @Column(name = "trading_start", length = 10)
    private String tradingStart = "00:00";
    
    /**
     * 交易时间结束 (HH:mm 格式)
     */
    @Column(name = "trading_end", length = 10)
    private String tradingEnd = "23:59";
}
