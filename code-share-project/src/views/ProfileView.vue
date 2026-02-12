<template>
  <div class="profile-page">
    <NavHeader />
    <div class="main-content container flex gap-20">
      <div class="profile-content">
        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="10" animated />
        
        <!-- 用户个人信息卡片 -->
        <el-card v-else class="user-info-card">
          <div class="user-header">
            <el-avatar :size="80" :src="profileData.user?.avatarUrl || defaultAvatar"></el-avatar>
            <div class="user-basic-info">
              <h2 class="username">{{ profileData.user?.username || '匿名用户' }}</h2>
              <div class="user-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ profileData.followCount || 0 }}</div>
                  <div class="stat-label">关注</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ profileData.fansCount || 0 }}</div>
                  <div class="stat-label">粉丝</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ getTotalContentCount() }}</div>
                  <div class="stat-label">内容数</div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="user-more-info">
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span>邮箱：{{ profileData.user?.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>注册时间：{{ formatDate(profileData.user?.createTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Timer /></el-icon>
              <span>最后登录：{{ formatDate(profileData.user?.lastLogin) }}</span>
            </div>
          </div>
        </el-card>
        
        <!-- 内容展示区域 -->
        <el-tabs v-model="activeTab" class="content-tabs" v-if="!loading">
          <!-- 博客列表 -->
          <el-tab-pane label="博客" name="blogs">
            <div v-if="profileData.blogsList?.length === 0" class="empty-content">
              <el-empty description="暂无博客内容" />
            </div>
            <div v-else class="table-container">
              <el-table :data="profileData.blogsList" style="width: 100%" stripe>
                <el-table-column prop="title" label="标题" min-width="180">
                  <template #default="scope">
                    <span class="table-title" @click="navigateTo('/article/' + scope.row.id)">{{ scope.row.title }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">
                      {{ scope.row.status === 1 ? '已发布' : '草稿' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="阅读量" width="100" align="center" />
                <el-table-column prop="likesCount" label="点赞数" width="100" align="center" />
                <el-table-column prop="createTime" label="创建时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleEdit('blog', scope.row)">修改</el-button>
                    <el-button size="small" type="danger" @click="handleDelete('blog', scope.row)">删除</el-button>
                    <el-button size="small" type="info" @click="handleViewHistory(scope.row)">历史版本</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 视频列表 -->
          <el-tab-pane label="视频" name="videos">
            <div v-if="profileData.videosList?.length === 0" class="empty-content">
              <el-empty description="暂无视频内容" />
            </div>
            <div v-else class="table-container">
              <el-table :data="profileData.videosList" style="width: 100%" stripe>
                <el-table-column prop="title" label="标题" min-width="180">
                  <template #default="scope">
                    <div class="video-title-cell" @click="navigateTo('/video/detail/' + scope.row.id)">
                      <el-image 
                        class="video-thumbnail" 
                        :src="scope.row.coverPath || defaultCover" 
                        fit="cover"
                      />
                      <span class="table-title">{{ scope.row.title }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="播放量" width="100" align="center" />
                <el-table-column prop="createTime" label="上传时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleEdit('video', scope.row)">修改</el-button>
                    <el-button size="small" type="danger" @click="handleDelete('video', scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 问答列表 -->
          <el-tab-pane label="问答" name="questions">
            <div v-if="profileData.questionsList?.length === 0" class="empty-content">
              <el-empty description="暂无问答内容" />
            </div>
            <div v-else class="table-container">
              <el-table :data="profileData.questionsList" style="width: 100%" stripe>
                <el-table-column prop="title" label="标题" min-width="180">
                  <template #default="scope">
                    <span class="table-title" @click="navigateTo('/question/detail/' + scope.row.id)">{{ scope.row.title }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="isResolved" label="状态" width="100">
                  <template #default="scope">
                    <el-tag size="small" :type="scope.row.isResolved === 1 ? 'success' : 'warning'">
                      {{ scope.row.isResolved === 1 ? '已解决' : '待解决' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="提问时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="240" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleEdit('question', scope.row)">修改</el-button>
                    <el-button size="small" type="danger" @click="handleDelete('question', scope.row)">删除</el-button>
                    <el-button 
                      v-if="scope.row.isResolved !== 1" 
                      size="small" 
                      type="success" 
                      @click="handleSolveQuestion(scope.row)"
                    >
                      设为已解决
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 投票列表 -->
          <el-tab-pane label="投票" name="votes">
            <div v-if="profileData.votesList?.length === 0" class="empty-content">
              <el-empty description="暂无投票内容" />
            </div>
            <div v-else class="table-container">
              <el-table :data="profileData.votesList" style="width: 100%" stripe>
                <el-table-column prop="title" label="标题" min-width="180">
                  <template #default="scope">
                    <span class="table-title" @click="navigateTo('/poll/detail/' + scope.row.id)">{{ scope.row.title }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="voteType" label="类型" width="100">
                  <template #default="scope">
                    <el-tag size="small" type="info">
                      {{ scope.row.voteType === 0 ? '单选' : '多选' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="endTime" label="状态" width="100">
                  <template #default="scope">
                    <el-tag size="small" :type="isPollExpired(scope.row.endTime) ? 'info' : 'success'">
                      {{ isPollExpired(scope.row.endTime) ? '已结束' : '进行中' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.endTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="danger" @click="handleDelete('vote', scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 关注列表 -->
          <el-tab-pane label="关注" name="follows">
            <div v-if="profileData.followList?.length === 0" class="empty-content">
              <el-empty description="暂无关注用户" />
            </div>
            <div v-else class="user-list">
              <el-card v-for="user in profileData.followList" :key="user.id" class="user-item" shadow="hover">
                <div class="user-brief">
                  <el-avatar :size="50" :src="user.avatarUrl || defaultAvatar"></el-avatar>
                  <div class="user-brief-info">
                    <h4 class="brief-username">{{ user.username }}</h4>
                    <p class="brief-email">{{ user.email }}</p>
                  </div>
                </div>
                <div class="user-actions">
                  <el-button 
                    size="small" 
                    type="primary" 
                    :loading="user.unfollowLoading" 
                    @click="handleUnfollow(user)"
                  >
                    <el-icon v-if="!user.unfollowLoading" class="el-icon--left"><Check /></el-icon>
                    {{ user.unfollowLoading ? '取消中...' : '已关注' }}
                  </el-button>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <!-- 历史版本对话框改为抽屉 -->
    <el-drawer
      v-model="historyDialogVisible"
      title="博客历史版本"
      size="60%"
      direction="rtl"
    >
      <template #header>
        <h4 class="drawer-title">
          博客历史版本 
          <span v-if="currentBlog" class="current-blog-title">- {{ currentBlog.title }}</span>
        </h4>
      </template>
      
      <div v-if="loadingHistory" class="history-loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="historyVersions.length === 0" class="history-empty">
        <el-empty description="暂无历史版本" />
      </div>
      <el-table v-else :data="historyVersions" style="width: 100%" stripe>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="version" label="版本" width="80">
          <template #default="scope">
            <el-tag 
              :type="scope.row.version === 0 ? 'info' : 'success'" 
              size="small"
            >
              v{{ scope.row.version }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="修改时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="浏览量" width="80" align="center">
          <template #default="scope">
            {{ scope.row.viewCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="viewBlogVersion(scope.row)"
            >
              查看
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              @click="confirmRestore(scope.row)"
              :disabled="isRestoringVersion"
            >
              还原
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  View, 
  Star, 
  Calendar, 
  Timer, 
  Message, 
  ChatDotRound,
  VideoPlay,
  Check
} from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import userService from '@/services/user'
import blogService from '@/services/blog'
import videoService from '@/services/video'
import questionService from '@/services/question'
import pollService from '@/services/poll'

const router = useRouter()
const loading = ref(true)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultCover = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'
const activeTab = ref('blogs')

// 用户个人中心数据
const profileData = reactive<any>({
  user: null,
  blogsList: [],
  videosList: [],
  questionsList: [],
  votesList: [],
  followList: [],
  fansList: [],
  followCount: 0,
  fansCount: 0
})

// 历史版本对话框
const historyDialogVisible = ref(false)
const historyVersions = ref<any[]>([])
const currentBlog = ref<any>(null)
const loadingHistory = ref(false)
const isRestoringVersion = ref(false)

// 加载个人中心数据
const loadProfileData = async () => {
  loading.value = true
  
  try {
    const res = await userService.getUserDetail()
    
    if (res.code === 0 && res.data) {
      // 更新个人资料数据
      profileData.user = res.data.user || null
      profileData.blogsList = res.data.blogsList || []
      profileData.videosList = res.data.videosList || []
      profileData.questionsList = res.data.questionsList || []
      profileData.votesList = res.data.votesList || []
      profileData.followList = res.data.followList || []
      profileData.fansList = res.data.fansList || []
      profileData.followCount = res.data.followCount || 0
      profileData.fansCount = res.data.fansCount || 0
      
      // 为关注列表中的每个用户添加加载状态
      profileData.followList.forEach((user: any) => {
        user.unfollowLoading = false
      })
    } else {
      ElMessage.error(res.message || '获取个人中心数据失败')
    }
  } catch (error) {
    console.error('加载个人中心数据失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取总内容数
const getTotalContentCount = () => {
  let total = 0
  if (profileData.blogsList) total += profileData.blogsList.length
  if (profileData.videosList) total += profileData.videosList.length
  if (profileData.questionsList) total += profileData.questionsList.length
  if (profileData.votesList) total += profileData.votesList.length
  return total
}

// 截断内容
const truncateContent = (content: string) => {
  if (!content) return ''
  
  // 去除markdown标记
  let plainText = content.replace(/#+\s|\*\*|\*|\[.*?\]\(.*?\)|```.*?```|`|>/g, '')
  
  // 截取前100个字符
  if (plainText.length > 100) {
    return plainText.slice(0, 100) + '...'
  }
  return plainText
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 检查投票是否过期
const isPollExpired = (endTimeStr: string) => {
  if (!endTimeStr) return false
  const endTime = new Date(endTimeStr)
  return endTime < new Date()
}

// 导航到详情页
const navigateTo = (path: string) => {
  router.push(path)
}

// 处理编辑操作
const handleEdit = (type: string, item: any) => {
  switch (type) {
    case 'blog':
      router.push(`/editor/blog?id=${item.id}`)
      break
    case 'video':
      router.push(`/video/edit/${item.id}`)
      break
    case 'question':
      router.push(`/question/edit/${item.id}`)
      break
  }
}

// 处理删除操作
const handleDelete = async (type: string, item: any) => {
  const typeNames = {
    blog: '博客',
    video: '视频',
    question: '问题',
    vote: '投票'
  }
  
  const typeName = typeNames[type as keyof typeof typeNames] || '内容'
  
  ElMessageBox.confirm(`确定要删除这个${typeName}吗？此操作不可恢复。`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      let response;
      
      // 根据内容类型调用相应的删除API
      switch (type) {
        case 'blog':
          response = await blogService.deleteBlog(item.id);
          break;
        case 'video':
          response = await videoService.deleteVideo(item.id);
          break;
        case 'question':
          // 使用问答服务的删除方法
          response = await questionService.deleteQuestion(item.id);
          break;
        case 'vote':
          // 实现投票删除API调用
          response = await pollService.deletePoll(item.id);
          break;
      }
      
      // 处理API响应
      if (response && response.code === 0) {
        ElMessage.success(`${typeName}已删除`);
        
        // 从列表中移除已删除的项
        switch (type) {
          case 'blog':
            profileData.blogsList = profileData.blogsList.filter((blog: any) => blog.id !== item.id);
            break;
          case 'video':
            profileData.videosList = profileData.videosList.filter((video: any) => video.id !== item.id);
            break;
          case 'question':
            profileData.questionsList = profileData.questionsList.filter((question: any) => question.id !== item.id);
            break;
          case 'vote':
            profileData.votesList = profileData.votesList.filter((vote: any) => vote.id !== item.id);
            break;
        }
      } else if (!response) {
        // 如果没有API响应（mock模式）
        ElMessage.success(`${typeName}已删除`);
        
        // 从列表中移除已删除的项
        switch (type) {
          case 'blog':
            profileData.blogsList = profileData.blogsList.filter((blog: any) => blog.id !== item.id);
            break;
          case 'video':
            profileData.videosList = profileData.videosList.filter((video: any) => video.id !== item.id);
            break;
          case 'question':
            profileData.questionsList = profileData.questionsList.filter((question: any) => question.id !== item.id);
            break;
          case 'vote':
            profileData.votesList = profileData.votesList.filter((vote: any) => vote.id !== item.id);
            break;
        }
      } else {
        ElMessage.error(response.message || `删除${typeName}失败`);
      }
    } catch (error) {
      console.error(`删除${typeName}出错:`, error);
      ElMessage.error(`删除${typeName}失败，请稍后重试`);
    }
  }).catch(() => {
    // 用户取消删除，不做任何操作
  });
}

// 设置问题为已解决
const handleSolveQuestion = async (question: any) => {
  try {
    ElMessageBox.confirm(
      '确定将问题设置为已解决状态吗？', 
      '操作确认', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(async () => {
      const response = await questionService.solveQuestion(question.id)
      
      if (response.code === 0) {
        ElMessage.success('问题已设置为已解决')
        
        // 更新本地问题状态
        const index = profileData.questionsList.findIndex((q: any) => q.id === question.id)
        if (index !== -1) {
          profileData.questionsList[index].isResolved = 1
        }
      } else {
        ElMessage.error(response.message || '操作失败，请稍后重试')
      }
    }).catch(() => {
      // 用户取消，不做任何处理
    })
  } catch (error) {
    console.error('设置问题已解决失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 处理查看历史版本
const handleViewHistory = async (blog: any) => {
  currentBlog.value = blog
  historyDialogVisible.value = true
  loadingHistory.value = true
  
  try {
    const res = await blogService.getBlogHistoryVersions(blog.id)
    
    if (res.code === 0 && res.data) {
      historyVersions.value = res.data
      
      // 按照版本号排序
      historyVersions.value.sort((a, b) => b.version - a.version)
    } else {
      ElMessage.error(res.message || '获取历史版本失败')
    }
  } catch (error) {
    console.error('获取历史版本失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loadingHistory.value = false
  }
}

// 跳转到博客详情查看指定版本
const viewBlogVersion = (blog: any) => {
  router.push(`/article/${blog.id}`)
  historyDialogVisible.value = false
}

// 确认还原历史版本
const confirmRestore = (blog: any) => {
  ElMessageBox.confirm(
    `确定要将博客还原到版本 v${blog.version} (${formatDate(blog.updateTime)}) 吗？此操作将覆盖当前最新版本。`,
    '还原确认',
    {
      confirmButtonText: '确定还原',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    restoreVersion(blog)
  }).catch(() => {
    // 用户取消，不执行任何操作
  })
}

// 执行还原操作
const restoreVersion = async (blog: any) => {
  isRestoringVersion.value = true
  
  try {
    const res = await blogService.restoreBlogVersion(blog.id)
    
    if (res.code === 0) {
      ElMessage.success('博客历史版本还原成功')
      
      // 刷新历史版本列表
      await handleViewHistory(currentBlog.value)
      
      // 刷新个人中心数据
      await loadProfileData()
    } else {
      ElMessage.error(res.message || '还原历史版本失败')
    }
  } catch (error) {
    console.error('还原历史版本失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    isRestoringVersion.value = false
  }
}

// 处理取消关注
const handleUnfollow = async (user: any) => {
  // 防止重复点击
  if (user.unfollowLoading) return
  
  // 设置用户特定的加载状态
  user.unfollowLoading = true
  
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm(
      `确定要取消关注 ${user.username} 吗？`,
      '取消关注',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用关注/取关接口
    const response = await userService.follow(user.id)
    
    if (response.code === 0) {
      // 显示成功消息
      ElMessage.success(`已取消关注 ${user.username}`)
      
      // 从关注列表中移除该用户
      profileData.followList = profileData.followList.filter((item: any) => item.id !== user.id)
      
      // 更新关注计数
      if (profileData.followCount > 0) {
        profileData.followCount--
      }
    } else {
      ElMessage.error(response.message || '取消关注失败')
    }
  } catch (error) {
    // 用户取消操作，不显示错误
    if (error !== 'cancel') {
      console.error('取消关注失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    // 重置加载状态
    user.unfollowLoading = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadProfileData()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  padding-top: 20px;
  flex: 1;
  justify-content: center;
}

.profile-content {
  width: 100%;
  max-width: 70% ;
  margin: 0 auto;
}

.user-info-card {
  margin-bottom: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.user-basic-info {
  margin-left: 20px;
  flex: 1;
}

.username {
  margin: 0 0 10px;
  font-size: 22px;
  color: #303133;
}

.user-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.user-more-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #606266;
}

.info-item .el-icon {
  font-size: 16px;
  color: #909399;
}

.content-tabs {
  margin-top: 20px;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.content-item {
  cursor: pointer;
  transition: all 0.3s;
}

.content-item:hover {
  transform: translateY(-2px);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.content-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.content-brief {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.content-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-right: 15px;
}

.meta-item .el-icon {
  margin-right: 4px;
  font-size: 14px;
}

.meta-date {
  margin-left: auto;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.video-item {
  cursor: pointer;
}

.video-cover {
  position: relative;
  height: 140px;
  overflow: hidden;
  border-radius: 4px;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 2px 6px;
  font-size: 12px;
  border-radius: 4px;
}

.video-info {
  padding: 10px 0;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
}

.user-brief {
  display: flex;
  align-items: center;
}

.user-brief-info {
  margin-left: 15px;
}

.brief-username {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.brief-email {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.empty-content {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.table-container {
  margin-top: 20px;
}

.table-title {
  color: #409EFF;
  cursor: pointer;
  transition: color 0.3s;
}

.table-title:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.video-title-cell {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.video-thumbnail {
  width: 60px;
  height: 40px;
  margin-right: 10px;
  border-radius: 4px;
  object-fit: cover;
}

/* 表格响应式处理 */
@media (max-width: 768px) {
  .user-header {
    flex-direction: column;
    text-align: center;
  }
  
  .user-basic-info {
    margin-left: 0;
    margin-top: 15px;
  }
  
  .user-stats {
    justify-content: center;
  }
  
  .user-more-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .meta-date {
    display: none;
  }
  
  .video-grid {
    grid-template-columns: 1fr;
  }
  
  .user-list {
    grid-template-columns: 1fr;
  }
  
  .user-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .user-actions {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
  
  .el-table {
    width: 100%;
    overflow-x: auto;
  }
}

.history-loading, .history-empty {
  padding: 20px 0;
}

.drawer-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.current-blog-title {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
  margin-left: 5px;
}
</style> 