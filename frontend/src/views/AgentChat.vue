<template>
  <div style="display:flex;flex-direction:column;height:100%;">
    <div class="page-header">
      <h2><el-icon :size="20"><ChatDotRound /></el-icon>AI Agent 对话</h2>
      <p>工具调用 · 多轮对话 · 多模型协同 · RAG 知识增强</p>
    </div>
    <div class="page-body" style="padding:0;">
      <div class="page-split">
        <!-- Session sidebar -->
        <div class="split-side">
          <div class="card" style="padding:10px;flex-shrink:0;">
            <div style="display:flex;gap:6px;">
              <el-button size="small" @click="loadSessions" :icon="Refresh" style="flex:1;">刷新</el-button>
              <el-button size="small" @click="newSession" type="primary" plain :icon="Plus" style="flex:1;">新建</el-button>
            </div>
          </div>
          <div class="card card-fill card-pad0" style="padding:6px;">
            <div class="sess-list">
              <div v-if="sessions.length===0" style="text-align:center;padding:36px 16px;color:var(--text-muted);font-size:13px;">
                <el-icon :size="26" style="margin-bottom:8px;"><ChatLineSquare /></el-icon>
                <div>暂无会话</div>
              </div>
              <div v-for="s in sessions" :key="s.sessionId"
                :class="['sess-item',{active:s.sessionId===sessionId}]"
                @click="loadSession(s.sessionId)">
                <div class="sess-top">
                  <div class="sess-title" @dblclick.stop="startRename(s)">{{s.title}}</div>
                  <div class="sess-actions">
                    <el-button :icon="Edit" size="small" text @click.stop="startRename(s)"/>
                    <el-button :icon="Delete" size="small" text @click.stop="confirmDelete(s)"/>
                  </div>
                </div>
                <div style="font-size:10.5px;color:var(--text-muted);margin-top:2px;">{{s.messageCount}} 条 · {{s.updatedAt?.substring(0,16)}}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Chat area -->
        <div class="split-main">
          <div class="card card-fill card-pad0">
            <div class="chat-wrap">
              <div class="chat-msgs">
                <div v-if="messages.length===0" class="empty-state">
                  <div class="empty-avatar"><el-icon :size="34"><Cpu /></el-icon></div>
                  <h3>JC Display AI Agent</h3>
                  <p>B2B 出口 AI 助手 · 支持工具调用与多轮对话</p>
                  <div class="hint-row">
                    <span class="hint-chip" @click="hintSend('帮我生成一个瓦楞纸展示架的产品详情')">📝 产品详情</span>
                    <span class="hint-chip" @click="hintSend('分析美国市场展示架的出口机会')">📊 市场分析</span>
                    <span class="hint-chip" @click="hintSend('将产品描述翻译成日语')">🌐 翻译</span>
                  </div>
                </div>

                <div v-for="(msg,i) in messages" :key="i" :class="['chat-msg',msg.role]">
                  <div :class="['chat-avatar',msg.role]">
                    <el-icon :size="14" v-if="msg.role==='user'"><UserFilled /></el-icon>
                    <el-icon :size="14" v-else><Cpu /></el-icon>
                  </div>
                  <div>
                    <div :class="['chat-bubble',msg.role==='user'?'user':'bot']" v-html="renderMd(msg.content)"></div>
                    <div v-if="msg.modelUsed" style="font-size:10px;color:var(--text-muted);margin-top:3px;text-align:right;">{{msg.modelUsed}}</div>
                    <div v-if="msg.toolCalls&&msg.toolCalls.length">
                      <div v-for="(tc,ti) in msg.toolCalls" :key="ti" class="tool-log">
                        <span class="t-name"><el-icon :size="12"><Tools /></el-icon>{{tc.toolName}}</span>
                        <span style="margin-left:8px;font-size:10px;color:var(--text-muted);">{{tc.durationMs}}ms</span>
                        <div style="margin-top:4px;white-space:pre-wrap;font-size:11px;max-height:70px;overflow-y:auto;color:var(--text-secondary);">{{tc.output}}</div>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="loading" class="chat-msg assistant">
                  <div class="chat-avatar bot"><el-icon :size="14"><Cpu /></el-icon></div>
                  <div class="typing"><span></span><span></span><span></span></div>
                </div>
              </div>

              <div class="chat-input">
                <el-switch v-model="enableTools" active-text="工具" size="small" style="flex-shrink:0;"/>
                <el-input v-model="inputText" placeholder="输入消息，Enter 发送..." @keyup.enter="sendMessage" :disabled="loading" clearable/>
                <el-button type="primary" @click="sendMessage" :loading="loading" :icon="Promotion">发送</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="renameVisible" title="重命名" width="400px" :close-on-click-modal="false">
      <el-input v-model="renameValue" placeholder="输入新名称" @keyup.enter="doRename" maxlength="60" show-word-limit/>
      <template #footer><el-button @click="renameVisible=false">取消</el-button><el-button type="primary" @click="doRename">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref,nextTick,onMounted} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import {Cpu,ChatDotRound,ChatLineSquare,UserFilled,Promotion,Refresh,Edit,Delete,Tools,Plus} from '@element-plus/icons-vue'
import {agentApi} from '../api/index.js'

const messages=ref([]),sessions=ref([]),inputText=ref(''),loading=ref(false),enableTools=ref(true),sessionId=ref(null)
const renameVisible=ref(false),renameValue=ref(''),renameTarget=ref(null)

function renderMd(t){if(!t)return'';return t.replace(/\*\*(.*?)\*\*/g,'<strong>$1</strong>').replace(/\*(.*?)\*/g,'<em>$1</em>').replace(/\n/g,'<br/>').replace(/`([^`]+)`/g,'<code style="background:var(--brand-bg);padding:1px 5px;border-radius:3px;color:var(--brand);">$1</code>')}
function scrollBottom(){nextTick(()=>{const e=document.querySelector('.chat-msgs');if(e)e.scrollTop=e.scrollHeight})}
onMounted(()=>loadSessions())
async function loadSessions(){try{const r=await agentApi.getSessions();sessions.value=r.data.sessions||[]}catch(e){}}
function newSession(){messages.value=[];sessionId.value=null}
async function loadSession(sid){try{const r=await agentApi.getDBHistory(sid);messages.value=(r.data.records||[]).map(r=>({role:r.role,content:r.content,toolCalls:r.toolName?[{toolName:r.toolName,output:r.toolResult,durationMs:r.processingTimeMs||0}]:undefined}));sessionId.value=sid;scrollBottom()}catch(e){ElMessage.error('加载失败')}}
function startRename(s){renameTarget.value=s;renameValue.value=s.title||'';renameVisible.value=true}
async function doRename(){const v=renameValue.value.trim();if(!v){ElMessage.warning('名称不能为空');return};try{await agentApi.updateTitle(renameTarget.value.sessionId,v);renameTarget.value.title=v;renameVisible.value=false;ElMessage.success('已重命名');loadSessions()}catch(e){ElMessage.error('重命名失败')}}
async function confirmDelete(s){try{await ElMessageBox.confirm('确定删除"${s.title}"吗？','确认删除',{confirmButtonText:'删除',cancelButtonText:'取消',type:'warning'});await agentApi.clearSession(s.sessionId);if(sessionId.value===s.sessionId){messages.value=[];sessionId.value=null};ElMessage.success('已删除');loadSessions()}catch(e){if(e!=='cancel')ElMessage.error('删除失败')}}
async function sendMessage(){const t=inputText.value.trim();if(!t||loading.value)return;messages.value.push({role:'user',content:t});inputText.value='';loading.value=true;scrollBottom();try{const r=enableTools.value?await agentApi.chatWithTools({sessionId:sessionId.value,message:t,preferredModel:void 0,enableTools:true}):await agentApi.chat({sessionId:sessionId.value,message:t,preferredModel:void 0,enableTools:false});sessionId.value=r.data.sessionId;messages.value.push({role:'assistant',content:r.data.message,toolCalls:r.data.toolCalls,modelUsed:r.data.modelUsed});loadSessions();setTimeout(()=>loadSessions(),2500)}catch(e){messages.value.push({role:'assistant',content:'请求失败：'+(e.message||'未知错误')})}finally{loading.value=false;scrollBottom()}}
function hintSend(t){inputText.value=t;sendMessage()}
</script>
