package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 学生积分流水
 *
 * @author GPT
 */
public class HxPointLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long logId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long userId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String userName;

    /** 业务类型 */
    @Excel(name = "业务类型")
    private String bizType;

    /** 业务主键 */
    @Excel(name = "业务ID")
    private Long bizId;

    /** 积分变动值 */
    @Excel(name = "积分变化")
    private Integer points;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    /** 事件时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
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

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    @Override
    public String getRemark()
    {
        return remark;
    }

    @Override
    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Date getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Date eventTime)
    {
        this.eventTime = eventTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("bizType", getBizType())
            .append("bizId", getBizId())
            .append("points", getPoints())
            .append("remark", getRemark())
            .append("eventTime", getEventTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
