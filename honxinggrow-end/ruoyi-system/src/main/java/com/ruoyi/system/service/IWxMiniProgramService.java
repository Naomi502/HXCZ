package com.ruoyi.system.service;

import com.ruoyi.system.domain.dto.WxSessionResponse;

/**
 * 微信小程序接口
 *
 * @author GPT
 */
public interface IWxMiniProgramService
{
    /**
     * 通过 wx.login code 换取 session
     *
     * @param jsCode 小程序临时登录凭证
     * @return 微信 session 信息
     */
    WxSessionResponse code2Session(String jsCode);
}


