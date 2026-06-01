<template>
  <div>
    <div class="page-header">
      <h2>🤖 AI Agent 对话</h2>
      <p>具备工具调用能力的智能Agent</p>
    </div>
    <div class="page-content" style="display: flex; gap: 16px; padding: 16px;">
      <div style="width: 260px; flex-shrink: 0; display: flex; flex-direction: column; gap: 12px;">
        <div class="card" style="padding: 12px;">
          <el-button size="small" @click="loadSessionList" :icon="Refresh" style="width: 100%;">刷新会话列表</el-button>
          <el-button size="small" @click="newSession" type="success" style="width: 100%; margin-top: 8px;">➕ 新建会话</el-button>
        </div>
        <div class="card" style="padding: 8px; flex: 1; overflow-y: auto; max-height: calc(100vh - 320px);">
          <div v-if="sessions.length === 0" style="text-align: center; padding: 20px; color: var(--text-secondary); font-size: 13px;">
            暂无历史会话
          </div>
          <div v-for="s in sessions" :key="s.sessionId"
            :class="['session-item', { active: s.sessionId === sessionId }]"
            @click="loadSession(s.sessionId)"
          >
            <div class="session-top">
              <div class="session-title" @dblclick.stop="startRename(s)">{{ s.title }}</div>
              <div class="session-actions">
                <el-button :icon="Edit" size="small" text @click.stop="startRename(s)" title="重命名" />
                <el-button :icon="Delete" size="small" text @click.stop="confirmDelete(s)" title="删除" />
              </div>
            </div>
            <div style="font-size: 11px; opacity: 0.5; margin-top: 4px;">
              {{ s.messageCount }} 条消息 · {{ s.updatedAt?.substring(0, 16) }}
            </div>
          </div>
        </div>
      </div>

      <div class="card" style="flex: 1; padding: 0;">
        <div class="chat-container" style="height: calc(100vh - 220px);">
          <div class="chat-messages" style="padding: 20px;">
            <div v-if="messages.length === 0" class="empty-state">
              <div class="empty-icon">
                <div class="empty-avatar">
                  <el-icon :size="40"><Cpu /></el-icon>
                </div>
              </div>
              <h3>杰创展示 AI 助手</h3>
              <p>我是JC Display的B2B出口AI助手，很高兴为您服务</p>
              <div class="empty-hints">
                <span class="hint-chip">"帮我生成一个瓦楞纸展示架的产品详情"</span>
                <span class="hint-chip">"分析美国市场展示架的出口机会"</span>
                <span class="hint-chip">"将产品描述翻译成日语"</span>
              </div>
            </div>

            <div v-for="(msg, index) in messages" :key="index"
              :class="['chat-message', msg.role, 'animate__animated', 'animate__fadeInUp']">
              <div :class="['chat-avatar', msg.role]">
                <el-icon :size="18" v-if="msg.role === 'user'"><UserFilled /></el-icon>
                <el-icon :size="18" v-else><Cpu /></el-icon>
              </div>
              <div>
                <div :class="['chat-bubble', msg.role]" v-html="renderMarkdown(msg.content)"></div>
                <div v-if="msg.toolCalls && msg.toolCalls.length > 0">
                  <div v-for="(tc, tIdx) in msg.toolCalls" :key="tIdx" class="tool-call-log animate__animated animate__fadeInUp">
                    <span class="tool-name"><el-icon :size="14"><Tools /></el-icon> {{ tc.toolName }}</span>
                    <span class="tool-time">{{ tc.durationMs }}ms</span>
                    <div class="tool-output">{{ tc.output }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="loading" class="chat-message assistant animate__animated animate__fadeInUp">
              <div class="chat-avatar assistant">
                <el-icon :size="18"><Cpu /></el-icon>
              </div>
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>

          <div style="padding: 0 20px;">
            <div style="display: flex; gap: 8px; margin-bottom: 8px;">
              <el-switch v-model="enableTools" active-text="启用工具调用" size="small" />
              <el-select v-model="preferredModel" size="small" style="width: 160px;" disabled>
                <el-option label="DeepSeek" value="deepseek" />
              </el-select>
              <el-button size="small" @click="clearSession" :disabled="!sessionId">清除当前</el-button>
              <div v-if="sessionId" style="font-size: 11px; opacity: 0.5; display: flex; align-items: center; margin-left: auto;">
                Session: {{ sessionId.substring(0, 8) }}...
              </div>
            </div>
            <div class="chat-input-area">
              <el-input
                v-model="inputText"
                @keyup.enter.exact="sendMessage"
                placeholder="输入您的需求..."
                :disabled="loading"
              />
              <el-button type="primary" @click="sendMessage" :loading="loading" :icon="Promotion">
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Rename Dialog -->
    <el-dialog v-model="renameVisible" title="重命名会话" width="400px" :close-on-click-modal="false">
      <el-input v-model="renameValue" placeholder="输入新名称" @keyup.enter="doRename" maxlength="60" show-word-limit />
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" @click="doRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion, Edit, Delete, Cpu, UserFilled, Tools, Refresh } from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const enableTools = ref(true)
const preferredModel = ref('deepseek')
const sessionId = ref(null)
const sessions = ref([])

const renameVisible = ref(false)
const renameValue = ref('')
const renameTarget = ref(null)

function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br/>')
    .replace(/`([^`]+)`/g, '<code style="background:#e8e8e8;padding:1px 4px;border-radius:3px;">$1</code>')
}

function scrollToBottom() {
  nextTick(() => {
    const el = document.querySelector('.chat-messages')
    if (el) el.scrollTop = el.scrollHeight
  })
}

onMounted(() => loadSessionList())

async function loadSessionList() {
  try {
    const res = await agentApi.getSessions()
    sessions.value = res.data.sessions || []
  } catch (e) {}
}

function newSession() {
  messages.value = []
  sessionId.value = null
}

async function loadSession(sid) {
  try {
    const res = await agentApi.getDBHistory(sid)
    const records = res.data.records || []
    messages.value = records.map(r => ({
      role: r.role,
      content: r.content,
      toolCalls: r.toolName ? [{
        toolName: r.toolName,
        output: r.toolResult,
        durationMs: r.processingTimeMs || 0
      }] : undefined
    }))
    sessionId.value = sid
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载历史失败')
  }
}

function startRename(s) {
  renameTarget.value = s
  renameValue.value = s.title || ''
  renameVisible.value = true
}

async function doRename() {
  const val = renameValue.value.trim()
  if (!val) { ElMessage.warning('名称不能为空'); return }
  try {
    await agentApi.updateTitle(renameTarget.value.sessionId, val)
    renameTarget.value.title = val
    renameVisible.value = false
    ElMessage.success('已重命名')
    loadSessionList()
  } catch (e) {
    ElMessage.error('重命名失败')
  }
}

async function confirmDelete(s) {
  try {
    await ElMessageBox.confirm(
      `确定删除会话「${s.title}」吗？删除后不可恢复。`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await agentApi.clearSession(s.sessionId)
    if (sessionId.value === s.sessionId) {
      messages.value = []
      sessionId.value = null
    }
    ElMessage.success('已删除')
    loadSessionList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = enableTools.value
      ? await agentApi.chatWithTools({
          sessionId: sessionId.value,
          message: text,
          preferredModel: preferredModel.value || undefined,
          enableTools: true
        })
      : await agentApi.chat({
          sessionId: sessionId.value,
          message: text,
          preferredModel: preferredModel.value || undefined,
          enableTools: false
        })

    sessionId.value = res.data.sessionId
    messages.value.push({
      role: 'assistant',
      content: res.data.message,
      toolCalls: res.data.toolCalls,
      modelUsed: res.data.modelUsed
    })
    loadSessionList()
    setTimeout(() => loadSessionList(), 2500)
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，请求失败：' + (e.message || '未知错误') })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

async function clearSession() {
  if (!sessionId.value) return
  try {
    await agentApi.clearSession(sessionId.value)
    messages.value = []
    sessionId.value = null
    ElMessage.success('会话已清除')
    loadSessionList()
  } catch (e) {
    ElMessage.error('清除失败')
  }
}
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-secondary);
}

.empty-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-bg), rgba(129, 140, 248, 0.12));
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: var(--primary);
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.15);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 4px 20px rgba(99, 102, 241, 0.15); }
  50% { box-shadow: 0 4px 30px rgba(99, 102, 241, 0.3); }
}

.empty-state h3 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: var(--text-secondary);
}

.empty-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 24px;
}

.hint-chip {
  display: inline-block;
  padding: 8px 16px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 20px;
  font-size: 12.5px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.hint-chip:hover {
  border-color: var(--primary-light);
  color: var(--primary);
  background: var(--primary-bg);
}

.tool-time {
  margin-left: 8px;
  color: var(--text-muted);
  font-size: 11px;
}

.tool-output {
  margin-top: 4px;
  white-space: pre-wrap;
  font-size: 11px;
  max-height: 80px;
  overflow-y: auto;
  color: var(--text-secondary);
}

.session-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all 0.2s;
}
.session-item:hover {
  background: #f0f5ff;
}
.session-item.active {
  background: #e6f7ff;
  border-left: 3px solid var(--primary);
}
.session-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.session-title {
  font-size: 13px;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-actions {
  display: none;
  gap: 2px;
}
.session-item:hover .session-actions {
  display: flex;
}
</style>
