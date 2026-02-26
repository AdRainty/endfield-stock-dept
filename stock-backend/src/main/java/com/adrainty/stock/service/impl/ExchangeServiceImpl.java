package com.adrainty.stock.service.impl;

import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.enums.ExchangeCode;
import com.adrainty.stock.mapper.ExchangeMapper;
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

    private final ExchangeMapper exchangeMapper;

    @Override
    public List<Exchange> getAllExchanges() {
        return exchangeMapper.findByStatus(1);
    }

    @Override
    public Exchange getByCode(ExchangeCode code) {
        return exchangeMapper.findByExchangeCode(code);
    }

    @Override
    @Transactional
    public void initExchanges() {
        // 初始化四号谷底交易所
        Exchange existingValley = exchangeMapper.findByExchangeCode(ExchangeCode.VALLEY);
        if (existingValley == null) {
            Exchange valley = new Exchange();
            valley.setExchangeCode(ExchangeCode.VALLEY);
            valley.setName("四号谷底");
            valley.setDescription("位于四号谷底的交易所，主要交易能源类调度券");
            valley.setStatus(1);
            valley.setTradingStart("00:00");
            valley.setTradingEnd("23:59");
            exchangeMapper.insert(valley);
            log.info("初始化交易所：{}", valley.getName());
        }

        // 初始化武陵交易所
        Exchange existingWuling = exchangeMapper.findByExchangeCode(ExchangeCode.WULING);
        if (existingWuling == null) {
            Exchange wuling = new Exchange();
            wuling.setExchangeCode(ExchangeCode.WULING);
            wuling.setName("武陵");
            wuling.setDescription("位于武陵地区的交易所，主要交易技术类调度券");
            wuling.setStatus(1);
            wuling.setTradingStart("00:00");
            wuling.setTradingEnd("23:59");
            exchangeMapper.insert(wuling);
            log.info("初始化交易所：{}", wuling.getName());
        }
    }

    @Override
    public void run(String... args) {
        initExchanges();
    }
}
