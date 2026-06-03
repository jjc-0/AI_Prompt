# JC Display AI Agent — 跨境电商智能运营助手

基于 LLM Agent + RAG 的展示架（POP Display）B2B 出口 AI 助手，面向深圳杰创展示公司的跨境业务场景，提供智能对话、文案生成、询盘评分、多语言翻译和市场分析能力。

> **核心亮点：知识库基于真实官网数据。** 内置爬虫从 [displaystandpop.com](http://www.displaystandpop.com) 自动抓取了 **1,435 款在售产品**，覆盖 **17 个品类**，确保 AI 回答始终以真实产品信息为依据，杜绝幻觉。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D)](https://vuejs.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-0.36.2-blue)](https://docs.langchain4j.dev/)
[![JDK](https://img.shields.io/badge/JDK-17-orange)](https://openjdk.org/)

---

## 功能特性

| 功能 | 说明 |
|---|---|
| **数据仪表盘** | 可视化展示系统运行状态、API 调用统计与业务概览 |
| **AI Agent 对话** | 多轮对话 + 工具调用（搜索、网页抓取、翻译、汇率、SEO），支持 DeepSeek / OpenAI 双引擎 |
| **询盘价值评分** | 对 B2B 询盘进行智能分析，评估客户质量与成交潜力 |
| **产品文案 & 询盘回复** | 基于 Prompt Engineering 生成展示架 B2B 产品详情和英文询盘回复邮件，支持协同生成 |
| **多语言翻译** | 展示架行业专业翻译，支持多语言互译与跨境电商本地化增强 |
| **市场分析** | 分析展示架/POP 产品在不同国家市场的出口机会，结合 RAG 知识库 |
| **Prompt 模板** | 可配置的 Prompt 模板管理与预览 |
| **RAG 知识库** | 10 篇行业知识文档 + **1,435 款官网真实产品** 向量化检索，增强 LLM 回答的专业性与准确性 |
| **会话管理** | 对话持久化存储、自动 AI 命名、历史回溯、会话清除 |
| **现代 UI** | Vue 3 + Element Plus，靛蓝紫配色，animate.css 动效 |

---

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.5
- **持久层**: Spring Data JPA + MySQL / H2
- **LLM 集成**: LangChain4j 0.36.2（OpenAI + DeepSeek 适配）
- **RAG**: AllMiniLmL6V2EmbeddingModel（ONNX 本地嵌入 384 维）+ InMemoryEmbeddingStore
- **工具调用**: Google 搜索、网页抓取（Jsoup）、汇率转换、SEO 分析
- **HTTP 客户端**: OkHttp 4.12

### 前端
- **框架**: Vue 3.4 + Composition API
- **UI 库**: Element Plus 2.7 + Element Plus Icons
- **状态管理**: Pinia
- **路由**: Vue Router 4（懒加载 + 过渡动画）
- **图表**: ECharts 6 + vue-echarts
- **动画**: animate.css 4.1
- **构建**: Vite 5

---

## 项目结构

```
项目根目录
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/ecommerce/agent/
│   │   ├── agent/                    # Agent 调度与会话管理
│   │   │   ├── AgentDispatcher.java  # 核心分发器（LLM + 工具调用 + 自动命名）
│   │   │   └── ConversationManager.java  # 会话 CRUD（内存 + MySQL 双写）
│   │   ├── config/                   # 全局配置与异常处理
│   │   ├── controller/               # REST API 控制器
│   │   │   ├── AgentController.java          # Agent 对话与会话管理
│   │   │   ├── CopywritingController.java    # 产品文案生成
│   │   │   ├── TranslateController.java      # 多语言翻译
│   │   │   ├── AnalysisController.java       # 市场分析
│   │   │   ├── TemplateController.java       # Prompt 模板管理
│   │   │   └── ProductScraperController.java # 官网产品爬虫 API
│   │   ├── llm/                      # LLM 提供者适配
│   │   │   ├── MultiModelOrchestrator.java  # 多模型编排（reasoning / completion）
│   │   │   ├── DeepSeekProvider.java        # DeepSeek API 适配
│   │   │   ├── OpenAIProvider.java          # OpenAI API 适配
│   │   │   └── PromptTemplateManager.java   # Prompt 模板管理
│   │   ├── model/                    # 数据模型（DTO + JPA Entity）
│   │   ├── rag/                      # RAG 检索增强
│   │   │   ├── RAGConfig.java        # 嵌入模型与向量存储 Bean 配置
│   │   │   ├── RAGService.java       # 检索 + 增强 Prompt 构建
│   │   │   ├── KnowledgeBaseLoader.java  # 文档切分、向量化、入库
│   │   │   ├── KnowledgeDocuments.java   # 10 篇展示架行业知识文档
│   │   │   └── ProductScraper.java   # 官网产品爬虫（Jsoup）
│   │   ├── repository/               # Spring Data JPA Repository
│   │   ├── service/                  # 业务服务层
│   │   │   ├── SessionTitleService.java  # LLM 异步自动命名
│   │   │   └── DemoResponseService.java  # 演示模式响应
│   │   ├── tool/                     # 可调用工具集
│   │   │   ├── Tool.java             # 工具抽象接口
│   │   │   ├── ToolRegistry.java     # 工具注册中心
│   │   │   ├── SearchTool.java       # Google 搜索工具
│   │   │   ├── ScraperTool.java      # 网页抓取工具
│   │   │   ├── TranslateTool.java    # 翻译工具
│   │   │   ├── CurrencyTool.java     # 汇率转换工具
│   │   │   └── SEOTool.java          # SEO 分析工具
│   │   └── util/                     # 工具类（国家语言解析）
│   └── src/main/resources/
│       ├── application.yml           # 主配置（LLM、RAG、工具、数据库）
│       └── knowledge/                # RAG 知识文档
│           ├── company-info.md
│           └── export-guide.md
│
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/index.js              # Axios API 封装
│       ├── router/index.js           # 路由定义
│       ├── views/                    # 页面视图
│       │   ├── Dashboard.vue         # 数据仪表盘
│       │   ├── AgentChat.vue         # AI Agent 对话页
│       │   ├── InquiryScoring.vue    # 询盘价值评分页
│       │   ├── CopyWriting.vue       # 产品文案页
│       │   ├── Translate.vue         # 多语言翻译页
│       │   ├── Analysis.vue          # 市场分析页
│       │   └── Templates.vue         # Prompt 模板页
│       ├── assets/styles/main.css    # 全局样式（CSS 变量、布局、动画）
│       ├── App.vue                   # 根组件（侧边栏 + 过渡动画）
│       └── main.js                   # 入口（Element Plus + animate.css）
└── README.md
```

---

## 快速启动

### 环境要求

- **JDK 17+**
- **MySQL 8.0+**（或使用内置 H2 自动降级）
- **Node.js 18+**
- **Maven 3.8+**

### 1. 配置 LLM API Key

在项目根目录创建 `backend/src/main/resources/application-secrets.yml`：

```yaml
DEEPSEEK_API_KEY: sk-your-deepseek-key
OPENAI_API_KEY: sk-your-openai-key   # 可选，使用 OpenAI 引擎时配置
```

> 该文件已被 `.gitignore` 排除，不会提交到仓库。

### 2. 启动后端

```bash
cd backend

# 使用 H2 内存数据库（无需 MySQL）
mvn spring-boot:run

# 或指定 MySQL 连接
mvn spring-boot:run -Dspring-boot.run.arguments="--MYSQL_URL=jdbc:mysql://localhost:3306/jc_agent --MYSQL_USER=root --MYSQL_PASS=yourpassword"
```

后端默认运行在 `http://localhost:8088`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`，开发代理自动转发 `/api` 请求到后端。

---

## API 接口

### AI Agent

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/agent/chat` | 发送对话消息 |
| `POST` | `/api/agent/chat/tools` | 带工具调用的对话 |
| `GET` | `/api/agent/sessions` | 获取会话列表 |
| `GET` | `/api/agent/sessions?operation=copywriting` | 按类型过滤会话 |
| `GET` | `/api/agent/session/{id}/history` | 获取会话历史（内存） |
| `GET` | `/api/agent/session/{id}/history/db` | 获取会话历史（数据库） |
| `POST` | `/api/agent/session/{id}/clear` | 清除会话历史 |
| `DELETE` | `/api/agent/session/{id}` | 删除会话 |
| `POST` | `/api/agent/knowledge/search` | 搜索知识库 |
| `GET` | `/api/agent/knowledge/status` | 知识库状态 |

### 产品文案

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/copywriting/generate` | 生成产品文案/询盘回复 |
| `POST` | `/api/copywriting/generate/collaborative` | 协同文案生成（多 Agent 协作）|

### 多语言翻译

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/translate` | 文本翻译（支持本地化增强） |

### 市场分析

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/analysis/market` | 市场出口机会分析 |
| `GET` | `/api/analysis/tools` | 获取可用分析工具列表 |

### Prompt 模板

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/copywriting/templates` | 获取模板列表 |
| `GET` | `/api/copywriting/templates/{id}` | 获取模板详情 |
| `POST` | `/api/copywriting/templates/{id}/preview` | 预览模板渲染结果 |

### 产品爬虫

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/scraper/run` | 触发全量产品爬取（官网 → RAG 知识库）|
| `POST` | `/api/scraper/test` | 测试单个URL抓取效果 |
| `GET` | `/api/scraper/products` | 分页获取已抓取的产品列表 |
| `GET` | `/api/scraper/status` | 获取抓取状态（产品数量、更新时间）|
| `POST` | `/api/scraper/reindex` | 重新加载知识库（含爬取的产品数据）|

---

## 官网产品爬虫

系统内置了针对公司官网 `displaystandpop.com` 的产品爬虫，可一键抓取全部在售产品信息并纳入 RAG 知识库。当前已抓取 **1,435 款真实产品**，覆盖 **17 个品类**，每款产品包含名称、型号、价格、描述、图片等完整信息。

### 使用方式

**方式一：通过 API 触发**

```bash
# 启动后端后，执行全量爬取
curl -X POST http://localhost:8088/api/scraper/run

# 爬取完成后，重新加载知识库
curl -X POST http://localhost:8088/api/scraper/reindex

# 查看抓取结果
curl http://localhost:8088/api/scraper/products
```

**方式二：测试单个页面**

```bash
curl -X POST http://localhost:8088/api/scraper/test \
  -H "Content-Type: application/json" \
  -d '{"url": "http://www.displaystandpop.com/product-url"}'
```

### 爬虫工作流程

```
POST /api/scraper/run
    ├─ 1. 爬取首页，发现分类页和产品链接
    ├─ 2. 逐页遍历分类列表（含分页）
    ├─ 3. 逐个抓取产品详情（名称、型号、价格、描述、图片）
    ├─ 4. 保存为 JSON + Markdown（持久化到 src/main/resources/knowledge/products/）
    └─ 5. 返回抓取结果（产品数量、名称列表、耗时）
```

### 抓取字段

| 字段 | 说明 |
|---|---|
| `name` | 产品名称 |
| `sku` | 产品型号/SKU |
| `price` | 产品价格 |
| `description` | 产品描述（截取前2000字符）|
| `imageUrl` | 产品主图链接 |
| `url` | 产品详情页URL |
| `category` | 所属分类 |

### 已覆盖的品类

| # | 品类 |
|---|---|
| 1 | Packaging（包装盒） |
| 2 | Cardboard Display Stands（瓦楞展示架） |
| 3 | Cardboard Hook Displays（瓦楞钩挂架） |
| 4 | Cardboard Pallet Displays（瓦楞托盘展示架） |
| 5 | Counter Display Boxes（柜台展盒） |
| 6 | Cardboard Dump Bins（瓦楞散装箱） |
| 7 | Brochure & leaflet holders（宣传册架） |
| 8 | Cardboard Totem Displays（瓦楞图腾展架） |
| 9 | Advertising Banners（广告横幅） |
| 10 | Cardboard Trolley Boxes（瓦楞推车盒） |
| 11 | Cardboard Cat Scratcher（猫抓板） |
| 12 | Paper Puzzles（纸质拼图） |
| 13 | Acrylic Displays（亚克力展架） |
| 14 | Gift Box Packaging（礼品盒包装） |
| 15 | Paper Bags（纸质袋） |
| 16 | Corrugated Boxes（瓦楞纸箱） |
| 17 | Cosmetic Packaging（化妆品包装） |

### 注意事项

- 爬虫使用 Jsoup 请求，内置 1.5s 请求间隔防止被反爬
- 最大抓取数量默认 2000 款产品
- 抓取结果持久化保存（JSON + Markdown），重启后无需重新爬取
- 执行 `POST /api/scraper/reindex` 将产品数据向量化后纳入 RAG 检索
- 如网站结构有变化，可调整 `ProductScraper.java` 中的 CSS 选择器

---

## 核心设计

### LLM Agent 工具调用流程

```
用户输入 → AgentDispatcher
    ├─ 会话管理（新建/恢复 + 异步 AI 自动命名）
    ├─ MultiModelOrchestrator（DeepSeek / OpenAI 双引擎）
    ├─ RAGService（检索相关业务知识增强 Prompt）
    └─ ToolRegistry（Google 搜索 / 网页抓取 / 翻译 / 汇率 / SEO）
         → 解析 LLM 返回的 tool_call → 执行工具 → 注入结果 → 最终回复
```

### RAG 知识库

- **嵌入模型**: `AllMiniLmL6V2EmbeddingModel`（384 维向量，本地 ONNX 推理）
- **内置文档**: 10 篇展示架行业知识（公司信息、产品规格、市场分析、合规认证、物流运输、行业展会、术语、B2B 优化、询盘邮件、本地化指南）
- **真实产品数据**: 从官网 `displaystandpop.com` 自动抓取 **1,435 款在售产品**，覆盖包装盒、展示架、钩挂架、托盘架、柜台展盒、散装箱、宣传册架、图腾展架、广告牌、推车盒、猫抓板、纸拼图、亚克力展架、礼品盒、纸袋、瓦楞箱、化妆品包装等 **17 个品类**
- **切分策略**: `DocumentSplitter.recursive(500, 50)`
- **检索**: 基于余弦相似度，默认 Top 5，最低阈值 0.6

### 会话自动命名

首次对话时，先用用户消息前 30 字符作为即时标题，同时异步调用 LLM 提炼 10 字以内的精准标题，2-3 秒后自动更新侧边栏。

### 数据库自动降级

- **MySQL 可用时**: 使用 `jc_agent` 数据库，JPA `ddl-auto: update` 自动建表
- **MySQL 不可用时**: 自动降级到 H2 内存数据库，`/h2-console` 可查看

| 表名 | 说明 |
|---|---|
| `conversation_session` | 会话元数据（标题、类型、消息数） |
| `conversation_record` | 对话消息记录 |
| `prompt_template` | Prompt 模板 |

---

## 配置项

核心配置参见 `application.yml`：

| 配置节 | 说明 |
|---|---|
| `ai.providers.deepseek` | DeepSeek API 配置 |
| `ai.providers.openai` | OpenAI API 配置 |
| `ai.agent.max-conversation-rounds` | 最大对话轮次（默认 10） |
| `ai.agent.context-window-size` | 上下文窗口大小（默认 20） |
| `ai.agent.tool-call-timeout` | 工具调用超时（默认 30000ms） |
| `ai.rag.max-results` | RAG 检索最大结果数（默认 5） |
| `ai.rag.min-score` | 相似度最低阈值（默认 0.6） |
| `tools.search.*` | Google 搜索工具配置 |
| `tools.scraper.*` | 网页抓取工具配置 |

---

## License

深圳市杰创展示公司版权所有 · For JC Display internal use.
