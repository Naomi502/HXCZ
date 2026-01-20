package com.ruoyi.system.mapper;

import java.util.Date;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.HxUserWechat;

/**
 * 用户微信绑定 Mapper
 *
 * @author GPT
 */
public interface HxUserWechatMapper
{
    HxUserWechat selectByUserId(@Param("userId") Long userId);

    HxUserWechat selectByOpenid(@Param("openid") String openid);

    int insertHxUserWechat(HxUserWechat record);

    int updateHxUserWechat(HxUserWechat record);

    int updateLoginTime(@Param("bindId") Long bindId, @Param("lastLoginTime") Date lastLoginTime);
    
    int deleteByUserId(@Param("userId") Long userId);
}


