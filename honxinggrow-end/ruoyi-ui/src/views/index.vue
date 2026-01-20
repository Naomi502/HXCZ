<template>
  <div class="app-container home">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <div class="welcome-container">
          <div class="welcome-left">
            <h2 class="welcome-title">红芯成长管理系统</h2>
            <p class="welcome-desc">
              红芯成长管理系统是一个专注于学生成长记录、活动管理、积分激励的综合平台。
            </p>
            <div class="welcome-tags">
               <el-tag size="small" effect="dark" type="success">校园成长</el-tag>
               <el-tag size="small" effect="dark" type="warning">积分激励</el-tag>
               <el-tag size="small" effect="plain" type="info">v1.0.0</el-tag>
               <el-button class="help-btn" type="text" icon="el-icon-reading" @click="showHelpDialog" style="margin-left: 15px;">使用指南</el-button>
            </div>
          </div>
          <div class="welcome-right">
             <img src="@/assets/images/login-background.jpg" class="welcome-img" style="opacity: 0.8; mix-blend-mode: multiply;">
          </div>
        </div>
      </el-col>
      
      <!-- 右侧4个小卡片网格 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-row :gutter="10">
          <el-col :span="12">
            <div class="dashboard-stat-card bg-primary small-card" @click="goToActivity">
              <div class="stat-content">
                <div class="stat-num"><count-to :start-val="0" :end-val="statistics.activityCount || 0" :duration="2000" /></div>
                <div class="stat-label">活动总数</div>
              </div>
              <div class="stat-icon-mini"><i class="el-icon-s-flag"></i></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="dashboard-stat-card bg-success small-card" @click="goToStory">
              <div class="stat-content">
                <div class="stat-num"><count-to :start-val="0" :end-val="statistics.storyCount || 0" :duration="2000" /></div>
                <div class="stat-label">故事总数</div>
              </div>
              <div class="stat-icon-mini"><i class="el-icon-reading"></i></div>
            </div>
          </el-col>
          <el-col :span="12" style="margin-top: 10px;">
            <div class="dashboard-stat-card bg-warning small-card" @click="goToActivity">
              <div class="stat-content">
                <div class="stat-num"><count-to :start-val="0" :end-val="statistics.ongoingActivityCount || 0" :duration="2000" /></div>
                <div class="stat-label">进行中</div>
              </div>
              <div class="stat-icon-mini"><i class="el-icon-time"></i></div>
            </div>
          </el-col>
          <el-col :span="12" style="margin-top: 10px;">
            <div class="dashboard-stat-card bg-danger small-card" @click="goToPoints">
              <div class="stat-content">
                <div class="stat-num"><count-to :start-val="0" :end-val="statistics.studentCount || 0" :duration="2000" /></div>
                <div class="stat-label">学生总数</div>
              </div>
              <div class="stat-icon-mini"><i class="el-icon-user-solid"></i></div>
            </div>
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧内容区：图表 + 综合列表 Tab -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <!-- 图表区 -->
        <el-card class="box-card" style="margin-bottom: 20px; height: 380px;">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold; font-size: 16px;">数据概览</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="chart-title">活动趋势</div>
              <line-chart :chart-data="activityChartData" height="280px" width="100%" />
            </el-col>
            <el-col :span="12">
              <div class="chart-title">积分增长</div>
              <line-chart :chart-data="pointsChartData" height="280px" width="100%" />
            </el-col>
          </el-row>
        </el-card>

        <!-- 综合列表 Tabs -->
        <el-card class="box-card" :body-style="{ padding: '0 20px 20px 20px' }" style="height: 480px;">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="最新活动" name="activity">
              <el-table :data="recentActivities" style="width: 100%" size="medium">
                <el-table-column prop="title" label="活动名称" show-overflow-tooltip>
                   <template slot-scope="scope">
                     <span style="font-weight: 500;">{{ scope.row.title }}</span>
                   </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === '0'" type="primary" size="small" effect="dark">未开始</el-tag>
                    <el-tag v-else-if="scope.row.status === '1'" type="success" size="small" effect="dark">进行中</el-tag>
                    <el-tag v-else type="danger" size="small" effect="dark">结束</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="participantCount" label="参与人数" width="100" align="center">
                  <template slot-scope="scope">
                    <i class="el-icon-user"></i> {{ scope.row.participantCount }}
                  </template>
                </el-table-column>
                <el-table-column prop="publishTime" label="发布时间" width="160" align="right">
                   <template slot-scope="scope">
                    {{ parseTime(scope.row.publishTime, '{y}-{m}-{d}') }}
                  </template>
                </el-table-column>
              </el-table>
              <div style="text-align: center; margin-top: 15px;">
                <el-button type="text" @click="goToActivity">查看更多活动 <i class="el-icon-arrow-right"></i></el-button>
              </div>
            </el-tab-pane>

            <el-tab-pane label="最新故事" name="story">
              <el-table :data="recentStories" style="width: 100%" size="medium">
                <el-table-column prop="coverUrl" label="封面" width="100">
                  <template slot-scope="scope">
                    <el-image
                      v-if="scope.row.coverUrl"
                      style="width: 70px; height: 40px; border-radius: 4px; vertical-align: middle;"
                      :src="scope.row.coverUrl"
                      :preview-src-list="[scope.row.coverUrl]"
                      fit="cover">
                    </el-image>
                    <div v-else style="width: 70px; height: 40px; background: #f5f7fa; display: inline-flex; align-items: center; justify-content: center; border-radius: 4px; color: #909399;">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" show-overflow-tooltip />
                <el-table-column prop="viewCount" label="阅读" width="80" align="center">
                   <template slot-scope="scope">
                    <span style="color: #909399;"><i class="el-icon-view"></i> {{ scope.row.viewCount }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="likeCount" label="点赞" width="80" align="center">
                   <template slot-scope="scope">
                    <span style="color: #F56C6C;"><i class="el-icon-star-off"></i> {{ scope.row.likeCount }}</span>
                  </template>
                </el-table-column>
              </el-table>
              <div style="text-align: center; margin-top: 15px;">
                <el-button type="text" @click="goToStory">查看更多故事 <i class="el-icon-arrow-right"></i></el-button>
              </div>
            </el-tab-pane>

            <el-tab-pane label="最新问答" name="qa">
              <el-table :data="topQa" style="width: 100%" size="medium">
                <el-table-column prop="title" label="问题" show-overflow-tooltip>
                   <template slot-scope="scope">
                     <i class="el-icon-question" style="color: #E6A23C; margin-right: 5px;"></i> {{ scope.row.title }}
                   </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template slot-scope="scope">
                    <el-tag v-if="scope.row.status === '1'" type="success" size="mini">发布</el-tag>
                    <el-tag v-else type="info" size="mini">其他</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="浏览" width="100" align="right" />
              </el-table>
              <div style="text-align: center; margin-top: 15px;">
                <el-button type="text" @click="goToQa">查看更多问答 <i class="el-icon-arrow-right"></i></el-button>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- 右侧内容区：心灵树洞 + 积分榜 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <!-- 心灵树洞 -->
        <el-card class="box-card" style="margin-bottom: 20px; height: 380px; display: flex; flex-direction: column;">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold;">心灵树洞</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToTreeHole">全部</el-button>
          </div>
          <div class="tree-hole-container">
            <el-timeline style="padding-left: 5px;">
              <el-timeline-item
                v-for="(item, index) in recentTreeHole"
                :key="index"
                :timestamp="parseTime(item.createTime, '{m}-{d} {h}:{i}')"
                placement="top"
                :color="item.status === '1' ? '#67C23A' : '#E6A23C'">
                <el-card shadow="hover" :body-style="{ padding: '10px' }">
                  <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                    <span style="font-weight: bold; color: #303133;">{{ item.nickName || '匿名' }}</span>
                    <el-tag size="mini" :type="item.status === '1' ? 'success' : 'warning'" effect="plain">{{ item.status === '1' ? '已阅' : '未读' }}</el-tag>
                  </div>
                  <div style="font-size: 13px; color: #606266; line-height: 1.5;">
                    {{ item.content }}
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="recentTreeHole.length === 0" class="empty-state">
              <i class="el-icon-chat-dot-square" style="font-size: 40px; color: #ddd; margin-bottom: 10px;"></i>
              <p>暂无树洞留言</p>
            </div>
          </div>
        </el-card>

        <!-- 积分动态 -->
        <el-card class="box-card" style="height: 480px; display: flex; flex-direction: column;">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold;">最新积分</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToPoints">详情</el-button>
          </div>
          <div class="points-list" style="flex: 1; overflow-y: auto;">
             <div v-for="(log, index) in recentPointLogs" :key="index" class="point-item">
               <div class="point-left">
                 <div class="point-user">{{ log.userName }}</div>
                 <div class="point-remark">{{ log.remark }}</div>
               </div>
               <div class="point-right" :class="log.points > 0 ? 'plus' : 'minus'">
                 {{ log.points > 0 ? '+' : '' }}{{ log.points }}
               </div>
             </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作说明弹窗 -->
    <el-dialog
      title="操作说明"
      :visible.sync="helpDialogVisible"
      width="700px"
      append-to-body
    >
      <el-collapse accordion>
        <el-collapse-item title="系统概述" name="1">
          <div class="help-content">
            <h4>红芯成长管理系统</h4>
            <p>本系统是一个专注于学生成长记录、活动管理、积分激励的综合平台,主要功能包括:</p>
            <ul>
              <li><strong>成长活动:</strong> 发布校园活动,学生可报名参与,上传活动证明,审核通过后获得积分奖励</li>
              <li><strong>成长故事:</strong> 发布学生成长故事,记录精彩瞬间,支持点赞和阅读统计</li>
              <li><strong>积分管理:</strong> 学生积分账户管理,积分流水记录,支持手动调整积分</li>
              <li><strong>成长轨迹:</strong> 汇总学生的活动参与记录和积分历史,可视化展示成长历程</li>
            </ul>
          </div>
        </el-collapse-item>
        <el-collapse-item title="活动管理流程" name="2">
          <div class="help-content">
            <h4>活动发布与管理</h4>
            <ol>
              <li><strong>创建活动:</strong> 填写活动名称、时间、地点、人数上限等信息</li>
              <li><strong>发布活动:</strong> 活动状态为"未开始"时学生即可提前报名；到达开始时间后状态变为"进行中"，停止报名。</li>
              <li><strong>管理报名:</strong> 查看报名学生名单,审核学生上传的活动证明</li>
              <li><strong>发放积分:</strong> 审核通过后,系统自动发放活动积分给学生</li>
              <li><strong>结束活动:</strong> 活动结束后,状态自动更新为"结束"</li>
            </ol>
          </div>
        </el-collapse-item>
        <el-collapse-item title="故事发布流程" name="3">
          <div class="help-content">
            <h4>成长故事管理</h4>
            <ol>
              <li><strong>创建故事:</strong> 填写故事标题、摘要、封面、正文内容</li>
              <li><strong>发布故事:</strong> 故事状态从"草稿"改为"发布",学生即可查看</li>
              <li><strong>数据统计:</strong> 系统自动统计故事的阅读次数和点赞数</li>
              <li><strong>下架故事:</strong> 如需修改或隐藏,可将状态改为"下架"</li>
            </ol>
          </div>
        </el-collapse-item>
        <el-collapse-item title="积分管理说明" name="4">
          <div class="help-content">
            <h4>积分体系</h4>
            <p>积分是激励学生参与活动的核心机制,主要来源包括:</p>
            <ul>
              <li><strong>活动参与:</strong> 学生参与活动并上传证明,审核通过后获得活动积分</li>
              <li><strong>手动调整:</strong> 管理员可根据实际情况手动调整学生积分</li>
              <li><strong>积分流水:</strong> 所有积分变动都会记录,可追溯积分来源和用途</li>
            </ul>
            <p><strong>注意事项:</strong></p>
            <ul>
              <li>积分调整需要填写备注,说明调整原因</li>
              <li>积分流水记录不可删除,保证数据完整性</li>
              <li>学生可在小程序端查看自己的积分和成长轨迹</li>
            </ul>
          </div>
        </el-collapse-item>
        <el-collapse-item title="常见问题" name="5">
          <div class="help-content">
            <h4>FAQ</h4>
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :lg="12">
                <div class="faq-item">
                  <strong>Q: 活动结束后还能报名吗?</strong>
                  <p>A: 不能。活动结束后,状态自动变为"结束",学生无法再报名。</p>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="12">
                <div class="faq-item">
                  <strong>Q: 学生上传的证明如何审核?</strong>
                  <p>A: 在活动管理页面,点击"参与人员",可查看学生上传的证明,进行审核。</p>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="12">
                <div class="faq-item">
                  <strong>Q: 积分可以扣减吗?</strong>
                  <p>A: 可以。在积分调整页面,输入负数即可扣减学生积分。</p>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :lg="12">
                <div class="faq-item">
                  <strong>Q: 故事下架后还能看到吗?</strong>
                  <p>A: 下架后,学生端无法查看该故事,但管理员仍可在后台看到。</p>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>
        <el-collapse-item title="安全功能说明" name="6">
          <div class="help-content">
            <p>为保障系统安全,防止校园内脚本小子和技术攻击,系统实现了以下安全功能:</p>
            <ul>
              <li><strong>客户端设备限制:</strong> 仅允许授权安全加密设备端（Android/IOS）访问，PC客户端无法获取代码进行抓包</li>
              <li><strong>登录失败次数限制:</strong> 连续5次登录失败后,账号将被锁定15分钟,防止暴力破解。</li>
              <li><strong>接口访问频率限制:</strong> 基于AOP的API速率限制,每个IP在60秒内最多只能请求20次,防止恶意请求。</li>
              <li><strong>密码复杂度验证:</strong> 密码长度至少8位,包含大小写字母、数字和特殊字符,提高密码安全性。</li>
              <li><strong>非顺序ID生成:</strong> 使用Snowflake算法生成非顺序ID,防止ID枚举攻击。</li>
              <li><strong>CSRF防护机制:</strong> 防止跨站请求伪造攻击,保护用户会话安全。</li>
              <li><strong>XSS防护过滤规则:</strong> 对请求参数进行XSS过滤,防止恶意脚本注入。</li>
              <li><strong>敏感信息脱敏功能:</strong> 对手机号、姓名、邮箱、等敏感信息进行脱敏处理,保护隐私。</li>
              <li><strong>接口调用日志审计:</strong> 记录所有接口调用,包括请求参数、响应结果、操作人、IP地址等,便于溯源和安全分析。</li>
              <li><strong>管理后台IP白名单:</strong> 可通过配置文件限制只有特定IP才能访问管理后台,支持IP地址、通配符和网段格式。</li>
            </ul>
          </div>
        </el-collapse-item>
      </el-collapse>
      <div slot="footer" class="dialog-footer">
        <el-button @click="helpDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import LineChart from '@/views/dashboard/LineChart'
import { getDashboardData } from '@/api/hx/dashboard'
import { listQa } from '@/api/hx/qa'
import { listHole } from '@/api/system/hole'

export default {
  name: 'Index',
  components: {
    CountTo,
    LineChart
  },
  filters: {
    ellipsis(value, limit) {
      if (!value) return ''
      if (value.length <= limit) return value
      return value.substring(0, limit) + '...'
    }
  },
  data() {
    return {
      statistics: {
        activityCount: 0,
        ongoingActivityCount: 0,
        studentCount: 0,
        totalPoints: 0,
        storyCount: 0,
        weeklyParticipantCount: 0
      },
      activityChartData: {
        expectedData: [],
        actualData: []
      },
      pointsChartData: {
        expectedData: [],
        actualData: []
      },
      recentActivities: [],
      recentStories: [],
      topStories: [],
      topQa: [],
      recentPointLogs: [],
      recentTreeHole: [],
      helpDialogVisible: false,
      activeTab: 'activity'
    }
  },
  created() {
    this.fetchDashboardData()
  },
  methods: {
    // 将下划线命名转换为驼峰命名
    convertKeysToCamel(obj) {
      if (!obj || typeof obj !== 'object') return obj;
      if (Array.isArray(obj)) return obj.map(item => this.convertKeysToCamel(item));

      const result = {};
      for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
          const camelKey = key.replace(/_([a-z])/g, (match, letter) => letter.toUpperCase());
          result[camelKey] = this.convertKeysToCamel(obj[key]);
        }
      }
      return result;
    },
    fetchDashboardData() {
      getDashboardData().then(response => {
        const data = response.data
        this.statistics = this.convertKeysToCamel(data.statistics || {})

        if (data.activityTrend && data.activityTrend.length > 0) {
          const convertedTrend = this.convertKeysToCamel(data.activityTrend)
          const months = convertedTrend.map(item => item.month)
          const counts = convertedTrend.map(item => item.count)
          this.activityChartData = {
            expectedData: counts,
            actualData: counts
          }
        }

        if (data.pointsTrend && data.pointsTrend.length > 0) {
          const convertedTrend = this.convertKeysToCamel(data.pointsTrend)
          const months = convertedTrend.map(item => item.month)
          const points = convertedTrend.map(item => item.points)
          this.pointsChartData = {
            expectedData: points,
            actualData: points
          }
        }

        // 限制只显示最新5条数据
        this.recentActivities = this.convertKeysToCamel(data.recentActivities || []).slice(0, 5)
        this.recentStories = this.convertKeysToCamel(data.recentStories || []).slice(0, 5)
        this.topStories = this.convertKeysToCamel(data.topStories || []).slice(0, 5)
        this.recentPointLogs = this.convertKeysToCamel(data.recentPointLogs || []).slice(0, 5)

        // 使用问答管理页的API单独获取最新问答数据
        this.fetchQaData()
        // 使用心灵树洞管理页的API单独获取最新心灵树洞数据
        this.fetchTreeHoleData()
      }).catch(() => {
        this.$message.error('获取首页数据失败')
        // 即使主数据获取失败，也尝试获取问答和心灵树洞数据
        this.fetchQaData()
        this.fetchTreeHoleData()
      })
    },
    // 获取最新问答数据
    fetchQaData() {
      listQa({ pageNum: 1, pageSize: 5, status: '1' }).then(response => {
        this.topQa = this.convertKeysToCamel(response.rows || []).slice(0, 5)
      }).catch(() => {
        this.$message.error('获取最新问答数据失败')
      })
    },
    // 获取最新心灵树洞数据（只获取未处理的）
    fetchTreeHoleData() {
      listHole({ pageNum: 1, pageSize: 5, status: '0' }).then(response => {
        this.recentTreeHole = this.convertKeysToCamel(response.rows || []).slice(0, 5)
      }).catch(() => {
        this.$message.error('获取心灵树洞数据失败')
      })
    },
    goToActivity() {
      this.$router.push('/growth/activity')
    },
    goToActivityAudit() {
      this.$router.push('/growth/activity')
    },
    goToStory() {
      this.$router.push('/growth/story')
    },
    goToPoints() {
      this.$router.push('/growth/points')
    },
    goToTreeHole() {
      this.$router.push('/growth/hole')
    },
    goToQa() {
      this.$router.push('/growth/qa')
    },
    // 显示操作说明弹窗
    showHelpDialog() {
      this.helpDialogVisible = true
    }
  }
}
</script>

<style scoped lang="scss">
.home {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;

  .welcome-container {
    background: #fff;
    border-radius: 6px;
    padding: 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    overflow: hidden;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);
    height: 148px;

    .welcome-left {
      z-index: 2;
      
      .welcome-title {
        font-size: 20px;
        color: #303133;
        margin: 0 0 12px 0;
        font-weight: 500;
      }
      
      .welcome-desc {
        color: #909399;
        font-size: 14px;
        margin: 0 0 20px 0;
        max-width: 600px;
        line-height: 1.5;
      }
      
      .welcome-tags {
        display: flex;
        align-items: center;

        .el-tag {
          margin-right: 10px;
        }

        .help-btn {
          font-size: 13px;
          color: #606266;
          padding: 6px 12px;
          border-radius: 4px;
          background: #f4f4f5;
          transition: all 0.3s;

          &:hover {
            color: #409eff;
            background: #ecf5ff;
          }

          i {
            margin-right: 4px;
          }
        }
      }
    }

    .welcome-right {
      position: absolute;
      right: 0;
      top: 0;
      bottom: 0;
      width: 400px;
      
      .welcome-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        mask-image: linear-gradient(to left, rgba(0,0,0,1), rgba(0,0,0,0));
        -webkit-mask-image: linear-gradient(to left, rgba(0,0,0,1), rgba(0,0,0,0));
      }
      
      .welcome-actions {
        display: none;
      }
    }
  }

  .dashboard-stat-card {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-radius: 6px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(0,0,0,0.1);
      
      .stat-icon {
        transform: scale(1.1);
      }
    }

    &.bg-primary { background: linear-gradient(135deg, #36a3f7 0%, #1976D2 100%); }
    &.bg-success { background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%); }
    &.bg-warning { background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%); }
    &.bg-danger { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); }

    .stat-icon {
      float: left;
      margin-right: 20px;
      font-size: 48px;
      opacity: 0.3;
      transition: all 0.3s ease;
    }

    .stat-info {
      flex: 1;
      text-align: right;
      
      .stat-num {
        font-size: 32px;
        font-weight: bold;
        line-height: 32px;
        margin-bottom: 8px;
      }
      
      .stat-label {
        font-size: 14px;
        opacity: 0.8;
      }
    }

    &.small-card {
      height: 69px;
      margin-bottom: 0;
      padding: 0 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .stat-content {
        .stat-num {
          font-size: 20px;
          margin-bottom: 4px;
          line-height: 1.2;
        }
        .stat-label {
          font-size: 12px;
          opacity: 0.9;
        }
      }

      .stat-icon-mini {
        font-size: 24px;
        opacity: 0.4;
      }
    }
  }

  .box-card {
    border: none;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);
    
    ::v-deep .el-card__header {
      padding: 15px 20px;
      border-bottom: 1px solid #f6f6f6;
    }
  }

  .chart-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 15px;
    text-align: center;
  }

  .tree-hole-container {
    flex: 1;
    overflow-y: auto;
    padding: 10px 0;
    display: flex; /* 确保子元素可以撑开高度 */
    flex-direction: column;
    
    .empty-state {
      flex: 1; /* 占据剩余空间 */
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      min-height: 200px; /* 给个最小高度 */
      color: #909399;
      font-size: 14px;
    }
  }

  .points-list {
    .point-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .point-left {
        .point-user {
          font-weight: bold;
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
        }
        .point-remark {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .point-right {
        font-size: 16px;
        font-weight: bold;
        
        &.plus { color: #67C23A; }
        &.minus { color: #F56C6C; }
      }
    }
  }
}
</style>
