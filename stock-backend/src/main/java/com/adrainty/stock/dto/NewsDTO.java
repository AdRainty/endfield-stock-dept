package com.adrainty.stock.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻 DTO
 *
 * @author adrainty
 * @since 2026-03-05
 */
@Data
public class NewsDTO {

    private Long id;

    /**
     * 新闻标题
     */
    private String title;

    /**
     * 新闻内容
     */
    private String content;

    /**
     * 交易所 ID
     */
    private Long exchangeId;

    /**
     * 交易所名称
     */
    private String exchangeName;

    /**
     * 品种代码
     */
    private String instrumentCode;

    /**
     * 品种名称
     */
    private String instrumentName;

    /**
     * 新闻类型：1-早报 2-晚报 3-实时新闻
     */
    private Integer newsType;

    /**
     * 新闻类型描述
     */
    private String newsTypeDesc;

    /**
     * 利好利空等级：-3-重大利空 -2-利空 -1-偏空 0-中性 1-利好 2-重大利好
     */
    private Integer sentimentLevel;

    /**
     *  sentiment 描述
     */
    private String sentimentDesc;

    /**
     * 影响幅度百分比
     */
    private Double impactPercent;

    /**
     * 是否已处理
     */
    private Boolean processed;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /**
     * 来源
     */
    private String source;

    /**
     * 获取 sentiment 描述
     */
    public String getSentimentDesc() {
        if (sentimentLevel == null) return "中性";
        return switch (sentimentLevel) {
            case 3 -> "重大利好";
            case 2 -> "利好";
            case 1 -> "偏多";
            case -1 -> "偏空";
            case -2 -> "利空";
            case -3 -> "重大利空";
            default -> "中性";
        };
    }

    /**
     * 获取新闻类型描述
     */
    public String getNewsTypeDesc() {
        if (newsType == null) return "未知";
        return switch (newsType) {
            case 1 -> "早报";
            case 2 -> "晚报";
            case 3 -> "实时";
            default -> "未知";
        };
    }
}
