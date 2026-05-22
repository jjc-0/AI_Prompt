<template>
  <div>
    <div class="page-header">
      <h2>✍️ 产品文案 & 询盘回复</h2>
      <p>基于Prompt Engineering，为展示架B2B出口生成专业产品详情和询盘回复邮件</p>
    </div>
    <div class="page-content" style="display: flex; gap: 16px; padding: 16px;">
      <div style="width: 260px; flex-shrink: 0; display: flex; flex-direction: column; gap: 12px;">
        <div class="card" style="padding: 12px;">
          <el-button size="small" @click="loadHistory" style="width: 100%;">🔄 刷新历史</el-button>
        </div>
        <div class="card" style="padding: 8px; flex: 1; overflow-y: auto; max-height: calc(100vh - 320px);">
          <div v-if="historyList.length === 0" style="text-align: center; padding: 20px; color: var(--text-secondary); font-size: 13px;">
            暂无文案生成历史
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
          <div class="card-title">📝 产品信息</div>
          <div class="form-row">
            <el-form-item label="产品名称" style="flex: 2;">
              <el-input v-model="form.productName" placeholder="例如：Cardboard Floor Display Stand" />
            </el-form-item>
            <el-form-item label="文案类型" style="flex: 1;">
              <el-select v-model="form.platform" placeholder="选择类型">
                <el-option label="B2B产品详情" value="alibaba" />
                <el-option label="询盘回复邮件" value="email" />
              </el-select>
            </el-form-item>
            <el-form-item label="目标市场" style="flex: 1;">
              <el-select v-model="form.targetCountry" placeholder="选择国家">
                <el-option label="美国 (US)" value="US" />
                <el-option label="日本 (JP)" value="JP" />
                <el-option label="英国 (UK)" value="UK" />
                <el-option label="德国 (DE)" value="DE" />
                <el-option label="澳大利亚 (AU)" value="AU" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="产品特点 / 客户需求" style="flex: 3;">
              <el-input v-model="form.sellingPoints" type="textarea" :rows="3"
                placeholder="例如：3层瓦楞纸陈列架、4C印刷、可定制尺寸、适合超市饮料区、客户需求300pcs" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="文案风格" style="flex: 1;">
              <el-select v-model="form.style" placeholder="选择风格">
                <el-option label="专业工厂" value="专业且有吸引力" />
                <el-option label="热情友好" value="亲切但不过度热情" />
                <el-option label="简洁高效" value="简洁明了，突出卖点" />
              </el-select>
            </el-form-item>
            <el-form-item label="输出语言" style="flex: 1;">
              <el-select v-model="form.language" placeholder="English">
                <el-option label="English" value="English" />
                <el-option label="Japanese" value="Japanese" />
                <el-option label="German" value="German" />
              </el-select>
            </el-form-item>
          </div>
          <div style="display: flex; gap: 12px; align-items: center;">
            <el-button type="primary" @click="generateCopy" :loading="generating" :icon="Edit">
              DeepSeek AI 生成
            </el-button>
            <el-tag v-if="generating" type="warning">⏳ 生成中...</el-tag>
            <el-tag v-if="currentSessionId" type="success" size="small" style="margin-left: auto;">💾 已保存</el-tag>
          </div>
        </div>
        <div v-if="result" class="card" style="margin-top: 16px;">
          <div class="card-title">生成结果</div>
          <div class="result-box" v-html="renderedResult"></div>
          <div style="margin-top: 16px; display: flex; gap: 8px;">
            <el-button size="small" @click="copyResult">复制结果</el-button>
            <el-button size="small" @click="generateCopy" :loading="generating">重新生成</el-button>
          </div>
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
import { Edit, Delete } from '@element-plus/icons-vue'
import { copywritingApi, agentApi } from '../api/index.js'

const form = ref({
  productName: '', platform: 'alibaba', targetCountry: 'US',
  sellingPoints: '', style: '专业且有吸引力', language: 'English'
})
const result = ref('')
const resultInfo = ref({})
const generating = ref(false)
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
  try {
    const res = await agentApi.getSessions('copywriting')
    historyList.value = res.data.sessions || []
  } catch (e) {}
}

async function loadHistoryItem(item) {
  try {
    const res = await agentApi.getDBHistory(item.sessionId)
    const records = res.data.records || []
    result.value = records.filter(r => r.role === 'assistant').map(r => r.content).join('\n\n') || 'No result found'
    currentSessionId.value = item.sessionId
  } catch (e) { ElMessage.error('加载失败') }
}

async function generateCopy() {
  if (!form.value.productName) { ElMessage.warning('请输入产品名称'); return }
  generating.value = true; result.value = ''
  try {
    const res = await copywritingApi.generate({
      productName: form.value.productName, platform: form.value.platform,
      targetCountry: form.value.targetCountry, sellingPoints: form.value.sellingPoints,
      style: form.value.style, language: form.value.language
    })
    result.value = res.data.result
    resultInfo.value = { model: res.data.model, processingTimeMs: res.data.processingTimeMs }
    currentSessionId.value = res.data.sessionId
    ElMessage.success(`生成完成 (${res.data.processingTimeMs}ms)`)
    loadHistory()
  } catch (e) { ElMessage.error('生成失败') }
  finally { generating.value = false }
}

function copyResult() { navigator.clipboard.writeText(result.value).then(() => ElMessage.success('已复制')) }

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
