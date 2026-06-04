<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;height:100%;">
    <div class="page-header">
      <h2><el-icon :size="20"><EditPen /></el-icon>文案 & 询盘回复</h2>
      <p>Prompt Engineering · 展示架 B2B 出口 · 产品详情 & 询盘邮件</p>
    </div>
    <div class="page-body" style="padding:0;">
      <div class="page-split">
        <div class="split-main">
          <div class="page-scroll">
            <!-- Form card -->
            <div class="card fade-in">
              <div class="card-head">产品信息</div>
              <div class="form-row" style="margin-bottom:12px;">
                <el-form-item label="产品名称" style="flex:2;">
                  <el-input v-model="form.productName" placeholder="Cardboard Floor Display Stand" size="large"/>
                </el-form-item>
                <el-form-item label="文案类型" style="flex:1;">
                  <el-select v-model="form.platform" size="large">
                    <el-option label="B2B 产品详情" value="alibaba"/>
                    <el-option label="询盘回复邮件" value="email"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="目标市场" style="flex:1;">
                  <el-select v-model="form.targetCountry" size="large">
                    <el-option label="美国" value="US"/>
                    <el-option label="日本" value="JP"/>
                    <el-option label="英国" value="UK"/>
                    <el-option label="德国" value="DE"/>
                    <el-option label="澳大利亚" value="AU"/>
                    <el-option label="法国" value="FR"/>
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-row" style="margin-bottom:12px;">
                <el-form-item label="产品特点" style="flex:3;">
                  <el-input v-model="form.sellingPoints" type="textarea" :rows="3" placeholder="3层瓦楞纸陈列架、4C印刷、可定制尺寸..." size="large"/>
                </el-form-item>
              </div>
              <div class="form-row" style="margin-bottom:14px;">
                <el-form-item label="风格" style="flex:1;">
                  <el-select v-model="form.style" size="large">
                    <el-option label="专业工厂" value="专业且有吸引力"/>
                    <el-option label="热情友好" value="亲切但不过度热情"/>
                    <el-option label="简洁高效" value="简洁明了，突出卖点"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="输出语言" style="flex:1;">
                  <el-select v-model="form.language" size="large">
                    <el-option label="English" value="English"/>
                    <el-option label="日本語" value="Japanese"/>
                    <el-option label="Deutsch" value="German"/>
                  </el-select>
                </el-form-item>
              </div>
              <div style="display:flex;gap:10px;align-items:center;">
                <el-button type="primary" @click="generate" :loading="generating" :icon="EditPen" size="large">
                  {{ generating ? 'AI 生成中...' : '生成文案' }}
                </el-button>
                <el-tag v-if="generating" type="warning" size="large">AI 生成中...</el-tag>
                <el-tag v-if="currentSessionId" type="success" size="small" style="margin-left:auto;">
                  <el-icon :size="12"><Check /></el-icon>已保存
                </el-tag>
              </div>
            </div>

            <!-- Result -->
            <div v-if="result" class="slide-up">
              <div class="card">
                <div class="card-head">生成结果</div>
                <div class="result-box" v-html="rendered"></div>
                <div style="margin-top:14px;display:flex;gap:10px;align-items:center;">
                  <el-button @click="copyResult" :icon="CopyDocument">复制</el-button>
                  <el-button @click="generate" :loading="generating">重新生成</el-button>
                  <span v-if="resultInfo.model" style="margin-left:auto;font-size:12px;color:var(--text-muted);">
                    模型: {{ resultInfo.model }} · {{ resultInfo.processingTimeMs }}ms
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- History sidebar -->
        <div class="split-side">
          <div class="card" style="padding:10px;flex-shrink:0;">
            <el-button size="small" @click="loadHistory" :icon="Refresh" style="width:100%;">刷新历史</el-button>
          </div>
          <div class="card card-fill" style="padding:6px;">
            <div class="sess-list">
              <div v-if="historyList.length===0" class="empty-state" style="padding:36px 16px;">
                <el-icon :size="26" style="margin-bottom:8px;opacity:.3;"><Document /></el-icon>
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

    <!-- Rename dialog -->
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, CopyDocument, Check, Refresh, Edit, Delete, Document } from '@element-plus/icons-vue'
import { copywritingApi, agentApi } from '../api/index.js'

const form = reactive({
  productName: '', platform: 'alibaba', targetCountry: 'US',
  sellingPoints: '', style: '专业且有吸引力', language: 'English'
})
const result = ref('')
const generating = ref(false)
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
    .replace(/###\s?(.*)/g, '<h3>$1</h3>')
})

onMounted(() => loadHistory())

async function loadHistory() {
  try {
    const r = await agentApi.getSessions('copywriting')
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

async function generate() {
  if (!form.productName) { ElMessage.warning('请输入产品名称'); return }
  generating.value = true
  result.value = ''
  try {
    const r = await copywritingApi.generate({
      productName: form.productName, platform: form.platform,
      targetCountry: form.targetCountry, sellingPoints: form.sellingPoints,
      style: form.style, language: form.language
    })
    result.value = r.data.result
    resultInfo.value = {
      model: r.data.model || r.data.modelUsed,
      processingTimeMs: r.data.processingTimeMs
    }
    currentSessionId.value = r.data.sessionId
    ElMessage.success('生成完成')
    loadHistory()
    setTimeout(() => loadHistory(), 2500)
  } catch (e) {
    ElMessage.error('生成失败: ' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
  }
}

function copyResult() {
  if (!result.value) return
  navigator.clipboard.writeText(result.value).then(() => ElMessage.success('已复制'))
}
</script>
