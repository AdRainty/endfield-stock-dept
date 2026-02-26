package com.adrainty.stock.repository;

import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.enums.ExchangeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 交易所数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
    /**
     * 根据交易所代码查找交易所
     * 
     * @param exchangeCode 交易所代码
     * @return 交易所对象
     */
    Optional<Exchange> findByExchangeCode(ExchangeCode exchangeCode);
    
    /**
     * 检查交易所代码是否存在
     * 
     * @param exchangeCode 交易所代码
     * @return 是否存在
     */
    boolean existsByExchangeCode(ExchangeCode exchangeCode);
    
    /**
     * 查找所有正常状态的交易所
     * 
     * @param status 状态
     * @return 交易所列表
     */
    List<Exchange> findByStatus(Integer status);
}
