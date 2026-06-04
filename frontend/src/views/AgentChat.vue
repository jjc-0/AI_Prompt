<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><ChatDotRound /></el-icon>AI Agent 对话</h2>
      <p>基于 1,435 款真实产品数据 · RAG 增强回答 · 多模型智能调度</p>
      <div style="margin-left:auto;display:flex;align-items:center;gap:10px;">
        <el-switch v-model="enableTools" active-text="工具调用" size="small" />
        <el-button size="small" @click="newSession" :icon="Plus">新对话</el-button>
      </div>
    </div>
    <div class="page-body">
      <div class="page-split">
        <!-- 左侧：会话历史 -->
        <div class="split-side">
          <div class="card" style="padding:10px;flex-shrink:0;display:flex;gap:8px;">
            <el-button size="small" @click="loadSessions" :icon="Refresh" style="flex:1;">刷新</el-button>
            <el-button size="small" @click="newSession" :icon="Plus" type="primary">新建</el-button>
          </div>
          <div class="card card-fill" style="padding:6px;">
            <div class="sess-list">
              <div v-if="sessions.length===0" class="empty-state" style="padding:36px 16px;">
                <el-icon :size="26" style="margin-bottom:8px;opacity:.3;"><ChatDotRound /></el-icon>
                <div style="font-size:13px;">暂无对话</div>
              </div>
              <div
                v-for="s in sessions" :key="s.sessionId"
                :class="['sess-item', { active: s.sessionId === sessionId }]"
                @click="loadSession(s.sessionId)"
              >
                <div class="sess-top">
                  <div class="sess-title" @dblclick.stop="startRename(s)">{{ s.title }}</div>
                  <div class="sess-actions">
                    <el-button size="small" text @click.stop="startRename(s)"><el-icon><Edit /></el-icon></el-button>
                    <el-button size="small" text @click.stop="confirmDelete(s)"><el-icon><Delete /></el-icon></el-button>
                  </div>
                </div>
                <div style="font-size:10.5px;color:var(--text-muted);margin-top:2px;">
                  {{ s.messageCount }} 条 · {{ s.updatedAt?.substring(0, 16) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：对话主体 -->
        <div class="split-main" style="display:flex;flex-direction:column;">
          <!-- 消息区域 -->
          <div class="chat-scroll" ref="msgContainer">
            <!-- 欢迎页 -->
            <div v-if="messages.length === 0" class="ds-welcome">
              <div class="ds-logo">
                <el-icon :size="36"><Cpu /></el-icon>
              </div>
              <h1>有什么可以帮助您的？</h1>
              <p>我是 JC Display 的 B2B 出口 AI 助手，基于 1,435 款真实展示架产品数据为您服务</p>
              <div class="ds-hints">
                <button class="ds-hint" @click="hintSend('帮我生成一个瓦楞纸展示架的产品详情')">
                  <el-icon :size="16"><EditPen /></el-icon>
                  <span>生成产品详情</span>
                </button>
                <button class="ds-hint" @click="hintSend('分析美国市场展示架的出口机会')">
                  <el-icon :size="16"><TrendCharts /></el-icon>
                  <span>分析出口机会</span>
                </button>
                <button class="ds-hint" @click="hintSend('将产品描述翻译成日语')">
                  <el-icon :size="16"><Connection /></el-icon>
                  <span>多语言翻译</span>
                </button>
                <button class="ds-hint" @click="hintSend('帮我写一封英文询盘回复邮件')">
                  <el-icon :size="16"><Message /></el-icon>
                  <span>询盘回复邮件</span>
                </button>
              </div>
            </div>

            <!-- 消息列表 -->
            <div v-for="(msg, i) in messages" :key="i" :class="['ds-msg', msg.role]">
              <div class="ds-msg-inner">
                <div class="ds-avatar">
                  <el-icon :size="18" v-if="msg.role==='user'"><UserFilled /></el-icon>
                  <el-icon :size="18" v-else><Cpu /></el-icon>
                </div>
                <div class="ds-bubble">
                  <div class="ds-text" v-html="renderMd(msg.content)"></div>
                  <!-- 工具调用 -->
                  <div v-if="msg.toolCalls && msg.toolCalls.length" class="ds-tools">
                    <div
                      v-for="(tc, ti) in msg.toolCalls" :key="ti"
                      :class="['ds-tool', { open: tc.expanded }]"
                      @click="tc.expanded = !tc.expanded"
                    >
                      <div class="ds-tool-bar">
                        <el-icon :size="13"><Tools /></el-icon>
                        <span>{{ tc.toolName }}</span>
                        <span class="ds-tool-ms">{{ tc.durationMs }}ms</span>
                        <el-icon :size="12" class="ds-tool-arrow"><ArrowDown /></el-icon>
                      </div>
                      <div class="ds-tool-out">{{ tc.output }}</div>
                    </div>
                  </div>
                  <!-- 底部操作 -->
                  <div class="ds-msg-actions">
                    <el-button text size="small" @click="copyMsg(msg.content)"><el-icon><CopyDocument /></el-icon></el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 加载动画 -->
            <div v-if="loading" class="ds-msg assistant">
              <div class="ds-msg-inner">
                <div class="ds-avatar">
                  <el-icon :size="18"><Cpu /></el-icon>
                </div>
                <div class="ds-bubble">
                  <div class="ds-typing">
                    <span></span><span></span><span></span>
                  </div>
                </div>
              </div>
            </div>

            <div class="chat-bottom-spacer"></div>
          </div>

          <!-- 输入区域 -->
          <div class="ds-input-wrap">
            <div class="ds-input-box">
              <textarea
                v-model="inputText"
                placeholder="向 AI Agent 提问..."
                @keydown.enter.exact.prevent="sendMessage"
                :disabled="loading"
                ref="inputRef"
                rows="1"
                class="ds-textarea"
                @input="autoResize"
              ></textarea>
              <div class="ds-input-foot">
                <div class="ds-input-tools">
                  <el-tooltip content="启用工具调用" placement="top">
                    <span :class="['ds-tool-tag', { on: enableTools }]" @click="enableTools = !enableTools">
                      <el-icon :size="12"><Tools /></el-icon> 工具
                    </span>
                  </el-tooltip>
                </div>
                <button
                  class="ds-send"
                  @click="sendMessage"
                  :disabled="!inputText.trim() || loading"
                >
                  <el-icon :size="18"><Promotion /></el-icon>
                </button>
              </div>
            </div>
            <div class="ds-disclaimer">AI 生成内容仅供参考，请核实关键信息</div>
          </div>
        </div>
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
  Cpu, ChatDotRound, UserFilled, Promotion, Edit, Delete,
  Tools, Plus, EditPen, TrendCharts, Connection, Message, Refresh,
  ArrowDown, CopyDocument
} from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'

const messages = ref([])
const sessions = ref([])
const inputText = ref('')
const loading = ref(false)
const enableTools = ref(true)
const sessionId = ref(null)
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

function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 200) + 'px'
}

onMounted(() => loadSessions())

async function loadSessions() {
  try {
    const r = await agentApi.getSessions()
    sessions.value = (r.data.sessions || []).filter(s => !s.operationType || s.operationType === 'agent' || !s.operationType)
  } catch (e) { /* ignore */ }
}

function newSession() {
  messages.value = []
  sessionId.value = null
  nextTick(() => inputRef.value?.focus())
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
  nextTick(autoResize)
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

function copyMsg(text) {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => ElMessage.success('已复制'))
}
</script>

<style scoped>
/* ============================================================
   DEEPSEEK-STYLE CHAT · CLEAN · MODERN · MINIMAL
   ============================================================ */

/* ---- Content layout ---- */
.page-split { height: 100%; }

.split-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;
  overflow: hidden;
}

/* ---- Chat Scroll ---- */
.chat-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  scroll-behavior: smooth;
}
.chat-scroll::-webkit-scrollbar { width: 5px; }
.chat-scroll::-webkit-scrollbar-track { background: transparent; }
.chat-scroll::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 5px; }

.chat-bottom-spacer { height: 40px; }

/* ---- Welcome (DeepSeek style) ---- */
.ds-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 80px 32px;
  text-align: center;
}

.ds-logo {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 28px;
  box-shadow: 0 8px 32px rgba(99,102,241,.2);
}

.ds-welcome h1 {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
  letter-spacing: -.4px;
}

.ds-welcome p {
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 40px;
  line-height: 1.6;
}

.ds-hints {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  max-width: 520px;
  width: 100%;
}

.ds-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 13.5px;
  color: #475569;
  background: #fff;
  cursor: pointer;
  transition: all .18s;
  text-align: left;
  font-family: inherit;
}
.ds-hint:hover {
  border-color: #6366f1;
  background: #eef2ff;
  color: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99,102,241,.08);
}
.ds-hint .el-icon { flex-shrink: 0; color: #6366f1; }

/* ---- Messages ---- */
.ds-msg {
  padding: 0 24px;
}
.ds-msg.assistant {
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

.ds-msg-inner {
  display: flex;
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 0;
}

.ds-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-top: 4px;
}
.ds-msg.user .ds-avatar { background: #6366f1; }
.ds-msg.assistant .ds-avatar { background: linear-gradient(135deg, #6366f1, #818cf8); }

.ds-bubble {
  flex: 1;
  min-width: 0;
}

.ds-text {
  font-size: 15px;
  line-height: 1.8;
  color: #1e293b;
}
.ds-text :deep(strong) { font-weight: 700; color: #0f172a; }
.ds-text :deep(code) {
  background: #eef2ff;
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 13px;
  color: #4f46e5;
  font-family: 'Fira Code', 'Consolas', monospace;
}

/* ---- Message actions (hover) ---- */
.ds-msg-actions {
  margin-top: 8px;
  opacity: 0;
  transition: opacity .15s;
}
.ds-msg:hover .ds-msg-actions { opacity: 1; }

/* ---- Tool calls ---- */
.ds-tools {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ds-tool {
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 12px;
  cursor: pointer;
  max-width: 440px;
  overflow: hidden;
  transition: all .15s;
}
.ds-tool:hover { border-color: #f59e0b; }
.ds-tool-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #a16207;
  user-select: none;
}
.ds-tool-ms { margin-left: auto; font-weight: 400; color: #94a3b8; font-size: 11px; }
.ds-tool-arrow { transition: transform .2s; }
.ds-tool.open .ds-tool-arrow { transform: rotate(180deg); }

.ds-tool-out {
  display: none;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #fde68a;
  font-size: 11px;
  color: #78716c;
  white-space: pre-wrap;
  max-height: 120px;
  overflow-y: auto;
  line-height: 1.6;
}
.ds-tool.open .ds-tool-out { display: block; }

/* ---- Typing ---- */
.ds-typing { display: flex; gap: 4px; padding: 6px 0; }
.ds-typing span {
  width: 6px; height: 6px; border-radius: 50%;
  background: #94a3b8;
  animation: dsBounce 1.4s infinite ease-in-out;
}
.ds-typing span:nth-child(2) { animation-delay: .2s; }
.ds-typing span:nth-child(3) { animation-delay: .4s; }
@keyframes dsBounce {
  0%,60%,100% { transform: translateY(0); opacity: .3; }
  30% { transform: translateY(-4px); opacity: 1; }
}

/* ---- Input area ---- */
.ds-input-wrap {
  flex-shrink: 0;
  padding: 0 24px 20px;
  background: #fff;
}

.ds-input-box {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 8px 10px 8px 16px;
  transition: border-color .15s, box-shadow .15s;
}
.ds-input-box:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,.08);
}

.ds-textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: 15px;
  line-height: 1.6;
  color: #1e293b;
  background: transparent;
  font-family: inherit;
  resize: none;
  min-height: 28px;
  max-height: 200px;
  padding: 4px 0;
}
.ds-textarea::placeholder { color: #94a3b8; }
.ds-textarea:disabled { opacity: .5; }

.ds-input-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
}

.ds-input-tools {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ds-tool-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 9px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  color: #94a3b8;
  background: #f1f5f9;
  cursor: pointer;
  transition: all .15s;
  user-select: none;
}
.ds-tool-tag.on {
  color: #4f46e5;
  background: #eef2ff;
}
.ds-tool-tag:hover { background: #e2e8f0; }
.ds-tool-tag.on:hover { background: #e0e7ff; }

.ds-send {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
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
.ds-send:hover:not(:disabled) { background: #4f46e5; transform: scale(1.05); }
.ds-send:disabled { background: #e2e8f0; color: #94a3b8; cursor: not-allowed; transform: none; }

.ds-disclaimer {
  text-align: center;
  font-size: 11px;
  color: #94a3b8;
  margin-top: 10px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}
</style>
