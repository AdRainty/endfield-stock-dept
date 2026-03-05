package com.adrainty.stock.config;

import com.adrainty.stock.service.MatchingEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 交易时间调度配置
 *
 * @author adrainty
 * @since 2026-03-05
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradingScheduleConfig {

    private final MatchingEngineService matchingEngineService;

    /**
     * 应用启动后启动所有交易品种的撮合线程
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startMatchingEngines() {
        log.info("启动所有交易品种的撮合引擎...");
        matchingEngineService.startAllMatchingEngines();
    }
}
