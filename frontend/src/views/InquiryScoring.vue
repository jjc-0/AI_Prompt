<template>
  <div>
    <div class="page-header">
      <h2><el-icon :size="22"><DataAnalysis /></el-icon> 询盘价值评分</h2>
      <p>基于 AI 的 B2B 询盘智能分析 · 自动识别采购意图、评估成交概率、提取关键信息</p>
    </div>
    <div class="page-content" style="display: flex; gap: 18px;">
      <!-- Left: Input Area -->
      <div style="flex: 1;">
        <div class="card">
          <div class="card-title">询盘分析</div>
          <div class="form-row">
            <el-form-item label="客户名称" style="flex:1;">
              <el-input v-model="form.customerName" placeholder="如：John Smith 或 ABC Corp" />
            </el-form-item>
            <el-form-item label="客户国家" style="flex:1;">
              <el-select v-model="form.customerCountry" placeholder="选择国家" filterable>
                <el-option label="美国" value="US" />
                <el-option label="英国" value="UK" />
                <el-option label="德国" value="DE" />
                <el-option label="日本" value="JP" />
                <el-option label="澳大利亚" value="AU" />
                <el-option label="法国" value="FR" />
                <el-option label="加拿大" value="CA" />
                <el-option label="巴西" value="BR" />
                <el-option label="印度" value="IN" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="询盘内容" style="flex:1;">
              <el-input
                v-model="form.inquiryText"
                type="textarea"
                :rows="5"
                placeholder="粘贴客户询盘内容，如：

Hi, I'm interested in your cardboard floor display stands. We need 5000pcs for our supermarket chain in Germany. Can you send quotation with FOB price and lead time? We also need custom printing with our logo.

Best regards,
John from Berlin Retail GmbH"
              />
            </el-form-item>
          </div>
          <div style="display: flex; gap: 12px; align-items: center;">
            <el-button type="primary" @click="analyzeInquiry" :loading="analyzing" :icon="DataAnalysis" size="large">
              智能分析询盘
            </el-button>
            <el-button @click="quickFill" size="large">示例填充</el-button>
            <el-tag v-if="analyzing" type="warning" size="large">AI 分析中...</el-tag>
          </div>
        </div>

        <!-- Result -->
        <div v-if="result" class="card slide-up" style="margin-top: 18px;">
          <div class="card-title">分析结果</div>

          <!-- Score Ring -->
          <div style="text-align:center; margin-bottom: 24px;">
            <div :class="['score-ring', scoreLevel]" :style="{'--pct': result.score}">
              <div class="score-ring-inner">
                {{ result.score }}
              </div>
            </div>
            <div style="font-size: 16px; font-weight: 700; margin-top: 8px;" :style="{color: scoreColor}">
              {{ scoreLabel }}
            </div>
            <div style="font-size: 13px; color: var(--text-secondary); margin-top: 4px;">
              成交概率评估
            </div>
          </div>

          <!-- Detail Grid -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div class="card" style="padding: 16px; margin: 0;">
              <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">采购意图</div>
              <div style="font-weight: 700; font-size: 16px;">{{ result.intent }}</div>
            </div>
            <div class="card" style="padding: 16px; margin: 0;">
              <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">客户阶段</div>
              <div style="font-weight: 700; font-size: 16px;">{{ result.buyerStage }}</div>
            </div>
            <div class="card" style="padding: 16px; margin: 0;">
              <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">需求数量</div>
              <div style="font-weight: 700; font-size: 16px;">{{ result.quantity || '未明确' }}</div>
            </div>
            <div class="card" style="padding: 16px; margin: 0;">
              <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">紧迫程度</div>
              <div style="font-weight: 700; font-size: 16px;">{{ result.urgency }}</div>
            </div>
          </div>

          <!-- Reason -->
          <div class="card" style="padding: 18px; margin: 12px 0 0; background: #f8fafc;">
            <div style="font-size: 13px; font-weight: 600; margin-bottom: 6px;">评分理由</div>
            <div style="font-size: 14px; line-height: 1.7; color: var(--text-secondary);">{{ result.reason }}</div>
          </div>

          <!-- Suggested Reply -->
          <div class="card" style="padding: 18px; margin: 12px 0 0; border-left: 4px solid var(--brand-500);">
            <div style="font-size: 13px; font-weight: 600; margin-bottom: 8px;">建议回复策略</div>
            <div style="font-size: 14px; line-height: 1.7; color: var(--text-secondary); white-space: pre-wrap;">{{ result.suggestedReply }}</div>
          </div>
        </div>
      </div>

      <!-- Right: Scoring Criteria -->
      <div style="width: 280px; flex-shrink: 0;">
        <div class="card">
          <div class="card-title">评分维度</div>
          <div v-for="dim in dimensions" :key="dim.name" style="margin-bottom: 14px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
              <span style="font-size: 13px; font-weight: 500;">{{ dim.name }}</span>
              <span style="font-size: 12px; color: var(--text-muted);">{{ dim.weight }}%</span>
            </div>
            <el-progress :percentage="dim.weight" :color="dim.color" :stroke-width="6" :show-text="false" />
          </div>
        </div>

        <div class="card" style="margin-top: 12px;">
          <div class="card-title">最近分析</div>
          <div v-if="recentAnalyses.length === 0" style="text-align:center; padding:20px; color:var(--text-muted); font-size:13px;">
            暂无分析记录
          </div>
          <div v-for="(item, idx) in recentAnalyses" :key="idx"
            style="padding: 10px 0; border-bottom: 1px solid var(--border-light); cursor: pointer;"
            @click="loadRecent(item)">
            <div style="font-weight:600;font-size:13px;">{{ item.customerName || '匿名客户' }}</div>
            <div style="display:flex;align-items:center;gap:8px;margin-top:4px;">
              <span :class="['badge', scoreBadge(item.score)]">{{ item.score }}分</span>
              <span style="font-size:12px;color:var(--text-muted);">{{ item.intent }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis } from '@element-plus/icons-vue'
import { agentApi } from '../api/index.js'

const form = reactive({
  customerName: '',
  customerCountry: '',
  inquiryText: ''
})

const analyzing = ref(false)
const result = ref(null)
const recentAnalyses = ref([])

const dimensions = [
  { name: '需求明确度', weight: 25, color: '#6366f1' },
  { name: '采购数量', weight: 20, color: '#8b5cf6' },
  { name: '客户规模', weight: 15, color: '#10b981' },
  { name: '紧迫程度', weight: 15, color: '#f59e0b' },
  { name: '市场匹配', weight: 15, color: '#3b82f6' },
  { name: '沟通质量', weight: 10, color: '#f43f5e' }
]

const scoreLevel = computed(() => {
  if (!result.value) return ''
  if (result.value.score >= 70) return 'high'
  if (result.value.score >= 40) return 'medium'
  return 'low'
})

const scoreColor = computed(() => {
  if (!result.value) return ''
  if (result.value.score >= 70) return 'var(--success)'
  if (result.value.score >= 40) return 'var(--warning)'
  return 'var(--danger)'
})

const scoreLabel = computed(() => {
  if (!result.value) return ''
  if (result.value.score >= 80) return '🔥 高价值询盘 · 优先跟进'
  if (result.value.score >= 60) return '👍 有潜力 · 值得跟进'
  if (result.value.score >= 40) return '🤔 一般询盘 · 常规跟进'
  return '👀 低优先级 · 选择性回复'
})

function scoreBadge(score) {
  if (score >= 70) return 'badge-success'
  if (score >= 40) return 'badge-warning'
  return 'badge-danger'
}

function quickFill() {
  form.customerName = 'Markus Weber'
  form.customerCountry = 'DE'
  form.inquiryText = `Hi,

I'm the purchasing manager at Berlin Retail Solutions GmbH. We are looking for high-quality cardboard floor display stands for our nationwide supermarket promotion campaign.

We need 8,000 units with 4-color custom printing, delivered by end of July. Can you provide FOB Shenzhen quotation including artwork setup cost?

Also, we would like to know about your MOQ for custom designs and your typical lead time.

Best regards,
Markus Weber
Berlin Retail Solutions GmbH`
  ElMessage.success('已填充示例询盘')
}

async function analyzeInquiry() {
  if (!form.inquiryText.trim()) {
    ElMessage.warning('请输入询盘内容')
    return
  }
  analyzing.value = true
  result.value = null

  try {
    // Use AI agent to analyze inquiry
    const res = await agentApi.chat({
      message: `请分析以下B2B询盘，返回JSON格式结果（只返回JSON，不要其他文字）：
{
  "score": 数字(0-100, 评估成交概率),
  "intent": "采购意图分类(如:紧急采购/比价询盘/信息收集/长期合作/无效询盘)",
  "buyerStage": "客户阶段(如:决策阶段/比较阶段/初筛阶段/信息收集)",
  "quantity": "需求数量(如未明确填'未明确')",
  "urgency": "紧迫程度(高/中/低)",
  "reason": "评分理由(50字以内,说明加分项和减分项)",
  "suggestedReply": "建议回复策略(80字以内)"
}

询盘来源客户: ${form.customerName || '未知'} (${form.customerCountry || '未知国家'})

询盘内容:
${form.inquiryText}`,
      enableTools: false
    })

    const data = res.data
    let parsed
    try {
      // Try to extract JSON from response
      const jsonMatch = data.message.match(/\{[\s\S]*\}/)
      parsed = jsonMatch ? JSON.parse(jsonMatch[0]) : null
    } catch (e) {
      console.error('Failed to parse inquiry analysis', e)
    }

    if (parsed && parsed.score !== undefined) {
      result.value = parsed
      // Save to recent
      recentAnalyses.value.unshift({
        ...parsed,
        customerName: form.customerName,
        customerCountry: form.customerCountry,
        inquiryText: form.inquiryText,
        timestamp: new Date().toISOString()
      })
      if (recentAnalyses.value.length > 10) recentAnalyses.value.pop()
      ElMessage.success('询盘分析完成')
    } else {
      // Fallback: generate demo scoring
      const demoScore = generateDemoScore(form.inquiryText)
      result.value = demoScore
      recentAnalyses.value.unshift({
        ...demoScore,
        customerName: form.customerName,
        customerCountry: form.customerCountry,
        inquiryText: form.inquiryText,
        timestamp: new Date().toISOString()
      })
      if (recentAnalyses.value.length > 10) recentAnalyses.value.pop()
      ElMessage.success('分析完成（演示模式）')
    }
  } catch (e) {
    // Generate demo scoring on error
    const demoScore = generateDemoScore(form.inquiryText)
    result.value = demoScore
    ElMessage.info('已生成本地分析结果')
  } finally {
    analyzing.value = false
  }
}

function generateDemoScore(text) {
  const lower = text.toLowerCase()
  let score = 50

  // Quantity indicators
  if (/\d{3,}/.test(text)) score += 15
  if (/pcs|pieces|units|quantity|qty/i.test(text)) score += 8

  // Intent indicators
  if (/quotation|quote|price|pricing|FOB|CIF/i.test(text)) score += 10
  if (/order|purchase|buying|procurement/i.test(text)) score += 10
  if (/urgent|asap|quickly|soon|immediately/i.test(text)) score += 5

  // Quality indicators
  if (/custom|logo|printing|branding|OEM/i.test(text)) score += 5
  if (/company|corporation|GmbH|Ltd|Inc|Co\./i.test(text)) score += 5

  // Penalties
  if (/just looking|browsing|curious/i.test(text)) score -= 20
  if (text.length < 30) score -= 15

  score = Math.max(5, Math.min(98, score))

  // Determine intent
  let intent = '信息收集'
  if (/quotation|quote|price/i.test(text)) intent = '比价询盘'
  if (/order|purchase|need \d+/i.test(text)) intent = '采购询盘'
  if (/urgent|asap/i.test(text)) intent = '紧急采购'
  if (text.length < 20) intent = '无效询盘'

  // Determine stage
  let buyerStage = '初筛阶段'
  if (score >= 70) buyerStage = '决策阶段'
  else if (score >= 40) buyerStage = '比较阶段'

  // Quantity
  let quantity = '未明确'
  const qtyMatch = text.match(/(\d[\d,]*)\s*(?:pcs|pieces|units)/i)
  if (qtyMatch) quantity = qtyMatch[1] + ' 件'

  // Urgency
  let urgency = '中'
  if (/urgent|asap|quickly|immediately|by \w+ \d+/i.test(text)) urgency = '高'
  if (text.length < 20) urgency = '低'

  // Reason
  let reasonParts = []
  if (score >= 70) reasonParts = ['需求明确', '包含具体数量', '采购意向强烈', '客户信息完整']
  else if (score >= 40) reasonParts = ['有一定需求', '信息基本完整']
  else reasonParts = ['需求不明确', '信息量较少']

  return {
    score,
    intent,
    buyerStage,
    quantity,
    urgency,
    reason: reasonParts.join('；') + (score >= 70 ? '，建议优先跟进' : ''),
    suggestedReply: score >= 70
      ? '立即回复专业报价单，包含FOB价格、MOQ、交期、定制选项。同时询问客户是否需要样品，建立WhatsApp直接沟通。'
      : score >= 40
        ? '发送产品目录和公司简介，询问更多需求细节，保持跟进但不急于报价。'
        : '礼貌回复并发送公司简介，将客户加入定期Newsletter列表，保持低频率跟进。'
  }
}

function loadRecent(item) {
  result.value = item
}
</script>

<style scoped>
.score-ring {
  width: 120px; height: 120px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 36px; font-weight: 800;
  position: relative; margin: 0 auto;
}

.score-ring.high {
  background: conic-gradient(#10b981 0deg, #10b981 calc(var(--pct) * 3.6deg), #ecfdf5 calc(var(--pct) * 3.6deg), #ecfdf5 360deg);
}

.score-ring.medium {
  background: conic-gradient(#f59e0b 0deg, #f59e0b calc(var(--pct) * 3.6deg), #fffbeb calc(var(--pct) * 3.6deg), #fffbeb 360deg);
}

.score-ring.low {
  background: conic-gradient(#ef4444 0deg, #ef4444 calc(var(--pct) * 3.6deg), #fef2f2 calc(var(--pct) * 3.6deg), #fef2f2 360deg);
}

.score-ring-inner {
  width: 96px; height: 96px;
  border-radius: 50%; background: #fff;
  display: flex; align-items: center; justify-content: center;
}
</style>
