package com.ruoyi.system.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.HxPointLog;
import com.ruoyi.system.domain.HxStudentProfile;
import com.ruoyi.system.mapper.HxActivityParticipantMapper;
import com.ruoyi.system.mapper.HxPointLogMapper;
import com.ruoyi.system.mapper.HxStoryLikeMapper;
import com.ruoyi.system.mapper.HxStudentProfileMapper;
import com.ruoyi.system.service.IHxPointService;
import com.ruoyi.system.service.IHxUserWechatService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 积分管理Service业务层处理
 *
 * @author GPT
 */
@Service
public class HxPointServiceImpl implements IHxPointService
{
    private static final String BIZ_TYPE_MANUAL_ADJUST = "POINT_ADJUST";

    @Autowired
    private HxStudentProfileMapper hxStudentProfileMapper;

    @Autowired
    private HxPointLogMapper hxPointLogMapper;

    @Autowired
    private HxStoryLikeMapper hxStoryLikeMapper;

    @Autowired
    private HxActivityParticipantMapper hxActivityParticipantMapper;

    @Autowired
    private ISysUserService sysUserService;
    
    @Autowired
    private ISysRoleService sysRoleService;
    
    @Autowired
    private IHxUserWechatService hxUserWechatService;

    @Override
    public List<HxStudentProfile> selectProfileList(HxStudentProfile profile)
    {
        return hxStudentProfileMapper.selectHxStudentProfileList(profile);
    }

    @Override
    public HxStudentProfile selectProfileByUserId(Long userId)
    {
        return hxStudentProfileMapper.selectByUserId(userId);
    }

    @Override
    public List<HxPointLog> selectPointLogList(HxPointLog log)
    {
        return hxPointLogMapper.selectHxPointLogList(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustPoints(Long userId, Integer points, String remark, String operator)
    {
        if (points == null || points == 0)
        {
            throw new ServiceException("积分变动值不能为空");
        }
        SysUser user = sysUserService.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }
        String userName = user.getNickName();
        Date now = DateUtils.getNowDate();

        HxPointLog log = new HxPointLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setBizType(BIZ_TYPE_MANUAL_ADJUST);
        log.setBizId(0L);
        log.setPoints(points);
        log.setRemark(remark);
        log.setEventTime(now);
        log.setCreateBy(operator);
        log.setCreateTime(now);
        hxPointLogMapper.insertHxPointLog(log);

        hxStudentProfileMapper.addPoints(userId, userName, points, null, null);
    }

    @Override
    public Map<String, Object> getGrowthHistory(Long userId)
    {
        try
        {
            // 计算近30天的日期范围（不包含今天）
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.minusDays(1); // 昨天（最后一天）
            LocalDate startDate = endDate.minusDays(29); // 从30天前开始，共30天
            
            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()); // 昨天23:59:59
            
            // 初始化30天的数据（不包含今天）
            List<String> dates = new ArrayList<>();
            List<Integer> storyLikes = new ArrayList<>();
            List<Integer> activities = new ArrayList<>();
            List<Integer> points = new ArrayList<>();
            
            for (int i = 0; i < 30; i++)
            {
                LocalDate date = startDate.plusDays(i);
                dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
                storyLikes.add(0);
                activities.add(0);
                points.add(0);
            }
            
            // 查询点赞故事数据
            try
            {
                List<Map<String, Object>> storyLikeData = hxStoryLikeMapper.selectDailyCountByUserAndDateRange(userId, start, end);
                for (Map<String, Object> item : storyLikeData)
                {
                    LocalDate localDate = parseDate(item.get("date"));
                    if (localDate != null)
                    {
                        Integer count = item.get("count") != null ? ((Number) item.get("count")).intValue() : 0;
                        int dayIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, localDate);
                        if (dayIndex >= 0 && dayIndex < 30)
                        {
                            storyLikes.set(dayIndex, count);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // 如果查询失败，继续处理其他数据
            }
            
            // 查询参加活动数据
            try
            {
                List<Map<String, Object>> activityData = hxActivityParticipantMapper.selectDailyCountByUserAndDateRange(userId, start, end);
                for (Map<String, Object> item : activityData)
                {
                    LocalDate localDate = parseDate(item.get("date"));
                    if (localDate != null)
                    {
                        Integer count = item.get("count") != null ? ((Number) item.get("count")).intValue() : 0;
                        int dayIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, localDate);
                        if (dayIndex >= 0 && dayIndex < 30)
                        {
                            activities.set(dayIndex, count);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // 如果查询失败，继续处理其他数据
            }
            
            // 查询积分数据
            try
            {
                List<Map<String, Object>> pointData = hxPointLogMapper.selectDailyPointsByUserAndDateRange(userId, start, end);
                for (Map<String, Object> item : pointData)
                {
                    LocalDate localDate = parseDate(item.get("date"));
                    if (localDate != null)
                    {
                        Object pointsObj = item.get("points");
                        Integer point = 0;
                        if (pointsObj != null)
                        {
                            if (pointsObj instanceof Number)
                            {
                                point = ((Number) pointsObj).intValue();
                            }
                            else if (pointsObj instanceof String)
                            {
                                try
                                {
                                    point = Integer.parseInt((String) pointsObj);
                                }
                                catch (NumberFormatException e)
                                {
                                    point = 0;
                                }
                            }
                        }
                        int dayIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, localDate);
                        if (dayIndex >= 0 && dayIndex < 30)
                        {
                            points.set(dayIndex, point);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // 如果查询失败，继续处理其他数据
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("dates", dates);
            result.put("storyLikes", storyLikes);
            result.put("activities", activities);
            result.put("points", points);
            return result;
        }
        catch (Exception e)
        {
            // 返回空数据而不是抛出异常（不包含今天）
            Map<String, Object> result = new HashMap<>();
            List<String> dates = new ArrayList<>();
            List<Integer> storyLikes = new ArrayList<>();
            List<Integer> activities = new ArrayList<>();
            List<Integer> points = new ArrayList<>();
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 30; i++)
            {
                LocalDate date = today.minusDays(30 - i); // 从30天前开始，不包含今天
                dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
                storyLikes.add(0);
                activities.add(0);
                points.add(0);
            }
            result.put("dates", dates);
            result.put("storyLikes", storyLikes);
            result.put("activities", activities);
            result.put("points", points);
            return result;
        }
    }
    
    @Override
    public Map<String, Object> getTodayGrowthHistory(Long userId)
    {
        try
        {
            // 获取当天的开始和结束时间
            java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
            java.time.LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);
            
            Date start = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());
            
            // 初始化24小时的数据（0-23）
            List<String> hours = new ArrayList<>();
            List<Integer> storyLikes = new ArrayList<>();
            List<Integer> activities = new ArrayList<>();
            List<Integer> points = new ArrayList<>();
            
            for (int i = 0; i < 24; i++)
            {
                hours.add(String.format("%02d:00", i));
                storyLikes.add(0);
                activities.add(0);
                points.add(0);
            }
            
            // 查询点赞故事数据（按小时）
            try
            {
                List<Map<String, Object>> storyLikeData = hxStoryLikeMapper.selectHourlyCountByUserAndDateRange(userId, start, end);
                for (Map<String, Object> item : storyLikeData)
                {
                    Integer hour = parseHour(item.get("hour"));
                    if (hour != null && hour >= 0 && hour < 24)
                    {
                        Integer count = item.get("count") != null ? ((Number) item.get("count")).intValue() : 0;
                        storyLikes.set(hour, count);
                    }
                }
            }
            catch (Exception e)
            {
                // 如果查询失败，继续处理其他数据
            }
            
            // 查询参加活动数据（按小时）
            try
            {
                List<Map<String, Object>> activityData = hxActivityParticipantMapper.selectHourlyCountByUserAndDateRange(userId, start, end);
                for (Map<String, Object> item : activityData)
                {
                    Integer hour = parseHour(item.get("hour"));
                    if (hour != null && hour >= 0 && hour < 24)
                    {
                        Integer count = item.get("count") != null ? ((Number) item.get("count")).intValue() : 0;
                        activities.set(hour, count);
                    }
                }
            }
            catch (Exception e)
            {
                // 如果查询失败，继续处理其他数据
            }
            
            // 查询积分数据（按小时）
            try
            {
                List<Map<String, Object>> pointData = hxPointLogMapper.selectHourlyPointsByUserAndDateRange(userId, start, end);
                for (Map<String, Object> item : pointData)
                {
                    Integer hour = parseHour(item.get("hour"));
                    if (hour != null && hour >= 0 && hour < 24)
                    {
                        Object pointsObj = item.get("points");
                        Integer point = 0;
                        if (pointsObj != null)
                        {
                            if (pointsObj instanceof Number)
                            {
                                point = ((Number) pointsObj).intValue();
                            }
                            else if (pointsObj instanceof String)
                            {
                                try
                                {
                                    point = Integer.parseInt((String) pointsObj);
                                }
                                catch (NumberFormatException ex)
                                {
                                    point = 0;
                                }
                            }
                        }
                        points.set(hour, point);
                    }
                }
            }
            catch (Exception e)
            {
                // 如果查询失败，继续处理其他数据
            }
            
            // 将数据转换为累计值（从前往后累加）
            for (int i = 1; i < 24; i++)
            {
                storyLikes.set(i, storyLikes.get(i - 1) + storyLikes.get(i));
                activities.set(i, activities.get(i - 1) + activities.get(i));
                points.set(i, points.get(i - 1) + points.get(i));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("dates", hours);
            result.put("storyLikes", storyLikes);
            result.put("activities", activities);
            result.put("points", points);
            return result;
        }
        catch (Exception e)
        {
            // 返回空数据而不是抛出异常
            Map<String, Object> result = new HashMap<>();
            List<String> hours = new ArrayList<>();
            List<Integer> storyLikes = new ArrayList<>();
            List<Integer> activities = new ArrayList<>();
            List<Integer> points = new ArrayList<>();
            for (int i = 0; i < 24; i++)
            {
                hours.add(String.format("%02d:00", i));
                storyLikes.add(0);
                activities.add(0);
                points.add(0);
            }
            result.put("dates", hours);
            result.put("storyLikes", storyLikes);
            result.put("activities", activities);
            result.put("points", points);
            return result;
        }
    }
    
    /**
     * 解析小时数
     */
    private Integer parseHour(Object hourObj)
    {
        if (hourObj == null)
        {
            return null;
        }
        try
        {
            if (hourObj instanceof Number)
            {
                return ((Number) hourObj).intValue();
            }
            else if (hourObj instanceof String)
            {
                return Integer.parseInt((String) hourObj);
            }
        }
        catch (Exception e)
        {
            // 解析失败，返回null
        }
        return null;
    }
    
    /**
     * 解析日期对象为LocalDate
     */
    private LocalDate parseDate(Object dateObj)
    {
        if (dateObj == null)
        {
            return null;
        }
        try
        {
            if (dateObj instanceof Date)
            {
                return ((Date) dateObj).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            else if (dateObj instanceof java.sql.Date)
            {
                return ((java.sql.Date) dateObj).toLocalDate();
            }
            else if (dateObj instanceof String)
            {
                String dateStr = (String) dateObj;
                // 尝试解析 "yyyy-MM-dd" 格式
                if (dateStr.length() >= 10)
                {
                    dateStr = dateStr.substring(0, 10);
                }
                return LocalDate.parse(dateStr);
            }
        }
        catch (Exception e)
        {
            // 解析失败，返回null
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> selectStudentList(HxStudentProfile profile)
    {
        return hxStudentProfileMapper.selectStudentList(profile);
    }

    @Override
    public Map<String, Object> selectStudentDetail(Long userId)
    {
        Map<String, Object> student = new HashMap<>();
        
        // 查询用户信息
        SysUser user = sysUserService.selectUserById(userId);
        if (user != null)
        {
            student.put("userId", user.getUserId());
            student.put("userName", user.getUserName());
            student.put("nickName", user.getNickName());
            student.put("email", user.getEmail());
            student.put("phonenumber", user.getPhonenumber());
            student.put("sex", user.getSex());
            student.put("status", user.getStatus());
            student.put("avatar", user.getAvatar());
            student.put("deptId", user.getDeptId());
            student.put("createTime", user.getCreateTime());
        }
        
        // 查询积分信息
        HxStudentProfile profile = hxStudentProfileMapper.selectByUserId(userId);
        if (profile != null)
        {
            student.put("pointsTotal", profile.getPointsTotal());
            student.put("lastStoryPointTime", profile.getLastStoryPointTime());
            student.put("lastActivityPointTime", profile.getLastActivityPointTime());
        }
        
        // 查询微信绑定状态
        student.put("wechatBound", isBound(userId));
        
        return student;
    }
    
    /**
     * 检查用户是否绑定微信
     * @param userId 用户ID
     * @return 是否绑定
     */
    private boolean isBound(Long userId)
    {
        return hxUserWechatService.selectByUserId(userId) != null;
    }
}
