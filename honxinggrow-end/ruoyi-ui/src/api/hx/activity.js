import request from '@/utils/request'

export function listActivity(query) {
  return request({
    url: '/hx/activity/list',
    method: 'get',
    params: query
  })
}

export function getActivity(activityId) {
  return request({
    url: `/hx/activity/${activityId}`,
    method: 'get'
  })
}

export function addActivity(data) {
  return request({
    url: '/hx/activity',
    method: 'post',
    data: data
  })
}

export function updateActivity(data) {
  return request({
    url: '/hx/activity',
    method: 'put',
    data: data
  })
}

export function delActivity(activityId) {
  return request({
    url: `/hx/activity/${activityId}`,
    method: 'delete'
  })
}

export function listParticipants(activityId, query) {
  return request({
    url: `/hx/activity/${activityId}/participants`,
    method: 'get',
    params: query
  })
}

export function auditParticipant(participantId, data) {
  return request({
    url: `/hx/activity/participant/${participantId}/audit`,
    method: 'post',
    data: data
  })
}

export function auditParticipation(participantId, data) {
  return request({
    url: `/hx/activity/participant/${participantId}/audit-enroll`,
    method: 'post',
    data: data
  })
}

export function cancelParticipation(participantId) {
  return request({
    url: `/hx/activity/participant/${participantId}/cancel`,
    method: 'post'
  })
}

