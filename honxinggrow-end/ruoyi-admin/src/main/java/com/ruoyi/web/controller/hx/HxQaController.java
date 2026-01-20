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
import com.ruoyi.system.domain.HxQa;
import com.ruoyi.system.service.IHxQaService;

/**
 * 问答管理控制器
 *
 * @author Trae IDE
 */
@RestController
@RequestMapping("/hx/qa")
public class HxQaController extends BaseController
{
    @Autowired
    private IHxQaService hxQaService;

    /**
     * 查询问答列表
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/list")
    public TableDataInfo list(HxQa qa)
    {
        startPage();
        List<HxQa> list = hxQaService.selectHxQaList(qa);
        return getDataTable(list);
    }

    /**
     * 获取问答详细信息
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/{qaId}")
    public AjaxResult getInfo(@PathVariable Long qaId)
    {
        return success(hxQaService.selectHxQaById(qaId));
    }

    /**
     * 新增问答
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "问答管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HxQa qa)
    {
        qa.setCreateBy(getUsername());
        if ("1".equals(qa.getStatus()))
        {
            qa.setPublishUserId(getUserId());
            qa.setPublishUserName(getUsername());
        }
        return toAjax(hxQaService.insertHxQa(qa));
    }

    /**
     * 修改问答
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "问答管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HxQa qa)
    {
        qa.setUpdateBy(getUsername());
        if ("1".equals(qa.getStatus()) && qa.getPublishUserId() == null)
        {
            qa.setPublishUserId(getUserId());
            qa.setPublishUserName(getUsername());
        }
        return toAjax(hxQaService.updateHxQa(qa));
    }

    /**
     * 删除问答
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @Log(title = "问答管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{qaIds}")
    public AjaxResult remove(@PathVariable Long[] qaIds)
    {
        return toAjax(hxQaService.deleteHxQaByIds(qaIds));
    }
}