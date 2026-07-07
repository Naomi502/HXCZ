const { checkGuestRequest, cacheGuestResponse } = require('./guestCache')

function getAuthorizationHeader() {
  const token = wx.getStorageSync('token')
  if (!token) {
    return {}
  }
  // 避免重复添加 Bearer
  const bearer = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  return { Authorization: bearer }
}

function getBaseUrl() {
  const app = getApp()
  if (app && app.getApiBaseUrl) {
    return app.getApiBaseUrl()
  }
  return 'http://localhost:8080'
}

function handleUnauthorized() {
  const app = getApp()
  if (app) {
    app.setToken('')
    if (typeof app.promptLogin === 'function') {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      app.promptLogin()
      return
    }
  }
  wx.removeStorageSync('token')
  wx.showToast({
    title: '请先登录',
    icon: 'none'
  })
}

function request(options) {
  const {
    url,
    method = 'GET',
    data,
    header = {},
    hideLoading = false,
    contentType = 'application/json'
  } = options

  // 未登录限流检查
  const guestResult = checkGuestRequest(options)
  if (guestResult) {
    return guestResult
  }

  if (!hideLoading) {
    wx.showLoading({
      title: '加载中',
      mask: true
    })
  }

  const fullHeader = Object.assign({
    'Content-Type': contentType
  }, getAuthorizationHeader(), header)

  const app = getApp()
  const baseUrl = app.globalData.apiBaseUrl || 'https:///prod-api'

  return new Promise((resolve, reject) => {
    const handleSuccess = (res) => {
      if (!hideLoading) {
        wx.hideLoading()
      }

      if (res.statusCode && res.statusCode !== 200) {
        wx.showToast({
          title: `请求失败: ${res.statusCode}`,
          icon: 'none'
        })
        reject(new Error(`HTTP Error: ${res.statusCode}`))
        return
      }

      const body = res.data

      if (body.code === 401) {
        handleUnauthorized()
        reject(new Error(body.msg || '未授权'))
        return
      }
      if (body.code !== 200) {
        wx.showToast({
          title: body.msg || '请求失败',
          icon: 'none'
        })
        reject(new Error(body.msg || '请求失败'))
        return
      }

      resolve(body)
      cacheGuestResponse(options, body)
    }

    const handleFail = (err) => {
      if (!hideLoading) {
        wx.hideLoading()
      }
      console.error('Request failed:', err)
      wx.showToast({
        title: '网络异常或请求失败',
        icon: 'none'
      })
      reject(err)
    }

    let fullUrl = url
    if (!url.startsWith('http')) {
      fullUrl = baseUrl + url
    }

    wx.request({
      url: fullUrl,
      method: method,
      data: data,
      header: fullHeader,
      success: handleSuccess,
      fail: handleFail
    })
  })
}

module.exports = {
  request,
  getAuthorizationHeader,
  getBaseUrl
}
