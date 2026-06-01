<template>
  <div>
    <div class="page-header">
      <h2>📊 展示架市场分析</h2>
      <p>使用 DeepSeek 深度推理，分析展示架/POP产品在不同市场的出口机会</p>
    </div>
    <div class="page-content" style="display: flex; gap: 16px; padding: 16px;">
      <div style="width: 260px; flex-shrink: 0; display: flex; flex-direction: column; gap: 12px;">
        <div class="card" style="padding: 12px;">
          <el-button size="small" @click="loadHistory" style="width: 100%;">🔄 刷新历史</el-button>
        </div>
        <div class="card" style="padding: 8px; flex: 1; overflow-y: auto; max-height: calc(100vh - 320px);">
          <div v-if="historyList.length === 0" style="text-align: center; padding: 20px; color: var(--text-secondary); font-size: 13px;">
            暂无市场分析历史
          </div>
          <div v-for="h in historyList" :key="h.sessionId"
            @click="loadHistoryItem(h)"
            :class="['session-item', { active: h.sessionId === currentSessionId }]">
            <div class="session-top">
              <div class="session-title" @dblclick.stop="startRename(h)">{{ h.title }}</div>
              <div class="session-actions">
                <el-button size="small" text @click.stop="startRename(h)" title="重命名"><el-icon><Edit /></el-icon></el-button>
                <el-button size="small" text @click.stop="confirmDelete(h)" title="删除"><el-icon><Delete /></el-icon></el-button>
              </div>
            </div>
            <div style="font-size: 11px; opacity: 0.5; margin-top: 4px;">
              {{ h.messageCount }} 条 · {{ h.updatedAt?.substring(0, 16) }}
            </div>
          </div>
        </div>
      </div>

      <div style="flex: 1;">
        <div class="card">
          <div class="card-title">🔍 分析参数</div>
          <div class="form-row">
            <el-form-item label="商品名称" style="flex: 1;">
              <el-input v-model="form.productName" placeholder="例如：Cardboard Floor Display Stand" />
            </el-form-item>
            <el-form-item label="目标市场" style="flex: 1;">
              <el-select v-model="form.targetCountry">
                <el-option label="美国" value="US" />
                <el-option label="日本" value="JP" />
                <el-option label="英国" value="UK" />
                <el-option label="德国" value="DE" />
                <el-option label="韩国" value="KR" />
              </el-select>
            </el-form-item>
          </div>
          <div style="display: flex; gap: 12px; align-items: center;">
            <el-button type="primary" @click="doAnalysis" :loading="analyzing" :icon="Opportunity">
              DeepSeek 深度分析
            </el-button>
            <el-tag v-if="analyzing" type="warning">⏳ 分析中...</el-tag>
            <el-tag v-if="currentSessionId" type="success" size="small" style="margin-left: auto;">💾 已保存</el-tag>
          </div>
        </div>
        <div v-if="result" class="card" style="margin-top: 16px;">
          <div class="card-title">分析报告</div>
          <div class="result-box" v-html="renderedResult"></div>
        </div>
      </div>
    </div>

    <!-- Rename Dialog -->
    <el-dialog v-model="renameVisible" title="重命名" width="400px" :close-on-click-modal="false">
      <el-input v-model="renameValue" placeholder="输入新名称" @keyup.enter="doRename" maxlength="60" show-word-limit />
      <template #footer><el-button @click="renameVisible = false">取消</el-button><el-button type="primary" @click="doRename">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Opportunity, Edit, Delete } from '@element-plus/icons-vue'
import { analysisApi, agentApi } from '../api/index.js'

const form = ref({ productName: '', targetCountry: 'US' })
const result = ref('')
const analyzing = ref(false)
const currentSessionId = ref(null)
const historyList = ref([])

const renameVisible = ref(false)
const renameValue = ref('')
const renameTarget = ref(null)

const renderedResult = computed(() => {
  if (!result.value) return ''
  return result.value.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br/>')
})

onMounted(() => loadHistory())

async function loadHistory() {
  try { const res = await agentApi.getSessions('analysis'); historyList.value = res.data.sessions || [] } catch (e) {}
}

async function loadHistoryItem(item) {
  try {
    const res = await agentApi.getDBHistory(item.sessionId)
    const records = res.data.records || []
    result.value = records.filter(r => r.role === 'assistant').map(r => r.content).join('\n\n') || 'No result'
    currentSessionId.value = item.sessionId
  } catch (e) { ElMessage.error('加载失败') }
}

async function doAnalysis() {
  if (!form.value.productName) { ElMessage.warning('请输入商品名称'); return }
  analyzing.value = true; result.value = ''
  try {
    const res = await analysisApi.analyzeMarket({
      productName: form.value.productName, targetCountry: form.value.targetCountry
    })
    result.value = res.data.result
    currentSessionId.value = res.data.sessionId
    ElMessage.success('分析完成')
    loadHistory()
    setTimeout(() => loadHistory(), 2500)
  } catch (e) { ElMessage.error('分析失败') }
  finally { analyzing.value = false }
}

function startRename(s) { renameTarget.value = s; renameValue.value = s.title || ''; renameVisible.value = true }
async function doRename() {
  const val = renameValue.value.trim()
  if (!val) { ElMessage.warning('名称不能为空'); return }
  try { await agentApi.updateTitle(renameTarget.value.sessionId, val); renameTarget.value.title = val; renameVisible.value = false; ElMessage.success('已重命名'); loadHistory() } catch (e) { ElMessage.error('重命名失败') }
}
async function confirmDelete(s) {
  try {
    await ElMessageBox.confirm(`确定删除「${s.title}」吗？`, '确认删除', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    await agentApi.clearSession(s.sessionId)
    if (currentSessionId.value === s.sessionId) { result.value = ''; currentSessionId.value = null }
    ElMessage.success('已删除'); loadHistory()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}
</script>

<style scoped>
.session-item { padding: 10px 12px; border-radius: 6px; cursor: pointer; margin-bottom: 4px; transition: all 0.2s; }
.session-item:hover { background: #f0f5ff; }
.session-item.active { background: #e6f7ff; border-left: 3px solid var(--primary); }
.session-top { display: flex; align-items: center; justify-content: space-between; }
.session-title { font-size: 13px; font-weight: 500; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-actions { display: none; gap: 2px; }
.session-item:hover .session-actions { display: flex; }
</style>
