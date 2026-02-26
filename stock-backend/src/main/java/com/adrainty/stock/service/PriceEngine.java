package com.adrainty.stock.service;

import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.PriceHistory;
import com.adrainty.stock.mapper.InstrumentMapper;
import com.adrainty.stock.mapper.PriceHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 价格波动引擎
 * 使用随机游走算法模拟价格波动
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceEngine {

    private final InstrumentMapper instrumentMapper;
    private final PriceHistoryMapper priceHistoryMapper;
    
    private final Random random = new Random();
    
    /**
     * 每 5 秒更新一次价格
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void updatePrices() {
        List<Instrument> instruments = instrumentMapper.selectList(null);
        
        for (Instrument instrument : instruments) {
            if (instrument.getStatus() == 1) {
                double currentPrice = instrument.getCurrentPrice().doubleValue();
                
                // 随机波动：-2% 到 +2%
                double changePercent = (random.nextDouble() - 0.5) * 0.04;
                double newPrice = currentPrice * (1 + changePercent);
                
                // 确保价格不低于 0.01
                newPrice = Math.max(0.01, newPrice);
                
                // 更新价格
                updatePrice(instrument, newPrice);
                
                // 记录价格历史（1 分钟 K 线）
                recordPriceHistory(instrument);
            }
        }
        
        log.debug("价格更新完成，共{}个品种", instruments.size());
    }
    
    /**
     * 更新价格
     */
    private void updatePrice(Instrument instrument, double newPrice) {
        BigDecimal price = BigDecimal.valueOf(newPrice);
        instrument.setCurrentPrice(price);
        
        // 更新最高最低价
        if (instrument.getHighPrice() == null || price.compareTo(instrument.getHighPrice()) > 0) {
            instrument.setHighPrice(price);
        }
        if (instrument.getLowPrice() == null || price.compareTo(instrument.getLowPrice()) < 0) {
            instrument.setLowPrice(price);
        }
        
        // 计算涨跌
        BigDecimal changeAmount = price.subtract(instrument.getPrevClosePrice());
        BigDecimal changePercent = changeAmount.divide(instrument.getPrevClosePrice(), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        instrument.setChangeAmount(changeAmount);
        instrument.setChangePercent(changePercent);
        
        instrumentMapper.updateById(instrument);
    }
    
    /**
     * 记录价格历史（用于 K 线图）
     */
    private void recordPriceHistory(Instrument instrument) {
        PriceHistory history = new PriceHistory();
        history.setExchangeId(instrument.getExchangeId());
        history.setInstrumentCode(instrument.getInstrumentCode());
        history.setPeriod("1m");
        history.setTradeTime(LocalDateTime.now());
        history.setOpenPrice(instrument.getCurrentPrice());
        history.setHighPrice(instrument.getHighPrice());
        history.setLowPrice(instrument.getLowPrice());
        history.setClosePrice(instrument.getCurrentPrice());
        history.setVolume(BigDecimal.valueOf(instrument.getVolume()));
        history.setTurnover(instrument.getTurnover());
        
        priceHistoryMapper.insert(history);
    }
    
    /**
     * 生成 1 分钟 K 线汇总
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void aggregate1mKline() {
        // 可以在这里实现更复杂的 K 线聚合逻辑
        log.debug("1 分钟 K 线聚合完成");
    }
}
