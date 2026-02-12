<template>
  <div class="ebook-detail-page">
    <NavHeader />
    
    <div class="ebook-detail-container" v-if="ebook">
      <div class="ebook-header">
        <div class="ebook-cover-container">
          <img :src="ebook.image || defaultCover" :alt="ebook.title" class="ebook-cover">
        </div>
        <div class="ebook-info">
          <h1 class="ebook-title">{{ ebook.title }}</h1>
          <div class="ebook-meta">
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>上传时间：{{ formatDate(ebook.createTime) }}</span>
            </div>
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>上传者：{{ ebook.adminName || '管理员' }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Download /></el-icon>
              <span>下载次数：{{ ebook.downloadCount || 0 }}</span>
            </div>
            <div class="meta-item">
              <el-icon><View /></el-icon>
              <span>查看次数：{{ ebook.viewCount || 0 }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Document /></el-icon>
              <span>文件格式：PDF</span>
            </div>
            <div class="meta-item">
              <el-icon><Tickets /></el-icon>
              <span>分类：{{ getCategoryName(ebook.categoryId) }}</span>
            </div>
          </div>
          <div class="ebook-actions">
            <!-- <el-button type="primary" @click="handleDownload" :disabled="!isLoggedIn">
              <el-icon><Download /></el-icon> 下载电子书
            </el-button> -->
            <el-button type="success" @click="previewDialogVisible = true">
              <el-icon><View /></el-icon> 在线预览
            </el-button>
            <el-button type="info" @click="handleReport" plain>
              <el-icon><Warning /></el-icon> 问题反馈
            </el-button>
          </div>
          <div class="login-tip" v-if="!isLoggedIn">
            <el-alert
              title="请先登录后再下载电子书"
              type="warning"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="ebook-content">
        <el-tabs type="border-card">
          <el-tab-pane label="电子书介绍">
            <div class="ebook-description">
              <h3>内容简介</h3>
              <p v-if="ebook.description">{{ ebook.description }}</p>
              <p v-else class="no-content">暂无简介内容</p>
            </div>
          </el-tab-pane>
          
          <!-- <el-tab-pane label="目录">
            <div class="ebook-toc" v-if="ebook.catalog && ebook.catalog.length">
              <div 
                v-for="(item, index) in ebook.catalog" 
                :key="index"
                class="toc-item"
                :class="{ 'level-1': item.level === 1, 'level-2': item.level === 2, 'level-3': item.level === 3 }"
              >
                {{ item.title }}
              </div>
            </div>
            <div v-else class="no-content">
              暂无目录信息
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="评价与反馈">
            <div class="ebook-comments" v-if="comments.length">
              <div class="comment-item" v-for="comment in comments" :key="comment.id">
                <div class="comment-avatar">
                  <el-avatar :src="comment.avatar || defaultAvatar" :size="40">{{ comment.username?.charAt(0) }}</el-avatar>
                </div>
                <div class="comment-content">
                  <div class="comment-user">{{ comment.username }}</div>
                  <div class="comment-text">{{ comment.content }}</div>
                  <div class="comment-meta">
                    <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                    <el-button type="primary" size="small" text @click="handleReply(comment)">回复</el-button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-content">
              暂无评价信息，成为第一个评价的用户吧！
            </div>
            
            <div class="comment-form" v-if="isLoggedIn">
              <h3>发表评价</h3>
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="请输入您对电子书的评价..."
              />
              <div class="form-action">
                <el-button type="primary" @click="submitComment" :disabled="!commentContent.trim()">提交评价</el-button>
              </div>
            </div>
            <div v-else class="login-to-comment">
              <el-alert
                title="请登录后发表评价"
                type="info"
                :closable="false"
                show-icon
              />
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="相关推荐">
            <div class="related-books" v-if="relatedBooks.length">
              <div class="related-book-list">
                <div v-for="book in relatedBooks" :key="book.id" class="related-book-item" @click="goToBook(book.id)">
                  <div class="related-book-cover">
                    <img :src="book.coverUrl || defaultCover" :alt="book.title">
                  </div>
                  <div class="related-book-info">
                    <div class="related-book-title">{{ book.title }}</div>
                    <div class="related-book-desc">{{ book.description || '暂无描述' }}</div>
                    <div class="related-book-meta">
                      <span>下载: {{ book.downloadCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-content">
              暂无相关推荐
            </div>
          </el-tab-pane> -->
        </el-tabs>
      </div>
    </div>
    
    <div v-else class="loading-container">
      <div class="loading-animation"></div>
      <p class="loading-text">正在加载电子书信息...</p>
    </div>
    
    <!-- PDF预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="ebook?.title"
      width="100%"
      fullscreen
      destroy-on-close
      :show-close="true"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      class="preview-dialog"
    >
      <div v-if="ebook" class="pdf-preview-container fullscreen-preview">
        <div class="preview-toolbar">
          <el-button type="primary" @click="previewDialogVisible = false" circle plain>
            <el-icon><Close /></el-icon>
          </el-button>
          <span class="preview-title">{{ ebook.title }}</span>
          <el-button type="success" @click="handleDownload" circle plain title="下载文件">
            <el-icon><Download /></el-icon>
          </el-button>
        </div>
        <iframe 
          v-if="isPreviewSupported"
          :src="getPreviewUrl(ebook.filePath)" 
          frameborder="0" 
          class="pdf-preview"
          allow="fullscreen"
        ></iframe>
        <div v-else class="preview-not-supported">
          <el-icon size="60"><WarningFilled /></el-icon>
          <p>当前文件类型不支持在线预览，请下载后查看</p>
          <el-button type="primary" @click="handleDownload">
            <el-icon><Download /></el-icon> 下载文件
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 反馈对话框 -->
    <el-dialog
      v-model="reportDialogVisible"
      title="问题反馈"
      width="500px"
    >
      <div class="report-form">
        <el-form :model="reportForm" label-position="top">
          <el-form-item label="问题类型">
            <el-select v-model="reportForm.type" placeholder="请选择问题类型" style="width: 100%">
              <el-option label="内容错误" value="content_error" />
              <el-option label="链接失效" value="link_error" />
              <el-option label="格式问题" value="format_error" />
              <el-option label="侵权问题" value="copyright_issue" />
              <el-option label="其他问题" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="问题描述">
            <el-input
              v-model="reportForm.content"
              type="textarea"
              :rows="4"
              placeholder="请详细描述您遇到的问题..."
            />
          </el-form-item>
          <el-form-item label="联系方式 (选填)">
            <el-input v-model="reportForm.contact" placeholder="您的邮箱或手机号，便于我们反馈" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReport" :disabled="!reportForm.content || !reportForm.type">
            提交反馈
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Calendar, 
  User, 
  Download, 
  View, 
  Document, 
  Warning, 
  Tickets,
  Star,
  WarningFilled,
  Close
} from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()

// 电子书数据
const ebook = ref<any>(null)
const comments = ref<any[]>([])
const relatedBooks = ref<any[]>([])
const previewDialogVisible = ref(false)

// 评论相关
const commentContent = ref('')

// 问题反馈相关
const reportDialogVisible = ref(false)
const reportForm = ref({
  type: '',
  content: '',
  contact: ''
})

// 默认图片
const defaultCover = 'https://img.alicdn.com/imgextra/i1/O1CN01u0Nn7c1QRZ4h2bKRD_!!6000000001973-2-tps-330-330.png'
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 分类列表
const categories = ref([
  { id: 1, name: '前端开发' },
  { id: 2, name: '后端开发' },
  { id: 3, name: '移动开发' },
  { id: 4, name: '数据库' },
  { id: 5, name: '人工智能' },
  { id: 6, name: '云计算' },
  { id: 7, name: '网络安全' }
])

// 判断是否登录
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '未分类'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知'
  
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    })
  } catch (e) {
    return dateStr
  }
}

// 获取电子书详情
const fetchEbookDetail = async () => {
  try {
    const id = route.params.id
    if (!id) {
      ElMessage.error('电子书ID无效')
      return
    }
    
    const response = await api.get(`/ebook/getEbookById/${id}`)
    
    if (response.code === 0 && response.data) {
      ebook.value = response.data
      
      // 记录查看次数
      //await api.post(`/ebook/view/${id}`)
      
      // 获取相关推荐
      // fetchRelatedBooks()
      
      // 获取评论
      // fetchComments()
    } else {
      ElMessage.error('获取电子书信息失败')
    }
  } catch (error) {
    console.error('获取电子书详情出错:', error)
    ElMessage.error('获取电子书详情出错')
  }
}

// 获取相关电子书
const fetchRelatedBooks = async () => {
  try {
    if (!ebook.value || !ebook.value.id) return
    
    const params = {
      id: ebook.value.id,
      categoryId: ebook.value.categoryId || 0,
      limit: 5
    }
    
    const response = await api.post('/ebook/related', params)
    
    if (response.code === 0 && response.data) {
      relatedBooks.value = response.data
    }
  } catch (error) {
    console.error('获取相关电子书出错:', error)
  }
}

// 获取评论
const fetchComments = async () => {
  try {
    if (!ebook.value || !ebook.value.id) return
    
    const response = await api.get(`/ebook/comments/${ebook.value.id}`)
    
    if (response.code === 0 && response.data) {
      comments.value = response.data
    }
  } catch (error) {
    console.error('获取评论出错:', error)
  }
}

// 处理下载
const handleDownload = async () => {
  try {
    if (!isLoggedIn.value) {
      ElMessage.warning('请先登录后再下载电子书')
      return
    }
    
    if (!ebook.value || !ebook.value.id) return
    
    // 显示下载中提示
    ElMessage({
      message: '开始下载，如果文件较大，请耐心等待...',
      type: 'info',
      duration: 3000
    })
    
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    
    // 创建一个临时的隐藏iframe来处理下载
    // 这样可以携带token并正确处理ResponseEntity<Resource>
    const iframe = document.createElement('iframe')
    iframe.style.display = 'none'
    document.body.appendChild(iframe)
    
    // 设置下载URL
    const baseURL = 'http://localhost:18000'
    const downloadUrl = `${baseURL}/file/download/${ebook.value.id}`
    
    // 创建一个form进行post请求，确保token正确传递
    const form = document.createElement('form')
    form.method = 'POST'
    form.action = downloadUrl
    
    // 添加token到form的header中
    if (token) {
      const tokenInput = document.createElement('input')
      tokenInput.type = 'hidden'
      tokenInput.name = 'Authorization'
      tokenInput.value = `Bearer ${token}`
      form.appendChild(tokenInput)
    }
    
    iframe.onload = () => {
      setTimeout(() => {
        // 清理iframe
        document.body.removeChild(iframe)
      }, 1000)
    }
    
    iframe.onerror = () => {
      ElMessage.error('下载失败，请稍后重试')
      document.body.removeChild(iframe)
    }
    
    // 将form添加到iframe并提交
    iframe.contentWindow?.document.body.appendChild(form)
    form.submit()
    
    // 同时使用传统的window.open方式作为备选方案
    if (token) {
      // 如果使用window.open方法，我们需要确保token可以被带上
      // 创建一个隐藏的a标签并点击
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = downloadUrl
      link.target = '_blank'
      link.rel = 'noopener noreferrer'
      
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
    
  } catch (error) {
    console.error('下载电子书出错:', error)
    ElMessage.error('下载失败，请稍后重试')
  }
}

// 处理问题反馈
const handleReport = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再提交反馈')
    return
  }
  
  reportDialogVisible.value = true
}

// 提交反馈
const submitReport = async () => {
  try {
    if (!ebook.value || !ebook.value.id) return
    
    const params = {
      ebookId: ebook.value.id,
      type: reportForm.value.type,
      content: reportForm.value.content,
      contact: reportForm.value.contact
    }
    
    const response = await api.post('/ebook/report', params)
    
    if (response.code === 0) {
      ElMessage.success('反馈提交成功，感谢您的反馈')
      reportDialogVisible.value = false
      reportForm.value = {
        type: '',
        content: '',
        contact: ''
      }
    } else {
      ElMessage.error('反馈提交失败，请稍后重试')
    }
  } catch (error) {
    console.error('提交反馈出错:', error)
    ElMessage.error('提交反馈出错')
  }
}

// 提交评论
const submitComment = async () => {
  try {
    if (!ebook.value || !ebook.value.id) return
    if (!commentContent.value.trim()) {
      ElMessage.warning('评价内容不能为空')
      return
    }
    
    const params = {
      ebookId: ebook.value.id,
      content: commentContent.value.trim()
    }
    
    const response = await api.post('/ebook/comment', params)
    
    if (response.code === 0) {
      ElMessage.success('评价提交成功')
      commentContent.value = ''
      fetchComments() // 刷新评论列表
    } else {
      ElMessage.error('评价提交失败，请稍后重试')
    }
  } catch (error) {
    console.error('提交评价出错:', error)
    ElMessage.error('提交评价出错')
  }
}

// 处理回复
const handleReply = (comment: any) => {
  commentContent.value = `@${comment.username} `
  // 滚动到评论表单
  setTimeout(() => {
    document.querySelector('.comment-form')?.scrollIntoView({ behavior: 'smooth' })
  }, 100)
}

// 前往指定电子书详情
const goToBook = (id: number) => {
  router.push(`/ebook/detail/${id}`)
  // 如果是同一页面不同参数，手动刷新数据
  if (route.params.id !== id.toString()) {
    setTimeout(() => {
      fetchEbookDetail()
    }, 100)
  }
}

// 判断文件是否支持预览
const isPreviewSupported = computed(() => {
  if (!ebook.value || !ebook.value.filePath) return false
  
  const filePath = ebook.value.filePath.toLowerCase()
  // 支持的预览文件类型
  return filePath.endsWith('.pdf') || 
         isOfficeFile(filePath) || 
         isImageFile(filePath)
})

// 判断是否为Office文件
const isOfficeFile = (filePath: string) => {
  const officeExts = ['.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx']
  return officeExts.some(ext => filePath.endsWith(ext))
}

// 判断是否为图片文件
const isImageFile = (filePath: string) => {
  const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  return imageExts.some(ext => filePath.endsWith(ext))
}

// 获取预览URL
const getPreviewUrl = (filePath: string) => {
  if (!filePath) return ''
  
  const lowerFilePath = filePath.toLowerCase()
  
  // 对于PDF和图片文件，直接使用原始URL
  if (lowerFilePath.endsWith('.pdf') || isImageFile(lowerFilePath)) {
    return filePath
  }
  
  // 对于Office文件，优先使用Microsoft Office Online预览
  if (isOfficeFile(lowerFilePath)) {
    // 使用Microsoft Office Online预览（需要公网可访问的URL）
    return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(filePath)}`
    
    // 备选：使用Google Docs预览
    // return `https://docs.google.com/viewer?url=${encodeURIComponent(filePath)}&embedded=true`
  }
  
  return filePath
}

// 页面加载时获取电子书详情
onMounted(() => {
  fetchEbookDetail()
})
</script>

<style scoped>
.ebook-detail-page {
  min-height: 100vh;
  background-color: var(--background-color);
}

.ebook-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 电子书头部信息 */
.ebook-header {
  display: flex;
  gap: 30px;
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 30px;
}

.ebook-cover-container {
  flex: 0 0 250px;
}

.ebook-cover {
  width: 100%;
  height: auto;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  object-fit: cover;
}

.ebook-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.ebook-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 20px;
  color: #333;
}

.ebook-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.ebook-actions {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.login-tip {
  margin-top: 15px;
}

.ebook-content {
  margin-bottom: 40px;
}

/* 内容描述 */
.ebook-description {
  padding: 10px 0;
}

.ebook-description h3 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 15px;
  color: #333;
}

.ebook-description p {
  line-height: 1.6;
  color: #606266;
  white-space: pre-line;
}

/* 目录 */
.toc-item {
  padding: 10px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.toc-item:last-child {
  border-bottom: none;
}

.level-1 {
  font-weight: bold;
}

.level-2 {
  padding-left: 30px;
}

.level-3 {
  padding-left: 60px;
  color: #606266;
}

/* 评论 */
.ebook-comments {
  margin-bottom: 30px;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-content {
  flex: 1;
}

.comment-user {
  font-weight: 500;
  margin-bottom: 5px;
}

.comment-text {
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-line;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 12px;
}

.comment-form {
  margin-top: 30px;
}

.comment-form h3 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 15px;
}

.form-action {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.login-to-comment {
  margin-top: 30px;
}

/* 相关推荐 */
.related-book-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.related-book-item {
  display: flex;
  gap: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
}

.related-book-item:hover {
  background-color: #f0f0f0;
  transform: translateY(-3px);
}

.related-book-cover {
  flex: 0 0 80px;
  height: 120px;
  overflow: hidden;
  border-radius: 4px;
}

.related-book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.related-book-title {
  font-weight: 500;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-book-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.related-book-meta {
  font-size: 12px;
  color: #909399;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
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

/* PDF预览 */
.pdf-preview-container {
  width: 100%;
  height: 70vh;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.pdf-preview {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-not-supported {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px;
  text-align: center;
}

.preview-not-supported p {
  margin: 20px 0;
  color: #909399;
  font-size: 16px;
}

.no-content {
  color: #909399;
  font-style: italic;
  padding: 20px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .ebook-header {
    flex-direction: column;
  }
  
  .ebook-cover-container {
    width: 200px;
    margin: 0 auto 20px;
  }
  
  .ebook-meta {
    grid-template-columns: 1fr;
  }
  
  .related-book-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .ebook-actions {
    flex-direction: column;
  }
  
  .related-book-item {
    flex-direction: column;
  }
  
  .related-book-cover {
    width: 100%;
    height: 150px;
  }
}

.preview-dialog :deep(.el-dialog__header) {
  display: none;
}

.preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.pdf-preview-container.fullscreen-preview {
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;
  border-radius: 0;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}

.preview-toolbar {
  height: 48px;
  background-color: rgba(0, 0, 0, 0.7);
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.pdf-preview {
  width: 100%;
  height: calc(100vh - 48px);
  border: none;
  flex: 1;
}

.preview-title {
  font-size: 16px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60%;
}
</style> 