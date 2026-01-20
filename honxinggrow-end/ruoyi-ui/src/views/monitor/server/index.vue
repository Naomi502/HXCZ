<template>
  <div class="app-container">
    <div class="monitor-header">
      <h3>服务器运行状态</h3>
      <el-button type="primary" icon="el-icon-refresh" @click="getList" size="small">刷新状态</el-button>
    </div>
    
    <el-row :gutter="10">
      <!-- 核心性能概览 -->
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span><i class="el-icon-view"></i> 性能概览</span>
          </div>
          <div class="overview-container">
            <div class="overview-item">
              <div class="overview-label">CPU状态</div>
              <div class="overview-value" :class="cpuStatusClass">
                <i class="el-icon-cpu"></i>
                <span>{{ server.cpu.used }}%</span>
              </div>
              <div class="overview-desc" :class="cpuStatusClass">
                {{ cpuStatusText }}
              </div>
            </div>
            <div class="overview-item">
              <div class="overview-label">内存状态</div>
              <div class="overview-value" :class="memStatusClass">
                <i class="el-icon-memory"></i>
                <span>{{ server.mem.usage }}%</span>
              </div>
              <div class="overview-desc" :class="memStatusClass">
                {{ memStatusText }}
              </div>
            </div>
            <div class="overview-item">
              <div class="overview-label">系统状态</div>
              <div class="overview-value status-success">
                <i class="el-icon-check"></i>
                <span>正常运行</span>
              </div>
              <div class="overview-desc">
                服务器工作正常
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- CPU使用率 -->
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span><i class="el-icon-cpu"></i> CPU使用率</span>
            <el-tag :type="cpuStatusType" size="mini" style="float: right;">
              {{ cpuStatusText }}
            </el-tag>
          </div>
          <div ref="cpuChart" class="chart-item" style="height: 200px;"></div>
        </el-card>
      </el-col>

      <!-- 内存使用率 -->
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span><i class="el-icon-memory"></i> 内存使用率</span>
            <el-tag :type="memStatusType" size="mini" style="float: right;">
              {{ memStatusText }}
            </el-tag>
          </div>
          <div ref="memChart" class="chart-item" style="height: 200px;"></div>
        </el-card>
      </el-col>

      <!-- 磁盘状态 -->
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span><i class="el-icon-receiving"></i> 磁盘使用情况</span>
          </div>
          <div class="disk-container">
            <div v-for="(sysFile, index) in server.sysFiles" :key="index" class="disk-item">
              <div class="disk-header">
                <span class="disk-name">{{ sysFile.dirName }}</span>
                <el-tag :type="sysFile.usage > 80 ? 'danger' : sysFile.usage > 50 ? 'warning' : 'success'" size="mini">
                  {{ sysFile.usage }}%
                </el-tag>
              </div>
              <el-progress 
                :percentage="sysFile.usage" 
                :stroke-width="15" 
                :color="sysFile.usage > 80 ? '#F56C6C' : sysFile.usage > 50 ? '#E6A23C' : '#67C23A'"
                :show-text="false"
              >
              </el-progress>
              <div class="disk-info">
                <span>已用: {{ sysFile.used }}</span>
                <span>总容量: {{ sysFile.total }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 基本信息 -->
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span><i class="el-icon-info"></i> 服务器信息</span>
          </div>
          <div class="basic-info">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-item">
                  <span class="info-label">服务器名称:</span>
                  <span class="info-value">{{ server.sys.computerName }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="info-label">IP地址:</span>
                  <span class="info-value">{{ server.sys.computerIp }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="info-label">操作系统:</span>
                  <span class="info-value">{{ server.sys.osName }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <span class="info-label">自动刷新:</span>
                  <span class="info-value">每5秒</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getServer } from "@/api/monitor/server"
import * as echarts from "echarts"

export default {
  name: "Server",
  data() {
    return {
      // 服务器信息
      server: {
        cpu: { used: 0 },
        mem: { usage: 0 },
        sys: { computerName: '', computerIp: '', osName: '' },
        sysFiles: []
      },
      // 图表实例
      cpuChart: null,
      memChart: null,
      // 数据历史记录
      cpuHistory: [],
      memHistory: [],
      timeHistory: [],
      // 定时器
      timer: null,
      // 刷新间隔（毫秒）
      refreshInterval: 5000
    }
  },
  computed: {
    // CPU状态相关
    cpuStatusClass() {
      if (this.server.cpu.used > 80) return 'status-danger'
      if (this.server.cpu.used > 50) return 'status-warning'
      return 'status-success'
    },
    cpuStatusType() {
      if (this.server.cpu.used > 80) return 'danger'
      if (this.server.cpu.used > 50) return 'warning'
      return 'success'
    },
    cpuStatusText() {
      if (this.server.cpu.used > 80) return 'CPU负载过高'
      if (this.server.cpu.used > 50) return 'CPU负载适中'
      return 'CPU负载正常'
    },
    // 内存状态相关
    memStatusClass() {
      if (this.server.mem.usage > 80) return 'status-danger'
      if (this.server.mem.usage > 50) return 'status-warning'
      return 'status-success'
    },
    memStatusType() {
      if (this.server.mem.usage > 80) return 'danger'
      if (this.server.mem.usage > 50) return 'warning'
      return 'success'
    },
    memStatusText() {
      if (this.server.mem.usage > 80) return '内存占用过高'
      if (this.server.mem.usage > 50) return '内存占用适中'
      return '内存占用正常'
    }
  },
  created() {
    this.getList()
    this.openLoading()
  },
  mounted() {
    // 初始化图表
    this.initCharts()
    // 设置定时刷新
    this.startTimer()
    // 监听窗口大小变化，自适应图表
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 清除定时器
    this.clearTimer()
    // 移除事件监听
    window.removeEventListener('resize', this.handleResize)
    // 销毁图表实例
    this.destroyCharts()
  },
  methods: {
    /** 查询服务器信息 */
    getList() {
      getServer().then(response => {
        this.server = response.data
        this.$modal.closeLoading()
        // 更新图表数据
        this.updateCharts()
      })
    },
    
    /** 打开加载层 */
    openLoading() {
      this.$modal.loading("正在加载服务器状态...")
    },
    
    /** 初始化图表 */
    initCharts() {
      // CPU使用率图表
      this.cpuChart = echarts.init(this.$refs.cpuChart)
      
      // 内存使用率图表
      this.memChart = echarts.init(this.$refs.memChart)
      
      // 初始化图表配置
      this.initChartOptions()
    },
    
    /** 初始化图表配置 */
    initChartOptions() {
      // 通用图表配置
      const baseOption = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.timeHistory
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        }
      }
      
      // CPU图表配置
      const cpuOption = {
        ...baseOption,
        series: [
          {
            name: 'CPU使用率',
            type: 'line',
            data: this.cpuHistory,
            smooth: true,
            itemStyle: {
              color: '#67C23A'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(103, 194, 58, 0.5)'
                },
                {
                  offset: 1,
                  color: 'rgba(103, 194, 58, 0.1)'
                }
              ])
            }
          }
        ]
      }
      
      // 内存图表配置
      const memOption = {
        ...baseOption,
        series: [
          {
            name: '内存使用率',
            type: 'line',
            data: this.memHistory,
            smooth: true,
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(64, 158, 255, 0.5)'
                },
                {
                  offset: 1,
                  color: 'rgba(64, 158, 255, 0.1)'
                }
              ])
            }
          }
        ]
      }
      
      // 设置图表配置
      this.cpuChart.setOption(cpuOption)
      this.memChart.setOption(memOption)
    },
    
    /** 更新图表数据 */
    updateCharts() {
      // 获取当前时间
      const now = new Date()
      const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
      
      // 限制历史数据长度
      const maxHistoryLength = 12
      
      // 更新CPU数据
      this.timeHistory.push(timeStr)
      this.cpuHistory.push(this.server.cpu.used)
      if (this.timeHistory.length > maxHistoryLength) {
        this.timeHistory.shift()
        this.cpuHistory.shift()
      }
      
      // 更新内存数据
      this.memHistory.push(this.server.mem.usage)
      if (this.memHistory.length > maxHistoryLength) {
        this.memHistory.shift()
      }
      
      // 更新CPU图表
      this.cpuChart.setOption({
        xAxis: {
          data: this.timeHistory
        },
        series: [
          {
            data: this.cpuHistory
          }
        ]
      })
      
      // 更新内存图表
      this.memChart.setOption({
        xAxis: {
          data: this.timeHistory
        },
        series: [
          {
            data: this.memHistory
          }
        ]
      })
    },
    
    /** 处理窗口大小变化 */
    handleResize() {
      this.cpuChart && this.cpuChart.resize()
      this.memChart && this.memChart.resize()
    },
    
    /** 销毁图表实例 */
    destroyCharts() {
      this.cpuChart && this.cpuChart.dispose()
      this.memChart && this.memChart.dispose()
    },
    
    /** 启动定时器 */
    startTimer() {
      this.timer = setInterval(() => {
        this.getList()
      }, this.refreshInterval)
    },
    
    /** 清除定时器 */
    clearTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    }
  }
}
</script>

<style scoped>
.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.monitor-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

/* 概览样式 */
.overview-container {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.overview-item {
  text-align: center;
  flex: 1;
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.overview-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.overview-value i {
  font-size: 24px;
}

.overview-desc {
  font-size: 12px;
}

/* 状态样式 */
.status-success {
  color: #67C23A;
}

.status-warning {
  color: #E6A23C;
}

.status-danger {
  color: #F56C6C;
}

/* 图表样式 */
.chart-item {
  width: 100%;
}

/* 磁盘样式 */
.disk-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px 0;
}

.disk-item {
  background-color: #fafafa;
  padding: 15px;
  border-radius: 8px;
}

.disk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.disk-name {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}

.disk-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
  margin-top: 5px;
}

/* 基本信息样式 */
.basic-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-label {
  width: 100px;
  font-size: 14px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
