package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 学生积分账户
 *
 * @author GPT
 */
public class HxStudentProfile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long profileId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long userId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String userName;

    /** 当前积分 */
    @Excel(name = "当前积分")
    private Integer pointsTotal;

    /** 最近故事积分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近故事积分时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastStoryPointTime;

    /** 最近活动积分时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近活动积分时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastActivityPointTime;

    /** 是否已绑定微信 */
    private Boolean wechatBound;
    
    /** 角色筛选 */
    private String roleKey;
    
    /** 部门模糊查找 */
    private String deptName;

    /** 手机号码 */
    @Excel(name = "手机号码")
    private String phonenumber;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    @Excel(name = "故事数")
    private Integer storyCount;

    @Excel(name = "活动数")
    private Integer activityCount;


    public Long getProfileId()
    {
        return profileId;
    }

    public void setProfileId(Long profileId)
    {
        this.profileId = profileId;
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

    public Integer getPointsTotal()
    {
        return pointsTotal;
    }

    public void setPointsTotal(Integer pointsTotal)
    {
        this.pointsTotal = pointsTotal;
    }

    public Date getLastStoryPointTime()
    {
        return lastStoryPointTime;
    }

    public void setLastStoryPointTime(Date lastStoryPointTime)
    {
        this.lastStoryPointTime = lastStoryPointTime;
    }

    public Date getLastActivityPointTime()
    {
        return lastActivityPointTime;
    }

    public void setLastActivityPointTime(Date lastActivityPointTime)
    {
        this.lastActivityPointTime = lastActivityPointTime;
    }

    public Boolean getWechatBound()
    {
        return wechatBound;
    }

    public void setWechatBound(Boolean wechatBound)
    {
        this.wechatBound = wechatBound;
    }

    public String getRoleKey()
    {
        return roleKey;
    }

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getStoryCount() {
        return storyCount;
    }

    public void setStoryCount(Integer storyCount) {
        this.storyCount = storyCount;
    }

    public Integer getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(Integer activityCount) {
        this.activityCount = activityCount;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("profileId", getProfileId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("pointsTotal", getPointsTotal())
            .append("lastStoryPointTime", getLastStoryPointTime())
            .append("lastActivityPointTime", getLastActivityPointTime())
            .append("wechatBound", getWechatBound())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
