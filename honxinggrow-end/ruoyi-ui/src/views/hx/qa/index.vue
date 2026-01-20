<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" inline label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入问题标题" clearable @keyup.enter.native="handleQuery" />
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
        <el-button type="warning" plain icon="el-icon-setting" size="mini" @click="handleAiConfig">
          AI 参数配置
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

    <el-table v-loading="loading" :data="qaList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="qaId" label="编号" width="80" align="center" />
      <el-table-column prop="title" label="问题标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="publishUserName" label="发布人" width="120" />
      <el-table-column prop="updateTime" label="更新时间" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="1"
            inactive-value="2"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
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
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">草稿</el-radio>
            <el-radio label="1">发布</el-radio>
            <el-radio label="2">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-tip">值越小越靠前</span>
        </el-form-item>
        <el-form-item label="解答内容" prop="content">
          <el-input type="textarea" v-model="form.content" :rows="10" placeholder="请输入解答内容（纯文本，供 AI 参考）" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入关键词，以空格分开（AI可以将相关的热点词进行匹配，以完成更好的解答，可留空）" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- AI Config Dialog -->
    <el-dialog title="AI 参数配置" :visible.sync="aiConfigOpen" width="800px" append-to-body>
      <el-form ref="aiConfigForm" :model="aiConfigForm" label-width="100px">
        <el-form-item label="AI 服务商" prop="provider">
          <el-select v-model="aiConfigForm.provider" placeholder="请选择服务商" style="width: 100%">
            <el-option label="智谱 AI (Zhipu)" value="zhipu" />
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问 (Aliyun)" value="aliyun" />
          </el-select>
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="aiConfigForm.apiKey" placeholder="请输入 API Key" show-password />
        </el-form-item>
        <el-form-item label="模型名称" prop="model">
          <el-input v-model="aiConfigForm.model" placeholder="例如: glm-4.6v-flash, qwen-flash-2025-07-28" />
        </el-form-item>
        <el-form-item label="API 地址" prop="apiUrl">
            <el-input v-model="aiConfigForm.apiUrl" placeholder="选填，默认自动匹配" />
        </el-form-item>
        <el-form-item label="TTS 模型" prop="ttsModel">
            <el-input v-model="aiConfigForm.ttsModel" placeholder="例如: qwen3-tts-flash" />
        </el-form-item>
        <el-form-item label="TTS 音色" prop="ttsVoice">
            <el-input v-model="aiConfigForm.ttsVoice" placeholder="例如: Cherry" />
        </el-form-item>
        <el-form-item label="提示词模板" prop="promptTemplate">
          <el-input type="textarea" v-model="aiConfigForm.promptTemplate" :rows="15" placeholder="支持占位符: {reference} 和 {question}" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAiConfig">保 存</el-button>
        <el-button @click="aiConfigOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listQa, getQa, addQa, updateQa, delQa, getAiConfig, updateAiConfig } from '@/api/hx/qa'
import { getToken } from '@/utils/auth'

export default {
  name: 'HxQa',
  components: {},
  data() {
    return {
      loading: false,
      qaList: [],
      total: 0,
      open: false,
      title: '',
      ids: [],
      single: true,
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined
      },
      form: {},
      rules: {
        title: [
          { required: true, message: '问题标题不能为空', trigger: 'blur' },
          { min: 2, max: 100, message: '长度应在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [{ required: true, message: '解答内容不能为空', trigger: 'blur' }]
      },
      aiConfigOpen: false,
      aiConfigForm: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQa(this.queryParams).then(response => {
        this.qaList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        qaId: undefined,
        title: '',
        status: '1', // Default to Published
        sort: 0,
        content: '',
        remark: ''
      }
      this.resetForm('form')
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
      this.ids = selection.map(item => item.qaId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleStatusChange(row) {
      let text = row.status === "1" ? "发布" : "下架";
      this.$modal.confirm('确认要"' + text + '""' + row.title + '"吗？').then(function() {
        return updateQa(row);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "1" ? "2" : "1";
      });
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增问答'
    },
    handleUpdate(row) {
      const qaId = row.qaId || this.ids[0]
      getQa(qaId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改问答'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.qaId) {
          updateQa(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addQa(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const qaIds = row.qaId ? [row.qaId] : this.ids
      this.$modal.confirm('是否确认删除所选问答？').then(() => {
        return delQa(qaIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleAiConfig() {
      getAiConfig().then(response => {
        this.aiConfigForm = response.data || {
            provider: 'zhipu',
            model: 'glm-4.6v-flash'
        }
        this.aiConfigOpen = true
      })
    },
    submitAiConfig() {
        updateAiConfig(this.aiConfigForm).then(() => {
            this.$modal.msgSuccess('配置保存成功')
            this.aiConfigOpen = false
        })
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
</style>
