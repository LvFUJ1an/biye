<template>
  <div class="poll-list-container">
    <NavHeader />
    <div class="main-content container flex gap-20">
      <CategorySidebar class="sidebar" />
      
      <div class="poll-content">
        <div class="section-header">
          <h2 class="section-title">社区投票</h2>
          <el-button type="primary" @click="createPoll" v-if="isLoggedIn">发起投票</el-button>
        </div>
        
        <div class="polls-filter">
          <!-- <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部" name="all"></el-tab-pane>
            <el-tab-pane label="进行中" name="active"></el-tab-pane>
            <el-tab-pane label="已结束" name="ended"></el-tab-pane>
            <el-tab-pane label="我参与的" name="participated" v-if="isLoggedIn"></el-tab-pane>
            <el-tab-pane label="我发起的" name="created" v-if="isLoggedIn"></el-tab-pane>
          </el-tabs> -->
          
          <div class="search-sort">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索投票" 
              class="search-input"
              @input="handleSearch"
            >
              <template #append>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <!-- <el-select v-model="sortBy" placeholder="排序方式" @change="handleSort">
              <el-option label="最新发布" value="newest"></el-option>
              <el-option label="最多参与" value="mostVoted"></el-option>
              <el-option label="即将结束" value="endingSoon"></el-option>
            </el-select> -->
          </div>
        </div>
        
        <div class="polls-list">
          <el-skeleton :rows="5" animated v-if="loading" />
          
          <template v-else>
            <!-- 无数据提示 -->
            <el-empty 
              v-if="polls.length === 0" 
              description="暂无投票数据" 
              :image-size="200"
            >
              <template #default>
                <el-button type="primary" @click="createPoll" v-if="isLoggedIn">
                  发起第一个投票
                </el-button>
              </template>
            </el-empty>
            
            <!-- 投票列表 -->
            <el-card 
              v-for="poll in polls" 
              :key="poll.id" 
              class="poll-card"
              shadow="hover"
              @click="viewPollDetail(poll.id)"
            >
              <div class="poll-header">
                <h3 class="poll-title">{{ poll.title }}</h3>
                <el-tag 
                  :type="poll.isExpired ? 'info' : 'success'" 
                  size="small"
                >
                  {{ poll.isExpired ? '已结束' : '进行中' }}
                </el-tag>
              </div>
              
              <div class="poll-info">
                <span class="poll-type">{{ poll.voteType === 0 ? '单选' : '多选' }}</span>
                <span class="poll-count">{{ poll.totalVotes }}人参与</span>
                <span class="poll-time">截止时间: {{ formatDate(poll.endTime) }}</span>
              </div>
              
              <div class="poll-options">
                <div 
                  v-for="(option, index) in poll.options.slice(0, 2)" 
                  :key="option.id" 
                  class="option-preview"
                >
                  <div class="option-text">{{ option.optionText }}</div>
                  <div class="option-progress">
                    <el-progress 
                      :percentage="calculatePercentage(option.voteCount, poll.totalVotes)" 
                      :stroke-width="12"
                      :color="getProgressColor(index)"
                    ></el-progress>
                  </div>
                </div>
                
                <div class="more-options" v-if="poll.options.length > 2">
                  +{{ poll.options.length - 2 }}个选项
                </div>
              </div>
              
              <div class="poll-footer">
                <el-button text type="primary" @click.stop="viewPollDetail(poll.id)">
                  {{ poll.hasVoted ? '查看结果' : '去投票' }}
                </el-button>
              </div>
            </el-card>
            
            <!-- 分页 -->
            <div class="pagination" v-if="polls.length > 0">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </template>
        </div>
      </div>
      
      <RightSidebar class="right-sidebar" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import NavHeader from '@/components/layout/NavHeader.vue'
import CategorySidebar from '@/components/sidebar/CategorySidebar.vue'
import RightSidebar from '@/components/sidebar/RightSidebar.vue'
import pollService from '@/services/poll'

const router = useRouter()
const loading = ref(true)
const polls = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const sortBy = ref('newest')
const activeTab = ref('all')

// 检查登录状态
const isLoggedIn = ref(false)
onMounted(() => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  
  // 加载投票数据
  fetchPolls()
})

// 获取投票列表
const fetchPolls = async () => {
  loading.value = true;
  
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      sort: sortBy.value,
      status: getStatusByTab(activeTab.value)
    }
    
    const res = await pollService.getPollList(params);
    if (res.code === 0) {
      // 适配后端返回的数据结构
      const { records, total: totalCount, current, size } = res.data;
      
      // 转换后端数据为前端需要的格式
      polls.value = records.map((poll: any) => {
        return {
          id: poll.id,
          title: poll.title,
          voteType: poll.voteType,
          userId: poll.userId,
          endTime: poll.endTime,
          createTime: poll.createTime,
          // 处理选项数据
          options: poll.options.map((opt: any) => ({
            id: opt.id,
            pollId: opt.voteId,
            optionText: opt.optionText,
            voteCount: opt.voteCount || 0,
            hasVoted: false // 默认未投票
          })),
          // 计算总投票数
          totalVotes: poll.options.reduce((sum: number, opt: any) => sum + (opt.voteCount || 0), 0),
          // 判断是否过期
          isExpired: new Date(poll.endTime) < new Date(),
          hasVoted: false // 默认未投票
        };
      });
      
      total.value = totalCount || 0;
      // 更新当前页和页大小（如果后端有返回）
      if (current) currentPage.value = current;
      if (size) pageSize.value = size;
    } else {
      ElMessage.error(res.message || '获取投票列表失败');
    }
  } catch (error) {
    console.error('获取投票列表失败:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 根据标签获取状态过滤
const getStatusByTab = (tab: string) => {
  switch (tab) {
    case 'active':
      return 'active'
    case 'ended':
      return 'ended'
    case 'participated':
      return 'participated'
    case 'created':
      return 'created'
    default:
      return 'all'
  }
}

// 处理标签切换
const handleTabChange = () => {
  currentPage.value = 1
  fetchPolls()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPolls()
}

// 处理排序方式变更
const handleSort = () => {
  currentPage.value = 1
  fetchPolls()
}

// 处理页码变更
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchPolls()
}

// 处理每页数量变更
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchPolls()
}

// 查看投票详情
const viewPollDetail = (id: number) => {
  router.push(`/poll/detail/${id}`)
}

// 创建投票
const createPoll = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再发起投票')
    return
  }
  
  router.push('/poll/create')
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 计算百分比
const calculatePercentage = (count: number, total: number) => {
  if (total === 0) return 0
  return Math.round((count / total) * 100)
}

// 获取进度条颜色
const getProgressColor = (index: number) => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
  return colors[index % colors.length]
}
</script>

<style scoped>
.poll-list-container {
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

.poll-content {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.right-sidebar {
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 20px);
  max-height: calc(100vh - var(--header-height) - 20px);
  overflow-y: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.polls-filter {
  margin-bottom: 20px;
}

.search-sort {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.search-input {
  flex: 1;
}

.polls-list {
  margin-top: 20px;
}

.poll-card {
  margin-bottom: 15px;
  cursor: pointer;
  transition: transform 0.2s;
}

.poll-card:hover {
  transform: translateY(-2px);
}

.poll-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.poll-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.poll-info {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 15px;
}

.poll-options {
  margin-bottom: 15px;
}

.option-preview {
  margin-bottom: 8px;
}

.option-text {
  font-size: 14px;
  margin-bottom: 4px;
}

.more-options {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
}

.poll-footer {
  display: flex;
  justify-content: flex-end;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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
  
  .search-sort {
    flex-direction: column;
  }
}
</style> 