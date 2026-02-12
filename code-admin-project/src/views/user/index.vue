<template>
  <div class="user-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">用户管理</span>
          <div class="header-operations">
            <el-input
              v-model="queryParams.keyword"
              placeholder="输入用户名或邮箱搜索"
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
          </div>
        </div>
      </template>

      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="id" label="用户ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="100" show-overflow-tooltip />
        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar 
              v-if="scope.row.avatarUrl" 
              :src="scope.row.avatarUrl"
              :size="40"
            />
            <el-avatar v-else :size="40">
              {{ scope.row.username.substring(0, 1).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.role === 1">管理员</el-tag>
            <el-tag v-else>普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="150" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.statusSwitch"
              :active-value="false"
              :inactive-value="true"
              @change="handleStatusChange(scope.row)"
              :disabled="scope.row.role === 1"
              active-color="#13ce66"
              inactive-color="#ff4949"
              active-text="禁用"
              inactive-text="启用"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" width="170">
          <template #default="scope">
            {{ scope.row.lastLogin ? formatDate(scope.row.lastLogin) : '未登录' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="showUserDetails(scope.row)"
            >
              详情
            </el-button>
            <el-button
              type="warning"
              link
              @click="handleResetPassword(scope.row)"
              :disabled="scope.row.role === 1 && scope.row.username === 'admin'"
            >
              重置密码
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(scope.row)"
              :disabled="scope.row.role === 1 && scope.row.username === 'admin'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="用户详情"
      width="500px"
      destroy-on-close
    >
      <div v-if="currentUser" class="user-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag type="danger" v-if="currentUser.role === 1">管理员</el-tag>
            <el-tag v-else>普通用户</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag type="success" v-if="currentUser.isActive === 0">正常</el-tag>
            <el-tag type="danger" v-else>禁用</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(currentUser.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="最后更新">
            {{ formatDate(currentUser.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="最后登录">
            {{ currentUser.lastLogin ? formatDate(currentUser.lastLogin) : '未登录' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserList, enableUser, disableUser, resetUserPassword, deleteUser } from '../../api/user'
import { message, confirm } from '../../utils/message'
import { Search, Refresh } from '@element-plus/icons-vue'

// 定义用户类型
interface User {
  id: number
  username: string
  password: string
  email: string
  avatarUrl: string
  role: number
  isActive: number
  statusSwitch?: boolean // 用于UI显示的开关状态
  createTime: string
  updateTime: string
  lastLogin: string | null
}

// 加载状态
const loading = ref(false)
// 用户列表
const userList = ref<User[]>([])
// 总数
const total = ref(0)
// 当前选中的用户
const currentUser = ref<User | null>(null)
// 对话框可见性
const dialogVisible = ref(false)

// 查询参数
const queryParams = reactive({
  keyword: '',
  pageSize: 10,
  currentPage: 1
})

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: queryParams.keyword,
      pageSize: queryParams.pageSize,
      currentPage: queryParams.currentPage
    })
    
    if (res.code === 0) {
      // 处理用户数据，添加statusSwitch属性用于UI显示
      userList.value = res.data.records.map(user => ({
        ...user,
        // 由于UI上已经调整了active-text和inactive-text的位置
        // 现在active-text是"禁用"，inactive-text是"启用"
        // 所以逻辑需要相应调整：isActive为0表示启用，对应statusSwitch为true
        statusSwitch: user.isActive === 0
      }))
      total.value = res.data.total
    } else {
      message.error(res.message || '获取用户列表失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取用户列表失败')
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

// 搜索
const handleQuery = () => {
  queryParams.currentPage = 1
  getList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.currentPage = 1
  getList()
}

// 显示用户详情
const showUserDetails = (user: User) => {
  currentUser.value = { ...user }
  dialogVisible.value = true
}

// 改变用户状态
const handleStatusChange = async (row: User) => {
  // 由于UI上的文本已调整，active-text是"禁用"，inactive-text是"启用"
  // 当statusSwitch为false时表示"启用"状态，为true表示"禁用"状态
  // 因此isEnabled的逻辑需要调整
  const isEnabled = row.statusSwitch
  const actionText = isEnabled ? '禁用' : '启用'
  
  // 管理员不能被禁用
  if (row.role === 1 && isEnabled) {
    message.warning('管理员账户不能被禁用')
    row.statusSwitch = false // 恢复开关状态为启用
    return
  }
  
  // 确认操作
  const confirmed = await confirm.show(
    `确定要${actionText}用户 "${row.username}" 吗？`,
    '操作确认',
    'warning'
  )
  
  if (!confirmed) {
    // 用户取消，恢复开关状态
    row.statusSwitch = !row.statusSwitch
    return
  }
  
  try {
    // 根据操作类型调用不同的API
    let res
    if (isEnabled) {
      // 禁用用户
      res = await disableUser(row.id)
    } else {
      // 启用用户
      res = await enableUser(row.id)
    }
    
    if (res.code === 0) {
      message.success(`${actionText}用户成功`)
      // 更新用户的isActive状态：0是启用，1是禁用
      row.isActive = isEnabled ? 1 : 0
    } else {
      message.error(res.message || `${actionText}用户失败`)
      // 恢复开关状态
      row.statusSwitch = !row.statusSwitch
    }
  } catch (error: any) {
    message.error(error.message || `${actionText}用户失败`)
    // 恢复开关状态
    row.statusSwitch = !row.statusSwitch
  }
}

// 页面大小变化
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  getList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  queryParams.currentPage = page
  getList()
}

// 重置用户密码
const handleResetPassword = async (row: User) => {
  // 确认操作
  const confirmed = await confirm.show(
    `确定要重置用户 "${row.username}" 的密码吗？`,
    '重置密码确认',
    'warning',
    '确定重置',
    '取消'
  )
  
  if (!confirmed) {
    return
  }
  
  try {
    const res = await resetUserPassword(row.id)
    
    if (res.code === 0) {
      message.success(`已成功重置用户 "${row.username}" 的密码`)
    } else {
      message.error(res.message || '重置密码失败')
    }
  } catch (error: any) {
    message.error(error.message || '重置密码失败，请稍后重试')
  }
}

// 删除用户
const handleDelete = async (row: User) => {
  // 确认操作
  const confirmed = await confirm.show(
    `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
    '删除用户确认',
    'error',
    '确定删除',
    '取消'
  )
  
  if (!confirmed) {
    return
  }
  
  try {
    const res = await deleteUser(row.id)
    
    if (res.code === 0) {
      message.success(`已成功删除用户 "${row.username}"`)
      // 重新加载列表数据
      getList()
    } else {
      message.error(res.message || '删除用户失败')
    }
  } catch (error: any) {
    message.error(error.message || '删除用户失败，请稍后重试')
  }
}

// 页面初始化时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.user-card {
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

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.user-detail {
  padding: 10px;
}
</style> 