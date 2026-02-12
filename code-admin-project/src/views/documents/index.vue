<template>
  <div class="documents-container">
    <el-card class="documents-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">文档中心</span>
          <div class="header-operations">
            <el-input
              v-model="queryParams.keyword"
              placeholder="搜索电子书标题"
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
              <el-icon><Upload /></el-icon>上传电子书
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="documents-content">
        <el-table
          v-loading="loading"
          :data="ebookList"
          style="width: 100%"
          border
          stripe
        >
          <el-table-column type="index" label="#" width="60" align="center" />
          <!-- <el-table-column prop="id" label="ID" width="60" align="center" /> -->
          <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
          <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
          <el-table-column label="封面" width="120" align="center">
            <template #default="scope">
              <el-image
                style="width: 80px; height: 100px; object-fit: cover;"
                :src="scope.row.image"
                :preview-src-list="[scope.row.image]"
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
          <el-table-column prop="filePath" label="文件" min-width="180">
            <template #default="scope">
              <el-button type="primary" link @click="previewFile(scope.row)">
                <el-icon><Document /></el-icon>预览
              </el-button>
              <el-link
                :href="scope.row.filePath"
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

    <!-- 添加/编辑电子书抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerType === 'add' ? '上传电子书' : '编辑电子书'"
      :size="500"
      direction="rtl"
      destroy-on-close
    >
      <div style="padding: 0 20px;">
        <el-form
          ref="ebookFormRef"
          :model="ebookForm"
          :rules="ebookRules"
          label-width="80px"
        >
          <el-form-item label="标题" prop="title">
            <el-input v-model="ebookForm.title" placeholder="请输入电子书标题" />
          </el-form-item>
          <el-form-item label="描述" prop="description">
            <el-input
              v-model="ebookForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入电子书描述"
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
            <div class="upload-tip">请上传图片作为电子书封面，支持JPG、PNG格式</div>
          </el-form-item>
          <el-form-item label="文件" prop="file">
            <el-upload
              class="ebook-uploader"
              action="#"
              :show-file-list="true"
              :limit="1"
              :http-request="uploadEbook"
              :before-upload="beforeEbookUpload"
              accept=".pdf,.epub,.mobi,.doc,.docx"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择文件
              </el-button>
              <template #file-list="{ files }">
                <div class="file-list">
                  <div v-for="file in files" :key="file.name" class="file-item">
                    <el-icon><Document /></el-icon>
                    <span class="file-name">{{ file.name }}</span>
                  </div>
                </div>
              </template>
            </el-upload>
            <div class="upload-tip">请上传电子书文件，支持PDF、EPUB、MOBI、DOC格式</div>
          </el-form-item>
        </el-form>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="文件预览"
      width="80%"
      destroy-on-close
      append-to-body
    >
      <div class="preview-container">
        <iframe v-if="previewUrl" :src="previewUrl" class="preview-iframe"></iframe>
        <div v-else class="preview-error">
          <el-icon><WarningFilled /></el-icon>
          <p>该文件格式不支持在线预览，请下载后查看</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getEbookList, addEbook, updateEbook, deleteEbook, updateEbookStatus, createEbook, modifyEbook } from '../../api/ebook'
import { message, confirm } from '../../utils/message'
import { 
  Search, 
  Refresh, 
  Upload, 
  Download, 
  Document, 
  Picture, 
  Plus, 
  WarningFilled 
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'

// 定义电子书类型
interface Ebook {
  id: number
  title: string
  description: string
  image: string
  filePath: string
  isPublished: number
  createTime: string
  updateTime: string
}

// 加载状态
const loading = ref(false)
// 电子书列表
const ebookList = ref<Ebook[]>([])
// 总数
const total = ref(0)
// 对话框可见性
const dialogVisible = ref(false)
// 抽屉可见性
const drawerVisible = ref(false)
// 对话框类型：add-新增，edit-编辑
const dialogType = ref<'add' | 'edit'>('add')
// 抽屉类型：add-新增，edit-编辑
const drawerType = ref<'add' | 'edit'>('add')
// 提交loading状态
const submitLoading = ref(false)
// 表单引用
const ebookFormRef = ref<FormInstance>()
// 封面图片URL
const coverImageUrl = ref('')
// 预览对话框可见性
const previewVisible = ref(false)
// 预览URL
const previewUrl = ref('')

// 查询参数
const queryParams = reactive({
  keyword: '',
  current: 1,
  size: 10
})

// 表单数据
const ebookForm = reactive({
  id: 0,
  title: '',
  description: '',
  image: null as File | null,
  file: null as File | null,
  // 如果是编辑模式，记录原始的图片和文件路径
  originalImage: '',
  originalFile: ''
})

// 表单校验规则
const ebookRules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入电子书标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入电子书描述', trigger: 'blur' },
    { min: 2, max: 500, message: '长度在 2 到 500 个字符', trigger: 'blur' }
  ]
})

// 获取电子书列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getEbookList({
      keyword: queryParams.keyword,
      current: queryParams.current,
      size: queryParams.size
    })
    
    if (res.code === 0) {
      ebookList.value = res.data.records
      total.value = res.data.total
    } else {
      message.error(res.message || '获取电子书列表失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取电子书列表失败')
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

// 电子书文件上传前的检查
const beforeEbookUpload = (file: File) => {
  const validExtensions = ['.pdf', '.epub', '.mobi', '.doc', '.docx']
  const isValidType = validExtensions.some(ext => file.name.toLowerCase().endsWith(ext))
  const isLt20M = file.size / 1024 / 1024 < 20
  
  if (!isValidType) {
    message.error('上传电子书只能是 PDF/EPUB/MOBI/DOC/DOCX 格式!')
    return false
  }
  
  if (!isLt20M) {
    message.error('上传电子书大小不能超过 20MB!')
    return false
  }
  
  return isValidType && isLt20M
}

// 上传封面图片
const uploadCover = (options: UploadRequestOptions) => {
  const file = options.file as File
  ebookForm.image = file
  // 创建本地预览URL
  coverImageUrl.value = URL.createObjectURL(file)
}

// 上传电子书文件
const uploadEbook = (options: UploadRequestOptions) => {
  const file = options.file as File
  ebookForm.file = file
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

// 新增电子书
const handleAdd = () => {
  drawerType.value = 'add'
  ebookForm.id = 0
  ebookForm.title = ''
  ebookForm.description = ''
  ebookForm.image = null
  ebookForm.file = null
  ebookForm.originalImage = ''
  ebookForm.originalFile = ''
  coverImageUrl.value = ''
  drawerVisible.value = true
}

// 编辑电子书
const handleEdit = (row: Ebook) => {
  drawerType.value = 'edit'
  ebookForm.id = row.id
  ebookForm.title = row.title
  ebookForm.description = row.description
  ebookForm.image = null
  ebookForm.file = null
  ebookForm.originalImage = row.image
  ebookForm.originalFile = row.filePath
  coverImageUrl.value = row.image
  drawerVisible.value = true
}

// 预览文件
const previewFile = (row: Ebook) => {
  const filePath = row.filePath
  // 检查是否是PDF文件，只支持PDF在线预览
  if (filePath.toLowerCase().endsWith('.pdf')) {
    previewUrl.value = filePath
  } else {
    previewUrl.value = ''
  }
  previewVisible.value = true
}

// 切换发布状态
const handleToggleStatus = async (row: Ebook) => {
  const newStatus = row.isPublished === 0 ? 1 : 0
  const actionText = newStatus === 0 ? '发布' : '下架'
  
  const confirmed = await confirm.show(
    `确定要${actionText}电子书 "${row.title}" 吗？`,
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
    const ebookId = row.id
    const isPublished = newStatus
    
    const res = await updateEbookStatus(ebookId, isPublished)
    
    if (res.code === 0) {
      message.success(`已成功${actionText}电子书`)
      // 更新本地数据
      row.isPublished = newStatus
    } else {
      message.error(res.message || `${actionText}电子书失败`)
    }
  } catch (error: any) {
    message.error(error.message || `${actionText}电子书失败，请稍后重试`)
  }
}

// 删除电子书
const handleDelete = async (row: Ebook) => {
  const confirmed = await confirm.show(
    `确定要删除电子书 "${row.title}" 吗？此操作不可恢复！`,
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
    const res = await deleteEbook(row.id)
    
    if (res.code === 0) {
      message.success(`已成功删除电子书 "${row.title}"`)
      
      // 如果当前页只有一条数据且不是第一页，删除后跳转到上一页
      if (queryParams.current > 1 && ebookList.value.length === 1) {
        queryParams.current--
      }
      
      // 重新加载列表
      getList()
    } else {
      message.error(res.message || '删除电子书失败')
      loading.value = false
    }
  } catch (error: any) {
    message.error(error.message || '删除电子书失败，请稍后重试')
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!ebookFormRef.value) return
  
  await ebookFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 非编辑模式下必须上传图片和文件
    if (drawerType.value === 'add') {
      if (!ebookForm.image) {
        message.error('请上传电子书封面图片')
        return
      }
      
      if (!ebookForm.file) {
        message.error('请上传电子书文件')
        return
      }
    }
    
    // 开始提交
    submitLoading.value = true
    
    try {
      // 创建FormData对象
      const formData = new FormData()
      
      // 添加公共参数
      formData.append('title', ebookForm.title)
      formData.append('description', ebookForm.description)
      
      if (drawerType.value === 'add') {
        // 新增电子书
        formData.append('image', ebookForm.image as File)
        formData.append('ebookFile', ebookForm.file as File)
        
        // 调用创建接口
        const res = await createEbook(formData)
        
        if (res.code === 0) {
          message.success('上传电子书成功')
          drawerVisible.value = false
          getList()
        } else {
          message.error(res.message || '上传电子书失败')
        }
      } else {
        // 编辑电子书
        formData.append('id', ebookForm.id.toString())
        
        // 处理图片参数 - 如果有新上传的图片使用新图片，否则告诉后端不更新图片
        if (ebookForm.image) {
          formData.append('image', ebookForm.image)
          // 显式设置为不保留原图
          formData.append('keepOriginalImage', 'false')
        } else {
          // 告诉后端我们没有上传新图片，保留原图
          formData.append('keepOriginalImage', 'true')
          // 创建一个空的文件对象作为占位符
          const emptyImageFile = new File(
            [new Uint8Array(0)], 
            'empty.png', 
            { type: 'image/png' }
          )
          formData.append('image', emptyImageFile)
        }
        
        // 处理文件参数 - 如果有新上传的文件使用新文件，否则告诉后端不更新文件
        if (ebookForm.file) {
          formData.append('ebookFile', ebookForm.file)
          // 显式设置为不保留原文件
          formData.append('keepOriginalFile', 'false')
        } else {
          // 告诉后端我们没有上传新文件，保留原文件
          formData.append('keepOriginalFile', 'true')
          // 创建一个空的文件对象作为占位符
          const emptyFile = new File(
            [new Uint8Array(0)], 
            'empty.pdf', 
            { type: 'application/pdf' }
          )
          formData.append('ebookFile', emptyFile)
        }
        
        // 调用修改接口
        const res = await modifyEbook(formData)
        
        if (res.code === 0) {
          message.success('更新电子书成功')
          drawerVisible.value = false
          getList()
        } else {
          message.error(res.message || '更新电子书失败')
        }
      }
    } catch (error: any) {
      message.error(error.message || '操作失败，请稍后重试')
    } finally {
      submitLoading.value = false
    }
  })
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

// 页面初始化时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.documents-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.documents-card {
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

.documents-content {
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
  width: 80px;
  height: 100px;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.cover-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cover-uploader .cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 160px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
}

.cover-uploader .cover-image {
  width: 120px;
  height: 160px;
  object-fit: cover;
  border-radius: 6px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 5px;
}

.ebook-uploader {
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

.preview-container {
  width: 100%;
  height: 70vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #f56c6c;
  font-size: 16px;
}

.preview-error .el-icon {
  font-size: 48px;
  margin-bottom: 20px;
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