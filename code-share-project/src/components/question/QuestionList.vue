<template>
  <div class="question-list-container">
    <!-- 问题列表头部 -->
    <div class="question-list-header">
      <div class="title-area">
        <h2>问答社区</h2>
        <p class="subtitle">解决技术难题，分享专业知识</p>
      </div>
      <div class="filter-area">
        <el-select v-model="orderBy" placeholder="排序方式" size="large" @change="handleFilterChange">
          <el-option label="最新发布" value="createTime" />
          <el-option label="最多浏览" value="viewCount" />
          <el-option label="最多回答" value="answerCount" />
        </el-select>
<!--         <el-button type="primary" size="large" @click="handleAskQuestion">
          <el-icon><EditPen /></el-icon>我要提问
        </el-button> -->
      </div>
    </div>

    <!-- 问题搜索 -->
    <div class="search-area">
      <el-input
        v-model="keyword"
        placeholder="搜索问题..."
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 问题列表 -->
    <div class="question-list">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <template v-else-if="questionList.length > 0">
        <div v-for="question in questionList" :key="question.id" class="question-item" @click="goToDetail(question.id)">
          <div class="question-main">
            <div class="question-title-row">
              <h3 class="question-title">{{ question.title }}</h3>
              <el-tag v-if="question.isResolved == 1" type="success" size="small">已解决</el-tag>
              <el-tag v-else type="info" size="small">未解决</el-tag>
            </div>
            <div class="question-summary" v-html="formatContent(question.content)"></div>
            <div class="question-tags">
              <el-tag v-for="(tag, index) in question.tags" :key="index" size="small" effect="plain" class="tag">{{ tag }}</el-tag>
            </div>
          </div>
          <div class="question-meta">
            <div class="author-info">
              <el-avatar :size="40" :src="question.avatarUrl || defaultAvatar"></el-avatar>
              <div class="author-details">
                <span class="author-name">{{ question.username }}</span>
                <span class="post-time">{{ formatTime(question.createTime) }}</span>
              </div>
            </div>
            <div class="stats">
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ question.viewCount }}</span>
              </div>
              <div class="stat-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ question.answerCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无问题数据" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { EditPen, Search, View, ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import questionService from '@/services/question'
import type { Question } from '@/services/question'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const md = new MarkdownIt({
  html: true,
  breaks: true,
  linkify: true
})

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 问题列表状态
const questionList = ref<Question[]>([])
const loading = ref(true)
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const keyword = ref('')
const orderBy = ref('createTime')
const orderDirection = ref('DESC')

// 获取问题列表
const fetchQuestionList = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value.toString(),
      size: pageSize.value.toString(),
      keyword: keyword.value,
      orderBy: orderBy.value,
      orderDirection: orderDirection.value
    }
    
    const res = await questionService.getQuestionList(params)
    questionList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取问题列表失败:', error)
    ElMessage.error('获取问题列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 格式化问题内容（取摘要）
const formatContent = (content: string) => {
  // 先移除Markdown标记，只保留文本内容
  const plainText = content.replace(/#+\s/g, '') // 移除标题
                         .replace(/\*\*(.*?)\*\*/g, '$1') // 移除加粗
                         .replace(/\*(.*?)\*/g, '$1') // 移除斜体
                         .replace(/\[(.*?)\]\((.*?)\)/g, '$1') // 移除链接
                         .replace(/`{1,3}([\s\S]*?)`{1,3}/g, '$1') // 移除代码块
                         .replace(/!\[(.*?)\]\((.*?)\)/g, '') // 移除图片
  
  // 截取前100个字符作为摘要
  const summary = plainText.length > 100 
    ? plainText.substring(0, 100) + '...' 
    : plainText
  
  return summary
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

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchQuestionList()
}

// 处理过滤条件变更
const handleFilterChange = () => {
  currentPage.value = 1
  fetchQuestionList()
}

// 处理分页变更
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchQuestionList()
}

// 跳转到问题详情
const goToDetail = (id: number) => {
  router.push(`/question/detail/${id}`)
}

// 处理提问按钮
const handleAskQuestion = () => {
  // 检查用户是否登录
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再提问')
    return
  }
  
  router.push('/question/ask')
}

// 初始加载
onMounted(() => {
  fetchQuestionList()
})
</script>

<style scoped>
.question-list-container {
  width: 100%;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.question-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.title-area h2 {
  font-size: 22px;
  margin: 0;
  color: #333;
}

.subtitle {
  color: #666;
  margin: 5px 0 0;
  font-size: 14px;
}

.filter-area {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-area {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.search-input {
  flex: 1;
}

.question-list {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px 0;
}

.question-item {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-item:last-child {
  border-bottom: none;
}

.question-item:hover {
  background-color: #f9f9f9;
}

.question-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #333;
  flex: 1;
}

.question-summary {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 5px;
}

.tag {
  font-size: 12px;
}

.question-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .question-list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .filter-area {
    width: 100%;
  }
  
  .search-area {
    flex-direction: column;
  }
}
</style> 