package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.HxStory;

/**
 * 故事管理Service接口
 *
 * @author GPT
 */
public interface IHxStoryService
{
    HxStory selectHxStoryById(Long storyId);

    List<HxStory> selectHxStoryList(HxStory story);

    int insertHxStory(HxStory story);

    int updateHxStory(HxStory story);

    int deleteHxStoryByIds(Long[] storyIds);

    int deleteHxStoryById(Long storyId);

    List<HxStory> selectPublishedStories();

    /**
     * 查询用户已点赞的故事
     * @param userId 用户ID
     * @return 故事列表
     */
    List<HxStory> selectLikedStoriesByUserId(Long userId);

    /**
     * 小程序阅读故事（不再给予积分）
     *
     * @param storyId 故事ID
     * @param userId 用户ID
     * @return 故事详情
     */
    HxStory readStory(Long storyId, Long userId);

    /**
     * 点赞故事（给予积分）
     *
     * @param storyId 故事ID
     * @param userId 用户ID
     * @return 是否点赞成功
     */
    boolean likeStory(Long storyId, Long userId);

    /**
     * 检查用户是否已点赞
     *
     * @param storyId 故事ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean hasLiked(Long storyId, Long userId);
}
