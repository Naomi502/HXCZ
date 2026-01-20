package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.HxActivity;

/**
 * 活动管理Mapper接口
 *
 * @author GPT
 */
public interface HxActivityMapper
{
    HxActivity selectHxActivityById(Long activityId);

    List<HxActivity> selectHxActivityList(HxActivity activity);

    List<HxActivity> selectPublishedActivities();

    /**
     * 查询小程序活动列表（支持状态筛选和岗位过滤）
     *
     * @param statusList 状态集合
     * @param userPostIds 用户岗位ID集合，用于过滤可见活动
     * @return 活动集合
     */
    List<HxActivity> selectActivitiesForApp(@Param("statusList") List<String> statusList, 
                                            @Param("userPostIds") List<Long> userPostIds);

    int insertHxActivity(HxActivity activity);

    int updateHxActivity(HxActivity activity);

    int deleteHxActivityById(Long activityId);

    int deleteHxActivityByIds(Long[] activityIds);

    int increaseParticipantCount(@Param("activityId") Long activityId);

    int decreaseParticipantCount(@Param("activityId") Long activityId);

    /**
     * 更新活动状态
     * 
     * @param activityId 活动ID
     * @param status 活动状态
     * @return 更新的记录数
     */
    int updateActivityStatus(@Param("activityId") Long activityId, @Param("status") String status);
}