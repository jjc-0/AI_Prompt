<template>
  <div id="app">
    <aside class="app-sidebar">
      <div class="sidebar-header">
        <h1>
          <el-icon :size="22"><Cpu /></el-icon>
          <span style="margin-left: 8px;">杰创展示 AI 助手</span>
        </h1>
        <div class="subtitle">JC Display · B2B Export Agent</div>
      </div>
      <nav class="sidebar-nav">
        <div class="nav-divider">AI 功能</div>
        <router-link to="/agent" class="nav-item" active-class="active">
          <el-icon :size="18" class="icon"><ChatDotRound /></el-icon>
          <span>AI Agent 对话</span>
        </router-link>
        <router-link to="/copywriting" class="nav-item" active-class="active">
          <el-icon :size="18" class="icon"><EditPen /></el-icon>
          <span>产品文案 & 询盘回复</span>
        </router-link>
        <router-link to="/translate" class="nav-item" active-class="active">
          <el-icon :size="18" class="icon"><Connection /></el-icon>
          <span>多语言翻译</span>
        </router-link>
        <router-link to="/analysis" class="nav-item" active-class="active">
          <el-icon :size="18" class="icon"><TrendCharts /></el-icon>
          <span>市场分析</span>
        </router-link>
        <div class="nav-divider">系统管理</div>
        <router-link to="/templates" class="nav-item" active-class="active">
          <el-icon :size="18" class="icon"><Document /></el-icon>
          <span>Prompt 模板</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="sidebar-footer-text">深圳市杰创展示公司版权所有</div>
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
import { Cpu, ChatDotRound, EditPen, Connection, TrendCharts, Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-footer-text {
  font-size: 10px;
  opacity: 0.3;
  line-height: 1.5;
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
