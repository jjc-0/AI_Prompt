<template>
  <div class="kb-container">
    <!-- 状态概览卡片 -->
    <div class="kb-stats">
      <div class="kb-stat-card">
        <div class="kb-stat-icon" style="background:linear-gradient(135deg,#6366f1,#818cf8)">
          <el-icon :size="22"><Document /></el-icon>
        </div>
        <div class="kb-stat-body">
          <div class="kb-stat-value">{{ status.knowledgeDocumentCount ?? 0 }}</div>
          <div class="kb-stat-label">知识文档</div>
        </div>
      </div>
      <div class="kb-stat-card">
        <div class="kb-stat-icon" style="background:linear-gradient(135deg,#10b981,#34d399)">
          <el-icon :size="22"><Goods /></el-icon>
        </div>
        <div class="kb-stat-body">
          <div class="kb-stat-value">{{ status.productCount ?? 0 }}</div>
          <div class="kb-stat-label">产品数据</div>
        </div>
      </div>
      <div class="kb-stat-card">
        <div class="kb-stat-icon" style="background:linear-gradient(135deg,#f59e0b,#fbbf24)">
          <el-icon :size="22"><Coin /></el-icon>
        </div>
        <div class="kb-stat-body">
          <div class="kb-stat-value">{{ status.storeType || 'MySQL' }}</div>
          <div class="kb-stat-label">存储引擎</div>
        </div>
      </div>
      <div class="kb-stat-card">
        <div class="kb-stat-icon" style="background:linear-gradient(135deg,#ef4444,#f87171)">
          <el-icon :size="22"><Search /></el-icon>
        </div>
        <div class="kb-stat-body">
          <div class="kb-stat-value">{{ status.enabled ? '正常' : '不可用' }}</div>
          <div class="kb-stat-label">向量索引</div>
        </div>
      </div>
    </div>

    <div class="kb-main">
      <!-- 左侧：知识文档列表 + 搜索 -->
      <div class="kb-left">
        <div class="kb-panel">
          <div class="kb-panel-header">
            <span class="kb-panel-title">
              <el-icon :size="16"><Collection /></el-icon>
              知识文档 ({{ documents.length }})
            </span>
            <el-button size="small" type="primary" :loading="reloading" @click="handleReload">
              <el-icon :size="14"><Refresh /></el-icon>
              重建索引
            </el-button>
          </div>

          <!-- RAG 搜索 -->
          <div class="kb-search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜索知识库..."
              clearable
              size="small"
              @keyup.enter="handleSearch"
            >
              <template #suffix>
                <el-icon :size="14" style="cursor:pointer" @click="handleSearch"><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <!-- 搜索中状态 -->
          <div v-if="searching" class="kb-loading-center">
            <el-icon class="is-loading" :size="24"><Loading /></el-icon>
            <span>搜索中...</span>
          </div>

          <!-- 搜索结果 -->
          <div v-else-if="searchResults.length > 0" class="kb-search-results">
            <div class="kb-search-header">
              找到 {{ searchResults.length }} 条结果
              <el-button text size="small" @click="clearSearch">清除</el-button>
            </div>
            <div v-for="(r, i) in searchResults" :key="i" class="kb-search-item">
              <div class="kb-search-snippet">{{ r.snippet }}</div>
              <el-collapse v-if="r.fullText && r.fullText.length > 200">
                <el-collapse-item title="展开全文">
                  <pre class="kb-search-full">{{ r.fullText }}</pre>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>

          <!-- 文档列表 -->
          <div v-else class="kb-doc-list">
            <div
              v-for="doc in pagedDocs"
              :key="doc.id"
              class="kb-doc-item"
              :class="{ active: selectedDoc?.id === doc.id }"
              @click="selectDoc(doc)"
            >
              <div class="kb-doc-title">{{ doc.title }}</div>
              <div class="kb-doc-meta">
                <el-tag size="small" type="info" effect="plain">{{ doc.category || '未分类' }}</el-tag>
                <span class="kb-doc-len">{{ doc.length || doc.content?.length || 0 }} 字</span>
              </div>
            </div>

            <el-pagination
              v-if="docTotalPages > 1"
              v-model:current-page="docPage"
              :page-size="docPageSize"
              :total="documents.length"
              layout="prev, next"
              small
              class="kb-doc-pager"
            />
          </div>
        </div>
      </div>

      <!-- 右侧：产品列表 / 文档详情 -->
      <div class="kb-right">
        <!-- 文档详情 -->
        <div v-if="selectedDoc" class="kb-panel">
          <div class="kb-panel-header">
            <span class="kb-panel-title">
              <el-icon :size="16"><Reading /></el-icon>
              文档详情
            </span>
            <el-button text size="small" @click="selectedDoc = null">
              <el-icon :size="14"><Close /></el-icon>
              关闭
            </el-button>
          </div>
          <div class="kb-doc-detail">
            <h3>{{ selectedDoc.title }}</h3>
            <div class="kb-doc-info">
              <el-tag size="small" type="info">{{ selectedDoc.category || '未分类' }}</el-tag>
              <span>创建: {{ formatTime(selectedDoc.createdAt) }}</span>
              <span>更新: {{ formatTime(selectedDoc.updatedAt) }}</span>
            </div>
            <div class="kb-doc-content">{{ selectedDoc.content }}</div>
          </div>
        </div>

        <!-- 产品列表 -->
        <div v-else class="kb-panel">
          <div class="kb-panel-header">
            <span class="kb-panel-title">
              <el-icon :size="16"><Goods /></el-icon>
              产品数据库 ({{ productTotal }} 条)
            </span>
            <span class="kb-panel-hint">来源：MySQL 持久化存储</span>
          </div>

          <div v-if="productsLoading" class="kb-loading-center">
            <el-icon class="is-loading" :size="24"><Loading /></el-icon>
            <span>加载产品...</span>
          </div>

          <div v-else class="kb-product-list">
            <div v-for="p in products" :key="p.id" class="kb-product-item">
              <img
                v-if="p.imageUrl"
                :src="p.imageUrl"
                class="kb-product-img"
                loading="lazy"
                @error="e => e.target.style.display='none'"
              />
              <div class="kb-product-body">
                <div class="kb-product-name" :title="p.name">{{ p.name }}</div>
                <div class="kb-product-meta">
                  <span v-if="p.price" class="kb-product-price">{{ p.price }}</span>
                  <span v-if="p.sku">SKU: {{ p.sku }}</span>
                  <span v-if="p.category">
                    <el-tag size="small" effect="plain">{{ p.category }}</el-tag>
                  </span>
                </div>
                <div v-if="p.description" class="kb-product-desc">{{ p.description }}</div>
                <a v-if="p.url" :href="p.url" target="_blank" class="kb-product-link">查看详情 →</a>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="productTotalPages > 1"
            v-model:current-page="productPage"
            :page-size="productPageSize"
            :total="productTotal"
            layout="prev, pager, next, total"
            size="small"
            class="kb-product-pager"
            @current-change="loadProducts"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Goods, Coin, Search, Loading, Collection, Refresh, Reading, Close } from '@element-plus/icons-vue'
import { agentApi } from '../api'

// 状态
const status = ref({})
const reloading = ref(false)
const documents = ref([])
const products = ref([])
const productsLoading = ref(false)
const productTotal = ref(0)
const productTotalPages = ref(0)
const productPage = ref(1)
const productPageSize = 20
const selectedDoc = ref(null)
const searchQuery = ref('')
const searching = ref(false)
const searchResults = ref([])

// 文档分页
const docPage = ref(1)
const docPageSize = 12
const docTotalPages = computed(() => Math.ceil(documents.value.length / docPageSize))
const pagedDocs = computed(() => {
  const start = (docPage.value - 1) * docPageSize
  return documents.value.slice(start, start + docPageSize)
})

// 加载状态
const loadStatus = async () => {
  try {
    const { data } = await agentApi.knowledgeStatus()
    status.value = data
  } catch {}
}

const loadDocuments = async () => {
  try {
    const { data } = await agentApi.getDocuments()
    documents.value = data.documents || []
  } catch {}
}

const loadProducts = async () => {
  productsLoading.value = true
  try {
    const { data } = await agentApi.getProducts(productPage.value - 1, productPageSize)
    products.value = data.items || []
    productTotal.value = data.total || 0
    productTotalPages.value = data.totalPages || 0
  } catch {}
  productsLoading.value = false
}

const handleReload = async () => {
  reloading.value = true
  try {
    await agentApi.reloadKnowledge()
    ElMessage.success('知识库索引重建完成')
    await Promise.all([loadStatus(), loadDocuments()])
  } catch {
    ElMessage.error('重建失败')
  }
  reloading.value = false
}

const handleSearch = async () => {
  if (!searchQuery.value.trim()) return
  searching.value = true
  selectedDoc.value = null
  try {
    const { data } = await agentApi.searchKnowledge(searchQuery.value.trim(), 8)
    searchResults.value = data.results || []
  } catch {}
  searching.value = false
}

const clearSearch = () => {
  searchQuery.value = ''
  searchResults.value = []
}

const selectDoc = (doc) => {
  selectedDoc.value = doc
  searchResults.value = []
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  Promise.all([loadStatus(), loadDocuments(), loadProducts()])
})
</script>

<style scoped>
.kb-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0;
  overflow: hidden;
}

.kb-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.kb-stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}
.kb-stat-icon {
  width: 46px; height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
}
.kb-stat-body { flex: 1; }
.kb-stat-value { font-size: 22px; font-weight: 700; color: #1e293b; line-height: 1.2; }
.kb-stat-label { font-size: 12px; color: #94a3b8; margin-top: 2px; }

.kb-main {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
}
.kb-left {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.kb-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.kb-panel {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.kb-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
}
.kb-panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 6px;
}
.kb-panel-hint { font-size: 12px; color: #94a3b8; }

/* 搜索 */
.kb-search-box {
  padding: 10px 14px;
  border-bottom: 1px solid #f1f5f9;
}
.kb-doc-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px 0;
}
.kb-doc-item {
  padding: 10px 18px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all .15s;
}
.kb-doc-item:hover { background: #f8fafc; }
.kb-doc-item.active { background: #eef2ff; border-left-color: #6366f1; }
.kb-doc-title {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kb-doc-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
  color: #94a3b8;
}
.kb-doc-len { color: #94a3b8; }
.kb-doc-pager {
  padding: 8px 18px;
  justify-content: center;
  border-top: 1px solid #f1f5f9;
}

/* 文档详情 */
.kb-doc-detail {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
}
.kb-doc-detail h3 {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 10px;
}
.kb-doc-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 14px;
}
.kb-doc-content {
  font-size: 14px;
  line-height: 1.8;
  color: #475569;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 搜索结果 */
.kb-search-results {
  flex: 1;
  overflow-y: auto;
  padding: 10px 14px;
}
.kb-search-header {
  font-size: 13px;
  color: #6366f1;
  margin-bottom: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.kb-search-item {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
}
.kb-search-snippet {
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}
.kb-search-full {
  font-size: 12px;
  color: #64748b;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
  margin-top: 8px;
  padding: 8px;
  background: #f1f5f9;
  border-radius: 6px;
}

/* 产品列表 */
.kb-product-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 14px;
}
.kb-product-item {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}
.kb-product-item:last-child { border-bottom: none; }
.kb-product-img {
  width: 80px; height: 80px;
  object-fit: contain;
  border-radius: 8px;
  background: #f8fafc;
  flex-shrink: 0;
  border: 1px solid #e2e8f0;
}
.kb-product-body { flex: 1; min-width: 0; }
.kb-product-name {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.kb-product-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
  flex-wrap: wrap;
}
.kb-product-price { color: #ef4444; font-weight: 600; }
.kb-product-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.kb-product-link {
  font-size: 12px;
  color: #6366f1;
  text-decoration: none;
  display: inline-block;
  margin-top: 4px;
}
.kb-product-link:hover { text-decoration: underline; }
.kb-product-pager {
  padding: 10px 18px;
  justify-content: center;
  border-top: 1px solid #f1f5f9;
}

.kb-loading-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 13px;
}
</style>
