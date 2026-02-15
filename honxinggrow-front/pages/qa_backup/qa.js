const request = require('../../utils/request.js')
const app = getApp()

Page({
  data: {
    msgList: [],
    inputValue: '',
    loading: false,
    userInfo: {},
    scrollIntoView: '',
    capsuleTop: 44, // 默认胶囊高度
    navHeight: 88, // 默认导航栏高度
    capsuleHeight: 32, // 默认胶囊高度
    runAnim: false,
    modelName: 'glm-4.6v-flash', // 默认模型名称
    isMuted: false,
    isReplying: false,
    isSpeaking: false // 是否正在朗读
  },

  onLoad() {
    this.setData({
      userInfo: app.globalData.userInfo || {}
    })
    this.initAudio()
    this.initNavBar()
    this.getAiModelName()
  },

  getAiModelName() {
    request.request({
      url: '/hx/ai/model',
      method: 'GET',
      hideLoading: true
    }).then(res => {
        if (res.data) {
            this.setData({ modelName: res.data })
        }
    }).catch(err => {
        console.error('Failed to get AI model name', err)
    })
  },

  onShow() {
    if (typeof this.getTabBar === 'function' &&
      this.getTabBar()) {
      this.getTabBar().setData({
        selected: 2 // 问答在 tab list 中的索引，假设是第3个（索引2）
      })
    }
    // 更新用户信息，确保头像一致
    const userInfo = app.globalData.userInfo || {}
    // 优先读取本地存储的头像
    const localAvatar = wx.getStorageSync('userAvatar')
    if (localAvatar) {
        userInfo.localAvatar = localAvatar
    }
    
    this.setData({
      userInfo: userInfo,
      runAnim: true
    })
  },

  onHide() {
    this.setData({ runAnim: false })
  },

  onUnload() {
    this.stopStreaming()
    this.stopAllAudio()
  },

  initNavBar() {
    const menuButton = wx.getMenuButtonBoundingClientRect()
    
    // 胶囊底部到底部的间距 = 胶囊顶部到状态栏底部的间距
    // 导航栏高度 = 胶囊底部 + 间距
    // 这里简单处理：让标题栏高度与胶囊对齐
    this.setData({
      capsuleTop: menuButton.top,
      capsuleHeight: menuButton.height,
      navHeight: menuButton.bottom + 10 // 留一点 padding
    })
  },

  handleBack() {
    wx.navigateBack()
  },

  onInput(e) {
    this.setData({
      inputValue: e.detail.value
    })
  },

  toggleMute() {
    const nextMuted = !this.data.isMuted
    this.setData({ isMuted: nextMuted })
    if (nextMuted) {
      this.stopAllAudio()
    } else {
      this.flushTtsBuffer(true)
      this.playNextTts()
    }
  },

  scrollToBottom() {
    this.setData({
      scrollIntoView: 'bottom-anchor'
    })
  },

  initAudio() {
    this.ttsQueue = []
    this.ttsPlaying = false
    this.pendingTtsText = ''
    this.streamBuffer = ''
    this.streamCharCount = 0
    this.audioContext = wx.createInnerAudioContext()
    if (this.audioContext.setObeyMuteSwitch) {
      this.audioContext.setObeyMuteSwitch(false) // 即使静音模式也播放声音
    }
    this.audioContext.onEnded(() => {
      this.ttsPlaying = false
      this.setData({ isSpeaking: false })
      this.playNextTts()
    })
    this.audioContext.onStop(() => {
      this.ttsPlaying = false
      this.setData({ isSpeaking: false })
    })
    this.audioContext.onError(() => {
      this.ttsPlaying = false
      this.setData({ isSpeaking: false })
      this.playNextTts()
    })
  },

  stopAllAudio() {
    if (this.audioContext) {
      this.audioContext.stop()
    }
    this.ttsQueue = []
    this.ttsPlaying = false
    this.pendingTtsText = ''
    this.setData({ isSpeaking: false })
  },

  enqueueTts(text) {
    const cleaned = (text || '').trim()
    if (!cleaned || this.data.isMuted) return
    this.ttsQueue.push(cleaned)
    this.playNextTts()
  },

  playNextTts() {
    if (this.ttsPlaying || this.data.isMuted) return
    const nextText = this.ttsQueue.shift()
    if (!nextText) return
    this.ttsPlaying = true
    this.setData({ isSpeaking: true })
    
    // 统一使用 request.request。
    // 在生产环境下(useCloud: true)，request.request 会自动调用 callApi 云函数转发到后端 /hx/ai/tts
    request.request({
      url: '/hx/ai/tts',
      method: 'POST',
      data: {
        text: nextText
      },
      hideLoading: true
    }).then(res => {
      if (this.data.isMuted) {
        this.ttsPlaying = false
        this.setData({ isSpeaking: false })
        return
      }
      
      // 注意：后端返回的数据结构可能是 res.data.audioBase64
      const audioBase64 = res.data?.audioBase64 || res.audioBase64
      const audioUrl = res.data?.audioUrl || res.audioUrl
      
      if (audioBase64) {
        this.handleTtsSuccess(audioBase64)
        return
      }
      if (audioUrl) {
        this.audioContext.src = audioUrl
        this.audioContext.play()
        return
      }
      console.warn('No audio data received from TTS API')
      this.handleTtsError()
    }).catch(err => {
      console.error('TTS request failed:', err)
      this.handleTtsError()
    })
  },

  handleTtsSuccess(audioBase64) {
    if (this.data.isMuted) {
      this.ttsPlaying = false
      this.setData({ isSpeaking: false })
      return
    }
    const filePath = `${wx.env.USER_DATA_PATH}/tts_${Date.now()}_${Math.floor(Math.random() * 10000)}.mp3`
    const fs = wx.getFileSystemManager()
    fs.writeFile({
      filePath,
      data: audioBase64,
      encoding: 'base64',
      success: () => {
        if (this.data.isMuted) {
          this.ttsPlaying = false
          this.setData({ isSpeaking: false })
          return
        }
        this.audioContext.src = filePath
        this.audioContext.play()
      },
      fail: (err) => {
        console.error('Write TTS file failed:', err)
        this.handleTtsError()
      }
    })
  },

  handleTtsError() {
    this.ttsPlaying = false
    this.setData({ isSpeaking: false })
    this.playNextTts()
  },

  flushTtsBuffer(force) {
    if (this.data.isMuted) {
      this.pendingTtsText = ''
      return
    }
    const text = this.pendingTtsText || ''
    if (!text) return
    if (force) {
      this.pendingTtsText = ''
      this.enqueueTts(text)
      return
    }
    const regex = /[^。！？!?；;]+[。！？!?；;]+/g
    let match
    let lastIndex = 0
    while ((match = regex.exec(text)) !== null) {
      const segment = match[0]
      if (segment && segment.trim().length >= 4) {
        this.enqueueTts(segment)
      }
      lastIndex = regex.lastIndex
    }
    this.pendingTtsText = text.slice(lastIndex)
  },

  // 打字机效果函数
  typeWriter(fullText, msgId) {
    const that = this
    let i = 0
    const msgList = this.data.msgList
    const targetMsgIndex = msgList.findIndex(item => item.id === msgId)
    
    if (targetMsgIndex === -1) return

    // 初始清空内容
    msgList[targetMsgIndex].content = ''
    
    // 设置正在回复状态，用于控制“中断”逻辑
    this.setData({ 
        msgList,
        isReplying: true // 标记为正在打字
    })

    // 定时器逐字显示
    // 将 timer 存储在实例上，以便中断
    this.typeWriterTimer = setInterval(() => {
      if (i < fullText.length) {
        const char = fullText.charAt(i)
        const key = `msgList[${targetMsgIndex}].content`
        this.setData({
          [key]: this.data.msgList[targetMsgIndex].content + char
        })
        i++
        // 每输出几个字就滚动到底部，保持视觉跟随
        if (i % 5 === 0) that.scrollToBottom()
      } else {
        clearInterval(this.typeWriterTimer)
        this.typeWriterTimer = null
        this.setData({ isReplying: false }) // 打字结束
        that.scrollToBottom() // 结束后再次滚动到底部
      }
    }, 30) // 打字速度：30ms/字
  },

  decodeChunk(buffer) {
    if (typeof TextDecoder !== 'undefined') {
      return new TextDecoder('utf-8').decode(buffer)
    }
    const bytes = new Uint8Array(buffer)
    let binary = ''
    for (let i = 0; i < bytes.length; i++) {
      binary += String.fromCharCode(bytes[i])
    }
    return decodeURIComponent(escape(binary))
  },

  startStream(question) {
    this.stopStreaming()
    this.streamBuffer = ''
    this.streamCharCount = 0
    this.pendingTtsText = ''
    const baseUrl = app.globalData.apiBaseUrl || 'http://localhost:8080'
    const token = wx.getStorageSync('token')
    const header = {
      'Content-Type': 'application/json'
    }
    if (token) {
      header.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }
    this.streamRequestTask = wx.request({
      url: `${baseUrl}/hx/ai/chat/stream`,
      method: 'POST',
      data: { question },
      header,
      enableChunked: true,
      responseType: 'text',
      success: (res) => {
        if (res.statusCode !== 200) {
          console.warn('Stream request failed with status:', res.statusCode)
          this.fallbackToNonStream(question)
        }
      },
      fail: (err) => {
        console.warn('Stream request failed:', err)
        // 在生产环境，wx.request 到 IP 地址通常会失败，直接回退到非流式（云函数）
        this.fallbackToNonStream(question)
      }
    })

    if (this.streamRequestTask && this.streamRequestTask.onChunkReceived) {
      this.streamRequestTask.onChunkReceived(res => {
        const chunkText = this.decodeChunk(res.data)
        this.handleStreamText(chunkText)
      })
    } else {
      this.fallbackToNonStream(question)
    }
  },

  stopStreaming() {
    if (this.streamRequestTask && this.streamRequestTask.abort) {
      this.streamRequestTask.abort()
    }
    this.streamRequestTask = null
    this.streamBuffer = ''
  },

  handleStreamText(chunkText) {
    if (!chunkText) return
    this.streamBuffer += chunkText
    const parts = this.streamBuffer.split('\n\n')
    this.streamBuffer = parts.pop() || ''
    parts.forEach(part => {
      const lines = part.split('\n')
      lines.forEach(line => {
        const trimmed = line.trim()
        if (!trimmed.startsWith('data:')) return
        const data = trimmed.slice(5).trim()
        if (!data) return
        if (data === '[DONE]') {
          this.finishStream()
          return
        }
        this.handleStreamData(data)
      })
    })
  },

  handleStreamData(data) {
    try {
      const parsed = JSON.parse(data)
      if (parsed.error) {
        this.handleStreamError(parsed.error)
        return
      }
      if (parsed.delta) {
        this.appendAiContent(parsed.delta)
      }
    } catch (e) {}
  },

  appendAiContent(delta) {
    const index = this.currentAiMsgIndex
    if (index === undefined || index < 0) return
    const current = this.data.msgList[index]?.content || ''
    const next = current + delta
    const key = `msgList[${index}].content`
    this.setData({ [key]: next, loading: false })
    this.streamCharCount += delta.length
    this.pendingTtsText += delta
    if (this.streamCharCount % 12 === 0) {
      this.scrollToBottom()
    }
    this.flushTtsBuffer(false)
  },

  finishStream() {
    this.setData({ isReplying: false, loading: false })
    this.flushTtsBuffer(true)
    this.scrollToBottom()
  },

  handleStreamError(message) {
    this.setData({ isReplying: false, loading: false })
    const errorMsg = {
      id: Date.now() + 1,
      role: 'ai',
      content: message || '网络开小差了，请稍后再试。'
    }
    const newMsgList = this.data.msgList
    newMsgList.push(errorMsg)
    this.setData({ msgList: newMsgList })
    this.scrollToBottom()
  },

  fallbackToNonStream(question) {
    request.request({
      url: '/hx/ai/chat',
      method: 'POST',
      data: {
        question
      },
      hideLoading: true
    }).then(res => {
      let answer = res.data || '抱歉，我暂时无法回答。'
      answer = answer.trim()
      this.setData({ loading: false })
      this.typeWriter(answer, this.data.msgList[this.data.msgList.length - 1].id)
      this.enqueueTts(answer)
    }).catch(() => {
      this.handleStreamError('网络开小差了，请稍后再试。')
    })
  },

  onSend() {
    // 1. 如果正在打字（isReplying），点击发送键视为“中断”
    if (this.data.isReplying) {
        this.stopStreaming()
        this.stopAllAudio()
        this.setData({
          isReplying: false,
          loading: false
        })
        wx.showToast({ title: '已中断回答', icon: 'none' })
        
        // 中断后，如果输入框为空，就停止即可；如果输入框有内容，继续往下走发送新请求
        if (!this.data.inputValue.trim()) {
            return
        }
    }

    const content = this.data.inputValue.trim()
    if (!content || this.data.loading) return

    // 2. Add user message
    const msgList = this.data.msgList
    const userMsg = {
      id: Date.now(),
      role: 'user',
      content: content
    }
    msgList.push(userMsg)

    this.setData({
      msgList,
      inputValue: '',
      loading: true
    })
    
    // 延迟一小会儿滚动，确保视图渲染完成
    setTimeout(() => {
      this.scrollToBottom()
    }, 100)

    const aiMsgId = Date.now() + 1
    const aiMsg = {
      id: aiMsgId,
      role: 'ai',
      content: ''
    }
    const newMsgList = this.data.msgList
    newMsgList.push(aiMsg)
    this.setData({
      msgList: newMsgList,
      isReplying: true
    })
    this.currentAiMsgIndex = newMsgList.length - 1
    
    // 生产环境/体验版走云函数转发（不支持流式），开发环境走本地连接（支持流式）
    if (app.globalData.useCloud) {
      this.fallbackToNonStream(content)
    } else {
      this.startStream(content, aiMsgId)
    }
  }
})
