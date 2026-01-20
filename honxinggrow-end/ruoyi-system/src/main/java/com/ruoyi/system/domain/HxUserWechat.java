package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户微信绑定信息
 *
 * @author GPT
 */
public class HxUserWechat extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 绑定记录ID */
    private Long bindId;

    /** 用户ID */
    private Long userId;

    /** 微信 openid */
    private String openid;

    /** 最近一次 session_key */
    private String sessionKey;

    /** 绑定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bindTime;

    /** 最近快捷登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    public Long getBindId()
    {
        return bindId;
    }

    public void setBindId(Long bindId)
    {
        this.bindId = bindId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }

    public String getSessionKey()
    {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }

    public Date getBindTime()
    {
        return bindTime;
    }

    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
    }

    public Date getLastLoginTime()
    {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime)
    {
        this.lastLoginTime = lastLoginTime;
    }
}


