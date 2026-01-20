package com.ruoyi.system.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SnowflakeIdWorker;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.HxActivity;
import com.ruoyi.system.domain.HxActivityParticipant;
import com.ruoyi.system.domain.HxPointLog;
import com.ruoyi.system.mapper.HxActivityMapper;
import com.ruoyi.system.mapper.HxActivityParticipantMapper;
import com.ruoyi.system.mapper.HxPointLogMapper;
import com.ruoyi.system.mapper.HxStudentProfileMapper;
import com.ruoyi.system.service.IHxActivityService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.utils.ActivityStatusUtils;

/**
 * 活动管理Service业务层处理
 *
 * @author GPT
 */
@Service
public class HxActivityServiceImpl implements IHxActivityService
{
    private static final String ACTIVITY_STATUS_PUBLISHED = "1";
    private static final String PARTICIPANT_STATUS_PENDING = "0";
    private static final String PARTICIPANT_STATUS_JOINED = "1";
    private static final String PARTICIPANT_STATUS_REJECTED = "2";
    private static final String DEL_FLAG_NORMAL = "0";
    private static final String BIZ_TYPE_ACTIVITY_JOIN = "ACTIVITY_JOIN";
    private static final String CONFIG_KEY_ACTIVITY_REWARD_POINT = "hx.activity.reward.points";
    private static final int DEFAULT_ACTIVITY_REWARD_POINT = 5;
    private static final String PROOF_STATUS_NOT_SUBMITTED = "0";
    private static final String PROOF_STATUS_PENDING = "1";
    private static final String PROOF_STATUS_APPROVED = "2";
    private static final String PROOF_STATUS_REJECTED = "3";
    private static final String REWARD_GRANTED_NO = "0";
    private static final String REWARD_GRANTED_YES = "1";

    @Autowired
    private HxActivityMapper hxActivityMapper;

    @Autowired
    private HxActivityParticipantMapper hxActivityParticipantMapper;

    @Autowired
    private HxPointLogMapper hxPointLogMapper;

    @Autowired
    private HxStudentProfileMapper hxStudentProfileMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private com.ruoyi.system.service.IHxUserMessageService hxUserMessageService;

    @Override
    public HxActivity selectHxActivityById(Long activityId)
    {
        return hxActivityMapper.selectHxActivityById(activityId);
    }

    @Override
    public List<HxActivity> selectHxActivityList(HxActivity activity)
    {
        List<HxActivity> activities = hxActivityMapper.selectHxActivityList(activity);
        return updateActivityStatusIfNeeded(activities);
    }

    @Override
    public int insertHxActivity(HxActivity activity)
    {
        Date now = DateUtils.getNowDate();
        // 使用雪花算法生成活动ID
        if (activity.getActivityId() == null) {
            activity.setActivityId(SnowflakeIdWorker.getId());
        }
        activity.setCreateTime(now);
        if (activity.getDelFlag() == null)
        {
            activity.setDelFlag(DEL_FLAG_NORMAL);
        }
        if (activity.getParticipantCount() == null)
        {
            activity.setParticipantCount(0);
        }
        if ((StringUtils.equals(ACTIVITY_STATUS_PUBLISHED, activity.getStatus()) || "0".equals(activity.getStatus())) 
                && activity.getPublishTime() == null)
        {
            activity.setPublishTime(now);
        }
        return hxActivityMapper.insertHxActivity(activity);
    }

    @Override
    public int updateHxActivity(HxActivity activity)
    {
        Date now = DateUtils.getNowDate();
        activity.setUpdateTime(now);
        if (activity.getDelFlag() == null)
        {
            activity.setDelFlag(DEL_FLAG_NORMAL);
        }
        if (activity.getParticipantCount() == null)
        {
            activity.setParticipantCount(0);
        }
        if ((StringUtils.equals(ACTIVITY_STATUS_PUBLISHED, activity.getStatus()) || "0".equals(activity.getStatus())) 
                && activity.getPublishTime() == null)
        {
            activity.setPublishTime(now);
        }
        return hxActivityMapper.updateHxActivity(activity);
    }

    @Override
    public int deleteHxActivityByIds(Long[] activityIds)
    {
        return hxActivityMapper.deleteHxActivityByIds(activityIds);
    }

    @Override
    public int deleteHxActivityById(Long activityId)
    {
        return hxActivityMapper.deleteHxActivityById(activityId);
    }

    @Override
    public List<HxActivity> selectPublishedActivities()
    {
        return hxActivityMapper.selectPublishedActivities();
    }

    @Override
    public List<HxActivity> selectAppActivities(List<String> statuses, List<Long> userPostIds)
    {
        if (statuses == null || statuses.isEmpty())
        {
            // 如果没有指定状态，查询已发布的活动，但需要根据岗位过滤
            // 注意：selectPublishedActivities不支持岗位过滤，所以需要先查询再过滤
            List<HxActivity> allActivities = hxActivityMapper.selectPublishedActivities();
            return filterByPostIds(allActivities, userPostIds);
        }
        return hxActivityMapper.selectActivitiesForApp(statuses, userPostIds);
    }

    /**
     * 根据岗位ID过滤活动
     * @param activities 活动列表
     * @param userPostIds 用户岗位ID列表
     * @return 过滤后的活动列表
     */
    private List<HxActivity> filterByPostIds(List<HxActivity> activities, List<Long> userPostIds)
    {
        if (activities == null || activities.isEmpty())
        {
            return activities;
        }
        if (userPostIds == null || userPostIds.isEmpty())
        {
            // 如果用户没有岗位，只返回没有限制岗位的活动
            return activities.stream()
                    .filter(activity -> activity.getVisiblePostIds() == null 
                            || activity.getVisiblePostIds().trim().isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
        return activities.stream()
                .filter(activity -> {
                    String visiblePostIds = activity.getVisiblePostIds();
                    // 如果没有设置可见岗位，则所有岗位可见
                    if (visiblePostIds == null || visiblePostIds.trim().isEmpty())
                    {
                        return true;
                    }
                    // 检查用户岗位是否在可见岗位列表中
                    String[] postIdArray = visiblePostIds.split(",");
                    for (String postIdStr : postIdArray)
                    {
                        try
                        {
                            Long postId = Long.parseLong(postIdStr.trim());
                            if (userPostIds.contains(postId))
                            {
                                return true;
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            // 忽略无效的岗位ID
                        }
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<HxActivityParticipant> selectParticipants(Long activityId)
    {
        List<HxActivityParticipant> list = hxActivityParticipantMapper.selectByActivityId(activityId);
        if (list == null || list.isEmpty())
        {
            return list;
        }

        for (HxActivityParticipant participant : list)
        {
            Long userId = participant.getUserId();
            if (userId == null)
            {
                continue;
            }
            SysUser user = sysUserService.selectUserById(userId);
            if (user == null)
            {
                continue;
            }
            // 学生部门
            if (user.getDept() != null)
            {
                participant.setDeptName(user.getDept().getDeptName());
            }
            // 学生岗位（使用系统已有的岗位组查询，多个岗位之间用顿号）
            String postGroup = sysUserService.selectUserPostGroup(user.getUserName());
            if (StringUtils.isNotEmpty(postGroup))
            {
                // 默认返回逗号分隔，这里替换为顿号，展示更友好
                participant.setPostNames(postGroup.replace(",", "、"));
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> selectUserActivityOverview(Long userId)
    {
        if (userId == null)
        {
            return Collections.emptyList();
        }
        return hxActivityParticipantMapper.selectUserActivityOverview(userId);
    }

    @Override
    public List<Map<String, Object>> selectStudentActivityList(Long userId)
    {
        if (userId == null)
        {
            return Collections.emptyList();
        }
        return hxActivityParticipantMapper.selectStudentActivityList(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HxActivityParticipant joinActivity(Long activityId, Long userId)
    {
        HxActivity activity = hxActivityMapper.selectHxActivityById(activityId);
        if (activity == null || "2".equals(activity.getDelFlag()))
        {
            throw new ServiceException("活动不存在或已删除");
        }
        
        // 允许未开始(0)的状态报名，进行中(1)的状态停止报名
        if ("1".equals(activity.getStatus()))
        {
            throw new ServiceException("活动已开始，停止报名");
        }
        if (!"0".equals(activity.getStatus()))
        {
            throw new ServiceException("活动未开放报名");
        }
        
        Integer currentCount = activity.getParticipantCount() == null ? 0 : activity.getParticipantCount();
        if (activity.getMaxParticipants() != null && currentCount >= activity.getMaxParticipants())
        {
            throw new ServiceException("活动名额已满");
        }

        HxActivityParticipant existing = hxActivityParticipantMapper
                .selectByActivityIdAndUserId(activityId, userId);
        Date now = DateUtils.getNowDate();
        SysUser user = sysUserService.selectUserById(userId);
        String userName = user != null ? user.getNickName() : null;

        HxActivityParticipant participant;
        if (existing == null)
        {
            participant = new HxActivityParticipant();
            participant.setActivityId(activityId);
            participant.setUserId(userId);
            participant.setUserName(userName);
            participant.setJoinTime(now);
            participant.setStatus(PARTICIPANT_STATUS_PENDING);
            participant.setProofStatus(PROOF_STATUS_NOT_SUBMITTED);
            participant.setRewardGranted(REWARD_GRANTED_NO);
            participant.setRewardPoints(0);
            participant.setCreateBy(userName);
            participant.setCreateTime(now);
            hxActivityParticipantMapper.insertHxActivityParticipant(participant);
            hxActivityMapper.increaseParticipantCount(activityId);
        }
        else
        {
            participant = existing;
            // 如果是被拒绝或取消的状态，重新申请时重置为待审核
            if (PARTICIPANT_STATUS_REJECTED.equals(participant.getStatus())) {
                participant.setStatus(PARTICIPANT_STATUS_PENDING);
                hxActivityMapper.increaseParticipantCount(activityId);
            }
            // 如果已经是报名或待审核状态，保持原状态或更新时间？
            // 这里假设重新点击报名不改变已有的有效状态，只更新时间
            participant.setJoinTime(now);
            participant.setUpdateBy(userName);
            participant.setUpdateTime(now);
            hxActivityParticipantMapper.updateHxActivityParticipant(participant);
        }
        return participant;
    }

    @Override
    public HxActivityParticipant getParticipant(Long activityId, Long userId)
    {
        if (activityId == null || userId == null)
        {
            return null;
        }
        return hxActivityParticipantMapper.selectByActivityIdAndUserId(activityId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HxActivityParticipant submitParticipationProof(Long activityId, Long userId, String proofImageUrl)
    {
        if (StringUtils.isBlank(proofImageUrl))
        {
            throw new ServiceException("请上传活动参加证明图片");
        }
        HxActivityParticipant participant = hxActivityParticipantMapper.selectByActivityIdAndUserId(activityId, userId);
        if (participant == null || !PARTICIPANT_STATUS_JOINED.equals(participant.getStatus()))
        {
            throw new ServiceException("请先报名该活动");
        }
        Date now = DateUtils.getNowDate();
        SysUser user = sysUserService.selectUserById(userId);
        String userName = user != null ? user.getNickName() : participant.getUserName();
        participant.setProofImageUrl(proofImageUrl);
        participant.setProofStatus(PROOF_STATUS_PENDING);
        participant.setProofSubmitTime(now);
        participant.setProofAuditBy(null);
        participant.setProofAuditRemark(null);
        participant.setProofAuditTime(null);
        participant.setRewardGranted(REWARD_GRANTED_NO);
        participant.setRewardTime(null);
        participant.setRewardPoints(0);
        participant.setUpdateBy(userName);
        participant.setUpdateTime(now);
        hxActivityParticipantMapper.updateHxActivityParticipant(participant);
        return participant;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditParticipantProof(Long participantId, boolean approved, String remark, Long auditorId,
            String auditorName)
    {
        HxActivityParticipant participant = hxActivityParticipantMapper.selectById(participantId);
        if (participant == null)
        {
            throw new ServiceException("报名记录不存在");
        }
        if (!StringUtils.equals(PROOF_STATUS_PENDING, participant.getProofStatus()))
        {
            throw new ServiceException("当前状态不可审核");
        }
        Date now = DateUtils.getNowDate();
        participant.setProofAuditBy(auditorName);
        participant.setProofAuditTime(now);
        participant.setProofAuditRemark(remark);
        participant.setUpdateBy(auditorName);
        participant.setUpdateTime(now);
        if (approved)
        {
            participant.setProofStatus(PROOF_STATUS_APPROVED);
            grantRewardIfNeeded(participant, now);
            
            // 发送消息
            hxUserMessageService.sendMessage(participant.getUserId(), 
                "活动证明审核通过", 
                "您参与的活动【" + hxActivityMapper.selectHxActivityById(participant.getActivityId()).getTitle() + "】证明已审核通过，积分已发放。", 
                "2");
        }
        else
        {
            participant.setProofStatus(PROOF_STATUS_REJECTED);
            participant.setRewardGranted(REWARD_GRANTED_NO);
            participant.setRewardTime(null);
            participant.setRewardPoints(0);
            
            // 发送消息
            String content = "您参与的活动【" + hxActivityMapper.selectHxActivityById(participant.getActivityId()).getTitle() + "】证明审核未通过";
            if (StringUtils.isNotEmpty(remark)) {
                content += "，原因：" + remark;
            }
            hxUserMessageService.sendMessage(participant.getUserId(), "活动证明审核未通过", content, "2");
        }
        hxActivityParticipantMapper.updateHxActivityParticipant(participant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditParticipation(Long participantId, boolean approved, String remark, Long auditorId, String auditorName)
    {
        HxActivityParticipant participant = hxActivityParticipantMapper.selectById(participantId);
        if (participant == null)
        {
            throw new ServiceException("报名记录不存在");
        }
        if (!PARTICIPANT_STATUS_PENDING.equals(participant.getStatus()))
        {
            throw new ServiceException("当前状态不可审核");
        }
        
        Date now = DateUtils.getNowDate();
        participant.setUpdateBy(auditorName);
        participant.setUpdateTime(now);
        
        if (approved)
        {
            participant.setStatus(PARTICIPANT_STATUS_JOINED);
            // 发送消息
            hxUserMessageService.sendMessage(participant.getUserId(), 
                "活动报名审核通过", 
                "恭喜！您申请报名的活动【" + hxActivityMapper.selectHxActivityById(participant.getActivityId()).getTitle() + "】已通过审核，请准时参加。", 
                "1");
        }
        else
        {
            participant.setStatus(PARTICIPANT_STATUS_REJECTED);
            // 拒绝报名，释放名额
            hxActivityMapper.decreaseParticipantCount(participant.getActivityId());
            
            // 发送消息
            String content = "很遗憾，您申请报名的活动【" + hxActivityMapper.selectHxActivityById(participant.getActivityId()).getTitle() + "】未通过审核";
            if (StringUtils.isNotEmpty(remark)) {
                content += "，原因：" + remark;
            }
            hxUserMessageService.sendMessage(participant.getUserId(), "活动报名审核未通过", content, "1");
        }
        hxActivityParticipantMapper.updateHxActivityParticipant(participant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelParticipation(Long participantId, String operatorName)
    {
        HxActivityParticipant participant = hxActivityParticipantMapper.selectById(participantId);
        if (participant == null)
        {
            throw new ServiceException("报名记录不存在");
        }
        
        // 只有已报名或待审核的可以取消
        if (!PARTICIPANT_STATUS_JOINED.equals(participant.getStatus()) && !PARTICIPANT_STATUS_PENDING.equals(participant.getStatus()))
        {
             throw new ServiceException("当前状态不可取消");
        }
        
        Date now = DateUtils.getNowDate();
        participant.setStatus(PARTICIPANT_STATUS_REJECTED);
        participant.setUpdateBy(operatorName);
        participant.setUpdateTime(now);
        
        hxActivityParticipantMapper.updateHxActivityParticipant(participant);
        // 取消报名，释放名额
        hxActivityMapper.decreaseParticipantCount(participant.getActivityId());
        
        // 发送消息
        hxUserMessageService.sendMessage(participant.getUserId(), 
            "活动报名已取消", 
            "您报名的活动【" + hxActivityMapper.selectHxActivityById(participant.getActivityId()).getTitle() + "】已被管理员取消。", 
            "1");
    }

    private void grantRewardIfNeeded(HxActivityParticipant participant, Date now)
    {
        if (participant.getUserId() == null)
        {
            return;
        }
        if (StringUtils.equals(REWARD_GRANTED_YES, participant.getRewardGranted()))
        {
            return;
        }
        HxPointLog existLog = hxPointLogMapper.selectByUserAndBiz(participant.getUserId(), BIZ_TYPE_ACTIVITY_JOIN,
                participant.getActivityId());
        
        // 检查是否存在有效的积分记录（积分大于0）
        if (existLog != null && existLog.getPoints() != null && existLog.getPoints() > 0)
        {
            participant.setRewardGranted(REWARD_GRANTED_YES);
            participant.setRewardTime(existLog.getEventTime());
            participant.setRewardPoints(existLog.getPoints());
            return;
        }
        
        int rewardPoint = getActivityRewardPoint();
        // 确保积分至少为1
        if (rewardPoint <= 0) {
            rewardPoint = DEFAULT_ACTIVITY_REWARD_POINT;
        }
        
        SysUser user = sysUserService.selectUserById(participant.getUserId());
        String userName = user != null ? user.getNickName() : participant.getUserName();
        
        // 如果存在无效记录（积分<=0），我们插入一条新的有效记录。
         // selectByUserAndBiz 会按 log_id 倒序取最新一条，所以新记录会覆盖旧记录的效果。
         
         HxPointLog log = new HxPointLog();
        log.setUserId(participant.getUserId());
        log.setUserName(userName);
        log.setBizType(BIZ_TYPE_ACTIVITY_JOIN);
        log.setBizId(participant.getActivityId());
        log.setPoints(rewardPoint);
        log.setRemark("活动证明审核通过获得积分");
        log.setEventTime(now);
        log.setCreateBy(userName);
        log.setCreateTime(now);
        hxPointLogMapper.insertHxPointLog(log);
        
        hxStudentProfileMapper.addPoints(participant.getUserId(), userName, rewardPoint, null, now);
        
        participant.setRewardGranted(REWARD_GRANTED_YES);
        participant.setRewardTime(now);
        participant.setRewardPoints(rewardPoint);
    }

    private int getActivityRewardPoint()
    {
        String value = configService.selectConfigByKey(CONFIG_KEY_ACTIVITY_REWARD_POINT);
        if (StringUtils.isNumeric(value))
        {
            try
            {
                int parsed = Integer.parseInt(value);
                return parsed > 0 ? parsed : DEFAULT_ACTIVITY_REWARD_POINT;
            }
            catch (NumberFormatException ex)
            {
                return DEFAULT_ACTIVITY_REWARD_POINT;
            }
        }
        return DEFAULT_ACTIVITY_REWARD_POINT;
    }

    /**
     * 检查并更新活动状态（如果需要）
     * 
     * @param activities 活动列表
     * @return 处理后的活动列表
     */
    private List<HxActivity> updateActivityStatusIfNeeded(List<HxActivity> activities)
    {
        if (activities == null || activities.isEmpty())
        {
            return activities;
        }
        
        Date now = DateUtils.getNowDate();
        boolean hasStatusChange = false;
        
        for (HxActivity activity : activities)
        {
            String actualStatus = ActivityStatusUtils.calculateStatus(activity.getStartTime(), activity.getEndTime(), now);
            if (!actualStatus.equals(activity.getStatus()))
            {
                activity.setStatus(actualStatus);
                hasStatusChange = true;
            }
        }
        
        // 如果有状态变化，批量更新到数据库
        if (hasStatusChange)
        {
            for (HxActivity activity : activities)
            {
                // 只更新状态字段
                hxActivityMapper.updateActivityStatus(activity.getActivityId(), activity.getStatus());
            }
        }
        
        return activities;
    }
}