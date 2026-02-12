import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../layout/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Menu' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'section',
        name: 'Section',
        component: () => import('../views/section/index.vue'),
        meta: { title: '板块管理', icon: 'Grid' }
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/stats/index.vue'),
        meta: { title: '数据统计', icon: 'DataLine' }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import('../views/documents/index.vue'),
        meta: { title: '文档中心', icon: 'Document' }
      },
      {
        path: 'videos',
        name: 'Videos',
        component: () => import('../views/videos/index.vue'),
        meta: { title: '视频中心', icon: 'VideoPlay' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin-token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router 