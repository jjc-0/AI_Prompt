<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><Picture /></el-icon>AI 智能识图</h2>
      <p>基于 GPT-4o Vision · 产品图片分析 · 竞品图片解读 · 多语言描述</p>
    </div>
    <div class="page-body">
      <div class="page-scroll">
        <!-- 上传区域 -->
        <div class="card fade-in">
          <div class="card-head">上传图片</div>
          <div style="display:flex;gap:16px;flex-wrap:wrap;">
            <!-- 左侧上传 -->
            <div style="flex:1;min-width:300px;">
              <el-upload
                class="img-upload"
                drag
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleFileChange"
                accept="image/jpeg,image/png,image/gif,image/webp"
              >
                <div v-if="!previewUrl" class="upload-placeholder">
                  <el-icon :size="48" style="color:var(--brand-400);"><UploadFilled /></el-icon>
                  <p style="margin-top:16px;font-size:14px;color:var(--text-secondary);">
                    拖拽图片到此处，或<em style="color:var(--brand);font-style:normal;">点击上传</em>
                  </p>
                  <p style="font-size:12px;color:var(--text-muted);margin-top:6px;">
                    支持 JPG / PNG / GIF / WebP，最大 10MB
                  </p>
                </div>
                <img v-else :src="previewUrl" class="preview-img" />
              </el-upload>
              <div v-if="previewUrl" style="display:flex;gap:8px;margin-top:10px;">
                <span style="font-size:12px;color:var(--text-muted);flex:1;">
                  {{ fileName }} ({{ fileSizeKB }}KB)
                </span>
                <el-button size="small" @click="clearImage" :icon="Delete">清除</el-button>
              </div>
            </div>

            <!-- 右侧提示词 -->
            <div style="flex:1;min-width:300px;display:flex;flex-direction:column;gap:12px;">
              <div>
                <div style="font-size:12px;color:var(--text-muted);margin-bottom:6px;">分析提示词</div>
                <el-input
                  v-model="prompt"
                  type="textarea"
                  :rows="4"
                  placeholder="请描述这张图片..."
                />
              </div>
              <div style="display:flex;gap:8px;flex-wrap:wrap;">
                <el-tag
                  v-for="p in quickPrompts" :key="p.label"
                  size="small"
                  class="quick-prompt-tag"
                  @click="prompt = p.text"
                >{{ p.label }}</el-tag>
              </div>
              <el-button
                type="primary"
                size="large"
                @click="doRecognize"
                :loading="recognizing"
                :disabled="!fileRaw"
                :icon="Picture"
                style="align-self:flex-start;"
              >
                {{ recognizing ? 'AI 识别中...' : '开始识别' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 结果 -->
        <div v-if="result" class="card slide-up">
          <div class="card-head">识别结果</div>
          <div class="result-box" v-html="rendered"></div>
          <div style="margin-top:14px;display:flex;gap:10px;align-items:center;">
            <el-button @click="copyResult" :icon="CopyDocument">复制</el-button>
            <el-button @click="doRecognize" :loading="recognizing">重新分析</el-button>
          </div>
        </div>

        <!-- 错误 -->
        <div v-if="error" class="card" style="border-color:var(--danger);background:var(--danger-bg);">
          <div style="display:flex;align-items:center;gap:8px;color:var(--danger);font-weight:600;">
            <el-icon :size="18"><WarningFilled /></el-icon>
            识别失败
          </div>
          <p style="margin-top:8px;font-size:13px;color:var(--text-secondary);">{{ error }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, UploadFilled, Delete, CopyDocument, WarningFilled } from '@element-plus/icons-vue'
import { imageApi } from '../api/index.js'

const fileRaw = ref(null)
const previewUrl = ref('')
const fileName = ref('')
const fileSizeKB = ref(0)
const prompt = ref('请详细描述这张图片的内容，包括产品特征、颜色、材质、用途等')
const recognizing = ref(false)
const result = ref('')
const error = ref('')

const quickPrompts = [
  { label: '产品分析', text: '请详细描述这张图片中的展示架产品，包括材质、结构、颜色、印刷工艺和适用场景' },
  { label: '竞品对比', text: '分析这张图片中的产品外观设计，指出其设计亮点和可能的市场竞争力' },
  { label: '英文描述', text: 'Describe this product image in English for an e-commerce listing, including features, materials, and benefits' },
  { label: '翻译文本', text: '识别图片中的所有文字内容，并以中文输出' },
  { label: '生成卖点', text: '基于图片内容，提炼出5条B2B产品卖点，用于阿里巴巴详情页' },
  { label: '结构拆解', text: '解析图片中展示架的结构组成，列出各部分功能和材质' },
]

const rendered = computed(() => {
  if (!result.value) return ''
  return result.value
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
    .replace(/###\s?(.*)/g, '<h3>$1</h3>')
    .replace(/##\s?(.*)/g, '<h2>$1</h2>')
})

function handleFileChange(uploadFile) {
  const file = uploadFile.raw
  if (!file) return

  // 类型校验
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) {
    ElMessage.warning('仅支持 JPG / PNG / GIF / WebP 格式')
    return
  }
  // 大小校验
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return
  }

  fileRaw.value = file
  fileName.value = file.name
  fileSizeKB.value = Math.round(file.size / 1024)
  previewUrl.value = URL.createObjectURL(file)
  result.value = ''
  error.value = ''
}

function clearImage() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  fileRaw.value = null
  previewUrl.value = ''
  fileName.value = ''
  fileSizeKB.value = 0
}

async function doRecognize() {
  if (!fileRaw.value) { ElMessage.warning('请先上传图片'); return }
  recognizing.value = true
  result.value = ''
  error.value = ''
  try {
    const r = await imageApi.recognize(fileRaw.value, prompt.value)
    if (r.data.success) {
      result.value = r.data.result
      ElMessage.success('识别完成')
    } else {
      error.value = r.data.error || '未知错误'
    }
  } catch (e) {
    error.value = e.response?.data?.error || e.message || '识别失败'
  } finally {
    recognizing.value = false
  }
}

function copyResult() {
  if (!result.value) return
  navigator.clipboard.writeText(result.value).then(() => ElMessage.success('已复制'))
}
</script>

<style scoped>
.img-upload { width: 100%; }
.img-upload :deep(.el-upload) { width: 100%; }
.img-upload :deep(.el-upload-dragger) {
  width: 100%;
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius);
  border: 2px dashed var(--border);
  transition: all .2s;
}
.img-upload :deep(.el-upload-dragger:hover) {
  border-color: var(--brand-400);
  background: var(--brand-50);
}

.upload-placeholder { text-align: center; padding: 40px 20px; }

.preview-img {
  width: 100%;
  max-height: 320px;
  object-fit: contain;
  border-radius: var(--radius-sm);
}

.quick-prompt-tag {
  cursor: pointer;
  transition: all .15s;
}
.quick-prompt-tag:hover {
  background: var(--brand-50);
  color: var(--brand);
  border-color: var(--brand-400);
}
</style>
