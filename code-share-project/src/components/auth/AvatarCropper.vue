<template>
  <div class="avatar-cropper">
    <div v-if="!previewUrl" class="avatar-upload">
      <label :for="inputId" class="upload-label">
        <div class="upload-icon">
          <el-icon><Plus /></el-icon>
        </div>
        <span>选择头像</span>
      </label>
      <input 
        :id="inputId" 
        type="file" 
        accept="image/*" 
        class="file-input" 
      />
    </div>
    
    <div v-else class="avatar-preview">
      <img :src="previewUrl" alt="预览" class="preview-img" />
      <div class="preview-actions">
        <button 
          type="button" 
          class="action-btn confirm-btn" 
          @click="cropImage" 
          :disabled="isLoading"
        >
          <span v-if="!isLoading">确认</span>
          <span v-else>处理中...</span>
        </button>
        <button 
          type="button" 
          class="action-btn cancel-btn" 
          @click="cancelCrop"
          :disabled="isLoading"
        >
          取消
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 定义组件选项
defineOptions({
  name: 'AvatarCropper'
})

// 定义组件接收的props
const props = defineProps({
  inputId: {
    type: String,
    required: true
  }
})

// 定义组件触发的事件
const emit = defineEmits(['crop', 'cancel'])

// 状态变量
const selectedFile = ref(null)
const previewUrl = ref('')
const isLoading = ref(false)

// 文件选择处理
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件类型
  if (!file.type.match('image.*')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 检查文件大小（最大2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片不能超过2MB')
    return
  }
  
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

// 裁剪图片
const cropImage = () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  isLoading.value = true
  
  // 创建一个新的图片元素加载选择的图片
  const img = new Image()
  img.onload = () => {
    // 创建一个canvas元素进行裁剪
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    
    // 计算裁剪区域（取中心正方形）
    const size = Math.min(img.width, img.height)
    const x = (img.width - size) / 2
    const y = (img.height - size) / 2
    
    // 设置canvas尺寸为目标尺寸
    canvas.width = 200
    canvas.height = 200
    
    // 在canvas上绘制裁剪后的图像
    ctx.drawImage(img, x, y, size, size, 0, 0, 200, 200)
    
    // 将canvas转换为Blob
    canvas.toBlob((blob) => {
      // 释放资源
      URL.revokeObjectURL(previewUrl.value)
      
      // 创建新的预览URL
      const croppedUrl = URL.createObjectURL(blob)
      
      // 触发crop事件，传递裁剪结果
      emit('crop', {
        url: croppedUrl,
        blob: blob
      })
      
      // 重置状态
      resetState()
      isLoading.value = false
    }, 'image/png')
  }
  
  img.onerror = () => {
    ElMessage.error('图片加载失败')
    isLoading.value = false
  }
  
  img.src = previewUrl.value
}

// 取消裁剪
const cancelCrop = () => {
  resetState()
  emit('cancel')
}

// 重置状态
const resetState = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  selectedFile.value = null
  previewUrl.value = ''
  
  // 重置文件输入
  const fileInput = document.getElementById(props.inputId)
  if (fileInput) {
    fileInput.value = ''
  }
}

// 组件卸载前清理
onMounted(() => {
  // 确保组件卸载时释放resources
  const fileInput = document.getElementById(props.inputId)
  if (fileInput) {
    fileInput.addEventListener('change', handleFileSelect)
  }
})
</script>

<style scoped>
.avatar-cropper {
  width: 100%;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #f5f7fa;
}

.upload-label:hover {
  border-color: var(--primary-color);
  background-color: #f0f9ff;
}

.upload-icon {
  font-size: 24px;
  color: #909399;
  margin-bottom: 8px;
}

.upload-label span {
  font-size: 14px;
  color: #606266;
}

.file-input {
  display: none;
}

.avatar-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--primary-color);
  margin-bottom: 16px;
}

.preview-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 6px 16px;
  border-radius: 4px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.confirm-btn {
  background-color: var(--primary-color);
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  background-color: #66b1ff;
}

.cancel-btn {
  background-color: #f56c6c;
  color: white;
}

.cancel-btn:hover:not(:disabled) {
  background-color: #f78989;
}
</style> 