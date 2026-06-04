<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;height:100%;">
    <div class="page-header">
      <h2><el-icon :size="20"><Document /></el-icon>Prompt 模板管理</h2>
      <p>可视化管理系统内置 Prompt 模板 · 版本对比 · 变量预览 · 分类查看</p>
    </div>
    <div class="page-body">
      <div class="page-scroll">
        <!-- Stats -->
        <div class="stats-row stagger-in">
          <div class="stat-card">
            <div class="s-icon s-violet"><el-icon :size="20"><Document /></el-icon></div>
            <div class="s-num">{{ templates.length }}</div>
            <div class="s-label">模板总数</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-blue"><el-icon :size="20"><Folder /></el-icon></div>
            <div class="s-num">{{ catsCount }}</div>
            <div class="s-label">分类数量</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-green"><el-icon :size="20"><Connection /></el-icon></div>
            <div class="s-num">2</div>
            <div class="s-label">LLM 提供商</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-amber"><el-icon :size="20"><Timer /></el-icon></div>
            <div class="s-num">{{ templates.length }}</div>
            <div class="s-label">活跃模板</div>
          </div>
        </div>

        <!-- Category filter + search + create -->
        <div class="card" style="padding:12px 16px;">
          <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;">
            <span style="font-size:12px;font-weight:600;color:var(--text-muted);">分类:</span>
            <el-radio-group v-model="selectedCategory" size="small">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="copywriting">文案</el-radio-button>
              <el-radio-button value="translation">翻译</el-radio-button>
              <el-radio-button value="analysis">分析</el-radio-button>
              <el-radio-button value="agent">Agent</el-radio-button>
            </el-radio-group>
            <el-input v-model="searchQuery" placeholder="搜索模板..." size="small" clearable style="width:200px;margin-left:auto;" :prefix-icon="Search"/>
            <el-button type="primary" size="small" @click="openCreateDialog" :icon="Plus">新建模板</el-button>
            <span style="font-size:12px;color:var(--text-muted);">
              共 {{ filtered.length }} 个模板
            </span>
          </div>
        </div>

        <!-- Template cards -->
        <div class="stagger-in" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(340px,1fr));gap:14px;">
          <div v-for="t in filtered" :key="t.id" class="tpl-card" @click="showDetail(t)">
            <div class="tpl-name">{{ t.name }}</div>
            <div class="tpl-desc">{{ t.description }}</div>
            <div class="tpl-meta">
              <span class="badge badge-primary">{{ t.category }}</span>
              <span v-if="t.targetPlatform" class="badge badge-success">{{ t.targetPlatform }}</span>
              <span v-if="t.variables && t.variables.length" class="badge badge-warning">{{ t.variables.length }}变量</span>
              <span :class="['badge', t.active ? 'badge-success' : 'badge-danger']">{{ t.active ? '✓ 启用' : '✗ 禁用' }}</span>
            </div>
            <!-- Variable chips -->
            <div style="margin-top:12px;">
              <span v-for="v in t.variables" :key="v"
                    style="display:inline-block;padding:3px 10px;background:var(--brand-50);border-radius:5px;font-size:10.5px;margin-right:5px;margin-bottom:3px;color:var(--brand);font-weight:500;">
                &#123;&#123;{{ v }}&#125;&#125;
              </span>
            </div>
            <div v-if="t.version" style="margin-top:10px;display:flex;align-items:center;gap:6px;font-size:10.5px;color:var(--text-muted);">
              <el-icon :size="11"><Timer /></el-icon> v{{ t.version }}
              <span style="margin-left:auto;">命中 {{ Math.round((t.hitRate || 0.85) * 100) }}%</span>
            </div>
          </div>
        </div>

        <!-- Detail dialog -->
        <el-dialog v-model="detailVisible" :title="'模板: ' + (sel?.name || '')" width="860px" top="3vh">
          <template v-if="sel">
            <el-descriptions :column="3" border size="small" style="margin-bottom:18px;">
              <el-descriptions-item label="ID">{{ sel.id }}</el-descriptions-item>
              <el-descriptions-item label="分类">{{ sel.category }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="sel.active ? 'success' : 'danger'" size="small">{{ sel.active ? '启用' : '禁用' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="平台">{{ sel.targetPlatform || '通用' }}</el-descriptions-item>
              <el-descriptions-item label="版本">v{{ sel.version || '1.0' }}</el-descriptions-item>
              <el-descriptions-item label="命中率">{{ Math.round((sel.hitRate || 0.85) * 100) }}%</el-descriptions-item>
            </el-descriptions>

            <div class="card-head" style="margin-bottom:12px;">模板内容</div>
            <div style="background:#1e293b;color:#e2e8f0;padding:16px;border-radius:8px;font-size:12.5px;white-space:pre-wrap;max-height:240px;overflow-y:auto;font-family:'Fira Code','Consolas',monospace;line-height:1.65;">
              {{ sel.template }}
            </div>

            <div v-if="sel.variables && sel.variables.length" style="margin-top:18px;">
              <div class="card-head" style="margin-bottom:12px;">变量预览</div>
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:12px;">
                <div v-for="v in sel.variables" :key="v">
                  <el-input :placeholder="'输入 ' + v + ' 的值'" v-model="previewVars[v]" size="small">
                    <template #prepend>{{ v }}</template>
                  </el-input>
                </div>
              </div>
              <el-button size="small" @click="previewTemplate" type="primary" :icon="View">预览渲染结果</el-button>
              <div v-if="previewResult" style="margin-top:14px;">
                <div class="card-head" style="margin-bottom:8px;">渲染结果</div>
                <div class="result-box" style="max-height:180px;font-size:13px;background:var(--bg-elevated);">{{ previewResult }}</div>
              </div>
            </div>
          </template>
          <template #footer>
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button type="primary" @click="openEditDialog(sel)">编辑</el-button>
          </template>
        </el-dialog>

        <!-- Create/Edit dialog -->
        <el-dialog v-model="editVisible" :title="editingId ? '编辑模板' : '新建模板'" width="760px" top="3vh" :close-on-click-modal="false">
          <el-form :model="editForm" label-width="90px" size="small">
            <el-form-item label="模板ID" required>
              <el-input v-model="editForm.id" placeholder="唯一标识, 如: copywriting-email" :disabled="!!editingId"/>
            </el-form-item>
            <el-form-item label="名称" required>
              <el-input v-model="editForm.name" placeholder="模板显示名称"/>
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="editForm.description" placeholder="简要描述模板用途"/>
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="editForm.category" style="width:100%;">
                <el-option label="文案" value="copywriting"/>
                <el-option label="翻译" value="translation"/>
                <el-option label="分析" value="analysis"/>
                <el-option label="Agent" value="agent"/>
              </el-select>
            </el-form-item>
            <el-form-item label="目标平台">
              <el-input v-model="editForm.targetPlatform" placeholder="如: Alibaba, Email"/>
            </el-form-item>
            <el-form-item label="模板内容" required>
              <el-input v-model="editForm.template" type="textarea" :rows="10" placeholder="使用 {{变量名}} 标记变量"/>
            </el-form-item>
            <el-form-item label="变量列表">
              <el-input v-model="editForm.variablesStr" placeholder="逗号分隔, 如: productName,sellingPoints"/>
              <div v-if="parsedVariables.length" style="margin-top:6px;display:flex;gap:6px;flex-wrap:wrap;">
                <el-tag v-for="v in parsedVariables" :key="v" size="small" type="info">&#123;&#123;{{ v }}&#125;&#125;</el-tag>
              </div>
            </el-form-item>
            <el-form-item label="启用">
              <el-switch v-model="editForm.active"/>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="editVisible = false">取消</el-button>
            <el-button type="danger" v-if="editingId" @click="doDelete">删除</el-button>
            <el-button type="primary" @click="doSave" :loading="saving">{{ editingId ? '更新' : '创建' }}</el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Folder, Connection, Timer, View, Search, Plus, Edit } from '@element-plus/icons-vue'
import { copywritingApi } from '../api/index.js'

const templates = ref([])
const selectedCategory = ref('')
const searchQuery = ref('')
const sel = ref(null)
const detailVisible = ref(false)
const previewVars = ref({})
const previewResult = ref('')

// Edit / Create
const editVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const editForm = reactive({
  id: '', name: '', description: '', category: 'copywriting',
  template: '', targetPlatform: '', variablesStr: '', active: true
})

const catsCount = computed(() => {
  const cats = new Set(templates.value.map(t => t.category))
  return cats.size
})

const filtered = computed(() => {
  let list = selectedCategory.value
    ? templates.value.filter(t => t.category === selectedCategory.value)
    : templates.value
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    list = list.filter(t =>
      t.name.toLowerCase().includes(q) ||
      t.description.toLowerCase().includes(q) ||
      t.category.toLowerCase().includes(q)
    )
  }
  return list
})

const parsedVariables = computed(() => {
  return editForm.variablesStr
    ? editForm.variablesStr.split(',').map(s => s.trim()).filter(Boolean)
    : []
})

onMounted(() => loadTemplates())

async function loadTemplates() {
  try {
    const r = await copywritingApi.getTemplates()
    const data = Array.isArray(r.data) ? r.data : (r.data.templates || r.data.data || [])
    templates.value = data.map(t => ({
      ...t,
      version: t.version || '1.0',
      hitRate: t.hitRate || 0.85,
      updatedAt: t.updatedAt || '2026-05-28'
    }))
  } catch (e) {
    ElMessage.error('加载模板失败')
  }
}

function showDetail(t) {
  sel.value = t
  previewVars.value = {}
  previewResult.value = ''
  detailVisible.value = true
}

async function previewTemplate() {
  try {
    const r = await copywritingApi.previewTemplate(sel.value.id, previewVars.value)
    previewResult.value = r.data.rendered
  } catch (e) {
    ElMessage.error('预览失败')
  }
}

function openCreateDialog() {
  editingId.value = null
  Object.assign(editForm, {
    id: '', name: '', description: '', category: 'copywriting',
    template: '', targetPlatform: '', variablesStr: '', active: true
  })
  editVisible.value = true
}

function openEditDialog(t) {
  editingId.value = t.id
  Object.assign(editForm, {
    id: t.id,
    name: t.name || '',
    description: t.description || '',
    category: t.category || 'copywriting',
    template: t.template || '',
    targetPlatform: t.targetPlatform || '',
    variablesStr: (t.variables || []).join(','),
    active: t.active !== false
  })
  detailVisible.value = false
  editVisible.value = true
}

async function doSave() {
  if (!editForm.id || !editForm.name || !editForm.template) {
    ElMessage.warning('请填写模板ID、名称和内容')
    return
  }
  saving.value = true
  try {
    const payload = {
      id: editForm.id,
      name: editForm.name,
      description: editForm.description,
      category: editForm.category,
      template: editForm.template,
      targetPlatform: editForm.targetPlatform,
      variables: parsedVariables.value,
      active: editForm.active
    }
    if (editingId.value) {
      await copywritingApi.updateTemplate(editingId.value, payload)
      ElMessage.success('模板已更新')
    } else {
      await copywritingApi.createTemplate(payload)
      ElMessage.success('模板已创建')
    }
    editVisible.value = false
    loadTemplates()
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.response?.data?.message || e.message))
  } finally {
    saving.value = false
  }
}

async function doDelete() {
  try {
    await ElMessageBox.confirm(`确定删除 "${editForm.name}" 吗？`, '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await copywritingApi.deleteTemplate(editingId.value)
    ElMessage.success('已删除')
    editVisible.value = false
    loadTemplates()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>
