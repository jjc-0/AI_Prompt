<template>
  <div style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><Link /></el-icon>API 集成</h2>
      <p>开放API接入 路 Webhook 回调 路 第三方系统对接 路 数据同步</p>
    </div>
    <div class="page-body">
      <div class="page-scroll">
        <div class="empty-state" style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;">
          <div class="empty-avatar">
            <el-icon :size="36"><Link /></el-icon>
          </div>
          <h3>API 集成中心</h3>
          <p>通过RESTful API和Webhook对接ERP、CRM等第三方系统，实现数据自动化流转</p>
          <div class="hint-row">
            <span class="hint-chip">RESTful API</span>
            <span class="hint-chip">Webhook 回调</span>
            <span class="hint-chip">API Key 管理</span>
            <span class="hint-chip">数据同步</span>
          </div>
        </div>

        <!-- Integration Cards -->
        <div class="stagger-in" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:14px;">
          <div class="card card-hover" v-for="api in apis" :key="api.id">
            <div style="display:flex;align-items:center;gap:12px;margin-bottom:10px;">
              <div :style="{width:'40px',height:'40px',borderRadius:'10px',background:api.color,display:'flex',alignItems:'center',justifyContent:'center',color:'#fff'}">
                <el-icon :size="20"><component :is="api.icon" /></el-icon>
              </div>
              <div>
                <div style="font-weight:700;font-size:14px;">{{ api.name }}</div>
                <div style="font-size:11px;color:var(--text-muted);">v{{ api.version }}</div>
              </div>
            </div>
            <p style="font-size:12.5px;color:var(--text-secondary);line-height:1.6;">{{ api.desc }}</p>
            <div style="margin-top:12px;display:flex;gap:8px;">
              <el-button size="small" type="primary" disabled>查看文档</el-button>
              <el-button size="small" disabled>测试连接</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Link, Connection, Setting, DataAnalysis } from '@element-plus/icons-vue'

const apis = [
  { id:1, name:'AI Agent API', icon:'Connection', color:'#6366f1', version:'1.0', desc:'AI对话接口，支持文本生成、翻译、分析等AI能力调用' },
  { id:2, name:'产品数据API', icon:'DataAnalysis', color:'#10b981', version:'1.0', desc:'产品信息查询接口，获取1,435款展示架产品的详细数据' },
  { id:3, name:'Webhook 回调', icon:'Link', color:'#f59e0b', version:'1.0', desc:'事件驱动的Webhook回调，实时推送订单状态、消息通知等' },
  { id:4, name:'管理配置API', icon:'Setting', color:'#8b5cf6', version:'1.0', desc:'系统配置管理接口，包括模板管理、会话管理、用户设置等' },
]
</script>
