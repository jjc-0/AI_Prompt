<template>
  <div>
    <div class="page-header">
      <h2>📋 Prompt 模板管理</h2>
      <p>可视化管理系统内置的Prompt模板，支持预览、变量渲染、分类查看</p>
    </div>
    <div class="page-content">
      <div class="stats-row">
        <div class="stat-card">
          <div class="number">{{ templates.length }}</div>
          <div class="label">模板总数</div>
        </div>
        <div class="stat-card">
          <div class="number">{{ categories.length }}</div>
          <div class="label">分类数量</div>
        </div>
        <div class="stat-card">
          <div class="number">2</div>
          <div class="label">LLM 提供方</div>
        </div>
        <div class="stat-card">
          <div class="number">5</div>
          <div class="label">Agent 工具</div>
        </div>
      </div>

      <div style="margin-bottom: 16px; display: flex; gap: 8px;">
        <el-radio-group v-model="selectedCategory" size="small" @change="filterTemplates">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="copywriting">文案生成</el-radio-button>
          <el-radio-button value="translation">翻译</el-radio-button>
          <el-radio-button value="analysis">市场分析</el-radio-button>
          <el-radio-button value="agent">Agent系统</el-radio-button>
        </el-radio-group>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px;">
        <div v-for="tpl in filteredTemplates" :key="tpl.id" class="template-card" @click="showDetail(tpl)">
          <div class="name">{{ tpl.name }}</div>
          <div class="desc">{{ tpl.description }}</div>
          <div class="meta">
            <span class="badge badge-primary">{{ tpl.category }}</span>
            <span v-if="tpl.targetPlatform" class="badge badge-success">{{ tpl.targetPlatform }}</span>
            <span v-if="tpl.variables && tpl.variables.length" class="badge badge-warning">{{ tpl.variables.length }}个变量</span>
            <span :class="['badge', tpl.active ? 'badge-success' : 'badge-warning']">{{ tpl.active ? '启用' : '禁用' }}</span>
          </div>
          <div style="margin-top: 12px;">
            <span v-for="v in tpl.variables" :key="v" style="display: inline-block; padding: 2px 8px; background: #f5f7fa; border-radius: 4px; font-size: 11px; margin-right: 4px; color: var(--text-secondary);">
              &#123;&#123;{{ v }}&#125;&#125;
            </span>
          </div>
        </div>
      </div>

      <el-dialog v-model="detailVisible" :title="'模板详情: ' + selectedTemplate?.name" width="800px" top="5vh">
        <template v-if="selectedTemplate">
          <el-descriptions :column="2" border size="small" style="margin-bottom: 16px;">
            <el-descriptions-item label="ID">{{ selectedTemplate.id }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ selectedTemplate.category }}</el-descriptions-item>
            <el-descriptions-item label="目标平台">{{ selectedTemplate.targetPlatform || '通用' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ selectedTemplate.active ? '启用' : '禁用' }}</el-descriptions-item>
          </el-descriptions>

          <div class="card-title">模板内容 (带变量)</div>
          <div style="background: #1e1e2e; color: #d4d4d4; padding: 16px; border-radius: 8px; font-size: 13px; white-space: pre-wrap; max-height: 300px; overflow-y: auto; font-family: 'Consolas', 'Monaco', monospace;">
            {{ selectedTemplate.template }}
          </div>

          <div v-if="selectedTemplate.variables && selectedTemplate.variables.length" style="margin-top: 16px;">
            <div class="card-title">变量填充预览</div>
            <div v-for="v in selectedTemplate.variables" :key="v" style="margin-bottom: 8px;">
              <el-input :placeholder="`输入 {{${v}}} 的值`" v-model="previewVars[v]" size="small" />
            </div>
            <el-button size="small" @click="previewTemplate" type="primary" style="margin-top: 8px;">预览渲染结果</el-button>
          </div>

          <div v-if="previewResult" style="margin-top: 16px;">
            <div class="card-title">渲染结果</div>
            <div class="result-box" style="max-height: 200px; font-size: 13px;">{{ previewResult }}</div>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
    templates.value = res.data
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
