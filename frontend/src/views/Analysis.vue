<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><TrendCharts /></el-icon>{{ pageTitle }}</h2>
      <p>展示架 POP 产品出口机会分析 · 竞争态势 · RAG 知识增强</p>
    </div>
    <div class="page-body">
      <div class="page-split">
        <div class="split-main">
          <div class="page-scroll">
            <div class="card fade-in">
              <div class="card-head">{{ analysisTab === 'seo' ? 'SEO审计参数' : analysisTab === 'competitor' ? '竞品分析参数' : '分析参数' }}</div>
              <template v-if="analysisTab === 'market'">
                <div class="form-row" style="margin-bottom:14px;">
                  <el-form-item label="商品名称" style="flex:2;">
                  <el-input v-model="form.productName" placeholder="Cardboard Floor Display Stand" size="large"/>
                </el-form-item>
                <el-form-item label="目标市场" style="flex:1;">
                  <el-select v-model="form.targetCountry" size="large">
                    <el-option label="美国" value="US"/>
                    <el-option label="日本" value="JP"/>
                    <el-option label="英国" value="UK"/>
                    <el-option label="德国" value="DE"/>
                    <el-option label="韩国" value="KR"/>
                    <el-option label="法国" value="FR"/>
                    <el-option label="澳大利亚" value="AU"/>
                  </el-select>
                </el-form-item>
              </div>
              <el-form-item label="产品类别" style="margin-bottom:14px;">
                <el-select v-model="form.category" size="large" style="width:100%;">
                  <el-option label="Floor Display Stand 落地展示架" value="floor_display"/>
                  <el-option label="Counter Display 柜台展架" value="counter_display"/>
                  <el-option label="PDQ Tray 快快消托盘" value="pdq_tray"/>
                  <el-option label="POP Dump Bin 散装箱" value="dump_bin"/>
                  <el-option label="Gift Box 礼品盒" value="gift_box"/>
                </el-select>
              </el-form-item>
              </template>

              <template v-if="analysisTab === 'seo'">
                <div class="form-row" style="margin-bottom:14px;">
                  <el-form-item label="目标页面URL" style="flex:2;">
                    <el-input v-model="form.url" placeholder="https://www.displaystandpop.com/products/..." size="large"/>
                  </el-form-item>
                  <el-form-item label="目标市场" style="flex:1;">
                    <el-select v-model="form.targetCountry" size="large">
                      <el-option label="美国" value="US"/><el-option label="日本" value="JP"/><el-option label="英国" value="UK"/><el-option label="德国" value="DE"/><el-option label="韩国" value="KR"/><el-option label="法国" value="FR"/><el-option label="澳大利亚" value="AU"/>
                    </el-select>
                  </el-form-item>
                </div>
                <el-form-item label="核心关键词（逗号分隔）" style="margin-bottom:14px;">
                  <el-input v-model="form.keywords" placeholder="cardboard display stand, pop display, retail display..." size="large"/>
                </el-form-item>
              </template>

              <template v-if="analysisTab === 'competitor'">
                <div class="form-row" style="margin-bottom:14px;">
                  <el-form-item label="竞品URL" style="flex:2;">
                    <el-input v-model="form.competitorUrl" placeholder="https://competitor.com/products/..." size="large"/>
                  </el-form-item>
                  <el-form-item label="目标市场" style="flex:1;">
                    <el-select v-model="form.targetCountry" size="large">
                      <el-option label="美国" value="US"/><el-option label="日本" value="JP"/><el-option label="英国" value="UK"/><el-option label="德国" value="DE"/><el-option label="韩国" value="KR"/><el-option label="法国" value="FR"/><el-option label="澳大利亚" value="AU"/>
                    </el-select>
                  </el-form-item>
                </div>
                <el-form-item label="产品类别" style="margin-bottom:14px;">
                  <el-select v-model="form.category" size="large" style="width:100%;">
                    <el-option label="Floor Display Stand 落地展示架" value="floor_display"/><el-option label="Counter Display 柜台展架" value="counter_display"/><el-option label="PDQ Tray 快快消托盘" value="pdq_tray"/><el-option label="POP Dump Bin 散装箱" value="dump_bin"/><el-option label="Gift Box 礼品盒" value="gift_box"/>
                  </el-select>
                </el-form-item>
              </template>

              <div style="display:flex;gap:12px;align-items:center;">
                <el-button type="primary" @click="doAnalysis" :loading="analyzing" :icon="TrendCharts" size="large">
                  {{ analyzing ? '分析中...' : analysisTab === 'seo' ? 'SEO审计' : analysisTab === 'competitor' ? '竞品分析' : '深度分析' }}'
                </el-button>
                <el-tag v-if="analyzing" type="warning" size="large">AI 分析中...</el-tag>
                <el-tag v-if="currentSessionId" type="success" size="small" style="margin-left:auto;">
                  <el-icon :size="12"><Check /></el-icon>已保存
                </el-tag>
              </div>
            </div>

            <!-- Results -->
            <div v-if="result" class="slide-up">
              <div class="card">
                <div class="card-head">分析报告</div>
                <div class="result-box" v-html="rendered"></div>
                <div style="margin-top:14px;display:flex;gap:10px;align-items:center;">
                  <el-button @click="copyResult" :icon="CopyDocument">复制</el-button>
                  <el-button @click="doAnalysis" :loading="analyzing">重新分析</el-button>
                  <span v-if="currentSessionId" style="margin-left:auto;font-size:12px;color:var(--text-muted);">
                    {{ resultInfo.processingTimeMs || '' }}ms
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- History sidebar -->
        <div class="split-side">
          <div class="card" style="padding:10px;flex-shrink:0;">
            <el-button size="small" @click="loadHistory" :icon="Refresh" style="width:100%;">刷新历史</el-button>
          </div>
          <div class="card card-fill" style="padding:6px;">
            <div class="sess-list">
              <div v-if="historyList.length===0" class="empty-state" style="padding:36px 16px;">
                <el-icon :size="26" style="margin-bottom:8px;opacity:.3;"><TrendCharts /></el-icon>
                <div style="font-size:13px;">暂无历史</div>
              </div>
              <div v-for="h in historyList" :key="h.sessionId"
                   :class="['sess-item', { active: h.sessionId === currentSessionId }]"
                   @click="loadHistoryItem(h)">
                <div class="sess-top">
                  <div class="sess-title" @dblclick.stop="startRename(h)">{{ h.title }}</div>
                  <div class="sess-actions">
                    <el-button size="small" text @click.stop="startRename(h)"><el-icon><Edit /></el-icon></el-button>
                    <el-button size="small" text @click.stop="confirmDelete(h)"><el-icon><Delete /></el-icon></el-button>
                  </div>
                </div>
                <div style="font-size:10.5px;color:var(--text-muted);margin-top:2px;">
                  {{ h.messageCount }} 条 · {{ h.updatedAt?.substring(0, 16) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="renameVisible" title="重命名" width="400px" :close-on-click-modal="false">
      <el-input v-model="renameValue" placeholder="输入新名称" @keyup.enter="doRename" maxlength="60" show-word-limit/>
      <template #footer>
        <el-button @click="renameVisible = false">取消</el-button>
        <el-button type="primary" @click="doRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, TrendCharts, Refresh, Check, CopyDocument } from '@element-plus/icons-vue'
import { analysisApi, agentApi } from '../api/index.js'

const route = useRoute()
const analysisTab = ref('market')

watch(() => route.path, (path) => {
  if (path.includes('/analysis/seo')) analysisTab.value = 'seo'
  else if (path.includes('/analysis/competitor')) analysisTab.value = 'competitor'
  else analysisTab.value = 'market'
}, { immediate: true })

const pageTitle = computed(() => {
  const t = { seo: 'SEO审计与优化', competitor: '竞品分析', market: '市场分析' }
  return t[analysisTab.value] || '市场分析'
})
const pageDesc = computed(() => {
  const d = {
    seo: '展示架产品SEO审计 - 关键词优化 - 排名提升建议',
    competitor: '竞品对比分析 - 优劣势评估 - 差异化策略',
    market: '展示架/POP 产品出口机会分析 - 竞争态势 - RAG 知识增强'
  }
  return d[analysisTab.value] || d.market
})

const form = ref({ productName: '', targetCountry: 'US', category: 'floor_display', keywords: '', url: '', competitorUrl: '' })
const result = ref('')
const analyzing = ref(false)
const currentSessionId = ref(null)
const historyList = ref([])
const renameVisible = ref(false)
const renameValue = ref('')
const renameTarget = ref(null)
const resultInfo = ref({})

const rendered = computed(() => {
  if (!result.value) return ''
  return result.value
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
    .replace(/###\s?(.*)/g, '<h3>$1</h3>')
})

onMounted(() => loadHistory())

async function loadHistory() {
  try {
    const r = await agentApi.getSessions('analysis')
    historyList.value = r.data.sessions || []
  } catch (e) { /* ignore */ }
}

async function loadHistoryItem(h) {
  try {
    const r = await agentApi.getDBHistory(h.sessionId)
    result.value = (r.data.records || [])
      .filter(r => r.role === 'assistant')
      .map(r => r.content)
      .join('\n\n') || ''
    currentSessionId.value = h.sessionId
  } catch (e) { ElMessage.error('加载失败') }
}

function startRename(s) {
  renameTarget.value = s
  renameValue.value = s.title || ''
  renameVisible.value = true
}

async function doRename() {
  const v = renameValue.value.trim()
  if (!v) { ElMessage.warning('名称不能为空'); return }
  try {
    await agentApi.updateTitle(renameTarget.value.sessionId, v)
    renameTarget.value.title = v
    renameVisible.value = false
    ElMessage.success('已重命名')
    loadHistory()
  } catch (e) { ElMessage.error('重命名失败') }
}

async function confirmDelete(s) {
  try {
    await ElMessageBox.confirm('确定删除"' + s.title + '"吗？', '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await agentApi.clearSession(s.sessionId)
    if (currentSessionId.value === s.sessionId) { result.value = ''; currentSessionId.value = null }
    ElMessage.success('已删除')
    loadHistory()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

async function doAnalysis() {
  analyzing.value = true
  result.value = ''
  try {
    let r
    if (analysisTab.value === 'seo') {
      if (!form.value.url) { ElMessage.warning('请输入目标页面URL'); analyzing.value = false; return }
      r = await analysisApi.analyzeSEO({ url: form.value.url, keywords: form.value.keywords, targetCountry: form.value.targetCountry })
    } else if (analysisTab.value === 'competitor') {
      if (!form.value.competitorUrl) { ElMessage.warning('请输入竞品URL'); analyzing.value = false; return }
      r = await analysisApi.analyzeCompetitor({ competitorUrl: form.value.competitorUrl, targetCountry: form.value.targetCountry, category: form.value.category })
    } else {
      if (!form.value.productName) { ElMessage.warning('请输入商品名称'); analyzing.value = false; return }
      r = await analysisApi.analyzeMarket({ productName: form.value.productName, targetCountry: form.value.targetCountry, category: form.value.category })
    }
    result.value = r.data.result
    currentSessionId.value = r.data.sessionId
    resultInfo.value = { processingTimeMs: r.data.processingTimeMs }
    ElMessage.success('分析完成')
    loadHistory()
    setTimeout(() => loadHistory(), 2500)
  } catch (e) {
    ElMessage.error('分析失败: ' + (e.response?.data?.message || e.message))
  } finally {
    analyzing.value = false
  }
}

function copyResult() {
  if (!result.value) return
  navigator.clipboard.writeText(result.value).then(() => ElMessage.success('已复制'))
}
</script>
