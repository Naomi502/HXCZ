const cloud = require('wx-server-sdk')
const axios = require('axios')

cloud.init()

// 后端API的基础URL
const BASE_URL = 'http://59.110.229.252:8080'

exports.main = async (event, context) => {
  const {
    url,          // 请求路径
    method = 'GET', // 请求方法
    data,         // 请求数据
    header = {},  // 请求头
    hideLoading = false // 是否隐藏加载提示
  } = event

  try {
    // 构造完整的请求URL
    const fullUrl = `${BASE_URL}${url}`
    
    // 构造axios请求配置
    const config = {
      method,
      url: fullUrl,
      data,
      headers: header,
      timeout: 60000 // 设置60秒超时
    }

    // 发送请求
    const response = await axios(config)
    
    // 返回结果
    return {
      success: true,
      statusCode: response.status,
      data: response.data
    }
  } catch (error) {
    // 错误处理
    console.error('请求后端API失败:', error)
    
    return {
      success: false,
      statusCode: error.response?.status || 500,
      message: error.message || '请求失败',
      error: error.response?.data || error
    }
  }
}