<template>
  <div class="page-fullscreen" style="display:flex;flex-direction:column;flex:1;min-height:0;">
    <div class="page-header">
      <h2><el-icon :size="20"><Message /></el-icon>消息渠道</h2>
      <p>多渠道消息管理 路 WhatsApp / Email / WeChat / 阿里询盘 路 统一收件箱</p>
    </div>
    <div class="page-body">
      <div class="page-scroll">
        <div class="empty-state" style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;">
          <div class="empty-avatar">
            <el-icon :size="36"><Message /></el-icon>
          </div>
          <h3>消息渠道集成</h3>
          <p>接入WhatsApp、Email、WeChat、阿里询盘等渠道，统一管理所有客户消息</p>
          <div class="hint-row">
            <span class="hint-chip">WhatsApp Business API</span>
            <span class="hint-chip">Email (IMAP/SMTP)</span>
            <span class="hint-chip">阿里国际站询盘</span>
            <span class="hint-chip">WeChat Official</span>
          </div>
        </div>

        <!-- Channel Cards -->
        <div class="stagger-in" style="display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:14px;">
          <div class="card card-hover" v-for="ch in channels" :key="ch.id">
            <div style="display:flex;align-items:center;gap:12px;margin-bottom:10px;">
              <div :style="{width:'40px',height:'40px',borderRadius:'10px',background:ch.color,display:'flex',alignItems:'center',justifyContent:'center',color:'#fff'}">
                <el-icon :size="20"><component :is="ch.icon" /></el-icon>
              </div>
              <div>
                <div style="font-weight:700;font-size:14px;">{{ ch.name }}</div>
                <div style="font-size:11px;color:var(--text-muted);">{{ ch.status }}</div>
              </div>
            </div>
            <p style="font-size:12.5px;color:var(--text-secondary);line-height:1.6;">{{ ch.desc }}</p>
            <div style="margin-top:12px;display:flex;gap:8px;">
              <el-button size="small" :type="ch.connected ? 'success' : 'primary'" disabled>
                {{ ch.connected ? '已连接' : '待配置' }}
              </el-button>
              <el-button size="small" disabled>配置</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Message, ChatDotRound, Iphone, Message as EmailIcon } from '@element-plus/icons-vue'

const channels = [
  { id:1, name:'WhatsApp Business', icon:'ChatDotRound', color:'#25D366', status:'未配置', connected:false, desc:'通过WhatsApp Business API接收和发送客户消息，支持多媒体和模板消息' },
  { id:2, name:'Email 邮件', icon:'Message', color:'#6366f1', status:'未配置', connected:false, desc:'IMAP/SMTP邮件集成，统一管理询盘邮件和历史往来记录' },
  { id:3, name:'阿里国际站', icon:'Message', color:'#f97316', status:'未配置', connected:false, desc:'接入Alibaba.com询盘系统，自动同步RFQ和TradeManager消息' },
  { id:4, name:'微信企业号', icon:'ChatDotRound', color:'#07C160', status:'未配置', connected:false, desc:'微信企业号集成，管理国内供应商和客户沟通' },
]
</script>
