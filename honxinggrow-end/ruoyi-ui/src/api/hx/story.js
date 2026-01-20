import request from '@/utils/request'

export function listStory(query) {
  return request({
    url: '/hx/story/list',
    method: 'get',
    params: query
  })
}

export function getStory(storyId) {
  return request({
    url: `/hx/story/${storyId}`,
    method: 'get'
  })
}

export function addStory(data) {
  return request({
    url: '/hx/story',
    method: 'post',
    data: data
  })
}

export function updateStory(data) {
  return request({
    url: '/hx/story',
    method: 'put',
    data: data
  })
}

export function delStory(storyId) {
  return request({
    url: `/hx/story/${storyId}`,
    method: 'delete'
  })
}

