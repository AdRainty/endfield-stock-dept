package com.adrainty.stock.service;

import com.adrainty.stock.dto.KlineDTO;

import java.util.List;

/**
 * K 线服务接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
public interface KlineService {

    /**
     * 获取 K 线数据
     *
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @param period 周期：1m-分时 1d-日 K 1M-月 K 1Y-年 K
     * @param limit 数据条数
     * @return K 线数据列表
     */
    List<KlineDTO> getKline(Long exchangeId, String instrumentCode, String period, Integer limit);
}
