package com.ruoyi.web.controller.hx;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.annotation.Validated;
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
import com.ruoyi.system.domain.HxActivity;
import com.ruoyi.system.domain.HxActivityParticipant;
import com.ruoyi.system.service.IHxActivityService;
import com.ruoyi.web.controller.hx.vo.ActivityProofAuditRequest;

/**
 * 活动管理控制器
 *
 * @author GPT
 */
@RestController
@RequestMapping("/hx/activity")
public class HxActivityController extends BaseController
{
    @Autowired
    private IHxActivityService hxActivityService;

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/list")
    public TableDataInfo list(HxActivity activity)
    {
        startPage();
        List<HxActivity> list = hxActivityService.selectHxActivityList(activity);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/{activityId}")
    public AjaxResult getInfo(@PathVariable Long activityId)
    {
        return success(hxActivityService.selectHxActivityById(activityId));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "成长活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HxActivity activity)
    {
        activity.setCreateBy(getUsername());
        if ("1".equals(activity.getStatus()))
        {
            activity.setPublishUserId(getUserId());
            activity.setPublishUserName(getUsername());
        }
        return toAjax(hxActivityService.insertHxActivity(activity));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "成长活动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HxActivity activity)
    {
        activity.setUpdateBy(getUsername());
        if ("1".equals(activity.getStatus()) && activity.getPublishUserId() == null)
        {
            activity.setPublishUserId(getUserId());
            activity.setPublishUserName(getUsername());
        }
        return toAjax(hxActivityService.updateHxActivity(activity));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "成长活动", businessType = BusinessType.DELETE)
    @DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        return toAjax(hxActivityService.deleteHxActivityByIds(activityIds));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/{activityId}/participants")
    public TableDataInfo participants(@PathVariable Long activityId)
    {
        startPage();
        List<HxActivityParticipant> list = hxActivityService.selectParticipants(activityId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "活动证明审核", businessType = BusinessType.UPDATE)
    @PostMapping("/participant/{participantId}/audit")
    public AjaxResult auditParticipant(@PathVariable Long participantId,
            @Validated @RequestBody ActivityProofAuditRequest request)
    {
        hxActivityService.auditParticipantProof(participantId, Boolean.TRUE.equals(request.getApproved()),
                request.getRemark(), getUserId(), getUsername());
        return success();
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "活动报名审核", businessType = BusinessType.UPDATE)
    @PostMapping("/participant/{participantId}/audit-enroll")
    public AjaxResult auditParticipation(@PathVariable Long participantId,
            @Validated @RequestBody ActivityProofAuditRequest request)
    {
        hxActivityService.auditParticipation(participantId, Boolean.TRUE.equals(request.getApproved()),
                request.getRemark(), getUserId(), getUsername());
        return success();
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "取消活动报名", businessType = BusinessType.UPDATE)
    @PostMapping("/participant/{participantId}/cancel")
    public AjaxResult cancelParticipation(@PathVariable Long participantId)
    {
        hxActivityService.cancelParticipation(participantId, getUsername());
        return success();
    }
}
