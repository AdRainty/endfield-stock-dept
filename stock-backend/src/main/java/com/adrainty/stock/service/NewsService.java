package com.adrainty.stock.service;

import com.adrainty.stock.entity.News;
import java.util.List;

/**
 * 新闻服务接口
 *
 * @author adrainty
 * @since 2026-03-05
 */
public interface NewsService {

    /**
     * 生成早报新闻（两个交易所）
     */
    void generateMorningNews();

    /**
     * 生成晚报新闻（两个交易所）
     */
    void generateEveningNews();

    /**
     * 分析新闻对品种的利好利空程度
     *
     * @param news 新闻
     * @param instrumentCode 品种代码
     * @return 影响幅度（-30% 到 30%）
     */
    Double analyzeSentiment(News news, String instrumentCode);

    /**
     * 获取最新新闻列表
     *
     * @param limit 数量
     * @return 新闻列表
     */
    List<News> getLatestNews(int limit);

    /**
     * 根据交易所获取新闻
     *
     * @param exchangeId 交易所 ID
     * @return 新闻列表
     */
    List<News> getNewsByExchange(Long exchangeId);

    /**
     * 根据品种获取新闻
     *
     * @param instrumentCode 品种代码
     * @return 新闻列表
     */
    List<News> getNewsByInstrument(String instrumentCode);

    /**
     * 执行集合竞价价格更新
     * 根据未处理的新闻更新品种价格
     */
    void executeCallAuction();

    /**
     * 保存新闻
     *
     * @param news 新闻对象
     */
    void saveNews(News news);
}
