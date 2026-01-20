package com.ruoyi.web.controller.hx;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.HxStory;
import com.ruoyi.system.service.IHxStoryService;

/**
 * 故事管理控制器
 *
 * @author GPT
 */
@RestController
@RequestMapping("/hx/story")
public class HxStoryController extends BaseController
{
    @Autowired
    private IHxStoryService hxStoryService;

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/list")
    public TableDataInfo list(HxStory story)
    {
        startPage();
        List<HxStory> list = hxStoryService.selectHxStoryList(story);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/{storyId}")
    public AjaxResult getInfo(@PathVariable Long storyId)
    {
        return success(hxStoryService.selectHxStoryById(storyId));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "成长故事", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HxStory story)
    {
        story.setCreateBy(getUsername());
        if ("1".equals(story.getStatus()))
        {
            story.setPublishUserId(getUserId());
            story.setPublishUserName(getUsername());
        }
        return toAjax(hxStoryService.insertHxStory(story));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "成长故事", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HxStory story)
    {
        story.setUpdateBy(getUsername());
        if ("1".equals(story.getStatus()) && story.getPublishUserId() == null)
        {
            story.setPublishUserId(getUserId());
            story.setPublishUserName(getUsername());
        }
        return toAjax(hxStoryService.updateHxStory(story));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "成长故事", businessType = BusinessType.DELETE)
    @DeleteMapping("/{storyIds}")
    public AjaxResult remove(@PathVariable Long[] storyIds)
    {
        return toAjax(hxStoryService.deleteHxStoryByIds(storyIds));
    }
}
