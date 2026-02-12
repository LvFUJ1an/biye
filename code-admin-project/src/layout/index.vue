<template>
  <div class="app-wrapper">
    <!-- 头部区域 -->
    <el-header class="app-header">
      <div class="logo">IT知识分享平台</div>
      <div class="header-right">
        <span class="admin-name">管理员</span>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-avatar :size="40" class="avatar">
            A
          </el-avatar>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="userInfo">个人信息</el-dropdown-item>
              <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主体区域 -->
    <el-container class="app-container">
      <!-- 侧边栏菜单 -->
      <el-aside width="220px" class="app-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          background-color="#1f2937"
          text-color="#fff"
          active-text-color="#3b82f6"
        >
          <div class="menu-title">管理菜单</div>
          
          <el-menu-item index="/dashboard">
            <el-icon><Menu /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/section">
            <el-icon><Grid /></el-icon>
            <span>板块管理</span>
          </el-menu-item>
          
          <!-- <el-menu-item index="/stats">
            <el-icon><DataLine /></el-icon>
            <span>数据统计</span>
          </el-menu-item> -->
          
          <el-menu-item index="/documents">
            <el-icon><Document /></el-icon>
            <span>文档中心</span>
          </el-menu-item>
          
          <el-menu-item index="/videos">
            <el-icon><VideoPlay /></el-icon>
            <span>视频中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区域 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, User, Grid, DataLine, Document, VideoPlay } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { message } from '../utils/message'

const route = useRoute()
const router = useRouter()

// 当前活动菜单
const activeMenu = computed(() => {
  return route.path
})

// 下拉菜单操作处理
const handleCommand = (command: string) => {
  switch (command) {
    case 'userInfo':
      // 查看个人信息
      message.info('个人信息功能开发中')
      break
    case 'changePassword':
      // 修改密码
      message.info('修改密码功能开发中')
      break
    case 'logout':
      // 退出登录
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 清除token
        localStorage.removeItem('admin-token')
        message.success('已成功退出登录')
        // 跳转到登录页
        router.push('/login')
      }).catch(() => {
        // 用户取消操作
      })
      break
  }
}

// 挂载时检查当前路由
onMounted(() => {
  // 如果是根路径，自动重定向到仪表盘
  if (route.path === '/') {
    router.push('/dashboard')
  }
})
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.app-header {
  height: 60px;
  background-color: #111827;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  color: white;
  border-bottom: 1px solid #2c3e50;
}

.logo {
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-name {
  font-size: 14px;
}

.avatar {
  cursor: pointer;
  background-color: #3b82f6;
}

.app-container {
  flex: 1;
  overflow: hidden;
}

.app-sidebar {
  background-color: #1f2937;
  overflow-y: auto;
  overflow-x: hidden;
  height: 100%;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.menu-title {
  padding: 16px 20px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.app-main {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
  height: 100%;
}

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style> 