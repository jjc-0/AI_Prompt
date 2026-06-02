<template>
  <div>
    <div class="page-header">
      <h2><el-icon :size="22"><ChatDotRound /></el-icon> AI Agent 对话</h2>
      <p>具备工具调用能力的智能 Agent · 支持多轮对话 · 多模型协同 · RAG 知识增强</p>
    </div>
    <div class="page-content" style="display: flex; gap: 16px;">
      <!-- Session sidebar -->
      <div style="width: 250px; flex-shrink: 0; display: flex; flex-direction: column; gap: 12px;">
        <div class="card" style="padding: 14px;">
          <div style="display: flex; gap: 8px;">
            <el-button size="small" @click="loadSessionList" :icon="Refresh" style="flex:1;">刷新</el-button>
            <el-button size="small" @click="newSession" type="primary" plain :icon="Plus" style="flex:1;">新建</el-button>
          </div>
        </div>
        <div class="card" style="padding: 8px; flex: 1; overflow-y: auto; max-height: calc(100vh - 260px);">
          <div v-if="sessions.length === 0" style="text-align: center; padding: 30px; color: var(--text-secondary); font-size: 13px;">
            <el-icon :size="28" style="color:var(--text-muted);margin-bottom:8px;"><ChatLineSquare /></el-icon>
            <div>暂无历史会话</div>
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
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 3px;">
              {{ s.messageCount }} 条 · {{ s.updatedAt?.substring(0, 16) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Chat area -->
      <div class="card" style="flex: 1; padding: 0; overflow: hidden; display: flex; flex-direction: column;">
        <div class="chat-container" style="height: calc(100vh - 215px);">
          <div class="chat-messages">
            <div v-if="messages.length === 0" class="empty-state">
              <div class="empty-avatar">
                <el-icon :size="36"><Cpu /></el-icon>
              </div>
              <h3>JC Display AI Agent</h3>
              <p>B2B 出口 AI 助手 · 支持工具调用与多轮对话</p>
              <div class="empty-hints">
                <span class="hint-chip" @click="hintSend('帮我生成一个瓦楞纸展示架的产品详情')">📝 生成产品详情</span>
                <span class="hint-chip" @click="hintSend('分析美国市场展示架的出口机会')">📊 市场分析</span>
                <span class="hint-chip" @click="hintSend('将产品描述翻译成日语')">🌐 多语言翻译</span>
              </div>
            </div>

            <div v-for="(msg, index) in messages" :key="index"
              :class="['chat-message', msg.role]">
              <div :class="['chat-avatar', msg.role]">
                <el-icon :size="16" v-if="msg.role === 'user'"><UserFilled /></el-icon>
                <el-icon :size="16" v-else><Cpu /></el-icon>
              </div>
              <div>
                <div :class="['chat-bubble', msg.role]" v-html="renderMarkdown(msg.content)"></div>
                <div v-if="msg.modelUsed" style="font-size:10px;color:var(--text-muted);margin-top:4px;text-align:right;">
                  {{ msg.modelUsed }}
                </div>
                <div v-if="msg.toolCalls && msg.toolCalls.length > 0">
                  <div v-for="(tc, tIdx) in msg.toolCalls" :key="tIdx" class="tool-call-log">
                    <span class="tool-name"><el-icon :size="13"><Tools /></el-icon> {{ tc.toolName }}</span>
                    <span style="margin-left: 8px; font-size: 11px; color: var(--text-muted);">{{ tc.durationMs }}ms</span>
                    <div style="margin-top: 6px; white-space: pre-wrap; font-size: 11px; max-height: 80px; overflow-y: auto; color: var(--text-secondary);">{{ tc.output }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="loading" class="chat-message assistant">
              <div class="chat-avatar assistant">
                <el-icon :size="16"><Cpu /></el-icon>
              </div>
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>

          <div class="chat-input-area">
            <div style="display: flex; gap: 8px; align-items: center; margin-right: 8px; flex-shrink: 0;">
              <el-switch v-model="enableTools" active-text="工具" size="small" />
            </div>
            <el-input
              v-model="inputText"
              placeholder="输入消息，Enter 发送..."
              @keyup.enter="sendMessage"
              :disabled="loading"
              clearable
            />
            <el-button type="primary" @click="sendMessage" :loading="loading" :icon="Promotion">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Rename dialog -->
    <el-dialog v-model="renameVisible" title="重命名" width="420px" :close-on-click-modal="false">
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
import { Cpu, ChatDotRound, ChatLineSquare, UserFilled, Promotion, Refresh, Edit, Delete, Tools, Plus } from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'

const messages = ref([])
const sessions = ref([])
const inputText = ref('')
const loading = ref(false)
const enableTools = ref(true)
const sessionId = ref(null)
const preferredModel = ref('')

const renameVisible = ref(false)
const renameValue = ref('')
const renameTarget = ref(null)

function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br/>')
    .replace(/`([^`]+)`/g, '<code style="background:#f1f5f9;padding:1px 6px;border-radius:4px;color:#6366f1;">$1</code>')
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
      `确定删除「${s.title}」吗？`,
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
    messages.value.push({ role: 'assistant', content: '请求失败：' + (e.message || '未知错误') })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function hintSend(text) {
  inputText.value = text
  sendMessage()
}
</script>
