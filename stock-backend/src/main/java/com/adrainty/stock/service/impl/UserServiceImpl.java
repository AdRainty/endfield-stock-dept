package com.adrainty.stock.service.impl;

import com.adrainty.stock.dto.LeaderboardDTO;
import com.adrainty.stock.dto.LoginResponse;
import com.adrainty.stock.dto.UserDTO;
import com.adrainty.stock.entity.TradeRecord;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.entity.UserPosition;
import com.adrainty.stock.enums.UserRole;
import com.adrainty.stock.mapper.TradeRecordMapper;
import com.adrainty.stock.mapper.UserMapper;
import com.adrainty.stock.mapper.UserPositionMapper;
import com.adrainty.stock.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户服务实现类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserPositionMapper userPositionMapper;
    private final TradeRecordMapper tradeRecordMapper;

    @Override
    @Transactional
    public LoginResponse wxLogin(String openid) {
        boolean newUser = false;
        User user = userMapper.findByWechatOpenid(openid);

        return LoginResponse.builder()
                .userId(user.getId())
                .openid(user.getWechatOpenid())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole().getCode())
                .newUser(newUser)
                .build();
    }

    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User findByOpenid(String openid) {
        return userMapper.findByWechatOpenid(openid);
    }

    @Override
    @Transactional
    public User register(String openid) {
        log.info("注册用户：{}", openid);
        User user = new User();
        user.setWechatOpenid(openid);
        user.setNickname("用户" + openid.substring(0, 8));
        user.setAvatar("");
        user.setRole(UserRole.USER);
        user.setAvailableCapital(new BigDecimal("100000"));
        user.setLockedCapital(BigDecimal.ZERO);
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public void updateLoginInfo(User user, String loginIp) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        userMapper.updateById(user);
    }

    @Override
    public UserDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setWechatOpenid(user.getWechatOpenid());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole().getCode());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setAvailableCapital(user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO);
        dto.setLockedCapital(user.getLockedCapital() != null ? user.getLockedCapital() : BigDecimal.ZERO);
        dto.setTotalCapital(dto.getAvailableCapital().add(dto.getLockedCapital()));

        return dto;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String nickname, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        userMapper.updateById(user);
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取用户所有持仓
        List<UserPosition> positions = userPositionMapper.findByUserId(userId);

        // 计算总资产（可用资金 + 锁定资金 + 持仓市值）
        User user = userMapper.selectById(userId);
        BigDecimal availableCapital = user.getAvailableCapital() != null ? user.getAvailableCapital() : BigDecimal.ZERO;
        BigDecimal lockedCapital = user.getLockedCapital() != null ? user.getLockedCapital() : BigDecimal.ZERO;

        BigDecimal positionValue = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;

        for (UserPosition position : positions) {
            BigDecimal latestPrice = position.getLatestPrice() != null ? position.getLatestPrice() : BigDecimal.ZERO;
            BigDecimal costPrice = position.getCostPrice() != null ? position.getCostPrice() : BigDecimal.ZERO;
            BigDecimal quantity = position.getQuantity() != null ? position.getQuantity() : BigDecimal.ZERO;

            // 持仓市值
            positionValue = positionValue.add(quantity.multiply(latestPrice));

            // 持仓盈亏
            BigDecimal positionPL = latestPrice.subtract(costPrice).multiply(quantity);
            totalProfitLoss = totalProfitLoss.add(positionPL);
        }

        BigDecimal totalAsset = availableCapital.add(lockedCapital).add(positionValue);

        // 计算初始资金（从资金流水中获取）
        BigDecimal initialCapital = getInitialCapital();
        BigDecimal totalReturnRate = initialCapital.compareTo(BigDecimal.ZERO) > 0
            ? totalProfitLoss.divide(initialCapital, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;

        // 计算今日盈亏（今日成交记录的盈亏总和）
        BigDecimal todayProfitLoss = getTodayProfitLoss(userId);

        stats.put("availableCapital", availableCapital);
        stats.put("lockedCapital", lockedCapital);
        stats.put("positionValue", positionValue);
        stats.put("totalAsset", totalAsset);
        stats.put("totalProfitLoss", totalProfitLoss);
        stats.put("todayProfitLoss", todayProfitLoss);
        stats.put("initialCapital", initialCapital);
        stats.put("totalReturnRate", totalReturnRate);

        return stats;
    }

    @Override
    public List<LeaderboardDTO> getLeaderboard(String type) {
        // 获取所有用户
        List<User> allUsers = userMapper.selectList(null);

        // 过滤掉管理员
        return allUsers.stream()
            .filter(u -> !"ADMIN".equals(u.getRole().getCode()))
            .map(user -> {
                LeaderboardDTO dto = new LeaderboardDTO();
                dto.setUserId(user.getId());
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
                dto.setCreatedAt(user.getCreatedAt());

                // 计算收益
                if ("daily".equals(type)) {
                    // 日收益
                    BigDecimal todayPL = getTodayProfitLoss(user.getId());
                    dto.setProfitLoss(todayPL);
                } else {
                    // 总收益
                    BigDecimal totalPL = getTotalProfitLoss(user.getId());
                    dto.setProfitLoss(totalPL);
                }

                // 计算收益率
                BigDecimal initialCapital = getInitialCapital();
                dto.setReturnRate(initialCapital.compareTo(BigDecimal.ZERO) > 0
                    ? dto.getProfitLoss().divide(initialCapital, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);

                return dto;
            })
            .sorted((a, b) ->
                // 按收益率降序排序
                b.getReturnRate().compareTo(a.getReturnRate())
            )
            .toList();
    }

    /**
     * 获取用户初始资金（从资金流水中获取初始化金额）
     */
    private BigDecimal getInitialCapital() {
        // 从资金流水中获取初始资金（DEPOSIT 类型的总和）
        return BigDecimal.valueOf(100000); // 默认初始资金 10 万
    }

    /**
     * 获取今日盈亏
     */
    private BigDecimal getTodayProfitLoss(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();

        // 查询今日所有成交记录
        List<TradeRecord> todayTrades = tradeRecordMapper.findByUserIdAndTradeTimeBetween(userId, startOfDay, LocalDateTime.now());

        if (todayTrades == null || todayTrades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算今日盈亏：卖出成交的盈亏总和
        BigDecimal todayPL = BigDecimal.ZERO;
        for (TradeRecord ignored : todayTrades) {
            // 这里简化计算，实际应该根据买卖方向和成本价计算
            // 暂时返回 0，因为需要更复杂的逻辑来计算实际盈亏
            todayPL = todayPL.add(BigDecimal.ZERO);
        }

        return todayPL;
    }

    /**
     * 获取总盈亏
     */
    private BigDecimal getTotalProfitLoss(Long userId) {
        // 从持仓中计算总盈亏
        List<UserPosition> positions = userPositionMapper.findByUserId(userId);
        if (positions == null || positions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return positions.stream()
            .map(UserPosition::getProfitLoss)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
