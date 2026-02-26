package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.KlineDTO;
import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.PriceHistory;
import com.adrainty.stock.mapper.InstrumentMapper;
import com.adrainty.stock.mapper.PriceHistoryMapper;
import com.adrainty.stock.service.KlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * K 线服务实现类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KlineServiceImpl implements KlineService {

    private final PriceHistoryMapper priceHistoryMapper;
    private final InstrumentMapper instrumentMapper;

    @Override
    public List<KlineDTO> getKline(Long exchangeId, String instrumentCode, String period, Integer limit) {
        List<PriceHistory> historyList = getHistoryData(exchangeId, instrumentCode, period, limit);
        return buildKlineDTO(historyList, period);
    }

    /**
     * 获取历史数据
     */
    private List<PriceHistory> getHistoryData(Long exchangeId, String instrumentCode, String period, Integer limit) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = getStartTime(now, period, limit);

        // 查询数据
        List<PriceHistory> rawList = priceHistoryMapper
            .findByExchangeIdAndInstrumentCodeAndPeriodAndTradeTimeBetweenOrderByTradeTimeAsc(
                exchangeId, instrumentCode, "1m", startTime, now);

        // 如果没有数据，返回空列表
        if (rawList == null || rawList.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据周期聚合数据
        return aggregateByPeriod(rawList, period);
    }

    /**
     * 根据周期聚合数据
     */
    private List<PriceHistory> aggregateByPeriod(List<PriceHistory> rawList, String period) {
        if ("1m".equals(period) || "1T".equals(period)) {
            // 分时数据，直接返回
            return rawList;
        }

        List<PriceHistory> result = new ArrayList<>();

        if ("1d".equals(period) || "1D".equals(period)) {
            // 日 K：按天聚合
            result = rawList.stream()
                .collect(Collectors.groupingBy(
                    h -> h.getTradeTime().toLocalDate()))
                .entrySet().stream()
                .map(entry -> aggregateDay(entry.getValue()))
                .sorted(Comparator.comparing(PriceHistory::getTradeTime))
                .collect(Collectors.toList());
        } else if ("1M".equals(period)) {
            // 月 K：按月聚合
            result = rawList.stream()
                .collect(Collectors.groupingBy(
                    h -> h.getTradeTime().getMonthValue()))
                .entrySet().stream()
                .map(entry -> aggregateMonth(entry.getValue(), entry.getKey()))
                .sorted(Comparator.comparing(PriceHistory::getTradeTime))
                .collect(Collectors.toList());
        } else if ("1Y".equals(period)) {
            // 年 K：按年聚合
            result = rawList.stream()
                .collect(Collectors.groupingBy(
                    h -> h.getTradeTime().getYear()))
                .entrySet().stream()
                .map(entry -> aggregateYear(entry.getValue(), entry.getKey()))
                .sorted(Comparator.comparing(PriceHistory::getTradeTime))
                .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * 聚合天数据
     */
    private PriceHistory aggregateDay(List<PriceHistory> dayData) {
        PriceHistory ph = new PriceHistory();
        ph.setTradeTime(dayData.get(0).getTradeTime().withHour(0).withMinute(0).withSecond(0).withNano(0));
        ph.setOpenPrice(dayData.get(0).getOpenPrice());
        ph.setHighPrice(dayData.stream().map(PriceHistory::getHighPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        ph.setLowPrice(dayData.stream().map(PriceHistory::getLowPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        ph.setClosePrice(dayData.get(dayData.size() - 1).getClosePrice());
        ph.setVolume(dayData.stream().map(PriceHistory::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add));
        ph.setTurnover(dayData.stream().map(PriceHistory::getTurnover).reduce(BigDecimal.ZERO, BigDecimal::add));
        return ph;
    }

    /**
     * 聚合月数据
     */
    private PriceHistory aggregateMonth(List<PriceHistory> monthData, int month) {
        PriceHistory ph = new PriceHistory();
        ph.setTradeTime(monthData.get(0).getTradeTime().withMonth(month).withDayOfMonth(1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0));
        ph.setOpenPrice(monthData.get(0).getOpenPrice());
        ph.setHighPrice(monthData.stream().map(PriceHistory::getHighPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        ph.setLowPrice(monthData.stream().map(PriceHistory::getLowPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        ph.setClosePrice(monthData.get(monthData.size() - 1).getClosePrice());
        ph.setVolume(monthData.stream().map(PriceHistory::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add));
        ph.setTurnover(monthData.stream().map(PriceHistory::getTurnover).reduce(BigDecimal.ZERO, BigDecimal::add));
        return ph;
    }

    /**
     * 聚合年数据
     */
    private PriceHistory aggregateYear(List<PriceHistory> yearData, int year) {
        PriceHistory ph = new PriceHistory();
        ph.setTradeTime(yearData.get(0).getTradeTime().withYear(year).withMonth(1).withDayOfMonth(1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0));
        ph.setOpenPrice(yearData.get(0).getOpenPrice());
        ph.setHighPrice(yearData.stream().map(PriceHistory::getHighPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        ph.setLowPrice(yearData.stream().map(PriceHistory::getLowPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        ph.setClosePrice(yearData.get(yearData.size() - 1).getClosePrice());
        ph.setVolume(yearData.stream().map(PriceHistory::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add));
        ph.setTurnover(yearData.stream().map(PriceHistory::getTurnover).reduce(BigDecimal.ZERO, BigDecimal::add));
        return ph;
    }

    /**
     * 计算开始时间
     */
    private LocalDateTime getStartTime(LocalDateTime now, String period, Integer limit) {
        int bars = limit != null ? limit : 100;

        switch (period) {
            case "1m":
            case "1T":
                // 分时：向前推 bars 分钟
                return now.getMinute() > 0 ? now.withSecond(0).withNano(0).minusMinutes(bars) : now.minusMinutes(bars);
            case "1d":
            case "1D":
                // 日 K：向前推 bars 天
                return now.minusDays(bars);
            case "1M":
                // 月 K：向前推 bars 个月
                return now.minusMonths(bars);
            case "1Y":
                // 年 K：向前推 bars 年
                return now.minusYears(bars);
            default:
                return now.minusDays(bars);
        }
    }

    /**
     * 构建 K 线 DTO
     */
    private List<KlineDTO> buildKlineDTO(List<PriceHistory> historyList, String period) {
        List<KlineDTO> result = new ArrayList<>();

        // 获取 prevClosePrice 用于计算涨跌
        Instrument instrument = null;
        if (!historyList.isEmpty()) {
            instrument = instrumentMapper.findByInstrumentCode(historyList.get(0).getInstrumentCode());
        }

        BigDecimal prevClose = instrument != null ? instrument.getPrevClosePrice() : BigDecimal.ZERO;

        for (PriceHistory ph : historyList) {
            KlineDTO dto = new KlineDTO();
            dto.setTime(ph.getTradeTime());
            dto.setOpen(ph.getOpenPrice());
            dto.setHigh(ph.getHighPrice());
            dto.setLow(ph.getLowPrice());
            dto.setClose(ph.getClosePrice());
            dto.setVolume(ph.getVolume());
            dto.setTurnover(ph.getTurnover());

            // 计算涨跌
            if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                dto.setChangeAmount(ph.getClosePrice().subtract(prevClose));
                dto.setChangePercent(
                    ph.getClosePrice().subtract(prevClose)
                        .divide(prevClose, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100)));
            }

            result.add(dto);
        }

        return result;
    }
}
