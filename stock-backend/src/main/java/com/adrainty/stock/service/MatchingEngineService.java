package com.adrainty.stock.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 撮合引擎服务接口
 *
 * @author adrainty
 * @since 2026-03-05
 */
public interface MatchingEngineService {

    /**
     * 启动所有交易品种的撮合引擎
     */
    void startAllMatchingEngines();

    /**
     * 停止所有撮合引擎
     */
    void stopAllMatchingEngines();

    /**
     * 启动指定品种的撮合引擎
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     */
    void startMatchingEngine(Long exchangeId, String instrumentCode);

    /**
     * 停止指定品种的撮合引擎
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     */
    void stopMatchingEngine(Long exchangeId, String instrumentCode);

    /**
     * 添加买单到队列
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param price 价格
     * @param quantity 数量
     * @param orderId 订单 ID
     * @param timestamp 下单时间戳
     */
    void addBidOrder(Long exchangeId, String instrumentCode, BigDecimal price,
                     BigDecimal quantity, Long orderId, Long timestamp);

    /**
     * 添加卖单到队列
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param price 价格
     * @param quantity 数量
     * @param orderId 订单 ID
     * @param timestamp 下单时间戳
     */
    void addAskOrder(Long exchangeId, String instrumentCode, BigDecimal price,
                     BigDecimal quantity, Long orderId, Long timestamp);

    /**
     * 移除订单
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param orderId 订单 ID
     */
    void removeOrder(Long exchangeId, String instrumentCode, Long orderId);

    /**
     * 获取最佳买单价格
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 最佳买单价格
     */
    BigDecimal getBestBidPrice(Long exchangeId, String instrumentCode);

    /**
     * 获取最佳卖单价格
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 最佳卖单价格
     */
    BigDecimal getBestAskPrice(Long exchangeId, String instrumentCode);

    /**
     * 检查是否在交易时间段内
     *
     * @param exchangeId 交易所 ID
     * @return 是否在交易时间内
     */
    boolean isWithinTradingHours(Long exchangeId);

    /**
     * 获取距离开盘的时间（秒）
     *
     * @param exchangeId 交易所 ID
     * @return 距离开盘的秒数，如果当前在交易时间内返回 0
     */
    long getSecondsUntilMarketOpen(Long exchangeId);
}
