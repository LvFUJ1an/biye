<template>
  <div class="video-detail-page">
    <NavHeader />
    
    <div class="video-detail-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-animation"></div>
        <p class="loading-text">正在加载视频...</p>
      </div>
      
      <!-- 加载错误 -->
      <div v-else-if="error" class="error-container">
        <el-result icon="error" :title="error" sub-title="视频加载失败，请稍后重试">
          <template #extra>
            <el-button type="primary" @click="fetchVideoDetail">重试</el-button>
            <el-button @click="$router.push('/video')">返回视频列表</el-button>
          </template>
        </el-result>
      </div>
      
      <!-- 视频内容 -->
      <div v-else-if="video" class="video-content">
        <div class="video-main-content">
          <!-- 视频播放器 -->
          <div class="video-player-container">
            <video 
              ref="videoPlayer" 
              class="video-player" 
              controls 
              autoplay
              @play="handleVideoPlay"
              @pause="handleVideoPause"
              @ended="handleVideoEnded"
            >
              <source :src="video.videoPath" type="video/mp4">
              您的浏览器不支持 HTML5 视频播放
            </video>
          </div>
          
          <!-- 视频信息 -->
          <div class="video-info">
            <h1 class="video-title">{{ video.title }}</h1>
            
            <div class="video-meta">
              <div class="view-count">
                <el-icon><View /></el-icon>
                <span>{{ video.viewCount || 0 }} 次观看</span>
              </div>
              <div class="upload-time">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(video.createTime) }}</span>
              </div>
            </div>
            
            <!-- <div class="video-actions">
              <el-button type="primary" size="large" :icon="isLiked ? 'StarFilled' : 'Star'" :class="{ 'is-liked': isLiked }" @click="handleLike">
                {{ isLiked ? '已点赞' : '点赞' }}
              </el-button>
              <el-button type="info" size="large" icon="Share" @click="handleShare">
                分享
              </el-button>
              <el-button type="warning" size="large" icon="Download" @click="handleDownload">
                下载
              </el-button>
            </div> -->
          </div>
          
          <!-- 视频描述 -->
          <div class="video-description">
            <div class="description-header">
              <h3>视频介绍</h3>
              <el-button 
                type="text" 
                class="expand-btn" 
                @click="isExpanded = !isExpanded"
              >
                {{ isExpanded ? '收起' : '展开' }}
                <el-icon>
                  <component :is="isExpanded ? 'ArrowUp' : 'ArrowDown'" />
                </el-icon>
              </el-button>
            </div>
            <p :class="{ 'expanded': isExpanded }">{{ video.description }}</p>
          </div>
          
          <!-- 作者信息 -->
          <div class="author-card">
            <div class="author-avatar">
              <el-avatar :size="60" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"></el-avatar>
            </div>
            <div class="author-info">
              <h3 class="author-name">{{ video.userName || '未知作者' }}</h3>
              <p class="author-subscribers">粉丝: 1.2k</p>
            </div>
            <div class="author-actions">
              <el-button type="danger" size="small">关注</el-button>
            </div>
          </div>
          
          <!-- 评论区 - 后期实现 -->
          <!-- <div class="comments-section">
            <h3 class="section-title">评论</h3>
            <p class="section-placeholder">评论功能正在开发中...</p>
          </div> -->
        </div>
        
        <!-- 推荐视频 -->
        <div class="related-videos">
          <h3 class="section-title">推荐视频</h3>
          
          <div v-if="relatedVideos.length > 0" class="related-list">
            <div 
              v-for="relatedVideo in relatedVideos" 
              :key="relatedVideo.id"
              class="related-video-item"
              @click="goToRelatedVideo(relatedVideo.id)"
            >
              <div class="related-video-thumbnail">
                <img :src="relatedVideo.coverPath" :alt="relatedVideo.title">
              </div>
              <div class="related-video-info">
                <h4 class="related-video-title">{{ relatedVideo.title }}</h4>
                <div class="related-video-meta">
                  <span>{{ relatedVideo.userName }}</span>
                  <span>{{ formatDate(relatedVideo.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="related-video-placeholder">
            暂无相关推荐视频
          </div>
        </div>
      </div>
      
      <!-- 没有数据 -->
      <div v-else class="no-data-container">
        <el-empty description="未找到视频信息" />
        <el-button type="primary" @click="$router.push('/video')">返回视频列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Calendar, Star, StarFilled, Share, Download, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()
const videoId = computed(() => route.params.id as string)

// 视频播放器引用
const videoPlayer = ref<HTMLVideoElement | null>(null)

// 视频数据
const video = ref<any>(null)
const loading = ref(true)
const error = ref('')

// 界面状态
const isLiked = ref(false)
const isExpanded = ref(false)

// 获取视频详情
const fetchVideoDetail = async () => {
  if (!videoId.value) {
    error.value = '视频ID无效'
    loading.value = false
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await api.post(`/video/getVideoById/${videoId.value}`)
    
    if (response.code === 0 && response.data) {
      video.value = response.data
      
      // 更新页面标题
      document.title = `${video.value.title} - IT技术社区`
      
      // 检查用户是否已点赞（模拟数据）
      isLiked.value = Math.random() > 0.5
      
      // 视频加载完成后尝试获取相关推荐
      fetchRelatedVideos()
    } else {
      error.value = response.message || '获取视频详情失败'
    }
  } catch (err) {
    console.error('获取视频详情出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 获取相关推荐视频
const relatedVideos = ref<any[]>([])
const fetchRelatedVideos = async () => {
  if (!video.value || !video.value.id) return
  
  try {
    // 这里使用video/page接口模拟相关视频推荐，实际项目中可能需要专门的推荐接口
    const response = await api.post('/video/page', {
      current: '1',
      size: '5',
      excludeId: video.value.id // 排除当前视频
    })
    
    if (response.code === 0 && response.data) {
      relatedVideos.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取相关视频失败:', error)
  }
}

// 处理视频播放事件
const handleVideoPlay = () => {
  console.log('视频开始播放')
  // 可以在这里进行播放数据统计等操作
}

// 处理视频暂停事件
const handleVideoPause = () => {
  console.log('视频已暂停')
}

// 处理视频播放结束事件
const handleVideoEnded = () => {
  console.log('视频播放结束')
  // 可以在这里处理视频播放完毕后的操作，如展示推荐视频等
}

// 处理点赞
const handleLike = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  isLiked.value = !isLiked.value
  ElMessage.success(isLiked.value ? '点赞成功' : '已取消点赞')
  // TODO: 实际的点赞API调用
}

// 处理分享
const handleShare = () => {
  // 复制当前页面URL到剪贴板
  const url = window.location.href
  navigator.clipboard.writeText(url)
    .then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制链接失败')
    })
}

// 处理下载
const handleDownload = () => {
  if (!video.value || !video.value.videoPath) {
    ElMessage.error('视频链接无效，无法下载')
    return
  }
  
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再下载视频')
    return
  }
  
  // 创建一个用于下载的a标签
  const downloadLink = document.createElement('a')
  downloadLink.href = video.value.videoPath
  downloadLink.download = video.value.title || 'video.mp4'
  document.body.appendChild(downloadLink)
  downloadLink.click()
  document.body.removeChild(downloadLink)
  
  ElMessage.success('开始下载视频')
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 跳转到相关视频
const goToRelatedVideo = (id: number) => {
  // 如果是同一个组件内跳转，需要监听路由变化并重新获取数据
  if (id === Number(videoId.value)) return
  
  router.push(`/video/detail/${id}`)
}

// 监听路由变化，当URL参数改变时重新加载数据
watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchVideoDetail()
  }
})

// 组件挂载时获取视频详情
onMounted(() => {
  fetchVideoDetail()
})

// 组件卸载时处理清理工作
onUnmounted(() => {
  // 停止视频播放，释放资源
  if (videoPlayer.value) {
    videoPlayer.value.pause()
    videoPlayer.value.src = ''
    videoPlayer.value.load()
  }
})
</script>

<style scoped>
.video-detail-page {
  min-height: 100vh;
  background-color: var(--background-color);
}

.video-detail-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
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

/* 错误状态 */
.error-container {
  padding: 40px 0;
}

/* 视频内容 */
.video-content {
  display: flex;
  gap: 20px;
}

.video-main-content {
  flex: 1;
}

.video-player-container {
  width: 100%;
  padding-top: 56.25%; /* 16:9 宽高比 */
  position: relative;
  background-color: #000;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.video-player {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.video-info {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.video-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}

.view-count, .upload-time {
  display: flex;
  align-items: center;
  gap: 6px;
}

.video-actions {
  display: flex;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.is-liked {
  background-color: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.video-description {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.description-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.description-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.expand-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-description p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  max-height: 100px;
  overflow: hidden;
  transition: max-height 0.3s;
}

.video-description p.expanded {
  max-height: 1000px;
}

.author-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.author-avatar {
  margin-right: 16px;
}

.author-info {
  flex: 1;
}

.author-name {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 500;
}

.author-subscribers {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.comments-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.section-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.section-placeholder {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

.related-videos {
  width: 320px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  align-self: flex-start;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.related-video-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.related-video-item:hover {
  transform: translateY(-3px);
}

.related-video-thumbnail {
  width: 120px;
  height: 67.5px; /* 16:9 宽高比 */
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.related-video-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-video-info {
  flex: 1;
  min-width: 0; /* 确保文本可以正确截断 */
}

.related-video-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 6px;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.related-video-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-direction: column;
}

.related-video-placeholder {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .video-content {
    flex-direction: column;
  }
  
  .related-videos {
    width: 100%;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .video-detail-container {
    padding: 10px;
  }
  
  .video-title {
    font-size: 18px;
  }
  
  .video-actions {
    flex-wrap: wrap;
  }
  
  .video-actions .el-button {
    flex: 1;
  }
  
  .author-card {
    flex-direction: column;
    text-align: center;
  }
  
  .author-avatar {
    margin: 0 0 10px;
  }
}
</style> 