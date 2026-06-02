<template>
  <div style="display:flex;flex-direction:column;height:100%;">
    <div class="page-header">
      <h2><el-icon :size="20"><Connection /></el-icon>多语言翻译</h2>
      <p>展示架行业专业翻译 · 多语言互译 · 跨境电商本地化增强</p>
    </div>
    <div class="page-body"><div class="page-split">
      <div class="split-side">
        <div class="card" style="padding:10px;flex-shrink:0;"><el-button size="small" @click="loadHistory" :icon="Refresh" style="width:100%;">刷新历史</el-button></div>
        <div class="card card-fill card-pad0" style="padding:6px;"><div class="sess-list stagger-in">
          <div v-if="historyList.length===0" style="text-align:center;padding:36px 16px;color:var(--text-muted);font-size:13px;"><el-icon :size="26" style="margin-bottom:8px;"><Connection /></el-icon><div>暂无历史</div></div>
          <div v-for="h in historyList" :key="h.sessionId" :class="['sess-item',{active:h.sessionId===currentSessionId}]" @click="loadHistoryItem(h)">
            <div class="sess-top"><div class="sess-title" @dblclick.stop="startRename(h)">{{h.title}}</div><div class="sess-actions"><el-button size="small" text @click.stop="startRename(h)"><el-icon><Edit /></el-icon></el-button><el-button size="small" text @click.stop="confirmDelete(h)"><el-icon><Delete /></el-icon></el-button></div></div>
            <div style="font-size:10.5px;color:var(--text-muted);margin-top:2px;">{{h.messageCount}} 条 · {{h.updatedAt?.substring(0,16)}}</div>
          </div>
        </div></div>
      </div>
      <div class="split-main"><div class="page-scroll">
        <div class="card">
          <div class="card-head">翻译设置</div>
          <div class="form-row" style="margin-bottom:12px;">
            <el-form-item label="源语言" style="flex:1;"><el-select v-model="form.sourceLanguage" size="large"><el-option label="中文" value="中文"/><el-option label="English" value="English"/><el-option label="日本語" value="Japanese"/></el-select></el-form-item>
            <el-form-item label="目标语言" style="flex:1;"><el-select v-model="form.targetLanguage" size="large"><el-option label="🇬🇧 English" value="英文"/><el-option label="🇯🇵 日本語" value="日文"/><el-option label="🇰🇷 한국어" value="韩文"/><el-option label="🇩🇪 Deutsch" value="德文"/><el-option label="🇫🇷 Français" value="法文"/><el-option label="🇪🇸 Español" value="西班牙文"/></el-select></el-form-item>
          </div>
          <el-form-item label="待翻译文本" style="margin-bottom:14px;"><el-input v-model="form.text" type="textarea" :rows="4" placeholder="输入要翻译的文本..." size="large"/></el-form-item>
          <div style="display:flex;align-items:center;gap:12px;">
            <el-button type="primary" @click="doTranslate" :loading="translating" :icon="Reading" size="large">智能翻译</el-button>
            <el-checkbox v-model="form.ecommerceLocalization" label="电商本地化增强" size="small" border/>
            <el-tag v-if="translating" type="warning" size="large">🤖 翻译中...</el-tag>
            <el-tag v-if="currentSessionId" type="success" size="small" style="margin-left:auto;"><el-icon :size="12"><Check /></el-icon>已保存</el-tag>
          </div>
        </div>
        <div v-if="result" class="card result-box-animated"><div class="card-head">翻译结果</div><div class="result-box" v-html="rendered"></div></div>
      </div></div>
    </div></div>
    <el-dialog v-model="renameVisible" title="重命名" width="400px" :close-on-click-modal="false">
      <el-input v-model="renameValue" placeholder="输入新名称" @keyup.enter="doRename" maxlength="60" show-word-limit/>
      <template #footer><el-button @click="renameVisible=false">取消</el-button><el-button type="primary" @click="doRename">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref,computed,onMounted} from 'vue'
import {ElMessage,ElMessageBox} from 'element-plus'
import {Reading,Edit,Delete,Connection,Refresh,Check} from '@element-plus/icons-vue'
import {translateApi,agentApi} from '../api/index.js'
const form=ref({sourceLanguage:'中文',targetLanguage:'英文',text:'',context:'跨境电商商品信息',ecommerceLocalization:true})
const result=ref(''),translating=ref(false),currentSessionId=ref(null),historyList=ref([]),resultInfo=ref({})
const renameVisible=ref(false),renameValue=ref(''),renameTarget=ref(null)
const rendered=computed(()=>result.value?result.value.replace(/\*\*(.*?)\*\*/g,'<strong>$1</strong>').replace(/\n/g,'<br/>'):'')
onMounted(()=>loadHistory())
async function loadHistory(){try{const r=await agentApi.getSessions('translate');historyList.value=r.data.sessions||[]}catch(e){}}
async function loadHistoryItem(h){try{const r=await agentApi.getDBHistory(h.sessionId);result.value=(r.data.records||[]).filter(r=>r.role==='assistant').map(r=>r.content).join('\n\n')||'No result';currentSessionId.value=h.sessionId}catch(e){ElMessage.error('加载失败')}}
async function doTranslate(){if(!form.value.text){ElMessage.warning('请输入文本');return};translating.value=true;result.value='';try{const r=await translateApi.translate({text:form.value.text,sourceLanguage:form.value.sourceLanguage,targetLanguage:form.value.targetLanguage,context:form.value.context,ecommerceLocalization:form.value.ecommerceLocalization});result.value=r.data.result;resultInfo.value={processingTimeMs:r.data.processingTimeMs};currentSessionId.value=r.data.sessionId;ElMessage.success('翻译完成');loadHistory();setTimeout(()=>loadHistory(),2500)}catch(e){ElMessage.error('翻译失败')}finally{translating.value=false}}
function startRename(s){renameTarget.value=s;renameValue.value=s.title||'';renameVisible.value=true}
async function doRename(){const v=renameValue.value.trim();if(!v){ElMessage.warning('名称不能为空');return};try{await agentApi.updateTitle(renameTarget.value.sessionId,v);renameTarget.value.title=v;renameVisible.value=false;ElMessage.success('已重命名');loadHistory()}catch(e){ElMessage.error('重命名失败')}}
async function confirmDelete(s){try{await ElMessageBox.confirm('确定删除"'+s.title+'"吗？','确认删除',{confirmButtonText:'删除',cancelButtonText:'取消',type:'warning'});await agentApi.clearSession(s.sessionId);if(currentSessionId.value===s.sessionId){result.value='';currentSessionId.value=null};ElMessage.success('已删除');loadHistory()}catch(e){if(e!=='cancel')ElMessage.error('删除失败')}}
</script>
