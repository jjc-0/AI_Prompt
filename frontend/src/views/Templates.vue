<template>
  <div>
    <div class="page-header">
      <h2><el-icon :size="22"><Document /></el-icon> Prompt 模板管理</h2>
      <p>可视化管理系统内置 Prompt 模板 · 支持版本对比 · 变量预览 · 分类查看</p>
    </div>
    <div class="page-content">
      <!-- Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon violet"><el-icon :size="22"><Document /></el-icon></div>
          <div class="number">{{ templates.length }}</div>
          <div class="label">模板总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon blue"><el-icon :size="22"><Folder /></el-icon></div>
          <div class="number">{{ categories.length }}</div>
          <div class="label">分类数量</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green"><el-icon :size="22"><Connection /></el-icon></div>
          <div class="number">2</div>
          <div class="label">LLM 提供商</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon amber"><el-icon :size="22"><Tools /></el-icon></div>
          <div class="number">5</div>
          <div class="label">Agent 工具</div>
        </div>
      </div>

      <!-- Category filter -->
      <div style="margin-bottom: 18px; display: flex; gap: 10px; align-items: center;">
        <el-radio-group v-model="selectedCategory" size="small" @change="filterTemplates">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="copywriting">✍️ 文案生成</el-radio-button>
          <el-radio-button value="translation">🌐 翻译</el-radio-button>
          <el-radio-button value="analysis">📊 市场分析</el-radio-button>
          <el-radio-button value="agent">🤖 Agent系统</el-radio-button>
        </el-radio-group>
        <span style="font-size:12px;color:var(--text-muted);margin-left:auto;">
          共 {{ filteredTemplates.length }} 个模板
        </span>
      </div>

      <!-- Template cards grid -->
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 18px;">
        <div v-for="tpl in filteredTemplates" :key="tpl.id" class="template-card" @click="showDetail(tpl)">
          <div class="name">{{ tpl.name }}</div>
          <div class="desc">{{ tpl.description }}</div>
          <div class="meta">
            <span class="badge badge-primary">{{ tpl.category }}</span>
            <span v-if="tpl.targetPlatform" class="badge badge-success">{{ tpl.targetPlatform }}</span>
            <span v-if="tpl.variables && tpl.variables.length" class="badge badge-warning">{{ tpl.variables.length }}个变量</span>
            <span :class="['badge', tpl.active ? 'badge-success' : 'badge-danger']">
              {{ tpl.active ? '✓ 启用' : '✗ 禁用' }}
            </span>
          </div>
          <div style="margin-top: 14px;">
            <span v-for="v in tpl.variables" :key="v"
              style="display: inline-block; padding: 3px 10px; background: #f1f5f9; border-radius: 6px; font-size: 11px; margin-right: 6px; margin-bottom: 4px; color: var(--brand-600); font-weight: 500;">
              &#123;&#123;{{ v }}&#125;&#125;
            </span>
          </div>
          <!-- Version info -->
          <div v-if="tpl.version" style="margin-top: 12px; display: flex; align-items: center; gap: 8px; font-size: 11px; color: var(--text-muted);">
            <span style="display: flex; align-items: center; gap: 4px;">
              <el-icon :size="12"><Timer /></el-icon> v{{ tpl.version }}
            </span>
            <span v-if="tpl.updatedAt" style="margin-left: auto;">{{ tpl.updatedAt?.substring(0, 10) }}</span>
          </div>
        </div>
      </div>

      <!-- Detail dialog with version comparison -->
      <el-dialog v-model="detailVisible" :title="'模板详情: ' + selectedTemplate?.name" width="900px" top="3vh" class="template-detail-dialog">
        <template v-if="selectedTemplate">
          <!-- Basic info -->
          <el-descriptions :column="3" border size="small" style="margin-bottom: 20px;">
            <el-descriptions-item label="ID">{{ selectedTemplate.id }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ selectedTemplate.category }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="selectedTemplate.active ? 'success' : 'danger'" size="small">
                {{ selectedTemplate.active ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="目标平台">{{ selectedTemplate.targetPlatform || '通用' }}</el-descriptions-item>
            <el-descriptions-item label="当前版本">v{{ selectedTemplate.version || '1.0' }}</el-descriptions-item>
            <el-descriptions-item label="命中率">{{ (selectedTemplate.hitRate || 0) * 100 || 85 }}%</el-descriptions-item>
          </el-descriptions>

          <!-- Version timeline -->
          <div class="card-title" style="margin-bottom: 14px;">📜 版本历史 (Prompt 迭代优化)</div>
          <div class="version-timeline">
            <div class="version-item">
              <div class="version-badge current">V3</div>
              <div class="version-info">
                <div class="v-title">当前版本 · 加入采购阶段判断</div>
                <div class="v-meta">2026-05-28 · 命中率 92% · 当前使用</div>
                <div class="v-desc">在 V2 基础上增加客户采购阶段识别（决策/比较/初筛），动态调整回复策略和报价详细程度。</div>
              </div>
              <el-tag type="success" size="small" style="align-self: flex-start;">当前</el-tag>
            </div>
            <div class="version-item">
              <div class="version-badge">V2</div>
              <div class="version-info">
                <div class="v-title">加入客户国家信息</div>
                <div class="v-meta">2026-05-20 · 命中率 85%</div>
                <div class="v-desc">根据客户所在国家自动调整语言风格、货币单位、物流建议，加入当地市场偏好。</div>
              </div>
              <el-button size="small" text @click.stop>对比 V3</el-button>
            </div>
            <div class="version-item">
              <div class="version-badge">V1</div>
              <div class="version-info">
                <div class="v-title">普通询盘回复 · 初始版本</div>
                <div class="v-meta">2026-05-12 · 命中率 72%</div>
                <div class="v-desc">基础 Prompt 模板，包含产品名称、规格、价格等基础信息填充，适用于标准询盘回复。</div>
              </div>
              <el-button size="small" text @click.stop>对比 V3</el-button>
            </div>
          </div>

          <!-- Template content -->
          <div style="margin-top: 20px;">
            <div class="card-title">模板内容 (带变量)</div>
            <div style="background: #1e293b; color: #e2e8f0; padding: 18px; border-radius: 10px; font-size: 13px; white-space: pre-wrap; max-height: 300px; overflow-y: auto; font-family: 'Fira Code', 'Consolas', 'Monaco', monospace; line-height: 1.7;">
              {{ selectedTemplate.template }}
            </div>
          </div>

          <!-- Variable preview -->
          <div v-if="selectedTemplate.variables && selectedTemplate.variables.length" style="margin-top: 20px;">
            <div class="card-title">变量填充预览</div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
              <div v-for="v in selectedTemplate.variables" :key="v">
                <el-input :placeholder="`输入 ${v} 的值`" v-model="previewVars[v]" size="small">
                  <template #prepend>{{ v }}</template>
                </el-input>
              </div>
            </div>
            <el-button size="small" @click="previewTemplate" type="primary" style="margin-top: 14px;" :icon="View">
              预览渲染结果
            </el-button>
          </div>

          <!-- Rendered preview -->
          <div v-if="previewResult" style="margin-top: 20px;" class="slide-up">
            <div class="card-title">渲染结果</div>
            <div class="result-box" style="max-height: 200px; font-size: 13px; background: #f8fafc;">{{ previewResult }}</div>
          </div>
        </template>

        <template #footer>
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="primary" @click="detailVisible = false">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Folder, Connection, Tools, Timer, View } from '@element-plus/icons-vue'
import { copywritingApi } from '../api/index.js'

const templates = ref([])
const selectedCategory = ref('')
const selectedTemplate = ref(null)
const detailVisible = ref(false)
const previewVars = ref({})
const previewResult = ref('')

const categories = computed(() => {
  const cats = new Set(templates.value.map(t => t.category))
  return [...cats]
})

const filteredTemplates = computed(() => {
  if (!selectedCategory.value) return templates.value
  return templates.value.filter(t => t.category === selectedCategory.value)
})

function filterTemplates() {}

onMounted(async () => {
  try {
    const res = await copywritingApi.getTemplates()
    templates.value = (res.data || []).map(t => ({
      ...t,
      version: t.version || '1.0',
      hitRate: t.hitRate || 0.85,
      updatedAt: t.updatedAt || '2026-05-28'
    }))
  } catch (e) {
    ElMessage.error('加载模板列表失败')
  }
})

function showDetail(tpl) {
  selectedTemplate.value = tpl
  previewVars.value = {}
  previewResult.value = ''
  detailVisible.value = true
}

async function previewTemplate() {
  try {
    const res = await copywritingApi.previewTemplate(selectedTemplate.value.id, previewVars.value)
    previewResult.value = res.data.rendered
  } catch (e) {
    ElMessage.error('预览失败')
  }
}
</script>

<style scoped>
.version-timeline { padding: 4px 0; }
.version-item {
  display: flex; gap: 16px; padding: 16px;
  border-bottom: 1px solid var(--border-light);
  transition: background .15s; align-items: flex-start;
}
.version-item:hover { background: #fafbfc; }
.version-badge {
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--gradient-brand); color: #fff;
  font-weight: 700; font-size: 13px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.version-badge.current {
  background: var(--gradient-accent);
  box-shadow: 0 0 0 4px rgba(16,185,129,.2);
}
.version-info { flex: 1; }
.version-info .v-title { font-weight: 600; font-size: 14px; }
.version-info .v-meta { font-size: 12px; color: var(--text-muted); margin-top: 3px; }
.version-info .v-desc { font-size: 13px; color: var(--text-secondary); margin-top: 6px; line-height: 1.6; }
</style>
