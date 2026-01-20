const { request } = require('../../utils/request')

Page({
  data: {
    runAnim: false,
    loading: true,
    profile: null,
    userName: '',
    totalPoints: 0,
    storyCount: 0,
    activityCount: 0,
    isLoggedIn: false,

    // 登录弹窗
    showLoginModal: false,
    loginMode: 'wechat', // ⭐ 新增：wechat | account

    loginUsername: '',
    loginPassword: '',
    loginSubmitting: false,

    wechatBound: false,
    wechatLoginLoading: false,
    localAvatar: '', // 本地持久化头像
    unreadCount: 0 // 新增未读消息数
  },

  onLoad() {
    // 读取本地存储的头像
    const localAvatar = wx.getStorageSync('userAvatar')
    if (localAvatar) {
      this.setData({ localAvatar })
    }
  },

  onHide() {
    if (this.animTimer) clearTimeout(this.animTimer)
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 3
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

    const token = app.globalData.token || wx.getStorageSync('token')

    if (!token) {
      const shouldShow = app.globalData.shouldShowLoginModal
      this.setData({
        isLoggedIn: false,
        loading: false,
        profile: null,
        userName: '',
        totalPoints: 0,
        storyCount: 0,
        activityCount: 0,
        showLoginModal: shouldShow,
        loginMode: 'wechat', // ⭐ 默认微信登录
        wechatBound: false
      })
      return
    }

    if (!app.globalData.token && token) {
      app.globalData.token = token
    }

    const showModal = app.globalData.shouldShowLoginModal
    if (showModal) {
      this.setData({
        showLoginModal: true,
        loginMode: 'wechat'
      })
    }
    app.globalData.shouldShowLoginModal = false

    this.setData({ isLoggedIn: true })

    // 优化：如果有缓存数据，则不再自动刷新，依赖下拉刷新更新
    if (this.data.profile) {
      return
    }

    this.loadData()
  },

  onPullDownRefresh() {
    if (!this.data.isLoggedIn) {
      wx.stopPullDownRefresh()
      return
    }
    this.loadData().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadData() {
    const app = getApp()
    this.setData({ loading: true })

    const p1 = app.fetchProfile().then(profile => {
      const wechatBound = !!profile?.wechatBound
      app.globalData.wechatBound = wechatBound

      this.setData({
        profile,
        totalPoints: profile?.pointsTotal || 0,
        userName:
          profile?.userName ||
          app.globalData.userInfo?.userName ||
          app.globalData.userInfo?.nickName ||
          '学生用户',
        storyCount: profile?.storyCount || 0,
        activityCount: profile?.activityCount || 0,
        wechatBound,
        isLoggedIn: true
      })
    })

    // 获取未读消息数
    const p2 = app.updateUnreadCount().then(count => {
       this.setData({ unreadCount: count })
    })

    return Promise.all([p1, p2]).finally(() => {
      this.setData({ loading: false })
    })
  },

  /* ================= 登录弹窗 ================= */

  handleProfileCardTap() {
    if (!this.data.isLoggedIn) {
      this.setData({
        showLoginModal: true,
        loginMode: 'wechat'
      })
      return
    }
    this.goToAccountSettings()
  },

  showLoginModal() {
    this.setData({
      showLoginModal: true,
      loginMode: 'wechat'
    })
  },

  closeLoginModal() {
    if (this.data.loginSubmitting || this.data.wechatLoginLoading) return

    this.setData({
      showLoginModal: false,
      loginMode: 'wechat',
      loginUsername: '',
      loginPassword: ''
    })

    const app = getApp()
    app.globalData.shouldShowLoginModal = false
  },

  // ⭐ 新增：切换到账号密码登录
  switchToAccountLogin() {
    this.setData({ loginMode: 'account' })
  },

  // ⭐ 新增：返回微信登录
  switchToWechatLogin() {
    this.setData({
      loginMode: 'wechat',
      loginUsername: '',
      loginPassword: ''
    })
  },

  onLoginUsernameInput(e) {
    this.setData({ loginUsername: e.detail.value.trim() })
  },

  onLoginPasswordInput(e) {
    this.setData({ loginPassword: e.detail.value })
  },

  handleInlineLogin() {
    if (this.data.loginSubmitting) return

    if (!this.data.loginUsername) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    if (!this.data.loginPassword) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ loginSubmitting: true })

    request({
      url: '/app/login',
      method: 'post',
      data: {
        username: this.data.loginUsername,
        password: this.data.loginPassword
      },
      hideLoading: true
    }).then(res => {
      const app = getApp()
      app.setToken(res.token)
      app.globalData.userInfo = { userName: this.data.loginUsername }
      app.globalData.shouldShowLoginModal = false

      wx.showToast({ title: '登录成功', icon: 'success' })
      this.setData({
        isLoggedIn: true,
        showLoginModal: false,
        loginMode: 'wechat'
      })
      this.loadData()
    }).finally(() => {
      this.setData({
        loginSubmitting: false,
        loginPassword: ''
      })
    })
  },

  handleWechatQuickLogin() {
    if (this.data.wechatLoginLoading) return

    this.setData({ wechatLoginLoading: true })

    wx.login({
      timeout: 5000,
      success: res => {
        if (!res.code) {
          wx.showToast({ title: '未获取到微信code', icon: 'none' })
          this.setData({ wechatLoginLoading: false })
          return
        }

        request({
          url: '/app/wechat/login',
          method: 'post',
          data: { code: res.code },
          hideLoading: true
        }).then(resp => {
          const app = getApp()
          app.setToken(resp.token)
          app.globalData.shouldShowLoginModal = false

          wx.showToast({ title: '登录成功', icon: 'success' })
          this.setData({
            isLoggedIn: true,
            showLoginModal: false,
            loginMode: 'wechat',
            loginUsername: '',
            loginPassword: ''
          })
          this.loadData()
        }).finally(() => {
          this.setData({ wechatLoginLoading: false })
        })
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败，请稍后再试', icon: 'none' })
        this.setData({ wechatLoginLoading: false })
      }
    })
  },

  onChooseAvatar(e) {
    const { avatarUrl } = e.detail
    if (!avatarUrl) return

    const fs = wx.getFileSystemManager()
    const savedFilePath = wx.env.USER_DATA_PATH + '/user_avatar_' + Date.now() + '.png'

    // 保存到本地永久存储
    fs.saveFile({
      tempFilePath: avatarUrl,
      filePath: savedFilePath,
      success: (res) => {
        const permanentPath = res.savedFilePath
        this.setData({ localAvatar: permanentPath })
        wx.setStorageSync('userAvatar', permanentPath) // 持久化存储
        
        // 更新全局变量，方便其他页面即时读取
        const app = getApp()
        if (app.globalData.userInfo) {
            app.globalData.userInfo.localAvatar = permanentPath
        }
      },
      fail: (err) => {
        console.error('保存头像失败', err)
        // 降级：直接用临时路径，哪怕下次可能会失效
        this.setData({ localAvatar: avatarUrl })
      }
    })
  },

  /* ================= 页面跳转（原样保留） ================= */

  goToTreeHole() {
    wx.navigateTo({ url: '/pages/treehole/index' })
  },

  goToMyActivities() {
    wx.navigateTo({ url: '/pages/myActivities/myActivities' })
  },

  goToLikedStories() {
    wx.navigateTo({ url: '/pages/likedStories/likedStories' })
  },

  goToPointsDetail() {
    wx.navigateTo({ url: '/pages/pointsDetail/pointsDetail' })
  },

  goToGrowthHistory() {
    wx.navigateTo({ url: '/pages/growthHistory/growthHistory' })
  },

  goToAccountSettings() {
    wx.navigateTo({ url: '/pages/accountSettings/accountSettings' })
  },

  goToMessageCenter() {
    wx.navigateTo({ url: '/pages/messageCenter/messageCenter' })
  },

  /* ================= 退出登录（原样保留） ================= */

  handleLogout() {
    const app = getApp()
    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      confirmColor: '#ff4d4f',
      success: res => {
        if (res.confirm) {
          app.setToken('')
          this.setData({
            profile: null,
            totalPoints: 0,
            userName: '',
            storyCount: 0,
            activityCount: 0,
            isLoggedIn: false,
            showLoginModal: true,
            loginMode: 'wechat',
            loginUsername: '',
            loginPassword: '',
            wechatBound: false
          })
          app.globalData.shouldShowLoginModal = true
          app.globalData.wechatBound = false
        }
      }
    })
  }
})
