package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.system.utils.ActivityStatusUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 成长活动实体
 *
 * @author GPT
 */
public class HxActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long activityId;

    /** 活动名称 */
    @Excel(name = "活动名称")
    private String title;

    /** 活动摘要 */
    @Excel(name = "摘要")
    private String summary;

    /** 封面地址 */
    private String coverUrl;

    /** 活动详情 */
    private String description;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 活动地点 */
    @Excel(name = "地点")
    private String location;

    /** 人数上限 */
    @Excel(name = "人数上限")
    private Integer maxParticipants;

    /** 状态（0草稿 1进行中 2结束） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=进行中,2=结束")
    private String status;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 发布人ID */
    private Long publishUserId;

    /** 发布人名称 */
    private String publishUserName;

    /** 报名人数 */
    @Excel(name = "报名人数")
    private Integer participantCount;

    /** 删除标志 */
    private String delFlag;

    /** 可见岗位ID列表，逗号分隔，为空表示所有岗位可见 */
    private String visiblePostIds;

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public Integer getMaxParticipants()
    {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants)
    {
        this.maxParticipants = maxParticipants;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    
    /**
     * 获取活动实际状态（根据时间实时计算）
     * 
     * @return 活动实际状态
     */
    public String getActualStatus()
    {
        return ActivityStatusUtils.calculateStatus(getStartTime(), getEndTime());
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

    public Integer getParticipantCount()
    {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount)
    {
        this.participantCount = participantCount;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getVisiblePostIds()
    {
        return visiblePostIds;
    }

    public void setVisiblePostIds(String visiblePostIds)
    {
        this.visiblePostIds = visiblePostIds;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("activityId", getActivityId())
            .append("title", getTitle())
            .append("summary", getSummary())
            .append("coverUrl", getCoverUrl())
            .append("description", getDescription())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("location", getLocation())
            .append("maxParticipants", getMaxParticipants())
            .append("status", getStatus())
            .append("publishTime", getPublishTime())
            .append("publishUserId", getPublishUserId())
            .append("publishUserName", getPublishUserName())
            .append("participantCount", getParticipantCount())
            .append("delFlag", getDelFlag())
            .append("visiblePostIds", getVisiblePostIds())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
