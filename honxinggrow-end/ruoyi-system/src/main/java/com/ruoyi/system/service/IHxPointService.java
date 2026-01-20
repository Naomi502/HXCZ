package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.HxPointLog;
import com.ruoyi.system.domain.HxStudentProfile;

/**
 * 积分管理Service接口
 *
 * @author GPT
 */
public interface IHxPointService
{
    List<HxStudentProfile> selectProfileList(HxStudentProfile profile);

    HxStudentProfile selectProfileByUserId(Long userId);

    List<HxPointLog> selectPointLogList(HxPointLog log);

    void adjustPoints(Long userId, Integer points, String remark, String operator);

    /**
     * 获取成长历程数据（近一个月按天统计）
     * @param userId 用户ID
     * @return 成长历程数据
     */
    java.util.Map<String, Object> getGrowthHistory(Long userId);

    /**
     * 获取当天成长历程数据（按24小时统计）
     * @param userId 用户ID
     * @return 当天成长历程数据
     */
    java.util.Map<String, Object> getTodayGrowthHistory(Long userId);
    
    /**
     * 查询学生列表（包含用户信息和微信绑定状态）
     * @param profile 查询条件
     * @return 学生列表
     */
    List<Map<String, Object>> selectStudentList(HxStudentProfile profile);
    
    /**
     * 查询学生详情（包含用户信息、微信绑定状态和积分信息）
     * @param userId 用户ID
     * @return 学生详情
     */
    Map<String, Object> selectStudentDetail(Long userId);
}
