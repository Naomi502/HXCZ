package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 成长故事实体
 *
 * @author GPT
 */
public class HxStory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long storyId;

    /** 标题 */
    @Excel(name = "故事标题")
    private String title;

    /** 摘要 */
    @Excel(name = "摘要")
    private String summary;

    /** 封面地址 */
    private String coverUrl;

    /** 正文内容 */
    private String content;

    /** 状态（0草稿 1发布 2下架） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=发布,2=下架")
    private String status;

    /** 排序值 */
    @Excel(name = "排序")
    private Integer sort;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 发布人ID */
    private Long publishUserId;

    /** 发布人名称 */
    private String publishUserName;

    /** 阅读次数 */
    @Excel(name = "阅读次数")
    private Integer viewCount;

    /** 点赞数 */
    @Excel(name = "点赞数")
    private Integer likeCount;

    /** 删除标志 */
    private String delFlag;

    public Long getStoryId()
    {
        return storyId;
    }

    public void setStoryId(Long storyId)
    {
        this.storyId = storyId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public Date getPublishTime()
    {
        return publishTime;
    }

    public void setPublishTime(Date publishTime)
    {
        this.publishTime = publishTime;
    }

    public Long getPublishUserId()
    {
        return publishUserId;
    }

    public void setPublishUserId(Long publishUserId)
    {
        this.publishUserId = publishUserId;
    }

    public String getPublishUserName()
    {
        return publishUserName;
    }

    public void setPublishUserName(String publishUserName)
    {
        this.publishUserName = publishUserName;
    }

    public Integer getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(Integer viewCount)
    {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount()
    {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount)
    {
        this.likeCount = likeCount;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("storyId", getStoryId())
            .append("title", getTitle())
            .append("summary", getSummary())
            .append("coverUrl", getCoverUrl())
            .append("content", getContent())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("publishTime", getPublishTime())
            .append("publishUserId", getPublishUserId())
            .append("publishUserName", getPublishUserName())
            .append("viewCount", getViewCount())
            .append("likeCount", getLikeCount())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
