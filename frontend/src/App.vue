<template>
  <div id="app">
    <!-- Sidebar -->
    <aside class="app-sidebar" v-if="!isAgent">
      <div class="sidebar-logo">
        <div class="sidebar-logo-icon">
          <el-icon :size="20"><Cpu /></el-icon>
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
          <div class="nav-item">
            <el-icon :size="16" class="nav-icon"><Message /></el-icon>
            <span>消息渠道</span>
          </div>
          <div class="nav-item">
            <el-icon :size="16" class="nav-icon"><Link /></el-icon>
            <span>API 集成</span>
          </div>
        </div>
      </nav>

      <!-- 团队 -->
      <div class="sidebar-footer">
        <div class="sidebar-footer-avatars">
          <div class="sidebar-footer-avatar">JD</div>
          <div class="sidebar-footer-avatar" style="background:#e0f2fe;color:#0284c7;">JK</div>
          <div class="sidebar-footer-avatar" style="background:#fef3c7;color:#d97706;">AL</div>
        </div>
        <div class="sidebar-footer-text">JC Display · 杰创展示</div>
      </div>
    </aside>

    <!-- Main -->
    <main class="app-main">
      <router-view v-slot="{ Component, route }">
        <transition name="page" mode="out-in" appear>
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Cpu, HomeFilled, ChatDotRound, EditPen, Connection, TrendCharts,
  Document, DataAnalysis, Setting, Clock, Message, Link, ArrowRight
} from '@element-plus/icons-vue'

const route = useRoute()
const isAgent = computed(() => route.name === 'AgentChat')
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
