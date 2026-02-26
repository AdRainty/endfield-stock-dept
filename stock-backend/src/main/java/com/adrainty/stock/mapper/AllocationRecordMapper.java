package com.adrainty.stock.mapper;

import com.adrainty.stock.entity.AllocationRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分配记录 Mapper 接口
 *
 * @author adrainty
 * @since 2026-02-26
 */
@Mapper
public interface AllocationRecordMapper extends BaseMapper<AllocationRecord> {

    /**
     * 根据用户 ID 查找分配记录
     *
     * @param userId 用户 ID
     * @return 分配记录列表
     */
    @Select("SELECT * FROM allocation_record WHERE user_id = #{userId} ORDER BY operate_time DESC")
    List<AllocationRecord> findByUserIdOrderByOperateTimeDesc(@Param("userId") Long userId);

    /**
     * 根据分配单号查找分配记录
     *
     * @param allocationNo 分配单号
     * @return 分配记录对象
     */
    @Select("SELECT * FROM allocation_record WHERE allocation_no = #{allocationNo}")
    AllocationRecord findByAllocationNo(@Param("allocationNo") String allocationNo);
}
