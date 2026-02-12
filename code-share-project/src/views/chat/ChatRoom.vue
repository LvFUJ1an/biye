<template>
  <div class="chat-room">
    <NavHeader />
    
    <div class="main-content container">
      <div class="chat-container">
        <!-- 聊天室头部 -->
        <div class="chat-header">
          <div class="chat-title">
            <el-icon><ChatLineRound /></el-icon>
            <h1>技术交流聊天室</h1>
          </div>
          <div class="header-actions">
            <div class="connection-status" :class="{ connected: chatService.state.connected }">
              <span class="status-dot"></span>
              <span class="status-text">{{ chatService.state.connected ? '已连接' : chatService.state.reconnecting ? '重新连接中...' : '未连接' }}</span>
            </div>
            <el-button 
              type="danger" 
              size="small" 
              plain 
              @click="exitChatRoom"
              class="exit-button"
            >
              <el-icon><Back /></el-icon> 退出聊天室
            </el-button>
          </div>
        </div>
        
        <!-- 聊天内容区域 -->
        <div class="chat-body">
          <!-- 聊天消息列表 -->
          <div class="messages-container">
            <div class="system-messages">
              <el-alert 
                v-for="(message, index) in systemMessages" 
                :key="index" 
                :title="message.content" 
                type="info" 
                :closable="false" 
                center 
                show-icon
              />
            </div>
            
            <div class="messages-list" ref="messagesListRef" @scroll="handleScroll">
              <div 
                v-for="message in filteredMessages" 
                :key="message.id" 
                class="message-item"
                :class="{ 
                  'message-self': message.isSelf,
                  'message-system': message.type === 'system'
                }"
              >
                <!-- 系统消息 -->
                <template v-if="message.type === 'system'">
                  <div class="system-message-content">{{ message.content }}</div>
                </template>
                
                <!-- 普通消息 -->
                <template v-else>
                  <div class="message-avatar">
                    <el-avatar :size="36" :src="message.senderAvatar" />
                  </div>
                  <div class="message-content">
                    <div class="message-header">
                      <span class="message-sender">{{ message.senderName }}</span>
                      <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                    </div>
                    <div class="message-text">{{ message.content }}</div>
                  </div>
                </template>
              </div>
              
              <!-- 底部占位，用于滚动 -->
              <div class="scroll-anchor" ref="scrollAnchor"></div>
            </div>
            
            <div class="new-messages-notice" v-if="userScrolled && newMessagesCount > 0" @click="scrollToNewMessages">
              {{ newMessagesCount }}条新消息 <el-icon><ArrowDown /></el-icon>
            </div>
          </div>
          
          <!-- 用户列表 -->
          <div class="users-container">
            <div class="users-header">
              <h3>在线用户 ({{ chatService.state.users.length }})</h3>
            </div>
            <div class="users-list">
              <div 
                v-for="user in chatService.state.users" 
                :key="user.id" 
                class="user-item"
                :class="{ 'current-user': chatService.currentUser?.id === user.id }"
              >
                <el-avatar :size="32" :src="user.avatarUrl" />
                <div class="user-info">
                  <span class="user-name">{{ user.username }}</span>
                </div>
                <div class="user-status">
                  <el-tag 
                    size="small" 
                    :type="user.status === 'online' ? 'success' : user.status === 'away' ? 'warning' : 'info'"
                  >
                    {{ user.status === 'online' ? '在线' : user.status === 'away' ? '离开' : '离线' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 聊天输入区域 -->
        <div class="chat-footer">
          <div class="message-input">
            <el-input
              v-model="messageText"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              @input="handleInput"
              @keydown.enter.exact.prevent="sendMessage"
              @focus="handleFocus"
              @blur="handleBlur"
              :disabled="!chatService.state.connected"
            />
          </div>
          <div class="message-actions">
            <el-button 
              type="primary"
              @click="sendMessage"
              :disabled="!chatService.state.connected || !messageText.trim()"
              :loading="sending"
            >
              发送
              <el-icon class="el-icon--right"><Position /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ChatLineRound, Position, ArrowDown, Back } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import NavHeader from '@/components/layout/NavHeader.vue'
import chatService from '@/services/chat'

const router = useRouter()

// 消息输入和发送
const messageText = ref('')
const sending = ref(false)
const messagesListRef = ref(null)
const scrollAnchor = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 过滤后的消息列表，去除无效消息和重复ID的消息，同时排除关注通知
const filteredMessages = computed(() => {
  const uniqueMessages = new Map()
  
  return chatService.state.messages.filter(msg => {
    // 排除关注消息类型
    if (msg.type === 'follow') {
      return false
    }
    
    // 过滤掉内容为空或只包含数字的无效消息
    if (!msg.content.trim() || /^\d+$/.test(msg.content.trim())) {
      return false
    }
    
    // 过滤掉包含"加入了聊天室"或"离开了聊天室"的系统消息
    if (msg.type === 'system' && 
       (msg.content.includes('加入了聊天室') || 
        msg.content.includes('离开了聊天室'))) {
      return false
    }
    
    // 检查消息ID是否已存在，保留第一个出现的消息
    if (uniqueMessages.has(msg.id)) {
      return false
    }
    
    uniqueMessages.set(msg.id, true)
    return true
  })
})

// 系统消息
const systemMessages = computed(() => 
  filteredMessages.value.filter(msg => msg.type === 'system').slice(-3)
)

// 连接聊天服务
const connect = async () => {
  const success = chatService.connectWebSocket()
  
  if (!success) {
    setTimeout(() => {
      ElMessage.warning('连接聊天服务失败，3秒后自动重试...')
      connect()
    }, 3000)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim() || !chatService.state.connected) {
    return
  }
  
  sending.value = true
  
  try {
    const success = chatService.sendMessage(messageText.value)
    
    if (success) {
      messageText.value = ''
    }
  } finally {
    sending.value = false
  }
}

// 处理输入事件
const handleInput = () => {
  // 不做任何处理
}

// 处理输入框聚焦
const handleFocus = () => {
  // 不做任何处理
}

// 处理输入框失焦
const handleBlur = () => {
  // 不做任何处理
}

// 用户是否手动滚动
const userScrolled = ref(false)

// 监听消息列表滚动
const handleScroll = () => {
  if (!messagesListRef.value) return
  
  const container = messagesListRef.value
  const isAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 50
  
  userScrolled.value = !isAtBottom
}

// 滚动到底部
const scrollToBottom = () => {
  if (scrollAnchor.value && !userScrolled.value) {
    scrollAnchor.value.scrollIntoView({ behavior: 'smooth' })
  }
}

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffSec = Math.floor(diffMs / 1000)
  const diffMin = Math.floor(diffSec / 60)
  const diffHour = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHour / 24)
  
  // 显示相对时间
  if (diffSec < 60) {
    return '刚刚'
  } else if (diffMin < 60) {
    return `${diffMin}分钟前`
  } else if (diffHour < 24) {
    return `${diffHour}小时前`
  } else if (diffDay < 30) {
    return `${diffDay}天前`
  }
  
  // 如果是今天的消息，只显示时间
  if (
    date.getDate() === now.getDate() &&
    date.getMonth() === now.getMonth() &&
    date.getFullYear() === now.getFullYear()
  ) {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  
  // 否则显示日期和时间
  return date.toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 新消息计数
const lastSeenMessageId = ref('')
const newMessagesCount = computed(() => {
  if (!userScrolled.value) {
    lastSeenMessageId.value = filteredMessages.value.length > 0 
      ? filteredMessages.value[filteredMessages.value.length - 1].id 
      : ''
    return 0
  }
  
  if (!lastSeenMessageId.value || filteredMessages.value.length === 0) return 0
  
  const lastSeenIndex = filteredMessages.value.findIndex(m => m.id === lastSeenMessageId.value)
  if (lastSeenIndex === -1) return filteredMessages.value.length
  
  return filteredMessages.value.length - lastSeenIndex - 1
})

// 滚动到新消息
const scrollToNewMessages = () => {
  userScrolled.value = false
  scrollToBottom()
}

// 监听消息列表变化，自动滚动到底部
watch(
  () => filteredMessages.value.length,
  () => {
    nextTick(() => {
      scrollToBottom()
    })
  }
)

// 退出聊天室
const exitChatRoom = () => {
  // 首先断开WebSocket连接
  chatService.disconnectWebSocket()
  
  // 跳转回首页
  router.push('/')
}

// 组件挂载时连接聊天服务并添加滚动监听
onMounted(() => {
  connect()
  
  // 添加滚动事件监听
  if (messagesListRef.value) {
    messagesListRef.value.addEventListener('scroll', handleScroll)
  }
})

// 组件卸载前断开聊天服务并移除滚动监听
onBeforeUnmount(() => {
  // 移除滚动事件监听
  if (messagesListRef.value) {
    messagesListRef.value.removeEventListener('scroll', handleScroll)
  }
  
  chatService.disconnectWebSocket()
})
</script>

<style scoped>
.chat-room {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.chat-container {
  width: 100%;
  max-width: 1200px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--header-height) - 40px);
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.chat-title .el-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #909399;
}

.connection-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.connection-status.connected .status-dot {
  background-color: #67c23a;
}

.connection-status .status-text {
  font-weight: 500;
}

.exit-button {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
}

.exit-button:hover {
  background-color: #f56c6c;
  color: white;
}

.chat-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #ebeef5;
}

.system-messages {
  padding: 8px;
  max-height: 120px;
  overflow-y: auto;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  max-width: 80%;
}

.message-self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-system {
  align-self: center;
  background-color: #f0f9eb;
  border-radius: 16px;
  padding: 8px 16px;
  font-size: 13px;
  color: #67c23a;
  max-width: 60%;
}

.message-avatar {
  margin: 0 12px;
}

.message-content {
  background-color: #f5f7fa;
  padding: 10px 14px;
  border-radius: 8px;
  position: relative;
}

.message-self .message-content {
  background-color: #ecf5ff;
  color: #303133;
}

.message-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 4px;
}

.message-sender {
  font-weight: 500;
  font-size: 14px;
  color: #606266;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.users-container {
  width: 260px;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.users-header {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fff;
}

.users-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.users-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.user-item:hover {
  background-color: #ecf5ff;
}

.current-user {
  background-color: #ecf5ff;
}

.user-info {
  margin-left: 12px;
  flex: 1;
  overflow: hidden;
}

.user-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  margin-left: 8px;
}

.chat-footer {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-input {
  width: 100%;
}

.message-actions {
  display: flex;
  justify-content: flex-end;
}

/* 输入指示器 */
.typing-indicator {
  display: none;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .chat-body {
    flex-direction: column;
  }
  
  .messages-container {
    border-right: none;
    border-bottom: 1px solid #ebeef5;
  }
  
  .users-container {
    width: 100%;
    height: 200px;
  }
  
  .message-item {
    max-width: 90%;
  }
}

.new-messages-notice {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: var(--primary-color);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}
</style> 