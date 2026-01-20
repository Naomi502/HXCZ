// treehole/send.js
const { request } = require('../../utils/request')

Page({
  /**
   * 页面的初始数据
   */
  data: {
    content: '', // 消息内容
    submitting: false, // 发送状态
    selectedMood: 'normal', // 默认心情
    moods: [
      { key: 'happy', icon: '😊', label: '开心' },
      { key: 'normal', icon: '😐', label: '平淡' },
      { key: 'sad', icon: '😔', label: '难过' },
      { key: 'angry', icon: '😠', label: '生气' },
      { key: 'confused', icon: '🤔', label: '迷茫' }
    ]
  },

  /**
   * 选择心情
   */
  onSelectMood(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ selectedMood: key })
    // 轻微震动反馈
    wx.vibrateShort({ type: 'light' })
  },

  /**
   * 处理文本输入
   */
  onInput(e) {
    const value = e.detail.value
    this.setData({
      content: value
    })
  },

  /**
   * 发送消息
   */
  sendMessage() {
    const app = getApp()
    if (!app.ensureLogin()) {
      return
    }
    
    const content = this.data.content.trim()
    
    if (!content) {
      wx.showToast({
        title: '写点什么吧~',
        icon: 'none'
      })
      return
    }

    this.setData({ submitting: true })

    request({
      url: '/app/treehole/send',
      method: 'post',
      data: { 
        content,
        mood: this.data.selectedMood 
      },
      hideLoading: false
    }).then(res => {
      wx.showToast({
        title: '投递成功',
        icon: 'success'
      })
      
      // 清空输入框
      this.setData({ content: '' })
      
      // 延迟返回上一页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }).catch(err => {
      // 错误处理由request.js完成
    }).finally(() => {
      this.setData({ submitting: false })
    })
  }
})
