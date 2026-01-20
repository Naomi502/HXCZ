package com.ruoyi.web.controller.hx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IHxDashboardService;

@RestController
@RequestMapping("/hx/dashboard")
public class HxDashboardController extends BaseController
{
    @Autowired
    private IHxDashboardService hxDashboardService;

    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/statistics")
    public AjaxResult statistics()
    {
        return success(hxDashboardService.getDashboardStatistics());
    }
}
