package com.ruoyi.web.controller.hx.vo;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 * 活动证明审核请求
 *
 * @author GPT
 */
public class ActivityProofAuditRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    @NotNull(message = "请选择审核结果")
    private Boolean approved;

    private String remark;

    public Boolean getApproved()
    {
        return approved;
    }

    public void setApproved(Boolean approved)
    {
        this.approved = approved;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}

