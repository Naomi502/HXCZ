package com.ruoyi.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.system.domain.dto.WxSessionResponse;
import com.ruoyi.system.service.IWxMiniProgramService;

/**
 * 微信小程序服务
 *
 * @author GPT
 */
@Service
public class WxMiniProgramServiceImpl implements IWxMiniProgramService
{
    private static final Logger log = LoggerFactory.getLogger(WxMiniProgramServiceImpl.class);

    private static final String CODE_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Value("${wx.miniapp.appid:}")
    private String appId;

    @Value("${wx.miniapp.secret:}")
    private String appSecret;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public WxSessionResponse code2Session(String jsCode)
    {
        if (StringUtils.isBlank(jsCode))
        {
            throw new ServiceException("code不能为空");
        }
        if (StringUtils.isAnyBlank(appId, appSecret))
        {
            throw new ServiceException("未配置微信小程序AppID或Secret");
        }
        String params = String.format("appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", appId, appSecret, jsCode);
        String raw = HttpUtils.sendGet(CODE_SESSION_URL, params);
        if (StringUtils.isBlank(raw))
        {
            throw new ServiceException("调用微信接口失败，未返回数据");
        }
        try
        {
            WxSessionResponse response = objectMapper.readValue(raw, WxSessionResponse.class);
            if (response.hasError())
            {
                log.warn("wx.code2session error, code={}, msg={}", response.getErrcode(), response.getErrmsg());
                throw new ServiceException("微信接口错误：" + response.getErrmsg());
            }
            if (StringUtils.isBlank(response.getOpenid()))
            {
                throw new ServiceException("未获取到有效的openid");
            }
            return response;
        }
        catch (JsonProcessingException e)
        {
            log.error("解析微信接口响应失败: {}", raw, e);
            throw new ServiceException("解析微信接口响应失败");
        }
    }
}


