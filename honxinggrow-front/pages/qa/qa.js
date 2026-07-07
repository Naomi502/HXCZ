const request = require('../../utils/request.js')
const app = getApp()

Page({
  data: {
    // --- UI & State Data (Merged) ---
    msgList: [],
    inputValue: '',
    loading: false,
    userInfo: {},
    scrollTop: 0,

    // Navigation Bar (From original qa.js)
    statusBarHeight: 44,
    navBarHeight: 44,
    navHeight: 88,
    menuButtonTop: 32,
    menuButtonHeight: 32,
    inputBottom: 0,

    // Animation & Model
    runAnim: false,
    modelName: 'Qwen-Flash', // Default from root qa.js

    // Quick Questions (From original qa.js)
    showAllQuestions: false,
    quickQuestions: [
      { id: 1, question: '获得各类资助的学生应当履行哪些基本义务？' },
      { id: 2, question: '申请校内勤工助学岗位需要满足什么条件？' },
      { id: 3, question: '奖学金的评选标准是什么？' },
      { id: 4, question: '家庭突发变故能否中途申请资助？' }
    ],

    // Internal State for Logic
    isReplying: false, // From root qa.js

    // TTS 状态
    ttsEnabled: true,  // 默认开启
    ttsPlaying: false,
    ttsCurrentMsgId: null, // 当前播放TTS的消息ID

    // 在线状态
    isOnline: true, // 默认在线

    // 流式响应状态
    streamFinished: false, // 流式响应是否完成

    // 底部距离配置 (单位 rpx)
    iosBottom: 100,
    androidBottom: 150,
    isIos: true,
  },

  // --- Lifecycle Methods ---

  onLoad() {
    const sysInfo = wx.getSystemInfoSync()
    // 使用 system 判断更准确，兼容开发者工具模拟器
    const isIos = sysInfo.system.toLowerCase().includes('ios')
    
    this.setData({
      userInfo: app.globalData.userInfo || {},
      isIos: isIos
    })
    this.initNavBar()
    this.getAiModelName()
    
    // 初始化TTS变量
    this.ttsSequence = 0
    this.nextPlaySequence = 0
    this.pendingTtsCount = 0
    this.audioQueue = []
    this.remainingSegments = []
    this.currentSegmentIndex = 0
    this.isPlayingAudio = false
    this._playingLock = false
    
    console.log('[TTS] TTS变量已初始化')
    
    // 初始化音频播放器（单例）
    this.audioContext = wx.createInnerAudioContext()
    this.audioContext.onEnded(() => {
      console.log('[TTS] ✓ 音频播放完成')
      this.isPlayingAudio = false
      this._playingLock = false
      
      // 增加下一个播放序号
      this.nextPlaySequence++
      
      // 提前发送下一段TTS（在播放当前段的同时生成下一段）
      setTimeout(() => {
        this.sendNextRemainingSegment()
      }, 300)
      
      // 播放下一个
      setTimeout(() => {
        this.playNextAudio()
      }, 200)
    })
    this.audioContext.onError((err) => {
      console.error('[TTS] ✗ 音频播放失败:', err)
      this.isPlayingAudio = false
      this._playingLock = false
      
      // 如果是音频实例未设置的错误，跳过
      if (err.errCode === -1) {
        console.log('[TTS] 跳过音频播放错误，继续下一个')
      }
      
      // 继续播放下一个
      setTimeout(() => {
        this.playNextAudio()
      }, 200)
    })
    this.audioContext.onPlay(() => {
      console.log('[TTS] ▶ 音频播放中...')
    })
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 2
      })
    }
    // Update user info
    const userInfo = app.globalData.userInfo || {}
    const localAvatar = wx.getStorageSync('userAvatar')
    if (localAvatar) {
        userInfo.localAvatar = localAvatar
    }
    
    // Animation logic from original qa.js
    const appInstance = getApp()
    if (this.animTimer) clearTimeout(this.animTimer)

    if (appInstance.globalData.isTabSwitch) {
      this.setData({ runAnim: false, userInfo }, () => {
        this.animTimer = setTimeout(() => {
          this.setData({ runAnim: true })
        }, 50)
      })
      appInstance.globalData.isTabSwitch = false
    } else {
      this.setData({ runAnim: true, userInfo })
    }
  },

  onHide() {
    if (this.animTimer) clearTimeout(this.animTimer)
    this.setData({ runAnim: false })
  },

  onUnload() {
    this.stopStreaming()

    // 清理定时器
    if (this.typeWriterTimer) {
      clearInterval(this.typeWriterTimer)
      this.typeWriterTimer = null
    }
    if (this.animTimer) {
      clearTimeout(this.animTimer)
      this.animTimer = null
    }

    // 清理音频播放器
    if (this.audioContext) {
      this.audioContext.stop()
      this.audioContext.destroy()
      this.audioContext = null
    }
  },

  // --- UI Methods ---

  initNavBar() {
    // Original qa.js logic for better UI fit
    const sysInfo = wx.getSystemInfoSync()
    const menuButton = wx.getMenuButtonBoundingClientRect()
    const statusBarHeight = sysInfo.statusBarHeight
    const navBarHeight = menuButton.height + (menuButton.top - statusBarHeight) * 2
    // 计算标题栏总高度：状态栏 + 卡片padding(8rpx) + 卡片内容(约200rpx) + 卡片padding(16rpx)
    // 转换为 px：200rpx ≈ 100px (以 750rpx = 375px 为基准)
    const cardContentHeight = 200 // rpx 单位，实际内容高度
    const cardPaddingTop = 8
    const cardPaddingBottom = 16
    const rpxToPx = sysInfo.windowWidth / 750
    const navTotalHeight = statusBarHeight + (cardContentHeight + cardPaddingTop + cardPaddingBottom) * rpxToPx
    
    this.setData({
      statusBarHeight,
      navBarHeight,
      navHeight: navTotalHeight,
      menuButtonTop: menuButton.top,
      menuButtonHeight: menuButton.height
    })
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

  handleBack() {
    wx.navigateBack()
  },

  onInput(e) {
    this.setData({
      inputValue: e.detail.value
    })
  },

  scrollToBottom() {
    // 使用 scrollTop 方式精确滚动到底部
    const query = wx.createSelectorQuery().in(this)
    query.select('.chat-scroll').boundingClientRect()
    query.select('.chat-content').boundingClientRect()
    query.exec((res) => {
      if (res[0] && res[1]) {
        const scrollViewHeight = res[0].height
        const contentHeight = res[1].height
        const scrollTop = contentHeight - scrollViewHeight + 100 // 额外偏移确保完全显示
        if (scrollTop > 0) {
          this.setData({ scrollTop: scrollTop })
        }
      }
    })
  },

  toggleQuickQuestions() {
    this.setData({ showAllQuestions: !this.data.showAllQuestions })
  },

  resetChat() {
    wx.showModal({
      title: '提示',
      content: '确定要清空对话记录吗？',
      success: (res) => {
        if (res.confirm) {
          this.stopStreaming()
          this.setData({
            msgList: [],
            inputValue: '',
            loading: false,
            isReplying: false
          })
        }
      }
    })
  },

  simpleMarkdownToHtml(text) {
    if (!text) return ''
    let html = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/`(.+?)`/g, '<code>$1</code>')
      .replace(/\n/g, '<br/>')
    return html
  },

  // --- Chat Logic (From Root qa.js) ---

  typeWriter(fullText, msgId) {
    const that = this
    let i = 0
    const msgList = this.data.msgList
    const targetMsgIndex = msgList.findIndex(item => item.id === msgId)
    
    if (targetMsgIndex === -1) return

    msgList[targetMsgIndex].content = ''
    msgList[targetMsgIndex].contentHtml = ''
    
    this.setData({ 
        msgList,
        isReplying: true 
    })

    this.typeWriterTimer = setInterval(() => {
      if (i < fullText.length) {
        const char = fullText.charAt(i)
        const currentContent = this.data.msgList[targetMsgIndex].content + char
        const keyContent = `msgList[${targetMsgIndex}].content`
        const keyHtml = `msgList[${targetMsgIndex}].contentHtml`
        
        this.setData({
          [keyContent]: currentContent,
          [keyHtml]: this.simpleMarkdownToHtml(currentContent)
        })
        i++
        if (i % 5 === 0) that.scrollToBottom()
      } else {
        clearInterval(this.typeWriterTimer)
        this.typeWriterTimer = null
        this.setData({ isReplying: false })
        that.scrollToBottom()
        
        // 触发TTS（非流式模式）
        console.log('[TTS] typeWriter 完成，触发TTS')
        that.finishStream()
      }
    }, 30)
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
    const keyContent = `msgList[${index}].content`
    const keyHtml = `msgList[${index}].contentHtml`
    
    this.setData({ 
        [keyContent]: next, 
        [keyHtml]: this.simpleMarkdownToHtml(next),
        loading: false 
    })

    // 不在流式输出时检测第一句，等输出完成后统一处理

    this.streamCharCount += delta.length
    if (this.streamCharCount % 12 === 0) {
      this.scrollToBottom()
    }
  },

  finishStream() {
    this.setData({ isReplying: false, loading: false, streamFinished: true })
    this.scrollToBottom()

    // TTS 处理：AI输出完成后，一次性处理
    console.log('[TTS] finishStream 被调用, ttsEnabled:', this.data.ttsEnabled)
    if (this.data.ttsEnabled) {
      const msgList = this.data.msgList
      console.log('[TTS] msgList 长度:', msgList ? msgList.length : 0)
      if (msgList && msgList.length > 0) {
        const lastMsg = msgList[msgList.length - 1]
        console.log('[TTS] lastMsg:', lastMsg ? `role=${lastMsg.role}, content长度=${lastMsg.content ? lastMsg.content.length : 0}` : 'null')
        if (lastMsg && lastMsg.role === 'ai' && lastMsg.content) {
          const fullText = lastMsg.content
          
          console.log(`[TTS] AI输出完成，开始处理 (${fullText.length}字)`)
          
          // 检测第一句
          let firstSentence = null
          let remainingText = fullText
          
          // 策略1：查找第一个换行符
          const firstLineBreak = fullText.indexOf('\n')
          if (firstLineBreak > 0 && firstLineBreak <= 50) {
            firstSentence = fullText.substring(0, firstLineBreak).trim()
            remainingText = fullText.substring(firstLineBreak).trim()
          } else {
            // 策略2：查找第一个句子结束符
            const sentenceMatch = fullText.match(/^(.+?[。！？.!?])/)
            if (sentenceMatch && sentenceMatch[1].length >= 8 && sentenceMatch[1].length <= 50) {
              firstSentence = sentenceMatch[1].trim()
              remainingText = fullText.substring(firstSentence.length).trim()
            } else if (fullText.length >= 30) {
              // 策略3：固定30字
              firstSentence = fullText.substring(0, 30).trim()
              remainingText = fullText.substring(30).trim()
            }
          }
          
          if (firstSentence && firstSentence.length >= 8) {
            console.log(`[TTS] 第一句: ${firstSentence}`)
            console.log(`[TTS] 剩余: ${remainingText.length}字`)
            
            // 准备剩余文本的分段（不立即发送）
            if (remainingText.length > 0) {
              this.prepareRemainingSegments(remainingText)
            }
            
            // 发送第一句TTS
            this.sendTtsRequest(firstSentence, true)
            
            // 延迟500ms后发送第一个剩余段落（避免触发限流）
            setTimeout(() => {
              this.sendNextRemainingSegment()
            }, 500)
          } else {
            // 文本太短，直接全部发送
            console.log('[TTS] 文本太短或无法分句，直接发送全部')
            const textToSend = fullText.length > 200 ? fullText.substring(0, 200) : fullText
            this.sendTtsRequest(textToSend, true)
          }
        } else {
          console.log('[TTS] ✗ 跳过TTS: lastMsg不符合条件')
        }
      } else {
        console.log('[TTS] ✗ 跳过TTS: msgList为空')
      }
    } else {
      console.log('[TTS] ✗ TTS已禁用')
    }
  },

  // 准备剩余文本的分段（不发送）
  prepareRemainingSegments(remainingText) {
    const maxLength = 100 // 每段100字左右
    const segments = []
    
    let start = 0
    while (start < remainingText.length) {
      let end = start + maxLength
      
      if (end >= remainingText.length) {
        segments.push(remainingText.substring(start).trim())
        break
      }
      
      // 尝试在换行符处分割
      const chunk = remainingText.substring(start, end)
      const lastLineBreak = chunk.lastIndexOf('\n')
      
      if (lastLineBreak > maxLength * 0.5) {
        end = start + lastLineBreak
      } else {
        const lastPuncIndex = Math.max(
          chunk.lastIndexOf('。'),
          chunk.lastIndexOf('！'),
          chunk.lastIndexOf('？'),
          chunk.lastIndexOf('.'),
          chunk.lastIndexOf('!'),
          chunk.lastIndexOf('?')
        )
        
        if (lastPuncIndex > maxLength * 0.5) {
          end = start + lastPuncIndex + 1
        }
      }
      
      segments.push(remainingText.substring(start, end).trim())
      start = end
    }
    
    // 保存分段
    this.remainingSegments = segments.filter(s => s.length > 0)
    this.currentSegmentIndex = 0
    
    console.log(`[TTS] 准备了 ${this.remainingSegments.length} 个剩余段落`)
  },

  // 发送下一个剩余段落
  sendNextRemainingSegment() {
    if (!this.remainingSegments || this.currentSegmentIndex >= this.remainingSegments.length) {
      return false
    }
    
    const segment = this.remainingSegments[this.currentSegmentIndex]
    this.currentSegmentIndex++
    
    console.log(`[TTS] 发送剩余段落 ${this.currentSegmentIndex}/${this.remainingSegments.length}`)
    this.sendTtsRequest(segment, false)
    
    return true
  },

  // 分段发送剩余文本（废弃，改用按需发送避免限流）
  sendRemainingTextTts_OLD(remainingText) {
    console.log(`[TTS] >>> sendRemainingTextTts 被调用，文本长度: ${remainingText.length}`)
    
    const maxLength = 100 // 减小到100字，加快TTS响应速度
    const segments = []
    
    let start = 0
    while (start < remainingText.length) {
      let end = start + maxLength
      
      if (end >= remainingText.length) {
        // 最后一段
        segments.push(remainingText.substring(start).trim())
        break
      }
      
      // 尝试在换行符处分割
      const chunk = remainingText.substring(start, end)
      const lastLineBreak = chunk.lastIndexOf('\n')
      
      if (lastLineBreak > maxLength * 0.5) {
        // 在换行符处分割（至少要有一半长度）
        end = start + lastLineBreak
      } else {
        // 尝试在句子结束符处分割
        const lastPuncIndex = Math.max(
          chunk.lastIndexOf('。'),
          chunk.lastIndexOf('！'),
          chunk.lastIndexOf('？'),
          chunk.lastIndexOf('.'),
          chunk.lastIndexOf('!'),
          chunk.lastIndexOf('?')
        )
        
        if (lastPuncIndex > maxLength * 0.5) {
          end = start + lastPuncIndex + 1
        }
      }
      
      segments.push(remainingText.substring(start, end).trim())
      start = end
    }
    
    console.log(`[TTS] 剩余文本分为 ${segments.length} 段，立即并行发送`)
    console.log(`[TTS] 段落详情:`, segments.map((s, i) => `#${i}: ${s.length}字`))
    
    // 立即并行发送所有段落
    segments.forEach((segment, index) => {
      if (segment.length > 0) {
        console.log(`[TTS] >>> 发送剩余段落 #${index}: ${segment.length}字`)
        this.sendTtsRequest(segment, false)
      }
    })
  },

  handleStreamError(message) {
    this.setData({ isReplying: false, loading: false })
    const errorMsg = {
      id: Date.now() + 1,
      role: 'ai',
      content: message || '网络开小差了，请稍后再试。',
      contentHtml: this.simpleMarkdownToHtml(message || '网络开小差了，请稍后再试。')
    }
    const newMsgList = [...this.data.msgList, errorMsg]
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
    }).catch(() => {
      this.handleStreamError('网络开小差了，请稍后再试。')
    })
  },

  onQuickQuestion(e) {
    const question = e.currentTarget.dataset.question
    this.doSend(question)
  },

  onSend() {
    this.doSend(this.data.inputValue)
  },

  doSend(content) {
    content = (content || '').trim()
    
    if (this.data.isReplying) {
        this.stopStreaming()
        this.setData({
          isReplying: false,
          loading: false
        })
        wx.showToast({ title: '已中断回答', icon: 'none' })
        
        if (!content) {
            return
        }
    }

    if (!content || this.data.loading) return

    // 重置TTS状态
    this.ttsSequence = 0
    this.nextPlaySequence = 0
    this.pendingTtsCount = 0
    this.remainingSegments = []
    this.currentSegmentIndex = 0
    this.setData({ streamFinished: false })
    this.stopAllTts()

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
    
    setTimeout(() => {
      this.scrollToBottom()
    }, 100)

    const aiMsgId = Date.now() + 1
    const aiMsg = {
      id: aiMsgId,
      role: 'ai',
      content: '',
      contentHtml: ''
    }
    const newMsgList = [...this.data.msgList, aiMsg]
    this.setData({
      msgList: newMsgList,
      isReplying: true
    })
    this.currentAiMsgIndex = newMsgList.length - 1
    
    console.log('[TTS] 使用流式模式')
    this.startStream(content)
  },

  // --- TTS 功能（待实现新方案）---

  // --- 两段式 TTS 功能 ---

  // TTS 状态变量
  audioQueue: [],            // 音频播放队列
  isPlayingAudio: false,     // 是否正在播放
  audioContext: null,        // 音频播放器实例
  pendingTtsCount: 0,        // 待处理的TTS请求数量
  _playingLock: false,       // 播放锁
  ttsSequence: 0,            // TTS序列号
  nextPlaySequence: 0,       // 下一个播放序号
  remainingSegments: [],     // 剩余段落（待发送）
  currentSegmentIndex: 0,    // 当前段落索引

  // 切换TTS开关
  toggleTts() {
    const newState = !this.data.ttsEnabled
    this.setData({ ttsEnabled: newState })
    
    console.log(`[TTS] TTS开关切换: ${newState ? '开启' : '关闭'}`)
    
    if (newState) {
      wx.showToast({ title: '语音播报已开启', icon: 'success' })
    } else {
      wx.showToast({ title: '语音播报已关闭', icon: 'none' })
      this.stopAllTts()
    }
  },

  // 停止所有TTS
  stopAllTts() {
    // 停止当前播放
    if (this.audioContext) {
      this.audioContext.stop()
    }
    
    // 清空队列
    this.audioQueue = []
    this.isPlayingAudio = false
    this.setData({ ttsPlaying: false })
  },

  // 检测并发送第一句
  detectAndSendFirstSentence(fullText) {
    let firstSentence = null
    
    // 策略1：查找第一个换行符
    const firstLineBreak = fullText.indexOf('\n')
    if (firstLineBreak > 0 && firstLineBreak <= 50) {
      // 第一行不超过50字，使用第一行作为第一句
      firstSentence = fullText.substring(0, firstLineBreak).trim()
    } else {
      // 策略2：查找第一个句子结束符
      const sentenceMatch = fullText.match(/^(.+?[。！？.!?])/)
      
      if (sentenceMatch && sentenceMatch[1].length >= 8 && sentenceMatch[1].length <= 50) {
        firstSentence = sentenceMatch[1].trim()
      } else if (fullText.length >= 30) {
        // 策略3：固定30字
        firstSentence = fullText.substring(0, 30).trim()
      } else if (fullText.length >= 15) {
        // 策略4：至少15字
        firstSentence = fullText.substring(0, 15).trim()
      }
    }
    
    if (firstSentence && firstSentence.length >= 8) {
      this.firstSentenceSent = true
      this.firstSentenceText = firstSentence
      
      console.log('[TTS] ✓ 检测到第一段:', firstSentence)
      
      // 立即发送第一句进行TTS
      this.sendTtsRequest(firstSentence, true)
    }
  },

  // 发送TTS请求
  sendTtsRequest(text, isFirstSentence) {
    console.log(`[TTS] >>> sendTtsRequest 被调用: text长度=${text.length}, isFirstSentence=${isFirstSentence}`)
    
    // 阿里云TTS限制，使用保守值
    const maxLength = 200
    
    if (text.length > maxLength) {
      console.warn(`[TTS] 文本超过${maxLength}字，截断为${maxLength}字`)
      text = text.substring(0, maxLength)
    }

    const token = wx.getStorageSync('token')
    console.log(`[TTS] Token存在: ${token ? '是' : '否'}`)
    
    // 分配序列号
    const sequence = this.ttsSequence++
    
    // 增加待处理计数
    this.pendingTtsCount++
    
    console.log(`[TTS] → 发送TTS #${sequence} ${isFirstSentence ? '(第一句)' : '(剩余)'} ${text.length}字 [待处理: ${this.pendingTtsCount}]`)
    console.log(`[TTS] API URL: ${app.globalData.apiBaseUrl}/hx/ai/tts`)
    console.log(`[TTS] Request data:`, { text, voice: 'Cherry', model: 'qwen3-tts-flash' })
    
    wx.request({
      url: `${app.globalData.apiBaseUrl}/hx/ai/tts`,
      method: 'POST',
      dataType: 'json',
      header: {
        'Authorization': token.startsWith('Bearer ') ? token : `Bearer ${token}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      data: {
        text: text,
        voice: 'Cherry',
        model: 'qwen3-tts-flash'
      },
      success: (res) => {
        this.pendingTtsCount--
        
        console.log(`[TTS] 收到响应 #${sequence}:`, res)
        
        if (res.data.code === 200) {
          const audioUrl = res.data.data.audioUrl
          
          console.log(`[TTS] ✓ TTS成功 #${sequence}, audioUrl: ${audioUrl}`)
          
          // 添加到队列
          this.audioQueue.push({
            url: audioUrl,
            sequence: sequence
          })
          
          // 按序号排序
          this.audioQueue.sort((a, b) => a.sequence - b.sequence)
          
          console.log(`[TTS] ✓ TTS成功 #${sequence} [队列: ${this.audioQueue.length}, 待处理: ${this.pendingTtsCount}]`)
          
          // 尝试播放
          if (!this.isPlayingAudio) {
            this.playNextAudio()
          }
        } else {
          console.error(`[TTS] ✗ TTS失败 #${sequence}:`, res.data.msg)
        }
        
        this.checkTtsComplete()
      },
      fail: (err) => {
        this.pendingTtsCount--
        console.error(`[TTS] ✗ TTS请求失败 #${sequence}:`, err)
        this.checkTtsComplete()
      }
    })
  },

  // 检查TTS是否全部完成
  checkTtsComplete() {
    if (this.streamFinished && this.pendingTtsCount === 0 && this.audioQueue.length === 0 && !this.isPlayingAudio) {
      console.log('[TTS] ✓ 所有TTS处理完成')
    }
  },

  // 处理超长文本（按换行分段发送TTS）
  sendLongTextTts(longText) {
    console.log(`[TTS] 文本过长(${longText.length}字)，按换行分段处理`)
    
    const maxLength = 280 // 每段最多280字
    
    // 先按换行符分割
    const paragraphs = longText.split(/\n+/).filter(p => p.trim().length > 0)
    
    console.log(`[TTS] 按换行分为 ${paragraphs.length} 个段落`)
    
    const segments = []
    
    // 处理每个段落
    for (let para of paragraphs) {
      para = para.trim()
      
      if (para.length <= maxLength) {
        // 段落不超长，直接添加
        segments.push(para)
      } else {
        // 段落超长，需要再分割
        let start = 0
        while (start < para.length) {
          let end = start + maxLength
          
          if (end >= para.length) {
            segments.push(para.substring(start).trim())
            break
          }
          
          // 尝试在句子结束符处分割
          const chunk = para.substring(start, end)
          const lastPuncIndex = Math.max(
            chunk.lastIndexOf('。'),
            chunk.lastIndexOf('！'),
            chunk.lastIndexOf('？'),
            chunk.lastIndexOf('.'),
            chunk.lastIndexOf('!'),
            chunk.lastIndexOf('?')
          )
          
          if (lastPuncIndex > 0) {
            end = start + lastPuncIndex + 1
          }
          
          segments.push(para.substring(start, end).trim())
          start = end
        }
      }
    }
    
    console.log(`[TTS] 最终分为 ${segments.length} 段:`, segments.map(s => s.length + '字'))
    
    // 依次发送每段
    segments.forEach((segment, index) => {
      if (segment.length > 0) {
        this.sendTtsRequest(segment, false)
      }
    })
  },

  // 播放下一个音频
  playNextAudio() {
    // 防止重入
    if (this._playingLock) {
      return
    }

    // 如果正在播放，不要重复调用
    if (this.isPlayingAudio) {
      return
    }

    // 查找下一个应该播放的音频
    const nextIndex = this.audioQueue.findIndex(item => item.sequence === this.nextPlaySequence)
    
    if (nextIndex === -1) {
      this.isPlayingAudio = false
      this.setData({ ttsPlaying: false })
      
      // 如果还有待处理的TTS请求，等待一下
      if (this.pendingTtsCount > 0) {
        console.log(`[TTS] 等待 #${this.nextPlaySequence}... (待处理: ${this.pendingTtsCount})`)
        setTimeout(() => {
          this.playNextAudio()
        }, 200) // 缩短到200ms
      } else {
        console.log('[TTS] ✓ 所有音频播放完成')
      }
      return
    }

    // 加锁
    this._playingLock = true
    
    const audioItem = this.audioQueue.splice(nextIndex, 1)[0]
    this.isPlayingAudio = true
    this.setData({ ttsPlaying: true })

    console.log(`[TTS] ▶ 开始播放 #${audioItem.sequence} (队列剩余: ${this.audioQueue.length})`)

    // 使用单例音频播放器
    this.audioContext.src = audioItem.url
    
    // 解锁
    this._playingLock = false
    
    this.audioContext.play()
  }
})
