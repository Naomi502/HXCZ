import request from '@/utils/request'

export function getDashboardData() {
  return request({
    url: '/hx/dashboard/statistics',
    method: 'get'
  })
}
