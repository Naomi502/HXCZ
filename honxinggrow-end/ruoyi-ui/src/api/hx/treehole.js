import request from '@/utils/request'

export function listTreeHole(query) {
  return request({
    url: '/hx/treehole/list',
    method: 'get',
    params: query
  })
}

export function getTreeHole(treeHoleId) {
  return request({
    url: `/hx/treehole/${treeHoleId}`,
    method: 'get'
  })
}

export function delTreeHole(treeHoleId) {
  return request({
    url: `/hx/treehole/${treeHoleId}`,
    method: 'delete'
  })
}

export function delTreeHoleByIds(ids) {
  return request({
    url: '/hx/treehole',
    method: 'delete',
    data: ids
  })
}
