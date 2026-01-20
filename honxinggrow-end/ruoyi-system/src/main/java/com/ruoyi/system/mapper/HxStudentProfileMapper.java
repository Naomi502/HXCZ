package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.HxStudentProfile;

/**
 * 学生积分账户Mapper接口
 *
 * @author GPT
 */
public interface HxStudentProfileMapper
{
    HxStudentProfile selectById(Long profileId);

    HxStudentProfile selectByUserId(@Param("userId") Long userId);

    List<HxStudentProfile> selectHxStudentProfileList(HxStudentProfile profile);

    int insertHxStudentProfile(HxStudentProfile profile);

    int updateHxStudentProfile(HxStudentProfile profile);

    int deleteById(Long profileId);

    int deleteByIds(Long[] profileIds);

    /**
     * 查询学生列表
     * 
     * @param profile 学生信息
     * @return 学生列表
     */
    List<java.util.Map<String, Object>> selectStudentList(HxStudentProfile profile);

    int addPoints(@Param("userId") Long userId, @Param("userName") String userName,
            @Param("points") Integer points,
            @Param("storyPointTime") Date storyPointTime,
            @Param("activityPointTime") Date activityPointTime);
}
