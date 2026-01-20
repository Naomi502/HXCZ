package com.ruoyi.system.utils;

import java.util.Date;

/**
 * 活动状态计算工具类
 * 用于根据时间实时计算活动状态
 * 
 * @author ruoyi
 */
public class ActivityStatusUtils {
    public static final String STATUS_DRAFT = "0"; // 草稿
    public static final String STATUS_ONGOING = "1"; // 进行中
    public static final String STATUS_FINISHED = "2"; // 结束
    
    /**
     * 根据时间计算活动状态
     * 
     * @param startTime 活动开始时间
     * @param endTime 活动结束时间
     * @param now 当前时间
     * @return 计算得到的活动状态
     */
    public static String calculateStatus(Date startTime, Date endTime, Date now) {
        // 优先判断是否已结束
        if (endTime != null && now.after(endTime)) {
            return STATUS_FINISHED;
        }
        // 如果未结束，再判断是否已开始
        else if (startTime != null && now.after(startTime)) {
            return STATUS_ONGOING;
        }
        // 其他情况返回草稿状态
        return STATUS_DRAFT;
    }
    
    /**
     * 根据时间计算活动状态（使用当前时间）
     * 
     * @param startTime 活动开始时间
     * @param endTime 活动结束时间
     * @return 计算得到的活动状态
     */
    public static String calculateStatus(Date startTime, Date endTime) {
        return calculateStatus(startTime, endTime, new Date());
    }
}