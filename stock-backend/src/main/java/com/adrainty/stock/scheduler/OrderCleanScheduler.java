package com.adrainty.stock.scheduler;

import com.adrainty.stock.enums.OrderStatus;
import com.adrainty.stock.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 订单清理定时任务
 * 每日收盘后自动撤销当日未成交的委托订单
 *
 * @author adrainty
 * @since 2026-02-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanScheduler {

    private final OrderService orderService;

    /**
     * 日终自动撤单任务
     * 每个交易日下午 15:30 执行（A 股收盘后）
     * 撤销当日所有未成交（PENDING）和部分成交（PARTIALLY_FILLED）的订单
     */
    @Scheduled(cron = "0 30 15 * * MON-FRI")
    public void autoCancelPendingOrders() {
        log.info("开始执行日终自动撤单任务...");

        int cancelledCount = orderService.batchCancelOrders(
            LocalDate.now(),
            OrderStatus.PENDING,
            "日终自动撤单"
        );

        log.info("日终自动撤单任务完成，共撤单 {} 笔", cancelledCount);
    }

}
