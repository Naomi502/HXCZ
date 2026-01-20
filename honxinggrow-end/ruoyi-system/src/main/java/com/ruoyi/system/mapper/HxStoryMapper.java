package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.HxStory;

/**
 * 故事管理Mapper接口
 *
 * @author GPT
 */
public interface HxStoryMapper
{
    HxStory selectHxStoryById(Long storyId);

    List<HxStory> selectHxStoryList(HxStory story);

    List<HxStory> selectPublishedStories();

    /**
     * 查询用户点赞的故事列表
     * @param userId 用户ID
     * @return 故事列表
     */
    List<HxStory> selectLikedStoriesByUserId(Long userId);

    int insertHxStory(HxStory story);

    int updateHxStory(HxStory story);

    int deleteHxStoryById(Long storyId);

    int deleteHxStoryByIds(Long[] storyIds);

    int increaseViewCount(@Param("storyId") Long storyId);

    /**
     * 增加点赞数
     * @param storyId 故事ID
     * @return 结果
     */
    int increaseLikeCount(@Param("storyId") Long storyId);
}
