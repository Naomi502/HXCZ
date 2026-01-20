package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.HxTreeHoleMapper;
import com.ruoyi.system.domain.HxTreeHole;
import com.ruoyi.system.service.IHxTreeHoleService;

/**
 * 心灵树洞Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-15
 */
@Service
public class HxTreeHoleServiceImpl implements IHxTreeHoleService 
{
    @Autowired
    private HxTreeHoleMapper hxTreeHoleMapper;

    /**
     * 查询心灵树洞详细信息
     * 自动关联用户表和部门表，获取用户昵称和部门名称
     * 
     * @param treeHoleId 心灵树洞主键
     * @return 心灵树洞信息（包含用户昵称和部门名称）
     */
    @Override
    public HxTreeHole selectHxTreeHoleByTreeHoleId(Long treeHoleId)
    {
        return hxTreeHoleMapper.selectHxTreeHoleByTreeHoleId(treeHoleId);
    }

    /**
     * 查询心灵树洞列表
     * 自动关联用户表和部门表，获取用户昵称和部门名称
     * 默认只查询未删除的记录（del_flag = '0'），Mapper XML会自动过滤
     * 
     * @param hxTreeHole 查询条件
     * @return 心灵树洞列表（包含用户昵称和部门名称）
     */
    @Override
    public List<HxTreeHole> selectHxTreeHoleList(HxTreeHole hxTreeHole)
    {
        return hxTreeHoleMapper.selectHxTreeHoleList(hxTreeHole);
    }

    /**
     * 新增心灵树洞
     * 
     * @param hxTreeHole 心灵树洞信息
     * @return 结果
     */
    @Override
    public int insertHxTreeHole(HxTreeHole hxTreeHole)
    {
        hxTreeHole.setCreateTime(DateUtils.getNowDate());
        // 使用雪花算法生成树洞ID
        if (hxTreeHole.getTreeHoleId() == null) {
            hxTreeHole.setTreeHoleId(SnowflakeIdWorker.getId());
        }
        // 设置默认值
        if (hxTreeHole.getStatus() == null || hxTreeHole.getStatus().isEmpty())
        {
            hxTreeHole.setStatus("0"); // 默认未处理
        }
        if (hxTreeHole.getDelFlag() == null || hxTreeHole.getDelFlag().isEmpty())
        {
            hxTreeHole.setDelFlag("0"); // 默认正常
        }
        return hxTreeHoleMapper.insertHxTreeHole(hxTreeHole);
    }

    /**
     * 修改心灵树洞信息
     * 
     * @param hxTreeHole 心灵树洞信息
     * @return 结果
     */
    @Override
    public int updateHxTreeHole(HxTreeHole hxTreeHole)
    {
        return hxTreeHoleMapper.updateHxTreeHole(hxTreeHole);
    }

    /**
     * 批量逻辑删除心灵树洞
     * 使用逻辑删除，将del_flag设置为'1'
     * 
     * @param treeHoleIds 需要删除的心灵树洞主键数组
     * @return 结果
     */
    @Override
    public int deleteHxTreeHoleByTreeHoleIds(Long[] treeHoleIds)
    {
        return hxTreeHoleMapper.deleteHxTreeHoleByTreeHoleIds(treeHoleIds);
    }

    /**
     * 逻辑删除心灵树洞信息
     * 使用逻辑删除，将del_flag设置为'1'
     * 
     * @param treeHoleId 心灵树洞主键
     * @return 结果
     */
    @Override
    public int deleteHxTreeHoleByTreeHoleId(Long treeHoleId)
    {
        return hxTreeHoleMapper.deleteHxTreeHoleByTreeHoleId(treeHoleId);
    }
}
