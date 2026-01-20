package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.HxUserMessage;

/**
 * 用户消息Mapper接口
 * 
 * @author GPT
 */
public interface HxUserMessageMapper 
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
     * 修改用户消息
     * 
     * @param hxUserMessage 用户消息
     * @return 结果
     */
    public int updateHxUserMessage(HxUserMessage hxUserMessage);

    /**
     * 删除用户消息
     * 
     * @param messageId 用户消息主键
     * @return 结果
     */
    public int deleteHxUserMessageByMessageId(Long messageId);

    /**
     * 批量删除用户消息
     * 
     * @param messageIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHxUserMessageByMessageIds(Long[] messageIds);

    /**
     * 获取未读消息数量
     * 
     * @param userId 用户ID
     * @return 数量
     */
    public int countUnreadMessages(Long userId);

    /**
     * 删除指定用户的所有消息
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteHxUserMessageByUserId(Long userId);
}
