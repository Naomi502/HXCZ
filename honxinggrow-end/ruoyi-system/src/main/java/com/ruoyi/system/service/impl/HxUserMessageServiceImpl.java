package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.HxUserMessageMapper;
import com.ruoyi.system.domain.HxUserMessage;
import com.ruoyi.system.service.IHxUserMessageService;

/**
 * 用户消息Service业务层处理
 * 
 * @author GPT
 */
@Service
public class HxUserMessageServiceImpl implements IHxUserMessageService 
{
    @Autowired
    private HxUserMessageMapper hxUserMessageMapper;

    /**
     * 查询用户消息
     * 
     * @param messageId 用户消息主键
     * @return 用户消息
     */
    @Override
    public HxUserMessage selectHxUserMessageByMessageId(Long messageId)
    {
        return hxUserMessageMapper.selectHxUserMessageByMessageId(messageId);
    }

    /**
     * 查询用户消息列表
     * 
     * @param hxUserMessage 用户消息
     * @return 用户消息
     */
    @Override
    public List<HxUserMessage> selectHxUserMessageList(HxUserMessage hxUserMessage)
    {
        return hxUserMessageMapper.selectHxUserMessageList(hxUserMessage);
    }

    /**
     * 新增用户消息
     * 
     * @param hxUserMessage 用户消息
     * @return 结果
     */
    @Override
    public int insertHxUserMessage(HxUserMessage hxUserMessage)
    {
        hxUserMessage.setCreateTime(DateUtils.getNowDate());
        if (hxUserMessage.getIsRead() == null) {
            hxUserMessage.setIsRead("0");
        }
        return hxUserMessageMapper.insertHxUserMessage(hxUserMessage);
    }

    @Override
    public void sendMessage(Long userId, String title, String content, String type) {
        HxUserMessage message = new HxUserMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        insertHxUserMessage(message);
    }

    /**
     * 修改用户消息
     * 
     * @param hxUserMessage 用户消息
     * @return 结果
     */
    @Override
    public int updateHxUserMessage(HxUserMessage hxUserMessage)
    {
        return hxUserMessageMapper.updateHxUserMessage(hxUserMessage);
    }

    @Override
    public int readMessage(Long messageId) {
        HxUserMessage message = new HxUserMessage();
        message.setMessageId(messageId);
        message.setIsRead("1");
        return hxUserMessageMapper.updateHxUserMessage(message);
    }

    /**
     * 批量删除用户消息
     * 
     * @param messageIds 需要删除的用户消息主键
     * @return 结果
     */
    @Override
    public int deleteHxUserMessageByMessageIds(Long[] messageIds)
    {
        return hxUserMessageMapper.deleteHxUserMessageByMessageIds(messageIds);
    }

    /**
     * 删除用户消息信息
     * 
     * @param messageId 用户消息主键
     * @return 结果
     */
    @Override
    public int deleteHxUserMessageByMessageId(Long messageId)
    {
        return hxUserMessageMapper.deleteHxUserMessageByMessageId(messageId);
    }

    @Override
    public int countUnreadMessages(Long userId) {
        return hxUserMessageMapper.countUnreadMessages(userId);
    }

    @Override
    public int clearAllMessages(Long userId) {
        return hxUserMessageMapper.deleteHxUserMessageByUserId(userId);
    }
}
