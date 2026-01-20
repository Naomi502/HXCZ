package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.HxPointLog;

/**
 * 积分流水Mapper接口
 *
 * @author GPT
 */
public interface HxPointLogMapper
{
    HxPointLog selectHxPointLogById(Long logId);

    List<HxPointLog> selectHxPointLogList(HxPointLog log);

    int insertHxPointLog(HxPointLog log);

    int deleteHxPointLogById(Long logId);

    int deleteHxPointLogByIds(Long[] logIds);

    HxPointLog selectByUserAndBiz(@Param("userId") Long userId, @Param("bizType") String bizType,
            @Param("bizId") Long bizId);

    /**
     * 按天统计用户获得积分（近一个月）
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    List<Map<String, Object>> selectDailyPointsByUserAndDateRange(@Param("userId") Long userId,
                                                                   @Param("startDate") java.util.Date startDate,
                                                                   @Param("endDate") java.util.Date endDate);

    /**
     * 按小时统计用户获得积分（当天）
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 统计数据
     */
    List<Map<String, Object>> selectHourlyPointsByUserAndDateRange(@Param("userId") Long userId,
                                                                     @Param("startDate") java.util.Date startDate,
                                                                     @Param("endDate") java.util.Date endDate);
}
