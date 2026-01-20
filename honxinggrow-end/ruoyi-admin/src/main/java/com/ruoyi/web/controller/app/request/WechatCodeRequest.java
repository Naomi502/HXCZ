package com.ruoyi.web.controller.app.request;

import javax.validation.constraints.NotBlank;

/**
 * 微信 code 请求体
 *
 * @author GPT
 */
public class WechatCodeRequest
{
    @NotBlank(message = "code不能为空")
    private String code;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
}


