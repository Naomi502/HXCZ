const { request, getBaseUrl } = require('../../utils/request')
const { formatTime } = require('../../utils/formatTime')

Page({
  data: {
    storyList: [],
    filteredStories: [],
    searchKeyword: '',
    loading: false,
    runAnim: false,
    isLoggedIn: false,
    hasLoaded: false, // 标记是否已加载过数据
    showMsgTip: false,
    isMsgTipHiding: false,
    unreadCount: 0
  },
  onHide() {
    if (this.animTimer) clearTimeout(this.animTimer)
  },
  onLoad() {
    const app = getApp()
    const isLoggedIn = !!app.globalData.token
    this.setData({ isLoggedIn })
    if (isLoggedIn) {
      this.loadStories()
    }
  },
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
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

    // 检查登录状态变化 (例如从"我的"页面登录回来)
    const isLoggedIn = !!app.globalData.token
    if (isLoggedIn !== this.data.isLoggedIn) {
      this.setData({ isLoggedIn })
      // 如果变成了已登录，且之前没加载过，则加载
      if (isLoggedIn && !this.data.hasLoaded) {
        this.loadStories()
      }
    }

    // 检查未读消息并提示
    if (isLoggedIn) {
      this.checkUnreadMessage()
    }
  },
  
  checkUnreadMessage() {
    const app = getApp()
    app.updateUnreadCount().then(count => {
      if (count > 0) {
        // 只有当未读数发生变化或首次显示时才提示，避免每次切换都弹窗过于打扰
        // 这里简单实现：只要有未读就显示顶部通知栏
        this.setData({
          showMsgTip: true,
          isMsgTipHiding: false, // 重置隐藏状态
          unreadCount: count
        })
        
        // 3秒后自动隐藏
        if (this.msgTimer) clearTimeout(this.msgTimer)
        this.msgTimer = setTimeout(() => {
          this.hideMsgTip()
        }, 3000)
      } else {
        this.setData({ showMsgTip: false })
      }
    })
  },

  hideMsgTip() {
    this.setData({ isMsgTipHiding: true })
    // 等待动画结束后再销毁
    setTimeout(() => {
      this.setData({ showMsgTip: false, isMsgTipHiding: false })
    }, 500) // 动画时间
  },

  onMsgTipTap() {
    wx.navigateTo({
      url: '/pages/messageCenter/messageCenter'
    })
    this.hideMsgTip()
  },
  onPullDownRefresh() {
    if (!this.data.isLoggedIn) {
      wx.stopPullDownRefresh()
      return
    }
    this.loadStories(() => wx.stopPullDownRefresh())
  },
  loadStories(callback) {
    if (!this.data.isLoggedIn) {
      this.setData({ loading: false })
      if (typeof callback === 'function') callback()
      return
    }
    this.setData({ loading: true })
    request({
      url: '/app/story/list',
      method: 'get',
      hideLoading: true
    }).then(res => {
      // 获取baseUrl
      const baseUrl = getBaseUrl()
      const list = (res.data || []).map(item => {
        // 处理封面URL，确保是完整的URL
        let coverUrl = item.coverUrl || '/images/activity_default.jpg'
        if (coverUrl && !coverUrl.startsWith('http') && !coverUrl.startsWith('/images/')) {
          // 如果不是完整URL，也不是本地图片资源，添加baseUrl前缀
          coverUrl = baseUrl + coverUrl
        }
        return {
          id: item.storyId,
          title: item.title,
          summary: item.summary || '',
          coverUrl: coverUrl,
          views: item.viewCount || 0,
          likes: item.likeCount || 0,
          publishTime: formatTime(item.publishTime)
        }
      })
      this.setData({
        storyList: list,
        filteredStories: this.filterStories(list, this.data.searchKeyword),
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
    this.setData({
      searchKeyword: keyword,
      filteredStories: this.filterStories(this.data.storyList, keyword)
    })
  },
  onClearSearch() {
    this.setData({
      searchKeyword: '',
      filteredStories: this.data.storyList
    })
  },
  goToAccount() {
    wx.navigateTo({
      url: 'pages/mine/mine'
    })
  },
  filterStories(list, keyword) {
    if (!keyword) return list
    const lower = keyword.toLowerCase()
    return list.filter(item => {
      return (item.title && item.title.toLowerCase().includes(lower)) ||
        (item.summary && item.summary.toLowerCase().includes(lower))
    })
  },
  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    const app = getApp()
    if (!app.ensureLogin()) {
      return
    }
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    })
  },

  onImageError(e) {
    const index = e.currentTarget.dataset.index
    const list = this.data.filteredStories
    const item = list[index]
    if (item) {
      // Update filteredStories directly to reflect change immediately
      const key = `filteredStories[${index}].coverUrl`
      this.setData({
        [key]: '/images/activity_default.jpg'
      })
      
      // Also update source list to prevent reversion on search clear
      const sourceIndex = this.data.storyList.findIndex(s => s.id === item.id)
      if (sourceIndex !== -1) {
        this.data.storyList[sourceIndex].coverUrl = '/images/activity_default.jpg'
      }
    }
  }
})
