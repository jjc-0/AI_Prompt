import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.response.use(
  response => response,
  error => {
    const msg = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export const copywritingApi = {
  generate(data) {
    return api.post('/copywriting/generate', data)
  },
  generateCollaborative(data) {
    return api.post('/copywriting/generate/collaborative', data)
  },
  getTemplates(category) {
    return api.get('/copywriting/templates', { params: { category } })
  },
  getTemplate(id) {
    return api.get(`/copywriting/templates/${id}`)
  },
  previewTemplate(id, variables) {
    return api.post(`/copywriting/templates/${id}/preview`, variables)
  },
  createTemplate(data) {
    return api.post('/copywriting/templates', data)
  },
  updateTemplate(id, data) {
    return api.put(`/copywriting/templates/${id}`, data)
  },
  deleteTemplate(id) {
    return api.delete(`/copywriting/templates/${id}`)
  },
  toggleTemplate(id) {
    return api.put(`/copywriting/templates/${id}/toggle`)
  }
}

export const translateApi = {
  translate(data) {
    return api.post('/translate', data)
  }
}

export const analysisApi = {
  analyzeMarket(data) {
    return api.post('/analysis/market', data)
  },
  getTools() {
    return api.get('/analysis/tools')
  }
}

export const inquiryApi = {
  score(data) {
    return api.post('/inquiry/score', data)
  }
}

export const agentApi = {
  chat(data) {
    return api.post('/agent/chat', data)
  },
  chatWithTools(data) {
    return api.post('/agent/chat/tools', data)
  },
  searchKnowledge(query, maxResults = 5) {
    return api.post('/agent/knowledge/search', { query, maxResults })
  },
  knowledgeStatus() {
    return api.get('/agent/knowledge/status')
  },
  getHistory(sessionId) {
    return api.get(`/agent/session/${sessionId}/history`)
  },
  getDBHistory(sessionId) {
    return api.get(`/agent/session/${sessionId}/history/db`)
  },
  getSessions(type) {
    return api.get('/agent/sessions', { params: { type } })
  },
  updateTitle(sessionId, title) {
    return api.put(`/agent/session/${sessionId}/title`, { title })
  },
  clearSession(sessionId) {
    return api.post(`/agent/session/${sessionId}/clear`)
  }
}

export default api
