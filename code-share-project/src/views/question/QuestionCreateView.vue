<template>
  <div class="question-create-container">
    <NavHeader />
    <div class="main-content container">
      <div class="question-form-container">
        <h1 class="page-title">提出问题</h1>
        <p class="page-desc">详细描述您的问题，将获得更准确的回答</p>
        
        <el-form 
          :model="questionForm" 
          :rules="rules" 
          ref="questionFormRef" 
          label-position="top"
          @submit.prevent="submitQuestion"
        >
          <!-- 问题标题 -->
          <el-form-item label="问题标题" prop="title">
            <el-input 
              v-model="questionForm.title" 
              placeholder="请输入问题标题（5-50字）" 
              maxlength="50" 
              show-word-limit
            />
          </el-form-item>
          
          <!-- 问题分类 -->
          <!-- <el-form-item label="问题分类" prop="categoryId">
            <el-select v-model="questionForm.categoryId" placeholder="请选择问题分类">
              <el-option 
                v-for="category in categories" 
                :key="category.id" 
                :label="category.name" 
                :value="category.id" 
              />
            </el-select>
          </el-form-item> -->
          
          <!-- 问题标签 -->
          <!-- <el-form-item label="问题标签" prop="tags">
            <el-select 
              v-model="questionForm.tags" 
              multiple 
              collapse-tags 
              collapse-tags-tooltip
              placeholder="请选择相关标签（最多5个）"
              :max-collapse-tags="3"
            >
              <el-option 
                v-for="tag in tags" 
                :key="tag.id" 
                :label="tag.name" 
                :value="tag.id" 
              />
            </el-select>
          </el-form-item> -->
          
          <!-- 问题内容 -->
          <el-form-item label="问题描述" prop="content">
            <div class="editor-container">
              <el-tabs v-model="activeTab" class="editor-tabs">
                <el-tab-pane label="编辑" name="edit">
                  <MarkdownEdit v-model:value="questionForm.content" />
                </el-tab-pane>
                <el-tab-pane label="预览" name="preview">
                  <MarkdownPreview :value="questionForm.content" />
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-form-item>
          
          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" @click="submitQuestion" :loading="submitting">
              发布问题
            </el-button>
            <el-button @click="cancelSubmit">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavHeader from '@/components/layout/NavHeader.vue'
import MarkdownEdit from '@/components/markdown/MarkdownEdit.vue'
import MarkdownPreview from '@/components/markdown/MarkdownPreview.vue'
import questionService from '@/services/question'

const router = useRouter()

// 表单引用
const questionFormRef = ref()

// 编辑器标签页
const activeTab = ref('edit')

// 提交状态
const submitting = ref(false)

// 问题表单
const questionForm = reactive({
  title: '',
  content: '',
  categoryId: undefined,
  tags: [] as string[]
})

// 分类列表
const categories = ref([
  { id: 1, name: 'Java' },
  { id: 2, name: 'Python' },
  { id: 3, name: 'JavaScript' },
  { id: 4, name: '前端开发' },
  { id: 5, name: '后端开发' },
  { id: 6, name: '移动开发' },
  { id: 7, name: '数据库' },
  { id: 8, name: '运维/DevOps' },
  { id: 9, name: '人工智能' },
  { id: 10, name: '其他' }
])

// 标签列表
const tags = ref([
  { id: 'vue', name: 'Vue.js' },
  { id: 'react', name: 'React' },
  { id: 'angular', name: 'Angular' },
  { id: 'node', name: 'Node.js' },
  { id: 'spring', name: 'Spring' },
  { id: 'django', name: 'Django' },
  { id: 'flask', name: 'Flask' },
  { id: 'mysql', name: 'MySQL' },
  { id: 'mongodb', name: 'MongoDB' },
  { id: 'redis', name: 'Redis' },
  { id: 'docker', name: 'Docker' },
  { id: 'kubernetes', name: 'Kubernetes' },
  { id: 'git', name: 'Git' },
  { id: 'linux', name: 'Linux' },
  { id: 'algorithm', name: '算法' }
])

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度应在5-50个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择问题分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, message: '问题描述不能少于10个字符', trigger: 'blur' }
  ]
}

// 初始化数据
onMounted(() => {
  // 可在此处加载分类和标签数据
  // loadCategories()
  // loadTags()
})

// 提交问题
const submitQuestion = async () => {
  if (submitting.value) return
  
  // 表单验证
  await questionFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }
    
    // 确认提交
    ElMessageBox.confirm('确定要发布这个问题吗？', '提交确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      submitQuestionData()
    }).catch(() => {})
  })
}

// 提交问题数据
const submitQuestionData = async () => {
  submitting.value = true
  try {
    // 准备提交数据
    const questionData = {
      title: questionForm.title,
      content: questionForm.content,
      categoryId: questionForm.categoryId,
      tags: questionForm.tags
    }
    
    // 调用API提交问题
    const response = await questionService.createQuestion(questionData)
    
    if (response.code === 0) {
      ElMessage.success('问题发布成功')
      
      // 跳转到问题详情页
      if (response.data) {
        router.push(`/question/detail/${response.data}`)
      } else {
        router.push('/question')
      }
    } else {
      ElMessage.error(response.message || '发布失败，请稍后重试')
    }
  } catch (error) {
    console.error('提交问题失败:', error)
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消提交
const cancelSubmit = () => {
  // 弹出确认框
  ElMessageBox.confirm('确定要放弃编辑吗？已填写的内容将丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.back()
  }).catch(() => {})
}
</script>

<style scoped>
.question-create-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  padding: 20px;
  flex: 1;
}

.question-form-container {
  max-width: 1000px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 30px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #333;
}

.page-desc {
  color: #909399;
  margin-bottom: 30px;
}

.editor-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 20px;
}

.editor-tabs {
  height: 500px;
}

:deep(.el-tabs__content) {
  height: calc(100% - 55px);
  overflow: auto;
}

:deep(.el-tab-pane) {
  height: 100%;
}

/* 确保MarkdownEdit和Preview组件占满容器 */
:deep(.markdown-edit), :deep(.markdown-preview) {
  height: 100%;
}
</style> 