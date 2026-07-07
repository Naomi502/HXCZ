/**
 * 实时音频播放器
 * 使用队列实现流式播放，确保音频连续不中断
 */

class RealtimeAudioPlayer {
  constructor(options = {}) {
    this.sampleRate = options.sampleRate || 24000
    this.format = options.format || 'mp3'
    this.audioQueue = []
    this.isPlaying = false
    this.currentAudio = null
    
    // 使用微信小程序的音频上下文
    this.innerAudioContext = wx.createInnerAudioContext()
    this.innerAudioContext.autoplay = false
    
    // 播放完成后自动播放下一个
    this.innerAudioContext.onEnded(() => {
      console.log('[AudioPlayer] 当前音频播放完成')
      this.isPlaying = false
      this.playNext()
    })
    
    // 播放错误时跳过
    this.innerAudioContext.onError((err) => {
      console.error('[AudioPlayer] 播放错误:', err)
      this.isPlaying = false
      this.playNext()
    })
    
    // 开始播放
    this.innerAudioContext.onPlay(() => {
      this.isPlaying = true
    })
  }

  /**
   * 添加音频块到队列
   */
  addAudioChunk(base64Audio) {
    const fs = wx.getFileSystemManager()
    const timestamp = Date.now()
    const filePath = `${wx.env.USER_DATA_PATH}/tts_${timestamp}.${this.format}`
    
    try {
      fs.writeFileSync(filePath, base64Audio, 'base64')
      this.audioQueue.push(filePath)
      
      // 如果当前没有播放，立即开始
      if (!this.isPlaying) {
        this.playNext()
      }
    } catch (err) {
      console.error('[AudioPlayer] 写入音频失败:', err)
    }
  }

  /**
   * 播放队列中的下一个音频
   */
  playNext() {
    if (this.audioQueue.length === 0) {
      this.isPlaying = false
      return
    }

    const filePath = this.audioQueue.shift()
    this.currentAudio = filePath
    this.innerAudioContext.src = filePath
    this.innerAudioContext.play()
  }

  /**
   * 停止播放并清空队列
   */
  stop() {
    this.innerAudioContext.stop()
    this.audioQueue = []
    this.isPlaying = false
    this.currentAudio = null
  }

  /**
   * 暂停播放
   */
  pause() {
    this.innerAudioContext.pause()
  }

  /**
   * 恢复播放
   */
  resume() {
    this.innerAudioContext.play()
  }

  /**
   * 清理资源
   */
  destroy() {
    this.stop()
    this.innerAudioContext.destroy()
    
    // 清理临时文件
    const fs = wx.getFileSystemManager()
    try {
      const files = fs.readdirSync(wx.env.USER_DATA_PATH)
      files.forEach(file => {
        if (file.startsWith('tts_')) {
          fs.unlinkSync(`${wx.env.USER_DATA_PATH}/${file}`)
        }
      })
    } catch (err) {
      console.error('[AudioPlayer] 清理临时文件失败:', err)
    }
  }
}

module.exports = RealtimeAudioPlayer
