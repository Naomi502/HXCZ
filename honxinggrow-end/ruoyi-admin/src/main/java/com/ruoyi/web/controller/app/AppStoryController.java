package com.ruoyi.web.controller.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.system.domain.HxStory;
import com.ruoyi.system.service.IHxStoryService;

/**
 * 小程序故事接口
 *
 * @author GPT
 */
@RestController
@RequestMapping("/app/story")
public class AppStoryController extends BaseController
{
    private static final Pattern SRC_PATTERN = Pattern.compile("src\\s*=\\s*(['\"])(.*?)\\1", Pattern.CASE_INSENSITIVE);

    @Autowired
    private IHxStoryService hxStoryService;

    @Autowired
    private ServerConfig serverConfig;

    @GetMapping("/list")
    public AjaxResult list()
    {
        List<HxStory> stories = hxStoryService.selectPublishedStories();
        return success(transformStories(stories));
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/liked-list")
    public AjaxResult likedList()
    {
        List<HxStory> stories = hxStoryService.selectLikedStoriesByUserId(getUserId());
        return success(transformStories(stories));
    }

    private List<Map<String, Object>> transformStories(List<HxStory> stories) {
        return stories.stream().map(story -> {
            Map<String, Object> map = new HashMap<>();
            map.put("storyId", story.getStoryId());
            map.put("title", story.getTitle());
            map.put("summary", story.getSummary());
            map.put("coverUrl", buildFullUrl(story.getCoverUrl()));
            map.put("publishTime", story.getPublishTime());
            map.put("viewCount", story.getViewCount());
            map.put("likeCount", story.getLikeCount() != null ? story.getLikeCount() : 0);
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{storyId}")
    public AjaxResult detail(@PathVariable Long storyId)
    {
        Long userId = getUserId();
        HxStory story = hxStoryService.readStory(storyId, userId);
        decorateStory(story);
        
        // 检查用户是否已点赞
        boolean hasLiked = hxStoryService.hasLiked(storyId, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("story", story);
        result.put("hasLiked", hasLiked);
        return success(result);
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @PostMapping("/{storyId}/like")
    public AjaxResult like(@PathVariable Long storyId)
    {
        Long userId = getUserId();
        boolean success = hxStoryService.likeStory(storyId, userId);
        if (success)
        {
            // 返回更新后的故事信息
            HxStory story = hxStoryService.selectHxStoryById(storyId);
            Map<String, Object> result = new HashMap<>();
            result.put("story", story);
            result.put("hasLiked", true);
            return success(result);
        }
        return error("点赞失败");
    }

    private boolean isStudent()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null)
        {
            return false;
        }
        if (loginUser.getUser().getRoles() != null)
        {
            for (SysRole role : loginUser.getUser().getRoles())
            {
                if ("student".equals(role.getRoleKey()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void decorateStory(HxStory story)
    {
        if (story == null)
        {
            return;
                }
        story.setCoverUrl(buildFullUrl(story.getCoverUrl()));
        story.setContent(normalizeContent(story.getContent()));
    }

    private String normalizeContent(String content)
    {
        if (StringUtils.isBlank(content))
        {
            return content;
        }
        Matcher matcher = SRC_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
        {
            String original = matcher.group(2);
            String updated = buildFullUrl(original);
            if (StringUtils.isBlank(updated))
            {
                updated = original;
            }
            String replacement = "src=" + matcher.group(1) + updated + matcher.group(1);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

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



