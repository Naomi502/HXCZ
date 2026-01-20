package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 活动参与记录
 *
 * @author GPT
 */
public class HxActivityParticipant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long participantId;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long userId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String userName;

    /** 报名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;

    /** 状态（1已报名，2取消） */
    @Excel(name = "状态", readConverterExp = "1=已报名,2=取消")
    private String status;

    /** 证明图片地址 */
    private String proofImageUrl;

    /** 证明状态（0未提交 1待审核 2已通过 3已驳回） */
    private String proofStatus;

    /** 证明提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date proofSubmitTime;

    /** 审核人 */
    private String proofAuditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date proofAuditTime;

    /** 审核意见 */
    private String proofAuditRemark;

    /** 是否已发放积分（0否 1是） */
    private String rewardGranted;

    /** 发放积分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date rewardTime;

    /** 发放积分数 */
    private Integer rewardPoints;

    /** 学生部门名称（仅展示用，不入库） */
    private String deptName;

    /** 学生岗位名称列表（逗号分隔，仅展示用，不入库） */
    private String postNames;

    public Long getParticipantId()
    {
        return participantId;
    }

    public void setParticipantId(Long participantId)
    {
        this.participantId = participantId;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
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

    public Date getJoinTime()
    {
        return joinTime;
    }

    public void setJoinTime(Date joinTime)
    {
        this.joinTime = joinTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getProofImageUrl()
    {
        return proofImageUrl;
    }

    public void setProofImageUrl(String proofImageUrl)
    {
        this.proofImageUrl = proofImageUrl;
    }

    public String getProofStatus()
    {
        return proofStatus;
    }

    public void setProofStatus(String proofStatus)
    {
        this.proofStatus = proofStatus;
    }

    public Date getProofSubmitTime()
    {
        return proofSubmitTime;
    }

    public void setProofSubmitTime(Date proofSubmitTime)
    {
        this.proofSubmitTime = proofSubmitTime;
    }

    public String getProofAuditBy()
    {
        return proofAuditBy;
    }

    public void setProofAuditBy(String proofAuditBy)
    {
        this.proofAuditBy = proofAuditBy;
    }

    public Date getProofAuditTime()
    {
        return proofAuditTime;
    }

    public void setProofAuditTime(Date proofAuditTime)
    {
        this.proofAuditTime = proofAuditTime;
    }

    public String getProofAuditRemark()
    {
        return proofAuditRemark;
    }

    public void setProofAuditRemark(String proofAuditRemark)
    {
        this.proofAuditRemark = proofAuditRemark;
    }

    public String getRewardGranted()
    {
        return rewardGranted;
    }

    public void setRewardGranted(String rewardGranted)
    {
        this.rewardGranted = rewardGranted;
    }

    public Date getRewardTime()
    {
        return rewardTime;
    }

    public void setRewardTime(Date rewardTime)
    {
        this.rewardTime = rewardTime;
    }

    public Integer getRewardPoints()
    {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints)
    {
        this.rewardPoints = rewardPoints;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getPostNames()
    {
        return postNames;
    }

    public void setPostNames(String postNames)
    {
        this.postNames = postNames;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("participantId", getParticipantId())
            .append("activityId", getActivityId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("joinTime", getJoinTime())
            .append("status", getStatus())
            .append("proofImageUrl", getProofImageUrl())
            .append("proofStatus", getProofStatus())
            .append("proofSubmitTime", getProofSubmitTime())
            .append("proofAuditBy", getProofAuditBy())
            .append("proofAuditTime", getProofAuditTime())
            .append("proofAuditRemark", getProofAuditRemark())
            .append("rewardGranted", getRewardGranted())
            .append("rewardTime", getRewardTime())
            .append("rewardPoints", getRewardPoints())
            .append("deptName", getDeptName())
            .append("postNames", getPostNames())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
