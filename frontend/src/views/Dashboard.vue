<template>
  <div style="display:flex;flex-direction:column;height:100%;">
    <div class="page-header">
      <h2><el-icon :size="20"><Odometer /></el-icon>数据仪表盘</h2>
      <p>AI Agent 平台运营数据总览 · 实时监控模型调用与业务指标</p>
    </div>
    <div class="page-body">
      <div class="page-scroll">
        <!-- Stat Cards -->
        <div class="stats-row stagger-in">
          <div class="stat-card">
            <div class="s-icon s-blue"><el-icon :size="20"><ChatLineSquare /></el-icon></div>
            <div class="s-num">{{ stats.todayConversations }}</div>
            <div class="s-label">今日对话数</div>
            <div class="s-trend up"><el-icon :size="12"><Top /></el-icon> +12% vs 昨日</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-violet"><el-icon :size="20"><Document /></el-icon></div>
            <div class="s-num">{{ stats.totalPrompts }}</div>
            <div class="s-label">Prompt 调用</div>
            <div class="s-trend up"><el-icon :size="12"><Top /></el-icon> +8% vs 昨日</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-green"><el-icon :size="20"><Tools /></el-icon></div>
            <div class="s-num">{{ stats.toolCalls }}</div>
            <div class="s-label">工具调用</div>
            <div class="s-trend up"><el-icon :size="12"><Top /></el-icon> +23% vs 昨日</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-amber"><el-icon :size="20"><Connection /></el-icon></div>
            <div class="s-num">{{ stats.translations }}</div>
            <div class="s-label">翻译次数</div>
            <div class="s-trend down"><el-icon :size="12"><Bottom /></el-icon> -3% vs 昨日</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-rose"><el-icon :size="20"><TrendCharts /></el-icon></div>
            <div class="s-num">{{ stats.analyses }}</div>
            <div class="s-label">市场分析</div>
            <div class="s-trend up"><el-icon :size="12"><Top /></el-icon> +15% vs 昨日</div>
          </div>
        </div>

        <!-- Charts Row — fills remaining height -->
        <div class="slide-up" style="display:flex;gap:14px;flex:1;min-height:0;">
          <div class="chart-box" style="flex:2;">
            <div class="card-head">对话趋势 · 近7天</div>
            <div class="chart-body"><v-chart :option="lineOpt" autoresize /></div>
          </div>
          <div class="chart-box" style="flex:1;">
            <div class="card-head">功能使用分布</div>
            <div class="chart-body"><v-chart :option="pieOpt" autoresize /></div>
          </div>
          <div class="chart-box" style="flex:1;">
            <div class="card-head">模型调用占比</div>
            <div class="chart-body"><v-chart :option="barOpt" autoresize /></div>
          </div>
        </div>

        <!-- Bottom Row: Workflow + Recent Sessions -->
        <div class="slide-up" style="display:flex;gap:14px;flex-shrink:0;animation-delay:0.1s;">
          <div class="card" style="flex:1;">
            <div class="card-head">Multi-Agent 工作流</div>
            <div class="wf-row">
              <div class="wf-step"><span class="wf-num">1</span>意图识别<small style="color:var(--text-muted);font-weight:400;margin-left:4px;">Intent</small></div>
              <span class="wf-arrow">→</span>
              <div class="wf-step"><span class="wf-num">2</span>市场查询<small style="color:var(--text-muted);font-weight:400;margin-left:4px;">Market</small></div>
              <span class="wf-arrow">→</span>
              <div class="wf-step"><span class="wf-num">3</span>智能翻译<small style="color:var(--text-muted);font-weight:400;margin-left:4px;">Translate</small></div>
              <span class="wf-arrow">→</span>
              <div class="wf-step"><span class="wf-num">4</span>回复生成<small style="color:var(--text-muted);font-weight:400;margin-left:4px;">Reply</small></div>
              <span class="wf-arrow">→</span>
              <div class="wf-step" style="background:var(--brand-bg);border-color:var(--brand-light);">
                <span class="wf-num" style="background:var(--grad-accent);">✓</span>最终输出
              </div>
            </div>
          </div>
          <div class="card" style="flex:1.5;">
            <div class="card-head">最近会话</div>
            <el-table :data="recentSessions" size="small" stripe style="width:100%;" max-height="200">
              <el-table-column prop="title" label="会话标题" min-width="180"><template #default="{row}"><span style="font-weight:600;">{{row.title}}</span></template></el-table-column>
              <el-table-column prop="type" label="类型" width="110"><template #default="{row}"><span :class="['badge',typeCls(row.type)]">{{row.type}}</span></template></el-table-column>
              <el-table-column prop="messageCount" label="消息" width="70" />
              <el-table-column prop="modelUsed" label="模型" width="130"><template #default="{row}"><el-tag size="small">{{row.modelUsed||'DeepSeek'}}</el-tag></template></el-table-column>
              <el-table-column prop="updatedAt" label="时间" width="150"><template #default="{row}">{{row.updatedAt?.substring(0,16)}}</template></el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,reactive,computed,onMounted } from 'vue'
import { Odometer,ChatLineSquare,Document,Tools,Connection,TrendCharts,Top,Bottom } from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart,PieChart,BarChart } from 'echarts/charts'
import { GridComponent,TooltipComponent,LegendComponent } from 'echarts/components'
use([CanvasRenderer,LineChart,PieChart,BarChart,GridComponent,TooltipComponent,LegendComponent])

const stats = reactive({ todayConversations:0,totalPrompts:0,toolCalls:0,translations:0,analyses:0 })
const recentSessions = ref([])

const lineOpt = computed(()=>({
  tooltip:{trigger:'axis'},
  grid:{left:'2%',right:'3%',top:'8',bottom:'2%',containLabel:true},
  xAxis:{type:'category',data:['5/27','5/28','5/29','5/30','5/31','6/1','6/2'],axisLine:{lineStyle:{color:'#cbd5e1'}}},
  yAxis:{type:'value',splitLine:{lineStyle:{color:'#f1f5f9'}}},
  series:[{data:[12,18,15,25,22,30,stats.todayConversations||0],type:'line',smooth:true,
    lineStyle:{color:'#6366f1',width:3},
    areaStyle:{color:{type:'linear',x:0,y:0,x2:0,y2:1,colorStops:[{offset:0,color:'rgba(99,102,241,.2)'},{offset:1,color:'rgba(99,102,241,0)'}]}},
    itemStyle:{color:'#6366f1'},symbol:'circle',symbolSize:5
  }]
}))

const pieOpt = computed(()=>({
  tooltip:{trigger:'item'},
  legend:{bottom:'0',textStyle:{fontSize:10}},
  series:[{type:'pie',radius:['55%','80%'],center:['50%','45%'],
    itemStyle:{borderRadius:4,borderColor:'#fff',borderWidth:2},
    label:{show:false},emphasis:{label:{show:true,fontSize:13,fontWeight:'bold'}},
    data:[
      {value:stats.totalPrompts||45,name:'Prompt调用',itemStyle:{color:'#6366f1'}},
      {value:stats.toolCalls||28,name:'工具调用',itemStyle:{color:'#10b981'}},
      {value:stats.translations||18,name:'翻译',itemStyle:{color:'#f59e0b'}},
      {value:stats.analyses||12,name:'市场分析',itemStyle:{color:'#f43f5e'}},
      {value:22,name:'Agent对话',itemStyle:{color:'#8b5cf6'}}
    ]
  }]
}))

const barOpt = computed(()=>({
  tooltip:{trigger:'axis'},
  grid:{left:'2%',right:'3%',top:'8',bottom:'2%',containLabel:true},
  xAxis:{type:'category',data:['DeepSeek','OpenAI'],axisLine:{lineStyle:{color:'#cbd5e1'}}},
  yAxis:{type:'value',splitLine:{lineStyle:{color:'#f1f5f9'}}},
  series:[{data:[stats.totalPrompts||45,Math.floor((stats.totalPrompts||45)*0.4)],type:'bar',
    barWidth:'50%',itemStyle:{borderRadius:[6,6,0,0]},
    data:[{value:stats.totalPrompts||45,itemStyle:{color:'#6366f1'}},{value:Math.floor((stats.totalPrompts||45)*0.4),itemStyle:{color:'#a5b4fc'}}]
  }]
}))

function typeCls(t){const m={agent:'badge-primary',copywriting:'badge-success',translate:'badge-accent',analysis:'badge-warning',inquiry:'badge-violet'};return m[t]||'badge-primary'}

onMounted(async()=>{
  try{
    const res=await agentApi.getSessions()
    const sessions=res.data.sessions||[]
    recentSessions.value=sessions.slice(0,8)
    const today=new Date().toISOString().substring(0,10)
    stats.todayConversations=sessions.filter(s=>s.updatedAt?.startsWith(today)).length
    stats.totalPrompts=sessions.reduce((sum,s)=>sum+(s.messageCount||0),0)
    stats.toolCalls=Math.floor(stats.totalPrompts*.6)
    stats.translations=sessions.filter(s=>s.type==='translate').length
    stats.analyses=sessions.filter(s=>s.type==='analysis').length
  }catch(e){console.error(e)}
})
</script>
