# JC Display AI Agent — 跨境电商智能运营助手

基于 RAG（检索增强生成）的展示架（POP Display）B2B 出口 AI 助手，面向深圳杰创展示公司的跨境业务场景。后端使用 Spring Boot + LangChain4j 集成 LLM，前端使用 Vue 3 + Element Plus。

> **核心能力：知识库基于真实官网数据。** 内置爬虫从 [displaystandpop.com](http://www.displaystandpop.com) 抓取了 **858 款在售产品**，覆盖 **17 个品类**，确保 AI 回答以真实产品信息为依据。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D)](https://vuejs.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-0.36.2-blue)](https://docs.langchain4j.dev/)
[![JDK](https://img.shields.io/badge/JDK-17-orange)](https://openjdk.org/)

---

## 功能特性

| 功能 | 说明 |
|---|---|
| **AI Agent 对话** | 多轮对话 + 工具调用（搜索、网页抓取、翻译、汇率、SEO），支持 DeepSeek / 通义千问 |
| **询盘价值评分** | 对 B2B 询盘进行智能分析，评估客户质量与成交潜力 |
| **产品文案 & 询盘回复** | 基于 Prompt Engineering 生成展示架 B2B 产品详情和英文回复邮件 |
| **多语言翻译** | 展示架行业专业翻译，支持多语言互译 |
| **市场分析** | 分析展示架/POP 产品在不同国家市场的出口机会 |
| **图片识别** | 上传产品图片，LLM 多模态分析并生成结构化产品信息 |
| **Prompt 模板** | 可配置的 Prompt 模板管理，持久化到 MySQL |
| **RAG 知识库** | 10 篇行业知识文档 + **858 款官网真实产品** 向量化检索，增强 LLM 回答准确性 |
| **会话管理** | 对话持久化存储、自动 AI 命名、历史回溯、按类型过滤 |
| **数据仪表盘** | 可视化展示系统运行状态与业务概览 |

---

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.5
- **持久层**: Spring Data JPA + MySQL（自动降级到 H2）
- **LLM 集成**: LangChain4j 0.36.2
  - OpenAI 协议（阿里云 DashScope 通义千问 / 兼容服务）
  - DeepSeek API 适配
- **RAG**: AllMiniLmL6V2EmbeddingModel（ONNX 本地嵌入，384 维）+ InMemoryEmbeddingStore
- **工具调用**: Google 搜索、网页抓取（Jsoup）、汇率转换、SEO 分析
- **HTTP 客户端**: OkHttp 4.12
- **数据抓取**: Jsoup 1.17.2（列表页 HTML 解析 + JSON 数据提取）

### 前端
- **框架**: Vue 3.4 + Composition API
- **UI 库**: Element Plus 2.7
- **状态管理**: Pinia 2
- **路由**: Vue Router 4
- **图表**: ECharts 6
- **构建**: Vite 5

---

## 项目结构

```
项目根目录
├── backend/
│   ├── src/main/java/com/ecommerce/agent/
│   │   ├── agent/
│   │   │   ├── AgentDispatcher.java         # Agent 调度器（LLM + 工具调用 + 自动命名）
│   │   │   └── ConversationManager.java     # 会话 CRUD（MySQL 持久化）
│   │   ├── config/
│   │   │   ├── AIConfig.java                # AI 引擎配置（LLM Provider、RAG 参数）
│   │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   │   └── WebConfig.java               # 跨域配置
│   │   ├── controller/
│   │   │   ├── AgentController.java          # Agent 对话、会话管理、知识库查询
│   │   │   ├── CopywritingController.java    # 产品文案生成
│   │   │   ├── TranslateController.java      # 多语言翻译
│   │   │   ├── AnalysisController.java       # 市场分析
│   │   │   ├── TemplateController.java       # Prompt 模板管理
│   │   │   ├── ProductScraperController.java # 产品爬虫 API
│   │   │   ├── ImageRecognitionController.java  # 图片识别
│   │   │   ├── InquiryScoringController.java    # 询盘评分
│   │   │   └── DeepSeekUsageController.java     # DeepSeek 用量查询
│   │   ├── llm/
│   │   │   ├── MultiModelOrchestrator.java   # 多模型编排
│   │   │   ├── OpenAIProvider.java           # OpenAI 协议适配（含 Vision）
│   │   │   ├── DeepSeekProvider.java         # DeepSeek API 适配
│   │   │   └── PromptTemplateManager.java    # Prompt 模板管理
│   │   ├── model/                            # JPA Entity + DTO
│   │   ├── rag/
│   │   │   ├── RAGConfig.java                # 嵌入模型与向量存储 Bean
│   │   │   ├── RAGService.java               # 检索 + 增强 Prompt
│   │   │   ├── KnowledgeBaseLoader.java      # 文档切分、向量化、入库
│   │   │   ├── KnowledgeDocuments.java       # 10 篇行业知识文档
│   │   │   └── ProductScraper.java           # 官网产品爬虫（Jsoup + JSON 解析）
│   │   ├── repository/                       # Spring Data JPA Repository
│   │   ├── service/
│   │   │   ├── SessionTitleService.java      # LLM 异步自动命名
│   │   │   └── DemoResponseService.java      # 演示模式响应
│   │   ├── tool/                             # 可调用工具集
│   │   │   ├── ToolRegistry.java             # 工具注册中心
│   │   │   ├── SearchTool.java               # Google 搜索
│   │   │   ├── ScraperTool.java              # 网页抓取
│   │   │   ├── TranslateTool.java            # 翻译
│   │   │   ├── CurrencyTool.java             # 汇率转换
│   │   │   └── SEOTool.java                  # SEO 分析
│   │   └── util/
│   │       └── CountryLanguageUtil.java       # 国家/语言映射
│   └── src/main/resources/
│       ├── application.yml                   # 应用配置
│       └── knowledge/
│           ├── company-info.md               # 公司信息
│           ├── export-guide.md               # 出口指南
│           └── products/products.json        # 抓取结果缓存
│
├── frontend/
│   └── src/
│       ├── api/index.js                      # Axios API 封装
│       ├── router/index.js                   # 路由定义
│       ├── views/
│       │   ├── Dashboard.vue                 # 数据仪表盘
│       │   ├── AgentChat.vue                 # AI Agent 对话
│       │   ├── InquiryScoring.vue            # 询盘价值评分
│       │   ├── CopyWriting.vue               # 产品文案
│       │   ├── Translate.vue                 # 多语言翻译
│       │   ├── Analysis.vue                  # 市场分析
│       │   ├── Templates.vue                 # Prompt 模板
│       │   ├── ImageRecognition.vue          # 图片识别
│       │   ├── KnowledgeBase.vue             # 知识库管理
│       │   ├── Channels.vue                  # 渠道管理
│       │   └── ApiIntegration.vue            # API 集成
│       ├── assets/styles/main.css            # 全局样式
│       ├── App.vue                           # 根组件
│       └── main.js                           # 入口
└── README.md
```

---

## 快速启动

### 环境要求

- **JDK 17+**
- **MySQL 8.0+**（可选，无 MySQL 时自动使用 H2 内存数据库）
- **Node.js 18+**
- **Maven 3.8+**

### 1. 配置 API Key

在 `backend/` 下创建 `src/main/resources/application-secrets.yml`：

```yaml
DEEPSEEK_API_KEY: sk-your-deepseek-key
OPENAI_API_KEY: sk-your-openai-key
```

> OpenAI key 指向阿里云 DashScope（通义千问），详见 `application.yml` 中的 `base-url` 配置。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 `http://localhost:8088`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`，API 请求自动代理到后端 8088 端口。

---

## API 接口

### AI Agent

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/agent/chat` | 发送对话消息 |
| `GET` | `/api/agent/sessions` | 获取会话列表 |
| `GET` | `/api/agent/session/{id}/history` | 获取会话历史 |
| `POST` | `/api/agent/session/{id}/clear` | 清除会话历史 |
| `DELETE` | `/api/agent/session/{id}` | 删除会话 |
| `GET` | `/api/agent/knowledge/status` | 知识库状态 |
| `GET` | `/api/agent/knowledge/products` | 分页查询产品列表 |
| `POST` | `/api/agent/knowledge/reload` | 重建 RAG 向量索引 |

### 产品文案

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/copywriting/generate` | 生成产品文案/询盘回复 |

### 多语言翻译

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/translate` | 文本翻译 |

### 市场分析

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/analysis/market` | 市场出口机会分析 |

### 产品爬虫

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/scraper/run` | 触发全量产品抓取 |
| `POST` | `/api/scraper/fill-images` | 补全缺失的产品图片 |
| `POST` | `/api/scraper/cleanup` | 清理非产品数据和假图 |
| `POST` | `/api/scraper/test` | 测试单个 URL 抓取 |
| `POST` | `/api/scraper/debug` | 调试页面 HTML 结构 |
| `PUT` | `/api/scraper/products/{id}/image` | 更新单个产品图片 |
| `GET` | `/api/scraper/status` | 获取抓取状态 |

### 图片识别

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/image/recognize` | 上传图片进行多模态分析 |

---

## 官网产品爬虫

系统内置了对公司官网 `displaystandpop.com` 的产品爬虫，通过解析列表页中 URL 编码的 JSON 数据块提取产品信息，纳入 RAG 知识库：

- **产品来源**: `displaystandpop.com` 17 个品类页面的列表 JSON 数据
- **产品链接**: 关联至 `alibaba.com` 中的实际产品详情页
- **图片来源**: 列表页 JSON 数据中隐藏的 `imageUrls` 字段（x120/x350/x640 缩略图）
- **当前数据**: 858 款产品，其中约 95 款已关联产品图片

### 使用方式

```bash
# 触发全量抓取
curl -X POST http://localhost:8088/api/scraper/run

# 补全产品图片
curl -X POST http://localhost:8088/api/scraper/fill-images

# 清理假图和无效数据
curl -X POST http://localhost:8088/api/scraper/cleanup

# 重建向量索引
curl -X POST http://localhost:8088/api/agent/knowledge/reload

# 查看状态
curl http://localhost:8088/api/scraper/status
```

---

## 配置说明

主要配置项在 `application.yml` 中，通过环境变量覆盖：

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/jc_agent` | MySQL 连接 |
| `MYSQL_USER` | `root` | 数据库用户 |
| `MYSQL_PASS` | `root` | 数据库密码 |
| `OPENAI_API_KEY` | — | LLM API Key（阿里云 DashScope） |
| `OPENAI_BASE_URL` | `https://dashscope.aliyuncs.com/compatible-mode/v1` | LLM 端点 |
| `OPENAI_MODEL` | `qwen3.5-omni-flash` | 默认模型 |
| `DEEPSEEK_API_KEY` | — | DeepSeek API Key |
| `GOOGLE_API_KEY` | — | Google 搜索 API Key |
| `GOOGLE_CX` | — | Google 自定义搜索引擎 ID |

---

## JD Display 知识库

知识库包含两类数据，全部存储于 MySQL 并实时向量化：

1. **行业知识文档**（10 篇）：公司概况、产品规格、出口指南、行业知识
2. **产品数据**（858 条）：17 个品类 + Alibaba 产品链接 + 产品图片
