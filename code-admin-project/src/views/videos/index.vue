<template>
  <div class="videos-container">
    <el-card class="videos-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">视频中心</span>
          <div class="header-operations">
            <el-input
              v-model="queryParams.keyword"
              placeholder="搜索视频标题"
              clearable
              style="width: 250px"
              @keyup.enter="handleQuery"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
            <el-button type="success" @click="handleAdd">
              <el-icon><VideoCamera /></el-icon>
              上传视频
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="page-content">
        <el-table
          v-loading="loading"
          :data="videoList"
          style="width: 100%"
          border
          stripe
        >
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
          <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
          <el-table-column label="封面" width="200" align="center">
            <template #default="scope">
              <el-image
                style="width: 160px; height: 90px; object-fit: cover;"
                :src="scope.row.coverPath"
                :preview-src-list="[scope.row.coverPath]"
                fit="cover"
                loading="lazy"
                :initial-index="0"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="videoPath" label="视频" min-width="180">
            <template #default="scope">
              <el-button type="primary" link @click="playVideo(scope.row)">
                <el-icon><VideoPlay /></el-icon>播放
              </el-button>
              <el-link
                :href="scope.row.videoPath"
                target="_blank"
                type="primary"
                :underline="false"
              >
                <el-icon><Download /></el-icon>下载
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isPublished === 0 ? 'success' : 'info'">
                {{ scope.row.isPublished === 0 ? '已发布' : '未发布' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button
                type="primary"
                link
                @click="handleEdit(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                :type="scope.row.isPublished === 0 ? 'warning' : 'success'"
                link
                @click="handleToggleStatus(scope.row)"
              >
                {{ scope.row.isPublished === 0 ? '下架' : '发布' }}
              </el-button>
              <el-button
                type="danger"
                link
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 视频播放对话框 -->
    <el-dialog
      v-model="playDialogVisible"
      title="视频播放"
      width="80%"
      destroy-on-close
      append-to-body
    >
      <div v-if="currentVideo" class="video-player-container">
        <video 
          controls 
          class="video-player" 
          :src="currentVideo.videoPath" 
          :poster="currentVideo.coverPath"
        >
          您的浏览器不支持 HTML5 视频播放
        </video>
        <div class="video-info">
          <h3>{{ currentVideo.title }}</h3>
          <p>{{ currentVideo.description }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 上传视频抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerType === 'add' ? '上传视频' : '编辑视频'"
      :size="500"
      direction="rtl"
      destroy-on-close
    >
      <div style="padding: 0 20px;">
        <el-form
          ref="videoFormRef"
          :model="videoForm"
          :rules="videoRules"
          label-width="80px"
        >
          <el-form-item label="标题" prop="title">
            <el-input v-model="videoForm.title" placeholder="请输入视频标题" />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="videoForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入视频描述"
            />
          </el-form-item>
          <el-form-item label="封面" prop="image">
            <el-upload
              class="cover-uploader"
              action="#"
              :show-file-list="false"
              :http-request="uploadCover"
              :before-upload="beforeImageUpload"
              accept="image/jpeg,image/png,image/gif"
            >
              <img v-if="coverImageUrl" :src="coverImageUrl" class="cover-image" />
              <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">请上传图片作为视频封面，支持JPG、PNG格式</div>
          </el-form-item>
          <el-form-item label="视频文件" prop="file">
            <el-upload
              class="video-uploader"
              action="#"
              :show-file-list="true"
              :limit="1"
              :http-request="uploadVideo"
              :before-upload="beforeVideoUpload"
              accept="video/mp4,video/webm,video/ogg"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择视频
              </el-button>
              <template #file-list="{ files }">
                <div class="file-list">
                  <div v-for="file in files" :key="file.name" class="file-item">
                    <el-icon><VideoCamera /></el-icon>
                    <span class="file-name">{{ file.name }}</span>
                  </div>
                </div>
              </template>
            </el-upload>
            <div class="upload-tip">
              {{ drawerType === 'add' ? '请上传视频文件，支持MP4、WebM、Ogg格式，大小不超过500MB' : '如需更换视频文件，请上传新文件，否则将保留原文件' }}
            </div>
          </el-form-item>
        </el-form>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getVideoList, createVideo, updateVideoStatus, modifyVideo, deleteVideo, Video } from '../../api/video'
import { message, confirm } from '../../utils/message'
import { 
  Search, 
  Refresh, 
  VideoCamera, 
  Download, 
  Picture, 
  VideoPlay,
  Upload,
  Plus
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'

// 加载状态
const loading = ref(false)
// 视频列表
const videoList = ref<Video[]>([])
// 总数
const total = ref(0)
// 视频播放对话框
const playDialogVisible = ref(false)
// 当前播放的视频
const currentVideo = ref<Video | null>(null)
// 抽屉可见性
const drawerVisible = ref(false)
// 抽屉类型：add-新增，edit-编辑
const drawerType = ref<'add' | 'edit'>('add')
// 提交loading状态
const submitLoading = ref(false)
// 表单引用
const videoFormRef = ref<FormInstance>()
// 封面图片URL
const coverImageUrl = ref('')

// 查询参数
const queryParams = reactive({
  keyword: '',
  current: 1,
  size: 10
})

// 视频表单数据
const videoForm = reactive({
  id: 0,
  title: '',
  description: '',
  image: null as File | null,
  file: null as File | null,
  userId: 0,
  // 如果是编辑模式，记录原始的图片和文件路径
  originalImage: '',
  originalFile: ''
})

// 表单校验规则
const videoRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入视频描述', trigger: 'blur' },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ]
})

// 获取视频列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getVideoList({
      keyword: queryParams.keyword,
      current: queryParams.current,
      size: queryParams.size
    })
    
    if (res.code === 0) {
      videoList.value = res.data.records
      total.value = res.data.total
    } else {
      message.error(res.message || '获取视频列表失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取视频列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 播放视频
const playVideo = (video: Video) => {
  currentVideo.value = video
  playDialogVisible.value = true
}

// 图片上传前的检查
const beforeImageUpload = (file: File) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    message.error('上传图片只能是 JPG/PNG/GIF 格式!')
    return false
  }
  
  if (!isLt2M) {
    message.error('上传图片大小不能超过 2MB!')
    return false
  }
  
  return isImage && isLt2M
}

// 视频文件上传前的检查
const beforeVideoUpload = (file: File) => {
  const isVideo = file.type === 'video/mp4' || file.type === 'video/webm' || file.type === 'video/ogg'
  const isLt500M = file.size / 1024 / 1024 < 500
  
  if (!isVideo) {
    message.error('上传视频只能是 MP4/WebM/Ogg 格式!')
    return false
  }
  
  if (!isLt500M) {
    message.error('上传视频大小不能超过 500MB!')
    return false
  }
  
  return isVideo && isLt500M
}

// 上传封面图片
const uploadCover = (options: UploadRequestOptions) => {
  const file = options.file as File
  videoForm.image = file
  // 创建本地预览URL
  coverImageUrl.value = URL.createObjectURL(file)
}

// 上传视频文件
const uploadVideo = (options: UploadRequestOptions) => {
  const file = options.file as File
  videoForm.file = file
}

// 新增视频
const handleAdd = () => {
  drawerType.value = 'add'
  videoForm.id = 0
  videoForm.title = ''
  videoForm.description = ''
  videoForm.image = null
  videoForm.file = null
  videoForm.userId = 0
  videoForm.originalImage = ''
  videoForm.originalFile = ''
  coverImageUrl.value = ''
  drawerVisible.value = true
}

// 编辑视频
const handleEdit = (row: Video) => {
  drawerType.value = 'edit'
  videoForm.id = row.id
  videoForm.title = row.title
  videoForm.description = row.description
  videoForm.image = null
  videoForm.file = null
  videoForm.userId = row.userId
  videoForm.originalImage = row.coverPath
  videoForm.originalFile = row.videoPath
  coverImageUrl.value = row.coverPath
  drawerVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!videoFormRef.value) return
  
  await videoFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 非编辑模式下必须上传图片和文件
    if (drawerType.value === 'add') {
      if (!videoForm.image) {
        message.error('请上传视频封面图片')
        return
      }
      
      if (!videoForm.file) {
        message.error('请上传视频文件')
        return
      }
    }
    
    // 开始提交
    submitLoading.value = true
    
    try {
      // 创建FormData对象
      const formData = new FormData()
      
      if (drawerType.value === 'add') {
        // 新增视频
        formData.append('title', videoForm.title)
        formData.append('description', videoForm.description)
        formData.append('image', videoForm.image as File)
        formData.append('videoFile', videoForm.file as File)
        
        // 调用创建接口
        const res = await createVideo(formData)
        
        if (res.code === 0) {
          message.success('上传视频成功')
          drawerVisible.value = false
          // 重新加载列表
          getList()
        } else {
          message.error(res.message || '上传视频失败')
        }
      } else {
        // 编辑视频
        formData.append('id', videoForm.id.toString())
        formData.append('title', videoForm.title)
        formData.append('description', videoForm.description)
        formData.append('userId', videoForm.userId.toString())
        
        // 处理图片参数 - 如果有新上传的图片使用新图片，否则告诉后端不更新图片
        const keepOriginalImage = !videoForm.image
        formData.append('keepOriginalImage', keepOriginalImage.toString())
        
        if (videoForm.image) {
          formData.append('image', videoForm.image)
        } else {
          // 创建一个空的文件对象作为占位符，因为后端接口要求必须传image参数
          const emptyImageFile = new File(
            [new Uint8Array(0)], 
            'empty.png', 
            { type: 'image/png' }
          )
          formData.append('image', emptyImageFile)
        }
        
        // 处理视频文件参数 - 如果有新上传的文件使用新文件，否则告诉后端不更新文件
        const keepOriginalFile = !videoForm.file
        formData.append('keepOriginalFile', keepOriginalFile.toString())
        
        if (videoForm.file) {
          formData.append('videoFile', videoForm.file)
        } else {
          // 创建一个空的文件对象作为占位符，因为后端接口要求必须传videoFile参数
          const emptyFile = new File(
            [new Uint8Array(0)], 
            'empty.mp4', 
            { type: 'video/mp4' }
          )
          formData.append('videoFile', emptyFile)
        }
        
        // 调用修改接口
        const res = await modifyVideo(formData)
        
        if (res.code === 0) {
          message.success('修改视频成功')
          drawerVisible.value = false
          // 重新加载列表
          getList()
        } else {
          message.error(res.message || '修改视频失败')
        }
      }
    } catch (error: any) {
      message.error(error.message || '操作失败，请稍后重试')
    } finally {
      submitLoading.value = false
    }
  })
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.current = 1
  getList()
}

// 页面大小变化
const handleSizeChange = (size: number) => {
  queryParams.size = size
  getList()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  queryParams.current = current
  getList()
}

// 切换视频发布状态
const handleToggleStatus = async (row: Video) => {
  const newStatus = row.isPublished === 0 ? 1 : 0
  const actionText = newStatus === 0 ? '发布' : '下架'
  
  const confirmed = await confirm.show(
    `确定要${actionText}视频 "${row.title}" 吗？`,
    `${actionText}确认`,
    'warning',
    `确定${actionText}`,
    '取消'
  )
  
  if (!confirmed) {
    return
  }
  
  try {
    // 准备API所需的参数
    const videoId = row.id
    const isPublished = newStatus
    
    const res = await updateVideoStatus(videoId, isPublished)
    
    if (res.code === 0) {
      message.success(`已成功${actionText}视频`)
      // 更新本地数据
      row.isPublished = newStatus
    } else {
      message.error(res.message || `${actionText}视频失败`)
    }
  } catch (error: any) {
    message.error(error.message || `${actionText}视频失败，请稍后重试`)
  }
}

// 删除视频
const handleDelete = async (row: Video) => {
  const confirmed = await confirm.show(
    `确定要删除视频 "${row.title}" 吗？此操作不可恢复！`,
    '删除确认',
    'error',
    '确定删除',
    '取消'
  )
  
  if (!confirmed) {
    return
  }
  
  loading.value = true
  
  try {
    const res = await deleteVideo(row.id)
    
    if (res.code === 0) {
      message.success(`已成功删除视频 "${row.title}"`)
      
      // 如果当前页只有一条数据且不是第一页，删除后跳转到上一页
      if (queryParams.current > 1 && videoList.value.length === 1) {
        queryParams.current--
      }
      
      // 重新加载列表
      getList()
    } else {
      message.error(res.message || '删除视频失败')
      loading.value = false
    }
  } catch (error: any) {
    message.error(error.message || '删除视频失败，请稍后重试')
    loading.value = false
  }
}

// 页面初始化时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.videos-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.videos-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.header-operations {
  display: flex;
  gap: 10px;
}

.page-content {
  padding: 20px 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 160px;
  height: 90px;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.video-player-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.video-player {
  width: 100%;
  max-height: 60vh;
  background-color: #000;
}

.video-info {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.video-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.video-info p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.cover-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cover-uploader .cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 160px;
  height: 90px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
}

.cover-uploader .cover-image {
  width: 160px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 5px;
}

.video-uploader {
  width: 100%;
}

.file-list {
  margin-top: 10px;
}

.file-item {
  display: flex;
  align-items: center;
  margin-top: 5px;
  padding: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.file-item .el-icon {
  color: #409eff;
  margin-right: 5px;
}

.file-name {
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
}
</style> 