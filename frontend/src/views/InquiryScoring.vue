<template>
  <div style="display:flex;flex-direction:column;height:100%;">
    <div class="page-header">
      <h2><el-icon :size="20"><DataAnalysis /></el-icon>询盘价值评分</h2>
      <p>AI 智能分析 B2B 询盘 · 识别采购意图 · 评估成交概率 · 提取关键信息</p>
    </div>
    <div class="page-body"><div class="page-split">
      <div class="split-main"><div class="page-scroll">
        <div class="card">
          <div class="card-head">询盘分析</div>
          <div class="form-row" style="margin-bottom:12px;">
            <el-form-item label="客户名称" style="flex:1;"><el-input v-model="form.customerName" placeholder="John Smith 或 ABC Corp"/></el-form-item>
            <el-form-item label="客户国家" style="flex:1;">
              <el-select v-model="form.customerCountry" placeholder="选择国家" filterable>
                <el-option label="美国" value="US"/><el-option label="英国" value="UK"/><el-option label="德国" value="DE"/>
                <el-option label="日本" value="JP"/><el-option label="澳大利亚" value="AU"/><el-option label="法国" value="FR"/>
                <el-option label="加拿大" value="CA"/><el-option label="巴西" value="BR"/><el-option label="其他" value="OTHER"/>
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="询盘内容" style="margin-bottom:14px;">
            <el-input v-model="form.inquiryText" type="textarea" :rows="5" placeholder="粘贴客户询盘内容..."/>
          </el-form-item>
          <div style="display:flex;gap:10px;align-items:center;">
            <el-button type="primary" @click="analyze" :loading="loading" :icon="DataAnalysis" size="large">智能分析</el-button>
            <el-button @click="quickFill" size="large">示例填充</el-button>
            <el-tag v-if="loading" type="warning" size="large">AI 分析中...</el-tag>
          </div>
        </div>

        <div v-if="result" class="card slide-up">
          <div class="card-head">分析结果</div>
          <div style="text-align:center;margin-bottom:20px;">
            <div :class="['score-ring']" :style="{background:'conic-gradient('+ringColor+' 0deg,'+ringColor+' calc('+result.score+'*3.6deg),'+ringBg+' calc('+result.score+'*3.6deg),'+ringBg+' 360deg)'}">
              <div class="score-ring-inner">{{result.score}}</div>
            </div>
            <div style="font-size:15px;font-weight:700;margin-top:6px;" :style="{color:ringColor}">{{scoreLabel}}</div>
            <div style="font-size:12px;color:var(--text-muted);">成交概率评估</div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:12px;">
            <div class="card" style="padding:14px;margin:0;"><div style="font-size:11px;color:var(--text-muted);">采购意图</div><div style="font-weight:700;font-size:15px;">{{result.intent}}</div></div>
            <div class="card" style="padding:14px;margin:0;"><div style="font-size:11px;color:var(--text-muted);">客户阶段</div><div style="font-weight:700;font-size:15px;">{{result.buyerStage}}</div></div>
            <div class="card" style="padding:14px;margin:0;"><div style="font-size:11px;color:var(--text-muted);">需求数量</div><div style="font-weight:700;font-size:15px;">{{result.quantity||'未明确'}}</div></div>
            <div class="card" style="padding:14px;margin:0;"><div style="font-size:11px;color:var(--text-muted);">紧迫程度</div><div style="font-weight:700;font-size:15px;">{{result.urgency}}</div></div>
          </div>
          <div class="card" style="padding:14px;margin-bottom:10px;background:var(--bg-elevated);">
            <div style="font-size:12.5px;font-weight:600;margin-bottom:4px;">评分理由</div>
            <div style="font-size:13.5px;line-height:1.65;color:var(--text-secondary);">{{result.reason}}</div>
          </div>
          <div class="card" style="padding:14px;border-left:3px solid var(--brand);">
            <div style="font-size:12.5px;font-weight:600;margin-bottom:4px;">建议回复策略</div>
            <div style="font-size:13.5px;line-height:1.65;color:var(--text-secondary);white-space:pre-wrap;">{{result.suggestedReply}}</div>
          </div>
        </div>
      </div></div>

      <div class="split-side">
        <div class="card">
          <div class="card-head">评分维度</div>
          <div v-for="d in dims" :key="d.name" style="margin-bottom:12px;">
            <div style="display:flex;justify-content:space-between;margin-bottom:3px;"><span style="font-size:12.5px;font-weight:500;">{{d.name}}</span><span style="font-size:11px;color:var(--text-muted);">{{d.w}}%</span></div>
            <el-progress :percentage="d.w" :color="d.c" :stroke-width="5" :show-text="false"/>
          </div>
        </div>
        <div class="card card-fill" style="overflow:hidden;"><div class="card-head">最近分析</div>
          <div style="overflow-y:auto;flex:1;" v-if="recent.length">
            <div v-for="(r,i) in recent" :key="i" style="padding:8px 0;border-bottom:1px solid var(--border-light);cursor:pointer;" @click="loadRecent(r)">
              <div style="font-weight:600;font-size:12.5px;">{{r.customerName||'匿名'}}</div>
              <div style="display:flex;align-items:center;gap:6px;margin-top:3px;">
                <span :class="['badge',scoreBadge(r.score)]">{{r.score}}分</span>
                <span style="font-size:11px;color:var(--text-muted);">{{r.intent}}</span>
              </div>
            </div>
          </div>
          <div v-else style="text-align:center;padding:24px;color:var(--text-muted);font-size:12px;">暂无记录</div>
        </div>
      </div>
    </div></div>
  </div>
</template>

<script setup>
import {ref,reactive,computed} from 'vue'
import {ElMessage} from 'element-plus'
import {DataAnalysis} from '@element-plus/icons-vue'
import {agentApi} from '../api/index.js'

const form=reactive({customerName:'',customerCountry:'',inquiryText:''})
const loading=ref(false),result=ref(null),recent=ref([])

const dims=[{name:'需求明确度',w:25,c:'#6366f1'},{name:'采购数量',w:20,c:'#8b5cf6'},{name:'客户规模',w:15,c:'#10b981'},{name:'紧迫程度',w:15,c:'#f59e0b'},{name:'市场匹配',w:15,c:'#3b82f6'},{name:'沟通质量',w:10,c:'#f43f5e'}]

const ringColor=computed(()=>{if(!result.value)return'';return result.value.score>=70?'#10b981':result.value.score>=40?'#f59e0b':'#ef4444'})
const ringBg=computed(()=>{if(!result.value)return'';return result.value.score>=70?'#ecfdf5':result.value.score>=40?'#fffbeb':'#fef2f2'})
const scoreLabel=computed(()=>{if(!result.value)return'';const s=result.value.score;return s>=80?'🔥 高价值 · 优先跟进':s>=60?'👍 有潜力 · 值得跟进':s>=40?'🤔 一般 · 常规跟进':'👀 低优先级'})

function scoreBadge(s){return s>=70?'badge-success':s>=40?'badge-warning':'badge-danger'}

function quickFill(){form.customerName='Markus Weber';form.customerCountry='DE';form.inquiryText="Hi,\n\nI'm the purchasing manager at Berlin Retail Solutions GmbH. We are looking for high-quality cardboard floor display stands for our nationwide supermarket promotion campaign.\n\nWe need 8,000 units with 4-color custom printing, delivered by end of July. Can you provide FOB Shenzhen quotation including artwork setup cost?\n\nAlso, we would like to know about your MOQ for custom designs and your typical lead time.\n\nBest regards,\nMarkus Weber";ElMessage.success('已填充示例')}

async function analyze(){
  if(!form.inquiryText.trim()){ElMessage.warning('请输入询盘内容');return}
  loading.value=true;result.value=null
  const demo=genDemo(form.inquiryText)
  try{
    const r=await agentApi.chat({message:'分析B2B询盘，只返回JSON：{"score":0-100,"intent":"分类","buyerStage":"阶段","quantity":"数量","urgency":"高/中/低","reason":"理由(50字)","suggestedReply":"策略(80字)"}\n客户:'+(form.customerName||'未知')+'('+(form.customerCountry||'未知')+')\n内容:\n'+form.inquiryText,enableTools:false})
    try{const m=r.data.message.match(/\{[\s\S]*\}/);const p=m?JSON.parse(m[0]):null;if(p&&p.score!==undefined){result.value=p;addRecent(p);return}}catch(e){}
  }catch(e){}
  result.value=demo;addRecent(demo);ElMessage.info('已生成本地分析')
  loading.value=false
}

function addRecent(d){recent.value.unshift({...d,customerName:form.customerName,customerCountry:form.customerCountry,timestamp:new Date().toISOString()});if(recent.value.length>10)recent.value.pop()}

function genDemo(t){const l=t.toLowerCase();let s=50;if(/\d{3,}/.test(t))s+=15;if(/pcs|pieces|units|quantity|qty/i.test(t))s+=8;if(/quotation|quote|price|FOB|CIF/i.test(t))s+=10;if(/order|purchase|buying/i.test(t))s+=10;if(/urgent|asap|quickly|immediately/i.test(t))s+=5;if(/custom|logo|printing|branding/i.test(t))s+=5;if(/company|GmbH|Ltd|Inc|Corp/i.test(t))s+=5;if(/just looking|browsing|curious/i.test(t))s-=20;if(t.length<30)s-=15;s=Math.max(5,Math.min(98,s));let intent='信息收集';if(/quotation|quote|price/i.test(t))intent='比价询盘';if(/order|purchase|need \d+/i.test(t))intent='采购询盘';if(/urgent|asap/i.test(t))intent='紧急采购';if(t.length<20)intent='无效询盘';let stage=s>=70?'决策阶段':s>=40?'比较阶段':'初筛阶段';let q='未明确';const m=t.match(/(\d[\d,]*)\s*(?:pcs|pieces|units)/i);if(m)q=m[1]+' 件';let urg='中';if(/urgent|asap|quickly|immediately|by \w+ \d+/i.test(t))urg='高';if(t.length<20)urg='低';return{score:s,intent,buyerStage:stage,quantity:q,urgency:urg,reason:s>=70?'需求明确，含具体数量，采购意向强':s>=40?'有一定需求，信息基本完整':'需求不明确，信息量少',suggestedReply:s>=70?'立即回复专业报价，含FOB价格、MOQ、交期、定制选项。建立WhatsApp直接沟通。':s>=40?'发送产品目录和公司简介，询问更多需求细节，保持跟进。':'礼貌回复并发送公司简介，低频率跟进。'}}
function loadRecent(r){result.value=r}
</script>
