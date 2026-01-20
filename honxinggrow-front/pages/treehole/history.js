// treehole/history.js
const { request } = require('../../utils/request')

Page({
  /**
   * 页面的初始数据
   */
  data: {
    messages: [], // 消息列表
    loading: false, // 加载状态
    capsuleTop: 24
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.initHeader()
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

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadMessages(true)
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  /**
   * 加载历史消息
   * @param {boolean} isRefresh 是否为下拉刷新
   */
  loadMessages(isRefresh = false) {
    if (!isRefresh) {
      this.setData({ loading: true })
    }

    request({
      url: '/app/treehole/history',
      method: 'get',
      showLoading: false
    }).then(res => {
      // 格式化时间
      const messages = res.data.map(item => {
        return {
          ...item,
          createTime: this.formatTime(item.createTime)
        }
      })

      this.setData({ messages, loading: false })
    }).catch(err => {
      this.setData({ loading: false })
      // 错误处理由request.js完成
    }).finally(() => {
      if (isRefresh) {
        wx.stopPullDownRefresh()
      }
    })
  },

  /**
   * 格式化时间
   * @param {string} time 时间字符串
   * @returns 格式化后的时间
   */
  formatTime(time) {
    if (!time) return ''
    
    const date = new Date(time)
    const now = new Date()
    
    // 计算时间差（毫秒）
    const diff = now - date
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (days === 0) {
      // 当天，显示时分
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (days === 1) {
      // 昨天
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (days < 7) {
      // 一周内，显示星期几
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[date.getDay()] + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else {
      // 显示年月日
      return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
    }
  },

  /**
   * 跳转到发送页面
   */
  goToSend() {
    wx.navigateTo({
      url: '/pages/treehole/send'
    })
  }
})