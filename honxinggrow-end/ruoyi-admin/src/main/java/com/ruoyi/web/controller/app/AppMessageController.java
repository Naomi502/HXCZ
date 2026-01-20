package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.HxUserMessage;
import com.ruoyi.system.service.IHxUserMessageService;

/**
 * APP 消息通知控制器
 *
 * @author GPT
 */
@RestController
@RequestMapping("/app/message")
public class AppMessageController extends BaseController
{
    @Autowired
    private IHxUserMessageService hxUserMessageService;

    /**
     * 获取我的消息列表
     */
    @PreAuthorize("@ss.hasRole('student')")
    @GetMapping("/list")
    public TableDataInfo list(HxUserMessage hxUserMessage)
    {
        startPage();
        hxUserMessage.setUserId(getUserId());
        List<HxUserMessage> list = hxUserMessageService.selectHxUserMessageList(hxUserMessage);
        return getDataTable(list);
    }

    /**
     * 获取未读消息数量
     */
    @PreAuthorize("@ss.hasRole('student')")
    @GetMapping("/unread")
    public AjaxResult getUnreadCount()
    {
        return success(hxUserMessageService.countUnreadMessages(getUserId()));
    }

    /**
     * 标记消息已读
     */
    @PreAuthorize("@ss.hasRole('student')")
    @PostMapping("/{messageId}/read")
    public AjaxResult read(@PathVariable Long messageId)
    {
        HxUserMessage message = hxUserMessageService.selectHxUserMessageByMessageId(messageId);
        if (message == null || !message.getUserId().equals(getUserId())) {
             return error("消息不存在或无权访问");
        }
        return toAjax(hxUserMessageService.readMessage(messageId));
    }

    /**
     * 删除消息
     */
    @PreAuthorize("@ss.hasRole('student')")
    @PostMapping("/{messageId}/delete")
    public AjaxResult delete(@PathVariable Long messageId)
    {
        HxUserMessage message = hxUserMessageService.selectHxUserMessageByMessageId(messageId);
        if (message == null || !message.getUserId().equals(getUserId())) {
             return error("消息不存在或无权访问");
        }
        return toAjax(hxUserMessageService.deleteHxUserMessageByMessageId(messageId));
    }

    /**
     * 清空我的所有消息
     */
    @PreAuthorize("@ss.hasRole('student')")
    @PostMapping("/clear")
    public AjaxResult clearAll()
    {
        return toAjax(hxUserMessageService.clearAllMessages(getUserId()));
    }
}
