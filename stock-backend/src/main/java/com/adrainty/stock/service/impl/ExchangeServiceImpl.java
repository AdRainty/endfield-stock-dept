package com.adrainty.stock.service.impl;

import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.enums.ExchangeCode;
import com.adrainty.stock.repository.ExchangeRepository;
import com.adrainty.stock.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 交易所服务实现类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService, CommandLineRunner {
    
    private final ExchangeRepository exchangeRepository;
    
    @Override
    public List<Exchange> getAllExchanges() {
        return exchangeRepository.findByStatus(1);
    }
    
    @Override
    public Exchange getByCode(ExchangeCode code) {
        return exchangeRepository.findByExchangeCode(code).orElse(null);
    }
    
    @Override
    @Transactional
    public void initExchanges() {
        // 初始化四号谷底交易所
        if (!exchangeRepository.existsByExchangeCode(ExchangeCode.VALLEY)) {
            Exchange valley = new Exchange();
            valley.setExchangeCode(ExchangeCode.VALLEY);
            valley.setName("四号谷底");
            valley.setDescription("位于四号谷底的交易所，主要交易能源类调度券");
            valley.setStatus(1);
            valley.setTradingStart("00:00");
            valley.setTradingEnd("23:59");
            exchangeRepository.save(valley);
            log.info("初始化交易所：{}", valley.getName());
        }
        
        // 初始化武陵交易所
        if (!exchangeRepository.existsByExchangeCode(ExchangeCode.WULING)) {
            Exchange wuling = new Exchange();
            wuling.setExchangeCode(ExchangeCode.WULING);
            wuling.setName("武陵");
            wuling.setDescription("位于武陵地区的交易所，主要交易技术类调度券");
            wuling.setStatus(1);
            wuling.setTradingStart("00:00");
            wuling.setTradingEnd("23:59");
            exchangeRepository.save(wuling);
            log.info("初始化交易所：{}", wuling.getName());
        }
    }
    
    @Override
    public void run(String... args) {
        initExchanges();
    }
}
