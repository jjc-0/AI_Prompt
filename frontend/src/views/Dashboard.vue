<template>
  <div style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><HomeFilled /></el-icon>数据仪表盘</h2>
      <p>AI Agent 平台运营数据总览 · 实时监控模型调用与业务指标</p>
      <span style="margin-left:auto;font-size:12px;color:var(--text-muted);">{{ today }}</span>
    </div>
    <div class="page-body">
      <div class="page-scroll">
        <!-- Stat Cards -->
        <div class="stats-row stagger-in">
          <div class="stat-card">
            <div class="s-icon s-violet"><el-icon :size="20"><ChatDotRound /></el-icon></div>
            <div class="s-num">{{ stats.todayConversations }}</div>
            <div class="s-label">今日对话</div>
            <div class="s-trend up">+12%</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-blue"><el-icon :size="20"><TrendCharts /></el-icon></div>
            <div class="s-num">{{ stats.totalPrompts }}</div>
            <div class="s-label">Prompt 调用</div>
            <div class="s-trend up">+8%</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-green"><el-icon :size="20"><Tools /></el-icon></div>
            <div class="s-num">{{ stats.toolCalls }}</div>
            <div class="s-label">工具调用</div>
            <div class="s-trend up">+23%</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-amber"><el-icon :size="20"><DataAnalysis /></el-icon></div>
            <div class="s-num">{{ stats.analyses }}</div>
            <div class="s-label">市场分析</div>
            <div class="s-trend up">+15%</div>
          </div>
        </div>

        <!-- 介绍文字 -->
        <div class="card fade-in">
          <div class="card-head">平台概览</div>
          <p style="font-size:13px;color:var(--text-secondary);line-height:1.7;">
            JC Display AI Agent 是一款面向 <strong>B2B 展示架出口业务</strong> 的智能运营平台。
            基于 <strong>1,435 款官网真实产品数据</strong>，提供 AI 对话、询盘评分、文案生成、
            多语言翻译、市场分析等一站式 AI 工具链，助力跨境电商高效增长。
          </p>
        </div>

        <!-- 图表 -->
        <div class="slide-up" style="display:flex;gap:14px;min-height:320px;">
          <div class="chart-box" style="flex:2;min-height:280px;">
            <div class="card-head">对话趋势 · 近7天</div>
            <div class="chart-body"><v-chart :option="lineOpt" autoresize /></div>
          </div>
          <div class="chart-box" style="flex:1;min-height:280px;">
            <div class="card-head">功能使用分布</div>
            <div class="chart-body"><v-chart :option="pieOpt" autoresize /></div>
          </div>
        </div>

        <!-- Viral Index 表格 -->
        <div class="table-card fade-in">
          <div class="table-toolbar">
            <h3>Viral Index · 产品热度排行</h3>
            <span style="font-size:11px;color:var(--text-muted);margin-left:8px;">{{ viralData.length }} 款产品</span>
            <span style="margin-left:auto;font-size:11px;color:var(--text-muted);">点击表头可排序</span>
          </div>
          <table class="viral-table">
            <thead>
              <tr>
                <th class="col-idx">#</th>
                <th>商品</th>
                <th @click="sortBy('platform')" :class="{ sorted: sortKey === 'platform' }">
                  平台 <span class="sort-icon">{{ sortKey==='platform' ? (sortAsc ? '▲' : '▼') : '⇅' }}</span>
                </th>
                <th @click="sortBy('viralIndex')" :class="{ sorted: sortKey === 'viralIndex' }">
                  Viral Index <span class="sort-icon">{{ sortKey==='viralIndex' ? (sortAsc ? '▲' : '▼') : '⇅' }}</span>
                </th>
                <th @click="sortBy('change')" :class="{ sorted: sortKey === 'change' }">
                  24h 动能 <span class="sort-icon">{{ sortKey==='change' ? (sortAsc ? '▲' : '▼') : '⇅' }}</span>
                </th>
                <th>来源</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, idx) in sortedViral" :key="item.id">
                <td class="col-idx">{{ idx + 1 }}</td>
                <td>
                  <div class="viral-product">
                    <div class="viral-product-icon">
                      <el-icon :size="16"><Box /></el-icon>
                    </div>
                    <div class="viral-product-info">
                      <span class="viral-product-name">{{ item.name }}</span>
                      <span class="viral-product-sku">SKU: {{ item.sku }}</span>
                    </div>
                  </div>
                </td>
                <td>
                  <span :class="['viral-platform-tag', item.platform]">{{ item.platformLabel }}</span>
                </td>
                <td class="viral-index">{{ item.viralIndex }}</td>
                <td>
                  <span :class="['viral-change', item.change >= 0 ? 'up' : 'down']">
                    {{ item.change >= 0 ? '▲' : '▼' }} {{ Math.abs(item.change) }}%
                  </span>
                </td>
                <td>
                  <div class="viral-source">
                    <span class="viral-source-flag">{{ item.sourceFlag }}</span>
                    <span style="font-size:12px;color:var(--text-secondary);">{{ item.source }}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 最近会话 -->
        <div class="card fade-in" style="flex-shrink:0;">
          <div class="card-head">最近会话</div>
          <el-table :data="recentSessions" size="small" stripe max-height="200">
            <el-table-column prop="title" label="会话标题" min-width="200">
              <template #default="{row}"><span style="font-weight:600;">{{ row.title }}</span></template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="110">
              <template #default="{row}"><span :class="['badge', typeCls(row.type)]">{{ row.type }}</span></template>
            </el-table-column>
            <el-table-column prop="messageCount" label="消息" width="70" />
            <el-table-column prop="modelUsed" label="模型" width="130">
              <template #default="{row}"><el-tag size="small">{{ row.modelUsed || 'DeepSeek' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="时间" width="150">
              <template #default="{row}">{{ row.updatedAt?.substring(0, 16) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { HomeFilled, ChatDotRound, TrendCharts, Tools, DataAnalysis, Box } from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const stats = reactive({ todayConversations: 0, totalPrompts: 0, toolCalls: 0, analyses: 0 })
const recentSessions = ref([])
const sortKey = ref('viralIndex')
const sortAsc = ref(false)
const today = new Date().toISOString().substring(0, 10)

// Viral Index 数据
const viralData = ref([
  { id: 1, name: '瓦楞纸落地展示架', sku: 'CDFS-001', platform: 'alibaba', platformLabel: 'Alibaba', viralIndex: 98.5, change: 12.3, source: '美国', sourceFlag: '🇺🇸' },
  { id: 2, name: '柜台化妆品展盒', sku: 'CDC-042', platform: 'amazon', platformLabel: 'Amazon', viralIndex: 94.2, change: 8.7, source: '德国', sourceFlag: '🇩🇪' },
  { id: 3, name: '纸质礼品盒套装', sku: 'PGB-128', platform: 'shopify', platformLabel: 'Shopify', viralIndex: 91.8, change: 15.1, source: '英国', sourceFlag: '🇬🇧' },
  { id: 4, name: '亚克力展示架', sku: 'ADS-056', platform: 'alibaba', platformLabel: 'Alibaba', viralIndex: 88.3, change: -2.4, source: '日本', sourceFlag: '🇯🇵' },
  { id: 5, name: '瓦楞托盘陈列架', sku: 'CPD-210', platform: 'amazon', platformLabel: 'Amazon', viralIndex: 86.7, change: 5.6, source: '法国', sourceFlag: '🇫🇷' },
  { id: 6, name: '瓦楞散装箱', sku: 'CDB-087', platform: 'alibaba', platformLabel: 'Alibaba', viralIndex: 83.1, change: 11.0, source: '美国', sourceFlag: '🇺🇸' },
  { id: 7, name: '广告横幅展架', sku: 'AB-033', platform: 'shopify', platformLabel: 'Shopify', viralIndex: 79.4, change: -1.8, source: '韩国', sourceFlag: '🇰🇷' },
  { id: 8, name: '纸质拼图套装', sku: 'PP-015', platform: 'amazon', platformLabel: 'Amazon', viralIndex: 76.2, change: 18.5, source: '澳大利亚', sourceFlag: '🇦🇺' },
])

const sortedViral = computed(() => {
  const data = [...viralData.value]
  data.sort((a, b) => {
    const av = a[sortKey.value], bv = b[sortKey.value]
    if (typeof av === 'string') return sortAsc.value ? av.localeCompare(bv) : bv.localeCompare(av)
    return sortAsc.value ? av - bv : bv - av
  })
  return data
})

function sortBy(key) {
  if (sortKey.value === key) { sortAsc.value = !sortAsc.value }
  else { sortKey.value = key; sortAsc.value = false }
}

const lineOpt = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '2%', right: '3%', top: '8', bottom: '2%', containLabel: true },
  xAxis: { type: 'category', data: ['5/27', '5/28', '5/29', '5/30', '5/31', '6/1', '6/2'], axisLine: { lineStyle: { color: '#cbd5e1' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [{
    data: [12, 18, 15, 25, 22, 30, stats.todayConversations || 0], type: 'line', smooth: true,
    lineStyle: { color: '#6366f1', width: 3 },
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(99,102,241,.15)' }, { offset: 1, color: 'rgba(99,102,241,0)' }] } },
    itemStyle: { color: '#6366f1' }, symbol: 'circle', symbolSize: 5
  }]
}))

const pieOpt = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0', textStyle: { fontSize: 10 } },
  series: [{
    type: 'pie', radius: ['55%', '80%'], center: ['50%', '45%'],
    itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
    label: { show: false }, emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } },
    data: [
      { value: stats.totalPrompts || 45, name: 'Prompt调用', itemStyle: { color: '#6366f1' } },
      { value: stats.toolCalls || 28, name: '工具调用', itemStyle: { color: '#10b981' } },
      { value: stats.analyses || 12, name: '市场分析', itemStyle: { color: '#f59e0b' } },
      { value: 22, name: 'Agent对话', itemStyle: { color: '#8b5cf6' } },
    ]
  }]
}))

function typeCls(t) {
  const m = { agent: 'badge-primary', copywriting: 'badge-success', translate: 'badge-accent', analysis: 'badge-warning', inquiry: 'badge-violet' }
  return m[t] || 'badge-primary'
}

onMounted(async () => {
  try {
    const res = await agentApi.getSessions()
    const sessions = res.data.sessions || []
    recentSessions.value = sessions.slice(0, 8)
    const todayStr = new Date().toISOString().substring(0, 10)
    stats.todayConversations = sessions.filter(s => s.updatedAt?.startsWith(todayStr)).length
    stats.totalPrompts = sessions.reduce((sum, s) => sum + (s.messageCount || 0), 0)
    stats.toolCalls = Math.floor(stats.totalPrompts * .6)
    stats.analyses = sessions.filter(s => s.type === 'analysis').length
  } catch (e) { console.error(e) }
})
</script>
