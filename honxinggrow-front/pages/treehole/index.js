// treehole/index.js
const { request } = require('../../utils/request')
const { formatTime } = require('../../utils/formatTime')

Page({
  /**
   * 页面的初始数据
   */
  data: {
    loading: false,
    refreshing: false,
    capsuleTop: 24, // 默认值
    treeholeList: [
      {
        id: 1,
        author: '匿名同学',
        avatarColor: '#FF9F43',
        content: '最近学习压力有点大，感觉时间完全不够用，每天都在赶作业...',
        createTime: '10分钟前',
        likes: 12,
        isLiked: false,
        replyCount: 2,
        hasReply: true // 老师已回复
      },
      {
        id: 2,
        author: '匿名同学',
        avatarColor: '#54A0FF',
        content: '今天在操场跑步的时候看到夕阳特别美，心情突然就变好了！分享给大家~',
        createTime: '2小时前',
        likes: 45,
        isLiked: true,
        replyCount: 5,
        hasReply: false
      },
      {
        id: 3,
        author: '匿名同学',
        avatarColor: '#1DD1A1',
        content: '想问问大家有没有推荐的英语单词书？感觉背单词好枯燥啊。',
        createTime: '昨天 14:30',
        likes: 3,
        isLiked: false,
        replyCount: 8,
        hasReply: false
      }
    ]
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.initHeader()
    this.loadData()
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

  goToHistory() {
    wx.navigateTo({
      url: '/pages/treehole/history'
    })
  },
  
  onShow() {
    // 每次显示时可以刷新一下，或者检查是否有新发布
    // this.loadData()
  },

  /**
   * 加载数据
   */
  loadData(callback) {
    this.setData({ loading: true })
    
    // TODO: 替换为真实的API调用
    // request({
    //   url: '/app/treehole/list',
    //   method: 'get'
    // }).then(res => {
    //   this.setData({
    //     treeholeList: res.data.map(item => ({...item, createTime: formatTime(item.createTime)}))
    //   })
    // }).finally(() => {
    //   this.setData({ loading: false, refreshing: false })
    //   if(callback) callback()
    // })

    // 模拟延迟
    setTimeout(() => {
      this.setData({ loading: false, refreshing: false })
      if(callback) callback()
    }, 500)
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadData(() => {
      wx.stopPullDownRefresh()
    })
  },

  /**
   * 跳转到发送页面
   */
  goToSend() {
    wx.navigateTo({
      url: '/pages/treehole/send'
    });
  },

  /**
   * 点赞
   */
  onLike(e) {
    const { id, index } = e.currentTarget.dataset
    const list = this.data.treeholeList
    const item = list[index]
    
    // 模拟点赞
    if (item.isLiked) {
      item.likes--
      item.isLiked = false
    } else {
      item.likes++
      item.isLiked = true
      // 震动反馈
      wx.vibrateShort({ type: 'light' })
    }
    
    this.setData({
      [`treeholeList[${index}]`]: item
    })
    
    // TODO: 调用后端点赞API
  },
  
  /**
   * 查看详情（如有详情页）
   */
  onCardTap(e) {
    const { id } = e.currentTarget.dataset
    // 暂时不做跳转，或者跳转到详情页
    // wx.navigateTo({ url: `/pages/treehole/detail?id=${id}` })
  }
})
