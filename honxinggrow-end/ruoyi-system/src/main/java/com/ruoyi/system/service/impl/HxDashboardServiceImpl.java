package com.ruoyi.system.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.HxDashboardMapper;
import com.ruoyi.system.service.IHxDashboardService;

@Service
public class HxDashboardServiceImpl implements IHxDashboardService
{
    @Autowired
    private HxDashboardMapper hxDashboardMapper;

    @Override
    public Map<String, Object> getDashboardStatistics()
    {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> statistics = hxDashboardMapper.getStatistics();
        result.put("statistics", statistics);
        
        List<Map<String, Object>> activityTrend = hxDashboardMapper.getActivityTrend();
        result.put("activityTrend", activityTrend);
        
        List<Map<String, Object>> pointsTrend = hxDashboardMapper.getPointsTrend();
        result.put("pointsTrend", pointsTrend);
        
        List<Map<String, Object>> topStories = hxDashboardMapper.getTopStories();
        result.put("topStories", topStories);
        
        List<Map<String, Object>> recentActivities = hxDashboardMapper.getRecentActivities();
        result.put("recentActivities", recentActivities);
        
        List<Map<String, Object>> recentStories = hxDashboardMapper.getRecentStories();
        result.put("recentStories", recentStories);
        
        List<Map<String, Object>> recentPointLogs = hxDashboardMapper.getRecentPointLogs();
        result.put("recentPointLogs", recentPointLogs);
        
        return result;
    }
}
