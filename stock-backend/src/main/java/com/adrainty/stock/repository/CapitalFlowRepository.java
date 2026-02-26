package com.adrainty.stock.repository;

import com.adrainty.stock.entity.CapitalFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资金流水数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface CapitalFlowRepository extends JpaRepository<CapitalFlow, Long> {
    
    /**
     * 根据用户 ID 查找资金流水
     * 
     * @param userId 用户 ID
     * @return 资金流水列表
     */
    List<CapitalFlow> findByUserIdOrderByOperateTimeDesc(Long userId);
    
    /**
     * 根据用户 ID 和交易所 ID 查找资金流水
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @return 资金流水列表
     */
    List<CapitalFlow> findByUserIdAndExchangeIdOrderByOperateTimeDesc(
        Long userId, Long exchangeId);
    
    /**
     * 根据流水号查找资金流水
     * 
     * @param flowNo 流水号
     * @return 资金流水对象
     */
    java.util.Optional<CapitalFlow> findByFlowNo(String flowNo);
}
