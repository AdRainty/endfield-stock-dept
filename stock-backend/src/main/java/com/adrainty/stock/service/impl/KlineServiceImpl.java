package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.KlineDTO;
import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.TradeRecord;
import com.adrainty.stock.mapper.InstrumentMapper;
import com.adrainty.stock.mapper.TradeRecordMapper;
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
 * K 线服务实现类（基于 TradeRecord）
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KlineServiceImpl implements KlineService {

    private final TradeRecordMapper tradeRecordMapper;
    private final InstrumentMapper instrumentMapper;

    @Override
    public List<KlineDTO> getKline(Long exchangeId, String instrumentCode, String period, Integer limit) {
        // 获取原始交易记录
        List<TradeRecord> tradeList = getTradeData(exchangeId, instrumentCode, period, limit);

        // 获取品种信息（用于在没有交易时返回当前价格）
        Instrument instrument = instrumentMapper.findByInstrumentCode(instrumentCode);

        // 如果没有交易数据，返回基于当前价格的 K 线数据
        if (tradeList.isEmpty()) {
            return generateKlineFromInstrument(instrument, period, limit);
        }

        // 根据周期聚合数据
        List<KlineDTO> klineList = aggregateByPeriod(tradeList, period);

        // 计算涨跌
        return buildKlineDTO(klineList, period, exchangeId, instrumentCode);
    }

    /**
     * 从品种信息生成 K 线数据（用于没有交易的情况）
     */
    private List<KlineDTO> generateKlineFromInstrument(Instrument instrument, String period, Integer limit) {
        List<KlineDTO> result = new ArrayList<>();
        if (instrument == null) {
            return result;
        }

        BigDecimal price = instrument.getCurrentPrice();
        if (price == null) {
            price = instrument.getPrevClosePrice();
        }
        if (price == null) {
            price = BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        int bars = limit != null ? limit : 100;

        // 根据周期生成 K 线
        if ("1m".equals(period) || "1T".equals(period)) {
            // 分时图：生成 240 分钟的数据（9:30-15:00）
            for (int i = 0; i < 240; i++) {
                int totalMinutes = 570 + i; // 9:30 = 9*60+30 = 570
                int hour = totalMinutes / 60;
                int minute = totalMinutes % 60;
                LocalDateTime time = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
                result.add(createKlineDTO(time, price, instrument));
            }
        } else if ("1d".equals(period) || "1D".equals(period)) {
            // 日 K：生成 bars 天的数据
            for (int i = 0; i < bars; i++) {
                LocalDateTime time = now.minusDays(bars - 1 - i).withHour(0).withMinute(0).withSecond(0).withNano(0);
                result.add(createKlineDTO(time, price, instrument));
            }
        } else if ("1M".equals(period)) {
            // 月 K：生成 12 个月的数据
            for (int i = 0; i < 12; i++) {
                LocalDateTime time = now.withMonth(i + 1).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
                result.add(createKlineDTO(time, price, instrument));
            }
        } else if ("1Y".equals(period)) {
            // 年 K：生成 10 年的数据
            for (int i = 0; i < 10; i++) {
                LocalDateTime time = now.minusYears(9 - i).withMonth(1).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
                result.add(createKlineDTO(time, price, instrument));
            }
        }

        return result;
    }

    /**
     * 创建单根 K 线 DTO
     */
    private KlineDTO createKlineDTO(LocalDateTime time, BigDecimal price, Instrument instrument) {
        KlineDTO dto = new KlineDTO();
        dto.setTime(time);
        dto.setOpen(price);
        dto.setHigh(price);
        dto.setLow(price);
        dto.setClose(price);
        dto.setVolume(BigDecimal.ZERO);
        dto.setTurnover(BigDecimal.ZERO);
        return dto;
    }

    /**
     * 获取交易数据
     */
    private List<TradeRecord> getTradeData(Long exchangeId, String instrumentCode, String period, Integer limit) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = getStartTime(now, period, limit);

        // 查询交易记录
        List<TradeRecord> rawList = tradeRecordMapper
            .findByExchangeIdAndInstrumentCodeAndTradeTimeBetweenOrderByTradeTimeAsc(
                exchangeId, instrumentCode, startTime, now);

        // 如果没有数据，返回空列表
        if (rawList == null || rawList.isEmpty()) {
            return new ArrayList<>();
        }

        return rawList;
    }

    /**
     * 根据周期聚合数据
     */
    private List<KlineDTO> aggregateByPeriod(List<TradeRecord> rawList, String period) {
        if ("1m".equals(period) || "1T".equals(period)) {
            // 分时数据：按分钟聚合
            return rawList.stream()
                .collect(Collectors.groupingBy(
                    h -> h.getTradeTime().withSecond(0).withNano(0))) // 按分钟分组
                .entrySet().stream()
                .map(entry -> aggregateMinute(entry.getValue()))
                .sorted(Comparator.comparing(KlineDTO::getTime))
                .collect(Collectors.toList());
        }

        List<KlineDTO> result = new ArrayList<>();

        if ("1d".equals(period) || "1D".equals(period)) {
            // 日 K：按天聚合
            result = rawList.stream()
                .collect(Collectors.groupingBy(
                    h -> h.getTradeTime().toLocalDate()))
                .entrySet().stream()
                .map(entry -> aggregateDay(entry.getValue()))
                .sorted(Comparator.comparing(KlineDTO::getTime))
                .collect(Collectors.toList());
        } else if ("1M".equals(period)) {
            // 月 K：按月聚合
            result = rawList.stream()
                .collect(Collectors.groupingBy(
                    h -> h.getTradeTime().getMonthValue()))
                .entrySet().stream()
                .map(entry -> aggregateMonth(entry.getValue(), entry.getKey()))
                .sorted(Comparator.comparing(KlineDTO::getTime))
                .collect(Collectors.toList());
        } else if ("1Y".equals(period)) {
            // 年 K：按年聚合
            result = rawList.stream()
                .collect(Collectors.groupingBy(
                    h -> h.getTradeTime().getYear()))
                .entrySet().stream()
                .map(entry -> aggregateYear(entry.getValue(), entry.getKey()))
                .sorted(Comparator.comparing(KlineDTO::getTime))
                .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * 将 TradeRecord 转换为 KlineDTO（用于单条记录）
     */
    private KlineDTO toKlineDTO(TradeRecord tr) {
        KlineDTO dto = new KlineDTO();
        dto.setTime(tr.getTradeTime());
        dto.setOpen(tr.getPrice());
        dto.setHigh(tr.getPrice());
        dto.setLow(tr.getPrice());
        dto.setClose(tr.getPrice());
        dto.setVolume(tr.getQuantity());
        dto.setTurnover(tr.getAmount());
        return dto;
    }

    /**
     * 聚合分钟数据（用于分时图）
     */
    private KlineDTO aggregateMinute(List<TradeRecord> minuteData) {
        KlineDTO dto = new KlineDTO();
        dto.setTime(minuteData.get(0).getTradeTime().withSecond(0).withNano(0));
        dto.setOpen(minuteData.get(0).getPrice());
        dto.setHigh(minuteData.stream().map(TradeRecord::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setLow(minuteData.stream().map(TradeRecord::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setClose(minuteData.get(minuteData.size() - 1).getPrice());
        dto.setVolume(minuteData.stream().map(TradeRecord::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setTurnover(minuteData.stream().map(TradeRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return dto;
    }

    /**
     * 聚合天数据
     */
    private KlineDTO aggregateDay(List<TradeRecord> dayData) {
        KlineDTO dto = new KlineDTO();
        dto.setTime(dayData.get(0).getTradeTime().withHour(0).withMinute(0).withSecond(0).withNano(0));
        dto.setOpen(dayData.get(0).getPrice());
        dto.setHigh(dayData.stream().map(TradeRecord::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setLow(dayData.stream().map(TradeRecord::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setClose(dayData.get(dayData.size() - 1).getPrice());
        dto.setVolume(dayData.stream().map(TradeRecord::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setTurnover(dayData.stream().map(TradeRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return dto;
    }

    /**
     * 聚合月数据
     */
    private KlineDTO aggregateMonth(List<TradeRecord> monthData, int month) {
        KlineDTO dto = new KlineDTO();
        dto.setTime(monthData.get(0).getTradeTime().withMonth(month).withDayOfMonth(1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0));
        dto.setOpen(monthData.get(0).getPrice());
        dto.setHigh(monthData.stream().map(TradeRecord::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setLow(monthData.stream().map(TradeRecord::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setClose(monthData.get(monthData.size() - 1).getPrice());
        dto.setVolume(monthData.stream().map(TradeRecord::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setTurnover(monthData.stream().map(TradeRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return dto;
    }

    /**
     * 聚合年数据
     */
    private KlineDTO aggregateYear(List<TradeRecord> yearData, int year) {
        KlineDTO dto = new KlineDTO();
        dto.setTime(yearData.get(0).getTradeTime().withYear(year).withMonth(1).withDayOfMonth(1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0));
        dto.setOpen(yearData.get(0).getPrice());
        dto.setHigh(yearData.stream().map(TradeRecord::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setLow(yearData.stream().map(TradeRecord::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        dto.setClose(yearData.get(yearData.size() - 1).getPrice());
        dto.setVolume(yearData.stream().map(TradeRecord::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setTurnover(yearData.stream().map(TradeRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return dto;
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
     * 构建 K 线 DTO（带涨跌计算）
     */
    private List<KlineDTO> buildKlineDTO(List<KlineDTO> klineList, String period, Long exchangeId, String instrumentCode) {
        if (klineList.isEmpty()) {
            return klineList;
        }

        // 获取 prevClosePrice 用于计算涨跌
        Instrument instrument = instrumentMapper.findByInstrumentCode(instrumentCode);
        BigDecimal prevClose = instrument != null ? instrument.getPrevClosePrice() : BigDecimal.ZERO;

        // 只有非分时数据才需要计算涨跌（分时数据每条就是一个交易记录）
        if (!"1m".equals(period) && !"1T".equals(period)) {
            for (KlineDTO dto : klineList) {
                // 计算涨跌
                if (prevClose.compareTo(BigDecimal.ZERO) > 0) {
                    dto.setChangeAmount(dto.getClose().subtract(prevClose));
                    dto.setChangePercent(
                        dto.getClose().subtract(prevClose)
                            .divide(prevClose, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100)));
                }
                // 更新 prevClose 为当前 K 线的收盘价，用于下一根 K 线
                prevClose = dto.getClose();
            }
        }

        return klineList;
    }
}
