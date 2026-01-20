package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.HxTreeHole;
import com.ruoyi.system.service.IHxTreeHoleService;

/**
 * 小程序心灵树洞接口
 *
 * @author GPT
 */
@RestController
@RequestMapping("/app/treehole")
public class AppTreeHoleController extends BaseController
{
    @Autowired
    private IHxTreeHoleService hxTreeHoleService;

    /**
     * 发送心灵树洞消息
     *
     * @param request 请求参数，包含content字段
     * @return 结果
     */
    @PreAuthorize("@ss.hasAnyRoles('student')")
    @PostMapping("/send")
    public AjaxResult send(@RequestBody TreeHoleSendRequest request)
    {
        Long userId = getUserId();
        String userName = getUsername();
        HxTreeHole hxTreeHole = new HxTreeHole();
        hxTreeHole.setUserId(userId);
        hxTreeHole.setUserName(userName);
        hxTreeHole.setContent(request.getContent());
        int result = hxTreeHoleService.insertHxTreeHole(hxTreeHole);
        return toAjax(result);
    }

    /**
     * 查询用户的心灵树洞历史记录
     *
     * @return 心灵树洞列表
     */
    @PreAuthorize("@ss.hasAnyRoles('student')")
    @GetMapping("/history")
    public AjaxResult history()
    {
        HxTreeHole hxTreeHole = new HxTreeHole();
        hxTreeHole.setUserId(getUserId());
        List<HxTreeHole> treeHoles = hxTreeHoleService.selectHxTreeHoleList(hxTreeHole);
        return success(treeHoles);
    }

    /**
     * 心灵树洞发送请求参数
     */
    public static class TreeHoleSendRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}