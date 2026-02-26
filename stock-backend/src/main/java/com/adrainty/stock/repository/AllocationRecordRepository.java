package com.adrainty.stock.repository;

import com.adrainty.stock.entity.AllocationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分配记录数据访问接口
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Repository
public interface AllocationRecordRepository extends JpaRepository<AllocationRecord, Long> {
    
    /**
     * 根据用户 ID 查找分配记录
     * 
     * @param userId 用户 ID
     * @return 分配记录列表
     */
    List<AllocationRecord> findByUserIdOrderByOperateTimeDesc(Long userId);
    
    /**
     * 根据分配单号查找分配记录
     * 
     * @param allocationNo 分配单号
     * @return 分配记录对象
     */
    java.util.Optional<AllocationRecord> findByAllocationNo(String allocationNo);
}
