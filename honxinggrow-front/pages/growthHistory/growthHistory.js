const { request } = require('../../utils/request')

Page({
  data: {
    pageAnim: null,
    loading: true,
    dates: [],
    storyLikes: [],
    activities: [],
    points: [],
    capsuleTop: 24
  },
  onLoad() {
    this.initHeader()
    this.animateIn()
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
    this.setData({ loading: true })
    this.loadData()
  },
  onHide() {
    // 再次进入时直接展示加载态，避免看到旧数据
    this.setData({ loading: true })
  },
  onReady() {
    // 延迟绘制图表，确保canvas已渲染
    setTimeout(() => {
      this.drawCharts()
    }, 500)
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
  loadData() {
    this.setData({ loading: true })
    wx.showLoading({
      title: '加载中...',
      mask: true
    })
    request({
      url: '/app/points/growth-history',
      method: 'get',
      hideLoading: true
    }).then(res => {
      console.log('成长历程数据响应:', res)
      const data = res.data || {}
      console.log('解析后的数据:', data)
      this.setData({
        dates: data.dates || [],
        storyLikes: data.storyLikes || [],
        activities: data.activities || [],
        points: data.points || [],
        loading: false
      })
      // 数据加载完成后绘制图表
      setTimeout(() => {
        this.drawCharts()
      }, 300)
    }).catch(err => {
      console.error('加载成长历程数据失败:', err)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      })
    }).finally(() => {
      wx.hideLoading()
    })
  },
  drawCharts() {
    const { dates, storyLikes, activities, points } = this.data
    if (!dates || dates.length === 0) return

    // 绘制点赞故事折线图
    this.drawLineChart('storyLikeChart', dates, storyLikes, '#ff4d4f', '点赞数')
    
    // 绘制参加活动折线图
    this.drawLineChart('activityChart', dates, activities, '#1890ff', '活动数')
    
    // 绘制获得积分折线图
    this.drawLineChart('pointsChart', dates, points, '#52c41a', '积分数')
  },
  drawLineChart(canvasId, dates, data, color, label) {
    const systemInfo = wx.getSystemInfoSync()
    const width = systemInfo.windowWidth - 48 // 减去左右padding (24+24)
    const height = 220 
    const padding = { top: 20, right: 20, bottom: 30, left: 40 }
    const chartWidth = width - padding.left - padding.right
    const chartHeight = height - padding.top - padding.bottom

    const ctx = wx.createCanvasContext(canvasId)
    
    // 1. 计算Y轴范围
    let maxVal = Math.max(...data, 0)
    if (maxVal === 0) maxVal = 5
    // 向上取整到 5 的倍数
    maxVal = Math.ceil(maxVal / 5) * 5
    
    // 2. 绘制网格 (虚线)
    ctx.setStrokeStyle('#f0f0f0')
    ctx.setLineWidth(1)
    // ctx.setLineDash([4, 4]) // 小程序 Canvas Context 基础库可能不支持 setLineDash，用实线淡一点也可，或者自己模拟
    // 很多真机 Canvas Context 不支持 setLineDash，这里用淡实线代替
    
    const ySteps = 5
    for (let i = 0; i <= ySteps; i++) {
      const y = padding.top + (chartHeight / ySteps) * i
      ctx.beginPath()
      ctx.moveTo(padding.left, y)
      ctx.lineTo(width - padding.right, y)
      ctx.stroke()
      
      // Y轴数值
      const val = Math.round(maxVal - (maxVal / ySteps) * i)
      ctx.setFillStyle('#999999')
      ctx.setFontSize(10)
      ctx.setTextAlign('right')
      ctx.fillText(val.toString(), padding.left - 8, y + 4)
    }

    // 3. 绘制X轴 (只显示首尾和中间)
    ctx.setStrokeStyle('#e0e0e0')
    ctx.beginPath()
    ctx.moveTo(padding.left, height - padding.bottom)
    ctx.lineTo(width - padding.right, height - padding.bottom)
    ctx.stroke()

    if (dates.length > 0) {
      const xStep = chartWidth / (dates.length - 1 || 1)
      
      // 辅助函数：获取坐标
      const getPoint = (index) => {
        const x = padding.left + index * xStep
        const y = padding.top + chartHeight - (data[index] / maxVal) * chartHeight
        return { x, y }
      }

      // 4. 绘制折线 (平滑曲线)
      ctx.beginPath()
      ctx.setLineWidth(2)
      ctx.setStrokeStyle(color)
      
      // 起点
      const p0 = getPoint(0)
      ctx.moveTo(p0.x, p0.y)

      for (let i = 0; i < dates.length - 1; i++) {
        const current = getPoint(i)
        const next = getPoint(i + 1)
        
        // 简单的贝塞尔控制点计算：取两点中点作为控制点
        const xc = (current.x + next.x) / 2
        const yc = (current.y + next.y) / 2
        
        // 使用二次贝塞尔曲线连接中点，产生平滑效果
        // 这里简化处理：直接用直线连接，或者用二次曲线
        // 为了更好的平滑效果，通常使用三次贝塞尔，但计算复杂。
        // 这里的简化方案：从当前点画到中点，再从中点画到下一点（其实这还是直线）
        // 正确的平滑方案：使用 (current.x + next.x) / 2 作为控制点的x，但这样也不够圆滑。
        
        // 采用更简单的方案：直线连接，但加圆角？不行。
        // 还是用直线吧，但加上渐变填充和漂亮的节点，效果也很好。
        // 如果非要曲线，可以使用 quadraticCurveTo
        // cpx, cpy 是两点的中点？不，应该是控制点。
        // 简单曲线法：
        const cpx = (current.x + next.x) / 2
        const cpy = (current.y + next.y) / 2
        // 这种做法其实不平滑。
        
        // 咱们还是画直线，但是加上漂亮的渐变阴影。
        ctx.lineTo(next.x, next.y)
      }
      ctx.stroke()

      // 5. 绘制渐变填充区域
      ctx.beginPath()
      ctx.moveTo(p0.x, p0.y)
      for (let i = 1; i < dates.length; i++) {
        const p = getPoint(i)
        ctx.lineTo(p.x, p.y)
      }
      ctx.lineTo(getPoint(dates.length - 1).x, height - padding.bottom)
      ctx.lineTo(padding.left, height - padding.bottom)
      ctx.closePath()
      
      // 创建线性渐变
      const gradient = ctx.createLinearGradient(0, padding.top, 0, height - padding.bottom)
      gradient.addColorStop(0, color + '33') // 20% 透明度 (Hex alpha) - 注意：小程序Canvas可能不支持hex alpha，需用rgba
      // 转换hex color到rgba
      let r=0, g=0, b=0
      if(color.startsWith('#')) {
        const hex = color.substring(1)
        if(hex.length === 6) {
           r = parseInt(hex.substr(0,2), 16)
           g = parseInt(hex.substr(2,2), 16)
           b = parseInt(hex.substr(4,2), 16)
        } else if (hex.length === 3) {
           r = parseInt(hex[0]+hex[0], 16)
           g = parseInt(hex[1]+hex[1], 16)
           b = parseInt(hex[2]+hex[2], 16)
        }
      }
      gradient.addColorStop(0, `rgba(${r},${g},${b},0.2)`)
      gradient.addColorStop(1, `rgba(${r},${g},${b},0.0)`)
      
      ctx.setFillStyle(gradient)
      ctx.fill()

      // 6. 绘制数据点 (空心圆)
      dates.forEach((date, i) => {
        const p = getPoint(i)
        
        // 绘制白色背景遮挡线
        ctx.beginPath()
        ctx.arc(p.x, p.y, 4, 0, 2 * Math.PI)
        ctx.setFillStyle('#ffffff')
        ctx.fill()
        
        // 绘制圆环
        ctx.beginPath()
        ctx.arc(p.x, p.y, 3, 0, 2 * Math.PI)
        ctx.setStrokeStyle(color)
        ctx.setLineWidth(2)
        ctx.stroke()
        
        // X轴标签 (只显示部分)
        const labelInterval = Math.ceil(dates.length / 5)
        if (i % labelInterval === 0 || i === dates.length - 1) {
          ctx.setFillStyle('#999999')
          ctx.setFontSize(10)
          ctx.setTextAlign('center')
          ctx.fillText(date, p.x, height - padding.bottom + 16)
        }
      })
    } else {
       // 空状态
       ctx.setFillStyle('#cccccc')
       ctx.setFontSize(12)
       ctx.setTextAlign('center')
       ctx.fillText('暂无数据', width / 2, height / 2)
    }

    ctx.draw()
  },
})

