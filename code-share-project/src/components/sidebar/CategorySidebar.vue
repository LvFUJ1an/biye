<template>
  <div class="category-sidebar">
    <div class="sidebar-header">
      <h3 class="sidebar-title">技术板块</h3>
    </div>
    <ul class="category-list">
      <li v-for="category in categories" :key="category.id" 
          class="category-item" 
          :class="{ active: category.active }"
          @click="setActiveCategory(category.id)">
        <el-icon>
          <component :is="getCategoryIcon(category.name)" />
        </el-icon>
        <span>{{ category.name }}</span>
        <span class="post-count">{{ category.count }}</span>
      </li>
    </ul>
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <!-- 错误状态 -->
    <div v-if="error" class="error-state">
      <el-icon><WarningFilled /></el-icon>
      <span>{{ error }}</span>
      <el-button size="small" type="primary" @click="fetchCategories">重试</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { 
  Monitor, 
  Connection, 
  DataAnalysis, 
  Cpu, 
  Lock, 
  MagicStick, 
  SetUp, 
  Promotion,
  Briefcase,
  Cloudy,
  Loading,
  WarningFilled
} from '@element-plus/icons-vue'
import axios from 'axios'
import api from '@/services/api'

const iconComponents = {
  Monitor,
  Connection,
  DataAnalysis,
  Cpu,
  Lock,
  MagicStick,
  SetUp,
  Promotion,
  Briefcase,
  Cloud: Cloudy
}

// 板块数据、加载状态和错误信息
const categories = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const activeId = ref<number | null>(null)

// 获取板块列表数据
const fetchCategories = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await api.post('/sections/list')
    
    // 如果请求成功
    if (response.code === 0) {
      // 处理数据，初始状态下不激活任何板块
      categories.value = response.data.map((item: any) => ({
        ...item,
        active: false // 默认不选中任何板块
      }))
      
      // 初始状态下不选择任何板块
      activeId.value = null
    } else {
      error.value = response.message || '获取板块数据失败'
    }
  } catch (err) {
    console.error('获取板块数据出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 根据板块名称获取对应的图标
const getCategoryIcon = (name: string) => {
  const iconMap: {[key: string]: string} = {
    '前端开发': 'Monitor',
    '后端开发': 'Connection',
    '数据库': 'DataAnalysis',
    'Python': 'MagicStick',
    'Java': 'Cpu',
    '安全技术': 'Lock',
    '人工智能': 'SetUp',
    '云计算': 'Cloud',
    '运维/测试': 'Promotion',
    '求职/招聘': 'Briefcase'
  }
  
  return iconComponents[iconMap[name] || 'Monitor']
}

// 设置激活的板块
const setActiveCategory = (id: number) => {
  activeId.value = id
  categories.value.forEach(cat => {
    cat.active = cat.id === id
  })
  
  // 发出自定义事件，通知其他组件板块已变更
  const event = new CustomEvent('section-change', {
    detail: { 
      sectionId: id,
      sectionName: categories.value.find(item => item.id === id)?.name || ''
    }
  })
  window.dispatchEvent(event)
  
  console.log('板块切换:', id)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-sidebar {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
  height: auto;
  min-height: 100%;
  position: relative;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.sidebar-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
}

.category-list {
  list-style: none;
  padding: 8px 0;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  color: var(--text-color-secondary);
}

.category-item:hover, .category-item.active {
  background-color: rgba(64, 158, 255, 0.1);
  color: var(--primary-color);
}

.category-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background-color: var(--primary-color);
}

.category-item .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.post-count {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
  background-color: #f2f6fc;
  padding: 2px 6px;
  border-radius: 10px;
}

.category-item:hover .post-count, .category-item.active .post-count {
  background-color: rgba(64, 158, 255, 0.2);
  color: var(--primary-color);
}

.loading-state, .error-state {
  padding: 20px;
  text-align: center;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.loading-icon {
  animation: spin 1s infinite linear;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-state .el-icon {
  color: #f56c6c;
  font-size: 24px;
}
</style> 