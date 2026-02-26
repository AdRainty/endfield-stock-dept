package com.adrainty.stock.service;

import com.adrainty.stock.dto.InstrumentDTO;
import com.adrainty.stock.entity.Instrument;

import java.util.List;

/**
 * 品种服务接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
public interface InstrumentService {

    /**
     * 获取所有品种
     *
     * @return 品种列表
     */
    List<InstrumentDTO> getAllInstruments();

    /**
     * 获取交易所下所有品种
     *
     * @param exchangeId 交易所 ID
     * @return 品种列表
     */
    List<InstrumentDTO> getByExchangeId(Long exchangeId);

    /**
     * 根据代码获取品种
     *
     * @param instrumentCode 品种代码
     * @return 品种 DTO
     */
    InstrumentDTO getByCode(String instrumentCode);

    /**
     * 初始化品种数据
     */
    void initInstruments();

    /**
     * 更新品种价格
     *
     * @param instrument 品种对象
     * @param newPrice 新价格
     */
    void updatePrice(Instrument instrument, double newPrice);
}
