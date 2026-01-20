package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.HxActivity;
import com.ruoyi.system.domain.HxActivityParticipant;

/**
 * 活动管理Service接口
 *
 * @author GPT
 */
public interface IHxActivityService
{
    HxActivity selectHxActivityById(Long activityId);

    List<HxActivity> selectHxActivityList(HxActivity activity);

    int insertHxActivity(HxActivity activity);

    int updateHxActivity(HxActivity activity);

    int deleteHxActivityByIds(Long[] activityIds);

    int deleteHxActivityById(Long activityId);

    List<HxActivity> selectPublishedActivities();

    /**
     * 查询小程序需要展示的活动
     *
     * @param statuses 需要筛选的状态列表，传空则默认只返回进行中的活动
     * @return 活动集合
     */
    List<HxActivity> selectAppActivities(List<String> statuses, List<Long> userPostIds);

    List<HxActivityParticipant> selectParticipants(Long activityId);

    /**
     * 查询用户已参与的活动概要
     *
     * @param userId 用户ID
     * @return 活动与参与信息列表
     */
    List<Map<String, Object>> selectUserActivityOverview(Long userId);

    /**
     * 小程序报名活动
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param gainPoints 是否需要积分奖励
     * @return 参与记录
     */
    HxActivityParticipant joinActivity(Long activityId, Long userId);

    HxActivityParticipant getParticipant(Long activityId, Long userId);

    HxActivityParticipant submitParticipationProof(Long activityId, Long userId, String proofImageUrl);

    /**
     * 审核活动证明
     * 
     * @param participantId 报名ID
     * @param approved 是否通过
     * @param remark 审核备注
     * @param auditorId 审核人ID
     * @param auditorName 审核人姓名
     */
    public void auditParticipantProof(Long participantId, boolean approved, String remark, Long auditorId, String auditorName);

    /**
     * 审核活动报名
     * 
     * @param participantId 报名ID
     * @param approved 是否通过
     * @param remark 审核备注
     * @param auditorId 审核人ID
     * @param auditorName 审核人姓名
     */
    public void auditParticipation(Long participantId, boolean approved, String remark, Long auditorId, String auditorName);

    /**
     * 取消活动报名
     * 
     * @param participantId 报名ID
     * @param operatorName 操作人姓名
     */
    public void cancelParticipation(Long participantId, String operatorName);

    /**
     * 查询学生活动记录列表
     * @param userId 用户ID
     * @return 活动记录列表
     */
    List<Map<String, Object>> selectStudentActivityList(Long userId);

    
}
