<template>
  <header class="nav-header">
    <div class="container flex justify-between align-center">
      <div class="header-left flex align-center gap-20">
        <div class="logo">
          <img src="@/assets/logo.svg" alt="IT社区" class="logo-img" />
          <span class="logo-text">IT技术社区</span>
        </div>
        <nav class="main-nav">
          <ul class="nav-list flex gap-10">
            <li v-for="(nav, index) in navItems" :key="index" class="nav-item">
              <router-link :to="nav.path" class="nav-link" :class="{ active: $route.path === nav.path }">{{ nav.name }}</router-link>
            </li>
          </ul>
        </nav>
      </div>
      <div class="header-right flex align-center gap-10">
        <!-- <el-input placeholder="搜索内容" class="search-input">
          <template #append>
            <el-icon><Search /></el-icon>
          </template>
        </el-input> -->
        
        <!-- 创作者中心下拉按钮 -->
        <div class="creator-center-dropdown">
          <el-button type="primary" class="creator-btn">
            创作者中心
            <el-icon class="el-icon--right"><CaretBottom /></el-icon>
          </el-button>
          <div class="dropdown-menu">
            <div class="menu-item" v-for="(item, index) in creatorMenuItems" :key="index" @click="handleCreatorAction(item.action)">
              <div class="menu-icon" :style="{ backgroundColor: item.iconBg }">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="menu-info">
                <span class="menu-title">{{ item.title }}</span>
                <span class="menu-desc">{{ item.description }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="user-info flex align-center gap-10">
          <!-- AI对话图标 -->
          <div class="ai-chat-icon" @click="showAiChatDrawer = true">
            <el-tooltip content="AI助手" placement="bottom">
              <el-icon class="ai-icon"><ChatDotRound /></el-icon>
            </el-tooltip>
          </div>
          
          <!-- 聊天室图标 -->
          <div class="chat-room-icon" @click="goToChatRoom">
            <el-tooltip content="聊天室" placement="bottom">
              <el-icon class="chat-icon"><ChatLineRound /></el-icon>
            </el-tooltip>
          </div>
          
          <template v-if="isLoggedIn">
            <!-- <el-badge :value="3" class="notification-badge">
              <el-icon><Bell /></el-icon>
            </el-badge> -->
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="avatar-wrapper">
                <el-avatar :size="36" :src="userInfo.avatarUrl || defaultAvatar"></el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                  <el-dropdown-item command="settings">设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="showAuthModal = true">登录/注册</el-button>
          </template>
        </div>
      </div>
    </div>
    
    <!-- 登录/注册模态框 -->
    <AuthModal 
      :visible="showAuthModal" 
      @close="showAuthModal = false"
      @login-success="handleLoginSuccess"
    />
    
    <!-- AI聊天抽屉 -->
    <el-drawer
      v-model="showAiChatDrawer"
      title="AI助手对话"
      size="60%"
      :destroy-on-close="false"
      :with-header="true"
    >
      <div class="ai-chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div 
            v-for="(msg, index) in chatMessages" 
            :key="index" 
            class="message-item"
            :class="msg.isUser ? 'user-message' : 'ai-message'"
          >
            <div class="message-avatar">
              <el-avatar :size="36" :src="msg.isUser ? userInfo.avatarUrl || defaultAvatar : aiAvatar"></el-avatar>
            </div>
            <div class="message-content">
              <div class="message-name">{{ msg.isUser ? (userInfo.username || '您') : 'AI助手' }}</div>
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>
          <div v-if="isAiTyping && !isReceivingMessages" class="message-item ai-message">
            <div class="message-avatar">
              <el-avatar :size="36" :src="aiAvatar"></el-avatar>
            </div>
            <div class="message-content">
              <div class="message-name">AI助手</div>
              <div class="message-text typing-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="userMessage"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题..."
            resize="none"
            @keyup.enter.exact.prevent="sendMessage"
          />
          <el-button 
            type="primary" 
            :disabled="!userMessage.trim() || isAiTyping" 
            @click="sendMessage"
          >
            <el-icon><Position /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </el-drawer>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { 
  Bell, 
  Search, 
  CaretBottom, 
  Edit, 
  ChatLineSquare, 
  VideoCamera, 
  ChatDotSquare,
  ChatDotRound,
  ChatLineRound,
  Position
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AuthModal from '@/components/auth/AuthModal.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 创作者中心菜单项
const creatorMenuItems = ref([
  { 
    title: '写博客', 
    description: '分享你的技术见解', 
    icon: Edit, 
    action: 'write-blog',
    iconBg: '#409EFF' 
  },
  { 
    title: '提问答', 
    description: '寻求专业技术解答', 
    icon: ChatLineSquare, 
    action: 'ask-question',
    iconBg: '#67C23A' 
  },
  { 
    title: '发视频', 
    description: '上传技术教程视频', 
    icon: VideoCamera, 
    action: 'upload-video',
    iconBg: '#E6A23C' 
  },
  { 
    title: '去投票', 
    description: '发起技术话题投票', 
    icon: ChatDotSquare, 
    action: 'create-poll',
    iconBg: '#F56C6C' 
  }
])

// 导航项
const navItems = ref([
  { name: '首页', active: true, path: '/' },
  // { name: '博客', active: false, path: '/blog' },
  { name: '问答', active: false, path: '/question' },
  { name: '视频', active: false, path: '/video' },
  { name: '电子书', active: false, path: '/ebook' },
  { name: '投票', active: false, path: '/poll' }
])

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 登录状态
const isLoggedIn = ref(false)
const userInfo = ref({
  username: '',
  email: '',
  avatarUrl: '',
  role: 0
})

// 登录/注册模态框显示状态
const showAuthModal = ref(false)

// AI聊天功能相关
const showAiChatDrawer = ref(false)
const userMessage = ref('')
const chatMessages = ref<Array<{isUser: boolean, content: string}>>([
  {
    isUser: false,
    content: '您好！我是AI助手，有什么可以帮助您的吗？'
  }
])
const isAiTyping = ref(false)
const isReceivingMessages = ref(false)
const chatMessagesRef = ref<HTMLElement | null>(null)
const aiAvatar = 'http://110.42.213.154:9000/wallpaper/avatar/0b4e3667c6dc40d59efcaca4fba1edcf.jpg'

// 初始化检查登录状态
onMounted(() => {
  checkLoginStatus()
})

// 处理创作者中心菜单操作
const handleCreatorAction = (action: string) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再进行创作')
    showAuthModal.value = true
    return
  }
  
  switch (action) {
    case 'write-blog':
      router.push('/editor/blog')
      break
    case 'ask-question':
      router.push('/question/create')
      break
    case 'upload-video':
      router.push('/video/create')
      break
    case 'create-poll':
      router.push('/poll/create')
      break
  }
}

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  const user = localStorage.getItem('user')
  
  if (token && user) {
    try {
      userInfo.value = JSON.parse(user)
      isLoggedIn.value = true
    } catch (error) {
      console.error('解析用户信息失败:', error)
      clearUserInfo()
    }
  }
}

// 清除用户信息
const clearUserInfo = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  userInfo.value = {
    username: '',
    email: '',
    avatarUrl: '',
    role: 0
  }
  isLoggedIn.value = false
}

// 处理用户菜单命令
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      // 跳转到个人主页
      router.push('/profile')
      break
    case 'settings':
      // 跳转到设置页面
      console.log('跳转到设置页面')
      break
    case 'logout':
      // 退出登录
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        clearUserInfo()
        ElMessage.success('已退出登录')
      }).catch(() => {})
      break
  }
}

// 登录成功回调
const handleLoginSuccess = (userData: any) => {
  userInfo.value = {
    username: userData.username,
    email: userData.email,
    avatarUrl: userData.avatarUrl,
    role: userData.role
  }
  isLoggedIn.value = true
}

// 发送消息到AI
const sendMessage = async () => {
  if (!userMessage.value.trim() || isAiTyping.value) return
  
  // 添加用户消息
  const userMsg = userMessage.value.trim()
  chatMessages.value.push({
    isUser: true,
    content: userMsg
  })
  
  // 清空输入框
  userMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 显示AI正在输入
  isAiTyping.value = true
  isReceivingMessages.value = false
  
  try {
    // 使用 EventSource 获取流式响应
    const baseUrl = 'http://localhost:18000'
    const eventSource = new EventSource(`${baseUrl}/chat/stream?userMessage=${encodeURIComponent(userMsg)}`)
    
    // 初始化AI回复
    let aiResponse = ''
    let currentAiMessageIndex = -1
    
    // 监听消息事件
    eventSource.onmessage = (event) => {
      if (event.data === '[DONE]') {
        // 流式响应结束
        eventSource.close()
        isAiTyping.value = false
        isReceivingMessages.value = false
        nextTick(() => scrollToBottom())
      } else {
        try {
          // 解析JSON格式数据
          const data = JSON.parse(event.data)
          // 从choices数组中提取内容
          if (data.choices && data.choices.length > 0 && data.choices[0].delta?.content) {
            const newContent = data.choices[0].delta.content
            
            // 第一次收到消息时创建一条新的AI消息
            if (currentAiMessageIndex === -1) {
              isReceivingMessages.value = true // 标记正在接收消息内容
              chatMessages.value.push({
                isUser: false,
                content: newContent
              })
              currentAiMessageIndex = chatMessages.value.length - 1
              aiResponse = newContent
            } else {
              // 更新已有的消息内容（实现打字机效果）
              aiResponse += newContent
              chatMessages.value[currentAiMessageIndex].content = aiResponse
            }
            
            // 每次更新内容后滚动到底部
            nextTick(() => scrollToBottom())
          }
        } catch (error) {
          console.error('解析AI响应数据失败:', error)
          // 如果解析失败，则尝试直接使用原始数据
          if (event.data && typeof event.data === 'string') {
            if (currentAiMessageIndex === -1) {
              isReceivingMessages.value = true // 标记正在接收消息内容
              chatMessages.value.push({
                isUser: false,
                content: event.data
              })
              currentAiMessageIndex = chatMessages.value.length - 1
              aiResponse = event.data
            } else {
              aiResponse += event.data
              chatMessages.value[currentAiMessageIndex].content = aiResponse
            }
            nextTick(() => scrollToBottom())
          }
        }
      }
    }
    
    // 监听错误
    eventSource.onerror = (error) => {
      console.error('EventSource error:', error)
      eventSource.close()
      isAiTyping.value = false
      isReceivingMessages.value = false
      
      // 添加错误消息
      chatMessages.value.push({
        isUser: false,
        content: '抱歉，与AI通信出现错误，请稍后再试。'
      })
      
      nextTick(() => scrollToBottom())
    }
  } catch (error) {
    console.error('AI聊天出错:', error)
    isAiTyping.value = false
    isReceivingMessages.value = false
    
    // 添加错误消息
    chatMessages.value.push({
      isUser: false,
      content: '抱歉，与AI通信出现错误，请稍后再试。'
    })
    
    nextTick(() => scrollToBottom())
  }
}

// 滚动聊天区域到底部
const scrollToBottom = () => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 格式化消息内容（处理换行和代码块）
const formatMessage = (text: string) => {
  if (!text) return ''
  
  // 替换换行符为<br>
  let formattedText = text.replace(/\n/g, '<br>')
  
  // 检测并格式化代码块（使用```包裹的内容）
  const codeBlockRegex = /```([^`]+)```/g
  formattedText = formattedText.replace(codeBlockRegex, (match, code) => {
    return `<pre class="code-block"><code>${code.trim()}</code></pre>`
  })
  
  return formattedText
}

// 添加进入聊天室的方法
const goToChatRoom = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再进入聊天室')
    showAuthModal.value = true
    return
  }
  
  router.push('/chat')
}
</script>

<style scoped>
.nav-header {
  height: var(--header-height);
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
}

.container {
  width: 100%;
  max-width: 1440px;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-img {
  height: 36px;
  width: auto;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: var(--primary-color);
}

.nav-list {
  list-style: none;
}

.nav-item {
  padding: 0 5px;
}

.nav-link {
  color: var(--text-color);
  font-size: 16px;
  display: block;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
  text-decoration: none;
}

.nav-link:hover, .nav-link.active {
  color: var(--primary-color);
  background-color: rgba(64, 158, 255, 0.1);
}

.search-input {
  width: 20rem;
}

/* 创作者中心样式 */
.creator-center-dropdown {
  position: relative;
  margin: 0 8px;
}

.creator-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  padding: 8px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(10px);
  transition: all 0.3s ease;
  z-index: 100;
}

.creator-center-dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-menu::before {
  content: '';
  position: absolute;
  top: -6px;
  right: 25px;
  width: 12px;
  height: 12px;
  background-color: white;
  transform: rotate(45deg);
  box-shadow: -2px -2px 5px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.menu-item:hover {
  background-color: rgba(64, 158, 255, 0.08);
}

.menu-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: white;
  font-size: 18px;
}

.menu-info {
  display: flex;
  flex-direction: column;
}

.menu-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.menu-desc {
  font-size: 12px;
  color: #909399;
}

.notification-badge {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.avatar-wrapper {
  cursor: pointer;
}

.ai-chat-icon {
  cursor: pointer;
  margin-right: 10px;
}

.ai-icon {
  font-size: 20px;
  color: var(--primary-color);
}

.chat-room-icon {
  cursor: pointer;
  margin-right: 10px;
}

.chat-icon {
  font-size: 20px;
  color: var(--primary-color);
}

@media (max-width: 992px) {
  .logo-text {
    display: none;
  }
  
  .creator-btn .el-icon--right {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .nav-list {
    display: none;
  }
  
  .search-input {
    width: 150px;
  }
  
  .creator-btn span {
    display: none;
  }
  
  .creator-btn {
    padding: 8px 10px;
  }
}

/* AI 聊天抽屉样式 */
:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
}

.message-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  max-width: 80%;
}

.message-name {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
}

.message-text {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.user-message .message-text {
  background-color: var(--primary-color);
  color: white;
  border-radius: 8px 0 8px 8px;
}

.ai-message .message-text {
  background-color: #f5f7fa;
  color: #333;
  border-radius: 0 8px 8px 8px;
}

.chat-input {
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.code-block {
  background-color: #282c34;
  color: #abb2bf;
  padding: 12px;
  border-radius: 4px;
  margin: 8px 0;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  overflow-x: auto;
  white-space: pre-wrap;
  max-width: 100%;
  box-sizing: border-box;
}

/* 对于非常长的单行代码，确保有水平滚动 */
.code-block code {
  display: inline-block;
  min-width: 100%;
}

.code-block::-webkit-scrollbar {
  height: 8px;
  background-color: #282c34;
}

.code-block::-webkit-scrollbar-thumb {
  background-color: #6b6b6b;
  border-radius: 4px;
}

.code-block::-webkit-scrollbar-thumb:hover {
  background-color: #8a8a8a;
}

/* 输入指示器动画 */
.typing-indicator {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 30px;
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #606266;
  margin-right: 3px;
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: 0s;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}
</style> 