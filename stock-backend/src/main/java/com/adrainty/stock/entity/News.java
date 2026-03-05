package com.adrainty.stock.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 新闻实体类
 *
 * @author adrainty
 * @since 2026-03-05
 */
@Getter
@Setter
@TableName("news")
public class News extends BaseEntity {

    /**
     * 新闻标题
     */
    @TableField("title")
    private String title;

    /**
     * 新闻内容
     */
    @TableField("content")
    private String content;

    /**
     * 所属交易所 ID（1-四号谷底，2-武陵，0-通用）
     */
    @TableField("exchange_id")
    private Long exchangeId;

    /**
     * 关联品种代码（可选）
     */
    @TableField("instrument_code")
    private String instrumentCode;

    /**
     * 新闻类型：1-早报 2-晚报 3-实时新闻
     */
    @TableField("news_type")
    private Integer newsType;

    /**
     * 利好利空等级：-3-重大利空 -2-利空 -1-偏空 0-中性 1-利好 2-重大利好
     */
    @TableField("sentiment_level")
    private Integer sentimentLevel;

    /**
     * 影响幅度百分比（-30% 到 30%）
     */
    @TableField("impact_percent")
    private Double impactPercent;

    /**
     * 是否已处理（用于集合竞价）
     */
    @TableField("processed")
    private Boolean processed = false;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private java.time.LocalDateTime publishTime;

    /**
     * 来源（AI 生成/网络抓取）
     */
    @TableField("source")
    private String source;

    /**
     * 参考新闻 ID 列表（JSON 数组）
     */
    @TableField("reference_ids")
    private String referenceIds;
}
