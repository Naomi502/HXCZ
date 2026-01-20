const { request } = require('../../utils/request')
const { formatTime } = require('../../utils/formatTime')

Page({
  data: {
    loading: true,
    messageList: [],
    unreadCount: 0,
    capsuleTop: 24, // 默认胶囊位置
    refreshing: false
  },

  onLoad() {
    this.initHeader()
  },

  onShow() {
    this.loadMessages()
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

  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadMessages(() => {
      this.setData({ refreshing: false })
    })
  },

  loadMessages(callback) {
    this.setData({ loading: true })
    request({
      url: '/app/message/list',
      method: 'get',
      hideLoading: true
    }).then(res => {
      const list = (res.rows || []).map(item => ({
        ...item,
        createTime: formatTime(item.createTime)
      }))
      const unreadCount = list.filter(m => m.isRead === '0').length
      this.setData({
        messageList: list,
        unreadCount,
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    }).finally(() => {
      if (typeof callback === 'function') callback()
    })
  },

  toggleExpand(e) {
    const { id } = e.currentTarget.dataset
    const list = this.data.messageList.map(item => {
      if (item.messageId === id) {
        // 如果是展开未读消息，自动标记为已读
        if (!item.expanded && item.isRead === '0') {
          this.markAsRead(id)
          item.isRead = '1'
        }
        item.expanded = !item.expanded
      }
      return item
    })
    this.setData({ messageList: list })
  },

  markAsRead(id) {
    request({
      url: `/app/message/${id}/read`,
      method: 'post',
      hideLoading: true
    }).then(() => {
      // 成功标记后，重新计算未读数并更新全局状态
      // 也可以简单地本地减 1，但为了准确性，这里重新计算列表中的未读数
      const unreadCount = this.data.messageList.filter(m => m.isRead === '0').length
      const app = getApp()
      if (app) {
        app.globalData.unreadCount = unreadCount
        // 如果当前未读数为0，尝试消除红点
        // 注意：这里我们不能直接操作 TabBar，因为 messageCenter 不是 Tab 页
        // 但我们可以更新 app.globalData，当用户返回 Tab 页时（onShow）会重新获取并更新
        // 为了保险，我们也可以尝试更新上一页（如果是 mine 页）的数据
      }
    })
  },

  onUnload() {
    // 页面卸载（返回）时，同步最新的未读数到全局和 TabBar
    // 因为在这个页面操作了已读，需要通知外面
    const app = getApp()
    if (app) {
      app.updateUnreadCount() // 这会触发网络请求获取最新数，并更新 TabBar
    }
  },

  deleteMessage(e) {
    const { id } = e.currentTarget.dataset
    wx.showModal({
      title: '提示',
      content: '确定要删除这条消息吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `/app/message/${id}/delete`,
            method: 'post'
          }).then(() => {
            wx.showToast({ title: '已删除', icon: 'none' })
            this.loadMessages()
          })
        }
      }
    })
  },

  clearAll() {
    if (this.data.messageList.length === 0) return

    wx.showModal({
      title: '确认清空',
      content: '确定要清空所有消息通知吗？此操作不可恢复。',
      confirmColor: '#FF6B6B',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '正在清空...', mask: true })
          request({
            url: '/app/message/clear',
            method: 'post'
          }).then(() => {
            wx.showToast({ title: '已清空', icon: 'success' })
            this.setData({
              messageList: [],
              unreadCount: 0
            })
            // 同步全局未读数
            const app = getApp()
            if (app) {
              app.globalData.unreadCount = 0
            }
          }).finally(() => {
            wx.hideLoading()
          })
        }
      }
    })
  }
})
