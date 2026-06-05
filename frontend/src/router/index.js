import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '数据仪表盘' }
  },
  {
    path: '/agent',
    name: 'AgentChat',
    component: () => import('../views/AgentChat.vue'),
    meta: { title: 'AI Agent 对话' }
  },
  {
    path: '/inquiry',
    name: 'InquiryScoring',
    component: () => import('../views/InquiryScoring.vue'),
    meta: { title: '询盘价值评分' }
  },
  {
    path: '/copywriting',
    name: 'CopyWriting',
    component: () => import('../views/CopyWriting.vue'),
    meta: { title: '文案 & 询盘回复' }
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
    meta: { title: '市场分析' }
  },
  {
    path: '/analysis/seo',
    name: 'AnalysisSEO',
    component: () => import('../views/Analysis.vue'),
    meta: { title: 'SEO 审计与优化' }
  },
  {
    path: '/analysis/competitor',
    name: 'AnalysisCompetitor',
    component: () => import('../views/Analysis.vue'),
    meta: { title: '竞品分析' }
  },
  {
    path: '/templates',
    name: 'Templates',
    component: () => import('../views/Templates.vue'),
    meta: { title: 'Prompt 模板管理' }
  },
  {
    path: '/channels',
    name: 'Channels',
    component: () => import('../views/Channels.vue'),
    meta: { title: '消息渠道' }
  },
  {
    path: '/api-integration',
    name: 'ApiIntegration',
    component: () => import('../views/ApiIntegration.vue'),
    meta: { title: 'API 集成' }
  },
  {
    path: '/channels',
    name: 'Channels',
    component: () => import('../views/Channels.vue'),
    meta: { title: '消息渠道' }
  },
  {
    path: '/api-integration',
    name: 'ApiIntegration',
    component: () => import('../views/ApiIntegration.vue'),
    meta: { title: 'API 集成' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach((to) => {
  const title = to.meta.title
  document.title = title ? title + ' - JC Display AI Agent' : 'JC Display AI Agent'
})

export default router
