import { ref, reactive } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

// 消息类型定义
export interface ChatMessage {
  id: string;
  senderId: number;
  senderName: string;
  senderAvatar: string;
  content: string;
  type: 'text' | 'image' | 'system' | 'follow' | 'like' | 'comment';
  timestamp: number;
  isSelf?: boolean;
  fromUserId?: number;
  fromUserName?: string;
  fromUserAvatar?: string;
  targetType?: string;
  targetId?: number;
  commentContent?: string;
}

// 用户类型定义
export interface ChatUser {
  id: number;
  username: string;
  avatarUrl: string;
  status: 'online' | 'offline' | 'away';
}

// 聊天状态
const state = reactive({
  connected: false,
  reconnecting: false,
  messages: [] as ChatMessage[],
  users: [] as ChatUser[],
  socket: null as WebSocket | null,
  reconnectAttempts: 0,
  maxReconnectAttempts: 5,
  reconnectInterval: 3000,
  timer: null as number | null
})

// 当前用户信息
const currentUser = ref<{id: number, username: string, avatarUrl: string} | null>(null)

// 创建WebSocket连接
export function connectWebSocket() {
  try {
    // 如果已经连接，则不重复连接
    if (state.connected && state.socket) {
      console.log('WebSocket已连接，无需重复连接')
      return true
    }
    
    // 重置重连状态
    if (state.reconnecting) {
      state.reconnecting = false
      if (state.timer) {
        clearTimeout(state.timer)
        state.timer = null
      }
    }
    
    // 获取当前用户信息
    const userInfo = localStorage.getItem('userInfo')
    if (!userInfo) {
      console.log('未获取到用户信息，无法连接聊天服务')
      return false
    }
    
    // 解析用户信息
    try {
      const parsedUserInfo = JSON.parse(userInfo)
      if (!parsedUserInfo.id) {
        console.log('用户信息不完整，无法连接聊天服务')
        return false
      }
      
      currentUser.value = {
        id: parsedUserInfo.id,
        username: parsedUserInfo.username,
        avatarUrl: parsedUserInfo.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
      return false
    }
    
    // 关闭现有连接（如果有）
    if (state.socket) {
      try {
        state.socket.close()
      } catch (e) {
        console.warn('关闭旧连接时出错:', e)
      }
    }
    
    // 创建WebSocket连接
    const wsUrl = `ws://localhost:18003/ws/${currentUser.value.id}`
    state.socket = new WebSocket(wsUrl)
    
    // 绑定事件处理器
    state.socket.onopen = handleSocketOpen
    state.socket.onmessage = handleSocketMessage
    state.socket.onclose = handleSocketClose
    state.socket.onerror = handleSocketError
    
    // 添加自动重连的ping机制
    setupPingInterval()
    
    console.log('WebSocket连接初始化完成，等待连接建立...')
    return true
  } catch (error) {
    console.error('连接聊天服务失败:', error)
    return false
  }
}

// 设置定时ping以保持连接活跃
let pingInterval: number | null = null;
function setupPingInterval() {
  // 清除现有的ping定时器
  if (pingInterval) {
    clearInterval(pingInterval)
  }
  
  // 每30秒发送一次ping，保持连接
  pingInterval = setInterval(() => {
    if (state.connected && state.socket && state.socket.readyState === WebSocket.OPEN) {
      try {
        state.socket.send(JSON.stringify({ type: 'ping' }))
      } catch (e) {
        console.error('发送ping失败:', e)
        // 如果发送ping失败，尝试重连
        disconnectWebSocket()
        connectWebSocket()
      }
    }
  }, 30000) as unknown as number
}

// 检查用户登录状态并自动连接WebSocket
export function autoConnectWebSocket() {
  // 检查是否有token和用户信息
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')
  
  if (token && userInfo) {
    console.log('检测到用户登录状态，自动连接WebSocket')
    return connectWebSocket()
  }
  
  return false
}

// 断开WebSocket连接
export function disconnectWebSocket() {
  // 清除ping定时器
  if (pingInterval) {
    clearInterval(pingInterval)
    pingInterval = null
  }
  
  if (state.socket) {
    try {
      state.socket.close()
    } catch (e) {
      console.warn('关闭WebSocket连接出错:', e)
    }
  }
  
  // 清除重连定时器
  if (state.timer) {
    clearTimeout(state.timer)
    state.timer = null
  }
  
  // 重置状态
  state.connected = false
  state.reconnecting = false
  state.reconnectAttempts = 0
}

// WebSocket连接成功处理
function handleSocketOpen() {
  console.log('WebSocket连接成功')
  state.connected = true
  state.reconnecting = false
  state.reconnectAttempts = 0
  
  // 加入聊天室
  sendSystemMessage('join')
}

// WebSocket接收消息处理
function handleSocketMessage(event: MessageEvent) {
  try {
    const data = JSON.parse(event.data)
    
    // 根据消息类型处理
    switch (data.type) {
      case 'message':
        // 添加消息到消息列表
        addMessage({
          id: data.id || Date.now().toString(),
          senderId: data.senderId,
          senderName: data.senderName,
          senderAvatar: data.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
          content: data.content,
          type: data.messageType || 'text',
          timestamp: data.timestamp || Date.now(),
          isSelf: currentUser.value ? currentUser.value.id === data.senderId : false
        })
        break
      case 'follow':
        // 使用全局通知显示关注消息，而不是添加到消息流
        showFollowNotification(data)
        break
      case 'like':
        // 使用全局通知显示点赞消息
        showLikeNotification(data)
        break
      case 'comment':
        // 使用全局通知显示评论消息
        showCommentNotification(data)
        break
      case 'onlineUsers':
        // 根据需求文档，处理onlineUsers消息类型
        if (Array.isArray(data.users)) {
          updateUserList(data.users.map((user: any) => ({
            id: user.senderId,
            username: user.senderName,
            avatarUrl: user.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
            status: 'online' as const // 确保类型匹配
          })))
        }
        break
      case 'userList':
        // 兼容旧格式的用户列表
        updateUserList(data.users)
        break
      case 'join':
      case 'userJoin':
        // 添加用户到用户列表
        const joinUser: ChatUser = {
          id: data.senderId,
          username: data.senderName,
          avatarUrl: data.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
          status: 'online'
        }
        addUser(joinUser)
        
        // 不再显示用户加入消息
        break
      case 'leave':
      case 'userLeave':
        // 从用户列表中移除用户
        removeUser(data.userId || data.senderId)
        
        // 不再显示用户离开消息
        break
      case 'system':
        // 添加系统消息
        addSystemMessage(data.content)
        break
      default:
        console.warn('未知消息类型:', data)
    }
  } catch (e) {
    console.error('解析消息失败:', e, event.data)
  }
}

// WebSocket关闭处理
function handleSocketClose(event: CloseEvent) {
  console.log('WebSocket连接已关闭:', event.code, event.reason)
  state.connected = false
  
  // 尝试重新连接
  if (!state.reconnecting && state.reconnectAttempts < state.maxReconnectAttempts) {
    state.reconnecting = true
    state.timer = setTimeout(() => {
      state.reconnectAttempts++
      console.log(`尝试重新连接 (${state.reconnectAttempts}/${state.maxReconnectAttempts})...`)
      
      const success = connectWebSocket()
      if (!success && state.reconnectAttempts < state.maxReconnectAttempts) {
        // 连接失败，继续尝试
        state.timer = setTimeout(() => {
          state.reconnecting = false
          handleSocketClose(event)
        }, state.reconnectInterval)
      } else if (!success) {
        // 达到最大重试次数
        state.reconnecting = false
        ElMessage.error('连接聊天服务失败，请稍后刷新页面重试')
      }
    }, state.reconnectInterval)
  } else if (state.reconnectAttempts >= state.maxReconnectAttempts) {
    ElMessage.error('无法连接到聊天服务，请稍后刷新页面重试')
  }
}

// WebSocket错误处理
function handleSocketError(error: Event) {
  console.error('WebSocket错误:', error)
  ElMessage.error('聊天服务连接错误')
}

// 发送消息
export function sendMessage(content: string) {
  if (!state.connected || !state.socket) {
    ElMessage.warning('聊天服务未连接，无法发送消息')
    return false
  }
  
  const trimmedContent = content.trim()
  if (!trimmedContent) {
    return false
  }
  
  // 避免只发送数字的消息
  if (/^\d+$/.test(trimmedContent)) {
    ElMessage.warning('消息内容不能只包含数字')
    return false
  }
  
  try {
    const message = {
      type: 'message',
      content: trimmedContent,
      senderId: currentUser.value?.id,
      senderName: currentUser.value?.username,
      senderAvatar: currentUser.value?.avatarUrl,
      timestamp: Date.now()
    }
    
    state.socket.send(JSON.stringify(message))
    return true
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
    return false
  }
}

// 发送关注通知
export function sendFollowNotification(targetUserId: number) {
  if (!state.connected || !state.socket) {
    ElMessage.warning('聊天服务未连接，无法发送关注通知')
    return false
  }
  
  if (!currentUser.value) {
    ElMessage.warning('用户未登录，无法发送关注通知')
    return false
  }
  
  try {
    const notification = {
      type: 'follow',
      targetUserId: targetUserId,
      fromUserId: currentUser.value.id,
      fromUserName: currentUser.value.username,
      fromUserAvatar: currentUser.value.avatarUrl,
      timestamp: Date.now()
    }
    
    state.socket.send(JSON.stringify(notification))
    return true
  } catch (error) {
    console.error('发送关注通知失败:', error)
    ElMessage.error('发送关注通知失败')
    return false
  }
}

// 发送点赞通知
export function sendLikeNotification(targetUserId: number, targetType: string, targetId: number) {
  if (!state.connected || !state.socket) {
    ElMessage.warning('聊天服务未连接，无法发送点赞通知')
    return false
  }
  
  if (!currentUser.value) {
    ElMessage.warning('用户未登录，无法发送点赞通知')
    return false
  }
  
  try {
    const notification = {
      type: 'like',
      targetUserId: targetUserId,
      fromUserId: currentUser.value.id,
      fromUserName: currentUser.value.username,
      fromUserAvatar: currentUser.value.avatarUrl,
      targetType: targetType,
      targetId: targetId,
      timestamp: Date.now()
    }
    
    state.socket.send(JSON.stringify(notification))
    return true
  } catch (error) {
    console.error('发送点赞通知失败:', error)
    ElMessage.error('发送点赞通知失败')
    return false
  }
}

// 发送评论通知
export function sendCommentNotification(targetUserId: number, content: string, targetType: string, targetId: number) {
  if (!state.connected || !state.socket) {
    ElMessage.warning('聊天服务未连接，无法发送评论通知')
    return false
  }
  
  if (!currentUser.value) {
    ElMessage.warning('用户未登录，无法发送评论通知')
    return false
  }
  
  try {
    const notification = {
      type: 'comment',
      targetUserId: targetUserId,
      fromUserId: currentUser.value.id,
      fromUserName: currentUser.value.username,
      fromUserAvatar: currentUser.value.avatarUrl,
      content: content,
      targetType: targetType,
      targetId: targetId,
      timestamp: Date.now()
    }
    
    state.socket.send(JSON.stringify(notification))
    return true
  } catch (error) {
    console.error('发送评论通知失败:', error)
    ElMessage.error('发送评论通知失败')
    return false
  }
}

// 发送系统消息
function sendSystemMessage(action: string) {
  if (!state.connected || !state.socket) return
  
  try {
    // 根据需求文档，使用正确的消息格式
    const message = {
      type: action === 'join' ? 'join' : 'leave',
      senderId: currentUser.value?.id,
      senderName: currentUser.value?.username,
      senderAvatar: currentUser.value?.avatarUrl,
      timestamp: Date.now()
    }
    
    state.socket.send(JSON.stringify(message))
  } catch (error) {
    console.error('发送系统消息失败:', error)
  }
}

// 添加消息到消息列表
function addMessage(message: ChatMessage) {
  // 验证消息内容，过滤掉空消息或只包含数字的消息
  if (!message.content.trim() || /^\d+$/.test(message.content.trim())) {
    return
  }
  
  // 检查消息ID是否已存在，避免重复消息
  if (state.messages.some(m => m.id === message.id)) {
    return
  }
  
  state.messages.push(message)
  
  // 最多保留100条消息
  if (state.messages.length > 100) {
    state.messages.shift()
  }
}

// 添加系统消息，避免重复显示
function addSystemMessage(content: string) {
  // 检查是否有相同内容的系统消息已经显示
  const recentMessages = state.messages.slice(-5); // 只检查最近5条消息
  const hasDuplicateMessage = recentMessages.some(msg => 
    msg.type === 'system' && msg.content === content
  );
  
  if (hasDuplicateMessage) {
    return; // 如果已有相同内容的系统消息，则不再添加
  }
  
  const message: ChatMessage = {
    id: Date.now().toString(),
    senderId: 0,
    senderName: '系统',
    senderAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    content: content,
    type: 'system',
    timestamp: Date.now()
  }
  
  addMessage(message)
}

// 更新用户列表
function updateUserList(users: ChatUser[]) {
  state.users = users;
}

// 添加用户到用户列表
function addUser(user: ChatUser) {
  const existingUserIndex = state.users.findIndex(u => u.id === user.id)
  
  if (existingUserIndex === -1) {
    state.users.push(user)
  } else {
    state.users[existingUserIndex] = user
  }
}

// 从用户列表中移除用户
function removeUser(userId: number) {
  const index = state.users.findIndex(user => user.id === userId)
  
  if (index !== -1) {
    state.users.splice(index, 1)
  }
}

// 根据ID获取用户名
function getUsernameById(userId: number): string | null {
  const user = state.users.find(user => user.id === userId)
  return user ? user.username : null
}

// 格式化关注消息
function formatFollowMessage(data: any): ChatMessage {
  const content = `${data.fromUserName} 关注了你`
  
  return {
    id: Date.now().toString(),
    senderId: 0, // 系统消息发送者ID为0
    senderName: '系统通知',
    senderAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    content: content,
    type: 'follow',
    timestamp: data.timestamp || Date.now(),
    isSelf: false,
    fromUserId: data.fromUserId,
    fromUserName: data.fromUserName,
    fromUserAvatar: data.fromUserAvatar
  }
}

// 显示全局关注通知
function showFollowNotification(data: any) {
  const avatar = data.fromUserAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  const username = data.fromUserName || '某用户'
  const userId = data.fromUserId
  
  ElNotification({
    title: '新关注通知',
    dangerouslyUseHTMLString: true,
    message: `
      <div style="display: flex; align-items: center; gap: 10px; cursor: pointer;" 
           onclick="window.location.href='/user/profile/${userId}'">
        <img src="${avatar}" style="width: 40px; height: 40px; border-radius: 50%;" />
        <div>
          <div><strong style="color: #409eff;">${username}</strong> 关注了你</div>
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">点击查看用户资料</div>
        </div>
      </div>
    `,
    type: 'success',
    duration: 8000,
    position: 'top-right',
    showClose: true,
    onClick: () => {
      // 在点击通知时也可以通过JS导航到用户资料页
      window.location.href = `/user/profile/${userId}`
    }
  })
}

// 显示全局点赞通知
function showLikeNotification(data: any) {
  const avatar = data.fromUserAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  const username = data.fromUserName || '某用户'
  const userId = data.fromUserId
  const targetType = data.targetType || '内容'
  const targetId = data.targetId
  
  // 根据目标类型获取不同的显示文本和链接
  let targetText = '你的内容'
  let targetLink = '#'
  
  switch (targetType) {
    case 'blog':
    case 'post':
      targetText = '你的博客'
      targetLink = `/article/${targetId}`
      break
    case 'comment':
      targetText = '你的评论'
      targetLink = `/comment/${targetId}`
      break
    case 'question':
      targetText = '你的提问'
      targetLink = `/question/detail/${targetId}`
      break
    case 'answer':
      targetText = '你的回答'
      targetLink = `/answer/${targetId}`
      break
    case 'video':
      targetText = '你的视频'
      targetLink = `/video/detail/${targetId}`
      break
  }
  
  ElNotification({
    title: '新点赞通知',
    dangerouslyUseHTMLString: true,
    message: `
      <div style="display: flex; align-items: center; gap: 10px;">
        <img src="${avatar}" style="width: 40px; height: 40px; border-radius: 50%;" />
        <div>
          <div>
            <strong style="color: #409eff;">${username}</strong> 
            点赞了${targetText}
            <span style="color: #ff9900; font-size: 16px;">&#9733;</span>
          </div>
          <div style="display: flex; margin-top: 5px;">
            <a href="${targetLink}" style="font-size: 12px; color: #409eff; text-decoration: none;">
              查看详情
            </a>
            <a href="/user/profile/${userId}" style="font-size: 12px; color: #909399; margin-left: 15px; text-decoration: none;">
              查看用户
            </a>
          </div>
        </div>
      </div>
    `,
    type: 'success',
    duration: 8000,
    position: 'top-right',
    showClose: true,
    onClick: () => {
      // 点击通知时跳转到被点赞的内容
      window.location.href = targetLink
    }
  })
}

// 显示全局评论通知
function showCommentNotification(data: any) {
  const avatar = data.fromUserAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  const username = data.fromUserName || '某用户'
  const userId = data.fromUserId
  const targetType = data.targetType || '内容'
  const targetId = data.targetId
  const commentContent = data.content || ''
  
  // 截取评论内容，避免过长
  const truncatedComment = commentContent.length > 50 
    ? commentContent.substring(0, 50) + '...' 
    : commentContent
  
  // 根据目标类型获取不同的显示文本和链接
  let targetText = '你的内容'
  let targetLink = '#'
  
  switch (targetType) {
    case 'blog':
    case 'post':
    case 'article':
      targetText = '你的博客'
      targetLink = `/article/${targetId}`
      break
    case 'question':
      targetText = '你的提问'
      targetLink = `/question/detail/${targetId}`
      break
    case 'answer':
      targetText = '你的回答'
      targetLink = `/answer/${targetId}`
      break
    case 'video':
      targetText = '你的视频'
      targetLink = `/video/detail/${targetId}`
      break
  }
  
  ElNotification({
    title: '新评论通知',
    dangerouslyUseHTMLString: true,
    message: `
      <div style="display: flex; align-items: flex-start; gap: 10px;">
        <img src="${avatar}" style="width: 40px; height: 40px; border-radius: 50%;" />
        <div>
          <div>
            <strong style="color: #409eff;">${username}</strong> 
            评论了${targetText}
          </div>
          <div style="margin: 6px 0; padding: 8px 12px; background-color: #f7f9fa; border-radius: 4px; color: #606266; font-size: 13px;">
            "${truncatedComment}"
          </div>
          <div style="display: flex; margin-top: 5px;">
            <a href="${targetLink}" style="font-size: 12px; color: #409eff; text-decoration: none;">
              查看详情
            </a>
            <a href="/user/profile/${userId}" style="font-size: 12px; color: #909399; margin-left: 15px; text-decoration: none;">
              查看用户
            </a>
          </div>
        </div>
      </div>
    `,
    type: 'info',
    duration: 10000,
    position: 'top-right',
    showClose: true,
    onClick: () => {
      // 点击通知时跳转到被评论的内容
      window.location.href = targetLink
    }
  })
}

// 导出聊天状态和方法
export default {
  state,
  currentUser,
  connectWebSocket,
  disconnectWebSocket,
  sendMessage,
  sendFollowNotification,
  sendLikeNotification,
  sendCommentNotification,
  autoConnectWebSocket
} 