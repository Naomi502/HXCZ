const { request, getBaseUrl } = require('../../utils/request')
const { uploadFile } = require('../../utils/upload')
const { formatTime, formatTimeRange } = require('../../utils/formatTime')

const PROOF_STATUS_TEXT = {
  '0': '待提交证明',
  '1': '审核中',
  '2': '审核通过',
  '3': '审核驳回'
}

Page({
  data: {
    id: null,
    loading: true,
    detailData: null,
    joined: false,
    canJoin: true,
    activityStatus: '',
    pageAnim: null,
    participantInfo: null,
    proofStatus: '0',
    proofStatusText: '待提交证明',
    proofHintText: '',
    proofImageUrl: '',
    proofAuditRemark: '',
    rewardGranted: false,
    rewardPoints: 0,
    canSubmitProof: false,
    proofUploading: false,
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
    this.fetchActivity(id)
  },
  fetchActivity(id) {
    request({
      url: `/app/activity/${id}`,
      method: 'get',
      hideLoading: true
    }).then(res => {
      const data = res.data || {}
      const activity = data.activity || {}
      wx.setNavigationBarTitle({ title: activity.title || '活动详情' })

      const now = new Date().getTime()
      const startTime = activity.startTime ? new Date(activity.startTime).getTime() : null
      const endTime = activity.endTime ? new Date(activity.endTime).getTime() : null
      let status = ''
      let canJoin = true

      if (endTime && now > endTime) {
        status = '活动已结束'
        canJoin = false
      } else if (startTime && now < startTime) {
        status = '活动未开始'
        canJoin = true
      } else if (startTime && endTime && now >= startTime && now <= endTime) {
        status = '进行中'
        canJoin = true
      }

      if (activity.maxParticipants && activity.participantCount >= activity.maxParticipants) {
        status = '报名已满'
        canJoin = false
      }

      const participant = data.participant || null
      const joined = !!data.joined
      const enrollStatus = participant ? participant.status : null // '0'=申请中, '1'=已报名, '2'=取消/拒绝
      
      // 获取baseUrl
      const baseUrl = getBaseUrl()
      // 处理封面URL，确保是完整的URL
      let coverUrl = activity.coverUrl || ''
      
      // 如果没有封面，使用默认封面
      if (!coverUrl) {
        coverUrl = '/images/activity_default.jpg' 
      } else if (!coverUrl.startsWith('http') && !coverUrl.startsWith('/images/')) {
        // 如果不是完整URL，也不是本地图片资源，添加baseUrl前缀
        coverUrl = baseUrl + coverUrl
      }
      const formattedActivity = {
        ...activity,
        coverUrl: coverUrl,
        startTime: formatTime(activity.startTime),
        endTime: formatTime(activity.endTime),
        timeRange: formatTimeRange(activity.startTime, activity.endTime)
      }
      
      this.setData({
        detailData: formattedActivity,
        joined,
        enrollStatus,
        canJoin: canJoin && (!joined || enrollStatus === '2'),
        activityStatus: status,
        loading: false
      })

      if (participant && participant.proofImageUrl && !participant.proofImageUrl.startsWith('http') && !participant.proofImageUrl.startsWith('/images/')) {
        participant.proofImageUrl = baseUrl + participant.proofImageUrl
      }

      this.updateParticipantState(participant, joined && enrollStatus === '1')
    }).catch(() => {
      this.setData({ loading: false })
    })
  },
  handleImageError(e) {
    // 图片加载失败时，回退到默认图
    console.warn('Cover image load failed, using default.', e)
    this.setData({
      'detailData.coverUrl': '/images/activity_default.jpg'
    })
  },
  updateParticipantState(participant, joinedAndApproved) {
    const status = (participant && participant.proofStatus) || '0'
    const rewardGranted = participant ? participant.rewardGranted === '1' : false
    const rewardPoints = rewardGranted ? (participant.rewardPoints || 0) : 0
    const proofImageUrl = participant ? (participant.proofImageUrl || '') : ''
    const proofAuditRemark = participant ? (participant.proofAuditRemark || '') : ''
    const statusText = this.formatProofStatus(status)
    const hintText = this.buildProofHint(status, rewardGranted)
    const canSubmitProof = joinedAndApproved && (status === '0' || status === '3')
    this.setData({
      participantInfo: participant,
      proofStatus: status,
      proofStatusText: statusText,
      proofHintText: hintText,
      proofImageUrl,
      proofAuditRemark,
      rewardGranted,
      rewardPoints,
      canSubmitProof,
      // 强制更新 joined 状态，确保视图渲染
      joined: !!participant,
      enrollStatus: participant ? participant.status : null
    })
  },
  formatProofStatus(status) {
    return PROOF_STATUS_TEXT[status] || PROOF_STATUS_TEXT['0']
  },
  buildProofHint(status, rewardGranted) {
    if (rewardGranted) {
      return '积分已发放，感谢参与！'
    }
    switch (status) {
      case '0':
        return '请上传参加证明，老师审核通过后才会发放积分。'
      case '1':
        return '老师正在审核，请耐心等待。'
      case '2':
        return '审核已通过，积分即将发放。'
      case '3':
        return '审核未通过，请根据老师意见重新上传。'
      default:
        return ''
    }
  },
  joinActivity() {
    if (this.data.joined && this.data.enrollStatus !== '2') {
      wx.showToast({ title: '您已报名该活动', icon: 'none' })
      return
    }
    if (!this.data.canJoin) {
      wx.showToast({ title: '当前无法报名', icon: 'none' })
      return
    }
    wx.showLoading({ title: '提交中', mask: true })
    request({
      url: `/app/activity/${this.data.id}/join`,
      method: 'post',
      hideLoading: true
    }).then(res => {
      wx.hideLoading()
      const participant = res.data
      if (participant && participant.status === '0') {
        wx.showToast({ title: '已提交申请', icon: 'success' })
      } else {
        wx.showToast({ title: '报名成功', icon: 'success' })
      }
      this.setData({
        joined: true,
        canJoin: false
      })
      this.fetchActivity(this.data.id)
    }).catch(() => {
      wx.hideLoading()
    })
  },
  chooseProofImage() {
    if (!this.data.joined) {
      wx.showToast({ title: '请先报名活动', icon: 'none' })
      return
    }
    if (!this.data.canSubmitProof) {
      wx.showToast({ title: '当前不可上传', icon: 'none' })
      return
    }
    if (this.data.proofUploading) {
      wx.showToast({ title: '正在上传', icon: 'none' })
      return
    }
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: res => {
        const filePath = res.tempFilePaths && res.tempFilePaths[0]
        if (filePath) {
          this.uploadProofImage(filePath)
        }
      }
    })
  },
  uploadProofImage(filePath) {
    this.setData({ proofUploading: true })
    uploadFile(filePath)
      .then(resp => {
        const fileUrl = this.normalizeUploadedUrl(resp)
        if (!fileUrl) {
          wx.showToast({ title: '文件地址异常', icon: 'none' })
          this.setData({ proofUploading: false })
          return
        }
        return this.submitProofImage(fileUrl)
      })
      .catch(() => {
        this.setData({ proofUploading: false })
      })
  },
  normalizeUploadedUrl(resp) {
    if (!resp) return ''
    if (resp.url) {
      return resp.url
    }
    if (resp.fileName) {
      const fileName = resp.fileName.startsWith('/') ? resp.fileName : `/${resp.fileName}`
      return `${getBaseUrl()}${fileName}`
    }
    return ''
  },
  submitProofImage(imageUrl) {
    return request({
      url: `/app/activity/${this.data.id}/proof`,
      method: 'post',
      data: { imageUrl }
    }).then(res => {
      wx.showToast({ title: '已提交审核', icon: 'success' })
      this.updateParticipantState(res.data || null, true)
    }).finally(() => {
      this.setData({ proofUploading: false })
    })
  },
  previewProofImage() {
    if (!this.data.proofImageUrl) return
    wx.previewImage({
      current: this.data.proofImageUrl,
      urls: [this.data.proofImageUrl]
    })
  },
  onShareAppMessage() {
    const detail = this.data.detailData || {}
    return {
      title: detail.title || '红芯成长',
      path: `/pages/activityDetail/activityDetail?id=${this.data.id}`
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


