/**
 * 阿里云百炼 TTS Realtime 客户端
 * 微信小程序版本 - 直连阿里云服务
 */

class TtsRealtimeClient {
  constructor(options = {}) {
    this.apiKey = options.apiKey
    this.model = options.model || 'qwen3-tts-flash-realtime'
    this.voice = options.voice || 'Cherry'
    this.mode = options.mode || 'server_commit' // 或 'commit'
    this.sampleRate = options.sampleRate || 24000
    this.responseFormat = options.responseFormat || 'pcm'
    
    // 北京地域（中国内地）
    this.wsUrl = 'wss://dashscope.aliyuncs.com/api-ws/v1/realtime?model=' + this.model
    // 如果使用新加坡地域，使用：wss://dashscope-intl.aliyuncs.com/api-ws/v1/realtime?model=xxx
    
    this.socketTask = null
    this.isConnected = false
    this.audioChunks = []
    
    // 回调函数
    this.onAudioData = options.onAudioData || null
    this.onConnected = options.onConnected || null
    this.onError = options.onError || null
    this.onDone = options.onDone || null
  }

  /**
   * 连接到阿里云 TTS 服务
   */
  connect() {
    return new Promise((resolve, reject) => {
      console.log('[TTS] 连接到阿里云:', this.wsUrl)
      
      this.socketTask = wx.connectSocket({
        url: this.wsUrl,
        header: {
          'Authorization': 'Bearer ' + this.apiKey,
          'Content-Type': 'application/json'
        },
        success: () => {
          console.log('[TTS] WebSocket 连接请求已发送')
        },
        fail: (err) => {
          console.error('[TTS] WebSocket 连接失败:', err)
          reject(err)
        }
      })

      this.socketTask.onOpen(() => {
        console.log('[TTS] WebSocket 已连接')
        this.isConnected = true
        
        // 发送会话配置
        this.updateSession()
        resolve()
      })

      this.socketTask.onMessage((res) => {
        this.handleMessage(res.data)
      })

      this.socketTask.onError((err) => {
        console.error('[TTS] WebSocket 错误:', err)
        this.isConnected = false
        if (this.onError) {
          this.onError(err)
        }
      })

      this.socketTask.onClose((res) => {
        console.log('[TTS] WebSocket 已关闭:', res.code, res.reason)
        this.isConnected = false
      })
    })
  }

  /**
   * 更新会话配置
   */
  updateSession() {
    const event = {
      type: 'session.update',
      event_id: 'event_' + Date.now(),
      session: {
        mode: this.mode,
        voice: this.voice,
        response_format: this.responseFormat,
        sample_rate: this.sampleRate
      }
    }
    
    console.log('[TTS] 更新会话配置:', event)
    this.sendEvent(event)
  }

  /**
   * 发送事件到服务器
   */
  sendEvent(event) {
    if (!this.isConnected || !this.socketTask) {
      console.error('[TTS] 未连接，无法发送事件')
      return false
    }

    this.socketTask.send({
      data: JSON.stringify(event),
      success: () => {
        console.log('[TTS] 事件已发送:', event.type)
      },
      fail: (err) => {
        console.error('[TTS] 发送事件失败:', err)
      }
    })
    
    return true
  }

  /**
   * 追加文本到缓冲区
   */
  appendText(text) {
    const event = {
      type: 'input_text_buffer.append',
      event_id: 'event_' + Date.now(),
      text: text
    }
    
    console.log('[TTS] 发送文本:', text)
    return this.sendEvent(event)
  }

  /**
   * 提交文本缓冲区（commit 模式）
   */
  commit() {
    const event = {
      type: 'input_text_buffer.commit',
      event_id: 'event_' + Date.now()
    }
    
    console.log('[TTS] 提交文本缓冲区')
    return this.sendEvent(event)
  }

  /**
   * 结束会话
   */
  finish() {
    const event = {
      type: 'session.finish',
      event_id: 'event_' + Date.now()
    }
    
    console.log('[TTS] 结束会话')
    return this.sendEvent(event)
  }

  /**
   * 处理服务器消息
   */
  handleMessage(data) {
    try {
      const event = JSON.parse(data)
      const eventType = event.type

      if (eventType !== 'response.audio.delta') {
        console.log('[TTS] 收到事件:', eventType)
      }

      switch (eventType) {
        case 'session.created':
          console.log('[TTS] 会话已创建:', event.session.id)
          break

        case 'session.updated':
          console.log('[TTS] 会话已更新')
          if (this.onConnected) {
            this.onConnected()
          }
          break

        case 'response.audio.delta':
          // 接收音频数据
          const audioBase64 = event.delta
          this.audioChunks.push(audioBase64)
          
          if (this.onAudioData) {
            this.onAudioData(audioBase64)
          }
          break

        case 'response.done':
          console.log('[TTS] 响应完成')
          if (this.onDone) {
            this.onDone()
          }
          break

        case 'session.finished':
          console.log('[TTS] 会话已结束')
          break

        case 'error':
          const errorMsg = event.message || event.error?.message || '未知错误'
          console.error('[TTS] 服务器错误:', errorMsg)
          if (this.onError) {
            this.onError(errorMsg)
          }
          break

        default:
          console.log('[TTS] 未处理的事件类型:', eventType)
      }
    } catch (e) {
      console.error('[TTS] 解析消息失败:', e, data)
    }
  }

  /**
   * 关闭连接
   */
  close() {
    if (this.socketTask) {
      this.socketTask.close({
        code: 1000,
        reason: 'Client closed'
      })
      this.socketTask = null
      this.isConnected = false
    }
  }

  /**
   * 获取所有音频数据
   */
  getAllAudioChunks() {
    return this.audioChunks
  }

  /**
   * 清空音频缓存
   */
  clearAudioChunks() {
    this.audioChunks = []
  }
}

module.exports = TtsRealtimeClient
