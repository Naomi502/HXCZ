package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SnowflakeIdWorker;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.HxQa;
import com.ruoyi.system.mapper.HxQaMapper;
import com.ruoyi.system.service.IHxQaService;

/**
 * 问答管理Service业务层处理
 *
 * @author Trae IDE
 */
@Service
public class HxQaServiceImpl implements IHxQaService
{
    private static final String QA_STATUS_PUBLISHED = "1";
    private static final String DEL_FLAG_NORMAL = "0";

    @Autowired
    private HxQaMapper hxQaMapper;

    @Override
    public HxQa selectHxQaById(Long qaId)
    {
        return hxQaMapper.selectHxQaById(qaId);
    }

    @Override
    public List<HxQa> selectHxQaList(HxQa qa)
    {
        return hxQaMapper.selectHxQaList(qa);
    }

    @Override
    public int insertHxQa(HxQa qa)
    {
        Date now = DateUtils.getNowDate();
        // 使用雪花算法生成问答ID
        if (qa.getQaId() == null) {
            qa.setQaId(SnowflakeIdWorker.getId());
        }
        qa.setCreateTime(now);
        if (qa.getDelFlag() == null)
        {
            qa.setDelFlag(DEL_FLAG_NORMAL);
        }
        if (qa.getViewCount() == null)
        {
            qa.setViewCount(0);
        }
        if (StringUtils.equals(QA_STATUS_PUBLISHED, qa.getStatus()) && qa.getPublishTime() == null)
        {
            qa.setPublishTime(now);
        }
        return hxQaMapper.insertHxQa(qa);
    }

    @Override
    public int updateHxQa(HxQa qa)
    {
        Date now = DateUtils.getNowDate();
        qa.setUpdateTime(now);
        if (qa.getDelFlag() == null)
        {
            qa.setDelFlag(DEL_FLAG_NORMAL);
        }
        if (StringUtils.equals(QA_STATUS_PUBLISHED, qa.getStatus()) && qa.getPublishTime() == null)
        {
            qa.setPublishTime(now);
        }
        return hxQaMapper.updateHxQa(qa);
    }

    @Override
    public int deleteHxQaByIds(Long[] qaIds)
    {
        return hxQaMapper.deleteHxQaByIds(qaIds);
    }

    @Override
    public int deleteHxQaById(Long qaId)
    {
        return hxQaMapper.deleteHxQaById(qaId);
    }

    @Override
    public List<HxQa> selectPublishedQas()
    {
        return hxQaMapper.selectPublishedQas();
    }

    @Override
    public HxQa readQa(Long qaId, Long userId)
    {
        HxQa qa = hxQaMapper.selectHxQaById(qaId);
        if (qa == null || "2".equals(qa.getDelFlag()))
        {
            throw new ServiceException("问答不存在或已删除");
        }
        if (!StringUtils.equals(QA_STATUS_PUBLISHED, qa.getStatus()))
        {
            throw new ServiceException("问答未发布，暂不可阅读");
        }
        hxQaMapper.increaseViewCount(qaId);
        return qa;
    }
}