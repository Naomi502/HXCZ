const { getBaseUrl, getAuthorizationHeader } = require('./request')

function uploadFile(filePath, options = {}) {
  const {
    name = 'file',
    formData = {},
    url = '/common/upload',
    hideLoading = false
  } = options

  if (!hideLoading) {
    wx.showLoading({
      title: '上传中',
      mask: true
    })
  }

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${getBaseUrl()}${url}`,
      filePath,
      name,
      formData,
      header: getAuthorizationHeader(),
      success(res) {
        try {
          const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (data.code !== 200) {
            wx.showToast({
              title: data.msg || '上传失败',
              icon: 'none'
            })
            reject(new Error(data.msg || '上传失败'))
            return
          }
          resolve(data)
        } catch (error) {
          reject(error)
        }
      },
      fail(err) {
        wx.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(err)
      },
      complete() {
        if (!hideLoading) {
          wx.hideLoading()
        }
      }
    })
  })
}

module.exports = {
  uploadFile
}

