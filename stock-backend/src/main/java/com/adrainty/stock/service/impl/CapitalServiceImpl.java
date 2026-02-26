package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.CapitalAccountDTO;
import com.adrainty.stock.entity.CapitalFlow;
import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.entity.UserPosition;
import com.adrainty.stock.mapper.CapitalFlowMapper;
import com.adrainty.stock.mapper.ExchangeMapper;
import com.adrainty.stock.mapper.UserPositionMapper;
import com.adrainty.stock.service.CapitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资金服务实现类
 * 使用内存存储用户资金（生产环境应该使用数据库）
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CapitalServiceImpl implements CapitalService {

    private final CapitalFlowMapper capitalFlowMapper;
    private final UserPositionMapper userPositionMapper;
    private final ExchangeMapper exchangeMapper;

    // 内存存储用户资金：key = userId_exchangeId
    private static final Map<String, BigDecimal> AVAILABLE_CAPITAL = new ConcurrentHashMap<>();
    private static final Map<String, BigDecimal> FROZEN_CAPITAL = new ConcurrentHashMap<>();

    @Override
    public CapitalAccountDTO getAccount(Long userId, Long exchangeId) {
        String key = getKey(userId, exchangeId);

        BigDecimal available = AVAILABLE_CAPITAL.getOrDefault(key, BigDecimal.ZERO);
        BigDecimal frozen = FROZEN_CAPITAL.getOrDefault(key, BigDecimal.ZERO);

        // 计算持仓市值和盈亏
        List<UserPosition> positions = userPositionMapper.findByUserIdAndExchangeId(userId, exchangeId);
        BigDecimal positionValue = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        BigDecimal todayProfitLoss = BigDecimal.ZERO;

        for (UserPosition position : positions) {
            BigDecimal latestPrice = position.getLatestPrice() != null ? position.getLatestPrice() : BigDecimal.ZERO;
            BigDecimal costPrice = position.getCostPrice() != null ? position.getCostPrice() : BigDecimal.ZERO;
            BigDecimal quantity = position.getQuantity() != null ? position.getQuantity() : BigDecimal.ZERO;

            // 持仓市值
            positionValue = positionValue.add(quantity.multiply(latestPrice));

            // 持仓盈亏 = (最新价 - 成本价) * 数量
            BigDecimal positionPL = latestPrice.subtract(costPrice).multiply(quantity);
            totalProfitLoss = totalProfitLoss.add(positionPL);

            // 当日盈亏：这里简化为当前持仓盈亏（实际应该对比昨日收盘价）
            // 如果没有昨收数据，暂且用持仓盈亏作为当日盈亏
            todayProfitLoss = todayProfitLoss.add(positionPL);
        }

        // 持仓盈亏 = 总盈亏（已实现 + 未实现）
        BigDecimal positionProfitLoss = positions.stream()
            .map(UserPosition::getProfitLoss)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        CapitalAccountDTO dto = new CapitalAccountDTO();
        dto.setExchangeId(exchangeId);
        dto.setExchangeName(getExchangeName(exchangeId));
        dto.setAvailable(available);
        dto.setFrozen(frozen);
        dto.setPositionValue(positionValue);
        dto.setTotalAsset(available.add(positionValue));
        dto.setTotalProfitLoss(totalProfitLoss);
        dto.setTodayProfitLoss(todayProfitLoss);
        dto.setPositionProfitLoss(positionProfitLoss);

        return dto;
    }

    @Override
    @Transactional
    public boolean freezeCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo) {
        String key = getKey(userId, exchangeId);
        BigDecimal available = AVAILABLE_CAPITAL.getOrDefault(key, BigDecimal.ZERO);

        if (available.compareTo(amount) < 0) {
            log.warn("资金不足：userId={}, exchangeId={}, available={}, need={}", userId, exchangeId, available, amount);
            return false;
        }

        AVAILABLE_CAPITAL.put(key, available.subtract(amount));
        BigDecimal frozen = FROZEN_CAPITAL.getOrDefault(key, BigDecimal.ZERO);
        FROZEN_CAPITAL.put(key, frozen.add(amount));

        log.debug("冻结资金：userId={}, exchangeId={}, amount={}, refNo={}", userId, exchangeId, amount, refNo);
        return true;
    }

    @Override
    @Transactional
    public boolean unfreezeCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo) {
        String key = getKey(userId, exchangeId);
        BigDecimal frozen = FROZEN_CAPITAL.getOrDefault(key, BigDecimal.ZERO);

        if (frozen.compareTo(amount) < 0) {
            log.warn("冻结资金不足：userId={}, exchangeId={}, frozen={}, need={}", userId, exchangeId, frozen, amount);
            return false;
        }

        FROZEN_CAPITAL.put(key, frozen.subtract(amount));
        BigDecimal available = AVAILABLE_CAPITAL.getOrDefault(key, BigDecimal.ZERO);
        AVAILABLE_CAPITAL.put(key, available.add(amount));

        log.debug("解冻资金：userId={}, exchangeId={}, amount={}, refNo={}", userId, exchangeId, amount, refNo);
        return true;
    }

    @Override
    @Transactional
    public boolean deductCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo, String remark) {
        String key = getKey(userId, exchangeId);
        BigDecimal available = AVAILABLE_CAPITAL.getOrDefault(key, BigDecimal.ZERO);

        if (available.compareTo(amount) < 0) {
            log.warn("资金不足：userId={}, exchangeId={}, available={}, need={}", userId, exchangeId, available, amount);
            return false;
        }

        AVAILABLE_CAPITAL.put(key, available.subtract(amount));

        // 记录资金流水
        recordCapitalFlow(userId, exchangeId, amount.negate(), AVAILABLE_CAPITAL.get(key), refNo, remark);

        log.debug("扣除资金：userId={}, exchangeId={}, amount={}, remark={}", userId, exchangeId, amount, remark);
        return true;
    }

    @Override
    @Transactional
    public boolean addCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo, String remark) {
        String key = getKey(userId, exchangeId);
        BigDecimal available = AVAILABLE_CAPITAL.getOrDefault(key, BigDecimal.ZERO);
        AVAILABLE_CAPITAL.put(key, available.add(amount));

        // 记录资金流水
        recordCapitalFlow(userId, exchangeId, amount, AVAILABLE_CAPITAL.get(key), refNo, remark);

        log.debug("增加资金：userId={}, exchangeId={}, amount={}, remark={}", userId, exchangeId, amount, remark);
        return true;
    }

    @Override
    @Transactional
    public void initCapital(Long userId, Long exchangeId, BigDecimal initialAmount) {
        String key = getKey(userId, exchangeId);

        if (!AVAILABLE_CAPITAL.containsKey(key)) {
            AVAILABLE_CAPITAL.put(key, initialAmount);
            FROZEN_CAPITAL.put(key, BigDecimal.ZERO);

            // 记录资金流水
            recordCapitalFlow(userId, exchangeId, initialAmount, initialAmount,
                "INIT_" + UUID.randomUUID().toString().substring(0, 8), "初始资金");

            log.info("初始化资金：userId={}, exchangeId={}, amount={}", userId, exchangeId, initialAmount);
        }
    }

    /**
     * 记录资金流水
     */
    private void recordCapitalFlow(Long userId, Long exchangeId, BigDecimal amount,
                                   BigDecimal balanceAfter, String refNo, String remark) {
        CapitalFlow flow = new CapitalFlow();
        flow.setFlowNo("FLOW_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        flow.setUserId(userId);
        flow.setExchangeId(exchangeId);
        flow.setFlowType(amount.compareTo(BigDecimal.ZERO) > 0 ? "DEPOSIT" : "WITHDRAW");
        flow.setAmount(amount);
        flow.setBalanceAfter(balanceAfter);
        flow.setRefNo(refNo);
        flow.setRemark(remark);
        flow.setOperateTime(LocalDateTime.now());

        capitalFlowMapper.insert(flow);
    }

    private String getKey(Long userId, Long exchangeId) {
        return userId + "_" + exchangeId;
    }

    /**
     * 获取交易所名称
     */
    private String getExchangeName(Long exchangeId) {
        try {
            Exchange exchange = exchangeMapper.selectById(exchangeId);
            return exchange != null ? exchange.getName() : "交易所 " + exchangeId;
        } catch (Exception e) {
            log.warn("获取交易所名称失败：exchangeId={}", exchangeId, e);
            return "交易所 " + exchangeId;
        }
    }

    /**
     * 获取用户资金（内部使用）
     */
    public BigDecimal getAvailableCapital(Long userId, Long exchangeId) {
        return AVAILABLE_CAPITAL.getOrDefault(getKey(userId, exchangeId), BigDecimal.ZERO);
    }
}
