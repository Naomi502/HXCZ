<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理标记" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择处理标记" clearable>
          <el-option label="未处理" value="0" />
          <el-option label="已处理" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:hole:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:hole:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:hole:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:hole:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="holeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="treeHoleId" />
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="学号" align="center" prop="userName" />
      <el-table-column label="用户昵称" align="center" prop="nickName" />
      <el-table-column label="部门名称" align="center" prop="deptName" />
      <el-table-column label="树洞内容" align="center" prop="content" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="showContentDialog(scope.row.content)">{{ scope.row.content.length > 20 ? scope.row.content.substring(0, 20) + '...' : scope.row.content }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="处理标记" align="center" prop="status" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'warning'" size="small">
            {{ scope.row.status === '1' ? '已处理' : '未处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleMarkProcessed(scope.row)"
            v-hasPermi="['system:hole:edit']"
            v-if="scope.row.status !== '1'"
          >标记已处理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleMarkUnprocessed(scope.row)"
            v-hasPermi="['system:hole:edit']"
            v-if="scope.row.status === '1'"
          >标记未处理</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:hole:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改心灵树洞对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="树洞内容" prop="content">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
        <el-form-item label="处理标记" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">未处理</el-radio>
            <el-radio label="1">已处理</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 内容查看对话框 -->
    <el-dialog title="树洞内容" :visible.sync="contentDialogVisible" width="600px" append-to-body>
      <div class="content-view">
        {{ contentToShow }}
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="contentDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listHole, getHole, delHole, addHole, updateHole } from "@/api/system/hole"

export default {
  name: "Hole",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 心灵树洞表格数据
      holeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示内容查看对话框
      contentDialogVisible: false,
      // 要显示的内容
      contentToShow: '',
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        userName: null,
        content: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "用户ID不能为空", trigger: "blur" }
        ],
        userName: [
          { required: true, message: "用户名不能为空", trigger: "blur" }
        ],
        content: [
          { required: true, message: "树洞内容不能为空", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "创建时间不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "处理标记不能为空", trigger: "change" }
        ],
        delFlag: [
          { required: true, message: "删除标志不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询心灵树洞列表 */
    getList() {
      this.loading = true
      listHole(this.queryParams).then(response => {
        this.holeList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 显示内容对话框 */
    showContentDialog(content) {
      this.contentToShow = content
      this.contentDialogVisible = true
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        treeHoleId: null,
        userId: null,
        userName: null,
        content: null,
        createTime: null,
        status: "0",
        delFlag: "0"
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.treeHoleId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加心灵树洞"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const treeHoleId = row.treeHoleId || this.ids
      getHole(treeHoleId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改心灵树洞"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.treeHoleId != null) {
            updateHole(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addHole(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const treeHoleIds = row.treeHoleId || this.ids
      this.$modal.confirm('是否确认删除心灵树洞编号为"' + treeHoleIds + '"的数据项？').then(function() {
        return delHole(treeHoleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/hole/export', {
        ...this.queryParams
      }, `hole_${new Date().getTime()}.xlsx`)
    },
    /** 标记为已处理 */
    handleMarkProcessed(row) {
      const form = {
        treeHoleId: row.treeHoleId,
        userId: row.userId,
        userName: row.userName,
        content: row.content,
        status: "1"
      }
      updateHole(form).then(response => {
        this.$modal.msgSuccess("标记成功")
        this.getList()
      })
    },
    /** 标记为未处理 */
    handleMarkUnprocessed(row) {
      const form = {
        treeHoleId: row.treeHoleId,
        userId: row.userId,
        userName: row.userName,
        content: row.content,
        status: "0"
      }
      updateHole(form).then(response => {
        this.$modal.msgSuccess("标记成功")
        this.getList()
      })
    }
  }
}
</script>
