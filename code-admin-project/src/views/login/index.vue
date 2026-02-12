<template>
  <div class="login-container">
    <!-- 背景动画元素 -->
    <div class="bg-animation">
      <div v-for="n in 10" :key="n" class="cube"></div>
      <div v-for="n in 15" :key="`line-${n}`" class="line"></div>
      <div v-for="n in 5" :key="`circle-${n}`" class="circle"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="login-header">
        <div class="logo-container">
          <div class="logo-icon"></div>
          <h1>IT知识分享平台</h1>
        </div>
        <p class="login-subtitle">管理员登录系统</p>
      </div>
      
      <form class="login-form" @submit.prevent="handleVerify">
        <div class="form-item">
          <div class="icon-wrapper">
            <div class="user-icon"></div>
          </div>
          <input 
            v-model="loginForm.username" 
            type="text" 
            placeholder="管理员账号" 
            :class="{ 'error-input': errors.username }" 
          />
          <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
        </div>
        
        <div class="form-item">
          <div class="icon-wrapper">
            <div class="password-icon"></div>
          </div>
          <input 
            v-model="loginForm.password" 
            :type="showPassword ? 'text' : 'password'" 
            placeholder="管理员密码" 
            :class="{ 'error-input': errors.password }" 
          />
          <div class="toggle-password" @click="showPassword = !showPassword">
            <div :class="showPassword ? 'eye-open' : 'eye-close'"></div>
          </div>
          <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
        </div>
        
        <div class="form-item remember-me">
          <label class="checkbox-container">
            <input v-model="loginForm.remember" type="checkbox" />
            <span class="checkmark"></span>
            <span>记住我</span>
          </label>
        </div>
        
        <button 
          :class="['login-button', {'login-button-loading': loading}]" 
          type="submit" 
          :disabled="loading"
        >
          <span v-if="!loading">登录系统</span>
          <div v-else class="loading-spinner"></div>
        </button>
      </form>
      
      <div class="login-footer">
        <p>欢迎使用IT技术交流与分享平台管理系统 v1.0</p>
      </div>
    </div>
  </div>
  
  <!-- 滑动验证模态框 -->
  <el-dialog
    v-model="showVerifyDialog"
    title="安全验证"
    width="400px"
    :show-close="false"
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    center
    class="verify-dialog"
  >
    <div class="verify-dialog-content">
      <p class="verify-tip">请完成拼图验证</p>
      <SliderVerify
        ref="sliderVerify"
        @verified="verifySuccess"
        @fail="verifyFail"
        tipText="拖动滑块完成拼图验证"
      />
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelVerify">取消</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import SliderVerify from '../../components/SliderVerify.vue'
import { login } from '../../api/user'
import { message } from '../../utils/message'

// 路由实例
const router = useRouter()

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 错误信息
const errors = reactive({
  username: '',
  password: ''
})

// 滑动验证组件引用 - 使用any类型以避免TS错误
const sliderVerify = ref<any>(null)

// 控制密码显示
const showPassword = ref(false)

// 加载状态
const loading = ref(false)

// 验证状态
const isVerified = ref(false)

// 控制验证模态框显示
const showVerifyDialog = ref(false)

// 验证表单并显示验证模态框
const handleVerify = () => {
  // 重置错误信息
  errors.username = ''
  errors.password = ''
  
  // 表单验证
  let isValid = true
  
  if (!loginForm.username.trim()) {
    errors.username = '请输入管理员账号'
    isValid = false
  }
  
  if (!loginForm.password) {
    errors.password = '请输入管理员密码'
    isValid = false
  } else if (loginForm.password.length < 6) {
    errors.password = '密码长度不能少于6位'
    isValid = false
  }
  
  if (!isValid) return
  
  // 显示滑动验证模态框
  showVerifyDialog.value = true
  
  // 重置滑动验证状态
  if (sliderVerify.value) {
    sliderVerify.value.reset()
  }
  isVerified.value = false
}

// 验证成功
const verifySuccess = () => {
  isVerified.value = true
  
  // 验证成功后关闭模态框，并发起登录请求
  setTimeout(() => {
    showVerifyDialog.value = false
    handleLogin()
  }, 500) // 延迟关闭，让用户看到验证成功的状态
}

// 验证失败
const verifyFail = () => {
  isVerified.value = false
}

// 取消验证
const cancelVerify = () => {
  showVerifyDialog.value = false
  if (sliderVerify.value) {
    sliderVerify.value.reset()
  }
  isVerified.value = false
}

// 登录处理函数
const handleLogin = async () => {
  // 开始加载
  loading.value = true
  
  try {
    // 准备登录数据
    const loginData = {
      username: loginForm.username,
      password: loginForm.password
    }
    
    // 使用API模块发送登录请求
    const response = await login(loginData)
    
    // 检查返回结果
    if (response && response.code === 0) {
      // 登录成功
      const token = response.data.token || 'admin-token-example'
      localStorage.setItem('admin-token', token)
      
      if (loginForm.remember) {
        localStorage.setItem('admin-username', loginForm.username)
      } else {
        localStorage.removeItem('admin-username')
      }
      
      // 使用消息工具类提示登录成功
      message.success('登录成功')
      
      // 跳转到仪表盘
      router.push('/dashboard')
    } else {
      // 登录失败
      message.error(response.message || '账号或密码错误')
    }
  } catch (error: any) {
    console.error('登录请求错误:', error)
    
    // 使用消息工具类显示错误信息
    message.error(error.message || '登录失败，请稍后重试')
  } finally {
    // 结束加载
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #111827;
  overflow: hidden;
}

/* 背景动画元素 */
.bg-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.cube {
  position: absolute;
  width: 60px;
  height: 60px;
  background: rgba(95, 145, 255, 0.1);
  border: 1px solid rgba(95, 145, 255, 0.2);
  border-radius: 8px;
  animation: float 8s infinite;
  animation-delay: calc(var(--i) * 0.5s);
}

.cube:nth-child(1) { top: 10%; left: 10%; --i: 1; }
.cube:nth-child(2) { top: 20%; left: 45%; --i: 2; }
.cube:nth-child(3) { top: 30%; left: 75%; --i: 3; }
.cube:nth-child(4) { top: 70%; left: 10%; --i: 4; }
.cube:nth-child(5) { top: 60%; left: 70%; --i: 5; }
.cube:nth-child(6) { top: 80%; left: 40%; --i: 6; }
.cube:nth-child(7) { top: 15%; left: 60%; --i: 7; }
.cube:nth-child(8) { top: 90%; left: 90%; --i: 8; }
.cube:nth-child(9) { top: 40%; left: 25%; --i: 9; }
.cube:nth-child(10) { top: 50%; left: 5%; --i: 10; }

.line {
  position: absolute;
  width: 100px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(95, 145, 255, 0.2), transparent);
  animation: slideIn 4s infinite linear;
  animation-delay: calc(var(--i) * 0.2s);
}

.line:nth-child(11) { top: 5%; left: -100px; --i: 1; }
.line:nth-child(12) { top: 15%; left: -100px; --i: 3; }
.line:nth-child(13) { top: 30%; left: -100px; --i: 5; }
.line:nth-child(14) { top: 45%; left: -100px; --i: 7; }
.line:nth-child(15) { top: 60%; left: -100px; --i: 9; }
.line:nth-child(16) { top: 75%; left: -100px; --i: 11; }
.line:nth-child(17) { top: 90%; left: -100px; --i: 13; }
.line:nth-child(18) { top: 10%; right: -100px; --i: 2; }
.line:nth-child(19) { top: 25%; right: -100px; --i: 4; }
.line:nth-child(20) { top: 40%; right: -100px; --i: 6; }
.line:nth-child(21) { top: 55%; right: -100px; --i: 8; }
.line:nth-child(22) { top: 70%; right: -100px; --i: 10; }
.line:nth-child(23) { top: 85%; right: -100px; --i: 12; }
.line:nth-child(24) { top: 95%; right: -100px; --i: 14; }
.line:nth-child(25) { top: 50%; right: -100px; --i: 15; }

.circle {
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 2px solid rgba(95, 145, 255, 0.1);
  animation: pulse 10s infinite;
}

.circle:nth-child(26) { top: 25%; left: 25%; --i: 1; }
.circle:nth-child(27) { top: 75%; left: 75%; --i: 2; }
.circle:nth-child(28) { top: 20%; left: 80%; --i: 3; }
.circle:nth-child(29) { top: 80%; left: 20%; --i: 4; }
.circle:nth-child(30) { top: 50%; left: 50%; --i: 5; }

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 40px;
  background: rgba(22, 28, 41, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(95, 145, 255, 0.1);
  animation: fadeIn 0.8s ease-out;
}

.login-header {
  margin-bottom: 30px;
  text-align: center;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  margin-right: 10px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.logo-icon:before {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.8);
  top: 10px;
  left: 10px;
  clip-path: polygon(0 0, 100% 0, 50% 100%);
}

.login-header h1 {
  color: #ffffff;
  font-size: 24px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.login-subtitle {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin-top: 5px;
  letter-spacing: 1px;
}

/* 表单样式 */
.login-form {
  margin-top: 30px;
}

.form-item {
  position: relative;
  margin-bottom: 20px;
}

.icon-wrapper {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  z-index: 1;
}

.user-icon {
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%235F91FF'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E");
  background-size: contain;
}

.password-icon {
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%235F91FF'%3E%3Cpath d='M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z'/%3E%3C/svg%3E");
  background-size: contain;
}

input[type="text"],
input[type="password"] {
  width: 100%;
  padding: 15px 15px 15px 45px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(95, 145, 255, 0.1);
  color: #ffffff;
  font-size: 14px;
  transition: all 0.3s;
}

input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

input:focus {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(95, 145, 255, 0.5);
  box-shadow: 0 0 0 2px rgba(95, 145, 255, 0.2);
}

.error-input {
  border-color: #f87171 !important;
}

.error-message {
  color: #f87171;
  font-size: 12px;
  margin-top: 5px;
  display: block;
  animation: fadeIn 0.3s;
}

.toggle-password {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.eye-open {
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%235F91FF'%3E%3Cpath d='M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z'/%3E%3C/svg%3E");
  background-size: contain;
}

.eye-close {
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%235F91FF'%3E%3Cpath d='M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z'/%3E%3C/svg%3E");
  background-size: contain;
}

.remember-me {
  display: flex;
  align-items: center;
  margin-top: 20px;
}

.checkbox-container {
  display: flex;
  align-items: center;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  cursor: pointer;
}

.checkbox-container input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  position: relative;
  display: inline-block;
  width: 18px;
  height: 18px;
  margin-right: 10px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(95, 145, 255, 0.3);
  border-radius: 4px;
  transition: all 0.3s;
}

.checkbox-container:hover input ~ .checkmark {
  background: rgba(255, 255, 255, 0.1);
}

.checkbox-container input:checked ~ .checkmark {
  background: rgba(95, 145, 255, 0.8);
  border-color: rgba(95, 145, 255, 0.8);
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-container input:checked ~ .checkmark:after {
  display: block;
}

.login-button {
  width: 100%;
  padding: 15px;
  margin-top: 30px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.3);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.2);
}

.login-button:before {
  content: "";
  position: absolute;
  top: -10%;
  left: -10%;
  width: 120%;
  height: 120%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.1) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: rotate(-45deg);
  transition: all 0.7s;
}

.login-button:hover:before {
  left: 110%;
}

.login-button-loading {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #ffffff;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.login-footer {
  margin-top: 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
}

/* 模态框样式 */
:deep(.verify-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.verify-dialog .el-dialog__header) {
  padding: 15px 20px;
  margin-right: 0;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  color: white;
}

:deep(.verify-dialog .el-dialog__title) {
  color: white;
  font-weight: 600;
}

:deep(.verify-dialog .el-dialog__body) {
  padding: 25px 20px;
}

.verify-dialog-content {
  padding: 10px 0;
}

.verify-tip {
  margin-bottom: 20px;
  color: #333;
  text-align: center;
  font-size: 14px;
}

:deep(.verify-dialog .el-dialog__footer) {
  padding: 10px 20px 20px;
  text-align: center;
}

/* 响应式样式 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 30px 20px;
  }
  
  .login-header h1 {
    font-size: 20px;
  }
  
  :deep(.verify-dialog) {
    width: 90% !important;
  }
}
</style> 