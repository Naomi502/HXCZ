package com.ruoyi.web.controller.hx;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.HxUserMessage;
import com.ruoyi.system.service.IHxUserMessageService;

/**
 * 用户消息管理控制器 (后台)
 *
 * @author GPT
 */
@RestController
@RequestMapping("/hx/message")
public class HxUserMessageController extends BaseController
{
    @Autowired
    private IHxUserMessageService hxUserMessageService;

    /**
     * 查询用户消息列表
     */
    @PreAuthorize("@ss.hasPermi('hx:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(HxUserMessage hxUserMessage)
    {
        startPage();
        List<HxUserMessage> list = hxUserMessageService.selectHxUserMessageList(hxUserMessage);
        return getDataTable(list);
    }

    /**
     * 删除用户消息
     */
    @PreAuthorize("@ss.hasPermi('hx:message:remove')")
    @Log(title = "用户消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds)
    {
        return toAjax(hxUserMessageService.deleteHxUserMessageByMessageIds(messageIds));
    }
}
