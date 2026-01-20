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
  // 保持与 request 函数内部一致的默认值逻辑
  return app.globalData.apiBaseUrl || 'http://localhost:8080'
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

  if (!hideLoading) {
    wx.showLoading({
      title: '加载中',
      mask: true
    })
  }

  // 构造完整的请求头
  const fullHeader = Object.assign({
    'Content-Type': contentType
  }, getAuthorizationHeader(), header)

  // 获取App实例以访问全局数据
  const app = getApp()
  // 获取是否使用云函数的配置
  const useCloud = app.globalData.useCloud
  // 获取本地请求的 BaseUrl
  const baseUrl = app.globalData.apiBaseUrl || 'http://localhost:8080'

  return new Promise((resolve, reject) => {
    // 统一处理成功回调
    const handleSuccess = (res, isCloud) => {
      if (!hideLoading) {
        wx.hideLoading()
      }

      let body;
      
      if (isCloud) {
        // 云函数返回处理
        const result = res.result
        if (!result) {
          reject(new Error('Cloud function returned empty result'))
          return
        }
        
        // 云函数层面的错误（如请求后端网络错误等）
        if (!result.success) {
           console.error('Cloud API Error:', result)
           wx.showToast({
             title: result.message || '请求失败',
             icon: 'none'
           })
           reject(new Error(result.message || 'Cloud API Error'))
           return
        }
        
        // 获取实际后端返回的数据
        body = result.data
      } else {
        // wx.request 返回处理
        if (res.statusCode && res.statusCode !== 200) {
           wx.showToast({
              title: `请求失败: ${res.statusCode}`,
              icon: 'none'
           })
           reject(new Error(`HTTP Error: ${res.statusCode}`))
           return
        }
        body = res.data
      }

      // 统一处理后端业务逻辑状态码
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
    }

    // 统一处理失败回调
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

    if (useCloud) {
      // 方式一：使用云函数 callApi
      // 注意：云函数目录是 callApi，这里名字要匹配
      // 传递相对路径 url 即可，云函数内部会处理 BaseURL
      wx.cloud.callFunction({
        name: 'callApi', 
        data: {
          url: url, 
          method: method,
          data,
          header: fullHeader
        },
        success: (res) => handleSuccess(res, true),
        fail: handleFail
      })
    } else {
      // 方式二：使用本地请求 wx.request
      // 需要拼接完整的 URL
      let fullUrl = url
      if (!url.startsWith('http')) {
        fullUrl = baseUrl + url
      }

      wx.request({
        url: fullUrl,
        method: method,
        data: data,
        header: fullHeader,
        success: (res) => handleSuccess(res, false),
        fail: handleFail
      })
    }
  })
}

module.exports = {
  request,
  getAuthorizationHeader,
  getBaseUrl
}
