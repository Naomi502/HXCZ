package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问答实体
 *
 * @author Trae IDE
 */
public class HxQa extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long qaId;

    /** 问题标题 */
    @Excel(name = "问题标题")
    private String title;

    /** 解答内容 */
    private String content;

    /** 视频链接 */
    private String videoUrl;

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

    /** 删除标志 */
    private String delFlag;

    public Long getQaId()
    {
        return qaId;
    }

    public void setQaId(Long qaId)
    {
        this.qaId = qaId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
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
            .append("qaId", getQaId())
            .append("title", getTitle())
            .append("content", getContent())
            .append("videoUrl", getVideoUrl())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("publishTime", getPublishTime())
            .append("publishUserId", getPublishUserId())
            .append("publishUserName", getPublishUserName())
            .append("viewCount", getViewCount())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}