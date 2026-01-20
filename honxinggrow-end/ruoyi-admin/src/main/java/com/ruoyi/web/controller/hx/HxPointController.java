package com.ruoyi.web.controller.hx;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.HxPointLog;
import com.ruoyi.system.domain.HxStudentProfile;
import com.ruoyi.system.service.IHxPointService;

/**
 * 积分管理控制器
 *
 * @author GPT
 */
@RestController
@Validated
@RequestMapping("/hx/points")
public class HxPointController extends BaseController
{
    @Autowired
    private IHxPointService hxPointService;

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/profile/list")
    public TableDataInfo profileList(HxStudentProfile profile)
    {
        startPage();
        List<HxStudentProfile> list = hxPointService.selectProfileList(profile);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/profile/{userId}")
    public AjaxResult profileInfo(@PathVariable Long userId)
    {
        return success(hxPointService.selectProfileByUserId(userId));
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/log/list")
    public TableDataInfo logList(HxPointLog log)
    {
        startPage();
        List<HxPointLog> list = hxPointService.selectPointLogList(log);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "积分调整", businessType = BusinessType.UPDATE)
    @PostMapping("/adjust")
    public AjaxResult adjust(@Valid @RequestBody PointAdjustRequest request)
    {
        hxPointService.adjustPoints(request.getUserId(), request.getPoints(), request.getRemark(), getUsername());
        return success();
    }

    public static class PointAdjustRequest
    {
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        @NotNull(message = "积分变动值不能为空")
        private Integer points;

        private String remark;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public Integer getPoints()
        {
            return points;
        }

        public void setPoints(Integer points)
        {
            this.points = points;
        }

        public String getRemark()
        {
            return remark;
        }

        public void setRemark(String remark)
        {
            this.remark = remark;
        }
    }
}
