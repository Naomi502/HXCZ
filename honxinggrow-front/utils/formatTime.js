/**
 * 格式化时间为易读格式
 * @param {string|Date} timeStr 时间字符串或Date对象
 * @param {boolean} showTime 是否显示时间部分，默认true（显示年月日 时分）
 * @returns {string} 格式化后的时间字符串，如 "2025-11-14 16:49" 或 "2025-11-14"
 */
function formatTime(timeStr, showTime = true) {
  // 检查是否为空值
  if (!timeStr || timeStr === 'null' || timeStr === 'undefined') {
    return '待定'
  }
  
  try {
    // 处理 ISO 8601 格式 (2025-11-14T16:49:02) 或标准格式 (2025-11-14 16:49:02)
    let dateStr = timeStr.toString().trim()
    
    // 如果为空字符串，返回待定
    if (!dateStr || dateStr === '') {
      return '待定'
    }
    
    // 如果是 ISO 格式，替换 T 为空格
    if (dateStr.includes('T')) {
      dateStr = dateStr.replace('T', ' ')
      // 移除时区信息（如果有）
      if (dateStr.includes('+') || dateStr.includes('Z')) {
        const plusIndex = dateStr.indexOf('+')
        const zIndex = dateStr.indexOf('Z')
        const cutIndex = plusIndex > 0 ? plusIndex : (zIndex > 0 ? zIndex : dateStr.length)
        dateStr = dateStr.substring(0, cutIndex).trim()
      }
    }
    
    // 提取日期部分（格式：yyyy-MM-dd）
    const datePart = dateStr.split(' ')[0].trim()
    
    // 验证日期格式是否正确（yyyy-MM-dd）
    if (!datePart || !/^\d{4}-\d{2}-\d{2}$/.test(datePart)) {
      return '待定'
    }
    
    // 如果只显示日期，直接返回日期部分
    if (!showTime) {
      return datePart
    }
    
    // 如果需要显示时间，提取时分（不显示秒）
    if (dateStr.includes(' ')) {
      const timePart = dateStr.split(' ')[1]
      if (timePart) {
        // 只取时分，去掉秒
        const timeParts = timePart.split(':')
        if (timeParts.length >= 2) {
          return `${datePart} ${timeParts[0]}:${timeParts[1]}`
        } else if (timeParts.length === 1) {
          // 如果只有小时，补充分钟
          return `${datePart} ${timeParts[0]}:00`
        }
      }
    }
    
    // 如果没有时间部分，只返回日期
    return datePart
  } catch (e) {
    console.error('格式化时间失败:', timeStr, e)
    return '待定'
  }
}

/**
 * 提取时间部分（时分）
 * @param {string|Date} timeStr 时间字符串或Date对象
 * @returns {string} 时分字符串，如 "16:49"，如果无法提取返回空字符串
 */
function extractTime(timeStr) {
  if (!timeStr || timeStr === 'null' || timeStr === 'undefined') {
    return ''
  }
  
  try {
    let dateStr = timeStr.toString().trim()
    
    if (!dateStr || dateStr === '') {
      return ''
    }
    
    // 如果是 ISO 格式，替换 T 为空格
    if (dateStr.includes('T')) {
      dateStr = dateStr.replace('T', ' ')
      // 移除时区信息
      if (dateStr.includes('+') || dateStr.includes('Z')) {
        const plusIndex = dateStr.indexOf('+')
        const zIndex = dateStr.indexOf('Z')
        const cutIndex = plusIndex > 0 ? plusIndex : (zIndex > 0 ? zIndex : dateStr.length)
        dateStr = dateStr.substring(0, cutIndex).trim()
      }
    }
    
    // 提取时间部分
    if (dateStr.includes(' ')) {
      const timePart = dateStr.split(' ')[1]
      if (timePart) {
        const timeParts = timePart.split(':')
        if (timeParts.length >= 2) {
          // 返回时分，不包含秒
          return `${timeParts[0]}:${timeParts[1]}`
        }
      }
    }
    
    return ''
  } catch (e) {
    return ''
  }
}

/**
 * 格式化日期为中文格式
 * @param {string} dateStr 日期字符串，格式：yyyy-MM-dd
 * @param {boolean} showYear 是否显示年份，默认true
 * @returns {string} 格式化后的日期，如 "2025年11月20日" 或 "11月20日"
 */
function formatDateChinese(dateStr, showYear = true) {
  if (!dateStr || !/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
    return ''
  }
  
  const parts = dateStr.split('-')
  const year = parts[0]
  const month = parseInt(parts[1], 10)
  const day = parseInt(parts[2], 10)
  
  if (showYear) {
    return `${year}年${month}月${day}日`
  } else {
    return `${month}月${day}日`
  }
}

/**
 * 格式化时间范围
 * @param {string|Date} startTime 开始时间
 * @param {string|Date} endTime 结束时间
 * @returns {string} 格式化后的时间范围字符串
 */
function formatTimeRange(startTime, endTime) {
  if (!startTime || !endTime) {
    return ''
  }
  
  try {
    // 处理开始时间
    let startStr = startTime.toString().trim()
    if (startStr.includes('T')) {
      startStr = startStr.replace('T', ' ')
      if (startStr.includes('+') || startStr.includes('Z')) {
        const plusIndex = startStr.indexOf('+')
        const zIndex = startStr.indexOf('Z')
        const cutIndex = plusIndex > 0 ? plusIndex : (zIndex > 0 ? zIndex : startStr.length)
        startStr = startStr.substring(0, cutIndex).trim()
      }
    }
    
    // 处理结束时间
    let endStr = endTime.toString().trim()
    if (endStr.includes('T')) {
      endStr = endStr.replace('T', ' ')
      if (endStr.includes('+') || endStr.includes('Z')) {
        const plusIndex = endStr.indexOf('+')
        const zIndex = endStr.indexOf('Z')
        const cutIndex = plusIndex > 0 ? plusIndex : (zIndex > 0 ? zIndex : endStr.length)
        endStr = endStr.substring(0, cutIndex).trim()
      }
    }
    
    const startDate = startStr.split(' ')[0]
    const startTimePart = extractTime(startTime)
    const endDate = endStr.split(' ')[0]
    const endTimePart = extractTime(endTime)
    
    if (!startDate || !endDate) {
      return ''
    }
    
    // 判断是否是同一天
    if (startDate === endDate) {
      // 同一天：显示 年月日 时分到时分
      const dateChinese = formatDateChinese(startDate, true)
      if (startTimePart && endTimePart) {
        return `${dateChinese} ${startTimePart} -${endTimePart}`
      } else if (startTimePart) {
        return `${dateChinese} ${startTimePart}`
      } else {
        return dateChinese
      }
    } else {
      // 跨天：显示 从年月日时分到 月日时分
      const startDateChinese = formatDateChinese(startDate, true)
      const endDateChinese = formatDateChinese(endDate, false)
      
      let result = ''
      if (startTimePart) {
        result += `${startDateChinese}${startTimePart} -`
      } else {
        result += `${startDateChinese} -`
      }
      
      if (endTimePart) {
        result += ` ${endDateChinese}${endTimePart}`
      } else {
        result += ` ${endDateChinese}`
      }
      
      return result
    }
  } catch (e) {
    console.error('格式化时间范围失败:', e)
    return ''
  }
}

module.exports = {
  formatTime,
  formatTimeRange,
  extractTime,
  formatDateChinese
}

