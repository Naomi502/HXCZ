const { request } = require('../../utils/request')
const { formatTime, formatTimeRange } = require('../../utils/formatTime')

const PROOF_STATUS_TEXT = {
  '0': '待提交证明',
  '1': '审核中',
  '2': '审核通过',
  '3': '审核驳回'
}

Page({
  data: {
    pageAnim: null,
    loading: true,
    activityList: [],
    filteredList: [],
    currentFilter: 'all',
    ongoingCount: 0,
    completedCount: 0,
    searchKeyword: '',
    capsuleTop: 24 // 默认值，防止获取失败
  },
  onLoad() {
    this.initHeader()
    this.animateIn()
  },
  initHeader() {
    try {
      const capsule = wx.getMenuButtonBoundingClientRect()
      if (capsule) {
        this.setData({ capsuleTop: capsule.top })
      }
    } catch (error) {
      console.warn('获取胶囊按钮位置失败', error)
    }
  },
  handleBack() {
    wx.navigateBack({ delta: 1 })
  },
  onShow() {
    const app = getApp()
    if (!app.ensureLogin()) return
    this.loadActivities()
  },
  onPullDownRefresh() {
    this.loadActivities(() => wx.stopPullDownRefresh())
  },
  animateIn() {
    const anim = wx.createAnimation({ duration: 280, timingFunction: 'ease-out' })
    anim.opacity(0).translateY(16).step({ duration: 0 })
    this.setData({ pageAnim: anim.export() })
    setTimeout(() => {
      const anim2 = wx.createAnimation({ duration: 280, timingFunction: 'ease-out' })
      anim2.opacity(1).translateY(0).step()
      this.setData({ pageAnim: anim2.export() })
    }, 40)
  },
  loadActivities(callback) {
    this.setData({ loading: true })
    request({
      url: '/app/activity/my',
      method: 'get',
      hideLoading: true
    }).then(res => {
      const list = (res.data || []).map(item => this.formatActivity(item))
      const ongoingCount = list.filter(item => item.statusType === 'success').length
      // 已完成统计：包括活动结束 OR 奖励已发放
      const completedCount = list.filter(item => item.statusType === 'default' || item.rewardGranted).length
      this.setData({
        activityList: list,
        filteredList: this.filterList(list, this.data.currentFilter, this.data.searchKeyword),
        loading: false,
        ongoingCount,
        completedCount
      })
    }).catch(() => {
      this.setData({ loading: false })
    }).finally(() => {
      if (typeof callback === 'function') callback()
    })
  },
  formatActivity(item) {
    // 直接使用后台返回的状态字段，并格式化时间
    const joinStatus = item.joinStatus || '1'
    let displayStatusText = PROOF_STATUS_TEXT[item.proofStatus] || PROOF_STATUS_TEXT['0']
    
    if (joinStatus === '0') {
      displayStatusText = '报名审核中'
    } else if (joinStatus === '2') {
      displayStatusText = '报名被拒'
    }

    // 计算活动状态 (时间状态)
    const now = Date.now()
    const start = new Date(item.startTime).getTime()
    const end = new Date(item.endTime).getTime()
    let statusText = '进行中'
    let statusType = 'success'

    if (now < start) {
      statusText = '未开始'
      statusType = 'info'
    } else if (now > end) {
      statusText = '已结束'
      statusType = 'default'
    }

    // 优先覆盖“已完成”状态（已获积分）
    // 注意：后端返回的可能是字符串 '1' 或布尔值 true
    if (item.rewardGranted === '1' || item.rewardGranted === true) {
      statusText = '已完成'
      statusType = 'default'
    }

    return {
      activityId: item.activityId,
      title: item.title,
      summary: item.summary,
      coverUrl: item.coverUrl || '/images/activity_default.jpg',
      startTime: formatTime(item.startTime),
      endTime: formatTime(item.endTime),
      timeRange: formatTimeRange(item.startTime, item.endTime),
      location: item.location || '待定地点',
      participantCount: item.participantCount || 0,
      maxParticipants: item.maxParticipants,
      statusText: statusText, // 使用最终计算的状态
      statusType: statusType, // 使用最终计算的类型
      proofStatus: item.proofStatus,
      proofStatusText: displayStatusText,
      joinStatus: joinStatus,
      rewardGranted: item.rewardGranted === '1' || item.rewardGranted === true,
      rewardPoints: item.rewardPoints || 0,
      proofAuditRemark: item.proofAuditRemark || '',
      joinTime: formatTime(item.joinTime)
    }
  },
  onFilterChange(e) {
    const { type } = e.currentTarget.dataset
    if (!type || type === this.data.currentFilter) {
      return
    }
    this.setData({
      currentFilter: type,
      filteredList: this.filterList(this.data.activityList, type, this.data.searchKeyword)
    })
  },
  onSearchInput(e) {
    const keyword = e.detail.value || ''
    this.setData({
      searchKeyword: keyword,
      filteredList: this.filterList(this.data.activityList, this.data.currentFilter, keyword)
    })
  },
  onClearSearch() {
    this.setData({
      searchKeyword: '',
      filteredList: this.filterList(this.data.activityList, this.data.currentFilter, '')
    })
  },
  filterList(list, type, keyword = '') {
    let temp = list
    if (type === 'ongoing') {
      temp = temp.filter(item => item.statusType === 'success')
    } else if (type === 'completed') {
      // 已完成：活动已结束(default) 或者 证明已通过(rewardGranted=true)
      temp = temp.filter(item => item.statusType === 'default' || item.rewardGranted)
    } else if (type === 'pending') {
      // 审核中：活动未开始(info) 且 未完成
      // 但实际上前端可能更想看到“证明审核中”或者“报名审核中”
      // 这里保持逻辑：状态为info(未开始) 或者 报名状态为0(待审核) 或者 证明状态为1(审核中)
      temp = temp.filter(item => (item.statusType === 'info' || item.joinStatus === '0' || item.proofStatus === '1') && !item.rewardGranted)
    }
    if (!keyword) {
      return temp
    }
    const lower = keyword.toLowerCase()
    return temp.filter(item => {
      return (item.title && item.title.toLowerCase().includes(lower)) ||
        (item.location && item.location.toLowerCase().includes(lower)) ||
        (item.summary && item.summary.toLowerCase().includes(lower))
    })
  },
  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/activityDetail/activityDetail?id=${id}`
    })
  }
})


