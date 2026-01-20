<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" inline label-width="80px">
      <el-form-item label="角色" prop="roleKey">
        <el-select v-model="queryParams.roleKey" placeholder="请选择角色" clearable style="width: 120px">
          <el-option label="学生" value="student" />
          <el-option label="教师" value="teacher" />
        </el-select>
      </el-form-item>
      <el-form-item label="姓名/学号" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入姓名或学号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="部门" prop="deptName">
        <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 100px">
          <el-option label="正常" value="0" />
          <el-option label="禁用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="userId" label="用户ID" width="120" />
      <el-table-column prop="userName" label="学号/工号" />
      <el-table-column prop="nickName" label="姓名" width="120" />
      <el-table-column prop="phonenumber" label="手机号" width="130" />
      <el-table-column prop="deptName" label="部门" width="150" />
      <el-table-column prop="pointsTotal" label="当前积分" width="100" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="'0'"
            :inactive-value="'1'"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="wechatBound" label="微信绑定" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.wechatBound" type="success">已绑定</el-tag>
          <el-tag v-else type="info">未绑定</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="300">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="mini" icon="el-icon-tickets" @click="handleViewLog(scope.row)">积分流水</el-button>
          <el-button type="text" size="mini" icon="el-icon-coin" @click="handleAdjust(scope.row)">积分调整</el-button>
          <el-button type="text" size="mini" icon="el-icon-key" @click="handleResetPassword(scope.row)">重置密码</el-button>
          <el-button v-if="scope.row.wechatBound" type="text" size="mini" icon="el-icon-delete" @click="handleUnbindWechat(scope.row)">解绑微信</el-button>
          <el-button type="text" size="mini" icon="el-icon-tickets" @click="handleViewActivities(scope.row)">
            查看活动</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 学生详情对话框 -->
    <el-dialog :title="'学生详情'" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-form :model="studentDetail" label-width="100px" size="small">
        <el-form-item label="学生ID">
          <span>{{ studentDetail.userId }}</span>
        </el-form-item>
        <el-form-item label="学生学号">
          <span>{{ studentDetail.userName }}</span>
        </el-form-item>
        <el-form-item label="昵称">
          <span>{{ studentDetail.nickName }}</span>
        </el-form-item>
        <el-form-item label="手机号">
          <span>{{ studentDetail.phonenumber }}</span>
        </el-form-item>
        <el-form-item label="邮箱">
          <span>{{ studentDetail.email }}</span>
        </el-form-item>
        <el-form-item label="当前积分">
          <span>{{ studentDetail.pointsTotal }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="studentDetail.status === '0' ? 'success' : 'danger'">
            {{ studentDetail.status === '0' ? '正常' : '禁用' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="微信绑定">
          <el-tag :type="studentDetail.wechatBound ? 'success' : 'info'">
            {{ studentDetail.wechatBound ? '已绑定' : '未绑定' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="创建时间">
          <span>{{ parseTime(studentDetail.createTime) }}</span>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 编辑学生信息对话框 -->
    <el-dialog :title="'编辑学生信息'" :visible.sync="editOpen" width="500px" append-to-body>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="学生ID">
          <span>{{ editForm.userId }}</span>
        </el-form-item>
        <el-form-item label="学生姓名" prop="userName">
          <el-input v-model="editForm.userName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="editForm.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="editForm.phonenumber" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEdit">确 定</el-button>
        <el-button @click="editOpen=false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 积分流水对话框 -->
    <el-dialog :title="`积分流水 - ${logQuery.userName}`" :visible.sync="logOpen" width="700px" append-to-body>
      <el-table v-loading="logLoading" :data="logList">
        <el-table-column prop="eventTime" label="时间" width="180">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.eventTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bizType" label="业务类型" width="140" />
        <el-table-column prop="points" label="积分" width="80" align="center">
          <template slot-scope="scope">
            <span :class="scope.row.points >= 0 ? 'text-success' : 'text-danger'"> {{ scope.row.points }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
      <pagination v-show="logTotal>0" :total="logTotal" :page.sync="logQuery.pageNum" :limit.sync="logQuery.pageSize"
        @pagination="getLogList" />
    </el-dialog>

    <!-- 积分调整对话框 -->
    <el-dialog :title="`积分调整 - ${adjustForm.userName || ''}`" :visible.sync="adjustOpen" width="420px" append-to-body>
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="90px">
        <el-form-item label="学生ID">
          <span>{{ adjustForm.userId }}</span>
        </el-form-item>
        <el-form-item label="调整积分" prop="points">
          <el-input-number v-model="adjustForm.points" :min="-99999" :max="99999" />
          <span class="form-tip">正数加分，负数扣分</span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="adjustForm.remark" placeholder="请输入积分调整原因，如：奖励表现优秀、扣除违规积分等" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAdjust">确 定</el-button>
        <el-button @click="adjustOpen=false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog :title="'重置密码'" :visible.sync="resetPwdOpen" width="420px" append-to-body>
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="100px">
        <el-form-item label="学生姓名">
          <span>{{ resetPwdForm.userName }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input type="password" v-model="resetPwdForm.newPassword" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input type="password" v-model="resetPwdForm.confirmPassword" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitResetPassword">确 定</el-button>
        <el-button @click="resetPwdOpen=false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 学生活动记录对话框 -->
    <el-dialog :title="`学生活动记录 - ${activityQuery.userName}`" :visible.sync="activityOpen" width="800px" append-to-body>
      <el-table v-loading="activityLoading" :data="activityList">
        <el-table-column prop="activityId" label="活动ID" width="100" />
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column prop="status" label="活动状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.statusType">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="报名时间" width="180">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.joinTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="proofStatus" label="证明状态" width="120">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.proofStatus === '0'" type="info">未提交</el-tag>
            <el-tag v-else-if="scope.row.proofStatus === '1'" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.proofStatus === '2'" type="success">已通过</el-tag>
            <el-tag v-else-if="scope.row.proofStatus === '3'" type="danger">已驳回</el-tag>
            <el-tag v-else type="default">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="proofSubmitTime" label="证明提交时间" width="180">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.proofSubmitTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="proofAuditRemark" label="审核意见" min-width="200" show-overflow-tooltip />
        <el-table-column prop="rewardGranted" label="积分发放" width="120">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.rewardGranted === '1'" type="success">已发放</el-tag>
            <el-tag v-else type="info">未发放</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rewardPoints" label="获得积分" width="100" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.rewardPoints || 0 }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="activityTotal>0" :total="activityTotal" :page.sync="activityQuery.pageNum" :limit.sync="activityQuery.pageSize"
        @pagination="getActivityList" />
    </el-dialog>
  </div>
</template>

<script>
import { listStudents, getStudent, updateStudent, adjustPoints, resetPassword, updateStatus, unbindWechat, listPointLogs, getStudentActivities } from '@/api/hx/points'

export default {
  name: 'StudentManagement',
  data() {
    return {
      loading: false,
      studentList: [],
      studentDetail: {},
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        roleKey: 'student', // 默认查询学生角色
        userName: undefined,
        deptName: undefined,
        phonenumber: undefined,
        status: undefined
      },
      selectedStudentIds: [], // 选中的学生ID数组
      detailOpen: false,
      editOpen: false,
      logOpen: false,
      adjustOpen: false,
      resetPwdOpen: false,
      logLoading: false,
      logList: [],
      logTotal: 0,
      logQuery: {
        userId: undefined,
        userName: '',
        pageNum: 1,
        pageSize: 10
      },
      // 编辑表单
      editForm: {
        userId: '',
        userName: '',
        nickName: '',
        phonenumber: '',
        email: ''
      },
      editRules: {
        userName: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
        phonenumber: [{ validator: this.validatePhone, trigger: 'blur' }],
        email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
      },
      // 积分调整表单
      adjustForm: {
        userId: '',
        userName: '',
        points: 0,
        remark: '积分调整'
      },
      adjustRules: {
        points: [{ required: true, message: '请填写调整积分', trigger: 'change' }],
        remark: [{ required: true, message: '请填写调整原因', trigger: 'blur' }]
      },
      // 重置密码表单
      resetPwdForm: {
        userId: '',
        userName: '',
        newPassword: '',
        confirmPassword: ''
      },
      resetPwdRules: {
        newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }],
        confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: this.validateConfirmPassword, trigger: 'blur' }]
      },
      // 活动记录相关
      activityOpen: false,
      activityLoading: false,
      activityList: [],
      activityTotal: 0,
      activityQuery: {
        userId: undefined,
        userName: '',
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.getList()
  },
  watch: {
    'adjustForm.points': {
      handler(newVal) {
        // 只有当备注是默认值时，才自动更新
        const currentRemark = this.adjustForm.remark;
        const isDefaultRemark = currentRemark === '奖励积分' || currentRemark === '扣除积分' || currentRemark === '积分调整';

        if (isDefaultRemark) {
          if (newVal > 0) {
            this.adjustForm.remark = '奖励积分'
          } else if (newVal < 0) {
            this.adjustForm.remark = '扣除积分'
          } else {
            this.adjustForm.remark = '积分调整'
          }
        }
      }
    }
  },
  methods: {
    // 获取学生列表
    getList() {
      this.loading = true
      listStudents(this.queryParams).then(response => {
        this.studentList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    // 重置
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    // 选择学生
    handleSelectionChange(selection) {
      this.selectedStudentIds = selection.map(item => item.userId)
    },
    // 查看学生详情
    handleView(row) {
      getStudent(row.userId).then(response => {
        console.log('学生详情数据:', response)
        this.studentDetail = response.data || response
        this.detailOpen = true
      })
    },
    // 编辑学生信息
    handleEdit(row) {
      getStudent(row.userId).then(response => {
        console.log('学生编辑数据:', response)
        this.editForm = response.data || response
        this.editOpen = true
      })
    },
    // 提交编辑
    submitEdit() {
      this.$refs['editFormRef'].validate(valid => {
        if (!valid) return
        updateStudent(this.editForm).then(() => {
          this.$modal.msgSuccess('编辑成功')
          this.editOpen = false
          this.getList()
        })
      })
    },
    // 查看积分流水
    handleViewLog(row) {
      this.logQuery.userId = row.userId
      this.logQuery.userName = row.userName
      this.logQuery.pageNum = 1
      this.getLogList()
    },
    // 获取积分流水
    getLogList() {
      this.logLoading = true
      listPointLogs({
        pageNum: this.logQuery.pageNum,
        pageSize: this.logQuery.pageSize,
        userId: this.logQuery.userId
      }).then(response => {
        this.logList = response.rows
        this.logTotal = response.total
        this.logOpen = true
        this.logLoading = false
      })
    },
    // 积分调整
    handleAdjust(row) {
      this.adjustForm = {
        userId: row.userId,
        userName: row.userName,
        points: 0,
        remark: '积分调整'
      }
      this.adjustOpen = true
      this.$nextTick(() => {
        this.resetForm('adjustFormRef')
      })
    },
    // 提交积分调整
    submitAdjust() {
      this.$refs['adjustFormRef'].validate(valid => {
        if (!valid) return
        if (!this.adjustForm.points || this.adjustForm.points === 0) {
          this.$modal.msgWarning('调整积分不能为 0')
          return
        }
        adjustPoints({
          userId: this.adjustForm.userId,
          points: this.adjustForm.points,
          remark: this.adjustForm.remark
        }).then(() => {
          this.$modal.msgSuccess('调整成功')
          this.adjustOpen = false
          this.getList()
        })
      })
    },
    // 重置密码
    handleResetPassword(row) {
      this.resetPwdForm = {
        userId: row.userId,
        userName: row.userName,
        newPassword: '',
        confirmPassword: ''
      }
      this.resetPwdOpen = true
      this.$nextTick(() => {
        this.resetForm('resetPwdFormRef')
      })
    },
    // 提交密码重置
    submitResetPassword() {
      this.$refs['resetPwdFormRef'].validate(valid => {
        if (!valid) return
        resetPassword({
          userId: this.resetPwdForm.userId,
          newPassword: this.resetPwdForm.newPassword
        }).then(() => {
          this.$modal.msgSuccess('密码重置成功')
          this.resetPwdOpen = false
        })
      })
    },
    // 状态变更
    handleStatusChange(row) {
      updateStatus({
        userId: row.userId,
        status: row.status
      }).then(() => {
        this.$modal.msgSuccess('状态更新成功')
      }).catch(() => {
        this.getList() // 刷新列表恢复原状态
      })
    },
    // 解绑微信
    handleUnbindWechat(row) {
      this.$modal.confirm('确定要解绑该学生的微信吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return unbindWechat({ userId: row.userId })
      }).then(() => {
        this.$modal.msgSuccess('微信解绑成功')
        this.getList()
      })
    },
    // 查看学生活动记录
    handleViewActivities(row) {
      this.activityQuery.userId = row.userId
      this.activityQuery.userName = row.userName
      this.activityQuery.pageNum = 1
      this.getActivityList()
    },
    // 获取学生活动列表
    getActivityList() {
      this.activityLoading = true
      getStudentActivities({
        pageNum: this.activityQuery.pageNum,
        pageSize: this.activityQuery.pageSize,
        userId: this.activityQuery.userId
      }).then(response => {
        this.activityList = response.rows
        this.activityTotal = response.total
        this.activityOpen = true
        this.activityLoading = false
      })
    },
    // 手机号验证
    validatePhone(rule, value, callback) {
      if (value && !/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号码'))
      } else {
        callback()
      }
    },
    // 确认密码验证
    validateConfirmPassword(rule, value, callback) {
      if (value !== this.resetPwdForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
  }
}
</script>

<style scoped>
.text-success {
  color: #67c23a;
}
.text-danger {
  color: #f56c6c;
}
.form-tip {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

