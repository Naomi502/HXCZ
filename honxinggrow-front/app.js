const { request } = require('./utils/request')

App({
  globalData: {
    // 开发环境(develop)默认不使用云函数(连本地)，体验版(trial)和正式版(release)默认使用云函数
    // 你也可以手动修改这里的 useCloud 值为 true 或 false 来强制切换
    useCloud: wx.getAccountInfoSync().miniProgram.envVersion !== 'develop', 
    apiBaseUrl: wx.getAccountInfoSync().miniProgram.envVersion === 'develop' 
      ? 'http://localhost:8080' 
      : 'http://59.110.229.252:8080',
    token: '',
    userInfo: null,
    profile: null,
    wechatBound: false,
    shouldShowLoginModal: false,
    isTabSwitch: false,
    unreadCount: 0 // 全局未读消息数
  },
  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
    // 初始化云开发环境
    wx.cloud.init({
      env: 'nan-5gbkqqnf229a1fe4', // 请替换为你的云开发环境ID
      traceUser: true
    })
  },
  setToken(token) {
    if (token) {
      this.globalData.token = token.startsWith('Bearer ') ? token.substring(7) : token
      wx.setStorageSync('token', this.globalData.token)
    } else {
      this.globalData.token = ''
      wx.removeStorageSync('token')
    }
  },
  promptLogin(options = {}) {
    this.globalData.shouldShowLoginModal = true
    const tabUrl = options.tabUrl || '/pages/mine/mine'
    wx.switchTab({
      url: tabUrl
    })
  },
  ensureLogin() {
    if (!this.globalData.token) {
      this.promptLogin()
      return false
    }
    return true
  },
  fetchProfile() {
    if (!this.ensureLogin()) {
      return Promise.reject(new Error('未登录'))
    }
    const p1 = request({
      url: '/app/points/profile',
      method: 'get',
      hideLoading: true
    }).then(res => {
      this.globalData.profile = res.data
      this.globalData.wechatBound = !!res.data?.wechatBound
      return res.data
    })
    
    // 顺便更新未读消息数
    this.updateUnreadCount()
    
    return p1
  },
  
  updateUnreadCount() {
    if (!this.globalData.token) return Promise.resolve(0)
    return request({
      url: '/app/message/unread',
      method: 'get',
      hideLoading: true
    }).then(res => {
      const count = res.data || 0
      this.globalData.unreadCount = count
      
      // 尝试更新 TabBar 红点
      // 注意：只有当页面使用了自定义 TabBar 且当前页面已加载时才有效
      // 这里无法直接获取 TabBar 实例，只能通过当前页面尝试
      const pages = getCurrentPages()
      if (pages.length > 0) {
        const currentPage = pages[pages.length - 1]
        if (typeof currentPage.getTabBar === 'function') {
          const tabBar = currentPage.getTabBar()
          if (tabBar) {
            tabBar.setData({ showRedDot: count > 0 })
          }
        }
      }
      
      return count
    }).catch(() => 0)
  }
})