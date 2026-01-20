package com.ruoyi.web.controller.app;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.system.domain.HxActivity;
import com.ruoyi.system.domain.HxActivityParticipant;
import com.ruoyi.system.service.IHxActivityService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.web.controller.app.request.ActivityProofSubmitRequest;

/**
 * 小程序活动接口
 *
 * @author GPT
 */
@RestController
@RequestMapping("/app/activity")
public class AppActivityController extends BaseController
{
    @Autowired
    private IHxActivityService hxActivityService;

    @Autowired
    private ISysPostService postService;
    
    @Autowired
    private ServerConfig serverConfig;

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "status", required = false) String status)
    {
        // 获取当前用户的岗位ID列表
        List<Long> userPostIds = postService.selectPostListByUserId(getUserId());
        List<HxActivity> activities = hxActivityService.selectAppActivities(resolveStatuses(status), userPostIds);
        Date now = DateUtils.getNowDate();
        List<Map<String, Object>> result = activities.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("activityId", item.getActivityId());
            map.put("title", item.getTitle());
            map.put("summary", item.getSummary());
            map.put("coverUrl", buildFullUrl(item.getCoverUrl()));
            map.put("startTime", item.getStartTime());
            map.put("endTime", item.getEndTime());
            map.put("location", item.getLocation());
            map.put("maxParticipants", item.getMaxParticipants());
            map.put("participantCount", item.getParticipantCount());
            map.put("publishTime", item.getPublishTime());
            // 根据时间计算活动状态
            Map<String, String> statusInfo = calculateActivityStatus(item.getStartTime(), item.getEndTime(), now);
            map.put("status", statusInfo.get("status"));
            map.put("statusType", statusInfo.get("statusType"));
            return map;
        }).collect(Collectors.toList());
        return success(result);
    }

    private List<String> resolveStatuses(String status)
    {
        if ("ongoing".equalsIgnoreCase(status))
        {
            return Collections.singletonList("1");
        }
        if ("finished".equalsIgnoreCase(status))
        {
            return Collections.singletonList("2");
        }
        if ("not_started".equalsIgnoreCase(status))
        {
            return Collections.singletonList("0");
        }
        if ("all".equalsIgnoreCase(status))
        {
            return Arrays.asList("0", "1", "2");
        }
        return Arrays.asList("0", "1", "2");
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/my")
    public AjaxResult myActivities()
    {
        List<Map<String, Object>> activities = hxActivityService.selectUserActivityOverview(getUserId());
        Date now = DateUtils.getNowDate();
        // 为每个活动添加状态信息并构建完整URL
        activities.forEach(item -> {
            Date startTime = convertToDate(item.get("startTime"));
            Date endTime = convertToDate(item.get("endTime"));
            Map<String, String> statusInfo = calculateActivityStatus(startTime, endTime, now);
            item.put("statusText", statusInfo.get("status"));
            item.put("statusType", statusInfo.get("statusType"));
            // 构建完整的封面URL
            String coverUrl = (String) item.get("coverUrl");
            if (coverUrl != null) {
                item.put("coverUrl", buildFullUrl(coverUrl));
            }
            // 构建完整的证明图片URL
            String proofImageUrl = (String) item.get("proofImageUrl");
            if (proofImageUrl != null) {
                item.put("proofImageUrl", buildFullUrl(proofImageUrl));
            }
        });
        return success(activities);
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/{activityId}")
    public AjaxResult detail(@PathVariable Long activityId)
    {
        HxActivity activity = hxActivityService.selectHxActivityById(activityId);
        if (activity == null)
        {
            throw new ServiceException("活动不存在");
        }
        Long userId = getUserId();
        HxActivityParticipant participant = hxActivityService.getParticipant(activityId, userId);
        boolean joined = participant != null && "1".equals(participant.getStatus());

        // 构建完整的封面URL
        activity.setCoverUrl(buildFullUrl(activity.getCoverUrl()));
        
        Map<String, Object> result = new HashMap<>();
        result.put("activity", activity);
        result.put("joined", joined);
        result.put("participant", buildParticipantResponse(participant));
        return success(result);
    }

    @PreAuthorize("@ss.hasRole('student')")
    @PostMapping("/{activityId}/join")
    public AjaxResult join(@PathVariable Long activityId)
    {
        HxActivityParticipant participant = hxActivityService.joinActivity(activityId, getUserId());
        return success(participant);
    }

    @PreAuthorize("@ss.hasRole('student')")
    @PostMapping("/{activityId}/proof")
    public AjaxResult submitProof(@PathVariable Long activityId,
            @Validated @RequestBody ActivityProofSubmitRequest request)
    {
        HxActivityParticipant participant = hxActivityService.submitParticipationProof(activityId, getUserId(),
                request.getImageUrl());
        return success(buildParticipantResponse(participant));
    }

    /**
     * 将时间对象转换为Date类型
     * @param timeObj 时间对象，可能是Date、LocalDateTime或其他类型
     * @return Date对象，如果转换失败返回null
     */
    private Date convertToDate(Object timeObj)
    {
        if (timeObj == null)
        {
            return null;
        }
        if (timeObj instanceof Date)
        {
            return (Date) timeObj;
        }
        if (timeObj instanceof LocalDateTime)
        {
            LocalDateTime localDateTime = (LocalDateTime) timeObj;
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        // 如果是其他类型，尝试转换为字符串再解析
        try
        {
            String timeStr = timeObj.toString();
            return DateUtils.parseDate(timeStr);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 根据时间计算活动状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param now 当前时间
     * @return 包含status和statusType的Map
     */
    private Map<String, String> calculateActivityStatus(Date startTime, Date endTime, Date now)
    {
        Map<String, String> result = new HashMap<>();
        String status = "进行中";
        String statusType = "success";
        
        // 优先判断是否已结束
        if (endTime != null && now.after(endTime))
        {
            status = "已结束";
            statusType = "default";
        }
        // 如果未结束，再判断是否未开始
        else if (startTime != null && now.before(startTime))
        {
            status = "未开始";
            statusType = "info";
        }
        
        result.put("status", status);
        result.put("statusType", statusType);
        return result;
    }

    private Map<String, Object> buildParticipantResponse(HxActivityParticipant participant)
    {
        if (participant == null)
        {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("participantId", participant.getParticipantId());
        map.put("userId", participant.getUserId());
        map.put("userName", participant.getUserName());
        map.put("status", participant.getStatus()); // Add status field
        map.put("proofImageUrl", buildFullUrl(participant.getProofImageUrl()));
        map.put("proofStatus", participant.getProofStatus());
        map.put("proofSubmitTime", participant.getProofSubmitTime());
        map.put("proofAuditRemark", participant.getProofAuditRemark());
        map.put("rewardGranted", participant.getRewardGranted());
        map.put("rewardPoints", participant.getRewardPoints());
        map.put("rewardTime", participant.getRewardTime());
        return map;
    }
    
    /**
     * 构建完整URL
     */
    private String buildFullUrl(String path)
    {
        if (StringUtils.isBlank(path))
        {
            return path;
        }
        String trimmed = path.trim();
        if (trimmed.startsWith("http"))
        {
            return trimmed;
        }
        if (trimmed.startsWith("/dev-api"))
        {
            trimmed = trimmed.substring("/dev-api".length());
        }
        if (!trimmed.startsWith("/"))
        {
            trimmed = "/" + trimmed;
        }
        return serverConfig.getUrl() + trimmed;
    }
}

