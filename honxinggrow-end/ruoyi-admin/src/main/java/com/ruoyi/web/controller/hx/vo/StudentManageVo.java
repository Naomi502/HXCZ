package com.ruoyi.web.controller.hx.vo;

import java.util.Date;

/**
 * 学生管理视图对象
 * 用于整合学生的完整信息，包括基本信息、积分信息、微信绑定信息等
 *
 * @author GPT
 */
public class StudentManageVo {
    
    /** 用户ID */
    private Long userId;
    
    /** 部门ID */
    private Long deptId;
    
    /** 部门名称 */
    private String deptName;
    
    /** 用户账号 */
    private String userName;
    
    /** 用户昵称 */
    private String nickName;
    
    /** 用户性别 */
    private String sex;
    
    /** 账号状态（0正常 1停用） */
    private String status;
    
    /** 用户邮箱 */
    private String email;
    
    /** 手机号码 */
    private String phonenumber;
    
    /** 最后登录IP */
    private String loginIp;
    
    /** 最后登录时间 */
    private Date loginDate;
    
    /** 密码最后更新时间 */
    private Date pwdUpdateDate;
    
    /** 创建时间 */
    private Date createTime;
    
    /** 当前积分 */
    private Integer pointsTotal;
    
    /** 最近故事积分时间 */
    private Date lastStoryPointTime;
    
    /** 最近活动积分时间 */
    private Date lastActivityPointTime;
    
    /** 是否已绑定微信 */
    private Boolean wechatBound;
    
    /** 微信OpenID */
    private String openid;
    
    /** 微信绑定时间 */
    private Date bindTime;
    
    /** 最近快捷登录时间 */
    private Date lastWechatLoginTime;
    
    // getter and setter methods
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getDeptId() {
        return deptId;
    }
    
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
    
    public String getDeptName() {
        return deptName;
    }
    
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getNickName() {
        return nickName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhonenumber() {
        return phonenumber;
    }
    
    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
    
    public String getLoginIp() {
        return loginIp;
    }
    
    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }
    
    public Date getLoginDate() {
        return loginDate;
    }
    
    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }
    
    public Date getPwdUpdateDate() {
        return pwdUpdateDate;
    }
    
    public void setPwdUpdateDate(Date pwdUpdateDate) {
        this.pwdUpdateDate = pwdUpdateDate;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Integer getPointsTotal() {
        return pointsTotal;
    }
    
    public void setPointsTotal(Integer pointsTotal) {
        this.pointsTotal = pointsTotal;
    }
    
    public Date getLastStoryPointTime() {
        return lastStoryPointTime;
    }
    
    public void setLastStoryPointTime(Date lastStoryPointTime) {
        this.lastStoryPointTime = lastStoryPointTime;
    }
    
    public Date getLastActivityPointTime() {
        return lastActivityPointTime;
    }
    
    public void setLastActivityPointTime(Date lastActivityPointTime) {
        this.lastActivityPointTime = lastActivityPointTime;
    }
    
    public Boolean getWechatBound() {
        return wechatBound;
    }
    
    public void setWechatBound(Boolean wechatBound) {
        this.wechatBound = wechatBound;
    }
    
    public String getOpenid() {
        return openid;
    }
    
    public void setOpenid(String openid) {
        this.openid = openid;
    }
    
    public Date getBindTime() {
        return bindTime;
    }
    
    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }
    
    public Date getLastWechatLoginTime() {
        return lastWechatLoginTime;
    }
    
    public void setLastWechatLoginTime(Date lastWechatLoginTime) {
        this.lastWechatLoginTime = lastWechatLoginTime;
    }
}