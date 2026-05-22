import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/agent'
  },
  {
    path: '/agent',
    name: 'AgentChat',
    component: () => import('../views/AgentChat.vue'),
    meta: { title: 'AI 助手对话' }
  },
  {
    path: '/copywriting',
    name: 'CopyWriting',
    component: () => import('../views/CopyWriting.vue'),
    meta: { title: '产品文案 & 询盘回复' }
  },
  {
    path: '/translate',
    name: 'Translate',
    component: () => import('../views/Translate.vue'),
    meta: { title: '多语言翻译' }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('../views/Analysis.vue'),
    meta: { title: '展示架市场分析' }
  },
  {
    path: '/templates',
    name: 'Templates',
    component: () => import('../views/Templates.vue'),
    meta: { title: 'Prompt 模板管理' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
