const { request } = require('../../utils/request')

Page({
  data: {
    pageAnim: null,
    loading: true,
    refreshing: false,
    logs: [],
    filteredLogs: [],
    currentFilter: 'all',
    totalPoints: 0,
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    capsuleTop: 24
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
    this.loadData(true)
  },
  onPullDownRefresh() {
    this.onRefresh()
  },
  animateIn() {
    const anim = wx.createAnimation({ duration: 300, timingFunction: 'ease-out' })
    anim.opacity(0).translateY(20).step({ duration: 0 })
    this.setData({ pageAnim: anim.export() })
    setTimeout(() => {
      const anim2 = wx.createAnimation({ duration: 300, timingFunction: 'ease-out' })
      anim2.opacity(1).translateY(0).step()
      this.setData({ pageAnim: anim2.export() })
    }, 50)
  },
  loadData(reset = false) {
    const app = getApp()
    if (reset) {
      this.setData({
        pageNum: 1,
        logs: [],
        hasMore: true,
        loading: true
      })
    }
    
    request({
      url: '/app/points/logs',
      method: 'get',
      data: {
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      },
      hideLoading: !reset
    }).then(res => {
      const newLogs = (res.data && res.data.rows) || []
      const total = res.data && res.data.total ? res.data.total : 0
      const logs = reset ? newLogs : [...this.data.logs, ...newLogs]
      
      // 获取总积分
      app.fetchProfile().then(profile => {
        this.setData({
          totalPoints: profile && profile.pointsTotal ? profile.pointsTotal : 0
        })
      }).catch(() => {})
      
      this.setData({
        logs,
        hasMore: logs.length < total,
        loading: false,
        refreshing: false
      })
      this.filterLogs({ currentTarget: { dataset: { type: this.data.currentFilter } } })
    }).catch(() => {
      this.setData({
        loading: false,
        refreshing: false
      })
    })
  },
  onRefresh() {
    this.setData({ refreshing: true })
    this.loadData(true)
  },
  loadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({
      pageNum: this.data.pageNum + 1
    })
    this.loadData(false)
  },
  filterLogs(e) {
    const type = e.currentTarget.dataset.type
    let filteredLogs = []
    
    if (type === 'all') {
      filteredLogs = this.data.logs
    } else if (type === 'income') {
      filteredLogs = this.data.logs.filter(log => log.points >= 0)
    } else if (type === 'expense') {
      filteredLogs = this.data.logs.filter(log => log.points < 0)
    }
    
    this.setData({
      currentFilter: type,
      filteredLogs
    })
  }
})


