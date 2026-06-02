<template>
  <div id="app">
    <aside class="app-sidebar">
      <div class="sidebar-header">
        <h1>
          <el-icon :size="22"><Cpu /></el-icon>
          <span>JC Display AI</span>
        </h1>
        <div class="subtitle">B2B Export Agent Platform</div>
      </div>
      <nav class="sidebar-nav">
        <div class="nav-divider">总览</div>
        <router-link to="/dashboard" class="nav-item" active-class="active">
          <el-icon :size="16" class="icon"><Odometer /></el-icon>
          <span>数据仪表盘</span>
          <span class="badge-nav">NEW</span>
        </router-link>
        <div class="nav-divider">AI 功能</div>
        <router-link to="/agent" class="nav-item" active-class="active">
          <el-icon :size="16" class="icon"><ChatDotRound /></el-icon>
          <span>AI Agent 对话</span>
        </router-link>
        <router-link to="/inquiry" class="nav-item" active-class="active">
          <el-icon :size="16" class="icon"><DataAnalysis /></el-icon>
          <span>询盘价值评分</span>
          <span class="badge-nav">NEW</span>
        </router-link>
        <router-link to="/copywriting" class="nav-item" active-class="active">
          <el-icon :size="16" class="icon"><EditPen /></el-icon>
          <span>文案 & 询盘回复</span>
        </router-link>
        <router-link to="/translate" class="nav-item" active-class="active">
          <el-icon :size="16" class="icon"><Connection /></el-icon>
          <span>多语言翻译</span>
        </router-link>
        <router-link to="/analysis" class="nav-item" active-class="active">
          <el-icon :size="16" class="icon"><TrendCharts /></el-icon>
          <span>市场分析</span>
        </router-link>
        <div class="nav-divider">系统管理</div>
        <router-link to="/templates" class="nav-item" active-class="active">
          <el-icon :size="16" class="icon"><Document /></el-icon>
          <span>Prompt 模板</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <span>JC Display · 杰创展示</span>
      </div>
    </aside>
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { Cpu, ChatDotRound, EditPen, Connection, TrendCharts, Document, Odometer, DataAnalysis } from '@element-plus/icons-vue'
</script>

<style scoped>
.sidebar-footer {
  padding: 16px 22px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 11px;
  color: rgba(148, 163, 184, 0.35);
  text-align: center;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
