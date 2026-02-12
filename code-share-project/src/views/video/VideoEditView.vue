<template>
  <div class="video-edit-page">
    <NavHeader />
    
    <div class="main-content container">
      <div class="page-header">
        <h1 class="page-title">修改视频</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/video' }">视频列表</el-breadcrumb-item>
          <el-breadcrumb-item>修改视频</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <!-- 视频编辑表单 -->
      <div v-else-if="!error && videoInfo" class="edit-form-container">
        <el-form
          ref="videoFormRef"
          :model="videoForm"
          :rules="formRules"
          label-position="top"
          class="video-form"
        >
          <!-- 标题 -->
          <el-form-item label="视频标题" prop="title">
            <el-input v-model="videoForm.title" placeholder="请输入视频标题" maxlength="100" show-word-limit />
          </el-form-item>
          
          <!-- 描述 -->
          <el-form-item label="视频描述" prop="description">
            <el-input
              v-model="videoForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入视频描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <!-- 视频文件 -->
          <el-form-item label="视频文件">
            <div class="video-preview" v-if="videoInfo.videoPath">
              <video 
                ref="videoRef"
                :src="videoInfo.videoPath" 
                controls 
                width="320" 
                height="180"
                class="preview-video"
              ></video>
              <div class="video-actions">
                <el-checkbox v-model="keepOriginalFile">保留原视频文件</el-checkbox>
                <el-button type="danger" @click="handleRemoveVideo" :disabled="keepOriginalFile">删除原视频</el-button>
              </div>
            </div>
            
            <el-upload
              v-if="!keepOriginalFile"
              ref="videoUploadRef"
              class="video-uploader"
              action=""
              :auto-upload="false"
              :limit="1"
              :on-change="handleVideoChange"
              :on-remove="handleVideoRemove"
              :on-exceed="handleExceed"
              :file-list="videoFileList"
              :multiple="false"
              accept="video/*"
            >
              <el-button type="primary">选择新视频文件</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  请上传 MP4、WebM 等格式的视频文件，文件大小不超过 500MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          
          <!-- 视频封面 -->
          <el-form-item label="视频封面">
            <div class="cover-preview" v-if="videoInfo.coverPath">
              <el-image 
                :src="videoInfo.coverPath" 
                fit="cover" 
                style="width: 320px; height: 180px" 
                class="preview-cover"
              ></el-image>
              <div class="cover-actions">
                <el-checkbox v-model="keepOriginalImage">保留原封面图片</el-checkbox>
                <el-button type="danger" @click="handleRemoveCover" :disabled="keepOriginalImage">删除原封面</el-button>
              </div>
            </div>
            
            <el-upload
              v-if="!keepOriginalImage"
              ref="coverUploadRef"
              class="cover-uploader"
              action=""
              :auto-upload="false"
              :limit="1"
              :on-change="handleCoverChange"
              :on-remove="handleCoverRemove"
              :on-exceed="handleExceed"
              :file-list="coverFileList"
              :multiple="false"
              accept="image/*"
            >
              <el-button type="primary">选择新封面图片</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  请上传 JPG、PNG 等格式的图片文件作为视频封面，建议尺寸 16:9，文件大小不超过 5MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          
          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting">保存修改</el-button>
            <el-button @click="$router.push('/profile')">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 错误信息 -->
      <div v-else-if="error" class="error-container">
        <el-result
          icon="error"
          title="加载失败"
          :sub-title="error"
        >
          <template #extra>
            <el-button type="primary" @click="$router.push('/profile')">返回个人中心</el-button>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavHeader from '@/components/layout/NavHeader.vue'
import videoService from '@/services/video'

// 路由和导航
const router = useRouter()
const route = useRoute()
const videoId = ref<string | null>(route.params.id as string)

// 状态控制
const loading = ref(true)
const submitting = ref(false)
const error = ref('')

// 表单引用
const videoFormRef = ref()
const videoUploadRef = ref()
const coverUploadRef = ref()
const videoRef = ref()

// 视频数据
const videoInfo = ref<any>(null)

// 表单数据
const videoForm = reactive({
  title: '',
  description: ''
})

// 文件保持选项
const keepOriginalFile = ref(true)
const keepOriginalImage = ref(true)

// 文件上传
const videoFile = ref<File | null>(null)
const videoFileList = ref<any[]>([])
const coverFile = ref<File | null>(null)
const coverFileList = ref<any[]>([])

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度需在 2 到 100 个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入视频描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度需在 10 到 500 个字符之间', trigger: 'blur' }
  ]
}

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
    const response = await videoService.getVideoDetail(videoId.value)
    
    if (response.code === 0 && response.data) {
      videoInfo.value = response.data
      
      // 填充表单数据
      videoForm.title = response.data.title || ''
      videoForm.description = response.data.description || ''
      
      // 更新页面标题
      document.title = `修改视频: ${videoForm.title} - IT技术社区`
      
      console.log('视频数据加载成功:', videoInfo.value)
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

// 处理视频文件选择
const handleVideoChange = (file: any) => {
  if (file) {
    // 检查文件大小
    const isLt500M = file.size / 1024 / 1024 < 500
    if (!isLt500M) {
      ElMessage.error('视频文件大小不能超过 500MB!')
      videoFileList.value = []
      return
    }
    
    videoFile.value = file.raw
    videoFileList.value = [file]
  }
}

// 处理视频文件移除
const handleVideoRemove = () => {
  videoFile.value = null
  videoFileList.value = []
}

// 处理封面图片选择
const handleCoverChange = (file: any) => {
  if (file) {
    // 检查文件大小
    const isLt5M = file.size / 1024 / 1024 < 5
    if (!isLt5M) {
      ElMessage.error('封面图片大小不能超过 5MB!')
      coverFileList.value = []
      return
    }
    
    coverFile.value = file.raw
    coverFileList.value = [file]
  }
}

// 处理封面图片移除
const handleCoverRemove = () => {
  coverFile.value = null
  coverFileList.value = []
}

// 处理超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

// 处理移除原视频
const handleRemoveVideo = () => {
  ElMessageBox.confirm('确定要删除原视频文件吗？此操作将需要重新上传视频文件。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    keepOriginalFile.value = false
  }).catch(() => {
    // 取消操作，保持原状
  })
}

// 处理移除原封面
const handleRemoveCover = () => {
  ElMessageBox.confirm('确定要删除原封面图片吗？此操作将需要重新上传封面图片。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    keepOriginalImage.value = false
  }).catch(() => {
    // 取消操作，保持原状
  })
}

// 提交表单
const submitForm = async () => {
  if (!videoFormRef.value) return
  
  // 表单验证
  await videoFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      ElMessage.error('请修正表单中的错误')
      return
    }
    
    // 检查文件要求
    if (!keepOriginalFile.value && !videoFile.value) {
      ElMessage.warning('请上传视频文件')
      return
    }
    
    if (!keepOriginalImage.value && !coverFile.value) {
      ElMessage.warning('请上传视频封面图片')
      return
    }
    
    submitting.value = true
    
    try {
      // 准备表单数据
      const formData = new FormData()
      formData.append('id', videoId.value || '')
      formData.append('title', videoForm.title)
      formData.append('description', videoForm.description)
      formData.append('keepOriginalImage', keepOriginalImage.value.toString())
      formData.append('keepOriginalFile', keepOriginalFile.value.toString())
      
      // 用户ID
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          const parsedUserInfo = JSON.parse(userInfo)
          if (parsedUserInfo && parsedUserInfo.id) {
            formData.append('userId', parsedUserInfo.id)
          }
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
      
      // 添加文件
      if (!keepOriginalFile.value && videoFile.value) {
        formData.append('videoFile', videoFile.value)
      } else {
        // 需要提供一个空文件占位
        const emptyBlob = new Blob([''], { type: 'application/octet-stream' })
        formData.append('videoFile', emptyBlob, 'empty.txt')
      }
      
      if (!keepOriginalImage.value && coverFile.value) {
        formData.append('image', coverFile.value)
      } else {
        // 需要提供一个空文件占位
        const emptyBlob = new Blob([''], { type: 'application/octet-stream' })
        formData.append('image', emptyBlob, 'empty.txt')
      }
      
      // 发送请求
      const response = await videoService.updateVideo(formData)
      
      if (response.code === 0) {
        ElMessage.success('视频修改成功')
        router.push('/profile')
      } else {
        ElMessage.error(response.message || '修改失败，请稍后重试')
      }
    } catch (error) {
      console.error('修改视频失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 监听保留原文件的变化
watch(keepOriginalFile, (newVal) => {
  if (newVal) {
    // 清除选择的新文件
    videoFile.value = null
    videoFileList.value = []
  }
})

// 监听保留原封面的变化
watch(keepOriginalImage, (newVal) => {
  if (newVal) {
    // 清除选择的新封面
    coverFile.value = null
    coverFileList.value = []
  }
})

// 组件挂载时加载数据
onMounted(() => {
  fetchVideoDetail()
})
</script>

<style scoped>
.video-edit-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  padding: 20px;
  flex: 1;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.loading-container, .error-container {
  margin: 20px 0;
}

.edit-form-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.video-form {
  max-width: 800px;
  margin: 0 auto;
}

.video-preview, .cover-preview {
  margin-bottom: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #f8f9fa;
}

.preview-video, .preview-cover {
  display: block;
  border-radius: 4px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.video-actions, .cover-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.video-uploader, .cover-uploader {
  width: 100%;
}

/* 上传样式 */
:deep(.el-upload-list__item) {
  transition: all 0.3s;
}

:deep(.el-upload-list__item-name) {
  color: #606266;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .page-title {
    margin-bottom: 10px;
  }
  
  .edit-form-container {
    padding: 20px 15px;
  }
  
  .preview-video, .preview-cover {
    width: 100% !important;
    height: auto !important;
  }
  
  .video-actions, .cover-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style> 