package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.InstrumentDTO;
import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.mapper.InstrumentMapper;
import com.adrainty.stock.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 品种服务实现类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentServiceImpl implements InstrumentService, CommandLineRunner {

    private final InstrumentMapper instrumentMapper;

    @Override
    public List<InstrumentDTO> getAllInstruments() {
        List<Instrument> instruments = instrumentMapper.selectList(null);
        return instruments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<InstrumentDTO> getByExchangeId(Long exchangeId) {
        List<Instrument> instruments = instrumentMapper.findByExchangeId(exchangeId);
        return instruments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public InstrumentDTO getByCode(String instrumentCode) {
        Instrument instrument = instrumentMapper.findByInstrumentCode(instrumentCode);
        return instrument != null ? convertToDTO(instrument) : null;
    }

    @Override
    @Transactional
    public void initInstruments() {
        // 四号谷底交易所品种 (exchange_id = 1)
        initInstrument("VL_ENERGY", "能源调度券", 1L, 100.00);
        initInstrument("VL_MATERIAL", "材料调度券", 1L, 50.00);
        initInstrument("VL_DATA", "数据调度券", 1L, 75.00);
        initInstrument("VL_TECH", "技术调度券", 1L, 120.00);

        // 武陵交易所品种 (exchange_id = 2)
        initInstrument("WL_ENERGY", "能源调度券", 2L, 100.00);
        initInstrument("WL_MATERIAL", "材料调度券", 2L, 50.00);
        initInstrument("WL_DATA", "数据调度券", 2L, 75.00);
        initInstrument("WL_TECH", "技术调度券", 2L, 120.00);

        log.info("初始化品种数据完成");
    }

    private void initInstrument(String code, String name, Long exchangeId, double price) {
        Instrument existing = instrumentMapper.findByInstrumentCode(code);
        if (existing == null) {
            Instrument instrument = new Instrument();
            instrument.setInstrumentCode(code);
            instrument.setName(name);
            instrument.setExchangeId(exchangeId);
            instrument.setCurrentPrice(BigDecimal.valueOf(price));
            instrument.setPrevClosePrice(BigDecimal.valueOf(price));
            instrument.setOpenPrice(BigDecimal.valueOf(price));
            instrument.setHighPrice(BigDecimal.valueOf(price));
            instrument.setLowPrice(BigDecimal.valueOf(price));
            instrument.setChangePercent(BigDecimal.ZERO);
            instrument.setChangeAmount(BigDecimal.ZERO);
            instrument.setVolume(0L);
            instrument.setTurnover(BigDecimal.ZERO);
            instrument.setStatus(1);
            instrumentMapper.insert(instrument);
            log.info("初始化品种：{} - {}", code, name);
        }
    }

    @Override
    @Transactional
    public void updatePrice(Instrument instrument, double newPrice) {
        BigDecimal price = BigDecimal.valueOf(newPrice);
        instrument.setCurrentPrice(price);

        if (instrument.getHighPrice() == null || price.compareTo(instrument.getHighPrice()) > 0) {
            instrument.setHighPrice(price);
        }
        if (instrument.getLowPrice() == null || price.compareTo(instrument.getLowPrice()) < 0) {
            instrument.setLowPrice(price);
        }

        BigDecimal changeAmount = price.subtract(instrument.getPrevClosePrice());
        BigDecimal changePercent = changeAmount.divide(instrument.getPrevClosePrice(), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        instrument.setChangeAmount(changeAmount);
        instrument.setChangePercent(changePercent);

        instrumentMapper.updateById(instrument);
    }

    private InstrumentDTO convertToDTO(Instrument instrument) {
        InstrumentDTO dto = new InstrumentDTO();
        dto.setId(instrument.getId());
        dto.setInstrumentCode(instrument.getInstrumentCode());
        dto.setName(instrument.getName());
        dto.setExchangeId(instrument.getExchangeId());
        dto.setCurrentPrice(instrument.getCurrentPrice());
        dto.setPrevClosePrice(instrument.getPrevClosePrice());
        dto.setOpenPrice(instrument.getOpenPrice());
        dto.setHighPrice(instrument.getHighPrice());
        dto.setLowPrice(instrument.getLowPrice());
        dto.setChangePercent(instrument.getChangePercent());
        dto.setChangeAmount(instrument.getChangeAmount());
        dto.setVolume(instrument.getVolume());
        dto.setTurnover(instrument.getTurnover());
        dto.setStatus(instrument.getStatus());
        return dto;
    }

    @Override
    public void run(String... args) {
        initInstruments();
    }
}
