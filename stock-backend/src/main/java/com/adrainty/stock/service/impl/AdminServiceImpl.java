package com.adrainty.stock.service.impl;

import com.adrainty.stock.entity.AllocationRecord;
import com.adrainty.stock.entity.User;
import com.adrainty.stock.enums.UserRole;
import com.adrainty.stock.exception.BusinessException;
import com.adrainty.stock.mapper.AllocationRecordMapper;
import com.adrainty.stock.mapper.UserMapper;
import com.adrainty.stock.service.AdminService;
import com.adrainty.stock.service.CapitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final AllocationRecordMapper allocationRecordMapper;
    private final CapitalService capitalService;

    @Override
    @Transactional
    public void allocateCapital(Long adminUserId, Long targetUserId, Long exchangeId,
                                 BigDecimal amount, String reason) {
        // 验证管理员权限
        User admin = userMapper.selectById(adminUserId);
        if (admin == null) {
            throw BusinessException.of("管理员不存在");
        }

        if (admin.getRole() != UserRole.ADMIN) {
            throw BusinessException.of("无权限执行分配操作");
        }

        // 验证目标用户
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw BusinessException.of("目标用户不存在");
        }

        // 执行分配
        capitalService.addCapital(targetUserId, exchangeId, amount,
            "ALLOC_" + UUID.randomUUID().toString().substring(0, 8),
            "管理员分配：" + reason);

        // 记录分配记录
        AllocationRecord record = new AllocationRecord();
        record.setAllocationNo("ALLOC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        record.setUserId(targetUserId);
        record.setExchangeId(exchangeId);
        record.setAmount(amount);
        record.setBalanceAfter(capitalService.getAvailableCapital(targetUserId, exchangeId));
        record.setReason(reason);
        record.setAdminUserId(adminUserId);
        record.setOperateTime(LocalDateTime.now());

        allocationRecordMapper.insert(record);

        log.info("管理员分配原能：adminUserId={}, targetUserId={}, exchangeId={}, amount={}, reason={}",
            adminUserId, targetUserId, exchangeId, amount, reason);
    }

    @Override
    public List<Map<String, Object>> getUserList() {
        List<User> users = userMapper.selectList(null);
        return users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("wechatOpenid", u.getWechatOpenid());
            map.put("nickname", u.getNickname());
            map.put("avatar", u.getAvatar());
            map.put("role", u.getRole().getCode());
            map.put("status", u.getStatus());
            map.put("createdAt", u.getCreatedAt());
            map.put("lastLoginAt", u.getLastLoginAt());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("wechatOpenid", user.getWechatOpenid());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole().getCode());
        result.put("status", user.getStatus());
        result.put("createdAt", user.getCreatedAt());
        result.put("lastLoginAt", user.getLastLoginAt());
        result.put("lastLoginIp", user.getLastLoginIp());

        return result;
    }

    @Override
    public List<Map<String, Object>> getAllocationRecords(Long userId) {
        List<AllocationRecord> records;

        if (userId != null) {
            records = allocationRecordMapper.findByUserIdOrderByOperateTimeDesc(userId);
        } else {
            records = allocationRecordMapper.selectList(null);
        }

        return records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("allocationNo", r.getAllocationNo());
            map.put("userId", r.getUserId());
            map.put("exchangeId", r.getExchangeId());
            map.put("amount", r.getAmount());
            map.put("balanceAfter", r.getBalanceAfter());
            map.put("reason", r.getReason());
            map.put("adminUserId", r.getAdminUserId());
            map.put("operateTime", r.getOperateTime());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        List<User> allUsers = userMapper.selectList(null);
        stats.put("totalUsers", allUsers.size());
        stats.put("normalUsers", allUsers.stream().filter(u -> u.getStatus() == 1).count());
        stats.put("adminUsers", allUsers.stream().filter(u -> u.getRole() == UserRole.ADMIN).count());

        // 分配记录统计
        List<AllocationRecord> allRecords = allocationRecordMapper.selectList(null);
        BigDecimal totalAllocated = allRecords.stream()
            .map(AllocationRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalAllocated", totalAllocated);
        stats.put("allocationCount", allRecords.size());

        return stats;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        log.info("更新用户状态：userId={}, status={}", userId, status);
    }
}
