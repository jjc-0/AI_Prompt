<template>
  <div id="app">
    <!-- Sidebar -->
    <aside class="app-sidebar">
      <div class="sidebar-logo">
        <div class="sidebar-logo-icon" style="background:linear-gradient(135deg,#6366f1,#818cf8);overflow:hidden;">
          <svg viewBox="0 0 100 100" style="width:28px;height:28px;">
            <defs><linearGradient id="lg" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="#fff" stop-opacity="0.95"/><stop offset="100%" stop-color="#e0e7ff" stop-opacity="0.9"/></linearGradient></defs>
            <rect x="18" y="72" width="64" height="8" rx="2" fill="url(#lg)"/>
            <rect x="30" y="22" width="40" height="52" rx="3" fill="url(#lg)" opacity=".35"/>
            <rect x="22" y="44" width="56" height="5" rx="1.5" fill="url(#lg)" opacity=".75"/>
            <rect x="24" y="28" width="52" height="5" rx="1.5" fill="url(#lg)" opacity=".75"/>
            <rect x="26" y="40" width="48" height="34" rx="3" fill="url(#lg)" opacity=".55"/>
            <text x="50" y="64" text-anchor="middle" font-family="Arial,sans-serif" font-weight="900" font-size="18" fill="#6366f1">JC</text>
          </svg>
        </div>
        <div>
          <div class="sidebar-logo-text">JC Display AI</div>
          <div class="sidebar-logo-sub">B2B Export Agent</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <!-- 新消息 -->
        <div class="nav-section">
          <div class="nav-section-title">主菜单</div>
          <router-link to="/dashboard" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><HomeFilled /></el-icon>
            <span>仪表盘</span>
          </router-link>
          <router-link to="/agent" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><ChatDotRound /></el-icon>
            <span>AI Agent 对话</span>
            <span class="nav-badge">AI</span>
          </router-link>
        </div>

        <!-- 智能体 -->
        <div class="nav-section">
          <div class="nav-section-title">智能体</div>
          <router-link to="/inquiry" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><DataAnalysis /></el-icon>
            <span>询盘价值评分</span>
          </router-link>
          <router-link to="/copywriting" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><EditPen /></el-icon>
            <span>文案 & 询盘回复</span>
          </router-link>
          <router-link to="/translate" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><Connection /></el-icon>
            <span>多语言翻译</span>
          </router-link>
          <router-link to="/analysis" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><TrendCharts /></el-icon>
            <span>市场分析</span>
          </router-link>
        </div>

        <!-- 插件 -->
        <div class="nav-section">
          <div class="nav-section-title">插件</div>
          <router-link to="/templates" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><Document /></el-icon>
            <span>Prompt 模板</span>
          </router-link>
          <div class="nav-item" @click="menuPlugins = !menuPlugins" :class="{ expanded: menuPlugins }">
            <el-icon :size="16" class="nav-icon"><Setting /></el-icon>
            <span>插件管理</span>
            <el-icon :size="12" class="nav-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="nav-sub" :class="{ open: menuPlugins }">
            <router-link to="/analysis/seo" class="nav-sub-item" active-class="active">
              <span class="nav-sub-dot"></span>
              <span>SEO 审计与优化</span>
            </router-link>
            <router-link to="/analysis/competitor" class="nav-sub-item" active-class="active">
              <span class="nav-sub-dot"></span>
              <span>竞品分析</span>
            </router-link>
          </div>
        </div>

        <!-- 定时任务 -->
        <div class="nav-section">
          <div class="nav-section-title">自动化</div>
          <div class="nav-item" @click="menuSessions = !menuSessions" :class="{ expanded: menuSessions }">
            <el-icon :size="16" class="nav-icon"><Clock /></el-icon>
            <span>定时任务</span>
            <el-icon :size="12" class="nav-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="nav-sub" :class="{ open: menuSessions }">
            <router-link to="/agent" class="nav-sub-item" active-class="active">
              <span class="nav-sub-dot"></span>
              <span>海报生成</span>
            </router-link>
            <router-link to="/agent" class="nav-sub-item" active-class="active">
              <span class="nav-sub-dot"></span>
              <span>AI 站点构建器</span>
            </router-link>
          </div>
        </div>

        <!-- 消息渠道 -->
        <div class="nav-section">
          <div class="nav-section-title">通道</div>
          <router-link to="/channels" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><Message /></el-icon>
            <span>消息渠道</span>
          </router-link>
          <router-link to="/api-integration" class="nav-item" active-class="active">
            <el-icon :size="16" class="nav-icon"><Link /></el-icon>
            <span>API 集成</span>
          </router-link>
        </div>
      </nav>

      <!-- 团队 -->
      <div class="sidebar-footer">
        <div class="sidebar-footer-avatars">
          <div class="sidebar-footer-avatar">JC</div>
        </div>
        <div class="sidebar-footer-text">JC Display · 杰创展示</div>
      </div>
    </aside>

    <!-- Main -->
    <main class="app-main">
      <div class="app-main-inner">
        <router-view v-slot="{ Component, route }">
          <transition name="page" mode="out-in" appear>
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  Cpu, HomeFilled, ChatDotRound, EditPen, Connection, TrendCharts,
  Document, DataAnalysis, Setting, Clock, Message, Link, ArrowRight
} from '@element-plus/icons-vue'

const menuPlugins = ref(false)
const menuSessions = ref(false)
</script>

<style scoped>
/* Transitions */
.page-enter-active { animation: pageIn .25s cubic-bezier(.22,1,.36,1); }
.page-leave-active { animation: pageOut .15s cubic-bezier(.55,0,1,.45); }
@keyframes pageIn { from{opacity:0;transform:translateY(10px)} to{opacity:1;transform:translateY(0)} }
@keyframes pageOut { from{opacity:1;transform:translateY(0)} to{opacity:0;transform:translateY(-8px)} }
</style>
