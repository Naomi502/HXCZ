package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 心灵树洞对象 hx_tree_hole
 * 
 * @author ruoyi
 * @date 2026-01-15
 */
public class HxTreeHole extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long treeHoleId;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 用户昵称 */
    @Excel(name = "用户昵称")
    private String nickName;

    /** 部门名称 */
    @Excel(name = "部门名称")
    private String deptName;

    /** 树洞内容 */
    @Excel(name = "树洞内容")
    private String content;

    /** 处理标记（0：未处理，1：已处理） */
    @Excel(name = "处理标记", readConverterExp = "0=未处理,1=已处理")
    private String status;

    /** 删除标志（0：正常，1：删除） */
    private String delFlag;

    public void setTreeHoleId(Long treeHoleId) 
    {
        this.treeHoleId = treeHoleId;
    }

    public Long getTreeHoleId() 
    {
        return treeHoleId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setNickName(String nickName) 
    {
        this.nickName = nickName;
    }

    public String getNickName() 
    {
        return nickName;
    }

    public void setDeptName(String deptName) 
    {
        this.deptName = deptName;
    }

    public String getDeptName() 
    {
        return deptName;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("treeHoleId", getTreeHoleId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("deptName", getDeptName())
            .append("content", getContent())
            .append("createTime", getCreateTime())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
