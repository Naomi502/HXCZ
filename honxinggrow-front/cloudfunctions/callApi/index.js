const cloud = require('wx-server-sdk')
const axios = require('axios')
const https = require('https')

cloud.init()

// 后端API的基础URL
const BASE_URL = 'https://59.110.229.252/prod-api'

// 创建axios实例，配置忽略SSL证书验证（如果是自签名证书）
const axiosInstance = axios.create({
  httpsAgent: new https.Agent({
    rejectUnauthorized: false // 忽略SSL证书验证
  })
})

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
    
    console.log('请求URL:', fullUrl)
    console.log('请求方法:', method)
    console.log('请求数据:', data)
    console.log('请求头:', header)
    
    // 构造axios请求配置
    const config = {
      method,
      url: fullUrl,
      data,
      headers: header,
      timeout: 60000 // 设置60秒超时
    }

    // 发送请求
    const response = await axiosInstance(config)
    
    console.log('响应状态:', response.status)
    console.log('响应数据:', response.data)
    
    // 返回结果
    return {
      success: true,
      statusCode: response.status,
      data: response.data
    }
  } catch (error) {
    // 错误处理
    console.error('请求后端API失败:', error.message)
    console.error('错误详情:', error.response?.data || error)
    
    return {
      success: false,
      statusCode: error.response?.status || 500,
      message: error.message || '请求失败',
      error: error.response?.data || error.toString()
    }
  }
}