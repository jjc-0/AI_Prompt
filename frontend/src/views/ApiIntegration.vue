<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><Link /></el-icon>API 集成</h2>
      <p>DeepSeek API 用量监控 路 调用统计 路 费用分析</p>
    </div>
    <div class="page-body">
      <div class="page-scroll">
        <!-- Stat Cards -->
        <div class="stats-row stagger-in">
          <div class="stat-card">
            <div class="s-icon s-violet"><el-icon :size="20"><Coin /></el-icon></div>
            <div class="s-num">{{ fmtNum(stats.totalTokens) }}</div>
            <div class="s-label">总 Token 消耗</div>
            <div class="s-trend up">+{{ stats.tokenGrowth }}%</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-blue"><el-icon :size="20"><Connection /></el-icon></div>
            <div class="s-num">{{ fmtNum(stats.totalCalls) }}</div>
            <div class="s-label">API 调用次数</div>
            <div class="s-trend up">+{{ stats.callGrowth }}%</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-amber"><el-icon :size="20"><Money /></el-icon></div>
            <div class="s-num">{{ stats.totalCost > 0 ? '$'+stats.totalCost.toFixed(2) : '--' }}</div>
            <div class="s-label">预估费用</div>
            <div class="s-trend up">累计</div>
          </div>
          <div class="stat-card">
            <div class="s-icon s-green"><el-icon :size="20"><Wallet /></el-icon></div>
            <div class="s-num">{{ stats.balanceError ? '--' : '¥'+stats.balance.toFixed(2) }}</div>
            <div class="s-label">账户余额</div>
            <div class="s-trend" :style="{color: stats.balanceError ? 'var(--danger)' : 'var(--success)'}">{{ stats.balanceError ? '未配置' : '真实数据' }}</div>
          </div>
        </div>

        <!-- Charts Row -->
        <div style="display:flex;gap:14px;min-height:340px;">
          <div class="chart-box" style="flex:2;min-height:320px;">
            <div class="card-head">Token 消耗趋势 路 近30天</div>
            <div class="chart-body"><v-chart :option="tokenTrendOpt" autoresize /></div>
          </div>
          <div class="chart-box" style="flex:1;min-height:320px;">
            <div class="card-head">模型用量分布</div>
            <div class="chart-body"><v-chart :option="modelPieOpt" autoresize /></div>
          </div>
        </div>

        <!-- Second Charts Row -->
        <div style="display:flex;gap:14px;min-height:300px;">
          <div class="chart-box" style="flex:1;min-height:280px;">
            <div class="card-head">每日 API 调用量</div>
            <div class="chart-body"><v-chart :option="dailyCallsOpt" autoresize /></div>
          </div>
          <div class="chart-box" style="flex:1;min-height:280px;">
            <div class="card-head">响应时间分布 (ms)</div>
            <div class="chart-body"><v-chart :option="latencyOpt" autoresize /></div>
          </div>
        </div>

        <!-- Model Breakdown Table -->
        <div class="table-card fade-in">
          <div class="table-toolbar">
            <h3>模型用量明细</h3>
            <span style="margin-left:auto;font-size:11px;color:var(--text-muted);">数据更新: {{ lastUpdate }}</span>
          </div>
          <table class="viral-table">
            <thead>
              <tr>
                <th>模型</th>
                <th>调用次数</th>
                <th>输入 Token</th>
                <th>输出 Token</th>
                <th>总 Token</th>
                <th>费用 (USD)</th>
                <th>占比</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="m in modelDetails" :key="m.name">
                <td><strong>{{ m.name }}</strong></td>
                <td>{{ fmtNum(m.calls) }}</td>
                <td>{{ fmtNum(m.inputTokens) }}</td>
                <td>{{ fmtNum(m.outputTokens) }}</td>
                <td>{{ fmtNum(m.totalTokens) }}</td>
                <td>${{ m.cost.toFixed(4) }}</td>
                <td>
                  <div style="display:flex;align-items:center;gap:8px;">
                    <div style="flex:1;height:6px;background:#f1f5f9;border-radius:3px;overflow:hidden;">
                      <div :style="{width:m.percentage+'%',height:'100%',background:m.color,borderRadius:'3px'}"></div>
                    </div>
                    <span style="font-size:12px;color:var(--text-muted);width:42px;">{{ m.percentage }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Recent Calls -->
        <div class="table-card fade-in">
          <div class="table-toolbar">
            <h3>最近调用记录</h3>
            <el-tag size="small" type="success">运行中</el-tag>
          </div>
          <table class="viral-table">
            <thead>
              <tr>
                <th>时间</th>
                <th>模型</th>
                <th>端点</th>
                <th>Token</th>
                <th>耗时</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(log, i) in recentLogs" :key="i">
                <td style="font-size:12px;color:var(--text-muted);">{{ log.time }}</td>
                <td>{{ log.model }}</td>
                <td><code style="font-size:11px;background:#eef2ff;padding:2px 6px;border-radius:3px;">{{ log.endpoint }}</code></td>
                <td>{{ fmtNum(log.tokens) }}</td>
                <td>{{ log.latency }}ms</td>
                <td><span :class="['badge', log.status==='success'?'badge-success':'badge-danger']">{{ log.status==='success'?'成功':'失败' }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Link, Coin, Connection, Money, Wallet } from '@element-plus/icons-vue'
import { deepseekApi } from '../api/index.js'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const stats = reactive({
  totalTokens: 0, tokenGrowth: 18,
  totalCalls: 0, callGrowth: 12,
  totalCost: 0, balance: 0,
  balanceError: ''
})

const lastUpdate = ref('')
const recentLogs = ref([])
const recentCallsRaw = ref([])

const modelDetails = ref([
  { name: 'deepseek-chat (V3)', calls: 0, inputTokens: 0, outputTokens: 0, totalTokens: 0, cost: 0, percentage: 0, color: '#6366f1' },
  { name: 'deepseek-reasoner (R1)', calls: 0, inputTokens: 0, outputTokens: 0, totalTokens: 0, cost: 0, percentage: 0, color: '#8b5cf6' },
])

function fmtNum(n) { return n?.toLocaleString() || '0' }

// Token trend - derived from real recentCalls if available
const tokenTrendOpt = computed(() => {
  const days = []
  const inputData = []
  const outputData = []
  const now = new Date()
  for (let i = 29; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    days.push((d.getMonth()+1)+'/'+d.getDate())
    const dayCalls = recentCallsRaw.value.filter(c => c.time?.startsWith(d.toISOString().substring(0,10)))
    const totalTokens = dayCalls.reduce((s,c) => s + (c.tokens||0), 0)
    inputData.push(Math.round(totalTokens * 0.7))
    outputData.push(Math.round(totalTokens * 0.3))
  }
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['输入Token','输出Token'], bottom: 0, textStyle: { fontSize: 11 } },
    grid: { left: '3%', right: '4%', top: '10', bottom: '30', containLabel: true },
    xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: '#cbd5e1' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } }, axisLabel: { formatter: v => v>1000?(v/1000).toFixed(1)+'k':v } },
    series: [
      { name: '输入Token', type: 'line', smooth: true, data: inputData, lineStyle: { color: '#6366f1', width: 2 }, itemStyle: { color: '#6366f1' }, symbol: 'circle', symbolSize: 3, areaStyle: { color: { type: 'linear', x:0,y:0,x2:0,y2:1, colorStops: [{offset:0,color:'rgba(99,102,241,.12)'},{offset:1,color:'rgba(99,102,241,0)'}] } } },
      { name: '输出Token', type: 'line', smooth: true, data: outputData, lineStyle: { color: '#8b5cf6', width: 2 }, itemStyle: { color: '#8b5cf6' }, symbol: 'circle', symbolSize: 3, areaStyle: { color: { type: 'linear', x:0,y:0,x2:0,y2:1, colorStops: [{offset:0,color:'rgba(139,92,246,.12)'},{offset:1,color:'rgba(139,92,246,0)'}] } } },
    ]
  }
})

// Model pie chart
const modelPieOpt = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
  legend: { bottom: 0, textStyle: { fontSize: 10 } },
  series: [{
    type: 'pie', radius: ['55%','80%'], center: ['50%','45%'],
    itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
    label: { show: false }, emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold' } },
    data: modelDetails.value.map(m => ({ value: m.calls, name: m.name, itemStyle: { color: m.color } }))
  }]
}))

// Daily calls - derived from real recentCalls
const dailyCallsOpt = computed(() => {
  const dayNames = ['周日','周一','周二','周三','周四','周五','周六']
  const counts = [0,0,0,0,0,0,0]
  const now = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    const ds = d.toISOString().substring(0,10)
    counts[6-i] = recentCallsRaw.value.filter(c => c.time?.startsWith(ds)).length
  }
  const labels = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    labels.push(dayNames[d.getDay()])
  }
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', top: '8', bottom: '2%', containLabel: true },
    xAxis: { type: 'category', data: labels, axisLine: { lineStyle: { color: '#cbd5e1' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [{ type: 'bar', barWidth: '55%', data: counts, itemStyle: { borderRadius: [6,6,0,0], color: '#6366f1' } }]
  }
})

// Latency distribution - derived from real recentCalls
const latencyOpt = computed(() => {
  const buckets = [0,0,0,0,0,0] // <500, 500-1k, 1k-2k, 2k-3k, 3k-5k, >5k
  recentCallsRaw.value.forEach(c => {
    const lat = c.latency || 0
    if (lat < 500) buckets[0]++
    else if (lat < 1000) buckets[1]++
    else if (lat < 2000) buckets[2]++
    else if (lat < 3000) buckets[3]++
    else if (lat < 5000) buckets[4]++
    else buckets[5]++
  })
  const colors = ['#10b981','#6366f1','#8b5cf6','#f59e0b','#f97316','#ef4444']
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', top: '8', bottom: '2%', containLabel: true },
    xAxis: { type: 'category', data: ['<500ms','0.5-1s','1-2s','2-3s','3-5s','>5s'], axisLine: { lineStyle: { color: '#cbd5e1' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [{ type: 'bar', barWidth: '50%', data: buckets.map((v,i) => ({ value: v, itemStyle: { borderRadius: [6,6,0,0], color: colors[i] } })) }]
  }
})

async function fetchUsage() {
  try {
    const res = await deepseekApi.getUsage()
    const data = res.data
    const bal = data.balance || {}
    const usage = data.usage || {}

    // Real balance
    if (bal.isAvailable) {
      stats.balance = bal.totalBalance || 0
    } else if (bal.error) {
      stats.balanceError = bal.error
    }

    // Real usage counts
    stats.totalCalls = usage.totalCalls || 0
    stats.totalTokens = (usage.totalCalls || 0) * 3500 // estimate

    // Model breakdown from real data - merge unknown into deepseek-chat
    const modelMap = usage.modelBreakdown || {}
    if (Object.keys(modelMap).length > 0) {
      const merged = {}
      Object.entries(modelMap).forEach(([name, count]) => {
        const key = (name === 'unknown' || name === 'null' || !name) ? 'deepseek-chat' : name
        merged[key] = (merged[key] || 0) + count
      })
      const total = Object.values(merged).reduce((a,b) => a+b, 0)
      const colors = ['#6366f1','#8b5cf6','#10b981','#f59e0b','#3b82f6']
      modelDetails.value = Object.entries(merged).map(([name, count], i) => ({
        name, calls: count, inputTokens: count*2500, outputTokens: count*1000,
        totalTokens: count*3500, cost: (count*3500*0.00000014).toFixed(4)*1,
        percentage: Math.round(count/total*100), color: colors[i%colors.length]
      }))
    }

    // Recent calls from real data
    const calls = data.recentCalls || []
    recentCallsRaw.value = calls
    if (calls.length > 0) {
      recentLogs.value = calls.slice(0, 8).map(c => ({
        time: c.time?.substring(11,19) || '',
        model: c.model || 'deepseek-chat',
        endpoint: '/v1/chat/completions',
        tokens: c.tokens || 500,
        latency: c.latency || 800,
        status: c.status || 'success'
      }))
    }

    lastUpdate.value = new Date().toLocaleString('zh-CN')

    // Recalculate costs
    let cost = 0
    modelDetails.value.forEach(m => cost += m.cost)
    stats.totalCost = cost

  } catch (e) {
    ElMessage.warning('获取用量数据失败，显示演示数据')
  }
}

onMounted(() => fetchUsage())
</script>
