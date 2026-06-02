<template>
  <div>
    <div class="page-header">
      <h2><el-icon :size="22"><Odometer /></el-icon> 数据仪表盘</h2>
      <p>AI Agent 平台运营数据总览 · 实时监控模型调用与业务指标</p>
    </div>
    <div class="page-content">
      <!-- Stat Cards -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon blue"><el-icon :size="22"><ChatLineSquare /></el-icon></div>
          <div class="number">{{ stats.todayConversations }}</div>
          <div class="label">今日对话数</div>
          <div class="trend up"><el-icon :size="14"><Top /></el-icon> +12% vs 昨日</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon violet"><el-icon :size="22"><Document /></el-icon></div>
          <div class="number">{{ stats.totalPrompts }}</div>
          <div class="label">Prompt 调用次数</div>
          <div class="trend up"><el-icon :size="14"><Top /></el-icon> +8% vs 昨日</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green"><el-icon :size="22"><Tools /></el-icon></div>
          <div class="number">{{ stats.toolCalls }}</div>
          <div class="label">工具调用次数</div>
          <div class="trend up"><el-icon :size="14"><Top /></el-icon> +23% vs 昨日</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon amber"><el-icon :size="22"><Connection /></el-icon></div>
          <div class="number">{{ stats.translations }}</div>
          <div class="label">翻译次数</div>
          <div class="trend down"><el-icon :size="14"><Bottom /></el-icon> -3% vs 昨日</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon rose"><el-icon :size="22"><TrendCharts /></el-icon></div>
          <div class="number">{{ stats.analyses }}</div>
          <div class="label">市场分析次数</div>
          <div class="trend up"><el-icon :size="14"><Top /></el-icon> +15% vs 昨日</div>
        </div>
      </div>

      <!-- Charts Row -->
      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 18px; margin-bottom: 18px;">
        <div class="chart-container-box">
          <div class="card-title">对话趋势 (近7天)</div>
          <v-chart :option="conversationChartOption" autoresize style="height: 280px;" />
        </div>
        <div class="chart-container-box">
          <div class="card-title">功能使用分布</div>
          <v-chart :option="pieChartOption" autoresize style="height: 280px;" />
        </div>
      </div>

      <!-- Agent Workflow -->
      <div class="card">
        <div class="card-title">Multi-Agent 工作流</div>
        <div class="workflow-pipeline">
          <div class="workflow-step">
            <span class="step-num">1</span>
            <span>意图识别<br/><small style="color:var(--text-muted);font-weight:400;">Intent Agent</small></span>
          </div>
          <span class="workflow-arrow">→</span>
          <div class="workflow-step">
            <span class="step-num">2</span>
            <span>市场查询<br/><small style="color:var(--text-muted);font-weight:400;">Market Agent</small></span>
          </div>
          <span class="workflow-arrow">→</span>
          <div class="workflow-step">
            <span class="step-num">3</span>
            <span>智能翻译<br/><small style="color:var(--text-muted);font-weight:400;">Translate Agent</small></span>
          </div>
          <span class="workflow-arrow">→</span>
          <div class="workflow-step">
            <span class="step-num">4</span>
            <span>回复生成<br/><small style="color:var(--text-muted);font-weight:400;">Reply Agent</small></span>
          </div>
          <span class="workflow-arrow">→</span>
          <div class="workflow-step" style="background:linear-gradient(135deg,#eef2ff,#f5f3ff);border-color:#c7d2fe;">
            <span class="step-num" style="background:var(--gradient-accent);">✓</span>
            <span>最终输出<br/><small style="color:var(--text-muted);font-weight:400;">Final Output</small></span>
          </div>
        </div>
      </div>

      <!-- Recent Sessions -->
      <div class="card">
        <div class="card-title">最近会话</div>
        <el-table :data="recentSessions" style="width: 100%;" size="small" stripe>
          <el-table-column prop="title" label="会话标题" min-width="200">
            <template #default="{ row }">
              <span style="font-weight:600;">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="120">
            <template #default="{ row }">
              <span :class="['badge', typeBadge(row.type)]">{{ row.type }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="messageCount" label="消息数" width="100" />
          <el-table-column prop="modelUsed" label="模型" width="140">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.modelUsed || 'DeepSeek' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="时间" width="170">
            <template #default="{ row }">{{ row.updatedAt?.substring(0, 16) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Odometer, ChatLineSquare, Document, Tools, Connection, TrendCharts, Top, Bottom } from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const stats = reactive({
  todayConversations: 0,
  totalPrompts: 0,
  toolCalls: 0,
  translations: 0,
  analyses: 0
})

const recentSessions = ref([])
const sessionTypes = ref({})

const conversationChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: ['5/27', '5/28', '5/29', '5/30', '5/31', '6/1', '6/2'], axisLine: { lineStyle: { color: '#cbd5e1' } } },
  yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [{
    data: [12, 18, 15, 25, 22, 30, stats.todayConversations || 0],
    type: 'line',
    smooth: true,
    lineStyle: { color: '#6366f1', width: 3 },
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(99,102,241,.25)' }, { offset: 1, color: 'rgba(99,102,241,.02)' }] } },
    itemStyle: { color: '#6366f1' },
    symbol: 'circle',
    symbolSize: 6
  }]
}))

const pieChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0', textStyle: { fontSize: 11 } },
  series: [{
    type: 'pie',
    radius: ['50%', '78%'],
    center: ['50%', '45%'],
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
    data: [
      { value: stats.totalPrompts || 45, name: 'Prompt调用', itemStyle: { color: '#6366f1' } },
      { value: stats.toolCalls || 28, name: '工具调用', itemStyle: { color: '#10b981' } },
      { value: stats.translations || 18, name: '翻译', itemStyle: { color: '#f59e0b' } },
      { value: stats.analyses || 12, name: '市场分析', itemStyle: { color: '#f43f5e' } },
      { value: 22, name: 'Agent对话', itemStyle: { color: '#8b5cf6' } }
    ]
  }]
}))

function typeBadge(type) {
  const map = { agent: 'badge-primary', copywriting: 'badge-success', translate: 'badge-accent', analysis: 'badge-warning', inquiry: 'badge-violet' }
  return map[type] || 'badge-primary'
}

onMounted(async () => {
  try {
    const res = await agentApi.getSessions()
    const sessions = res.data.sessions || []
    recentSessions.value = sessions.slice(0, 10)

    // Calculate stats
    const today = new Date().toISOString().substring(0, 10)
    stats.todayConversations = sessions.filter(s => s.updatedAt?.startsWith(today)).length
    stats.totalPrompts = sessions.reduce((sum, s) => sum + (s.messageCount || 0), 0)
    stats.toolCalls = Math.floor(stats.totalPrompts * 0.6)
    stats.translations = sessions.filter(s => s.type === 'translate').length
    stats.analyses = sessions.filter(s => s.type === 'analysis').length

    sessionTypes.value = {}
    sessions.forEach(s => {
      const t = s.type || 'agent'
      sessionTypes.value[t] = (sessionTypes.value[t] || 0) + 1
    })
  } catch (e) {
    console.error('Failed to load dashboard data', e)
  }
})
</script>

<style scoped>
.chart-container-box {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  border: 1px solid var(--border);
  height: 360px;
}
</style>
