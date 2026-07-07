const { request, getBaseUrl } = require('../../utils/request')
const { formatTime, formatTimeRange } = require('../../utils/formatTime')

Page({
  data: {
    loading: false,
    searchKeyword: '',
    activityList: [],
    filteredActivities: [],
    runAnim: false,
    isLoggedIn: false,
    statusFilter: 'ongoing', // 前端过滤：只显示进行中的活动
    hasLoaded: false
  },
  onHide() {
    if (this.animTimer) clearTimeout(this.animTimer)
  },
  onLoad() {
    const app = getApp()
    const isLoggedIn = !!app.globalData.token
    this.setData({ isLoggedIn })
    this.loadActivities()
  },
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 1
      })
    }
    
    const app = getApp()
    if (this.animTimer) clearTimeout(this.animTimer)
    
    if (app.globalData.isTabSwitch) {
      // 先强制重置为 false，等待视图更新后，再开启 true
      this.setData({ runAnim: false }, () => {
        this.animTimer = setTimeout(() => {
          this.setData({ runAnim: true })
        }, 50)
      })
      app.globalData.isTabSwitch = false
    } else {
      this.setData({ runAnim: true })
    }

    const isLoggedIn = !!app.globalData.token
    if (isLoggedIn !== this.data.isLoggedIn) {
      this.setData({ isLoggedIn })
      if (!this.data.hasLoaded) {
        this.loadActivities()
      }
    }
  },
  onPullDownRefresh() {
    this.loadActivities(() => wx.stopPullDownRefresh())
  },
  loadActivities(callback) {
    this.setData({ loading: true })
    // 请求所有活动，后台已经计算好状态
    request({
      url: '/app/activity/list',
      method: 'get',
      data: {
        status: 'all' // 请求所有活动
      },
      hideLoading: true
    }).then(res => {
      // 获取baseUrl
      const baseUrl = getBaseUrl()
      // 直接使用后台返回的状态字段，并格式化时间
      const list = (res.data || []).map(item => {
        // 处理封面URL，确保是完整的URL
        let coverUrl = item.coverUrl || '/images/activity_default.jpg'
        if (coverUrl && !coverUrl.startsWith('http') && !coverUrl.startsWith('/images/')) {
          // 如果不是完整URL，也不是本地图片资源，添加baseUrl前缀
          coverUrl = baseUrl + coverUrl
        }
        return {
          activityId: item.activityId,
          title: item.title,
          summary: item.summary,
          coverUrl: coverUrl,
          startTime: formatTime(item.startTime),
          endTime: formatTime(item.endTime),
          timeRange: formatTimeRange(item.startTime, item.endTime),
          location: item.location,
          participantCount: item.participantCount || 0,
          maxParticipants: item.maxParticipants,
          status: item.status || '进行中',
          statusType: item.statusType || 'success'
        }
      })
      // 根据 statusFilter 过滤活动：只显示进行中的活动
      const filteredByStatus = this.filterByStatus(list)
      this.setData({
        activityList: list,
        filteredActivities: this.filterActivities(filteredByStatus, this.data.searchKeyword),
        loading: false,
        hasLoaded: true
      })
    }).catch(() => {
      this.setData({ loading: false })
    }).finally(() => {
      if (typeof callback === 'function') callback()
    })
  },
  onSearchInput(e) {
    const keyword = e.detail.value
    // 先过滤状态，再过滤关键词
    const statusFiltered = this.filterByStatus(this.data.activityList)
    this.setData({
      searchKeyword: keyword,
      filteredActivities: this.filterActivities(statusFiltered, keyword)
    })
  },
  onClearSearch() {
    // 先过滤状态，再过滤关键词
    const statusFiltered = this.filterByStatus(this.data.activityList)
    this.setData({
      searchKeyword: '',
      filteredActivities: statusFiltered
    })
  },
  filterByStatus(list) {
    // 根据 statusFilter 过滤活动：只显示进行中的活动
    if (this.data.statusFilter === 'ongoing') {
      // 显示进行中和未开始的活动
      return list.filter(item => item.statusType === 'success' || item.statusType === 'info')
    }
    return list
  },
  filterActivities(list, keyword) {
    if (!keyword) return list
    const lower = keyword.toLowerCase()
    return list.filter(item => {
      return (item.title && item.title.toLowerCase().includes(lower)) ||
        (item.summary && item.summary.toLowerCase().includes(lower)) ||
        (item.location && item.location.toLowerCase().includes(lower))
    })
  },
  goAllActivities() {
    wx.navigateTo({
      url: `/pages/activityAll/activityAll?status=${this.data.statusFilter || 'all'}`
    })
  },
  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/activityDetail/activityDetail?id=${id}`
    })
  }
})
