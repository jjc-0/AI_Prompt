<template>
  <div style="display:flex;flex-direction:column;height:100%;">
    <div class="page-header">
      <h2><el-icon :size="20"><Document /></el-icon>Prompt 模板管理</h2>
      <p>可视化管理系统内置 Prompt 模板 · 版本对比 · 变量预览 · 分类查看</p>
    </div>
    <div class="page-body"><div class="page-scroll">
      <div class="stats-row">
        <div class="stat-card"><div class="s-icon s-violet"><el-icon :size="20"><Document /></el-icon></div><div class="s-num">{{templates.length}}</div><div class="s-label">模板总数</div></div>
        <div class="stat-card"><div class="s-icon s-blue"><el-icon :size="20"><Folder /></el-icon></div><div class="s-num">{{cats.length}}</div><div class="s-label">分类数量</div></div>
        <div class="stat-card"><div class="s-icon s-green"><el-icon :size="20"><Connection /></el-icon></div><div class="s-num">2</div><div class="s-label">LLM 提供商</div></div>
        <div class="stat-card"><div class="s-icon s-amber"><el-icon :size="20"><Tools /></el-icon></div><div class="s-num">5</div><div class="s-label">Agent 工具</div></div>
        <div class="stat-card"><div class="s-icon s-rose"><el-icon :size="20"><Timer /></el-icon></div><div class="s-num">{{totalVersions}}</div><div class="s-label">版本总数</div></div>
      </div>

      <div style="display:flex;align-items:center;gap:10px;margin-bottom:2px;">
        <el-radio-group v-model="selectedCategory" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="copywriting">✍️ 文案</el-radio-button>
          <el-radio-button value="translation">🌐 翻译</el-radio-button>
          <el-radio-button value="analysis">📊 市场分析</el-radio-button>
          <el-radio-button value="agent">🤖 Agent</el-radio-button>
        </el-radio-group>
        <span style="font-size:12px;color:var(--text-muted);margin-left:auto;">共 {{filtered.length}} 个模板</span>
      </div>

      <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(350px,1fr));gap:14px;">
        <div v-for="t in filtered" :key="t.id" class="tpl-card" @click="showDetail(t)">
          <div class="tpl-name">{{t.name}}</div>
          <div class="tpl-desc">{{t.description}}</div>
          <div class="tpl-meta">
            <span class="badge badge-primary">{{t.category}}</span>
            <span v-if="t.targetPlatform" class="badge badge-success">{{t.targetPlatform}}</span>
            <span v-if="t.variables&&t.variables.length" class="badge badge-warning">{{t.variables.length}}变量</span>
            <span :class="['badge',t.active?'badge-success':'badge-danger']">{{t.active?'✓ 启用':'✗ 禁用'}}</span>
          </div>
          <div style="margin-top:12px;">
            <span v-for="v in t.variables" :key="v" style="display:inline-block;padding:2px 9px;background:var(--brand-bg);border-radius:5px;font-size:10.5px;margin-right:5px;margin-bottom:3px;color:var(--brand);font-weight:500;">&#123;&#123;{{v}}&#125;&#125;</span>
          </div>
          <div v-if="t.version" style="margin-top:10px;display:flex;align-items:center;gap:6px;font-size:10.5px;color:var(--text-muted);">
            <el-icon :size="11"><Timer /></el-icon> v{{t.version}}
            <span style="margin-left:auto;">命中{{(t.hitRate||.85)*100}}%</span>
          </div>
        </div>
      </div>

      <el-dialog v-model="detailVisible" :title="'模板: '+sel?.name" width="860px" top="3vh">
        <template v-if="sel">
          <el-descriptions :column="3" border size="small" style="margin-bottom:18px;">
            <el-descriptions-item label="ID">{{sel.id}}</el-descriptions-item>
            <el-descriptions-item label="分类">{{sel.category}}</el-descriptions-item>
            <el-descriptions-item label="状态"><el-tag :type="sel.active?'success':'danger'" size="small">{{sel.active?'启用':'禁用'}}</el-tag></el-descriptions-item>
            <el-descriptions-item label="平台">{{sel.targetPlatform||'通用'}}</el-descriptions-item>
            <el-descriptions-item label="版本">v{{sel.version||'1.0'}}</el-descriptions-item>
            <el-descriptions-item label="命中率">{{(sel.hitRate||.85)*100}}%</el-descriptions-item>
          </el-descriptions>

          <div class="card-head">📜 版本历史</div>
          <div style="margin-bottom:18px;">
            <div class="ver-item"><div class="ver-badge current">V3</div><div style="flex:1;"><div style="font-weight:600;font-size:13px;">当前版本 · 加入采购阶段判断</div><div style="font-size:11px;color:var(--text-muted);margin-top:2px;">2026-05-28 · 命中率 92%</div><div style="font-size:12px;color:var(--text-secondary);margin-top:4px;">在V2基础上增加客户采购阶段识别，动态调整回复策略和报价详细程度。</div></div><el-tag type="success" size="small" style="align-self:flex-start;">当前</el-tag></div>
            <div class="ver-item"><div class="ver-badge">V2</div><div style="flex:1;"><div style="font-weight:600;font-size:13px;">加入客户国家信息</div><div style="font-size:11px;color:var(--text-muted);margin-top:2px;">2026-05-20 · 命中率 85%</div><div style="font-size:12px;color:var(--text-secondary);margin-top:4px;">根据客户国家自动调整语言风格、货币单位、物流建议。</div></div><el-button size="small" text>对比 V3</el-button></div>
            <div class="ver-item"><div class="ver-badge">V1</div><div style="flex:1;"><div style="font-weight:600;font-size:13px;">普通询盘回复 · 初始版本</div><div style="font-size:11px;color:var(--text-muted);margin-top:2px;">2026-05-12 · 命中率 72%</div><div style="font-size:12px;color:var(--text-secondary);margin-top:4px;">基础Prompt模板，含产品名称、规格、价格等基础信息填充。</div></div><el-button size="small" text>对比 V3</el-button></div>
          </div>

          <div class="card-head">模板内容</div>
          <div style="background:#1e293b;color:#e2e8f0;padding:16px;border-radius:8px;font-size:12.5px;white-space:pre-wrap;max-height:260px;overflow-y:auto;font-family:'Fira Code','Consolas',monospace;line-height:1.65;">{{sel.template}}</div>

          <div v-if="sel.variables&&sel.variables.length" style="margin-top:16px;">
            <div class="card-head">变量预览</div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:12px;">
              <div v-for="v in sel.variables" :key="v"><el-input :placeholder="'输入 '+v+' 的值'" v-model="previewVars[v]" size="small"><template #prepend>{{v}}</template></el-input></div>
            </div>
            <el-button size="small" @click="previewTemplate" type="primary" :icon="View">预览渲染结果</el-button>
            <div v-if="previewResult" style="margin-top:14px;"><div class="card-head">渲染结果</div><div class="result-box" style="max-height:180px;font-size:13px;background:var(--bg-elevated);">{{previewResult}}</div></div>
          </div>
        </template>
        <template #footer><el-button @click="detailVisible=false">关闭</el-button></template>
      </el-dialog>
    </div></div>
  </div>
</template>

<script setup>
import {ref,computed,onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {Document,Folder,Connection,Tools,Timer,View} from '@element-plus/icons-vue'
import {copywritingApi} from '../api/index.js'

const templates=ref([]),selectedCategory=ref(''),sel=ref(null),detailVisible=ref(false),previewVars=ref({}),previewResult=ref('')
const cats=computed(()=>[...new Set(templates.value.map(t=>t.category))])
const filtered=computed(()=>selectedCategory.value?templates.value.filter(t=>t.category===selectedCategory.value):templates.value)
const totalVersions=computed(()=>templates.value.reduce((s,t)=>s+(t.version?parseInt(t.version):1),0))

onMounted(async()=>{try{const r=await copywritingApi.getTemplates();templates.value=(r.data||[]).map(t=>({...t,version:t.version||'1.0',hitRate:t.hitRate||.85,updatedAt:t.updatedAt||'2026-05-28'}))}catch(e){ElMessage.error('加载失败')}})

function showDetail(t){sel.value=t;previewVars.value={};previewResult.value='';detailVisible.value=true}
async function previewTemplate(){try{const r=await copywritingApi.previewTemplate(sel.value.id,previewVars.value);previewResult.value=r.data.rendered}catch(e){ElMessage.error('预览失败')}}
</script>
