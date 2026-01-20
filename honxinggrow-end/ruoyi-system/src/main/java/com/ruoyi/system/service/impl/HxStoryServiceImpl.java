package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SnowflakeIdWorker;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.HxPointLog;
import com.ruoyi.system.domain.HxStory;
import com.ruoyi.system.domain.HxStoryLike;
import com.ruoyi.system.mapper.HxPointLogMapper;
import com.ruoyi.system.mapper.HxStoryLikeMapper;
import com.ruoyi.system.mapper.HxStoryMapper;
import com.ruoyi.system.mapper.HxStudentProfileMapper;
import com.ruoyi.system.service.IHxStoryService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 故事管理Service业务层处理
 *
 * @author GPT
 */
@Service
public class HxStoryServiceImpl implements IHxStoryService
{
    private static final String STORY_STATUS_PUBLISHED = "1";
    private static final String DEL_FLAG_NORMAL = "0";
    private static final String BIZ_TYPE_STORY_LIKE = "STORY_LIKE";
    private static final String CONFIG_KEY_STORY_REWARD_POINT = "hx.story.reward.points";
    private static final int DEFAULT_STORY_REWARD_POINT = 1;

    @Autowired
    private HxStoryMapper hxStoryMapper;

    @Autowired
    private HxStoryLikeMapper hxStoryLikeMapper;

    @Autowired
    private HxPointLogMapper hxPointLogMapper;

    @Autowired
    private HxStudentProfileMapper hxStudentProfileMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysConfigService configService;

    @Override
    public HxStory selectHxStoryById(Long storyId)
    {
        return hxStoryMapper.selectHxStoryById(storyId);
    }

    @Override
    public List<HxStory> selectHxStoryList(HxStory story)
    {
        return hxStoryMapper.selectHxStoryList(story);
    }

    @Override
    public int insertHxStory(HxStory story)
    {
        Date now = DateUtils.getNowDate();
        // 使用雪花算法生成故事ID
        if (story.getStoryId() == null) {
            story.setStoryId(SnowflakeIdWorker.getId());
        }
        story.setCreateTime(now);
        if (story.getDelFlag() == null)
        {
            story.setDelFlag(DEL_FLAG_NORMAL);
        }
        if (story.getViewCount() == null)
        {
            story.setViewCount(0);
        }
        if (story.getLikeCount() == null)
        {
            story.setLikeCount(0);
        }
        if (StringUtils.equals(STORY_STATUS_PUBLISHED, story.getStatus()) && story.getPublishTime() == null)
        {
            story.setPublishTime(now);
        }
        return hxStoryMapper.insertHxStory(story);
    }

    @Override
    public int updateHxStory(HxStory story)
    {
        Date now = DateUtils.getNowDate();
        story.setUpdateTime(now);
        if (story.getDelFlag() == null)
        {
            story.setDelFlag(DEL_FLAG_NORMAL);
        }
        if (StringUtils.equals(STORY_STATUS_PUBLISHED, story.getStatus()) && story.getPublishTime() == null)
        {
            story.setPublishTime(now);
        }
        return hxStoryMapper.updateHxStory(story);
    }

    @Override
    public int deleteHxStoryByIds(Long[] storyIds)
    {
        return hxStoryMapper.deleteHxStoryByIds(storyIds);
    }

    @Override
    public int deleteHxStoryById(Long storyId)
    {
        return hxStoryMapper.deleteHxStoryById(storyId);
    }

    @Override
    public List<HxStory> selectPublishedStories()
    {
        return hxStoryMapper.selectPublishedStories();
    }

    @Override
    public List<HxStory> selectLikedStoriesByUserId(Long userId)
    {
        return hxStoryMapper.selectLikedStoriesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HxStory readStory(Long storyId, Long userId)
    {
        HxStory story = hxStoryMapper.selectHxStoryById(storyId);
        if (story == null || "2".equals(story.getDelFlag()))
        {
            throw new ServiceException("故事不存在或已删除");
        }
        if (!StringUtils.equals(STORY_STATUS_PUBLISHED, story.getStatus()))
        {
            throw new ServiceException("故事未发布，暂不可阅读");
        }
        hxStoryMapper.increaseViewCount(storyId);
        return story;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeStory(Long storyId, Long userId)
    {
        if (userId == null)
        {
            throw new ServiceException("用户未登录");
        }
        
        HxStory story = hxStoryMapper.selectHxStoryById(storyId);
        if (story == null || "2".equals(story.getDelFlag()))
        {
            throw new ServiceException("故事不存在或已删除");
        }
        if (!StringUtils.equals(STORY_STATUS_PUBLISHED, story.getStatus()))
        {
            throw new ServiceException("故事未发布，暂不可点赞");
        }

        // 检查是否已点赞
        HxStoryLike existLike = hxStoryLikeMapper.selectByStoryAndUser(storyId, userId);
        if (existLike != null)
        {
            throw new ServiceException("您已经点赞过这篇故事了");
        }

        // 检查是否已获得过积分（防止重复点赞）
        HxPointLog existLog = hxPointLogMapper.selectByUserAndBiz(userId, BIZ_TYPE_STORY_LIKE, storyId);
        if (existLog != null)
        {
            throw new ServiceException("您已经点赞过这篇故事了");
        }

        SysUser user = sysUserService.selectUserById(userId);
        String userName = user != null ? user.getNickName() : null;
        Date now = DateUtils.getNowDate();

        // 插入点赞记录
        HxStoryLike like = new HxStoryLike();
        like.setStoryId(storyId);
        like.setUserId(userId);
        like.setUserName(userName);
        like.setCreateTime(now);
        hxStoryLikeMapper.insertHxStoryLike(like);

        // 增加故事点赞数
        hxStoryMapper.increaseLikeCount(storyId);

        // 给予积分奖励（仅学生）
        boolean isStudent = isStudent(userId);
        if (isStudent)
        {
            int rewardPoint = getStoryRewardPoint();
            HxPointLog log = new HxPointLog();
            log.setUserId(userId);
            log.setUserName(userName);
            log.setBizType(BIZ_TYPE_STORY_LIKE);
            log.setBizId(storyId);
            log.setPoints(rewardPoint);
            log.setRemark("点赞故事获得积分");
            log.setEventTime(now);
            log.setCreateBy(userName);
            log.setCreateTime(now);
            hxPointLogMapper.insertHxPointLog(log);

            hxStudentProfileMapper.addPoints(userId, userName, rewardPoint, now, null);
        }

        return true;
    }

    @Override
    public boolean hasLiked(Long storyId, Long userId)
    {
        if (storyId == null || userId == null)
        {
            return false;
        }
        HxStoryLike like = hxStoryLikeMapper.selectByStoryAndUser(storyId, userId);
        return like != null;
    }

    private boolean isStudent(Long userId)
    {
        SysUser user = sysUserService.selectUserById(userId);
        if (user == null || user.getRoles() == null)
        {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> "student".equals(role.getRoleKey()));
    }

    private int getStoryRewardPoint()
    {
        String value = configService.selectConfigByKey(CONFIG_KEY_STORY_REWARD_POINT);
        if (StringUtils.isNumeric(value))
        {
            try
            {
                int parsed = Integer.parseInt(value);
                return parsed > 0 ? parsed : DEFAULT_STORY_REWARD_POINT;
            }
            catch (NumberFormatException ex)
            {
                return DEFAULT_STORY_REWARD_POINT;
            }
        }
        return DEFAULT_STORY_REWARD_POINT;
    }
}
