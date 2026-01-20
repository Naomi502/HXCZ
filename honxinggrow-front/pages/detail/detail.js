const { request } = require('../../utils/request')
const { formatTime } = require('../../utils/formatTime')

Page({
  data: {
    id: null,
    loading: true,
    detailData: null,
    hasLiked: false,
    recommendedStories: [],
    pageAnim: null,
    contentBlocks: [],
    imageList: [],
    capsuleTop: 24
  },
  onLoad(options) {
    const { id } = options
    this.setData({ id: id })
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
    const app = getApp()
    if (!app.ensureLogin()) return
    this.animateIn()
    this.loadDetail()
  },
  animateIn() {
    const anim = wx.createAnimation({ duration: 300, timingFunction: 'ease-out' })
    anim.opacity(0).translateY(20).step({ duration: 0 })
    this.setData({ pageAnim: anim.export() })
    setTimeout(() => {
      const anim2 = wx.createAnimation({ duration: 300, timingFunction: 'ease-out' })
      anim2.opacity(1).translateY(0).step()
      this.setData({ pageAnim: anim2.export() })
    }, 50)
  },
  loadDetail() {
    const { id } = this.data
    if (!id) return
    this.setData({ loading: true })
    this.fetchStory(id)
  },
  fetchStory(id) {
    request({
      url: `/app/story/${id}`,
      method: 'get',
      hideLoading: true
    }).then(res => {
      const result = res.data || {}
      const story = result.story || result
      wx.setNavigationBarTitle({ title: story.title || '故事详情' })
      const processed = this.processRichText(story.content || story.description || '')
      // 格式化时间
      const formattedStory = {
        ...story,
        publishTime: formatTime(story.publishTime)
      }
      this.setData({
        detailData: formattedStory,
        hasLiked: result.hasLiked || false,
        contentBlocks: processed.blocks,
        imageList: processed.images,
        loading: false
      })
      this.loadRecommended(story.storyId)
    }).catch(() => {
      this.setData({ loading: false })
    })
  },
  handleLike() {
    const { id, hasLiked } = this.data
    if (hasLiked) {
      wx.showToast({
        title: '您已经点赞过了',
        icon: 'none'
      })
      return
    }
    if (!id) return
    
    wx.showLoading({ title: '点赞中...' })
    request({
      url: `/app/story/${id}/like`,
      method: 'post'
    }).then(res => {
      wx.hideLoading()
      const result = res.data || {}
      const story = result.story || {}
      const formattedStory = {
        ...story,
        publishTime: formatTime(story.publishTime)
      }
      this.setData({
        detailData: formattedStory,
        hasLiked: true
      })
      wx.showToast({
        title: '点赞成功',
        icon: 'success'
      })
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({
        title: err.msg || '点赞失败',
        icon: 'none'
      })
    })
  },
  processRichText(html) {
    if (!html) {
      return { blocks: [], images: [] }
    }
    // 处理tab字符，将tab转换为4个空格
    html = html.replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;')
    const blocks = []
    const images = []
    const imgRegex = /<img\s+[^>]*?src\s*=\s*['"]([^'"]+)['"][^>]*>/gi
    let lastIndex = 0
    let match
    while ((match = imgRegex.exec(html)) !== null) {
      if (match.index > lastIndex) {
        const textContent = html.substring(lastIndex, match.index)
        if (textContent.trim()) {
          blocks.push({ type: 'html', content: textContent })
        }
      }
      const src = match[1]
      images.push(src)
      blocks.push({ type: 'image', src })
      lastIndex = match.index + match[0].length
    }
    if (lastIndex < html.length) {
      const remaining = html.substring(lastIndex)
      if (remaining.trim()) {
        blocks.push({ type: 'html', content: remaining })
      }
    }
    if (!blocks.length) {
      blocks.push({ type: 'html', content: html })
    }
    return { blocks, images }
  },
  previewImage(e) {
    const current = e.currentTarget.dataset.current
    const urls = this.data.imageList && this.data.imageList.length ? this.data.imageList : [current]
    wx.previewImage({
      current,
      urls
    })
  },
  loadRecommended(currentId) {
    request({
      url: '/app/story/list',
      method: 'get',
      hideLoading: true
    }).then(res => {
      const list = res.data || []
      const filtered = list.filter(item => item.storyId !== Number(currentId)).slice(0, 3)
      // 格式化推荐故事的时间
      const formatted = filtered.map(item => ({
        ...item,
        publishTime: formatTime(item.publishTime)
      }))
      this.setData({ recommendedStories: formatted })
    }).catch(() => {})
  },
  viewRelatedStory(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`
    })
  },
  onShareAppMessage() {
    const detail = this.data.detailData || {}
    return {
      title: detail.title || '红芯成长',
      path: `/pages/detail/detail?id=${this.data.id}`
    }
  },
  onShareTimeline() {
    const detail = this.data.detailData || {}
    return {
      title: detail.title || '红芯成长',
      query: `id=${this.data.id}`
    }
  }
})

