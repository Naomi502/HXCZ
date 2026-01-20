package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 故事点赞记录实体
 *
 * @author GPT
 */
public class HxStoryLike
{
    private static final long serialVersionUID = 1L;

    /** 点赞ID */
    private Long likeId;

    /** 故事ID */
    private Long storyId;

    /** 用户ID */
    private Long userId;

    /** 用户名称 */
    private String userName;

    /** 点赞时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getLikeId()
    {
        return likeId;
    }

    public void setLikeId(Long likeId)
    {
        this.likeId = likeId;
    }

    public Long getStoryId()
    {
        return storyId;
    }

    public void setStoryId(Long storyId)
    {
        this.storyId = storyId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("likeId", getLikeId())
            .append("storyId", getStoryId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("createTime", getCreateTime())
            .toString();
    }
}

