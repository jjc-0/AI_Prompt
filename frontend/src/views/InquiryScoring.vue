<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><DataAnalysis /></el-icon>询盘价值评分</h2>
      <p>AI 智能分析 B2B 询盘 · 识别采购意图 · 评估成交概率 · 提取关键信息</p>
    </div>
    <div class="page-body">
      <div class="page-split">
        <!-- Main area -->
        <div class="split-main">
          <div class="page-scroll">
            <!-- Input card -->
            <div class="card fade-in">
              <div class="card-head">询盘分析</div>
              <div class="form-row" style="margin-bottom:14px;">
                <el-form-item label="客户名称" style="flex:1;">
                  <el-input v-model="form.customerName" placeholder="John Smith 或 ABC Corp" size="large"/>
                </el-form-item>
                <el-form-item label="客户国家" style="flex:1;">
                  <el-select v-model="form.customerCountry" placeholder="选择国家" filterable size="large">
                    <el-option label="美国" value="US"/>
                    <el-option label="英国" value="UK"/>
                    <el-option label="德国" value="DE"/>
                    <el-option label="日本" value="JP"/>
                    <el-option label="澳大利亚" value="AU"/>
                    <el-option label="法国" value="FR"/>
                    <el-option label="加拿大" value="CA"/>
                    <el-option label="巴西" value="BR"/>
                    <el-option label="韩国" value="KR"/>
                    <el-option label="其他" value="OTHER"/>
                  </el-select>
                </el-form-item>
              </div>
              <el-form-item label="询盘内容" style="margin-bottom:14px;">
                <el-input v-model="form.inquiryText" type="textarea" :rows="6" placeholder="粘贴客户询盘内容，如：Hi, I'm interested in your cardboard display stands. We are a retail chain with 15 stores in New York..." size="large"/>
              </el-form-item>
              <div style="display:flex;gap:10px;align-items:center;">
                <el-button type="primary" @click="analyze" :loading="loading" :icon="DataAnalysis" size="large">
                  {{ loading ? 'AI 分析中...' : '智能分析' }}
                </el-button>
                <el-button @click="quickFill" size="large">示例填充</el-button>
                <el-tag v-if="loading" type="warning" size="large">AI 分析中...</el-tag>
              </div>
            </div>

            <!-- Results -->
            <div v-if="result" class="slide-up">
              <!-- Score ring -->
              <div class="card" style="margin-bottom:0;">
                <div class="card-head">分析结果</div>
                <div style="text-align:center;margin-bottom:24px;">
                  <div :class="['score-ring']" :style="{background:'conic-gradient('+ringColor+' 0deg,'+ringColor+' calc('+result.score+'*3.6deg),'+ringBg+' calc('+result.score+'*3.6deg),'+ringBg+' 360deg)'}">
                    <div class="score-ring-inner">{{ result.score }}</div>
                  </div>
                  <div style="font-size:16px;font-weight:700;margin-top:8px;" :style="{color:ringColor}">{{ scoreLabel }}</div>
                  <div style="font-size:12px;color:var(--text-muted);">成交概率评估</div>
                </div>

                <!-- Detail grid -->
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:14px;">
                  <div class="card" style="padding:16px;border:1px solid var(--border);">
                    <div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:.5px;">采购意图</div>
                    <div style="font-weight:700;font-size:16px;margin-top:4px;">{{ result.intent }}</div>
                  </div>
                  <div class="card" style="padding:16px;border:1px solid var(--border);">
                    <div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:.5px;">客户阶段</div>
                    <div style="font-weight:700;font-size:16px;margin-top:4px;">{{ result.buyerStage }}</div>
                  </div>
                  <div class="card" style="padding:16px;border:1px solid var(--border);">
                    <div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:.5px;">需求数量</div>
                    <div style="font-weight:700;font-size:16px;margin-top:4px;">{{ result.quantity || '未明确' }}</div>
                  </div>
                  <div class="card" style="padding:16px;border:1px solid var(--border);">
                    <div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:.5px;">紧迫程度</div>
                    <div style="font-weight:700;font-size:16px;margin-top:4px;">{{ result.urgency }}</div>
                  </div>
                </div>

                <!-- Reason -->
                <div class="card" style="padding:16px;margin-bottom:10px;background:var(--bg-elevated);border:1px solid var(--border);">
                  <div style="font-size:12px;font-weight:600;margin-bottom:6px;color:var(--text);">评分理由</div>
                  <div style="font-size:13.5px;line-height:1.7;color:var(--text-secondary);white-space:pre-wrap;">{{ result.reason }}</div>
                </div>

                <!-- Suggested reply -->
                <div class="card" style="padding:16px;border-left:3px solid var(--brand);background:var(--brand-50);">
                  <div style="font-size:12px;font-weight:600;margin-bottom:6px;color:var(--brand);">建议回复策略</div>
                  <div style="font-size:13.5px;line-height:1.7;color:var(--text-secondary);white-space:pre-wrap;">{{ result.suggestedReply }}</div>
                </div>

                <div style="margin-top:14px;text-align:right;">
                  <el-button @click="copyResult" :icon="CopyDocument">复制结果</el-button>
                  <el-button @click="analyze" :loading="loading">重新分析</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="split-side">
          <div class="card">
            <div class="card-head">评分维度</div>
            <div v-for="d in dims" :key="d.name" style="margin-bottom:14px;">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px;">
                <span style="font-size:12.5px;font-weight:500;">{{ d.name }}</span>
                <span style="font-size:11px;color:var(--text-muted);">{{ d.w }}%</span>
              </div>
              <el-progress :percentage="d.w" :color="d.c" :stroke-width="6" :show-text="false"/>
            </div>
          </div>
          <div class="card card-fill" style="overflow:hidden;">
            <div class="card-head">最近分析</div>
            <div style="overflow-y:auto;flex:1;min-height:0;" v-if="recent.length">
              <div v-for="(r, i) in recent" :key="i" 
                   style="padding:10px 0;border-bottom:1px solid var(--border-light);cursor:pointer;"
                   @click="loadRecent(r)">
                <div style="font-weight:600;font-size:12.5px;">{{ r.customerName || '匿名' }}</div>
                <div style="display:flex;align-items:center;gap:6px;margin-top:4px;">
                  <span :class="['badge', scoreBadge(r.score)]">{{ r.score }}分</span>
                  <span style="font-size:11px;color:var(--text-muted);">{{ r.intent }}</span>
                </div>
              </div>
            </div>
            <div v-else style="text-align:center;padding:40px 16px;color:var(--text-muted);font-size:12px;">
              <el-icon :size="28" style="margin-bottom:8px;opacity:.3;"><DataAnalysis /></el-icon>
              <div>暂无记录</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Refresh, CopyDocument } from '@element-plus/icons-vue'
import { inquiryApi, agentApi } from '../api/index.js'

const form = reactive({ customerName: '', customerCountry: '', inquiryText: '' })
const loading = ref(false)
const result = ref(null)
const recent = ref([])

onMounted(() => loadRecentHistory())

async function loadRecentHistory() {
  try {
    const r = await agentApi.getSessions('inquiry')
    const sessions = r.data.sessions || []
    const items = []
    for (const s of sessions.slice(0, 10)) {
      try {
        const h = await agentApi.getDBHistory(s.sessionId)
        const records = h.data.records || []
        const assistant = records.find(r => r.role === 'assistant')
        if (assistant) {
          items.push({
            sessionId: s.sessionId,
            customerName: s.title || '匿名',
            score: 50,
            intent: s.title || '',
            inquiryText: records.find(r => r.role === 'user')?.content || ''
          })
        }
      } catch (e) { /* skip */ }
    }
    if (items.length) recent.value = items
  } catch (e) { /* ignore */ }
}

const dims = [
  { name: '需求明确度', w: 25, c: '#6366f1' },
  { name: '采购数量', w: 20, c: '#8b5cf6' },
  { name: '客户规模', w: 15, c: '#10b981' },
  { name: '紧迫程度', w: 15, c: '#f59e0b' },
  { name: '市场匹配', w: 15, c: '#3b82f6' },
  { name: '沟通质量', w: 10, c: '#f43f5e' }
]

const ringColor = computed(() => {
  const s = result.value?.score || 0
  if (s >= 80) return '#10b981'
  if (s >= 60) return '#f59e0b'
  if (s >= 40) return '#f97316'
  return '#ef4444'
})
const ringBg = computed(() => {
  const s = result.value?.score || 0
  if (s >= 80) return '#ecfdf5'
  if (s >= 60) return '#fffbeb'
  if (s >= 40) return '#fff7ed'
  return '#fef2f2'
})
const scoreLabel = computed(() => {
  const s = result.value?.score || 0
  if (s >= 80) return '高价值客户'
  if (s >= 60) return '中等价值'
  if (s >= 40) return '低价值'
  return '需进一步沟通'
})
const scoreBadge = s => s >= 80 ? 'badge-success' : s >= 60 ? 'badge-accent' : s >= 40 ? 'badge-warning' : 'badge-danger'

function quickFill() {
  form.customerName = 'John Smith'
  form.customerCountry = 'US'
  form.inquiryText = 'Hi, I am John from New York. We are a mid-size retail chain with 15 stores. We are very interested in your cardboard floor display stands. Can you provide a quote for 500 units? We need them delivered within 4 weeks for our upcoming promotion. Please send details on pricing, MOQ, and custom printing options.'
}

function loadRecent(r) {
  form.customerName = r.customerName || ''
  form.customerCountry = r.customerCountry || ''
  form.inquiryText = r.inquiryText || ''
  result.value = r
}

async function analyze() {
  if (!form.inquiryText) { ElMessage.warning('请输入询盘内容'); return }
  loading.value = true
  try {
    const r = await inquiryApi.score({
      customerName: form.customerName,
      customerCountry: form.customerCountry,
      inquiryText: form.inquiryText
    })
    result.value = r.data
    recent.value.unshift({
      ...r.data,
      customerName: form.customerName,
      customerCountry: form.customerCountry,
      inquiryText: form.inquiryText
    })
    if (recent.value.length > 20) recent.value.pop()
    ElMessage.success('分析完成')
  } catch (e) {
    ElMessage.error('分析失败: ' + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

function copyResult() {
  if (!result.value) return
  const text = `询盘评分: ${result.value.score}分
采购意图: ${result.value.intent}
客户阶段: ${result.value.buyerStage}
需求数量: ${result.value.quantity || '未明确'}
紧迫程度: ${result.value.urgency}
评分理由: ${result.value.reason}
建议回复: ${result.value.suggestedReply}`
  navigator.clipboard.writeText(text).then(() => ElMessage.success('已复制'))
}
</script>
