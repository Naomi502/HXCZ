package com.ruoyi.web.controller.app.request;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;

/**
 * 活动证明提交请求
 *
 * @author GPT
 */
public class ActivityProofSubmitRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "请上传活动参加证明图片")
    private String imageUrl;

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
}

