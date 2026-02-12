<template>
  <div class="question-detail-container">
    <NavHeader />
    <div class="main-content container flex gap-20">
      <CategorySidebar class="sidebar" />
      <div class="content-area">
        <!-- 加载中状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton animated>
            <template #template>
              <div class="skeleton-container">
                <el-skeleton-item variant="p" style="width: 60%; height: 30px;" />
                <el-skeleton-item variant="text" style="width: 30%; margin-top: 10px;" />
                <el-skeleton-item variant="p" style="width: 100%; height: 200px; margin-top: 20px;" />
              </div>
            </template>
          </el-skeleton>
        </div>
        
        <!-- 问题详情 -->
        <div v-else class="question-detail">
          <div class="question-header">
            <h1 class="question-title">{{ question.title }}</h1>
            <div class="question-meta">
              <span class="post-time">发布于 {{ formatTime(question.createTime) }}</span>
              <el-tag v-if="question.isResolved === 1" type="success" effect="dark" size="small">已解决</el-tag>
              <el-tag v-else type="info" effect="plain" size="small">未解决</el-tag>
            </div>
          </div>
          
          <div class="question-content markdown-body" v-html="renderedContent"></div>
          
          <div class="question-author">
            <div class="author-info">
              <el-avatar :size="40" :src="questionAuthor.avatarUrl"></el-avatar>
              <div class="author-name">{{ questionAuthor.username }}</div>
            </div>
          </div>
          
          <!-- 回答内容 -->
          <div class="answers-section">
            <div class="answers-header">
              <h2>
                <span>{{ question.answers ? question.answers.length : 0 }}</span> 个回答
              </h2>
              <!-- <el-radio-group v-model="answerSortBy" size="small">
                <el-radio-button label="latest">最新</el-radio-button>
                <el-radio-button label="popular">最受欢迎</el-radio-button>
              </el-radio-group> -->
            </div>
            
            <!-- 回答列表 -->
            <div v-if="answers.length > 0" class="answer-list">
              <div v-for="answer in sortedAnswers" :key="answer.id" class="answer-item">
                <div class="answer-author">
                  <el-avatar :size="40" :src="answer.avatarUrl || getAuthorAvatar(answer.userId)"></el-avatar>
                  <div class="author-info">
                    <div class="author-name">{{ answer.username || getAuthorName(answer.userId) }}</div>
                    <div class="answer-time">{{ formatTime(answer.createTime) }}</div>
                  </div>
                </div>
                
                <div class="answer-content markdown-body" v-html="renderMarkdown(answer.content)"></div>
                
                <div class="answer-actions">
                  <div class="action-item">
                    <el-button 
                      type="primary" 
                      size="small" 
                      :plain="!answer.isAccepted" 
                      :disabled="question.isResolved === 1 && !answer.isAccepted"
                      @click="handleAcceptAnswer(answer.id)"
                      v-if="isCurrentUserQuestion"
                    >
                      <el-icon><Check /></el-icon> 
                      {{ answer.isAccepted ? '已采纳' : '采纳回答' }}
                    </el-button>
                    <div v-if="answer.isAccepted" class="accepted-stamp">已采纳</div>
                  </div>
                  
                  <div class="action-item">
                    <!-- <el-button 
                      size="small" 
                      plain 
                      @click="handleLikeAnswer(answer.id)"
                    >
                      <el-icon><Star /></el-icon>
                      有用 ({{ answer.likeCount }})
                    </el-button> -->
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无回答" />
            
            <!-- 编写回答 -->
            <div class="write-answer" v-if="isLoggedIn">
              <h3>撰写回答</h3>
              <el-form @submit.prevent="submitAnswer">
                <el-form-item>
                  <el-input
                    v-model="newAnswer"
                    type="textarea"
                    :rows="6"
                    placeholder="请输入回答内容..."
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitAnswer" :loading="submitting">提交回答</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div v-else class="login-notice">
              <el-alert
                title="请登录后回答问题"
                type="info"
                :closable="false"
                center
              >
                <template #default>
                  <el-button type="primary" size="small" @click="handleLogin">登录</el-button>
                </template>
              </el-alert>
            </div>
          </div>
        </div>
      </div>
      <RightSidebar class="right-sidebar" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavHeader from '@/components/layout/NavHeader.vue'
import CategorySidebar from '@/components/sidebar/CategorySidebar.vue'
import RightSidebar from '@/components/sidebar/RightSidebar.vue'
import MarkdownEditor from '@/components/markdown/MarkdownEdit.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Star } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import questionService from '@/services/question'

const route = useRoute()
const router = useRouter()
const md = new MarkdownIt({
  html: true,
  breaks: true,
  linkify: true
})

// 问题详情数据
const loading = ref(true)
const question = ref<any>({})
const answers = ref<any[]>([])
const answerSortBy = ref('latest')
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const userName = ref('匿名用户')
const newAnswer = ref('')
const submitting = ref(false)
const isLoggedIn = ref(false)
const currentUser = ref<any>(null)

// 当前用户是否是问题提问者
const isCurrentUserQuestion = computed(() => {
  if (!currentUser.value || !question.value) return false
  return currentUser.value.id === question.value.userId
})

// 排序后的回答列表
const sortedAnswers = computed(() => {
  if (!answers.value.length) return []
  
  return [...answers.value].sort((a, b) => {
    // 已采纳的回答优先显示
    if (a.isAccepted && !b.isAccepted) return -1
    if (!a.isAccepted && b.isAccepted) return 1
    
    // 根据排序方式排序
    if (answerSortBy.value === 'latest') {
      return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
    } else {
      return b.likeCount - a.likeCount
    }
  })
})

// 渲染后的问题内容
const renderedContent = computed(() => {
  if (!question.value.content) return ''
  return renderMarkdown(question.value.content)
})

// 获取问题作者信息
const questionAuthor = computed(() => {
  if (!question.value || !question.value.user) return {
    username: '匿名用户',
    avatarUrl: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  
  return {
    username: question.value.user.username || '匿名用户',
    avatarUrl: question.value.user.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
})

// 渲染Markdown内容
const renderMarkdown = (content: string) => {
  if (!content) return ''
  return md.render(content)
}

// 格式化时间
const formatTime = (timeStr: string) => {
  try {
    const date = new Date(timeStr)
    return formatDistanceToNow(date, { addSuffix: true, locale: zhCN })
  } catch (error) {
    return timeStr
  }
}

// 获取作者头像
const getAuthorAvatar = (userId: number) => {
  // 这里需要根据实际情况，从用户服务获取头像
  return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
}

// 获取作者名称
const getAuthorName = (userId: number) => {
  // 这里需要根据实际情况，从用户服务获取用户名
  return '用户' + userId
}

// 加载问题详情
const loadQuestionDetail = async () => {
  loading.value = true
  try {
    const id = parseInt(route.params.id as string)
    const response = await questionService.getQuestionDetail(id)
    question.value = response.data
    
    // 设置回答列表
    if (question.value.answers) {
      answers.value = question.value.answers
    }
  } catch (error) {
    console.error('加载问题详情失败:', error)
    ElMessage.error('加载问题详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理采纳回答
const handleAcceptAnswer = async (answerId: number) => {
  try {
    const response = await questionService.acceptAnswer(answerId)
    
    if (response.code === 0) {
      ElMessage.success('已采纳该回答')
      
      // 更新本地状态
      const targetAnswer = answers.value.find(a => a.id === answerId)
      if (targetAnswer) {
        targetAnswer.isAccepted = true
      }
      
      // 注意：我们允许多个回答被采纳，所以不需要将其他回答标记为未采纳
    } else {
      ElMessage.error(response.message || '采纳回答失败')
    }
  } catch (error) {
    console.error('采纳回答失败:', error)
    ElMessage.error('采纳回答失败，请稍后重试')
  }
}

// 处理点赞回答
const handleLikeAnswer = async (answerId: number) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  try {
    await questionService.likeAnswer(answerId)
    
    // 更新本地点赞数
    answers.value = answers.value.map(answer => {
      if (answer.id === answerId) {
        return {
          ...answer,
          likeCount: answer.likeCount + 1
        }
      }
      return answer
    })
    
    ElMessage.success('点赞成功')
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败，请稍后重试')
  }
}

// 提交回答
const submitAnswer = async () => {
  if (!newAnswer.value.trim()) {
    ElMessage.warning('回答内容不能为空')
    return
  }
  
  submitting.value = true
  try {
    const response = await questionService.createAnswer({
      questionId: parseInt(route.params.id as string),
      content: newAnswer.value
    })
    
    if (response.code === 0) {
      ElMessage.success('回答已提交')
      
      // 刷新问题详情，获取最新回答列表
      await loadQuestionDetail()
      
      // 清空回答内容
      newAnswer.value = ''
    } else {
      ElMessage.error(response.message || '提交回答失败')
    }
  } catch (error) {
    console.error('提交回答失败:', error)
    ElMessage.error('提交回答失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 处理登录
const handleLogin = () => {
  // 存储当前页面路径，登录后返回
  router.push({
    path: '/',
    query: { redirect: route.fullPath }
  })
}

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const user = localStorage.getItem('user')
  
  if (token && user) {
    try {
      isLoggedIn.value = true
      currentUser.value = JSON.parse(user)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      isLoggedIn.value = false
      currentUser.value = null
    }
  } else {
    isLoggedIn.value = false
    currentUser.value = null
  }
}

// 初始化
onMounted(() => {
  checkLoginStatus()
  loadQuestionDetail()
})
</script>

<style scoped>
.question-detail-container {
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

.content-area {
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

.loading-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.skeleton-container {
  padding: 20px;
}

.question-detail {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 20px;
}

.question-header {
  margin-bottom: 20px;
}

.question-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #333;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #999;
  font-size: 14px;
}

.post-time {
  color: #999;
}

.question-content {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.question-author {
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.answers-section {
  margin-top: 30px;
}

.answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.answers-header h2 {
  font-size: 18px;
  margin: 0;
}

.answers-header h2 span {
  color: var(--primary-color);
  font-weight: 600;
}

.answer-list {
  margin-bottom: 30px;
}

.answer-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.answer-item:last-child {
  border-bottom: none;
}

.answer-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.answer-time {
  font-size: 12px;
  color: #999;
}

.answer-content {
  margin-bottom: 15px;
}

.answer-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.action-item {
  display: flex;
  align-items: center;
}

.write-answer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.write-answer h3 {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 20px;
}

.login-notice {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 全局样式覆盖 */
:deep(.markdown-body) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  color: #333;
}

:deep(.markdown-body pre) {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow: auto;
}

:deep(.markdown-body code) {
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
  background-color: rgba(27, 31, 35, 0.05);
  padding: 0.2em 0.4em;
  border-radius: 3px;
}

:deep(.markdown-body pre code) {
  background-color: transparent;
  padding: 0;
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
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .answers-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

.accepted-stamp {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #f56c6c;
  border: 2px solid #f56c6c;
  padding: 5px 10px;
  font-size: 14px;
  font-weight: bold;
  transform: rotate(15deg);
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style> 