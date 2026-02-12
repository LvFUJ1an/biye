<template>
  <div class="stats-container">
    <el-card class="stats-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">数据统计</span>
          <div class="header-operations">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
            <el-button type="primary">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="page-content">
        <el-empty description="数据统计功能开发中" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

const dateRange = ref([])
</script>

<style scoped>
.stats-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.stats-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.header-operations {
  display: flex;
  gap: 10px;
}

.page-content {
  padding: 20px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style> 