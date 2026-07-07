const GUEST_REQUEST_LOG_KEY = 'guest_req_log_v1'
const GUEST_CACHE_KEY = 'guest_api_cache_v1'
const LIMIT_COUNT = 5
const LIMIT_WINDOW_MS = 5 * 60 * 1000 // 5分钟
const CACHE_TTL_MS = 5 * 60 * 1000 // 5分钟

// 需要限流+缓存的白名单接口（仅GET）
const WHITE_LIST = [
  { method: 'GET', pattern: /^\/app\/story\/list/ },
  { method: 'GET', pattern: /^\/app\/story\/\d+/ },
  { method: 'GET', pattern: /^\/app\/activity\/list/ },
  { method: 'GET', pattern: /^\/app\/activity\/\d+/ }
]

function isLoggedIn() {
  return !!wx.getStorageSync('token')
}

function isWhiteListed(method, url) {
  const upperMethod = (method || 'GET').toUpperCase()
  return WHITE_LIST.some(item => {
    return item.method === upperMethod && item.pattern.test(url)
  })
}

function getRequestLog() {
  try {
    const raw = wx.getStorageSync(GUEST_REQUEST_LOG_KEY)
    return raw ? JSON.parse(raw) : []
  } catch (e) {
    return []
  }
}

function saveRequestLog(log) {
  wx.setStorageSync(GUEST_REQUEST_LOG_KEY, JSON.stringify(log))
}

function addRequestRecord() {
  const now = Date.now()
  const log = getRequestLog().filter(ts => now - ts < LIMIT_WINDOW_MS)
  log.push(now)
  saveRequestLog(log)
  return log.length
}

function getCacheKey(url, data) {
  const dataStr = data ? JSON.stringify(data) : ''
  return `${url}#${dataStr}`
}

function getCache(url, data) {
  try {
    const raw = wx.getStorageSync(GUEST_CACHE_KEY)
    if (!raw) return null
    const cache = JSON.parse(raw)
    const key = getCacheKey(url, data)
    const entry = cache[key]
    if (!entry) return null
    if (Date.now() - entry.ts > CACHE_TTL_MS) return null
    return entry.data
  } catch (e) {
    return null
  }
}

function setCache(url, data, responseData) {
  try {
    const raw = wx.getStorageSync(GUEST_CACHE_KEY)
    const cache = raw ? JSON.parse(raw) : {}
    const key = getCacheKey(url, data)
    cache[key] = { ts: Date.now(), data: responseData }
    wx.setStorageSync(GUEST_CACHE_KEY, JSON.stringify(cache))
  } catch (e) {
    console.error('Guest cache set failed', e)
  }
}

function clearCache() {
  wx.removeStorageSync(GUEST_CACHE_KEY)
  wx.removeStorageSync(GUEST_REQUEST_LOG_KEY)
}

/**
 * 检查并处理未登录请求
 * @returns {Promise|null} 如果返回 Promise，表示直接返回结果（缓存命中或限流）；返回 null 表示继续正常请求
 */
function checkGuestRequest(options) {
  if (isLoggedIn()) return null
  const { url, method, data } = options
  if (!isWhiteListed(method, url)) return null

  const cached = getCache(url, data)
  const log = getRequestLog()
  const now = Date.now()
  const recentCount = log.filter(ts => now - ts < LIMIT_WINDOW_MS).length

  if (recentCount >= LIMIT_COUNT) {
    if (cached) {
      return Promise.resolve({ code: 200, data: cached, msg: 'success', fromCache: true })
    }
    wx.showToast({
      title: '浏览次数已达上限，请稍后再试',
      icon: 'none',
      duration: 3000
    })
    return Promise.reject({
      code: 429,
      msg: '浏览次数已达上限，请5分钟后再试或登录后无限制浏览',
      isGuestLimit: true
    })
  }

  addRequestRecord()
  return null
}

/**
 * 缓存正常请求的响应
 */
function cacheGuestResponse(options, body) {
  if (isLoggedIn()) return
  const { url, method, data } = options
  if (!isWhiteListed(method, url)) return
  if (body && body.code === 200 && body.data !== undefined) {
    setCache(url, data, body.data)
  }
}

module.exports = {
  checkGuestRequest,
  cacheGuestResponse,
  clearCache,
  isLoggedIn
}
