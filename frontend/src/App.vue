<template>
  <div id="app">
    <aside class="app-sidebar">
      <div class="sidebar-header">
        <h1>杰创展示 AI 助手</h1>
        <div class="subtitle">JC Display · B2B Export Agent</div>
      </div>
      <nav class="sidebar-nav">
        <div class="nav-divider">AI 功能</div>
        <router-link to="/agent" class="nav-item" active-class="active">
          <span class="icon">🤖</span>
          <span>AI Agent 对话</span>
        </router-link>
        <router-link to="/copywriting" class="nav-item" active-class="active">
          <span class="icon">✍️</span>
          <span>产品文案 & 询盘回复</span>
        </router-link>
        <router-link to="/translate" class="nav-item" active-class="active">
          <span class="icon">🌐</span>
          <span>多语言翻译</span>
        </router-link>
        <router-link to="/analysis" class="nav-item" active-class="active">
          <span class="icon">📊</span>
          <span>市场分析</span>
        </router-link>
        <div class="nav-divider">系统管理</div>
        <router-link to="/templates" class="nav-item" active-class="active">
          <span class="icon">📋</span>
          <span>Prompt 模板</span>
        </router-link>
      </nav>
      <div style="padding: 16px 20px; font-size: 11px; opacity: 0.4; border-top: 1px solid rgba(255,255,255,0.06);">
        深圳市杰创展示公司权力所有
      </div>
    </aside>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
</script>
