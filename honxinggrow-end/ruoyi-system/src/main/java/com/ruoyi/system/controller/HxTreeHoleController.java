package com.ruoyi.system.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.HxTreeHole;
import com.ruoyi.system.service.IHxTreeHoleService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 心灵树洞管理Controller
 * 
 * @author ruoyi
 * @date 2026-01-15
 */
@RestController
@RequestMapping("/system/hole")
public class HxTreeHoleController extends BaseController
{
    @Autowired
    private IHxTreeHoleService hxTreeHoleService;

    /**
     * 查询心灵树洞列表
     * 支持根据用户ID、用户名、内容、状态等条件查询
     * 自动关联用户表和部门表，获取用户昵称和部门名称
     */
    @PreAuthorize("@ss.hasPermi('system:hole:list')")
    @GetMapping("/list")
    public TableDataInfo list(HxTreeHole hxTreeHole)
    {
        startPage();
        List<HxTreeHole> list = hxTreeHoleService.selectHxTreeHoleList(hxTreeHole);
        return getDataTable(list);
    }

    /**
     * 导出心灵树洞列表
     */
    @PreAuthorize("@ss.hasPermi('system:hole:export')")
    @Log(title = "心灵树洞", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HxTreeHole hxTreeHole)
    {
        List<HxTreeHole> list = hxTreeHoleService.selectHxTreeHoleList(hxTreeHole);
        ExcelUtil<HxTreeHole> util = new ExcelUtil<HxTreeHole>(HxTreeHole.class);
        util.exportExcel(response, list, "心灵树洞数据");
    }

    /**
     * 获取心灵树洞详细信息
     * 包含用户昵称和部门名称信息
     */
    @PreAuthorize("@ss.hasPermi('system:hole:query')")
    @GetMapping(value = "/{treeHoleId}")
    public AjaxResult getInfo(@PathVariable("treeHoleId") Long treeHoleId)
    {
        return success(hxTreeHoleService.selectHxTreeHoleByTreeHoleId(treeHoleId));
    }

    /**
     * 新增心灵树洞
     * 注意：树洞通常由小程序端用户创建，管理端新增功能仅用于特殊情况
     */
    @PreAuthorize("@ss.hasPermi('system:hole:add')")
    @Log(title = "心灵树洞", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HxTreeHole hxTreeHole)
    {
        // 设置默认值
        if (hxTreeHole.getStatus() == null || hxTreeHole.getStatus().isEmpty())
        {
            hxTreeHole.setStatus("0"); // 默认未处理
        }
        if (hxTreeHole.getDelFlag() == null || hxTreeHole.getDelFlag().isEmpty())
        {
            hxTreeHole.setDelFlag("0"); // 默认正常
        }
        return toAjax(hxTreeHoleService.insertHxTreeHole(hxTreeHole));
    }

    /**
     * 修改心灵树洞
     * 主要用于更新状态等信息
     */
    @PreAuthorize("@ss.hasPermi('system:hole:edit')")
    @Log(title = "心灵树洞", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HxTreeHole hxTreeHole)
    {
        return toAjax(hxTreeHoleService.updateHxTreeHole(hxTreeHole));
    }

    /**
     * 删除心灵树洞
     * 支持批量删除
     */
    @PreAuthorize("@ss.hasPermi('system:hole:remove')")
    @Log(title = "心灵树洞", businessType = BusinessType.DELETE)
    @DeleteMapping("/{treeHoleIds}")
    public AjaxResult remove(@PathVariable Long[] treeHoleIds)
    {
        return toAjax(hxTreeHoleService.deleteHxTreeHoleByTreeHoleIds(treeHoleIds));
    }
}
