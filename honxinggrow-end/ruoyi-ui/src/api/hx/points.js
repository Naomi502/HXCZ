import request from '@/utils/request'

// 学生列表
export function listStudents(query) {
  return request({
    url: '/hx/student/list',
    method: 'get',
    params: query
  })
}

// 学生详情
export function getStudent(userId) {
  return request({
    url: `/hx/student/${userId}`,
    method: 'get'
  })
}

// 更新学生信息
export function updateStudent(data) {
  return request({
    url: '/hx/student',
    method: 'put',
    data: data
  })
}

// 调整学生积分
export function adjustPoints(data) {
  return request({
    url: '/hx/student/adjustPoints',
    method: 'post',
    data: data
  })
}

// 重置学生密码
export function resetPassword(data) {
  return request({
    url: '/hx/student/resetPwd',
    method: 'put',
    data: data
  })
}

// 更新学生状态
export function updateStatus(data) {
  return request({
    url: '/hx/student/changeStatus',
    method: 'put',
    data: data
  })
}

// 解绑微信
export function unbindWechat(data) {
  return request({
    url: `/hx/student/unbindWechat/${data.userId}`,
    method: 'delete'
  })
}

// 学生积分流水
export function listPointLogs(query) {
  return request({
    url: '/hx/points/log/list',
    method: 'get',
    params: query
  })
}

// 批量删除学生
export function deleteStudents(studentIds) {
  return request({
    url: '/hx/student/' + studentIds,
    method: 'delete'
  })
}

// 批量更新学生状态
export function updateStudentsStatus(data) {
  return request({
    url: '/hx/student/batchUpdateStatus',
    method: 'put',
    data: data
  })
}

// 获取学生活动记录
export function getStudentActivities(query) {
  return request({
    url: '/hx/student/activities',
    method: 'get',
    params: query
  })
}

