<template>
  <div class="poll-create-container">
    <NavHeader />
    <div class="main-content container">
      <div class="poll-form-container">
        <h1 class="page-title">创建投票</h1>
        <p class="page-desc">发起一个技术话题投票，收集社区成员的意见</p>
        
        <el-form 
          :model="pollForm" 
          :rules="rules" 
          ref="pollFormRef" 
          label-position="top"
          class="poll-form"
        >
          <!-- 投票标题 -->
          <el-form-item label="投票标题" prop="title">
            <el-input 
              v-model="pollForm.title" 
              placeholder="请输入投票标题（5-50字）" 
              maxlength="50" 
              show-word-limit
            />
          </el-form-item>
          
          <!-- 投票类型 -->
          <el-form-item label="投票类型" prop="voteType">
            <el-radio-group v-model="pollForm.voteType">
              <el-radio :label="0">单选</el-radio>
              <el-radio :label="1">多选</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 截止时间 -->
          <el-form-item label="截止时间" prop="endTime">
            <el-date-picker
              v-model="pollForm.endTime"
              type="datetime"
              placeholder="选择截止时间"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              :disabled-time="disabledTime"
            />
          </el-form-item>
          
          <!-- 投票选项 -->
          <el-form-item label="投票选项" prop="options">
            <div class="options-container">
              <div v-for="(option, index) in pollForm.options" :key="index" class="option-item">
                <el-input 
                  v-model="pollForm.options[index]" 
                  placeholder="请输入选项内容" 
                  maxlength="100"
                  class="option-input"
                />
                <el-button 
                  type="danger" 
                  icon="Delete" 
                  circle 
                  @click="removeOption(index)"
                  :disabled="pollForm.options.length <= 2"
                  class="option-delete-btn"
                />
              </div>
              
              <el-button 
                type="primary" 
                icon="Plus" 
                plain 
                @click="addOption" 
                :disabled="pollForm.options.length >= 10"
                class="add-option-btn"
              >
                添加选项
              </el-button>
            </div>
            <div class="option-hint">
              至少2个选项，最多10个选项
            </div>
          </el-form-item>
          
          <!-- 提交按钮 -->
          <el-form-item>
            <div class="form-actions">
              <el-button type="default" @click="resetForm">重置</el-button>
              <el-button type="primary" @click="submitPoll" :loading="submitting">
                {{ submitting ? '提交中...' : '发布投票' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import pollService from '@/services/poll'

const router = useRouter()
const pollFormRef = ref()
const submitting = ref(false)

// 投票表单
const pollForm = reactive({
  title: '',
  voteType: 0,  // 默认单选
  endTime: '',
  options: ['', '']  // 默认两个空选项
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入投票标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度应在5-50个字符之间', trigger: 'blur' }
  ],
  voteType: [
    { required: true, message: '请选择投票类型', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  options: [
    { 
      validator: (rule: any, value: string[], callback: Function) => {
        // 检查是否至少有两个选项
        if (value.length < 2) {
          callback(new Error('至少需要2个选项'))
          return
        }
        
        // 检查是否有空选项
        for (let i = 0; i < value.length; i++) {
          if (!value[i] || value[i].trim() === '') {
            callback(new Error(`选项${i + 1}不能为空`))
            return
          }
        }
        
        // 检查是否有重复选项
        const uniqueOptions = new Set(value.map(opt => opt.trim()))
        if (uniqueOptions.size !== value.length) {
          callback(new Error('选项不能重复'))
          return
        }
        
        callback()
      }, 
      trigger: 'change' 
    }
  ]
}

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7  // 禁用昨天及之前的日期
}

// 禁用过去的时间
const disabledTime = (date: Date) => {
  const now = new Date()
  const hours = []
  const minutes = []
  
  // 如果是今天，禁用过去的小时和分钟
  if (date && date.toDateString() === now.toDateString()) {
    for (let i = 0; i < now.getHours(); i++) {
      hours.push(i)
    }
    
    if (date.getHours() === now.getHours()) {
      for (let i = 0; i < now.getMinutes(); i++) {
        minutes.push(i)
      }
    }
  }
  
  return {
    hours,
    minutes
  }
}

// 添加选项
const addOption = () => {
  if (pollForm.options.length < 10) {
    pollForm.options.push('')
  }
}

// 删除选项
const removeOption = (index: number) => {
  if (pollForm.options.length > 2) {
    pollForm.options.splice(index, 1)
  }
}

// 重置表单
const resetForm = () => {
  pollFormRef.value.resetFields()
  pollForm.options = ['', '']
}

// 提交投票
const submitPoll = async () => {
  // 表单验证
  await pollFormRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      ElMessage.warning('请完善投票信息')
      return
    }
    
    // 二次确认
    try {
      await ElMessageBox.confirm('确定要发布该投票吗？', '发布确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      })
      
      // 提交投票
      submitting.value = true
      
      try {
        const params = {
          title: pollForm.title,
          voteType: pollForm.voteType,
          endTime: pollForm.endTime,
          optionTextList: pollForm.options.map(opt => opt.trim())
        }
        
        const res = await pollService.createPoll(params)
        
        if (res.code === 0) {
          ElMessage.success('投票创建成功')
          router.push('/poll')
        } else {
          ElMessage.error(res.message || '创建失败')
        }
      } catch (error) {
        console.error('创建投票失败:', error)
        ElMessage.error('网络错误，请稍后重试')
      } finally {
        submitting.value = false
      }
    } catch {
      // 用户取消发布
    }
  })
}
</script>

<style scoped>
.poll-create-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.poll-form-container {
  max-width: 800px;
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

.poll-form {
  margin-top: 20px;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.option-input {
  flex: 1;
}

.option-delete-btn {
  flex-shrink: 0;
}

.add-option-btn {
  margin-top: 10px;
  width: fit-content;
}

.option-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .poll-form-container {
    padding: 20px 15px;
  }
}
</style> 