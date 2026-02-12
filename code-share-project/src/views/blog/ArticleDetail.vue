<template>
  <div class="article-page">
    <NavHeader />
    
    <div class="article-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-animation"></div>
        <p class="loading-text">正在加载文章内容...</p>
      </div>
      
      <!-- 加载错误 -->
      <div v-else-if="error" class="error-container">
        <el-result icon="error" :title="error" sub-title="文章加载失败，请稍后重试">
          <template #extra>
            <el-button type="primary" @click="fetchArticleDetail">重试</el-button>
            <el-button @click="$router.push('/')">返回首页</el-button>
          </template>
        </el-result>
      </div>
      
      <!-- 文章内容 -->
      <div v-else-if="article" class="article-content-container">
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          
          <div class="article-meta">
            <div class="author-info">
              <el-avatar :size="40" :src="article.authorAvatar || defaultAvatar"></el-avatar>
              <div class="author-details">
                <span class="author-name">{{ article.authorName || '匿名用户' }}</span>
                <span class="publish-time">{{ formatDate(article.createTime) }}</span>
              </div>
            </div>
            
            <div class="article-stats">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ formatNumber(article.viewCount || 0) }} 阅读</span>
              </span>
              <span class="stat-item like-stat" @click="handleLike" v-if="isLoggedIn">
                <el-icon :class="{ 'liked-icon': isLiked }"><Star /></el-icon>
                <span :class="{ 'liked-text': isLiked }">{{ formatNumber(article.likesCount || 0) }} 点赞</span>
              </span>
              <span class="stat-item" v-else>
                <el-icon><Star /></el-icon>
                <span>{{ formatNumber(article.likesCount || 0) }} 点赞</span>
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ formatNumber(article.commentsCount || 0) }} 评论</span>
              </span>
            </div>
          </div>
          
          <div class="article-category">
            <el-tag size="small" type="success">{{ article.sectionName || '未分类' }}</el-tag>
          </div>
        </div>
        
        <div class="article-divider"></div>
        
        <div class="article-body">
          <markdown-preview :value="article.content || ''" />
        </div>
        
        <div class="article-actions">
          <el-button 
            type="primary" 
            :icon="isLiked ? 'Star' : 'star'" 
            size="large" 
            :class="{ 'is-liked': isLiked }"
            @click="handleLike"
            round
          >
            {{ article.isLiked ? '已点赞' : '点赞' }} {{ formatNumber(article.likesCount || 0) }}
          </el-button>
        </div>
        
        <div class="article-author-card">
          <div class="author-card-avatar">
            <el-avatar :size="60" :src="article.authorAvatar || defaultAvatar"></el-avatar>
          </div>
          <div class="author-card-info">
            <h3 class="author-card-name">{{ article.authorName || '匿名用户' }}</h3>
            <p class="author-card-bio">{{ article.authorBio || '这个人很懒，还没有填写个人简介' }}</p>
          </div>
          <div class="author-card-actions">
            <el-button 
              :type="article.isFollow ? 'default' : 'primary'" 
              size="small"
              @click="handleFollowAuthor"
              :loading="followLoading"
              v-if="isLoggedIn && article.authorId !== parseInt(userId)"
            >
              {{ article.isFollow ? '已关注' : '关注作者' }}
            </el-button>
          </div>
        </div>
        
        <!-- 评论区 -->
        <div class="article-comments">
          <h2 class="comments-title">评论 ({{ article.commentCount || 0 }})</h2>
          
          <!-- 评论输入框 -->
          <div class="comment-input-container">
            <el-avatar :size="40" :src="userAvatar || defaultAvatar"></el-avatar>
            <div class="comment-input-wrapper">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="添加评论..."
                :disabled="!isLoggedIn || commentSubmitting"
                maxlength="1000"
                show-word-limit
              />
              <div class="comment-preview" v-if="commentContent">
                <div class="preview-title">预览</div>
                <div class="preview-content">{{ commentContent }}</div>
              </div>
              <div class="comment-actions">
                <el-button 
                  type="primary" 
                  :disabled="!commentContent || !isLoggedIn || commentSubmitting" 
                  @click="submitComment"
                  :loading="commentSubmitting"
                >
                  发表评论
                </el-button>
                <span v-if="!isLoggedIn" class="login-tip">请先<a @click="handleLogin">登录</a>后发表评论</span>
              </div>
            </div>
          </div>
          
          <!-- 评论列表 -->
          <div v-if="article.comments && article.comments.length > 0" class="comments-list">
            <comment-item 
              v-for="comment in article.comments" 
              :key="comment.id" 
              :comment="comment"
              :default-avatar="defaultAvatar"
              :current-user-id="parseInt(userId)"
              @reply="handleReply"
              @delete="handleDeleteComment"
              @like="handleLikeComment"
            />
          </div>
          
          <!-- 无评论展示 -->
          <el-empty
            v-else
            description="暂无评论，快来发表第一条评论吧"
            :image-size="100"
          >
            <template #image>
              <el-icon size="40" color="#909399"><ChatLineRound /></el-icon>
            </template>
          </el-empty>
        </div>
      </div>
      
      <!-- 没有数据 -->
      <div v-else class="no-data-container">
        <el-empty description="未找到文章信息" />
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </div>
    </div>
    
    <!-- 点赞成功动画 -->
    <div class="like-animation" v-if="showLikeAnimation">
      <el-icon class="like-icon"><Star /></el-icon>
    </div>
    
    <!-- 返回顶部按钮 -->
    <el-backtop :right="20" :bottom="20" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Star, Share, CollectionTag, StarFilled, ChatLineRound } from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import MarkdownPreview from '@/components/markdown/MarkdownPreview.vue'
import api from '@/services/api'
import chatService from '@/services/chat'
import CommentItem from './components/CommentItem.vue'

const route = useRoute()
const router = useRouter()
const articleId = computed(() => route.params.id as string)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 文章数据
const article = ref<any>(null)
const loading = ref(true)
const error = ref('')

// 交互状态
const isLiked = ref(false)

// 用户数据
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const userId = computed(() => JSON.parse(localStorage.getItem('user') || '{}').id || '')
const userAvatar = computed(() => localStorage.getItem('userAvatar') || '')

// 评论相关状态
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref<number | null>(null)
const replyingToUser = ref<string>('')
const commentSubmitting = ref(false) // 添加评论提交状态
const replySubmitting = ref(false) // 添加回复提交状态
const deleteLoading = ref(false) // 删除评论的加载状态
const currentReplyComment = ref<any>(null) // 保存当前回复的评论对象

// 点赞动画状态
const showLikeAnimation = ref(false)

// 关注状态
const followLoading = ref(false)

// 获取文章详情
const fetchArticleDetail = async () => {
  if (!articleId.value) {
    error.value = '文章ID无效'
    loading.value = false
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await api.get(`/blog/detail/${articleId.value}`)
    
    if (response.code === 0 && response.data) {
      article.value = response.data
      
      // 更新页面标题
      document.title = `${article.value.title} - IT技术社区`
      
      // 将字符串的isLiked转为布尔值
      if (typeof article.value.isLiked === 'string') {
        article.value.isLiked = article.value.isLiked.toLowerCase() === 'true'
      }
      
      // 设置点赞状态
      isLiked.value = article.value.isLiked || false
    } else {
      error.value = response.message || '获取文章详情失败'
    }
  } catch (err) {
    console.error('获取文章详情出错:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 处理点赞
const handleLike = async (event?: Event) => {
  // 如果是从统计信息点击，阻止事件冒泡
  if (event) {
    event.stopPropagation()
  }
  
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  if (isLiked.value) {
    ElMessage.info('您已经点过赞了')
    return
  }
  
  try {
    const response = await api.post(`/blog/like/${articleId.value}`)
    
    if (response.code === 0) {
      isLiked.value = true
      if (article.value) {
        article.value.likesCount = (article.value.likesCount || 0) + 1
      }
      
      // 显示点赞动画
      showLikeAnimation.value = true
      setTimeout(() => {
        showLikeAnimation.value = false
      }, 1500)
      
      ElMessage.success('点赞成功')
    } else {
      ElMessage.error(response.message || '点赞失败')
    }
  } catch (error) {
    console.error('点赞请求出错:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化数字
const formatNumber = (num: number | string) => {
  const n = Number(num)
  if (isNaN(n)) return '0'
  
  if (n >= 1000) {
    return (n / 1000).toFixed(1) + 'k'
  }
  
  return n.toString()
}

// 处理登录
const handleLogin = () => {
  // 此处可以触发登录弹窗
  ElMessage.info('请先登录后再评论')
  // 或者导航到登录页
  // router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
}

// 检查当前用户是否为评论的作者
const isCommentAuthor = (comment: any): boolean => {
  if (!isLoggedIn.value) return false

  
  
  const currentUserId = parseInt(userId.value)
  return comment.userId === currentUserId
}

// 处理删除评论
const handleDeleteComment = async (comment: any) => {
  if (!isLoggedIn.value) {
    handleLogin()
    return
  }
  
  if (!isCommentAuthor(comment)) {
    ElMessage.warning('只能删除自己发布的评论')
    return
  }
  
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后将无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    // 用户确认后执行删除操作
    deleteLoading.value = true
    
    const response = await api.del(`/comment/${comment.id}`)
    
    if (response.code === 0) {
      ElMessage.success('评论已成功删除')
      
      // 删除评论后通知被回复的用户或文章作者
      if (article.value && article.value.authorId) {
        const targetUserId = article.value.authorId
        
        // 如果当前登录用户不是文章作者，则向文章作者发送评论通知
        if (parseInt(userId.value) !== targetUserId) {
          chatService.sendCommentNotification(
            targetUserId,
            '评论已被删除',
            'blog',
            parseInt(articleId.value)
          )
        }
      }
      
      // 刷新文章数据以更新评论列表
      fetchArticleDetail()
    } else {
      ElMessage.error(response.message || '删除评论失败')
    }
  } catch (error) {
    // 用户取消删除或发生错误
    if (error !== 'cancel') {
      console.error('删除评论出错:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    deleteLoading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!isLoggedIn.value) {
    handleLogin()
    return
  }
  
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  // 设置提交状态，防止重复提交
  commentSubmitting.value = true
  
  try {
    const response = await api.post('/comment/create', {
      content: commentContent.value,
      targetId: articleId.value,
      parentId: 0 // 顶级评论
    })
    
    if (response.code === 0) {
      ElMessage.success('评论发布成功')
      // 清空输入框
      commentContent.value = ''
      
      // 如果当前登录用户不是文章作者，则向文章作者发送评论通知
      if (article.value && article.value.authorId && parseInt(userId.value) !== article.value.authorId) {
        chatService.sendCommentNotification(
          article.value.authorId,
          commentContent.value,
          'blog',
          parseInt(articleId.value)
        )
      }
      
      // 刷新文章数据以获取最新评论
      fetchArticleDetail()
    } else {
      ElMessage.error(response.message || '评论发布失败')
    }
  } catch (err) {
    console.error('发布评论出错:', err)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    commentSubmitting.value = false
  }
}

// 处理回复评论
const handleReply = (comment: any) => {
  if (!isLoggedIn.value) {
    handleLogin()
    return
  }
  
  // 处理从组件传递过来的带有replyContent的评论对象
  if (comment.replyContent) {
    // 保存当前回复的评论对象(不包含replyContent属性)
    const { replyContent, ...commentData } = comment;
    currentReplyComment.value = commentData;
    
    // 自动提交回复
    submitCommentReply(commentData, replyContent);
    return;
  }
  
  // 直接显示回复框的情况
  currentReplyComment.value = comment
  replyingTo.value = comment.id
  replyingToUser.value = comment.username || '匿名用户'
  replyContent.value = `@${replyingToUser.value} `
}

// 使用提供的内容提交回复
const submitCommentReply = async (comment: any, content: string) => {
  if (!isLoggedIn.value) {
    handleLogin()
    return
  }
  
  if (!content.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  
  // 设置回复提交状态
  replySubmitting.value = true
  
  try {
    const response = await api.post('/comment/create', {
      content: content,
      targetId: articleId.value,
      parentId: comment.id // 使用评论ID作为parentId
    })
    
    if (response.code === 0) {
      ElMessage.success('回复发布成功')
      
      // 向被回复的用户发送评论通知
      if (isLoggedIn.value && comment.userId && parseInt(userId.value) !== comment.userId) {
        chatService.sendCommentNotification(
          comment.userId,
          content,
          'blog',
          parseInt(articleId.value)
        )
      }
      
      // 清空回复框
      cancelReply()
      // 刷新文章数据以获取最新评论
      fetchArticleDetail()
    } else {
      ElMessage.error(response.message || '回复发布失败')
    }
  } catch (err) {
    console.error('发布回复出错:', err)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    replySubmitting.value = false
  }
}

// 提交回复
const submitReply = async (parentComment: any) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  
  // 使用通用方法提交回复
  submitCommentReply(currentReplyComment.value, replyContent.value);
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
  replyingToUser.value = ''
  currentReplyComment.value = null
}

// 点赞评论
const handleLikeComment = async (comment: any) => {
  if (!isLoggedIn.value) {
    handleLogin()
    return
  }
  
  try {
    const response = await api.post(`/blog/comment/like/${comment.id}`)
    
    if (response.code === 0) {
      // 更新评论点赞数
      comment.likes = (comment.likes || 0) + 1
      ElMessage.success('点赞成功')
    } else {
      ElMessage.error(response.message || '点赞失败')
    }
  } catch (err) {
    console.error('评论点赞出错:', err)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 处理关注作者
const handleFollowAuthor = async () => {
  if (!isLoggedIn.value) {
    handleLogin()
    return
  }
  
  // 不能关注自己
  if (article.value.authorId === parseInt(userId.value)) {
    ElMessage.warning('不能关注自己')
    return
  }
  
  followLoading.value = true
  
  try {
    const response = await api.post(`/follow/${article.value.authorId}`)
    
    if (response.code === 0) {
      // 更新关注状态
      article.value.isFollow = !article.value.isFollow
      
      // 根据返回值显示不同的消息
      if (response.data === 0) {
        ElMessage.success('关注成功')
      } else if (response.data === 1) {
        ElMessage.success('已取消关注')
      }
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('关注作者出错:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

// 监听文章ID变化
watch(() => route.params.id, () => {
  fetchArticleDetail()
}, { immediate: true })

// 组件挂载时获取文章详情
onMounted(() => {
  fetchArticleDetail()
})
</script>

<style scoped>
.article-page {
  min-height: 100vh;
  background-color: var(--background-color);
}

.article-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
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

/* 文章内容 */
.article-content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 40px;
}

.article-header {
  margin-bottom: 30px;
}

.article-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  align-items: center;
}

.author-details {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.publish-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.article-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.stat-item .el-icon {
  margin-right: 4px;
  font-size: 16px;
}

.like-stat {
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 2px 6px;
  border-radius: 12px;
}

.like-stat:hover {
  background-color: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.liked-icon {
  color: #1890ff;
}

.liked-text {
  color: #1890ff;
}

.article-category {
  margin-top: 16px;
}

.article-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 20px 0 40px;
}

.article-body {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 40px;
}

.article-actions {
  display: flex;
  justify-content: center;
  margin: 40px 0;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.article-actions .el-button {
  min-width: 150px;
  font-size: 16px;
  padding: 12px 24px;
  transition: all 0.3s ease;
}

.article-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.is-liked {
  background-color: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.article-author-card {
  display: flex;
  align-items: center;
  background-color: #fafafa;
  border-radius: 8px;
  padding: 24px;
  margin: 40px 0;
}

.author-card-info {
  flex: 1;
  margin-left: 20px;
}

.author-card-name {
  font-size: 18px;
  font-weight: 500;
  margin: 0 0 8px;
}

.author-card-bio {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 评论区样式 */
.article-comments {
  margin-top: 40px;
}

.comments-title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-input-container {
  display: flex;
  margin-bottom: 30px;
  gap: 16px;
}

.comment-input-wrapper {
  flex: 1;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.login-tip {
  font-size: 14px;
  color: #909399;
}

.login-tip a {
  color: var(--primary-color);
  cursor: pointer;
}

.comments-list {
  margin-top: 20px;
}

.comment-item {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  display: flex;
  gap: 16px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-author {
  font-weight: 500;
  font-size: 15px;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
  color: #333;
  word-break: break-word;
}

.comment-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.reply-input-container {
  display: flex;
  margin: 16px 0 16px 56px;
  gap: 12px;
}

.reply-input-wrapper {
  flex: 1;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  gap: 8px;
}

.comment-children {
  margin-left: 56px;
  margin-top: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
}

.child-comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.child-comment-item:last-child {
  margin-bottom: 0;
}

.child-comment-body {
  flex: 1;
}

.reply-preview {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.preview-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.preview-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.comment-preview {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-container {
    padding: 20px 15px;
  }
  
  .article-content-container {
    padding: 20px;
  }
  
  .article-title {
    font-size: 22px;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .article-stats {
    width: 100%;
    justify-content: space-between;
  }
  
  .article-actions {
    flex-wrap: wrap;
  }
  
  .article-actions .el-button {
    flex: 1;
    min-width: auto;
  }
  
  .article-author-card {
    flex-direction: column;
    text-align: center;
    padding: 20px 15px;
  }
  
  .author-card-info {
    margin: 15px 0;
  }
  
  .comment-input-container,
  .reply-input-container {
    flex-direction: column;
    gap: 12px;
  }
  
  .comment-input-container .el-avatar,
  .reply-input-container .el-avatar {
    align-self: flex-start;
  }
  
  .comment-children {
    margin-left: 20px;
    padding: 12px;
  }
  
  .child-comment-item {
    gap: 8px;
  }
  
  .comment-content {
    gap: 12px;
  }
}

/* 点赞动画 */
.like-animation {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 9999;
  animation: like-pulse 1.5s ease-in-out;
  opacity: 0;
  pointer-events: none;
}

.like-icon {
  font-size: 120px;
  color: #1890ff;
  filter: drop-shadow(0 0 10px rgba(24, 144, 255, 0.5));
}

@keyframes like-pulse {
  0% {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 0;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0;
  }
}
</style> 