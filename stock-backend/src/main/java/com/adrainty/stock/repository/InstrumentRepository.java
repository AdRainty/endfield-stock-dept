package com.adrainty.stock.repository;

import com.adrainty.stock.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 调度券品种数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    
    /**
     * 根据品种代码查找品种
     * 
     * @param instrumentCode 品种代码
     * @return 品种对象
     */
    Optional<Instrument> findByInstrumentCode(String instrumentCode);
    
    /**
     * 检查品种代码是否存在
     * 
     * @param instrumentCode 品种代码
     * @return 是否存在
     */
    boolean existsByInstrumentCode(String instrumentCode);
    
    /**
     * 根据交易所 ID 查找所有品种
     * 
     * @param exchangeId 交易所 ID
     * @return 品种列表
     */
    List<Instrument> findByExchangeId(Long exchangeId);
    
    /**
     * 根据交易所 ID 和状态查找品种
     * 
     * @param exchangeId 交易所 ID
     * @param status 状态
     * @return 品种列表
     */
    List<Instrument> findByExchangeIdAndStatus(Long exchangeId, Integer status);
}
