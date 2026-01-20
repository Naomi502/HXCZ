package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface HxDashboardMapper
{
    Map<String, Object> getStatistics();

    List<Map<String, Object>> getActivityTrend();

    List<Map<String, Object>> getPointsTrend();

    List<Map<String, Object>> getTopStories();

    List<Map<String, Object>> getRecentActivities();

    List<Map<String, Object>> getRecentStories();

    List<Map<String, Object>> getRecentPointLogs();
}
