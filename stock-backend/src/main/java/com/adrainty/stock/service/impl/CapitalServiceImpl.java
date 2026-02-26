package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.CapitalAccountDTO;
import com.adrainty.stock.entity.CapitalFlow;
import com.adrainty.stock.entity.Exchange;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.entity.UserPosition;
import com.adrainty.stock.mapper.CapitalFlowMapper;
import com.adrainty.stock.mapper.ExchangeMapper;
import com.adrainty.stock.mapper.UserMapper;
import com.adrainty.stock.mapper.UserPositionMapper;
import com.adrainty.stock.service.CapitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 资金服务实现类
 * 使用数据库 User 表存储用户资金（available_capital、locked_capital）
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
    private final UserMapper userMapper;

    @Override
    public CapitalAccountDTO getAccount(Long userId, Long exchangeId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在：" + userId);
        }

        BigDecimal available = user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO;
        BigDecimal locked = user.getLockedCapital() != null ? user.getLockedCapital() : BigDecimal.ZERO;

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
        dto.setFrozen(locked);
        dto.setPositionValue(positionValue);
        dto.setTotalAsset(available.add(locked).add(positionValue)); // 总资金 = 可用 + 锁定 + 持仓市值
        dto.setTotalProfitLoss(totalProfitLoss);
        dto.setTodayProfitLoss(todayProfitLoss);
        dto.setPositionProfitLoss(positionProfitLoss);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在：userId={}", userId);
            return false;
        }

        BigDecimal available = user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO;
        if (available.compareTo(amount) < 0) {
            log.warn("可用资金不足：userId={}, available={}, need={}", userId, available, amount);
            return false;
        }

        user.setAvailableCapital(available.subtract(amount));
        BigDecimal locked = user.getLockedCapital() != null ? user.getLockedCapital() : BigDecimal.ZERO;
        user.setLockedCapital(locked.add(amount));

        userMapper.updateById(user);

        log.debug("冻结资金：userId={}, amount={}, refNo={}, available={}, locked={}",
            userId, amount, refNo, user.getAvailableCapital(), user.getLockedCapital());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在：userId={}", userId);
            return false;
        }

        BigDecimal locked = user.getLockedCapital() != null ? user.getLockedCapital() : BigDecimal.ZERO;
        if (locked.compareTo(amount) < 0) {
            log.warn("锁定资金不足：userId={}, locked={}, need={}", userId, locked, amount);
            return false;
        }

        user.setLockedCapital(locked.subtract(amount));
        BigDecimal available = user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO;
        user.setAvailableCapital(available.add(amount));

        userMapper.updateById(user);

        log.debug("解冻资金：userId={}, amount={}, refNo={}, available={}, locked={}",
            userId, amount, refNo, user.getAvailableCapital(), user.getLockedCapital());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo, String remark) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在：userId={}", userId);
            return false;
        }

        BigDecimal available = user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO;
        if (available.compareTo(amount) < 0) {
            log.warn("可用资金不足：userId={}, available={}, need={}", userId, available, amount);
            return false;
        }

        user.setAvailableCapital(available.subtract(amount));
        userMapper.updateById(user);

        // 记录资金流水
        recordCapitalFlow(userId, exchangeId, amount.negate(), user.getAvailableCapital(), refNo, remark);

        log.debug("扣除资金：userId={}, amount={}, remark={}, available={}", userId, amount, remark, user.getAvailableCapital());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCapital(Long userId, Long exchangeId, BigDecimal amount, String refNo, String remark) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在：userId={}", userId);
            return false;
        }

        BigDecimal available = user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO;
        user.setAvailableCapital(available.add(amount));
        userMapper.updateById(user);

        // 记录资金流水
        recordCapitalFlow(userId, exchangeId, amount, user.getAvailableCapital(), refNo, remark);

        log.debug("增加资金：userId={}, amount={}, remark={}, available={}", userId, amount, remark, user.getAvailableCapital());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initCapital(Long userId, Long exchangeId, BigDecimal initialAmount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在：userId={}", userId);
            return;
        }

        // 如果用户已有资金（不为 0），则不初始化
        if (user.getAvailableCapital() != null && user.getAvailableCapital().compareTo(BigDecimal.ZERO) > 0) {
            return;
        }

        user.setAvailableCapital(initialAmount);
        user.setLockedCapital(BigDecimal.ZERO);
        userMapper.updateById(user);

        // 记录资金流水
        recordCapitalFlow(userId, exchangeId, initialAmount, initialAmount,
            "INIT_" + UUID.randomUUID().toString().substring(0, 8), "初始资金");

        log.info("初始化资金：userId={}, exchangeId={}, amount={}, available={}",
            userId, exchangeId, initialAmount, user.getAvailableCapital());
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
     * 获取可用资金（内部使用）
     */
    public BigDecimal getAvailableCapital(Long userId, Long exchangeId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO;
    }
}
