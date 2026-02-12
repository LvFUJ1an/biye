<template>
  <div class="auth-modal-overlay" v-if="visible" @click.self="closeModal">
    <div class="auth-modal-container">
      <div class="auth-modal-card">
        <div class="auth-modal-left">
          <div class="left-content">
            <div class="branding">
              <img src="@/assets/logo.svg" alt="Logo" class="branding-logo" />
              <h2>IT技术社区</h2>
            </div>
            <div class="illustration">
              <div class="animated-elements">
                <div class="circle circle-1"></div>
                <div class="circle circle-2"></div>
                <div class="circle circle-3"></div>
                <div class="rect rect-1"></div>
                <div class="rect rect-2"></div>
              </div>
              <p class="welcome-text">加入我们的技术社区</p>
              <p class="sub-text">分享知识，共同成长</p>
              <div class="features">
                <div class="feature-item">
                  <el-icon><Document /></el-icon>
                  <span>优质技术博客</span>
                </div>
                <div class="feature-item">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>专业问答社区</span>
                </div>
                <div class="feature-item">
                  <el-icon><VideoPlay /></el-icon>
                  <span>丰富视频资源</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="auth-modal-right">
          <button class="close-button" @click="closeModal">
            <el-icon><Close /></el-icon>
          </button>
          <div class="form-container">
            <div class="tabs">
              <button 
                class="tab-btn" 
                :class="{ active: activeTab === 'login' }" 
                @click="activeTab = 'login'"
              >
                登录
              </button>
              <button 
                class="tab-btn" 
                :class="{ active: activeTab === 'register' }" 
                @click="activeTab = 'register'"
              >
                注册
              </button>
            </div>
            
            <!-- 登录表单 -->
            <form v-if="activeTab === 'login'" class="auth-form" @submit.prevent="handleLogin">
              <h3 class="form-title">欢迎回来</h3>
              <div class="form-group">
                <label for="login-username">用户名</label>
                <div class="input-wrapper">
                  <el-icon><User /></el-icon>
                  <input 
                    id="login-username" 
                    type="text" 
                    v-model="loginForm.username" 
                    placeholder="请输入用户名"
                    required
                  />
                </div>
              </div>
              <div class="form-group">
                <label for="login-password">密码</label>
                <div class="input-wrapper">
                  <el-icon><Lock /></el-icon>
                  <input 
                    id="login-password" 
                    :type="showLoginPassword ? 'text' : 'password'" 
                    v-model="loginForm.password" 
                    placeholder="请输入密码"
                    required
                  />
                  <button 
                    type="button" 
                    class="toggle-password" 
                    @click="showLoginPassword = !showLoginPassword"
                  >
                    <el-icon v-if="showLoginPassword"><Hide /></el-icon>
                    <el-icon v-else><View /></el-icon>
                  </button>
                </div>
              </div>
              <div class="form-options">
                <label class="remember-me">
                  <input type="checkbox" v-model="loginForm.rememberMe" />
                  <span>记住我</span>
                </label>
                <a href="#" class="forgot-password">忘记密码？</a>
              </div>
              <button type="submit" class="submit-btn" :disabled="loginLoading">
                <span v-if="!loginLoading">登录</span>
                <el-icon v-else class="loading-icon"><Loading /></el-icon>
              </button>
              <div class="third-party-login">
                <p>其他登录方式</p>
                <div class="third-party-icons">
                  <button type="button" class="third-party-icon github">
                    <el-icon><IconGithub /></el-icon>
                  </button>
                  <button type="button" class="third-party-icon wechat">
                    <el-icon><ChatDotRound /></el-icon>
                  </button>
                  <button type="button" class="third-party-icon weibo">
                    <el-icon><Promotion /></el-icon>
                  </button>
                </div>
              </div>
            </form>
            
            <!-- 注册表单 -->
            <form v-else class="auth-form" @submit.prevent="handleRegister">
              <h3 class="form-title">创建账号</h3>
              <div class="form-group">
                <label for="register-username">用户名</label>
                <div class="input-wrapper">
                  <el-icon><User /></el-icon>
                  <input 
                    id="register-username" 
                    type="text" 
                    v-model="registerForm.username" 
                    placeholder="请输入用户名"
                    required
                  />
                </div>
              </div>
              <div class="form-group">
                <label for="register-email">邮箱</label>
                <div class="input-wrapper">
                  <el-icon><Message /></el-icon>
                  <input 
                    id="register-email" 
                    type="email" 
                    v-model="registerForm.email" 
                    placeholder="请输入邮箱"
                    required
                  />
                </div>
              </div>
              <div class="form-group">
                <label for="register-password">密码</label>
                <div class="input-wrapper">
                  <el-icon><Lock /></el-icon>
                  <input 
                    id="register-password" 
                    :type="showRegisterPassword ? 'text' : 'password'" 
                    v-model="registerForm.password" 
                    placeholder="请输入密码"
                    required
                  />
                  <button 
                    type="button" 
                    class="toggle-password" 
                    @click="showRegisterPassword = !showRegisterPassword"
                  >
                    <el-icon v-if="showRegisterPassword"><Hide /></el-icon>
                    <el-icon v-else><View /></el-icon>
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label for="avatar">头像</label>
                <div class="avatar-section">
                  <div v-if="avatarPreview" class="avatar-preview-container">
                    <img :src="avatarPreview" alt="头像预览" class="avatar-preview-img" />
                    <button type="button" class="avatar-remove-btn" @click="removeAvatar">
                      <el-icon><Close /></el-icon>
                    </button>
                  </div>
                  <AvatarCropper
                    v-else
                    :inputId="avatarInputId"
                    @crop="handleCrop"
                    @cancel="removeAvatar"
                  />
                </div>
              </div>
              <div class="form-agreement">
                <label class="agreement-checkbox">
                  <input type="checkbox" v-model="registerForm.agreement" required />
                  <span>我已阅读并同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a></span>
                </label>
              </div>
              <button type="submit" class="submit-btn" :disabled="registerLoading">
                <span v-if="!registerLoading">注册</span>
                <el-icon v-else class="loading-icon"><Loading /></el-icon>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, watch, onMounted, onBeforeUnmount } from 'vue'
import { 
  User, 
  Lock, 
  View, 
  Hide, 
  Close, 
  Loading, 
  Message, 
  Plus,
  Document,
  VideoPlay,
  ChatLineRound,
  ChatDotRound,
  Promotion
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'
import chatService from '@/services/chat'
import AvatarCropper from './AvatarCropper.vue'

const IconGithub = {
  template: `
    <svg viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
      <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
    </svg>
  `
}

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'login-success'])

// 控制页面滚动
watch(() => props.visible, (newVal) => {
  if (newVal) {
    disableScroll()
  } else {
    enableScroll()
  }
})

// 禁用页面滚动
const disableScroll = () => {
  document.body.style.overflow = 'hidden'
  document.body.style.paddingRight = '17px' // 防止滚动条消失导致页面抖动
}

// 启用页面滚动
const enableScroll = () => {
  document.body.style.overflow = ''
  document.body.style.paddingRight = ''
}

// 组件卸载前确保恢复滚动
onBeforeUnmount(() => {
  enableScroll()
})

// 生成唯一的ID用于文件输入
const avatarInputId = `avatar-input-${Math.random().toString(36).substring(2, 11)}`

// 标签页状态
const activeTab = ref('login')

// 登录表单
const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false
})

// 注册表单
const registerForm = ref({
  username: '',
  email: '',
  password: '',
  agreement: false
})

// 密码显示控制
const showLoginPassword = ref(false)
const showRegisterPassword = ref(false)

// 加载状态
const loginLoading = ref(false)
const registerLoading = ref(false)

// 头像预览
const avatarPreview = ref('')
const avatarFile = ref<File | null>(null)

// 关闭模态框
const closeModal = () => {
  emit('close')
}

// 处理裁剪完成
const handleCrop = (data: {url: string, blob: Blob}) => {
  avatarPreview.value = data.url
  
  // 将Blob转换为File对象
  const file = new File([data.blob], 'avatar.png', { type: 'image/png' })
  avatarFile.value = file
}

// 移除头像
const removeAvatar = () => {
  avatarPreview.value = ''
  avatarFile.value = null
}

// 处理登录
const handleLogin = async () => {
  try {
    loginLoading.value = true
    
    // 发送登录请求
    const response = await api.post('/user/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    })
    
    // 根据后端统一的返回格式进行处理
    if (response.code === 0) {
      // 登录成功
      ElMessage.success('登录成功')
      
      // 存储用户信息和token
      const userData = response.data
      localStorage.setItem('token', userData.token)
      localStorage.setItem('user', JSON.stringify({
        id: userData.id,
        username: userData.username,
        email: userData.email,
        avatarUrl: userData.avatarUrl || '',
        role: userData.role
      }))
      
      // 存储完整的用户信息用于其他组件使用
      localStorage.setItem('userInfo', JSON.stringify(userData))
      
      // 连接WebSocket聊天服务
      try {
        const connected = chatService.connectWebSocket();
        if (connected) {
          console.log('WebSocket聊天服务连接成功');
        } else {
          console.warn('WebSocket聊天服务连接失败，将在需要时重试');
        }
      } catch (error) {
        console.error('WebSocket连接错误:', error);
      }
      
      // 发送登录成功事件
      emit('login-success', userData)
      
      // 关闭模态框
      closeModal()
    } else {
      // 登录失败
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('登录请求失败，请检查网络连接')
  } finally {
    loginLoading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    registerLoading.value = true
    
    // 验证表单
    if (!registerForm.value.agreement) {
      ElMessage.warning('请先阅读并同意用户协议和隐私政策')
      registerLoading.value = false
      return
    }
    
    // 构建FormData
    const formData = new FormData()
    formData.append('username', registerForm.value.username)
    formData.append('password', registerForm.value.password)
    formData.append('email', registerForm.value.email)
    formData.append('role', '0') // 默认普通用户
    
    if (avatarFile.value) {
      formData.append('avatarFile', avatarFile.value)
    }
    
    // 发送注册请求
    const response = await api.post('/user/register', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 根据后端统一的返回格式进行处理
    if (response.code === 0) {
      // 注册成功
      ElMessage.success('注册成功，请登录')
      
      // 切换到登录标签页
      activeTab.value = 'login'
      
      // 预填充登录表单
      loginForm.value.username = registerForm.value.username
      
      // 清空注册表单
      registerForm.value = {
        username: '',
        email: '',
        password: '',
        agreement: false
      }
      avatarPreview.value = ''
      avatarFile.value = null
    } else {
      // 注册失败
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
    ElMessage.error('注册请求失败，请检查网络连接')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.auth-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
  backdrop-filter: blur(5px);
}

.auth-modal-container {
  width: 100%;
  max-width: 900px;
  margin: 0 20px;
}

.auth-modal-card {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  min-height: 600px;
  animation: slideUp 0.4s ease;
}

.auth-modal-left {
  width: 40%;
  background: linear-gradient(135deg, var(--primary-color), #36b4ff);
  color: white;
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.auth-modal-right {
  width: 60%;
  padding: 40px;
  position: relative;
}

.branding {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.branding-logo {
  width: 40px;
  height: 40px;
  margin-right: 10px;
  filter: brightness(0) invert(1);
}

.branding h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.left-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.illustration {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.welcome-text {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.sub-text {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 30px;
}

.animated-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.2;
  background-color: white;
}

.circle-1 {
  width: 150px;
  height: 150px;
  top: -50px;
  left: -50px;
  animation: float 8s infinite ease-in-out;
}

.circle-2 {
  width: 80px;
  height: 80px;
  bottom: 20%;
  right: 10%;
  animation: float 7s infinite ease-in-out reverse;
}

.circle-3 {
  width: 60px;
  height: 60px;
  bottom: 10%;
  left: 20%;
  animation: float 6s infinite ease-in-out 1s;
}

.rect {
  position: absolute;
  opacity: 0.15;
  background-color: white;
}

.rect-1 {
  width: 120px;
  height: 120px;
  transform: rotate(45deg);
  top: 30%;
  right: -40px;
  animation: spin 15s infinite linear;
}

.rect-2 {
  width: 80px;
  height: 80px;
  transform: rotate(15deg);
  bottom: -20px;
  left: 30%;
  animation: spin 12s infinite linear reverse;
}

.features {
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.feature-item .el-icon {
  margin-right: 10px;
  font-size: 18px;
}

.close-button {
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-button:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: #333;
}

.form-container {
  max-width: 400px;
  margin: 0 auto;
  padding-top: 20px;
}

.tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
}

.tab-btn {
  flex: 1;
  padding: 12px 0;
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
  position: relative;
  transition: all 0.3s;
}

.tab-btn.active {
  color: var(--primary-color);
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: var(--primary-color);
  border-radius: 3px 3px 0 0;
}

.form-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.auth-form {
  animation: fadeIn 0.3s ease;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
  background-color: #f9f9f9;
}

.input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  background-color: white;
}

.input-wrapper .el-icon {
  color: #999;
  margin-left: 10px;
  font-size: 18px;
}

.input-wrapper input {
  flex: 1;
  height: 44px;
  padding: 0 15px;
  font-size: 15px;
  border: none;
  background: transparent;
  color: #333;
  outline: none;
}

.toggle-password {
  background: none;
  border: none;
  color: #999;
  padding: 0 12px;
  cursor: pointer;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
  color: #666;
}

.remember-me input {
  margin-right: 6px;
}

.forgot-password {
  color: var(--primary-color);
}

.avatar-section {
  width: 100%;
}

.avatar-preview-container {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto;
}

.avatar-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid var(--primary-color);
}

.avatar-remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #f56c6c;
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.avatar-remove-btn:hover {
  background-color: #e74c3c;
}

.submit-btn {
  width: 100%;
  height: 44px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
}

.submit-btn:hover {
  background-color: #337ecc;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.loading-icon {
  animation: spin 1s infinite linear;
}

.third-party-login {
  margin-top: 25px;
  text-align: center;
}

.third-party-login p {
  position: relative;
  font-size: 14px;
  color: #999;
  margin-bottom: 15px;
}

.third-party-login p::before,
.third-party-login p::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30%;
  height: 1px;
  background-color: #eee;
}

.third-party-login p::before {
  left: 0;
}

.third-party-login p::after {
  right: 0;
}

.third-party-icons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.third-party-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  background: none;
}

.third-party-icon:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.third-party-icon.github:hover {
  background-color: #333;
  color: white;
  border-color: #333;
}

.third-party-icon.wechat:hover {
  background-color: #07c160;
  color: white;
  border-color: #07c160;
}

.third-party-icon.weibo:hover {
  background-color: #e6162d;
  color: white;
  border-color: #e6162d;
}

.form-agreement {
  margin-bottom: 20px;
}

.agreement-checkbox {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  color: #666;
}

.agreement-checkbox input {
  margin-top: 3px;
  margin-right: 8px;
}

.agreement-checkbox a {
  color: var(--primary-color);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式样式 */
@media (max-width: 768px) {
  .auth-modal-card {
    flex-direction: column;
    max-height: 90vh;
    overflow-y: auto;
  }
  
  .auth-modal-left {
    width: 100%;
    padding: 30px 20px;
    min-height: 200px;
  }
  
  .auth-modal-right {
    width: 100%;
    padding: 30px 20px;
  }
  
  .form-container {
    padding-top: 0;
  }
  
  .features {
    display: none;
  }
}
</style> 