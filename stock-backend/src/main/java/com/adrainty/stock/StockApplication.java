package com.adrainty.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 终末地调度卷交易模拟系统 - 主启动类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@SpringBootApplication
@EnableScheduling
public class StockApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
        System.out.println("========================================");
        System.out.println("  终末地调度卷交易模拟系统启动成功!");
        System.out.println("========================================");
    }
}
