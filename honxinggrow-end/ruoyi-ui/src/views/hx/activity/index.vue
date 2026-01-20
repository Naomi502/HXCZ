<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" inline label-width="68px">
      <el-form-item label="活动名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入活动名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="activityList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="activityId" label="编号" width="80" align="center" />
      <el-table-column prop="coverUrl" label="封面" width="120" align="center">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.coverUrl"
            style="width: 90px; height: 60px; border-radius: 4px"
            :src="scope.row.coverUrl"
            :preview-src-list="[scope.row.coverUrl]"
            fit="cover">
          </el-image>
          <div v-else style="width: 90px; height: 60px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; border-radius: 4px; color: #909399;">
            <i class="el-icon-picture-outline" style="font-size: 20px;"></i>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="活动名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="participantCount" label="已报名" width="100" align="center" />
      <el-table-column prop="maxParticipants" label="人数上限" width="100" align="center" />
      <el-table-column prop="publishUserName" label="发布人" width="120" />
      <el-table-column prop="publishTime" label="发布时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.publishTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-user" @click="handleParticipants(scope.row)">报名名单</el-button>
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="840px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input type="textarea" :rows="2" v-model="form.summary" placeholder="请输入摘要" />
        </el-form-item>
        <el-form-item label="封面地址" prop="coverUrl">
          <el-input v-model="form.coverUrl" placeholder="请输入封面图片地址" readonly>
            <el-button slot="append" icon="el-icon-upload" @click="triggerCoverUpload">上传</el-button>
          </el-input>
          <el-upload
            class="cover-hidden-upload"
            ref="activityCoverUploader"
            :action="uploadUrl"
            :show-file-list="false"
            :headers="uploadHeaders"
            :data="{ fileCategory: 'cover', businessType: 'activity', businessField: 'coverUrl' }"
            :before-upload="beforeCoverUpload"
            :on-success="handleCoverUploadSuccess"
            :on-error="handleCoverUploadError">
          </el-upload>
          <div v-if="form.coverUrl" class="cover-preview">
            <el-image
              :src="form.coverUrl"
              :preview-src-list="[form.coverUrl]"
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 4px; cursor: pointer;"
            />
            <el-button type="text" size="mini" @click="clearCover">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="yyyy-MM-dd HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="yyyy-MM-dd HH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数上限" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" />
          <span class="form-tip">为 0 或留空表示不限人数</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="item in statusOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="可见岗位" prop="visiblePostIds" v-if="$auth.hasPermi('system:post:list')">
          <el-select
            v-model="form.visiblePostIds"
            multiple
            placeholder="请选择可见岗位，不选择则所有用户可见"
            clearable
            collapse-tags
            style="width: 100%"
            @clear="handleClearPosts">
            <el-option v-for="item in postOptions" :key="item.postId" :label="item.postName" :value="item.postId" />
          </el-select>
          <div class="form-tip">不选择任何岗位时，所有用户都可以看到此活动</div>
        </el-form-item>
        <el-form-item label="活动详情" prop="description">
          <editor v-model="form.description" :min-height="260" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-drawer
      :title="`报名学生（${participantQuery.activityTitle}）`"
      :visible.sync="participantOpen"
      direction="rtl"
      size="70%"
      append-to-body>
      <div style="padding: 20px;">
        <el-table v-loading="participantLoading" :data="participantList">
        <el-table-column prop="userId" label="学生ID" width="100" />
        <el-table-column prop="userName" label="学生姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="140" show-overflow-tooltip />
        <el-table-column prop="joinTime" label="报名时间" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.joinTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="报名状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="participantStatusTag(scope.row.status)">{{ participantStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="proofImageUrl" label="参加证明" width="100">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.proofImageUrl"
              style="width: 50px; height: 50px; border-radius: 6px"
              :src="scope.row.proofImageUrl"
              :preview-src-list="[scope.row.proofImageUrl]"
              fit="cover">
              <div slot="error" style="width: 50px; height: 50px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; border-radius: 6px; color: #909399; font-size: 12px; flex-direction: column;">
                <span style="transform: scale(0.8);">加载失败</span>
              </div>
            </el-image>
            <div v-else style="width: 50px; height: 50px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; border-radius: 6px; color: #909399; font-size: 12px; flex-direction: column;">
              <span style="transform: scale(0.8);">未上传</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="proofStatus" label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="proofStatusTag(scope.row.proofStatus)">{{ proofStatusName(scope.row.proofStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="proofAuditBy" label="审核人" width="100" />
        <el-table-column prop="proofAuditTime" label="审核时间" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.proofAuditTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="proofAuditRemark" label="审核意见" min-width="150" show-overflow-tooltip />
        <el-table-column prop="rewardPoints" label="积分" width="80">
          <template slot-scope="scope">
            <span>{{ scope.row.rewardGranted === '1' ? (scope.row.rewardPoints || 0) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template slot-scope="scope">
            <template v-if="scope.row.status === '0'">
              <el-button type="text" size="mini" icon="el-icon-check" @click="handleAuditEnroll(scope.row, true)">通过报名</el-button>
              <el-button type="text" size="mini" icon="el-icon-close" @click="handleAuditEnroll(scope.row, false)">拒绝报名</el-button>
            </template>
            <template v-else-if="scope.row.status === '1'">
              <el-button type="text" size="mini" icon="el-icon-remove-outline" @click="handleCancelEnroll(scope.row)">取消报名</el-button>
              <template v-if="scope.row.proofStatus === '1'">
                <el-divider direction="vertical"></el-divider>
                <el-button type="text" size="mini" @click="auditProof(scope.row, true)">通过证明</el-button>
                <el-button type="text" size="mini" @click="auditProof(scope.row, false)">驳回证明</el-button>
              </template>
            </template>
            <template v-else>
               <span style="color: #909399">-</span>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="participantTotal>0" :total="participantTotal" :page.sync="participantQuery.pageNum"
        :limit.sync="participantQuery.pageSize" @pagination="getParticipants" />
      </div>
    </el-drawer>
  </div>
</template>

<script>
import Editor from '@/components/Editor'
import { listActivity, getActivity, addActivity, updateActivity, delActivity, listParticipants, auditParticipant, auditParticipation, cancelParticipation } from '@/api/hx/activity'
import { listPost } from '@/api/system/post'
import { getToken } from '@/utils/auth'

export default {
  name: 'HxActivity',
  components: { Editor },
  data() {
    return {
      loading: false,
      activityList: [],
      total: 0,
      open: false,
      title: '',
      ids: [],
      single: true,
      multiple: true,
      statusOptions: [
        { label: '未开始', value: '0' },
        { label: '进行中', value: '1' },
        { label: '结束', value: '2' }
      ],
      participantStatusOptions: [
        { label: '申请中', value: '0', tag: 'warning' },
        { label: '已报名', value: '1', tag: 'success' },
        { label: '已取消', value: '2', tag: 'info' }
      ],
      proofStatusOptions: [
        { label: '未提交', value: '0', tag: 'info' },
        { label: '待审核', value: '1', tag: 'warning' },
        { label: '已通过', value: '2', tag: 'success' },
        { label: '已驳回', value: '3', tag: 'danger' }
      ],
      postOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: undefined
      },
      form: {
        visiblePostIds: []
      },
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: {
        Authorization: getToken()
      },
      rules: {
        title: [{ required: true, message: '活动名称不能为空', trigger: 'blur' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        description: [{ required: true, message: '活动详情不能为空', trigger: 'blur' }]
      },
      participantOpen: false,
      participantLoading: false,
      participantList: [],
      participantTotal: 0,
      participantQuery: {
        activityId: undefined,
        activityTitle: '',
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.getList()
    // 仅在拥有岗位列表权限时才加载岗位，用于“可见岗位”下拉
    if (this.$auth && this.$auth.hasPermi('system:post:list')) {
      this.getPostList()
    }
  },
  methods: {
    getList() {
      this.loading = true
      listActivity(this.queryParams).then(response => {
        this.activityList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    proofStatusName(value) {
      const item = this.proofStatusOptions.find(opt => opt.value === value)
      return item ? item.label : '-'
    },
    proofStatusTag(value) {
      const item = this.proofStatusOptions.find(opt => opt.value === value)
      return item ? item.tag : 'info'
    },
    participantStatusName(value) {
      const item = this.participantStatusOptions.find(opt => opt.value === value)
      return item ? item.label : '-'
    },
    participantStatusTag(value) {
      const item = this.participantStatusOptions.find(opt => opt.value === value)
      return item ? item.tag : 'info'
    },
    statusName(value) {
      const item = this.statusOptions.find(opt => opt.value === value)
      return item ? item.label : value
    },
    statusTag(value) {
      switch (value) {
        case '0':
          return 'primary'
        case '1':
          return 'success'
        case '2':
          return 'info'
        default:
          return 'warning'
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.activityId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    reset() {
      this.form = {
        activityId: undefined,
        title: '',
        summary: '',
        coverUrl: '',
        location: '',
        startTime: '',
        endTime: '',
        maxParticipants: null,
        status: '0',
        description: '',
        remark: '',
        visiblePostIds: []
      }
      this.resetForm('form')
    },
    getPostList() {
      listPost({ status: '0' }).then(response => {
        this.postOptions = response.rows || []
      })
    },
    handleClearPosts() {
      // 清空岗位选择时，确保设置为空数组
      this.$set(this.form, 'visiblePostIds', [])
    },
    triggerCoverUpload() {
      this.$refs.activityCoverUploader.$el.querySelector('input').click()
    },
    beforeCoverUpload(file) {
      const isImg = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp'].includes(file.type)
      if (!isImg) {
        this.$message.error('只能上传 JPG/PNG/WebP 图片')
      }
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB')
      }
      return isImg && isLt5M
    },
    handleCoverUploadSuccess(res) {
      if (res.code === 200) {
        this.form.coverUrl = res.url
        this.$message.success('封面上传成功')
      } else {
        this.$message.error(res.msg || '封面上传失败')
      }
    },
    handleCoverUploadError() {
      this.$message.error('封面上传失败')
    },
    clearCover() {
      // 只清空表单字段，不删除本地文件
      if (this.form.coverUrl) {
        this.$confirm('确认要移除该封面吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.form.coverUrl = ''
          this.$message.success('封面已移除')
        }).catch(() => {
          // 取消操作，不做任何处理
        })
      }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增活动'
    },
    handleUpdate(row) {
      const activityId = row.activityId || this.ids[0]
      getActivity(activityId).then(response => {
        this.form = response.data
        // 处理可见岗位ID，将逗号分隔的字符串转换为数组
        let postIds = []
        if (this.form.visiblePostIds != null && this.form.visiblePostIds !== '') {
          if (typeof this.form.visiblePostIds === 'string') {
            const str = this.form.visiblePostIds.trim()
            if (str !== '') {
              postIds = str.split(',').map(id => {
                const num = Number(id.trim())
                return isNaN(num) ? null : num
              }).filter(id => id !== null)
            }
          } else if (Array.isArray(this.form.visiblePostIds)) {
            // 如果已经是数组，确保是数字数组
            postIds = this.form.visiblePostIds.map(id => {
              const num = Number(id)
              return isNaN(num) ? null : num
            }).filter(id => id !== null)
          }
        }
        // 使用$set确保Vue能正确响应
        this.$set(this.form, 'visiblePostIds', postIds)
        this.open = true
        this.title = '修改活动'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) return
        // 处理可见岗位ID，将数组转换为逗号分隔的字符串
        const formData = { ...this.form }
        // 确保visiblePostIds是数组
        if (!Array.isArray(formData.visiblePostIds)) {
          formData.visiblePostIds = []
        }
        // 过滤掉无效值并转换为字符串
        const validPostIds = formData.visiblePostIds.filter(id => id != null && !isNaN(Number(id)))
        if (validPostIds.length === 0) {
          // 如果没有选择岗位，设置为空字符串，后端会处理为null
          formData.visiblePostIds = ''
        } else {
          formData.visiblePostIds = validPostIds.map(id => String(id)).join(',')
        }
        if (this.form.activityId) {
          updateActivity(formData).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addActivity(formData).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const activityIds = row.activityId ? [row.activityId] : this.ids
      this.$modal.confirm('是否确认删除所选活动？').then(() => {
        return delActivity(activityIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleParticipants(row) {
      this.participantQuery.activityId = row.activityId
      this.participantQuery.activityTitle = row.title
      this.participantQuery.pageNum = 1
      this.getParticipants()
    },
    getParticipants() {
      this.participantLoading = true
      listParticipants(this.participantQuery.activityId, {
        pageNum: this.participantQuery.pageNum,
        pageSize: this.participantQuery.pageSize
      }).then(response => {
        this.participantList = response.rows
        this.participantTotal = response.total
        this.participantOpen = true
        this.participantLoading = false
      })
    },
    auditProof(row, approved) {
      if (!row.participantId) {
        return
      }
      if (approved) {
        this.$confirm('确认通过该学生的参加证明并发放积分？', '审核确认', {
          type: 'warning'
        }).then(() => {
          this.submitAudit(row, true, '')
        }).catch(() => {})
      } else {
        this.$prompt('请输入驳回原因', '驳回证明', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S+/,
          inputErrorMessage: '驳回原因不能为空'
        }).then(({ value }) => {
          this.submitAudit(row, false, value)
        }).catch(() => {})
      }
    },
    submitAudit(row, approved, remark) {
      auditParticipant(row.participantId, {
        approved: approved,
        remark: remark
      }).then(() => {
        this.$modal.msgSuccess('审核完成')
        this.getParticipants()
      })
    },
    handleAuditEnroll(row, approved) {
      if (approved) {
        this.$confirm('确认同意该学生的报名申请？', '报名审核', {
          type: 'warning'
        }).then(() => {
          this.submitEnrollAudit(row, true, '')
        }).catch(() => {})
      } else {
        this.$prompt('请输入拒绝原因', '拒绝报名', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S+/,
          inputErrorMessage: '拒绝原因不能为空'
        }).then(({ value }) => {
          this.submitEnrollAudit(row, false, value)
        }).catch(() => {})
      }
    },
    submitEnrollAudit(row, approved, remark) {
      auditParticipation(row.participantId, {
        approved: approved,
        remark: remark
      }).then(() => {
        this.$modal.msgSuccess('操作成功')
        this.getParticipants()
      })
    },
    handleCancelEnroll(row) {
      this.$confirm('确认取消该学生的报名？', '取消确认', {
        type: 'warning'
      }).then(() => {
        cancelParticipation(row.participantId).then(() => {
          this.$modal.msgSuccess('已取消')
          this.getParticipants()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.form-tip {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.cover-hidden-upload {
  display: none;
}

.cover-preview {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.cover-preview img {
  width: 140px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}
</style>

