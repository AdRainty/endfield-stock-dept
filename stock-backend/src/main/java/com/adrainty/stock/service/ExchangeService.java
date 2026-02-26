package com.adrainty.stock.service;

import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.enums.ExchangeCode;

import java.util.List;

/**
 * 交易所服务接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
public interface ExchangeService {
    
    /**
     * 获取所有交易所
     * 
     * @return 交易所列表
     */
    List<Exchange> getAllExchanges();
    
    /**
     * 根据代码获取交易所
     * 
     * @param code 交易所代码
     * @return 交易所对象
     */
    Exchange getByCode(ExchangeCode code);
    
    /**
     * 初始化交易所数据
     */
    void initExchanges();
}
