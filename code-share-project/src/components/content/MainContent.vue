<template>
  <div class="main-content-wrapper">
    <div class="content-header">
      <div class="header-top flex justify-between align-center mb-10">
        <!-- <el-tabs v-model="activeTab" class="content-tabs">
          <el-tab-pane label="推荐" name="recommended"></el-tab-pane>
          <el-tab-pane label="最新" name="latest"></el-tab-pane>
          <el-tab-pane label="热门" name="popular"></el-tab-pane>
        </el-tabs> -->
        
        <div class="search-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索博客文章"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>
      
      <div v-if="pageParams.keyword" class="search-info flex align-center mb-10">
        <el-tag closable @close="clearSearch">
          关键词: {{ pageParams.keyword }}
        </el-tag>
      </div>
    </div>
    
    <div class="article-list">
      <div v-for="(article, index) in articles" :key="article.id || index" class="article-item">
        <div class="article-header flex justify-between align-center">
          <div class="author-info flex align-center gap-10">
            <el-avatar :size="30" :src="article.authorAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
            <span class="author-name">{{ article.authorName || '匿名用户' }}</span>
            <span class="post-time">{{ formatDate(article.createTime) }}</span>
          </div>
          <div class="category-tag">
            <el-tag size="small" :type="index % 5 === 0 ? '' : index % 5 === 1 ? 'success' : index % 5 === 2 ? 'warning' : index % 5 === 3 ? 'danger' : 'info'">
              {{ article.sectionName || '未分类' }}
            </el-tag>
          </div>
        </div>
        
        <div class="article-content">
          <h3 class="article-title">
            <a :href="`/article/${article.id}`">{{ article.title }}</a>
          </h3>
          <p class="article-summary">{{ getTextSummary(article.content) }}</p>
          
          <div class="article-cover" v-if="article.coverImage">
            <img :src="article.coverImage" :alt="article.title" />
          </div>
        </div>
        
        <div class="article-footer flex justify-between align-center">
          <div class="article-stats flex align-center gap-10">
            <span class="stat-item flex align-center">
              <el-icon><View /></el-icon>
              {{ formatNumber(article.viewCount || 0) }}
            </span>
            <span class="stat-item flex align-center">
              <el-icon><ChatDotRound /></el-icon>
              {{ formatNumber(article.commentCount || 0) }}
            </span>
            <span class="stat-item flex align-center">
              <el-icon><Star /></el-icon>
              {{ formatNumber(article.likeCount || 0) }}
            </span>
          </div>
          <div class="action-buttons">
            <el-button link type="primary" size="small" @click="$router.push(`/article/${article.id}`)">阅读全文</el-button>
            <el-button link type="primary" size="small">收藏</el-button>
          </div>
        </div>
      </div>
      
      <!-- 加载状态和无数据状态 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-if="finished && articles.length === 0" class="empty-state">
        <el-empty description="暂无数据" />
      </div>
      
      <div v-if="finished && articles.length > 0" class="no-more-state">
        <span>没有更多数据了</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch, onUnmounted } from 'vue'
import { View, ChatDotRound, Star, Loading, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'
import { marked } from 'marked'
import { useRouter } from 'vue-router'

// 获取router实例
const router = useRouter()

// Markdown配置 - 用于预处理内容
marked.setOptions({
  headerIds: false,
  mangle: false,
  gfm: true
})

// 选项卡状态
const activeTab = ref('latest')

// 博客列表数据
const articles = ref<any[]>([])

// 分页和加载状态
const loading = ref(false)
const finished = ref(false)
const pageParams = reactive({
  current: 1,
  size: 10,
  sectionId: null as number | null,
  authorId: null as number | null,
  sortBy: 'latest' as 'latest' | 'hottest' | 'recommended',
  keyword: '' as string // 新增搜索关键词参数
})

// 搜索关键词输入
const searchKeyword = ref('')

// 根据选项卡映射排序方式
const tabToSortBy = {
  'latest': 'latest',
  'popular': 'hottest',
  'recommended': 'recommended'
} as const

// 处理搜索
const handleSearch = () => {
  // 如果搜索关键词没有变化，不需要重新加载
  if (pageParams.keyword === searchKeyword.value.trim()) return
  
  // 更新关键词并重置列表
  pageParams.keyword = searchKeyword.value.trim()
  resetArticleList()
  
  // 重新加载数据
  loadArticles()
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  pageParams.keyword = ''
  resetArticleList()
  loadArticles()
}

// 重置文章列表和分页状态
const resetArticleList = () => {
  articles.value = []
  pageParams.current = 1
  finished.value = false
}

// 监听选项卡变化，重置列表并重新加载
watch(() => activeTab.value, (newTab) => {
  // 重置列表和状态
  resetArticleList()
  
  // 更新排序方式
  pageParams.sortBy = tabToSortBy[newTab as keyof typeof tabToSortBy] || 'latest'
  
  // 重新加载数据
  loadArticles()
})

// 监听来自CategorySidebar的板块变更事件
const handleSectionChange = (sectionId: number | null) => {
  // 如果板块ID没有变化，则不重新加载
  if (pageParams.sectionId === sectionId) return
  
  // 更新板块ID并重置列表
  pageParams.sectionId = sectionId
  resetArticleList()
  
  // 重新加载数据
  loadArticles()
}

// 将Markdown转换为纯文本摘要
const getTextSummary = (markdown: string, maxLength = 200) => {
  if (!markdown) return '暂无内容'
  
  try {
    // 将Markdown转换为HTML
    const html = marked.parse(markdown)
    
    // 去除HTML标签
    const text = html.replace(/<[^>]+>/g, '')
    
    // 去除多余空格和换行
    const cleanText = text.replace(/\s+/g, ' ').trim()
    
    // 返回截断的文本
    return cleanText.length > maxLength 
      ? cleanText.substring(0, maxLength) + '...' 
      : cleanText
  } catch (error) {
    console.error('解析Markdown出错:', error)
    return markdown.substring(0, maxLength) + '...'
  }
}

// 加载博客文章列表
const loadArticles = async () => {
  if (loading.value || finished.value) return
  
  loading.value = true
  
  try {
    // 构建请求参数
    const params = {
      current: pageParams.current,
      size: pageParams.size,
      sortBy: pageParams.sortBy,
      status: 1  // 固定为已发布状态
    } as any
    
    // 可选参数
    if (pageParams.sectionId) params.sectionId = pageParams.sectionId
    if (pageParams.authorId) params.authorId = pageParams.authorId
    if (pageParams.keyword) params.keyword = pageParams.keyword // 添加关键词搜索参数
    
    console.log('加载博客文章，参数:', params)
    
    const response = await api.get('/blog/page', params)
    
    if (response.code === 0) {
      // 处理直接返回数组的情况
      let newRecords = []
      let isLastPage = false
      
      if (Array.isArray(response.data)) {
        // 数据直接是一个数组
        newRecords = response.data
        // 如果返回的数据少于请求的数量，说明没有更多数据了
        isLastPage = newRecords.length < pageParams.size
      } else if (response.data && response.data.records) {
        // 数据是包含分页信息的对象
        const { records, current, pages } = response.data
        newRecords = records
        isLastPage = current >= pages || records.length === 0
      } else {
        // 其他情况，可能是空数据
        isLastPage = true
      }
      
      // 添加到当前列表
      articles.value = [...articles.value, ...newRecords]
      
      // 更新页码
      pageParams.current += 1
      
      // 判断是否加载完成
      finished.value = isLastPage
      
      if (finished.value && articles.value.length === 0) {
        console.log('暂无数据')
      } else if (finished.value) {
        console.log('没有更多数据了')
      }
    } else {
      ElMessage.error(response.message || '获取博客列表失败')
      finished.value = true
    }
  } catch (error) {
    console.error('加载博客文章出错:', error)
    ElMessage.error('网络错误，请稍后重试')
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 小于1小时，显示x分钟前
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000))
    return `${minutes}分钟前`
  }
  
  // 小于24小时，显示x小时前
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }
  
  // 小于7天，显示x天前
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前`
  }
  
  // 大于7天，显示年月日
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric'
  })
}

// 处理无封面图的情况
const getRandomImage = (index: number) => {
  const images = [
    'https://picsum.photos/id/48/600/300',
    'https://picsum.photos/id/180/600/300',
    'https://picsum.photos/id/237/600/300',
    'https://picsum.photos/id/24/600/300',
    'https://picsum.photos/id/96/600/300'
  ]
  return images[index % images.length]
}

// 处理滚动触底事件
const handleScroll = () => {
  const scrollHeight = document.documentElement.scrollHeight
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const clientHeight = document.documentElement.clientHeight
  
  // 触底时加载更多
  if (scrollTop + clientHeight >= scrollHeight - 100 && !loading.value && !finished.value) {
    loadArticles()
  }
}

// 格式化浏览量、点赞数
const formatNumber = (num: number | string) => {
  const n = Number(num)
  if (isNaN(n)) return '0'
  
  if (n >= 1000) {
    return (n / 1000).toFixed(1) + 'k'
  }
  
  return n.toString()
}

// 初始化
onMounted(() => {
  // 监听技术板块变化事件
  window.addEventListener('section-change', ((event: CustomEvent) => {
    handleSectionChange(event.detail.sectionId)
  }) as EventListener)
  
  // 加载初始数据
  loadArticles()
  
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
})

// 组件卸载时移除监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('section-change', ((event: CustomEvent) => {
    handleSectionChange(event.detail.sectionId)
  }) as EventListener)
})
</script>

<style scoped>
.main-content-wrapper {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
  height: 100%;
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

.content-header {
  padding: 10px 16px;
  border-bottom: 1px solid var(--border-color);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-tabs {
  flex: 1;
}

.search-wrapper {
  width: 300px;
}

.search-input {
  width: 100%;
}

.search-info {
  margin-top: 5px;
}

.mb-10 {
  margin-bottom: 10px;
}

.article-list {
  padding: 0 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color);
}

.article-item:last-child {
  border-bottom: none;
}

.author-name {
  font-weight: 500;
  color: var(--text-color);
}

.post-time {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.article-title {
  margin: 12px 0;
  font-size: 18px;
  color: var(--text-color);
  line-height: 1.4;
}

.article-title a {
  color: inherit;
  text-decoration: none;
}

.article-title a:hover {
  color: var(--primary-color);
  text-decoration: none;
}

.article-summary {
  color: var(--text-color-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.article-cover {
  width: 100%;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: auto;
  object-fit: cover;
  transition: transform 0.3s;
}

.article-cover img:hover {
  transform: scale(1.02);
}

.article-stats {
  color: var(--text-color-secondary);
  font-size: 13px;
}

.stat-item {
  margin-right: 12px;
}

.stat-item .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.loading-state, .no-more-state, .empty-state {
  padding: 24px 0;
  text-align: center;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.loading-icon {
  animation: spin 1s infinite linear;
  margin-right: 6px;
  font-size: 18px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style> 