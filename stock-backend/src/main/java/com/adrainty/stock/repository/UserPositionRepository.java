package com.adrainty.stock.repository;

import com.adrainty.stock.entity.UserPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户持仓数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface UserPositionRepository extends JpaRepository<UserPosition, Long> {
    
    /**
     * 根据用户 ID 和交易所 ID 查找所有持仓
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @return 持仓列表
     */
    List<UserPosition> findByUserIdAndExchangeId(Long userId, Long exchangeId);
    
    /**
     * 根据用户 ID、交易所 ID 和品种代码查找持仓
     * 
     * @param userId 用户 ID
     * @param exchangeId 交易所 ID
     * @param instrumentCode 品种代码
     * @return 持仓对象
     */
    Optional<UserPosition> findByUserIdAndExchangeIdAndInstrumentCode(
        Long userId, Long exchangeId, String instrumentCode);
    
    /**
     * 根据用户 ID 查找所有持仓
     * 
     * @param userId 用户 ID
     * @return 持仓列表
     */
    List<UserPosition> findByUserId(Long userId);
}
