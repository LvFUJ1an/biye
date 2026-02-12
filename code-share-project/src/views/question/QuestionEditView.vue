<template>
  <div class="question-edit-container">
    <NavHeader />
    <div class="main-content container">
      <div class="question-form-container">
        <h1 class="page-title">修改问题</h1>
        <p class="page-desc">编辑您的问题，提供清晰的描述，有助于获得更准确的回答</p>
        
        <el-skeleton v-if="loading" :rows="10" animated />
        
        <el-form 
          v-else
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
          
          <!-- 回答列表 -->
          <div class="answers-section" v-if="questionDetail && questionDetail.answers && questionDetail.answers.length > 0">
            <h3>已有回答</h3>
            <div class="answer-item" v-for="answer in questionDetail.answers" :key="answer.id">
              <div class="answer-header">
                <span class="answer-user">用户回答</span>
                <span class="answer-time">{{ formatDate(answer.createTime) }}</span>
              </div>
              <div class="answer-content">{{ answer.content }}</div>
              <div class="answer-footer">
                <span class="like-count"><el-icon><Star /></el-icon> {{ answer.likeCount }}</span>
                <el-tag size="small" type="success" v-if="answer.isAccepted === 1">已采纳</el-tag>
              </div>
            </div>
          </div>
          
          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" @click="submitQuestion" :loading="submitting">
              更新问题
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import MarkdownEdit from '@/components/markdown/MarkdownEdit.vue'
import MarkdownPreview from '@/components/markdown/MarkdownPreview.vue'
import questionService from '@/services/question'

const router = useRouter()
const route = useRoute()
const questionId = ref<number>(Number(route.params.id))

// 表单引用
const questionFormRef = ref()

// 编辑器标签页
const activeTab = ref('edit')

// 加载和提交状态
const loading = ref(true)
const submitting = ref(false)

// 问题详情
const questionDetail = ref<any>(null)

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
  // 检查问题ID是否有效
  if (!questionId.value) {
    ElMessage.error('问题ID无效，无法编辑')
    router.push('/profile')
    return
  }
  
  // 加载问题详情
  loadQuestionDetail()
})

// 加载问题详情
const loadQuestionDetail = async () => {
  loading.value = true
  try {
    const response = await questionService.getQuestionDetail(questionId.value)
    
    if (response.code === 0 && response.data) {
      questionDetail.value = response.data
      
      // 填充表单数据
      questionForm.title = response.data.title || ''
      questionForm.content = response.data.content || ''
      questionForm.categoryId = response.data.categoryId
      
      // 如果有标签数据
      if (response.data.tags && Array.isArray(response.data.tags)) {
        questionForm.tags = response.data.tags
      }
    } else {
      ElMessage.error(response.message || '获取问题详情失败')
      router.push('/profile')
    }
  } catch (error) {
    console.error('获取问题详情失败:', error)
    ElMessage.error('获取问题详情失败，请稍后重试')
    router.push('/profile')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

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
    ElMessageBox.confirm('确定要修改这个问题吗？', '提交确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      updateQuestionData()
    }).catch(() => {})
  })
}

// 更新问题数据
const updateQuestionData = async () => {
  submitting.value = true
  try {
    // 准备提交数据，根据接口要求简化参数
    const questionData = {
      id: questionId.value,
      title: questionForm.title,
      content: questionForm.content
    }
    
    // 调用修改问题API
    const response = await questionService.modifyQuestion(questionData)
    
    if (response.code === 0) {
      ElMessage.success('问题修改成功')
      
      // 跳转到个人中心页面
      router.push('/profile')
    } else {
      ElMessage.error(response.message || '修改失败，请稍后重试')
    }
  } catch (error) {
    console.error('修改问题失败:', error)
    ElMessage.error('修改失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消提交
const cancelSubmit = () => {
  // 弹出确认框
  ElMessageBox.confirm('确定要放弃编辑吗？所有修改将丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.back()
  }).catch(() => {})
}
</script>

<style scoped>
.question-edit-container {
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
  max-width: 900px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.page-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 30px;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 20px;
}

.editor-tabs {
  height: 400px;
}

.editor-tabs :deep(.el-tabs__content) {
  height: calc(100% - 55px);
  overflow-y: auto;
}

.answers-section {
  margin: 20px 0;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.answers-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
}

.answer-item {
  background-color: #f9f9f9;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.answer-user {
  font-weight: 500;
  color: #409eff;
}

.answer-time {
  font-size: 13px;
  color: #909399;
}

.answer-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.answer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.like-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #909399;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .question-form-container {
    padding: 20px;
  }
  
  .editor-tabs {
    height: 300px;
  }
}
</style> 