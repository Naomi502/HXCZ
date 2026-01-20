package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.HxUserMessage;

/**
 * 用户消息Service接口
 * 
 * @author GPT
 */
public interface IHxUserMessageService 
{
    /**
     * 查询用户消息
     * 
     * @param messageId 用户消息主键
     * @return 用户消息
     */
    public HxUserMessage selectHxUserMessageByMessageId(Long messageId);

    /**
     * 查询用户消息列表
     * 
     * @param hxUserMessage 用户消息
     * @return 用户消息集合
     */
    public List<HxUserMessage> selectHxUserMessageList(HxUserMessage hxUserMessage);

    /**
     * 新增用户消息
     * 
     * @param hxUserMessage 用户消息
     * @return 结果
     */
    public int insertHxUserMessage(HxUserMessage hxUserMessage);

    /**
     * 发送系统通知
     * 
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param type 类型
     */
    public void sendMessage(Long userId, String title, String content, String type);

    /**
     * 修改用户消息
     * 
     * @param hxUserMessage 用户消息
     * @return 结果
     */
    public int updateHxUserMessage(HxUserMessage hxUserMessage);

    /**
     * 标记消息已读
     * 
     * @param messageId 消息ID
     * @return 结果
     */
    public int readMessage(Long messageId);

    /**
     * 批量删除用户消息
     * 
     * @param messageIds 需要删除的用户消息主键集合
     * @return 结果
     */
    public int deleteHxUserMessageByMessageIds(Long[] messageIds);

    /**
     * 删除用户消息信息
     * 
     * @param messageId 用户消息主键
     * @return 结果
     */
    public int deleteHxUserMessageByMessageId(Long messageId);

    /**
     * 获取未读消息数
     * 
     * @param userId 用户ID
     * @return 数量
     */
    public int countUnreadMessages(Long userId);

    /**
     * 清空指定用户的所有消息
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int clearAllMessages(Long userId);
}
