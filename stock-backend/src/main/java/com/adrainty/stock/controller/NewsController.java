package com.adrainty.stock.controller;

import com.adrainty.stock.dto.NewsDTO;
import com.adrainty.stock.entity.News;
import com.adrainty.stock.exception.GlobalExceptionHandler;
import com.adrainty.stock.mapper.NewsMapper;
import com.adrainty.stock.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 新闻控制器
 *
 * @author adrainty
 * @since 2026-03-05
 */
@Tag(name = "新闻管理", description = "新闻查询、生成相关接口")
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final NewsMapper newsMapper;

    /**
     * 获取最新新闻列表
     */
    @Operation(summary = "获取最新新闻", description = "获取最新生成的新闻列表")
    @GetMapping("/latest")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<NewsDTO>>> getLatestNews(
            @RequestParam(defaultValue = "20") int limit) {
        List<News> newsList = newsService.getLatestNews(limit);
        List<NewsDTO> dtos = newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(dtos));
    }

    /**
     * 根据交易所获取新闻
     */
    @Operation(summary = "获取交易所新闻", description = "获取指定交易所的新闻列表")
    @GetMapping("/exchange/{exchangeId}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<NewsDTO>>> getNewsByExchange(
            @PathVariable Long exchangeId) {
        List<News> newsList = newsService.getNewsByExchange(exchangeId);
        List<NewsDTO> dtos = newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(dtos));
    }

    /**
     * 根据品种获取新闻
     */
    @Operation(summary = "获取品种新闻", description = "获取指定品种的新闻列表")
    @GetMapping("/instrument/{instrumentCode}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<List<NewsDTO>>> getNewsByInstrument(
            @PathVariable String instrumentCode) {
        List<News> newsList = newsService.getNewsByInstrument(instrumentCode);
        List<NewsDTO> dtos = newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(dtos));
    }

    /**
     * 手动触发早报生成
     */
    @Operation(summary = "生成早报", description = "手动触发早报新闻生成")
    @PostMapping("/generate/morning/{exchangeId}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<NewsDTO>> generateMorningNews(
            @PathVariable Long exchangeId) {
        News news = newsService.generateMorningNews(exchangeId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(convertToDTO(news)));
    }

    /**
     * 手动触发晚报生成
     */
    @Operation(summary = "生成晚报", description = "手动触发晚报新闻生成")
    @PostMapping("/generate/evening/{exchangeId}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<NewsDTO>> generateEveningNews(
            @PathVariable Long exchangeId) {
        News news = newsService.generateEveningNews(exchangeId);
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(convertToDTO(news)));
    }

    /**
     * 手动触发集合竞价
     */
    @Operation(summary = "执行集合竞价", description = "手动触发集合竞价价格更新")
    @PostMapping("/call-auction")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<Boolean>> executeCallAuction() {
        newsService.executeCallAuction();
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(true));
    }

    /**
     * 获取新闻详情
     */
    @Operation(summary = "获取新闻详情", description = "获取单条新闻的详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<GlobalExceptionHandler.ApiResult<NewsDTO>> getNewsDetail(
            @PathVariable Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(null));
        }
        return ResponseEntity.ok(GlobalExceptionHandler.ApiResult.success(convertToDTO(news)));
    }

    /**
     * 转换为 DTO
     */
    private NewsDTO convertToDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setExchangeId(news.getExchangeId());
        dto.setExchangeName(news.getExchangeId() == 1 ? "四号谷底" : "武陵");
        dto.setInstrumentCode(news.getInstrumentCode());
        dto.setNewsType(news.getNewsType());
        dto.setSentimentLevel(news.getSentimentLevel());
        dto.setImpactPercent(news.getImpactPercent() != null ? news.getImpactPercent() * 100 : 0.0);
        dto.setProcessed(news.getProcessed());
        dto.setPublishTime(news.getPublishTime());
        dto.setSource(news.getSource());
        return dto;
    }
}
