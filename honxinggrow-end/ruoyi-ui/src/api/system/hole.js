import request from '@/utils/request'

// 查询心灵树洞列表
export function listHole(query) {
  return request({
    url: '/system/hole/list',
    method: 'get',
    params: query
  })
}

// 查询心灵树洞详细
export function getHole(treeHoleId) {
  return request({
    url: '/system/hole/' + treeHoleId,
    method: 'get'
  })
}

// 新增心灵树洞
export function addHole(data) {
  return request({
    url: '/system/hole',
    method: 'post',
    data: data
  })
}

// 修改心灵树洞
export function updateHole(data) {
  return request({
    url: '/system/hole',
    method: 'put',
    data: data
  })
}

// 删除心灵树洞
export function delHole(treeHoleId) {
  return request({
    url: '/system/hole/' + treeHoleId,
    method: 'delete'
  })
}
