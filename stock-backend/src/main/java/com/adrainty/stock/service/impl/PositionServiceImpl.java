package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.PositionDTO;
import com.adrainty.stock.entity.Instrument;
import com.adrainty.stock.entity.UserPosition;
import com.adrainty.stock.repository.InstrumentRepository;
import com.adrainty.stock.repository.UserPositionRepository;
import com.adrainty.stock.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 持仓服务实现类
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {
    
    private final UserPositionRepository userPositionRepository;
    private final InstrumentRepository instrumentRepository;
    
    @Override
    public List<PositionDTO> getUserPositions(Long userId, Long exchangeId) {
        List<UserPosition> positions = userPositionRepository.findByUserIdAndExchangeId(userId, exchangeId);
        return positions.stream()
            .filter(p -> p.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public PositionDTO getPosition(Long userId, Long exchangeId, String instrumentCode) {
        UserPosition position = userPositionRepository
            .findByUserIdAndExchangeIdAndInstrumentCode(userId, exchangeId, instrumentCode)
            .orElse(null);
        return position != null ? convertToDTO(position) : null;
    }
    
    @Override
    @Transactional
    public void initPosition(Long userId, Long exchangeId) {
        // 获取交易所下所有品种
        List<Instrument> instruments = instrumentRepository.findByExchangeId(exchangeId);
        
        for (Instrument instrument : instruments) {
            UserPosition position = userPositionRepository
                .findByUserIdAndExchangeIdAndInstrumentCode(userId, exchangeId, instrument.getInstrumentCode())
                .orElse(null);
            
            if (position == null) {
                position = new UserPosition();
                position.setUserId(userId);
                position.setExchangeId(exchangeId);
                position.setInstrumentCode(instrument.getInstrumentCode());
                position.setQuantity(BigDecimal.ZERO);
                position.setAvailableQuantity(BigDecimal.ZERO);
                position.setFrozenQuantity(BigDecimal.ZERO);
                position.setCostPrice(BigDecimal.ZERO);
                position.setCostAmount(BigDecimal.ZERO);
                position.setLatestPrice(instrument.getCurrentPrice());
                position.setProfitLoss(BigDecimal.ZERO);
                position.setProfitLossRate(BigDecimal.ZERO);
                
                userPositionRepository.save(position);
            }
        }
    }
    
    /**
     * 转换为 DTO
     */
    private PositionDTO convertToDTO(UserPosition position) {
        PositionDTO dto = new PositionDTO();
        dto.setId(position.getId());
        dto.setExchangeId(position.getExchangeId());
        dto.setInstrumentCode(position.getInstrumentCode());
        dto.setQuantity(position.getQuantity());
        dto.setAvailableQuantity(position.getAvailableQuantity());
        dto.setFrozenQuantity(position.getFrozenQuantity());
        dto.setCostPrice(position.getCostPrice());
        dto.setCostAmount(position.getCostAmount());
        dto.setLatestPrice(position.getLatestPrice());
        
        // 计算市值
        dto.setMarketValue(position.getQuantity().multiply(position.getLatestPrice()));
        dto.setProfitLoss(position.getProfitLoss());
        dto.setProfitLossRate(position.getProfitLossRate());
        
        // 获取品种名称
        Instrument instrument = instrumentRepository.findByInstrumentCode(position.getInstrumentCode()).orElse(null);
        if (instrument != null) {
            dto.setInstrumentName(instrument.getName());
            dto.setExchangeName(instrument.getExchangeId().toString());
        }
        
        return dto;
    }
    
    /**
     * 更新持仓（买入）
     */
    @Transactional
    public void increasePosition(Long userId, Long exchangeId, String instrumentCode, 
                                  BigDecimal quantity, BigDecimal price) {
        UserPosition position = userPositionRepository
            .findByUserIdAndExchangeIdAndInstrumentCode(userId, exchangeId, instrumentCode)
            .orElseGet(() -> {
                UserPosition newPos = new UserPosition();
                newPos.setUserId(userId);
                newPos.setExchangeId(exchangeId);
                newPos.setInstrumentCode(instrumentCode);
                newPos.setQuantity(BigDecimal.ZERO);
                newPos.setAvailableQuantity(BigDecimal.ZERO);
                newPos.setFrozenQuantity(BigDecimal.ZERO);
                newPos.setCostPrice(BigDecimal.ZERO);
                newPos.setCostAmount(BigDecimal.ZERO);
                newPos.setLatestPrice(price);
                return userPositionRepository.save(newPos);
            });
        
        // 计算新的持仓成本
        BigDecimal oldCostAmount = position.getCostAmount();
        BigDecimal newCostAmount = oldCostAmount.add(price.multiply(quantity));
        BigDecimal newQuantity = position.getQuantity().add(quantity);
        
        position.setQuantity(newQuantity);
        position.setAvailableQuantity(position.getAvailableQuantity().add(quantity));
        position.setCostAmount(newCostAmount);
        position.setCostPrice(newQuantity.compareTo(BigDecimal.ZERO) > 0 
            ? newCostAmount.divide(newQuantity, 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
        position.setLatestPrice(price);
        
        userPositionRepository.save(position);
    }
    
    /**
     * 更新持仓（卖出）
     */
    @Transactional
    public void decreasePosition(Long userId, Long exchangeId, String instrumentCode, 
                                  BigDecimal quantity, BigDecimal price) {
        UserPosition position = userPositionRepository
            .findByUserIdAndExchangeIdAndInstrumentCode(userId, exchangeId, instrumentCode)
            .orElse(null);
        
        if (position == null) return;
        
        // 减少持仓
        position.setQuantity(position.getQuantity().subtract(quantity));
        position.setAvailableQuantity(position.getAvailableQuantity().subtract(quantity));
        
        // 计算盈亏
        BigDecimal sellAmount = price.multiply(quantity);
        BigDecimal costRatio = position.getCostAmount().divide(
            position.getQuantity().add(quantity), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal sellCost = costRatio.multiply(quantity);
        BigDecimal profitLoss = sellAmount.subtract(sellCost);
        
        position.setProfitLoss(position.getProfitLoss().add(profitLoss));
        position.setCostAmount(position.getCostAmount().subtract(sellCost));
        position.setLatestPrice(price);
        
        // 计算盈亏比例
        if (position.getCostAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marketValue = position.getQuantity().multiply(position.getLatestPrice());
            position.setProfitLossRate(
                position.getProfitLoss().divide(position.getCostAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        }
        
        userPositionRepository.save(position);
    }
}
