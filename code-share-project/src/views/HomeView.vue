<template>
  <div class="home-container">
    <NavHeader />
    <div class="main-content container flex gap-20">
      <CategorySidebar class="sidebar" />
      <MainContent class="content" />
      <RightSidebar class="right-sidebar" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import NavHeader from '@/components/layout/NavHeader.vue'
import CategorySidebar from '@/components/sidebar/CategorySidebar.vue'
import MainContent from '@/components/content/MainContent.vue'
import RightSidebar from '@/components/sidebar/RightSidebar.vue'

const route = useRoute()

// 监听路由变化
const isChat = computed(() => route.path === '/chat')

// 简化逻辑，直接显示MainContent组件
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  padding-top: 20px;
  flex: 1;
  align-items: flex-start;
  background-color: #f5f7fa;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 20px);
  max-height: calc(100vh - var(--header-height) - 20px);
  overflow-y: auto;
}

.content {
  flex: 1;
}

.right-sidebar {
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 20px);
  max-height: calc(100vh - var(--header-height) - 20px);
  overflow-y: auto;
}

@media (max-width: 1200px) {
  .right-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style> 