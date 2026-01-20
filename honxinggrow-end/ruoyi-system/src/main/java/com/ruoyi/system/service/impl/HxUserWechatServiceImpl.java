package com.ruoyi.system.service.impl;

import java.util.Date;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.HxUserWechat;
import com.ruoyi.system.mapper.HxUserWechatMapper;
import com.ruoyi.system.service.IHxUserWechatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户微信绑定服务实现
 *
 * @author GPT
 */
@Service
public class HxUserWechatServiceImpl implements IHxUserWechatService
{
    @Autowired
    private HxUserWechatMapper hxUserWechatMapper;

    @Override
    public HxUserWechat selectByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        return hxUserWechatMapper.selectByUserId(userId);
    }

    @Override
    public HxUserWechat selectByOpenid(String openid)
    {
        if (StringUtils.isBlank(openid))
        {
            return null;
        }
        return hxUserWechatMapper.selectByOpenid(openid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindUserWechat(Long userId, String openid, String sessionKey)
    {
        if (userId == null)
        {
            throw new ServiceException("用户ID不能为空");
        }
        if (StringUtils.isBlank(openid))
        {
            throw new ServiceException("openid不能为空");
        }
        HxUserWechat existed = selectByOpenid(openid);
        if (existed != null && !userId.equals(existed.getUserId()))
        {
            throw new ServiceException("该微信已绑定其他校园账号，请联系管理员处理");
        }
        Date now = new Date();
        HxUserWechat record = selectByUserId(userId);
        if (record == null)
        {
            record = new HxUserWechat();
            record.setUserId(userId);
            record.setOpenid(openid);
            record.setSessionKey(sessionKey);
            record.setBindTime(now);
            record.setCreateTime(now);
            hxUserWechatMapper.insertHxUserWechat(record);
        }
        else
        {
            record.setOpenid(openid);
            record.setSessionKey(sessionKey);
            record.setBindTime(now);
            record.setUpdateTime(now);
            hxUserWechatMapper.updateHxUserWechat(record);
        }
    }

    @Override
    public void updateLoginTime(Long bindId)
    {
        if (bindId == null)
        {
            return;
        }
        hxUserWechatMapper.updateLoginTime(bindId, new Date());
    }

    @Override
    public boolean isBound(Long userId)
    {
        return selectByUserId(userId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindUserWechat(Long userId)
    {
        if (userId == null)
        {
            throw new ServiceException("用户ID不能为空");
        }
        hxUserWechatMapper.deleteByUserId(userId);
    }
}