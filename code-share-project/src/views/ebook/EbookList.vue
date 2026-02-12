<template>
  <div class="ebook-page">
    <NavHeader />
    
    <div class="ebook-container">
      <!-- 头部区域 -->
      <div class="page-header">
        <h1 class="page-title">电子书资源</h1>
        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索电子书"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      
      <!-- 筛选区域 -->
<!--       <div class="filter-section">
        <div class="filter-item">
          <span class="filter-label">分类：</span>
          <el-radio-group v-model="selectedCategory" size="small" @change="handleSearch">
            <el-radio-button :label="0">全部</el-radio-button>
            <el-radio-button v-for="category in categories" :key="category.id" :label="category.id">
              {{ category.name }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <div class="filter-item">
          <span class="filter-label">排序：</span>
          <el-radio-group v-model="orderType" size="small" @change="handleSearch">
            <el-radio-button label="updateTime">最新上传</el-radio-button>
            <el-radio-button label="downloads">下载最多</el-radio-button>
          </el-radio-group>
        </div>
      </div> -->
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-animation"></div>
        <p class="loading-text">正在加载电子书资源...</p>
      </div>
      
      <!-- 电子书列表 -->
      <div v-else-if="ebookList.length > 0" class="ebook-grid">
        <div 
          v-for="ebook in ebookList" 
          :key="ebook.id" 
          class="ebook-card"
          @click="goToEbookDetail(ebook.id)"
        >
          <div class="ebook-cover">
            <img :src="ebook.image || defaultCover" :alt="ebook.title" class="cover-img">
            <div class="ebook-tag" v-if="ebook.isNew">
              <el-tag type="success" size="small">新上传</el-tag>
            </div>
          </div>
          <div class="ebook-info">
            <h3 class="ebook-title">{{ ebook.title }}</h3>
            <p class="ebook-desc">{{ ebook.description || '暂无描述' }}</p>
            <div class="ebook-meta">
              <span class="author">
                上传者: {{ ebook.adminName || '管理员' }}
              </span>
              <span class="download-count">
                <el-icon><Download /></el-icon>
                {{ ebook.downloadCount || 0 }}
              </span>
            </div>
            <!-- <div class="ebook-actions">
              <el-button type="primary" size="small" @click.stop="handleDownload(ebook)">
                <el-icon><Download /></el-icon> 下载
              </el-button>
              <el-button type="info" size="small" @click.stop="handlePreview(ebook)">
                <el-icon><View /></el-icon> 预览
              </el-button>
            </div> -->
          </div>
        </div>
      </div>
      
      <!-- 无数据状态 -->
      <el-empty v-else description="暂无电子书资源" :image-size="200">
        <template #image>
          <el-icon size="60" color="#909399"><Reading /></el-icon>
        </template>
      </el-empty>
      
      <!-- 分页控件 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="current"
          v-model:page-size="size"
          :page-sizes="[12, 24, 36, 60]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- PDF预览对话框 -->
    <el-dialog
      v-model="pdfVisible"
      :title="currentEbook?.title"
      width="90%"
      destroy-on-close
      top="5vh"
    >
      <div v-if="currentEbook" class="pdf-preview-container">
        <iframe 
          v-if="pdfUrl"
          :src="pdfUrl" 
          frameborder="0" 
          class="pdf-preview"
        ></iframe>
        <div v-else class="preview-not-supported">
          <el-icon size="60" color="#909399"><WarningFilled /></el-icon>
          <p>当前文件类型不支持在线预览，请下载后查看</p>
          <el-button type="primary" @click="downloadEbook(currentEbook)">
            <el-icon><Download /></el-icon> 下载文件
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Download, View, Reading, WarningFilled } from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import ebookService, { Ebook, EbookListParams, EbookPageResult } from '@/services/ebook'

const router = useRouter()

// 电子书列表数据
const ebookList = ref<Ebook[]>([])
const total = ref(0)
const loading = ref(false)

// 筛选和分页参数
const current = ref(1)
const size = ref(12)
const keyword = ref('')
const selectedCategory = ref(0)
const orderType = ref('updateTime')
const orderDirection = ref('desc')

// 默认电子书封面
const defaultCover = 'https://img.alicdn.com/imgextra/i1/O1CN01u0Nn7c1QRZ4h2bKRD_!!6000000001973-2-tps-330-330.png'

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

// 预览相关
const pdfVisible = ref(false)
const currentEbook = ref<Ebook | null>(null)
const pdfUrl = ref('')

// 加载电子书列表
const loadEbooks = async () => {
  loading.value = true
  try {
    const params: EbookListParams = {
      current: current.value.toString(),
      size: size.value.toString(),
      orderBy: orderType.value,
      orderDirection: orderDirection.value
    }
    
    if (keyword.value) {
      params.keyword = keyword.value
    }
    
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    
    const res = await ebookService.getEbookList(params)
    if (res.code === 0) {
      const data = res.data as EbookPageResult
      ebookList.value = data.records.map(ebook => {
        // 标记最近3天上传的电子书为新书
        const uploadTime = new Date(ebook.createTime).getTime()
        const now = new Date().getTime()
        const threeDays = 3 * 24 * 60 * 60 * 1000
        ebook.isNew = (now - uploadTime) < threeDays
        return ebook
      })
      total.value = data.total
    } else {
      ElMessage.error(res.message || '获取电子书列表失败')
    }
  } catch (error) {
    console.error('获取电子书列表失败', error)
    ElMessage.error('获取电子书列表失败')
  } finally {
    loading.value = false
  }
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  current.value = val
  loadEbooks()
}

// 处理每页数量变化
const handleSizeChange = (val: number) => {
  size.value = val
  current.value = 1
  loadEbooks()
}

// 处理搜索
const handleSearch = () => {
  current.value = 1
  loadEbooks()
}

// 处理分类选择
const handleCategoryChange = (categoryId: number) => {
  selectedCategory.value = categoryId
  current.value = 1
  loadEbooks()
}

// 处理排序方式变化
const handleSortChange = (type: string) => {
  orderType.value = type
  current.value = 1
  loadEbooks()
}

// 处理排序方向变化
const handleDirectionChange = (direction: string) => {
  orderDirection.value = direction
  current.value = 1
  loadEbooks()
}

// 预览电子书
const previewEbook = async (ebook: Ebook) => {
  try {
    currentEbook.value = ebook
    // 调用浏览接口
    await ebookService.viewEbook(ebook.id)
    // 显示PDF预览
    pdfUrl.value = getPreviewUrl(ebook.filePath)
    pdfVisible.value = true
  } catch (error) {
    console.error('预览电子书失败', error)
    ElMessage.error('预览电子书失败')
  }
}

// 下载电子书
const downloadEbook = async (ebook: Ebook) => {
  try {
    // 调用下载接口
    const res = await ebookService.downloadEbook(ebook.id)
    if (res.code === 0) {
      // 创建下载链接
      const link = document.createElement('a')
      link.href = ebook.filePath
      
      // 从URL中提取文件名，如果无法提取则使用电子书标题
      let fileName = ebook.title
      try {
        // 尝试从URL获取原始文件名
        const urlParts = ebook.filePath.split('/')
        const fileNameWithParams = urlParts[urlParts.length - 1]
        // 移除URL参数（如果有）
        const fileNameOnly = fileNameWithParams.split('?')[0]
        if (fileNameOnly) {
          fileName = fileNameOnly
        }
      } catch (e) {
        console.error('提取文件名出错，使用标题作为文件名', e)
      }
      
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      ElMessage.success('下载成功')
    } else {
      ElMessage.error(res.message || '下载失败')
    }
  } catch (error) {
    console.error('下载电子书失败', error)
    ElMessage.error('下载失败')
  }
}

// 处理预览按钮点击
const handlePreview = (ebook: Ebook) => {
  previewEbook(ebook)
}

// 处理下载按钮点击
const handleDownload = (ebook: Ebook) => {
  downloadEbook(ebook)
}

// 判断是否为Office文件
const isOfficeFile = (filePath: string) => {
  const officeExts = ['.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx']
  return officeExts.some(ext => filePath.toLowerCase().endsWith(ext))
}

// 判断是否为图片文件
const isImageFile = (filePath: string) => {
  const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  return imageExts.some(ext => filePath.toLowerCase().endsWith(ext))
}

// 获取预览URL
const getPreviewUrl = (filePath: string) => {
  if (!filePath) return ''
  
  const lowerFilePath = filePath.toLowerCase()
  
  // 对于PDF和图片文件，直接使用原始URL
  if (lowerFilePath.endsWith('.pdf') || isImageFile(lowerFilePath)) {
    return filePath
  }
  
  // 对于Office文件，使用Office Online预览或其他第三方服务
  if (isOfficeFile(lowerFilePath)) {
    // 使用Microsoft Office Online预览（需要公网可访问的URL）
    return `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(filePath)}`
    
    // 备选：使用Google Docs预览
    // return `https://docs.google.com/viewer?url=${encodeURIComponent(filePath)}&embedded=true`
  }
  
  return filePath
}

// 跳转到电子书详情页
const goToEbookDetail = (id: number) => {
  router.push(`/ebook/detail/${id}`)
}

// 初始加载
onMounted(() => {
  loadEbooks()
})
</script>

<style scoped>
.ebook-page {
  min-height: 100vh;
  background-color: var(--background-color);
}

.ebook-container {
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
  margin-bottom: 10px;
}

.filter-item:last-child {
  margin-bottom: 0;
}

.filter-label {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
  min-width: 50px;
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

/* 电子书卡片网格 */
.ebook-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.ebook-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.ebook-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.ebook-cover {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.ebook-card:hover .cover-img {
  transform: scale(1.05);
}

.ebook-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.ebook-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.ebook-title {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 10px;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  height: 44.8px; /* 2 lines * 1.4 * 16px */
}

.ebook-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  height: 39px; /* 2 lines * 1.5 * 13px */
  flex: 1;
}

.ebook-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.download-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ebook-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
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
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .ebook-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .ebook-grid {
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
  
  .filter-section {
    overflow-x: auto;
  }
  
  .ebook-grid {
    grid-template-columns: 1fr;
  }
  
  .ebook-cover {
    height: 200px;
  }
}
</style> 