const { request } = require('./utils/request')
const { clearCache } = require('./utils/guestCache')
const envConfig = require('./config/env')

App({
  globalData: {
    env: envConfig.currentEnv,
    apiBaseUrl: '',
    token: '',
    userInfo: null,
    profile: null,
    wechatBound: false,
    shouldShowLoginModal: false,
    isTabSwitch: false,
    unreadCount: 0
  },
  
  getApiBaseUrl() {
    if (this.globalData.apiBaseUrl) {
      return this.globalData.apiBaseUrl
    }
    
    const config = envConfig.getEnvConfig()
    this.globalData.apiBaseUrl = config.apiBaseUrl
    
    return this.globalData.apiBaseUrl
  },
  
  setEnv(envName) {
    if (envConfig.setEnv(envName)) {
      this.globalData.env = envName
      this.globalData.apiBaseUrl = ''
      return this.getApiBaseUrl()
    }
    return null
  },
  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
    
    this.getApiBaseUrl()
    
    console.log('[ENV] 当前环境:', this.globalData.env)
    console.log('[ENV] API地址:', this.globalData.apiBaseUrl)
  },
  setToken(token) {
    if (token) {
      this.globalData.token = token.startsWith('Bearer ') ? token.substring(7) : token
      wx.setStorageSync('token', this.globalData.token)
      clearCache()
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