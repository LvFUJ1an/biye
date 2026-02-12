<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import chatService from '@/services/chat'

// 应用加载时自动检查登录状态并连接WebSocket
onMounted(() => {
  // 延迟一点执行，确保用户信息已加载
  setTimeout(() => {
    chatService.autoConnectWebSocket()
  }, 500)
})
</script>

<style>
:root {
  --header-height: 60px;
  --primary-color: #409EFF;
  --text-color: #303133;
  --text-color-secondary: #909399;
  --border-color: #EBEEF5;
  --background-color: #f5f7fa;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  background-color: var(--background-color);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
}

#app {
  width: 100%;
  min-height: 100vh;
  background-color: var(--background-color);
}

.router-view-fade-enter-active,
.router-view-fade-leave-active {
  transition: opacity 0.3s ease;
}

.router-view-fade-enter-from,
.router-view-fade-leave-to {
  opacity: 0;
}
</style>
