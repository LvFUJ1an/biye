<template>
  <div class="video-create-container">
    <NavHeader />
    <div class="main-content container">
      <div class="video-form-container">
        <h1 class="page-title">上传视频</h1>
        <p class="page-desc">分享您的技术教程，帮助更多人学习成长</p>
        
        <el-steps :active="currentStep" finish-status="success" simple class="steps-nav">
          <el-step title="上传视频文件" icon="VideoCamera" />
          <el-step title="上传封面与信息" icon="Picture" />
          <el-step title="完成发布" icon="Check" />
        </el-steps>
        
        <!-- 步骤1: 上传视频文件 -->
        <div v-if="currentStep === 1" class="step-content">
          <el-card shadow="hover" class="upload-card">
            <div class="upload-area">
              <el-upload
                ref="videoUploadRef"
                class="video-uploader"
                drag
                action="#"
                :auto-upload="false"
                :limit="1"
                :on-change="handleVideoChange"
                :on-exceed="handleExceed"
                :on-remove="handleVideoRemove"
                :file-list="videoFileList"
                accept=".mp4,.mov,.avi,.flv,.mkv"
              >
                <div class="upload-content">
                  <el-icon class="upload-icon"><VideoCamera /></el-icon>
                  <div class="upload-text">
                    <span>点击或拖拽上传视频文件</span>
                    <p class="upload-hint">支持MP4、MOV等格式，建议大小不超过1GB</p>
                  </div>
                </div>
              </el-upload>
            </div>
            
            <div v-if="videoFile" class="file-info">
              <div class="file-preview">
                <div class="video-preview-placeholder">
                  <el-icon class="placeholder-icon"><VideoPlay /></el-icon>
                  <span>{{ videoFile.name }}</span>
                </div>
              </div>
              <div class="file-details">
                <p><strong>文件名：</strong>{{ videoFile.name }}</p>
                <p><strong>大小：</strong>{{ formatFileSize(videoFile.size) }}</p>
                <p><strong>类型：</strong>{{ videoFile.type }}</p>
              </div>
            </div>
            
            <div class="step-actions">
              <el-button type="primary" @click="nextStep" :disabled="!videoFile">下一步</el-button>
            </div>
          </el-card>
        </div>
        
        <!-- 步骤2: 上传封面与填写信息 -->
        <div v-if="currentStep === 2" class="step-content">
          <el-form 
            :model="videoForm" 
            :rules="rules" 
            ref="videoFormRef" 
            label-position="top"
            class="video-info-form"
          >
            <el-row :gutter="20">
              <el-col :span="16">
                <!-- 视频标题 -->
                <el-form-item label="视频标题" prop="title">
                  <el-input 
                    v-model="videoForm.title" 
                    placeholder="请输入视频标题（5-50字）" 
                    maxlength="50" 
                    show-word-limit
                  />
                </el-form-item>
                
                <!-- 视频描述 -->
                <el-form-item label="视频描述" prop="description">
                  <el-input 
                    v-model="videoForm.description" 
                    type="textarea" 
                    :rows="6" 
                    placeholder="请输入视频描述（10-500字）" 
                    maxlength="500" 
                    show-word-limit
                  />
                </el-form-item>
                
                <!-- 视频分类 -->
                <el-form-item label="视频分类" prop="categoryId">
                  <el-select v-model="videoForm.categoryId" placeholder="请选择视频分类">
                    <el-option 
                      v-for="category in categories" 
                      :key="category.id" 
                      :label="category.name" 
                      :value="category.id" 
                    />
                  </el-select>
                </el-form-item>
                
                <!-- 视频标签 -->
                <el-form-item label="视频标签" prop="tags">
                  <el-select 
                    v-model="videoForm.tags" 
                    multiple 
                    collapse-tags 
                    placeholder="请选择相关标签（最多5个）"
                  >
                    <el-option 
                      v-for="tag in tags" 
                      :key="tag.id" 
                      :label="tag.name" 
                      :value="tag.id" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="8">
                <!-- 视频封面上传 -->
                <el-form-item label="视频封面" prop="image">
                  <el-upload
                    class="cover-uploader"
                    action="#"
                    :auto-upload="false"
                    :on-change="handleCoverChange"
                    :on-remove="handleCoverRemove"
                    :file-list="coverFileList"
                    :limit="1"
                    list-type="picture-card"
                    accept="image/jpeg,image/png,image/jpg"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <div class="upload-tip">建议尺寸: 16:9，JPG/PNG格式</div>
                </el-form-item>
                
                <!-- 视频文件信息展示 -->
                <div class="selected-video-info">
                  <h4>已选视频文件</h4>
                  <p class="video-filename">{{ videoFile?.name }}</p>
                  <p class="video-filesize">{{ formatFileSize(videoFile?.size || 0) }}</p>
                </div>
              </el-col>
            </el-row>
            
            <div class="step-actions">
              <el-button @click="prevStep">上一步</el-button>
              <el-button type="primary" @click="submitVideo" :loading="uploading">
                {{ uploading ? '上传中...' : '发布视频' }}
              </el-button>
            </div>
          </el-form>
        </div>
        
        <!-- 步骤3: 发布完成 -->
        <div v-if="currentStep === 3" class="step-content">
          <el-result
            icon="success"
            title="视频上传成功！"
            sub-title="您的视频正在审核中，审核通过后将会展示在视频页面"
          >
            <template #extra>
              <el-button type="primary" @click="$router.push('/video')">查看视频列表</el-button>
              <el-button @click="resetForm">继续上传</el-button>
            </template>
          </el-result>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  VideoCamera, 
  Picture, 
  Check, 
  VideoPlay, 
  Plus, 
  Delete
} from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import api from '@/services/api'

const router = useRouter()

// 表单引用
const videoFormRef = ref()
const videoUploadRef = ref()

// 步骤控制
const currentStep = ref(1)
const uploading = ref(false)

// 视频文件
const videoFile = ref<File | null>(null)
const videoFileList = ref<any[]>([])

// 封面文件
const coverFile = ref<File | null>(null)
const coverFileList = ref<any[]>([])

// 视频表单
const videoForm = reactive({
  title: '',
  description: '',
  categoryId: undefined,
  tags: [] as (string | number)[]
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度应在5-50个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入视频描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应在10-500个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择视频分类', trigger: 'change' }
  ]
}

// 视频分类
const categories = ref([
  { id: 1, name: 'Java开发' },
  { id: 2, name: 'Python编程' },
  { id: 3, name: 'Web前端' },
  { id: 4, name: '移动开发' },
  { id: 5, name: '数据库' },
  { id: 6, name: '运维/DevOps' },
  { id: 7, name: '人工智能' },
  { id: 8, name: '云计算' },
  { id: 9, name: '框架原理' },
  { id: 10, name: '其他教程' }
])

// 视频标签
const tags = ref([
  { id: 'java', name: 'Java' },
  { id: 'spring', name: 'Spring' },
  { id: 'python', name: 'Python' },
  { id: 'django', name: 'Django' },
  { id: 'javascript', name: 'JavaScript' },
  { id: 'vue', name: 'Vue.js' },
  { id: 'react', name: 'React' },
  { id: 'node', name: 'Node.js' },
  { id: 'flutter', name: 'Flutter' },
  { id: 'mysql', name: 'MySQL' },
  { id: 'mongodb', name: 'MongoDB' },
  { id: 'redis', name: 'Redis' },
  { id: 'docker', name: 'Docker' },
  { id: 'kubernetes', name: 'Kubernetes' },
  { id: 'tensorflow', name: 'TensorFlow' }
])

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 处理视频文件变更
const handleVideoChange = (file: any) => {
  // 文件大小限制检查 (1GB)
  const maxSize = 1024 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('视频文件大小不能超过1GB')
    videoFileList.value = []
    videoFile.value = null
    return false
  }
  
  // 文件类型检查
  const acceptedTypes = ['video/mp4', 'video/quicktime', 'video/x-msvideo', 'video/x-flv', 'video/x-matroska']
  if (!acceptedTypes.includes(file.raw.type)) {
    ElMessage.error('请上传正确格式的视频文件')
    videoFileList.value = []
    videoFile.value = null
    return false
  }
  
  videoFile.value = file.raw
  return true
}

// 处理封面图片变更
const handleCoverChange = (file: any) => {
  // 文件大小限制检查 (2MB)
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('封面图片大小不能超过2MB')
    coverFileList.value = []
    coverFile.value = null
    return false
  }
  
  // 文件类型检查
  const acceptedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  if (!acceptedTypes.includes(file.raw.type)) {
    ElMessage.error('请上传JPG或PNG格式的图片')
    coverFileList.value = []
    coverFile.value = null
    return false
  }
  
  coverFile.value = file.raw
  return true
}

// 处理文件数量超出限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个视频文件')
}

// 移除视频文件
const handleVideoRemove = () => {
  videoFile.value = null
  videoFileList.value = []
}

// 移除封面图片
const handleCoverRemove = () => {
  coverFile.value = null
  coverFileList.value = []
}

// 下一步
const nextStep = () => {
  if (currentStep.value === 1 && !videoFile.value) {
    ElMessage.warning('请先上传视频文件')
    return
  }
  
  currentStep.value++
}

// 上一步
const prevStep = () => {
  currentStep.value--
}

// 提交视频
const submitVideo = async () => {
  if (!videoFile.value) {
    ElMessage.warning('请先上传视频文件')
    currentStep.value = 1
    return
  }
  
  if (!coverFile.value) {
    ElMessage.warning('请上传视频封面')
    return
  }
  
  // 表单验证
  await videoFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      ElMessage.warning('请完善视频信息')
      return
    }
    
    // 二次确认
    try {
      await ElMessageBox.confirm('确定要发布该视频吗？', '发布确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      })
      
      // 上传视频
      await uploadVideo()
    } catch {
      // 用户取消发布
    }
  })
}

// 上传视频到服务器
const uploadVideo = async () => {
  uploading.value = true
  
  try {
    const formData = new FormData()
    formData.append('title', videoForm.title)
    formData.append('description', videoForm.description)
    formData.append('image', coverFile.value as File)
    formData.append('videoFile', videoFile.value as File)
    
    // 可选字段
    if (videoForm.categoryId) {
      formData.append('categoryId', videoForm.categoryId.toString())
    }
    
    if (videoForm.tags && videoForm.tags.length > 0) {
      formData.append('tags', JSON.stringify(videoForm.tags))
    }
    
    // 获取token
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('登录已过期，请重新登录')
      return
    }
    
    // 发送请求
    const response = await fetch('http://localhost:18000/video/create', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    
    // 解析响应
    const result = await response.json()
    
    if (result.code === 0) {
      // 上传成功，进入第三步
      currentStep.value = 3
      ElMessage.success('视频上传成功')
    } else {
      ElMessage.error(result.message || '上传失败，请稍后重试')
    }
  } catch (error) {
    console.error('上传视频失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    uploading.value = false
  }
}

// 重置表单
const resetForm = () => {
  videoFile.value = null
  videoFileList.value = []
  coverFile.value = null
  coverFileList.value = []
  videoForm.title = ''
  videoForm.description = ''
  videoForm.categoryId = undefined
  videoForm.tags = []
  currentStep.value = 1
}

// 组件销毁前确认
onBeforeUnmount(() => {
  if ((videoFile.value || coverFile.value) && currentStep.value !== 3) {
    const result = window.confirm('您有未完成的视频上传，确定要离开吗？')
    if (!result) {
      // 用户点击取消，但无法阻止离开
      ElMessage.info('请完成视频上传或手动关闭此窗口')
    }
  }
})
</script>

<style scoped>
.video-create-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.video-form-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 30px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.page-desc {
  color: #909399;
  margin-top: 10px;
  margin-bottom: 30px;
}

.steps-nav {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.step-content {
  min-height: 300px;
}

.upload-card {
  margin-bottom: 20px;
}

.upload-area {
  padding: 20px;
}

.video-uploader {
  width: 100%;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.upload-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 20px;
}

.upload-text {
  text-align: center;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.file-info {
  display: flex;
  padding: 20px;
  border-top: 1px dashed #e6e6e6;
}

.file-preview {
  flex: 0 0 200px;
  margin-right: 20px;
}

.video-preview-placeholder {
  width: 100%;
  height: 120px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.placeholder-icon {
  font-size: 36px;
  color: #409EFF;
}

.file-details {
  flex: 1;
}

.file-details p {
  margin: 5px 0;
  font-size: 14px;
}

.step-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.video-info-form {
  margin-top: 20px;
}

.cover-uploader {
  margin-bottom: 10px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.selected-video-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-top: 20px;
}

.selected-video-info h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.video-filename {
  margin: 5px 0;
  font-size: 13px;
  word-break: break-all;
}

.video-filesize {
  margin: 5px 0;
  font-size: 13px;
  color: #909399;
}

@media (max-width: 768px) {
  .video-form-container {
    padding: 20px 15px;
  }
  
  .upload-content {
    padding: 20px 0;
  }
  
  .file-info {
    flex-direction: column;
  }
  
  .file-preview {
    margin-right: 0;
    margin-bottom: 20px;
  }
}
</style> 