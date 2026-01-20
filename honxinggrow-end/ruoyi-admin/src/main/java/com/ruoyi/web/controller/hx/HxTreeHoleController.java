package com.ruoyi.web.controller.hx;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.HxTreeHole;
import com.ruoyi.system.service.IHxTreeHoleService;

/**
 * 心灵树洞管理
 *
 * @author GPT
 */
@RestController("webHxTreeHoleController")
@RequestMapping("/hx/treehole")
public class HxTreeHoleController extends BaseController
{
    @Autowired
    private IHxTreeHoleService hxTreeHoleService;

    /**
     * 查询心灵树洞列表
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping("/list")
    public TableDataInfo list(HxTreeHole hxTreeHole)
    {
        startPage();
        List<HxTreeHole> list = hxTreeHoleService.selectHxTreeHoleList(hxTreeHole);
        return getDataTable(list);
    }

    /**
     * 获取心灵树洞详细信息
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @GetMapping(value = "/{treeHoleId}")
    public AjaxResult getInfo(@PathVariable("treeHoleId") Long treeHoleId)
    {
        return success(hxTreeHoleService.selectHxTreeHoleByTreeHoleId(treeHoleId));
    }

    /**
     * 删除心灵树洞
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    @DeleteMapping("/{treeHoleIds}")
    public AjaxResult remove(@PathVariable Long[] treeHoleIds)
    {
        return toAjax(hxTreeHoleService.deleteHxTreeHoleByTreeHoleIds(treeHoleIds));
    }
}
