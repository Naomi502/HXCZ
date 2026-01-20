package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.HxStoryLike;

/**
 * 故事点赞Mapper接口
 *
 * @author GPT
 */
public interface HxStoryLikeMapper
{
    /**
     * 查询点赞记录
     * @param storyId 故事ID
     * @param userId 用户ID
     * @return 点赞记录
     */
    HxStoryLike selectByStoryAndUser(@Param("storyId") Long storyId, @Param("userId") Long userId);

    /**
     * 新增点赞记录
     * @param like 点赞记录
     * @return 结果
     */
    int insertHxStoryLike(HxStoryLike like);

    /**
     * 按天统计用户点赞故事数量（近一个月）
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    List<Map<String, Object>> selectDailyCountByUserAndDateRange(@Param("userId") Long userId, 
                                                                  @Param("startDate") java.util.Date startDate,
                                                                  @Param("endDate") java.util.Date endDate);

    /**
     * 按小时统计用户点赞故事数量（当天）
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 统计数据
     */
    List<Map<String, Object>> selectHourlyCountByUserAndDateRange(@Param("userId") Long userId,
                                                                     @Param("startDate") java.util.Date startDate,
                                                                     @Param("endDate") java.util.Date endDate);
}

