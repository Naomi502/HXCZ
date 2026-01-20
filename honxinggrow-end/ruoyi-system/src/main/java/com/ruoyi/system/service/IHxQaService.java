package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.HxQa;

/**
 * 问答管理Service接口
 *
 * @author Trae IDE
 */
public interface IHxQaService
{
    /**
     * 根据ID查询问答
     * @param qaId 问答ID
     * @return 问答对象
     */
    HxQa selectHxQaById(Long qaId);

    /**
     * 查询问答列表
     * @param qa 问答对象
     * @return 问答列表
     */
    List<HxQa> selectHxQaList(HxQa qa);

    /**
     * 新增问答
     * @param qa 问答对象
     * @return 结果
     */
    int insertHxQa(HxQa qa);

    /**
     * 修改问答
     * @param qa 问答对象
     * @return 结果
     */
    int updateHxQa(HxQa qa);

    /**
     * 删除问答
     * @param qaId 问答ID
     * @return 结果
     */
    int deleteHxQaById(Long qaId);

    /**
     * 批量删除问答
     * @param qaIds 问答ID数组
     * @return 结果
     */
    int deleteHxQaByIds(Long[] qaIds);

    /**
     * 查询已发布的问答列表
     * @return 问答列表
     */
    List<HxQa> selectPublishedQas();

    /**
     * 小程序阅读问答
     * @param qaId 问答ID
     * @param userId 用户ID
     * @return 问答详情
     */
    HxQa readQa(Long qaId, Long userId);
}