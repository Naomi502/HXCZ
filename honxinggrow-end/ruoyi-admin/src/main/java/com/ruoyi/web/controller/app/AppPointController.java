package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.HxPointLog;
import com.ruoyi.system.domain.HxStudentProfile;
import com.ruoyi.system.service.IHxPointService;
import com.ruoyi.system.service.IHxUserWechatService;

/**
 * 小程序积分接口
 *
 * @author GPT
 */
@RestController
@RequestMapping("/app/points")
public class AppPointController extends BaseController
{
    @Autowired
    private IHxPointService hxPointService;

    @Autowired
    private IHxUserWechatService hxUserWechatService;

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/profile")
    public AjaxResult profile()
    {
        HxStudentProfile profile = hxPointService.selectProfileByUserId(getUserId());
        if (profile == null)
        {
            profile = new HxStudentProfile();
            profile.setUserId(getUserId());
            profile.setUserName(SecurityUtils.getUsername());
            profile.setPointsTotal(0);
        }
        else if (profile.getUserName() == null)
        {
            profile.setUserName(SecurityUtils.getUsername());
        }
        profile.setWechatBound(hxUserWechatService.isBound(getUserId()));
        return success(profile);
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/logs")
    public AjaxResult logs(HxPointLog log)
    {
        log.setUserId(getUserId());
        startPage();
        List<HxPointLog> list = hxPointService.selectPointLogList(log);
        return success(getDataTable(list));
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/growth-history")
    public AjaxResult growthHistory()
    {
        return success(hxPointService.getGrowthHistory(getUserId()));
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @GetMapping("/today-growth-history")
    public AjaxResult todayGrowthHistory()
    {
        return success(hxPointService.getTodayGrowthHistory(getUserId()));
    }
}

