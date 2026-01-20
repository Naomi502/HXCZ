package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.HxTreeHole;

/**
 * 心灵树洞Mapper接口
 * 所有查询操作都会自动关联sys_user和sys_dept表，获取用户昵称和部门名称
 * 
 * @author ruoyi
 * @date 2026-01-15
 */
public interface HxTreeHoleMapper 
{
    /**
     * 查询心灵树洞详细信息
     * 自动关联用户表和部门表，获取用户昵称和部门名称
     * 
     * @param treeHoleId 心灵树洞主键
     * @return 心灵树洞信息（包含用户昵称和部门名称）
     */
    public HxTreeHole selectHxTreeHoleByTreeHoleId(Long treeHoleId);

    /**
     * 查询心灵树洞列表
     * 自动关联用户表和部门表，获取用户昵称(nickName)和部门名称(deptName)
     * 支持按用户ID、用户名、用户昵称、内容、状态等条件查询
     * 默认只查询未删除的记录（del_flag = '0'）
     * 
     * @param hxTreeHole 查询条件
     * @return 心灵树洞集合（包含用户昵称和部门名称）
     */
    public List<HxTreeHole> selectHxTreeHoleList(HxTreeHole hxTreeHole);

    /**
     * 新增心灵树洞
     * 
     * @param hxTreeHole 心灵树洞信息
     * @return 结果
     */
    public int insertHxTreeHole(HxTreeHole hxTreeHole);

    /**
     * 修改心灵树洞信息
     * 
     * @param hxTreeHole 心灵树洞信息
     * @return 结果
     */
    public int updateHxTreeHole(HxTreeHole hxTreeHole);

    /**
     * 逻辑删除心灵树洞
     * 将del_flag设置为'1'
     * 
     * @param treeHoleId 心灵树洞主键
     * @return 结果
     */
    public int deleteHxTreeHoleByTreeHoleId(Long treeHoleId);

    /**
     * 批量逻辑删除心灵树洞
     * 将del_flag设置为'1'
     * 
     * @param treeHoleIds 需要删除的数据主键数组
     * @return 结果
     */
    public int deleteHxTreeHoleByTreeHoleIds(Long[] treeHoleIds);
}
