<template>
  <div class="security-audit-page">
    <!-- 背景光晕 -->
    <div class="bg-glow"></div>

    <div class="audit-container">
      <!-- 标题 -->
      <h1 class="title">
        <span v-for="(w, i) in titleWords" :key="i">{{ w }}</span>
      </h1>

      <p class="subtitle">
        红芯成长管理系统 · 安全审计模块
      </p>

      <!-- 告警 -->
      <el-alert
        class="alert"
        title="系统检测到异常访问行为"
        type="warning"
        description="本次访问已进入安全审计流程，相关信息已记录。"
        show-icon
        :closable="false"
      />

      <!-- 审计信息 -->
      <div class="audit-panel">
        <div class="audit-item">
          <span>访问来源 IP</span>
          <b class="highlight">{{ ip }}</b>
        </div>
        <div class="audit-item">
          <span>访问时间</span>
          <b>{{ currentTime }}</b>
        </div>
        <div class="audit-item">
          <span>告警等级</span>
          <b class="danger">Level 2 · Suspicious</b>
        </div>
        <div class="audit-item">
          <span>浏览器信息</span>
          <b>{{ browser }}</b>
        </div>
        <div class="audit-item">
          <span>操作系统</span>
          <b>{{ os }}</b>
        </div>
        <div class="audit-item">
          <span>设备类型</span>
          <b>{{ deviceType }}</b>
        </div>
        <div class="audit-item">
          <span>访问位置</span>
          <b>{{ location }}</b>
        </div>
        <div class="audit-item">
          <span>会话ID</span>
          <b class="mono">{{ sessionId }}</b>
        </div>
      </div>

      <!-- 说明卡片 -->
      <div class="info-cards">
        <div class="card">
          <h3>访问说明</h3>
          <p>
            当前页面为系统安全防护模块，仅用于异常访问提示。
            未授权访问不会展示任何业务数据。
          </p>
          <p>
            但是我也会照样记录你的信息
          </p>
        </div>

        <div class="card">
          <h3>已启用安全机制</h3>
          <ul>
            <li>记录同设备IP登录历史记录</li>
            <li>未登录情况进行IP匹配</li>
            <li>安全日志与审计追踪</li>
            <li>通过短信/微信接口通知辅导员</li>
          </ul>
        </div>

        <div class="card">
          <h3>合规建议</h3>
          <p>
            你当我傻吗，装了个破解版微信进调试模式 ，再找一下接口访问链接，我后台就给你登上了？
          </p>
        </div>
      </div>

      <!-- 操作 -->
      <div class="actions">
        <el-button type="primary" size="large" @click="exit">
          返回登录页
        </el-button>
        <el-button type="danger" plain size="large" @click="ignore">
          继续访问
        </el-button>
      </div>

      <!-- 页脚 -->
      <footer class="footer">
        <p>© 2025 红芯成长管理系统</p>
        <p class="mono">Security & Compliance Enabled</p>
      </footer>
    </div>
  </div>
</template>
<script>
export default {
  name: 'ScriptKiddoIndex',
  data() {
    return {
      titleWords: ['安', '全', '访', '问', '告', '警'],
      currentTime: '',
      // 访问数据
      ip: '',
      browser: '',
      os: '',
      deviceType: '',
      sessionId: '',
      location: '',
      userAgent: ''
    }
  },
  mounted() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
    // 获取真实访问数据
    this.getUserData()
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    updateTime() {
      this.currentTime = new Date().toLocaleString('zh-CN')
    },
    // 获取真实用户数据
    getUserData() {
      // 获取用户代理字符串
      this.userAgent = navigator.userAgent

      // 生成唯一会话ID
      this.sessionId = 'sess_' + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)

      // 解析浏览器信息
      this.parseBrowserInfo()

      // 解析操作系统
      this.parseOSInfo()

      // 检测设备类型
      this.detectDeviceType()

      // 获取IP地址和位置信息
      this.getIPAndLocation()
    },
    // 解析浏览器信息
    parseBrowserInfo() {
      const ua = this.userAgent
      let browser = '未知浏览器'

      if (ua.indexOf('Chrome') > -1 && ua.indexOf('Edg') === -1) {
        const version = ua.match(/Chrome\/(\d+\.\d+)/)
        browser = 'Chrome ' + (version ? version[1] : '未知版本')
      } else if (ua.indexOf('Edg') > -1) {
        const version = ua.match(/Edg\/(\d+\.\d+)/)
        browser = 'Edge ' + (version ? version[1] : '未知版本')
      } else if (ua.indexOf('Firefox') > -1) {
        const version = ua.match(/Firefox\/(\d+\.\d+)/)
        browser = 'Firefox ' + (version ? version[1] : '未知版本')
      } else if (ua.indexOf('Safari') > -1 && ua.indexOf('Chrome') === -1) {
        const version = ua.match(/Version\/(\d+\.\d+)/)
        browser = 'Safari ' + (version ? version[1] : '未知版本')
      } else if (ua.indexOf('MSIE') > -1 || ua.indexOf('Trident') > -1) {
        const version = ua.match(/(MSIE|rv):(\d+\.\d+)/)
        browser = 'IE ' + (version ? version[2] : '未知版本')
      }

      this.browser = browser
    },
    // 解析操作系统
    parseOSInfo() {
      const ua = this.userAgent
      let os = '未知操作系统'

      if (ua.indexOf('Windows') > -1) {
        if (ua.indexOf('Windows NT 10.0') > -1) {
          os = 'Windows 10/11'
        } else if (ua.indexOf('Windows NT 6.3') > -1) {
          os = 'Windows 8.1'
        } else if (ua.indexOf('Windows NT 6.2') > -1) {
          os = 'Windows 8'
        } else if (ua.indexOf('Windows NT 6.1') > -1) {
          os = 'Windows 7'
        } else if (ua.indexOf('Windows NT 6.0') > -1) {
          os = 'Windows Vista'
        } else if (ua.indexOf('Windows NT 5.1') > -1) {
          os = 'Windows XP'
        } else {
          os = 'Windows 其他版本'
        }
      } else if (ua.indexOf('Mac OS X') > -1) {
        const version = ua.match(/Mac OS X (\d+_\d+)/)
        os = 'macOS ' + (version ? version[1].replace('_', '.') : '未知版本')
      } else if (ua.indexOf('Linux') > -1) {
        os = 'Linux'
      } else if (ua.indexOf('Android') > -1) {
        const version = ua.match(/Android (\d+\.\d+)/)
        os = 'Android ' + (version ? version[1] : '未知版本')
      } else if (ua.indexOf('iOS') > -1) {
        const version = ua.match(/OS (\d+_\d+)/)
        os = 'iOS ' + (version ? version[1].replace('_', '.') : '未知版本')
      }

      this.os = os
    },
    // 检测设备类型
    detectDeviceType() {
      const ua = this.userAgent
      if (ua.match(/(iPhone|iPod|iPad|Android|BlackBerry|BB10|Windows Phone|SymbianOS)/i)) {
        this.deviceType = '移动设备'
      } else {
        this.deviceType = '桌面设备'
      }
    },
    // 获取IP地址和位置信息
    getIPAndLocation() {
      // 使用ipify API获取公网IP
      fetch('https://api.ipify.org?format=json')
        .then(response => response.json())
        .then(data => {
          this.ip = data.ip
          // 使用ipinfo.io获取位置信息
          return fetch(`https://ipinfo.io/${data.ip}/json`)
        })
        .then(response => response.json())
        .then(data => {
          if (data.city && data.region) {
            this.location = `${data.region} ${data.city}`
          } else if (data.country) {
            this.location = data.country
          } else {
            this.location = '位置未知'
          }
        })
        .catch(error => {
          console.error('获取IP和位置信息失败:', error)
          this.ip = '127.0.0.1'
          this.location = '本地访问'
        })
    },
    exit() {
      this.$message.info('已结束本次访问')
      setTimeout(() => {
        this.$store.dispatch('LogOut').then(() => {
          this.$router.push('/login')
        })
      }, 800)
    },
    ignore() {
      this.$message.warning('该行为已记录')
    }
  }
}
</script>
<style scoped lang="scss">
.security-audit-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #ffffff, #f3f4f6);
  color: #1f2937;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.bg-glow {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(800px 400px at 10% 10%, rgba(59,130,246,.1), transparent),
    radial-gradient(800px 400px at 90% 90%, rgba(239,68,68,.1), transparent);
}

.audit-container {
  position: relative;
  width: 90%;
  max-width: 960px;
  background: rgba(255,255,255,.95);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(0,0,0,.1);
  border-radius: 18px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0,0,0,.08);
}

.title {
  font-size: 42px;
  letter-spacing: 8px;
  text-align: center;
  color: #1f2937;
}

.subtitle {
  text-align: center;
  margin: 10px 0 30px;
  color: #6b7280;
}

.alert {
  margin-bottom: 30px;
}

.audit-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px,1fr));
  gap: 20px;
  margin-bottom: 30px;

  .audit-item {
    background: rgba(0,0,0,.02);
    padding: 16px;
    border-radius: 12px;
    font-size: 14px;
    border: 1px solid rgba(0,0,0,.05);

    span {
      color: #6b7280;
    }

    b {
      display: block;
      margin-top: 6px;
      font-size: 16px;
      color: #1f2937;
    }

    .highlight {
      color: #f59e0b;
    }

    .danger {
      color: #dc2626;
    }

    .mono {
      font-family: monospace;
      font-size: 14px;
    }
  }
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px,1fr));
  gap: 20px;

  .card {
    background: rgba(255,255,255,.9);
    border: 1px solid rgba(0,0,0,.08);
    border-radius: 14px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,.05);

    h3 {
      color: #3b82f6;
    }

    p, li {
      font-size: 14px;
      color: #4b5563;
    }

    ul {
      padding-left: 18px;
    }
  }
}

.actions {
  margin-top: 40px;
  text-align: center;

  .el-button {
    margin: 0 12px;
    padding: 12px 32px;
    font-size: 16px;
  }
}

.footer {
  margin-top: 30px;
  text-align: center;
  font-size: 13px;
  color: #9ca3af;

  .mono {
    font-family: monospace;
    margin-top: 6px;
  }
}
</style>
