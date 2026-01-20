这是一个完全由您掌控配置的实施计划。我将只负责代码实现，不进行任何数据初始化。

# 阶段一：后端重构 (Spring Boot)

## 1. 基础设施
- **修改 `HttpUtils`**: 增加支持自定义 Header 的 `sendPost` 方法（等待您接受代码变更）。
- **配置逻辑**: 实现从 `sys_config` 读取 AI 参数的逻辑，若无配置则报错。

## 2. 业务逻辑 (`HxAiService` & `HxAiController`)
- **新增接口**:
    -   `POST /hx/ai/chat`: 对话核心接口。
    -   `GET/POST /hx/ai/config`: 配置管理接口。
- **清理**: 删除 `AppQaController.java`。

---

# 阶段二：后台管理 UI 重构 (Vue)

## 1. 知识库管理页 (`views/hx/qa/index.vue`)
- **UI 升级**: 标题改为“问答知识库”。
- **新增配置入口**: 添加“AI 参数设置”按钮。
- **配置弹窗**: 提供表单供您手动输入 API Key、模型名称 (`glm-4.6v-flash`) 等信息并保存。

---

# 阶段三：小程序重构 (Frontend)

## 1. 页面改造 (`pages/qa/qa`)
- **UI**: 聊天窗口风格。
- **交互**: 连接后端对话接口。

## 2. 清理
- **删除**: `pages/qaDetail`。

---

# 交付物
- 完成所有代码修改后，我会给您**操作指引**，由您亲自在后台界面录入 Key 和模型名称。
