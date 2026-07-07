const { request } = require('../../utils/request')

Page({
  data: {
    capsuleTop: 44, // 默认胶囊位置
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    submitting: false,
    wechatBound: false,
    bindingWechat: false
  },

  onLoad() {
    this.initHeader()
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
    this.refreshBindState()
  },

  refreshBindState() {
    const app = getApp()
    if (!app || !app.globalData.token) {
      return
    }
    app.fetchProfile().then(profile => {
      this.setData({ wechatBound: !!profile?.wechatBound })
    }).catch(() => {
      // 已有统一提示
    })
  },
  onOldPwdInput(e) {
    this.setData({ oldPassword: e.detail.value })
  },
  onNewPwdInput(e) {
    this.setData({ newPassword: e.detail.value })
  },
  onConfirmPwdInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },
  handleSubmit() {
    const { oldPassword, newPassword, confirmPassword } = this.data
    if (!oldPassword) {
      wx.showToast({ title: '请输入当前密码', icon: 'none' })
      return
    }
    if (!newPassword || newPassword.length < 6) {
      wx.showToast({ title: '新密码至少6位', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    request({
      url: '/system/user/profile/updatePwd',
      method: 'put',
      data: {
        oldPassword,
        newPassword
      }
    }).then(() => {
      wx.showToast({ title: '密码修改成功', icon: 'success' })
      this.setData({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
      this.refreshBindState()
    }).catch(() => {
      // 失败提示由 request.js 统一处理
    }).finally(() => {
      this.setData({ submitting: false })
    })
  },

  handleBindWechat() {
    if (this.data.wechatBound || this.data.bindingWechat) {
      if (this.data.wechatBound) {
        wx.showToast({ title: '已绑定微信', icon: 'none' })
      }
      return
    }
    this.setData({ bindingWechat: true })
    wx.login({
      timeout: 5000,
      success: res => {
        if (!res.code) {
          wx.showToast({ title: '未获取到微信code', icon: 'none' })
          this.setData({ bindingWechat: false })
          return
        }
        request({
          url: '/app/wechat/bind',
          method: 'post',
          data: { code: res.code },
          hideLoading: true
        }).then(() => {
          wx.showToast({ title: '绑定成功', icon: 'success' })
          this.setData({ wechatBound: true })
          const app = getApp()
          if (app) {
            app.globalData.wechatBound = true
          }
        }).catch(() => {
          // 提示在 request 中
        }).finally(() => {
          this.setData({ bindingWechat: false })
        })
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
        this.setData({ bindingWechat: false })
      }
    })
  }
})

