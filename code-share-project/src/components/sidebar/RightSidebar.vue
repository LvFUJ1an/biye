<template>
  <div class="right-sidebar">
    <!-- 排行榜区域 -->
    <div class="sidebar-card ranking-card">
      <div class="card-header">
        <h3 class="card-title">热门榜单</h3>
        <el-tabs v-model="activeRanking" class="ranking-tabs">
          <el-tab-pane label="博客榜" name="blogs"></el-tab-pane>
          <el-tab-pane label="作者榜" name="authors"></el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="ranking-list" v-if="activeRanking === 'blogs'">
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else-if="hotBlogs.length === 0" class="empty-data">
          暂无数据
        </div>
        <div v-else v-for="(blog, index) in hotBlogs" :key="blog.id" class="ranking-item" @click="navigateToBlog(blog.id)">
          <div class="ranking-number" :class="{ 'top-three': index < 3 }">
            {{ index + 1 }}
          </div>
          <div class="ranking-content">
            <div class="ranking-title">{{ blog.title }}</div>
            <div class="ranking-stats flex align-center gap-10">
              <span class="view-count flex align-center">
                <el-icon><View /></el-icon>
                {{ blog.viewCount }}
              </span>
              <span class="likes-count flex align-center">
                <el-icon><Star /></el-icon>
                {{ blog.likesCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="ranking-list" v-else>
        <el-skeleton v-if="loading" :rows="5" animated />
        <div v-else-if="hotAuthors.length === 0" class="empty-data">
          暂无数据
        </div>
        <div v-else v-for="(author, index) in hotAuthors" :key="author.id" class="ranking-item author-item flex align-center">
          <div class="ranking-number" :class="{ 'top-three': index < 3 }">
            {{ index + 1 }}
          </div>
          <el-avatar :size="30" :src="author.avatarUrl || defaultAvatar"></el-avatar>
          <div class="author-info">
            <div class="author-name">{{ author.username }}</div>
            <div class="author-stats">用户ID：{{ author.id }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 广告区域 -->
    <div class="sidebar-card ad-card">
      <div class="card-header">
        <h3 class="card-title">推荐资源</h3>
      </div>
      <div class="ad-content">
        <a href="#" class="ad-item">
          <img src="https://picsum.photos/id/1/300/200" alt="广告" class="ad-image" />
          <div class="ad-title">Vue.js高级开发教程</div>
        </a>
      </div>
    </div>
    
    <!-- 活动通知区域 -->
    <div class="sidebar-card event-card">
      <div class="card-header">
        <h3 class="card-title">活动通知</h3>
      </div>
      <div class="event-list">
        <div v-for="(event, index) in events" :key="index" class="event-item">
          <div class="event-title">{{ event.title }}</div>
          <div class="event-time">{{ event.time }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { View, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import blogService from '@/services/blog'

const router = useRouter()
const activeRanking = ref('blogs')
const loading = ref(true)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 热门博客列表
const hotBlogs = ref<any[]>([])

// 热门作者列表
const hotAuthors = ref<any[]>([])

// 活动列表（保持静态数据）
const events = ref([
  { title: '前端技术沙龙：Vue3实战经验分享', time: '2023-10-15 14:00' },
  { title: '程序员职业发展线上讲座', time: '2023-10-20 19:30' },
  { title: '2023年度开发者大会', time: '2023-11-05 09:00' }
])

// 获取排行榜数据
const fetchRankingData = async () => {
  loading.value = true
  try {
    const res = await blogService.getBlogRank()
    
    if (res.code === 0 && res.data) {
      // 更新热门博客列表
      hotBlogs.value = res.data.blogsRanks || []
      
      // 更新热门作者列表（过滤掉null值）
      hotAuthors.value = (res.data.usersRanks || []).filter(author => author !== null)
    } else {
      ElMessage.error(res.message || '获取排行榜数据失败')
    }
  } catch (error) {
    console.error('获取排行榜数据失败:', error)
    ElMessage.error('获取排行榜数据失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 导航到博客详情页
const navigateToBlog = (id: number | string) => {
  router.push(`/article/${id}`)
}

// 组件挂载时加载数据
onMounted(() => {
  fetchRankingData()
})
</script>

<style scoped>
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
}

.ranking-tabs {
  margin-top: 8px;
}

.ranking-list {
  padding: 8px 0;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  transition: background-color 0.3s;
  cursor: pointer;
}

.ranking-item:hover {
  background-color: #f5f7fa;
}

.ranking-number {
  font-size: 14px;
  font-weight: bold;
  color: #909399;
  width: 24px;
  text-align: center;
  margin-right: 8px;
}

.ranking-number.top-three {
  color: #ff6b6b;
}

.ranking-content {
  flex: 1;
}

.ranking-title {
  font-size: 14px;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.ranking-stats {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.ranking-stats .el-icon {
  margin-right: 4px;
  font-size: 14px;
}

.author-item {
  gap: 8px;
}

.author-info {
  flex: 1;
  overflow: hidden;
}

.author-name {
  font-size: 14px;
  color: var(--text-color);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author-stats {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.ad-content {
  padding: 16px;
}

.ad-item {
  display: block;
  text-decoration: none;
}

.ad-image {
  width: 100%;
  border-radius: 6px;
  transition: opacity 0.3s;
}

.ad-image:hover {
  opacity: 0.9;
}

.ad-title {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-color);
  text-align: center;
}

.event-list {
  padding: 8px 16px;
}

.event-item {
  padding: 8px 0;
  border-bottom: 1px dashed var(--border-color);
}

.event-item:last-child {
  border-bottom: none;
}

.event-title {
  font-size: 14px;
  color: var(--text-color);
  margin-bottom: 4px;
}

.event-time {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.empty-data {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}
</style> 