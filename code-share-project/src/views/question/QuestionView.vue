<template>
  <div class="question-view-container">
    <NavHeader />
    <div class="main-content container flex gap-20">
      <CategorySidebar class="sidebar" />
      <QuestionList class="content" />
      <RightSidebar class="right-sidebar" />
    </div>
  </div>
</template>

<script setup lang="ts">
import NavHeader from '@/components/layout/NavHeader.vue'
import CategorySidebar from '@/components/sidebar/CategorySidebar.vue'
import QuestionList from '@/components/question/QuestionList.vue'
import RightSidebar from '@/components/sidebar/RightSidebar.vue'
</script>

<style scoped>
.question-view-container {
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