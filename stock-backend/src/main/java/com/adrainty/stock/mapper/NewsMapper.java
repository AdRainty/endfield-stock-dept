package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.News;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻 Mapper 接口
 *
 * @author adrainty
 * @since 2026-03-05
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {

    /**
     * 根据交易所 ID 查找新闻
     *
     * @param exchangeId 交易所 ID
     * @return 新闻列表
     */
    @Select("SELECT * FROM news WHERE exchange_id = #{exchangeId} OR exchange_id = 0 ORDER BY publish_time DESC")
    List<News> findByExchangeId(@Param("exchangeId") Long exchangeId);

    /**
     * 根据品种代码查找新闻
     *
     * @param instrumentCode 品种代码
     * @return 新闻列表
     */
    @Select("SELECT * FROM news WHERE instrument_code = #{instrumentCode} OR exchange_id = 0 ORDER BY publish_time DESC")
    List<News> findByInstrumentCode(@Param("instrumentCode") String instrumentCode);

    /**
     * 查找最新 N 条新闻
     *
     * @param limit 数量
     * @return 新闻列表
     */
    @Select("SELECT * FROM news ORDER BY publish_time DESC LIMIT #{limit}")
    List<News> findLatest(@Param("limit") int limit);

    /**
     * 查找指定时间范围内的新闻
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新闻列表
     */
    @Select("SELECT * FROM news WHERE publish_time BETWEEN #{startTime} AND #{endTime} ORDER BY publish_time DESC")
    List<News> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查找未处理的新闻
     *
     * @return 新闻列表
     */
    @Select("SELECT * FROM news WHERE processed = false ORDER BY publish_time ASC")
    List<News> findUnprocessed();

    /**
     * 查找指定类型的新闻
     *
     * @param newsType 新闻类型
     * @return 新闻列表
     */
    @Select("SELECT * FROM news WHERE news_type = #{newsType} ORDER BY publish_time DESC")
    List<News> findByType(@Param("newsType") Integer newsType);
}
