<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><Connection /></el-icon>多语言翻译</h2>
      <p>展示架行业专业翻译 · 多语言互译 · 跨境电商本地化增强</p>
    </div>
    <div class="page-body">
      <div class="page-split">
        <div class="split-main">
          <div class="page-scroll">
            <div class="card fade-in">
              <div class="card-head">翻译设置</div>
              <div class="form-row" style="margin-bottom:12px;">
                <el-form-item label="源语言" style="flex:1;">
                  <el-select v-model="form.sourceLanguage" size="large">
                    <el-option label="中文" value="中文"/>
                    <el-option label="English" value="英文"/>
                    <el-option label="日本語" value="日文"/>
                    <el-option label="한국어" value="韩文"/>
                    <el-option label="Deutsch" value="德文"/>
                    <el-option label="Français" value="法文"/>
                    <el-option label="Español" value="西班牙文"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="目标语言" style="flex:1;">
                  <el-select v-model="form.targetLanguage" size="large">
                    <el-option label="English" value="英文"/>
                    <el-option label="日本語" value="日文"/>
                    <el-option label="한국어" value="韩文"/>
                    <el-option label="Deutsch" value="德文"/>
                    <el-option label="Français" value="法文"/>
                    <el-option label="Español" value="西班牙文"/>
                    <el-option label="中文" value="中文"/>
                  </el-select>
                </el-form-item>
              </div>
              <el-form-item label="待翻译文本" style="margin-bottom:14px;">
                <el-input v-model="form.text" type="textarea" :rows="5" placeholder="输入要翻译的文本，如产品描述、报价单、邮件等..." size="large"/>
              </el-form-item>
              <div style="display:flex;align-items:center;gap:12px;">
                <el-button type="primary" @click="doTranslate" :loading="translating" :icon="Reading" size="large">
                  {{ translating ? '翻译中...' : '智能翻译' }}
                </el-button>
                <el-checkbox v-model="form.ecommerceLocalization" label="电商本地化增强" size="small" border/>
                <el-tag v-if="translating" type="warning" size="large">AI 翻译中...</el-tag>
                <el-tag v-if="currentSessionId" type="success" size="small" style="margin-left:auto;">
                  <el-icon :size="12"><Check /></el-icon>已保存
                </el-tag>
              </div>
            </div>

            <div v-if="result" class="slide-up">
              <div class="card">
                <div class="card-head">翻译结果</div>
                <div class="result-box" v-html="rendered"></div>
                <div style="margin-top:14px;display:flex;gap:10px;align-items:center;">
                  <el-button @click="copyResult" :icon="CopyDocument">复制</el-button>
                  <span v-if="resultInfo.processingTimeMs" style="margin-left:auto;font-size:12px;color:var(--text-muted);">
                    {{ resultInfo.processingTimeMs }}ms
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- History -->
        <div class="split-side">
          <div class="card" style="padding:10px;flex-shrink:0;">
            <el-button size="small" @click="loadHistory" :icon="Refresh" style="width:100%;">刷新历史</el-button>
          </div>
          <div class="card card-fill" style="padding:6px;">
            <div class="sess-list">
              <div v-if="historyList.length===0" class="empty-state" style="padding:36px 16px;">
                <el-icon :size="26" style="margin-bottom:8px;opacity:.3;"><Connection /></el-icon>
                <div style="font-size:13px;">暂无历史</div>
              </div>
              <div v-for="h in historyList" :key="h.sessionId"
                   :class="['sess-item', { active: h.sessionId === currentSessionId }]"
                   @click="loadHistoryItem(h)">
                <div class="sess-top">
                  <div class="sess-title" @dblclick.stop="startRename(h)">{{ h.title }}</div>
                  <div class="sess-actions">
                    <el-button size="small" text @click.stop="startRename(h)"><el-icon><Edit /></el-icon></el-button>
                    <el-button size="small" text @click.stop="confirmDelete(h)"><el-icon><Delete /></el-icon></el-button>
                  </div>
                </div>
                <div style="font-size:10.5px;color:var(--text-muted);margin-top:2px;">
                  {{ h.messageCount }} 条 · {{ h.updatedAt?.substring(0, 16) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="renameVisible" title="重命名" width="400px" :close-on-click-modal="false">
      <el-input v-model="renameValue" placeholder="输入新名称" @keyup.enter="doRename" maxlength="60" show-word-limit/>
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" @click="doRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading, Edit, Delete, Connection, Refresh, Check, CopyDocument } from '@element-plus/icons-vue'
import { translateApi, agentApi } from '../api/index.js'

const form = ref({
  sourceLanguage: '中文', targetLanguage: '英文', text: '',
  context: '跨境电商商品信息', ecommerceLocalization: true
})
const result = ref('')
const translating = ref(false)
const currentSessionId = ref(null)
const historyList = ref([])
const resultInfo = ref({})
const renameVisible = ref(false)
const renameValue = ref('')
const renameTarget = ref(null)

const rendered = computed(() => {
  if (!result.value) return ''
  return result.value
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
})

onMounted(() => loadHistory())

async function loadHistory() {
  try {
    const r = await agentApi.getSessions('translate')
    historyList.value = r.data.sessions || []
  } catch (e) { /* ignore */ }
}

async function loadHistoryItem(h) {
  try {
    const r = await agentApi.getDBHistory(h.sessionId)
    result.value = (r.data.records || [])
      .filter(r => r.role === 'assistant')
      .map(r => r.content)
      .join('\n\n') || ''
    currentSessionId.value = h.sessionId
  } catch (e) { ElMessage.error('加载失败') }
}

function startRename(s) {
  renameTarget.value = s
  renameValue.value = s.title || ''
  renameVisible.value = true
}

async function doRename() {
  const v = renameValue.value.trim()
  if (!v) { ElMessage.warning('名称不能为空'); return }
  try {
    await agentApi.updateTitle(renameTarget.value.sessionId, v)
    renameTarget.value.title = v
    renameVisible.value = false
    ElMessage.success('已重命名')
    loadHistory()
  } catch (e) { ElMessage.error('重命名失败') }
}

async function confirmDelete(s) {
  try {
    await ElMessageBox.confirm('确定删除"' + s.title + '"吗？', '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await agentApi.clearSession(s.sessionId)
    if (currentSessionId.value === s.sessionId) { result.value = ''; currentSessionId.value = null }
    ElMessage.success('已删除')
    loadHistory()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

async function doTranslate() {
  if (!form.value.text) { ElMessage.warning('请输入文本'); return }
  translating.value = true
  result.value = ''
  try {
    const r = await translateApi.translate({
      text: form.value.text, sourceLanguage: form.value.sourceLanguage,
      targetLanguage: form.value.targetLanguage, context: form.value.context,
      ecommerceLocalization: form.value.ecommerceLocalization
    })
    result.value = r.data.result
    resultInfo.value = { processingTimeMs: r.data.processingTimeMs }
    currentSessionId.value = r.data.sessionId
    ElMessage.success('翻译完成')
    loadHistory()
    setTimeout(() => loadHistory(), 2500)
  } catch (e) {
    ElMessage.error('翻译失败: ' + (e.response?.data?.message || e.message))
  } finally {
    translating.value = false
  }
}

function copyResult() {
  if (!result.value) return
  navigator.clipboard.writeText(result.value).then(() => ElMessage.success('已复制'))
}
</script>
