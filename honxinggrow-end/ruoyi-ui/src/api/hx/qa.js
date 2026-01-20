import request from '@/utils/request'

export function listQa(query) {
  return request({
    url: '/hx/qa/list',
    method: 'get',
    params: query
  })
}

export function getQa(qaId) {
  return request({
    url: `/hx/qa/${qaId}`,
    method: 'get'
  })
}

export function addQa(data) {
  return request({
    url: '/hx/qa',
    method: 'post',
    data: data
  })
}

export function updateQa(data) {
  return request({
    url: '/hx/qa',
    method: 'put',
    data: data
  })
}

export function delQa(qaId) {
  return request({
    url: `/hx/qa/${qaId}`,
    method: 'delete'
  })
}

export function getAiConfig() {
  return request({
    url: '/hx/ai/config',
    method: 'get'
  })
}

export function updateAiConfig(data) {
  return request({
    url: '/hx/ai/config',
    method: 'post',
    data: data
  })
}