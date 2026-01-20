const { request } = require('../../utils/request')
const { formatTime } = require('../../utils/formatTime')

Page({
  data: {
    runAnim: false,
    loading: true,
    storyList: [],
    capsuleTop: 24
  },

  onLoad() {
    this.initHeader()
    this.animateIn()
    this.loadStories()
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

  animateIn() {
    setTimeout(() => {
      this.setData({ runAnim: true })
    }, 50)
  },

  loadStories() {
    this.setData({ loading: true })
    request({
      url: '/app/story/liked-list',
      method: 'get',
      hideLoading: true
    }).then(res => {
      const list = (res.data || []).map(item => ({
        ...item,
        publishTime: formatTime(item.publishTime)
      }))
      this.setData({
        storyList: list,
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    })
  },

  onPullDownRefresh() {
    this.loadStories()
    wx.stopPullDownRefresh()
  }
})
