<template>
  <div class="chat-layout">
    <!-- 对话历史侧边栏 -->
    <aside class="chat-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="chat-sidebar-inner">
        <div class="chat-sidebar-new" @click="newSession">
          <el-icon :size="16"><Plus /></el-icon>
          <span>新对话</span>
        </div>
        <div class="chat-sidebar-list">
          <div v-if="sessions.length===0" class="chat-sidebar-empty">暂无对话记录</div>
          <div
            v-for="s in sessions" :key="s.sessionId"
            :class="['chat-sidebar-item', { active: s.sessionId === sessionId }]"
            @click="loadSession(s.sessionId)"
          >
            <el-icon :size="14" style="flex-shrink:0;opacity:.5;"><ChatDotRound /></el-icon>
            <span class="chat-sidebar-title" @dblclick.stop="startRename(s)">{{ s.title }}</span>
            <span class="chat-sidebar-actions">
              <el-button text size="small" @click.stop="startRename(s)"><el-icon :size="12"><Edit /></el-icon></el-button>
              <el-button text size="small" @click.stop="confirmDelete(s)"><el-icon :size="12"><Delete /></el-icon></el-button>
            </span>
          </div>
        </div>
        <div class="chat-sidebar-bottom">
          <span>JC Display</span>
        </div>
      </div>
    </aside>

    <!-- 主对话区 -->
    <div class="chat-main">
      <!-- 侧边栏折叠按钮 -->
      <div class="chat-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
        <el-icon :size="18">
          <Expand v-if="sidebarCollapsed" />
          <Fold v-else />
        </el-icon>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="msgContainer">
        <!-- 欢迎页 -->
        <div v-if="messages.length === 0" class="chat-welcome">
          <div class="chat-welcome-logo">
            <el-icon :size="40"><Cpu /></el-icon>
          </div>
          <h2>有什么可以帮助您的？</h2>
          <p>基于 1,435 款真实产品数据，智能回答产品咨询</p>
          <div class="chat-hints">
            <div class="chat-hint" @click="hintSend('帮我生成一个瓦楞纸展示架的产品详情')">
              <el-icon :size="14"><EditPen /></el-icon><span>生成产品详情</span>
            </div>
            <div class="chat-hint" @click="hintSend('分析美国市场展示架的出口机会')">
              <el-icon :size="14"><TrendCharts /></el-icon><span>分析出口机会</span>
            </div>
            <div class="chat-hint" @click="hintSend('将产品描述翻译成日语')">
              <el-icon :size="14"><Connection /></el-icon><span>多语言翻译</span>
            </div>
            <div class="chat-hint" @click="hintSend('帮我写一封英文询盘回复邮件')">
              <el-icon :size="14"><Message /></el-icon><span>询盘回复</span>
            </div>
          </div>
        </div>

        <!-- 消息 -->
        <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
          <div class="chat-msg-row">
            <div class="chat-msg-avatar">
              <el-icon :size="18" v-if="msg.role==='user'"><UserFilled /></el-icon>
              <el-icon :size="20" v-else><Cpu /></el-icon>
            </div>
            <div class="chat-msg-body">
              <div class="chat-msg-text" v-html="renderMd(msg.content)"></div>
              <div v-if="msg.toolCalls && msg.toolCalls.length" class="chat-tools">
                <div
                  v-for="(tc, ti) in msg.toolCalls" :key="ti"
                  class="chat-tool" :class="{ open: tc.expanded }"
                  @click="tc.expanded = !tc.expanded"
                >
                  <div class="chat-tool-bar">
                    <el-icon :size="12"><Tools /></el-icon>
                    <span>{{ tc.toolName }}</span>
                    <span class="chat-tool-ms">{{ tc.durationMs }}ms</span>
                    <el-icon :size="12" class="chat-tool-arrow"><ArrowDown /></el-icon>
                  </div>
                  <div class="chat-tool-out">{{ tc.output }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载动画 -->
        <div v-if="loading" class="chat-msg assistant">
          <div class="chat-msg-row">
            <div class="chat-msg-avatar">
              <el-icon :size="20"><Cpu /></el-icon>
            </div>
            <div class="chat-msg-body">
              <div class="chat-typing"><span></span><span></span><span></span></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-wrap">
        <div class="chat-input-row">
          <el-switch v-model="enableTools" active-text="工具" size="small" class="chat-tools-switch" />
          <div class="chat-input-box">
            <input
              v-model="inputText"
              placeholder="向 AI Agent 提问..."
              @keydown.enter.exact.prevent="sendMessage"
              :disabled="loading"
              class="chat-input-el"
              ref="inputRef"
            />
            <button
              class="chat-send"
              @click="sendMessage"
              :disabled="!inputText.trim() || loading"
            >
              <el-icon :size="18"><Promotion /></el-icon>
            </button>
          </div>
        </div>
        <div class="chat-input-note">基于 1,435 款真实产品 · RAG 增强回答</div>
      </div>
    </div>

    <!-- 重命名弹窗 -->
    <el-dialog v-model="renameVisible" title="重命名会话" width="380px" :close-on-click-modal="false">
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
import {
  Cpu, ChatDotRound, UserFilled, Promotion, Refresh, Edit, Delete,
  Tools, Plus, EditPen, TrendCharts, Connection, Message, Expand, Fold, ArrowDown
} from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'

const messages = ref([])
const sessions = ref([])
const inputText = ref('')
const loading = ref(false)
const enableTools = ref(true)
const sessionId = ref(null)
const sidebarCollapsed = ref(false)
const renameVisible = ref(false)
const renameValue = ref('')
const renameTarget = ref(null)
const msgContainer = ref(null)
const inputRef = ref(null)

function renderMd(t) {
  if (!t) return ''
  return t
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br/>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

function scrollBottom() {
  nextTick(() => {
    const el = msgContainer.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

onMounted(() => loadSessions())

async function loadSessions() {
  try {
    const r = await agentApi.getSessions()
    sessions.value = r.data.sessions || []
  } catch (e) { /* ignore */ }
}

function newSession() {
  messages.value = []
  sessionId.value = null
  inputRef.value?.focus()
}

async function loadSession(sid) {
  try {
    const r = await agentApi.getDBHistory(sid)
    messages.value = (r.data.records || []).map(r => ({
      role: r.role,
      content: r.content,
      toolCalls: r.toolName ? [{ toolName: r.toolName, output: r.toolResult, durationMs: r.processingTimeMs || 0, expanded: false }] : undefined
    }))
    sessionId.value = sid
    scrollBottom()
  } catch (e) {
    ElMessage.error('加载失败')
  }
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
    loadSessions()
  } catch (e) { ElMessage.error('重命名失败') }
}

async function confirmDelete(s) {
  try {
    await ElMessageBox.confirm(`确定删除 "${s.title}"？`, '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await agentApi.clearSession(s.sessionId)
    if (sessionId.value === s.sessionId) { messages.value = []; sessionId.value = null }
    ElMessage.success('已删除')
    loadSessions()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

async function sendMessage() {
  const t = inputText.value.trim()
  if (!t || loading.value) return
  messages.value.push({ role: 'user', content: t })
  inputText.value = ''
  loading.value = true
  scrollBottom()
  try {
    const r = enableTools.value
      ? await agentApi.chatWithTools({ sessionId: sessionId.value, message: t, preferredModel: undefined, enableTools: true })
      : await agentApi.chat({ sessionId: sessionId.value, message: t, preferredModel: undefined, enableTools: false })
    sessionId.value = r.data.sessionId
    messages.value.push({
      role: 'assistant',
      content: r.data.message,
      toolCalls: (r.data.toolCalls || []).map(tc => ({ ...tc, expanded: false })),
      modelUsed: r.data.modelUsed
    })
    loadSessions()
    setTimeout(() => loadSessions(), 2500)
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '请求失败：' + (e.message || '未知错误') })
  } finally {
    loading.value = false
    scrollBottom()
  }
}

function hintSend(t) { inputText.value = t; sendMessage() }
</script>

<style scoped>
/* ============================================================
   CHATGPT-STYLE LAYOUT · INDIGO THEME · CLEAN & PREMIUM
   ============================================================ */
.chat-layout {
  display: flex;
  height: 100%;
  background: #fff;
  overflow: hidden;
  font-family: 'Inter', -apple-system, 'Segoe UI', 'Noto Sans SC', sans-serif;
}

/* ---- Sidebar ---- */
.chat-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: #0f172a;
  color: #94a3b8;
  display: flex;
  flex-direction: column;
  transition: width .2s ease;
  overflow: hidden;
}
.chat-sidebar.collapsed { width: 0; }

.chat-sidebar-inner {
  width: 260px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-sidebar-new {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px;
  padding: 10px 14px;
  border: 1px solid rgba(148, 163, 184, .2);
  border-radius: 8px;
  color: #cbd5e1;
  font-size: 13.5px;
  cursor: pointer;
  transition: background .15s;
}
.chat-sidebar-new:hover { background: rgba(99, 102, 241, .1); border-color: rgba(99, 102, 241, .4); }

.chat-sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}
.chat-sidebar-empty {
  text-align: center;
  padding: 32px 16px;
  font-size: 12px;
  color: rgba(148, 163, 184, .3);
}

.chat-sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #94a3b8;
  transition: background .12s;
}
.chat-sidebar-item:hover { background: rgba(255, 255, 255, .05); }
.chat-sidebar-item.active { background: rgba(99, 102, 241, .15); color: #e2e8f0; }

.chat-sidebar-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-sidebar-actions {
  display: none;
  gap: 2px;
}
.chat-sidebar-item:hover .chat-sidebar-actions { display: flex; }
.chat-sidebar-actions :deep(.el-button) { color: #64748b !important; }
.chat-sidebar-actions :deep(.el-button:hover) { color: #cbd5e1 !important; }

.chat-sidebar-bottom {
  padding: 16px;
  border-top: 1px solid rgba(148, 163, 184, .08);
  font-size: 11px;
  color: rgba(148, 163, 184, .25);
  text-align: center;
}

/* ---- Toggle ---- */
.chat-toggle {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
  transition: all .15s;
}
.chat-toggle:hover { background: #f1f5f9; color: #6366f1; border-color: #6366f1; }

/* ---- Main ---- */
.chat-main {
  flex: 1;
  min-width: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* ---- Messages ---- */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* Welcome */
.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 60px 24px;
  text-align: center;
}

.chat-welcome-logo {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, .25);
}

.chat-welcome h2 {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 6px;
}
.chat-welcome p {
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 32px;
}

.chat-hints {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  max-width: 480px;
  width: 100%;
}
.chat-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all .15s;
}
.chat-hint:hover {
  border-color: #6366f1;
  background: #eef2ff;
  color: #6366f1;
}

/* ---- Message Row ---- */
.chat-msg {
  padding: 0 16px;
}
.chat-msg.assistant {
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

.chat-msg-row {
  display: flex;
  gap: 16px;
  max-width: 768px;
  margin: 0 auto;
  padding: 20px 0;
}

.chat-msg-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-top: 2px;
}
.chat-msg.user .chat-msg-avatar { background: #6366f1; }
.chat-msg.assistant .chat-msg-avatar { background: linear-gradient(135deg, #6366f1, #818cf8); }

.chat-msg-body {
  flex: 1;
  min-width: 0;
}

.chat-msg-text {
  font-size: 15px;
  line-height: 1.75;
  color: #1e293b;
}
.chat-msg-text :deep(code) {
  background: #eef2ff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #4f46e5;
}
.chat-msg-text :deep(strong) { font-weight: 700; }

/* Tool calls inline */
.chat-tools {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.chat-tool {
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 12px;
  cursor: pointer;
  max-width: 420px;
  overflow: hidden;
}
.chat-tool-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #a16207;
}
.chat-tool-ms { margin-left: auto; font-weight: 400; color: #94a3b8; font-size: 11px; }
.chat-tool-arrow { transition: transform .2s; }
.chat-tool.open .chat-tool-arrow { transform: rotate(180deg); }

.chat-tool-out {
  display: none;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #fde68a;
  font-size: 11px;
  color: #78716c;
  white-space: pre-wrap;
  max-height: 100px;
  overflow-y: auto;
}
.chat-tool.open .chat-tool-out { display: block; }

/* ---- Typing ---- */
.chat-typing { display: flex; gap: 4px; padding: 6px 0; }
.chat-typing span {
  width: 7px; height: 7px; border-radius: 50%;
  background: #6366f1;
  animation: chatBounce 1.4s infinite ease-in-out;
}
.chat-typing span:nth-child(2) { animation-delay: .2s; }
.chat-typing span:nth-child(3) { animation-delay: .4s; }
@keyframes chatBounce {
  0%,60%,100% { transform: translateY(0); opacity: .3; }
  30% { transform: translateY(-5px); opacity: 1; }
}

/* ---- Input ---- */
.chat-input-wrap {
  flex-shrink: 0;
  padding: 16px 24px 24px;
  background: #fff;
}

.chat-input-row {
  max-width: 768px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-tools-switch {
  flex-shrink: 0;
}

.chat-input-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 4px 6px 4px 16px;
  transition: border-color .15s, box-shadow .15s;
}
.chat-input-box:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, .1);
}

.chat-input-el {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  line-height: 1.5;
  padding: 8px 0;
  color: #1e293b;
  background: transparent;
  font-family: inherit;
}
.chat-input-el::placeholder { color: #94a3b8; }
.chat-input-el:disabled { opacity: .5; }

.chat-send {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: #6366f1;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s;
}
.chat-send:hover:not(:disabled) { background: #4f46e5; }
.chat-send:disabled { background: #e2e8f0; color: #94a3b8; cursor: not-allowed; }

.chat-input-note {
  text-align: center;
  font-size: 11px;
  color: #94a3b8;
  margin-top: 10px;
  max-width: 768px;
  margin-left: auto;
  margin-right: auto;
}

/* ---- Scrollbar ---- */
.chat-sidebar-list::-webkit-scrollbar { width: 4px; }
.chat-sidebar-list::-webkit-scrollbar-track { background: transparent; }
.chat-sidebar-list::-webkit-scrollbar-thumb { background: rgba(148, 163, 184, .15); border-radius: 4px; }

.chat-messages::-webkit-scrollbar { width: 5px; }
.chat-messages::-webkit-scrollbar-track { background: transparent; }
.chat-messages::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 5px; }
</style>
