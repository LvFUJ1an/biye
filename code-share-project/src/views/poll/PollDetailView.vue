<template>
  <div class="poll-detail-container">
    <NavHeader />
    <div class="main-content container flex gap-20">
      <CategorySidebar class="sidebar" />
      
      <div class="poll-detail-content">
        <el-skeleton :rows="10" animated v-if="loading" />
        
        <template v-else>
          <el-card class="poll-card" shadow="never">
            <div class="poll-header">
              <h1 class="poll-title">{{ poll.title }}</h1>
              <div class="poll-status">
                <el-tag 
                  :type="poll.isExpired ? 'info' : 'success'" 
                  size="large"
                  class="status-tag"
                >
                  {{ poll.isExpired ? '已结束' : '进行中' }}
                </el-tag>
                <el-tag 
                  v-if="poll.isCreator" 
                  type="warning"
                  size="large"
                  class="creator-tag"
                >
                  我创建的
                </el-tag>
              </div>
            </div>
            
            <div class="poll-info">
              <div class="info-item">
                <el-icon><Opportunity /></el-icon>
                <span class="label">投票类型：</span>
                <span>{{ poll.voteType === 0 ? '单选' : '多选' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span class="label">截止时间：</span>
                <span>{{ formatDate(poll.endTime) }}</span>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span class="label">参与人数：</span>
                <span>{{ poll.totalVotes }}人</span>
              </div>
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span class="label">创建时间：</span>
                <span>{{ formatDate(poll.createTime) }}</span>
              </div>
            </div>
            
            <!-- 投票选项 -->
            <div class="poll-options-container">
              <h3 class="section-title">
                {{ hasVoted || poll.isExpired ? '投票结果' : '请选择选项' }}
                <span v-if="poll.voteType === 1 && !hasVoted && !poll.isExpired" class="option-hint">
                  (可多选)
                </span>
              </h3>
              
              <el-radio-group 
                v-if="poll.voteType === 0 && !poll.isExpired && !hasVoted" 
                v-model="selectedOption"
                class="option-group"
              >
                <el-radio 
                  v-for="option in poll.options" 
                  :key="option.id" 
                  :label="option.id"
                  class="option-item"
                  border
                >
                  {{ option.optionText }}
                </el-radio>
              </el-radio-group>
              
              <el-checkbox-group 
                v-if="poll.voteType === 1 && !poll.isExpired && !hasVoted" 
                v-model="selectedOptions"
                class="option-group"
              >
                <el-checkbox 
                  v-for="option in poll.options" 
                  :key="option.id" 
                  :label="option.id"
                  class="option-item"
                  border
                >
                  {{ option.optionText }}
                </el-checkbox>
              </el-checkbox-group>
              
              <!-- 投票结果显示 -->
              <div v-if="poll.isExpired || hasVoted" class="poll-results">
                <div 
                  v-for="(option, index) in sortedOptions" 
                  :key="option.id" 
                  class="result-item"
                  :class="{ 
                    'voted': option.hasVoted,
                    'most-votes': index === 0 && sortedOptions.length > 1 && option.voteCount > 0
                  }"
                >
                  <div class="result-header">
                    <div class="result-text">
                      <span class="option-text">{{ option.optionText }}</span>
                      <div class="vote-stats">
                        <span class="vote-count">{{ option.voteCount }} 票</span>
                        <span class="vote-percent">{{ calculatePercentage(option.voteCount, poll.totalVotes) }}%</span>
                      </div>
                    </div>
                    <el-icon v-if="option.hasVoted" class="voted-icon"><Check /></el-icon>
                    <el-icon v-if="index === 0 && sortedOptions.length > 1 && option.voteCount > 0" class="leading-icon"><Trophy /></el-icon>
                  </div>
                  
                  <div class="result-progress">
                    <el-progress 
                      :percentage="calculatePercentage(option.voteCount, poll.totalVotes)" 
                      :stroke-width="18"
                      :color="getProgressColor(index, option.hasVoted)"
                    ></el-progress>
                  </div>
                </div>
              </div>
              
              <!-- 投票按钮 -->
              <div class="vote-actions" v-if="!poll.isExpired && !hasVoted">
                <el-button 
                  type="primary" 
                  size="large"
                  @click="submitVote" 
                  :disabled="isSubmitDisabled"
                  :loading="submitting"
                >
                  提交投票
                </el-button>
              </div>
              
              <!-- 投票结束信息 -->
              <div class="vote-ended" v-if="poll.isExpired && !hasVoted">
                <el-alert
                  title="投票已结束"
                  type="info"
                  show-icon
                  :closable="false"
                >
                  <template #default>
                    <p>该投票已于 {{ formatDate(poll.endTime) }} 结束，无法继续投票</p>
                  </template>
                </el-alert>
              </div>
              
              <!-- 已投票提示 -->
              <div class="vote-success" v-if="hasVoted && !poll.isExpired">
                <el-alert
                  title="您已成功参与投票"
                  type="success"
                  show-icon
                  :closable="false"
                >
                  <template #default>
                    <p>感谢您的参与，投票将于 {{ formatDate(poll.endTime) }} 结束</p>
                  </template>
                </el-alert>
              </div>
            </div>
          </el-card>
          
          <!-- 投票用户列表或其他信息 -->
          <el-card class="meta-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>投票信息</span>
              </div>
            </template>
            
            <!-- 投票分享区域 -->
            <div class="share-section">
              <h4>分享投票</h4>
              <div class="share-buttons">
                <el-button size="small" @click="copyPollLink">
                  <el-icon><Link /></el-icon>
                  复制链接
                </el-button>
                <!-- 其他分享按钮 -->
              </div>
            </div>
            
            <!-- 投票创建者信息 -->
            <!-- <div class="creator-section">
              <h4>创建者</h4>
              <div class="creator-info">
                <el-avatar :size="40" :src="poll.avatarUrl"></el-avatar>
                <div class="creator-detail">
                  <span class="creator-name">{{ poll.userName }}</span>
                  <span class="creator-time">创建于 {{ formatDate(poll.createTime) }}</span>
                </div>
              </div>
            </div> -->
            <!-- 投票统计 -->
            <div class="stats-section">
              <h4>投票统计</h4>
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="stat-card">
                    <div class="stat-icon"><el-icon><User /></el-icon></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ poll.totalVotes }}</div>
                      <div class="stat-label">参与人数</div>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-card">
                    <div class="stat-icon"><el-icon><List /></el-icon></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ poll.options.length }}</div>
                      <div class="stat-label">选项数</div>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-card">
                    <div class="stat-icon">
                      <el-icon v-if="poll.isExpired"><WarningFilled /></el-icon>
                      <el-icon v-else><Finished /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ poll.isExpired ? '已结束' : '进行中' }}</div>
                      <div class="stat-label">状态</div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </template>
      </div>
      
      <RightSidebar class="right-sidebar" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  Clock, 
  Calendar, 
  Check, 
  Trophy, 
  Link,
  Opportunity,
  List,
  WarningFilled,
  Finished
} from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import CategorySidebar from '@/components/sidebar/CategorySidebar.vue'
import RightSidebar from '@/components/sidebar/RightSidebar.vue'
import pollService from '@/services/poll'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const selectedOption = ref<number | null>(null)
const selectedOptions = ref<number[]>([])
const hasVoted = ref(false)
const creatorName = ref('匿名用户') // 默认创建者名称

// 投票数据
const poll = ref<any>({
  id: 0,
  title: '',
  voteType: 0,
  userId: 0,
  endTime: '',
  createTime: '',
  options: [],
  totalVotes: 0,
  isExpired: false,
  hasVoted: false,
  isCreator: false
})

// 投票选项排序：按票数降序
const sortedOptions = computed(() => {
  return [...poll.value.options].sort((a, b) => {
    // 先按票数排序
    if (b.voteCount !== a.voteCount) {
      return b.voteCount - a.voteCount
    }
    // 票数相同时按ID排序
    return a.id - b.id
  })
})

// 是否可以提交投票
const isSubmitDisabled = computed(() => {
  if (poll.value.voteType === 0) {
    return selectedOption.value === null
  } else {
    return selectedOptions.value.length === 0
  }
})

// 获取投票ID
const pollId = computed(() => route.params.id)

// 加载投票详情
onMounted(async () => {
  if (!pollId.value) {
    ElMessage.error('投票ID不存在')
    router.push('/poll')
    return
  }
  
  await fetchPollDetail()
})

// 获取投票详情
const fetchPollDetail = async () => {
  loading.value = true
  
  try {
    const res = await pollService.getPollDetail(pollId.value)
    
    if (res.code === 0) {
      const pollData = res.data
      
      poll.value = {
        id: pollData.id,
        title: pollData.title,
        voteType: pollData.voteType,
        userId: pollData.userId,
        endTime: pollData.endTime,
        createTime: pollData.createTime,
        options: pollData.options.map((opt: any) => ({
          id: opt.id,
          pollId: opt.voteId,
          optionText: opt.optionText,
          voteCount: opt.voteCount || 0,
          hasVoted: false // 先默认为false，稍后根据用户投票情况更新
        })),
        totalVotes: pollData.totalVotes || 0,
        isExpired: new Date(pollData.endTime) < new Date(),
        hasVoted: pollData.hasVoted || false // 直接使用后端返回的hasVoted字段
      }
      
      // 如果用户已投票，设置hasVoted状态
      hasVoted.value = poll.value.hasVoted
      
      // 通过localStorage检查是否是当前用户创建的投票
      const userInfo = localStorage.getItem('user')
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo)
          if (user.id === poll.value.userId) {
            // 如果是当前用户创建的投票，标记特殊显示
            poll.value.isCreator = true
            creatorName.value = user.username || '匿名用户'
          }
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
      
      // 如果用户已投票，尝试确定用户选择了哪些选项
      // 这里需要后端API返回用户选择的选项或通过本地存储判断
      if (hasVoted.value) {
        // 从本地存储获取用户选择的选项
        const votedRecord = localStorage.getItem(`voted_poll_${pollId.value}`)
        if (votedRecord) {
          try {
            const record = JSON.parse(votedRecord)
            
            // 设置用户已选选项
            if (poll.value.voteType === 0 && record.optionIds.length > 0) {
              selectedOption.value = record.optionIds[0]
              // 标记已选选项
              const option = poll.value.options.find((opt: any) => opt.id === selectedOption.value)
              if (option) {
                option.hasVoted = true
              }
            } else if (poll.value.voteType === 1) {
              selectedOptions.value = record.optionIds
              // 标记已选选项
              record.optionIds.forEach((optId: number) => {
                const option = poll.value.options.find((opt: any) => opt.id === optId)
                if (option) {
                  option.hasVoted = true
                }
              })
            }
          } catch (e) {
            console.error('解析投票记录失败', e)
            // 如果本地存储解析失败，标记第一个选项为已选（作为后备方案）
            if (poll.value.options.length > 0) {
              poll.value.options[0].hasVoted = true
            }
          }
        } else {
          // 如果没有本地存储记录但后端表示已投票，
          // 可以根据投票数标记最可能选择的选项（仅是视觉上的展示）
          if (poll.value.options.length > 0) {
            // 按票数排序
            const sortedOptions = [...poll.value.options].sort((a, b) => b.voteCount - a.voteCount)
            // 标记票数最高的选项为已选
            if (sortedOptions[0]) {
              sortedOptions[0].hasVoted = true
              
              // 对于单选，设置selectedOption
              if (poll.value.voteType === 0) {
                selectedOption.value = sortedOptions[0].id
              }
              // 对于多选，设置selectedOptions为所有有票的选项
              else {
                selectedOptions.value = sortedOptions
                  .filter(opt => opt.voteCount > 0)
                  .map(opt => opt.id)
              }
            }
          }
        }
      }
    } else {
      ElMessage.error(res.message || '获取投票详情失败')
    }
  } catch (error) {
    console.error('获取投票详情失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 提交投票
const submitVote = async () => {
  if (poll.value.isExpired) {
    ElMessage.warning('投票已结束')
    return
  }
  
  if (hasVoted.value) {
    ElMessage.warning('您已参与过此投票')
    return
  }
  
  // 获取选中的选项ID
  const optionIds = poll.value.voteType === 0 
    ? [selectedOption.value]
    : selectedOptions.value
  
  if (!optionIds.length) {
    ElMessage.warning('请选择投票选项')
    return
  }
  
  // 二次确认
  try {
    await ElMessageBox.confirm('确定要提交您的投票吗？', '投票确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    submitting.value = true
    
    try {
      // 调用投票API，使用正确的参数格式
      const res = await pollService.vote(pollId.value, optionIds)
      
      if (res.code === 0) {
        ElMessage.success('投票成功')
        hasVoted.value = true
        
        // 保存投票记录到本地存储
        localStorage.setItem(`voted_poll_${pollId.value}`, JSON.stringify({
          timestamp: new Date().getTime(),
          optionIds: optionIds
        }))
        
        // 刷新投票详情
        await fetchPollDetail()
        
        // 如果后端不会立即更新结果，前端临时更新UI展示
        optionIds.forEach((optId: any) => {
          const option = poll.value.options.find((opt: any) => opt.id === optId)
          if (option) {
            option.hasVoted = true
            // 更新投票计数
            option.voteCount = (option.voteCount || 0) + 1
          }
        })
        
        // 更新总票数
        poll.value.totalVotes += 1
      } else {
        ElMessage.error(res.message || '投票失败')
      }
    } catch (error) {
      console.error('投票失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      submitting.value = false
    }
  } catch {
    // 用户取消投票
  }
}

// 复制投票链接
const copyPollLink = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error('复制失败，请手动复制')
  })
}

// 计算百分比
const calculatePercentage = (count: number, total: number) => {
  if (total === 0) return 0
  return Math.round((count / total) * 100)
}

// 获取进度条颜色
const getProgressColor = (index: number, isVoted: boolean) => {
  if (isVoted) {
    return '#409EFF' // 用户选择的选项
  }
  
  if (index === 0) {
    return '#67C23A' // 票数最多的选项
  } else if (index === 1) {
    return '#E6A23C' // 票数第二的选项
  } else {
    return '#909399' // 其他选项
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.poll-detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  padding-top: 20px;
  flex: 1;
  align-items: flex-start;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 20px);
  max-height: calc(100vh - var(--header-height) - 20px);
  overflow-y: auto;
}

.poll-detail-content {
  flex: 1;
}

.right-sidebar {
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 20px);
  max-height: calc(100vh - var(--header-height) - 20px);
  overflow-y: auto;
}

.poll-card {
  margin-bottom: 20px;
  padding: 10px;
}

.poll-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.poll-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.poll-status {
  display: flex;
  gap: 10px;
}

.status-tag, .creator-tag {
  font-size: 14px;
  font-weight: 500;
}

.poll-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 30px;
  font-size: 14px;
  color: #606266;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-item .el-icon {
  margin-right: 2px;
  font-size: 16px;
  color: #909399;
}

.label {
  font-weight: 500;
  margin-right: 5px;
  color: #606266;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #303133;
}

.option-hint {
  font-size: 14px;
  font-weight: normal;
  color: #909399;
}

.poll-options-container {
  margin-top: 30px;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.option-item {
  margin-bottom: 10px;
  width: 100%;
  border-radius: 4px;
  transition: all 0.3s;
  max-width: 600px; /* 限制最大宽度 */
}

.option-item:hover {
  background-color: #f5f7fa;
}

/* 添加选项内容样式，确保统一宽度 */
:deep(.el-radio__label),
:deep(.el-checkbox__label) {
  width: 100%;
  display: inline-block;
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
  padding-right: 10px;
}

:deep(.el-radio),
:deep(.el-checkbox) {
  width: 100%;
  display: flex;
  align-items: flex-start;
}

:deep(.el-radio__input),
:deep(.el-checkbox__input) {
  margin-top: 3px;
}

.poll-results {
  margin-top: 20px;
}

.result-item {
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  background-color: #f8f9fa;
  transition: all 0.3s;
  max-width: 800px; /* 限制结果项的最大宽度 */
}

.result-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.result-item.voted {
  border-color: #409EFF;
  background-color: rgba(64, 158, 255, 0.05);
}

.result-item.most-votes {
  border-color: #67C23A;
  background-color: rgba(103, 194, 58, 0.05);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.result-text {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.option-text {
  font-weight: 500;
  font-size: 15px;
}

.vote-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}

.vote-count {
  color: #606266;
  font-weight: 500;
}

.vote-percent {
  color: #909399;
  font-size: 14px;
}

.voted-icon {
  color: #409EFF;
  font-size: 18px;
  margin-left: 10px;
}

.leading-icon {
  color: #E6A23C;
  font-size: 18px;
  margin-left: 10px;
}

.vote-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.vote-ended, .vote-success {
  margin-top: 20px;
}

.meta-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
}

.share-section, .creator-section, .stats-section {
  margin-bottom: 25px;
}

.share-section h4, .creator-section h4, .stats-section h4 {
  font-size: 16px;
  font-weight: 500;
  margin-top: 0;
  margin-bottom: 15px;
  color: #606266;
}

.share-buttons {
  display: flex;
  gap: 10px;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.creator-detail {
  display: flex;
  flex-direction: column;
}

.creator-name {
  font-weight: 500;
  font-size: 14px;
}

.creator-time {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.stat-card {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409EFF;
  font-size: 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.stat-label {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

@media (max-width: 1200px) {
  .right-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .poll-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .poll-title {
    font-size: 20px;
  }
  
  .poll-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .result-text {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .vote-stats {
    width: 100%;
    justify-content: space-between;
  }
}
</style> 