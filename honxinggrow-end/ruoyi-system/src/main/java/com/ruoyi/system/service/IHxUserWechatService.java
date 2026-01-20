package com.ruoyi.system.service;

import com.ruoyi.system.domain.HxUserWechat;

/**
 * 用户微信绑定服务
 *
 * @author GPT
 */
public interface IHxUserWechatService
{
    /**
     * 查询指定用户的绑定信息
     */
    HxUserWechat selectByUserId(Long userId);

    /**
     * 根据 openid 查询绑定信息
     */
    HxUserWechat selectByOpenid(String openid);

    /**
     * 绑定/更新微信 openid
     *
     * @param userId     用户ID
     * @param openid     微信 openid
     * @param sessionKey sessionKey
     */
    void bindUserWechat(Long userId, String openid, String sessionKey);

    /**
     * 更新快捷登录时间
     *
     * @param bindId 绑定记录ID
     */
    void updateLoginTime(Long bindId);

    /**
     * 是否已绑定
     */
    boolean isBound(Long userId);
    
    /**
     * 解绑用户微信
     * @param userId 用户ID
     */
    void unbindUserWechat(Long userId);
}

