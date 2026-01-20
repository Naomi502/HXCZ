<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" inline label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入故事标题" clearable @keyup.enter.native="handleQuery" />
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
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">
          删除
        </el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="storyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="storyId" label="编号" width="80" align="center" />
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
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读次数" width="100" align="center" />
      <el-table-column prop="publishUserName" label="发布人" width="120" />
      <el-table-column prop="publishTime" label="发布时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.publishTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="故事标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入故事标题" />
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
            ref="storyCoverUploader"
            :action="uploadUrl"
            :show-file-list="false"
            :headers="uploadHeaders"
            :data="{ fileCategory: 'cover', businessType: 'story', businessField: 'coverUrl' }"
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
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="item in statusOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-tip">值越小越靠前</span>
        </el-form-item>
        <el-form-item label="正文内容" prop="content">
          <editor v-model="form.content" :min-height="260" />
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
  </div>
</template>

<script>
import Editor from '@/components/Editor'
import { listStory, getStory, addStory, updateStory, delStory } from '@/api/hx/story'
import { getToken } from '@/utils/auth'
import request from '@/utils/request'

export default {
  name: 'HxStory',
  components: { Editor },
  data() {
    return {
      loading: false,
      storyList: [],
      total: 0,
      open: false,
      title: '',
      ids: [],
      single: true,
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: undefined
      },
      form: {},
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      },
      rules: {
        title: [
          { required: true, message: '故事标题不能为空', trigger: 'blur' },
          { min: 2, max: 100, message: '长度应在 2 到 100 个字符', trigger: 'blur' }
        ],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        content: [{ required: true, message: '故事正文不能为空', trigger: 'blur' }]
      },
      statusOptions: [
        { label: '草稿', value: '0' },
        { label: '发布', value: '1' },
        { label: '下架', value: '2' }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listStory(this.queryParams).then(response => {
        this.storyList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    statusName(value) {
      const item = this.statusOptions.find(opt => opt.value === value)
      return item ? item.label : value
    },
    statusTag(value) {
      switch (value) {
        case '1':
          return 'success'
        case '2':
          return 'info'
        default:
          return 'warning'
      }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        storyId: undefined,
        title: '',
        summary: '',
        coverUrl: '',
        status: '0',
        sort: 0,
        content: '',
        remark: ''
      }
      this.resetForm('form')
    },
    triggerCoverUpload() {
      this.$refs.storyCoverUploader.$el.querySelector('input').click()
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
    handleCoverUploadError(err) {
      // err是一个Error对象，包含message属性
      this.$message.error('封面上传失败：' + (err.message || '网络错误'))
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
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.storyId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增故事'
    },
    handleUpdate(row) {
      const storyId = row.storyId || this.ids[0]
      getStory(storyId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改故事'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.storyId) {
          updateStory(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addStory(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const storyIds = row.storyId ? [row.storyId] : this.ids
      this.$modal.confirm('是否确认删除所选故事？').then(() => {
        return delStory(storyIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
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
  width: 120px;
  height: 70px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}
</style>

