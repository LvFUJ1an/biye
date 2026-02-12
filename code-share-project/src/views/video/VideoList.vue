<template>
  <div class="video-page">
    <NavHeader />
    
    <div class="video-container">
      <!-- 头部区域 -->
      <div class="page-header">
        <h1 class="page-title">视频教程</h1>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索视频"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      
      <!-- 视频筛选区域 -->
      <!-- <div class="filter-section">
        <div class="filter-item">
          <span class="filter-label">排序：</span>
          <el-radio-group v-model="sortBy" size="small" @change="handleSearch">
            <el-radio-button label="newest">最新发布</el-radio-button>
            <el-radio-button label="hottest">最热</el-radio-button>
          </el-radio-group>
        </div>
      </div> -->
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-animation"></div>
        <p class="loading-text">正在加载视频...</p>
      </div>
      
      <!-- 视频卡片列表 -->
      <div v-else-if="videoList.length > 0" class="video-grid">
        <div 
          v-for="video in videoList" 
          :key="video.id" 
          class="video-card"
          @click="goToVideoDetail(video.id)"
          @mouseenter="handleMouseEnter($event, video)"
          @mouseleave="handleMouseLeave($event)"
        >
          <div class="video-thumbnail">
            <img :src="video.coverPath" :alt="video.title" class="thumbnail-img">
            <video 
              :src="video.videoPath" 
              class="hover-video" 
              muted 
              preload="none"
            ></video>
            <div class="video-duration">
              <el-icon><VideoPlay /></el-icon>
            </div>
          </div>
          <div class="video-info">
            <h3 class="video-title">{{ video.title }}</h3>
            <p class="video-desc">{{ video.description }}</p>
            <div class="video-meta">
              <span class="author">
                <el-avatar :size="24" :src="video.avatarUrl || defaultAvatar"></el-avatar>
                {{ video.userName }}
              </span>
              <span class="publish-date">{{ formatDate(video.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 无数据状态 -->
      <el-empty v-else description="暂无视频数据" :image-size="200">
        <template #image>
          <el-icon size="60" color="#909399"><VideoCamera /></el-icon>
        </template>
      </el-empty>
      
      <!-- 分页控件 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, VideoPlay, VideoCamera } from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import api from '@/services/api'

const router = useRouter()

// 视频列表数据
const videoList = ref<any[]>([])
const total = ref(0)
const loading = ref(true)

// 分页和搜索参数
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const sortBy = ref('newest')

// 获取视频列表数据
const fetchVideoList = async () => {
  loading.value = true
  
  try {
    const params = {
      current: currentPage.value.toString(),
      size: pageSize.value.toString(),
      keyword: searchKeyword.value
    }
    
    // 根据排序方式添加排序字段
    if (sortBy.value === 'newest') {
      params['orderBy'] = 'createTime'
      params['orderDirection'] = 'desc'
    } else if (sortBy.value === 'hottest') {
      params['orderBy'] = 'viewCount'
      params['orderDirection'] = 'desc'
    }
    
    const response = await api.post('/video/page', params)
    
    if (response.code === 0 && response.data) {
      videoList.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      console.error('获取视频列表失败:', response.message)
    }
  } catch (error) {
    console.error('获取视频列表出错:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 搜索时重置到第一页
  fetchVideoList()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchVideoList()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1 // 改变每页条数时重置到第一页
  fetchVideoList()
}

// 跳转到视频详情页
const goToVideoDetail = (id: number) => {
  router.push(`/video/detail/${id}`)
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 小于1天，显示几小时前
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return hours > 0 ? `${hours}小时前` : '刚刚'
  }
  
  // 小于7天，显示几天前
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前`
  }
  
  // 其他情况显示年月日
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 处理鼠标悬停视频卡片
const handleMouseEnter = (event, video) => {
  const videoElement = event.currentTarget.querySelector('.hover-video')
  if (videoElement) {
    videoElement.style.opacity = '1'
    videoElement.currentTime = 0
    videoElement.play().catch(error => {
      console.error('自动播放失败:', error)
    })
  }
}

// 处理鼠标离开视频卡片
const handleMouseLeave = (event) => {
  const videoElement = event.currentTarget.querySelector('.hover-video')
  if (videoElement) {
    videoElement.pause()
    videoElement.style.opacity = '0'
  }
}

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 页面加载时获取视频列表
onMounted(() => {
  fetchVideoList()
})
</script>

<style scoped>
.video-page {
  min-height: 100vh;
  background-color: var(--background-color);
}

.video-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.search-box {
  width: 300px;
}

.search-icon {
  cursor: pointer;
  color: #909399;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.filter-item {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-animation {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

.loading-text {
  color: var(--text-color-secondary);
  font-size: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 视频卡片网格 */
.video-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.video-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.video-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.video-thumbnail {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* 16:9 宽高比 */
  overflow: hidden;
}

.thumbnail-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.hover-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 2;
}

.video-duration {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  z-index: 3;
}

.video-duration .el-icon {
  margin-right: 4px;
  font-size: 14px;
}

.video-info {
  padding: 12px;
}

.video-title {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  height: 44.8px; /* 2 lines * 1.4 * 16px */
}

.video-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  height: 39px; /* 2 lines * 1.5 * 13px */
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.author {
  display: flex;
  align-items: center;
  gap: 5px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .video-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .search-box {
    width: 100%;
  }
  
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style> 