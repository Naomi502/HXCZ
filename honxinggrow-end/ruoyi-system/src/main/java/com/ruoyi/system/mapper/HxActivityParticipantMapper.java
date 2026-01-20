package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.HxActivityParticipant;

/**
 * 活动参与记录Mapper接口
 *
 * @author GPT
 */
public interface HxActivityParticipantMapper
{
    HxActivityParticipant selectById(Long participantId);

    List<HxActivityParticipant> selectByActivityId(Long activityId);

    HxActivityParticipant selectByActivityIdAndUserId(@Param("activityId") Long activityId,
            @Param("userId") Long userId);

    /**
     * 查询用户参加的活动及参与状态
     *
     * @param userId 用户ID
     * @return 活动概要信息
     */
    List<Map<String, Object>> selectUserActivityOverview(@Param("userId") Long userId);

    int insertHxActivityParticipant(HxActivityParticipant participant);

    int updateHxActivityParticipant(HxActivityParticipant participant);

    int deleteById(Long participantId);

    int deleteByIds(Long[] participantIds);

    int deleteByActivityId(@Param("activityId") Long activityId);

    /**
     * 按天统计用户参加活动数量（近一个月）
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    List<Map<String, Object>> selectDailyCountByUserAndDateRange(@Param("userId") Long userId,
                                                                  @Param("startDate") java.util.Date startDate,
                                                                  @Param("endDate") java.util.Date endDate);

    /**
     * 按小时统计用户参加活动数量（当天）
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 统计数据
     */
    List<Map<String, Object>> selectHourlyCountByUserAndDateRange(@Param("userId") Long userId,
                                                                    @Param("startDate") java.util.Date startDate,
                                                                    @Param("endDate") java.util.Date endDate);
    
    /**
     * 查询学生活动记录列表（分页）
     * @param userId 用户ID
     * @return 活动记录列表
     */
    List<Map<String, Object>> selectStudentActivityList(@Param("userId") Long userId);
}
