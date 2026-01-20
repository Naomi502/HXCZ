package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.framework.web.service.UserDetailsServiceImpl;
import com.ruoyi.system.domain.HxUserWechat;
import com.ruoyi.system.domain.dto.WxSessionResponse;
import com.ruoyi.system.service.IHxUserWechatService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.IWxMiniProgramService;
import com.ruoyi.web.controller.app.request.WechatCodeRequest;

/**
 * 小程序认证接口
 *
 * @author GPT
 */
@Validated
@RestController
@RequestMapping("/app")
public class AppAuthController extends BaseController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private IWxMiniProgramService wxMiniProgramService;

    @Autowired
    private IHxUserWechatService hxUserWechatService;

    @PostMapping("/login")
    @RateLimiter(time = 60, count = 20, limitType = com.ruoyi.common.enums.LimitType.IP)
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        String token = loginService.loginWithoutCaptcha(loginBody.getUsername(), loginBody.getPassword());
        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 绑定微信快捷登录
     */
    @PreAuthorize("@ss.hasAnyRoles('student,teacher')")
    @PostMapping("/wechat/bind")
    public AjaxResult bindWechat(@Validated @RequestBody WechatCodeRequest request)
    {
        WxSessionResponse session = wxMiniProgramService.code2Session(request.getCode());
        hxUserWechatService.bindUserWechat(getUserId(), session.getOpenid(), session.getSessionKey());
        return AjaxResult.success("微信绑定成功");
    }

    /**
     * 微信快捷登录
     */
    @PostMapping("/wechat/login")
    @RateLimiter(time = 60, count = 20, limitType = com.ruoyi.common.enums.LimitType.IP)
    public AjaxResult wechatLogin(@Validated @RequestBody WechatCodeRequest request)
    {
        WxSessionResponse session = wxMiniProgramService.code2Session(request.getCode());
        HxUserWechat binding = hxUserWechatService.selectByOpenid(session.getOpenid());
        if (binding == null || binding.getUserId() == null)
        {
            return AjaxResult.error("该微信尚未绑定校园账号，请先使用账号密码登录并绑定");
        }
        SysUser user = sysUserService.selectUserById(binding.getUserId());
        if (user == null || StringUtils.isEmpty(user.getUserName()))
        {
            return AjaxResult.error("绑定的校园账号不存在，请联系老师处理");
        }
        // 检查用户状态
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            return AjaxResult.error("登录用户已被删除");
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            return AjaxResult.error("登录用户已被停用");
        }
        LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
        String token = tokenService.createToken(loginUser);
        hxUserWechatService.updateLoginTime(binding.getBindId());
        loginService.recordLoginInfo(user.getUserId());
        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }
}

