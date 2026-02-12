<template>
  <div class="main-content">
    <div class="tab-container">
      <div 
        :class="['tab-item', { active: currentTab === 'latest' }]" 
        @click="switchTab('latest')"
      >
        最新发布
      </div>
      <div 
        :class="['tab-item', { active: currentTab === 'hot' }]" 
        @click="switchTab('hot')"
      >
        热门文章
      </div>
    </div>
    
    <div class="blog-list" ref="blogListRef">
      <BlogCard 
        v-for="blog in blogList" 
        :key="blog.id" 
        :blog="blog" 
      />
      
      <div v-if="loading" class="loading-more">
        <el-icon class="loading"><Loading /></el-icon>
        加载更多...
      </div>
      
      <div v-if="!loading && !hasMore" class="no-more">
        没有更多内容了
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, reactive } from 'vue'
import { Loading } from '@element-plus/icons-vue'

// 当前选中的标签页
const currentTab = ref('latest')
// 当前选中的板块ID
const currentSectionId = ref<number | null>(null)
// 博客列表
const blogList = ref<any[]>([])
// 加载状态
const loading = ref(false)
// 是否还有更多
const hasMore = ref(true)
// 分页信息
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
// 博客列表元素引用
const blogListRef = ref<HTMLElement | null>(null)

// 切换标签页
const switchTab = (tab: string) => {
  currentTab.value = tab
  // 重置分页
  pagination.pageNum = 1
  blogList.value = []
  hasMore.value = true
  // 重新加载数据
  loadBlogs()
}

// 加载博客数据
const loadBlogs = async () => {
  if (loading.value || !hasMore.value) return
  
  loading.value = true
  try {
    // 构建请求参数
    const params: any = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: 1  // 已发布状态
    }
    
    // 添加板块ID
    if (currentSectionId.value) {
      params.sectionId = currentSectionId.value
    }
    
    // 根据标签页添加排序参数
    if (currentTab.value === 'hot') {
      params.orderBy = 'viewCount'
      params.orderDirection = 'desc'
    } else {
      params.orderBy = 'createTime'
      params.orderDirection = 'desc'
    }
    
    // 发起请求
    const res = await api.get('/blog/page', params)
    
    if (res.data.code === 0) {
      const { list, total } = res.data.data
      
      if (list && list.length > 0) {
        if (pagination.pageNum === 1) {
          blogList.value = list
        } else {
          blogList.value.push(...list)
        }
        
        pagination.total = total
        pagination.pageNum++
        hasMore.value = blogList.value.length < total
      } else {
        hasMore.value = false
      }
    } else {
      console.error('加载博客失败:', res.data.message)
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载博客出错:', error)
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

// 监听滚动到底部事件
const handleScroll = () => {
  if (!blogListRef.value) return
  
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  
  // 滚动到底部加载更多
  if (scrollTop + clientHeight >= scrollHeight - 200 && !loading.value && hasMore.value) {
    loadBlogs()
  }
}

// 处理板块变化事件
const handleSectionChange = (event: CustomEvent) => {
  const { sectionId } = event.detail
  currentSectionId.value = sectionId
  
  // 重置分页
  pagination.pageNum = 1
  blogList.value = []
  hasMore.value = true
  
  // 重新加载数据
  loadBlogs()
}

onMounted(() => {
  // 初始加载数据
  loadBlogs()
  
  // 添加滚动事件监听
  window.addEventListener('scroll', handleScroll)
  
  // 添加板块变化事件监听
  window.addEventListener('section-change', handleSectionChange as EventListener)
})

onBeforeUnmount(() => {
  // 移除事件监听
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('section-change', handleSectionChange as EventListener)
})
</script>

<style scoped>
.main-content {
  flex: 1;
  padding: 20px;
  border-radius: 8px;
  background-color: var(--background-color, #fff);
}

.tab-container {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.tab-item {
  padding: 10px 20px;
  cursor: pointer;
  position: relative;
  font-weight: 500;
  color: #666;
}

.tab-item:hover {
  color: var(--primary-color, #409eff);
}

.tab-item.active {
  color: var(--primary-color, #409eff);
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--primary-color, #409eff);
}

.blog-list {
  min-height: 200px;
}

.loading-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  color: #999;
}

.loading {
  margin-right: 5px;
  animation: rotating 2s linear infinite;
}

.no-more {
  text-align: center;
  padding: 20px 0;
  color: #999;
  font-size: 14px;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style> 