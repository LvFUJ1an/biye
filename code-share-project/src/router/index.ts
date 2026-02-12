import { createRouter, createWebHistory } from 'vue-router'

// 导入路由组件
// import HomeView from '@/views/HomeView.vue'
// import QuestionView from '@/views/question/QuestionView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/editor/blog',
      name: 'blogEditor',
      component: () => import('../views/editor/BlogEditor.vue'),
      meta: {
        requiresAuth: true,
        title: '写博客 - IT技术社区'
      }
    },
    {
      path: '/article/:id',
      name: 'articleDetail',
      component: () => import('../views/blog/ArticleDetail.vue'),
      meta: {
        title: '文章详情 - IT技术社区'
      }
    },
    {
      path: '/video',
      name: 'videoList',
      component: () => import('../views/video/VideoList.vue'),
      meta: {
        title: '视频教程 - IT技术社区'
      }
    },
    {
      path: '/video/detail/:id',
      name: 'videoDetail',
      component: () => import('../views/video/VideoDetail.vue'),
      meta: {
        title: '视频播放 - IT技术社区'
      }
    },
    {
      path: '/video/create',
      name: 'videoCreate',
      component: () => import('../views/video/VideoCreateView.vue'),
      meta: {
        requiresAuth: true,
        title: '上传视频 - IT技术社区'
      }
    },
    {
      path: '/video/edit/:id',
      name: 'videoEdit',
      component: () => import('../views/video/VideoEditView.vue'),
      meta: {
        requiresAuth: true,
        title: '修改视频 - IT技术社区'
      }
    },
    {
      path: '/ebook',
      name: 'ebookList',
      component: () => import('../views/ebook/EbookList.vue'),
      meta: {
        title: '电子书 - IT技术社区'
      }
    },
    {
      path: '/ebook/detail/:id',
      name: 'ebookDetail',
      component: () => import('../views/ebook/EbookDetail.vue'),
      meta: {
        title: '电子书详情 - IT技术社区'
      }
    },
    // 问答路由
    {
      path: '/question',
      name: 'question',
      component: () => import('../views/question/QuestionView.vue'),
      meta: {
        title: '问答社区 - IT技术社区'
      }
    },
    // 问题详情
    {
      path: '/question/detail/:id',
      name: 'questionDetail',
      component: () => import('../views/question/QuestionDetailView.vue'),
      meta: {
        title: '问题详情 - IT技术社区'
      }
    },
    // 创建问题页面
    {
      path: '/question/create',
      name: 'questionCreate',
      component: () => import('../views/question/QuestionCreateView.vue'),
      meta: {
        requiresAuth: true,
        title: '提问 - IT技术社区'
      }
    },
    // 投票列表页面
    {
      path: '/poll',
      name: 'pollList',
      component: () => import('../views/poll/PollListView.vue'),
      meta: {
        title: '投票 - IT技术社区'
      }
    },
    // 创建投票页面
    {
      path: '/poll/create',
      name: 'pollCreate',
      component: () => import('../views/poll/PollCreateView.vue'),
      meta: {
        requiresAuth: true,
        title: '创建投票 - IT技术社区'
      }
    },
    // 投票详情页面
    {
      path: '/poll/detail/:id',
      name: 'pollDetail',
      component: () => import('../views/poll/PollDetailView.vue'),
      meta: {
        title: '投票详情 - IT技术社区'
      }
    },
    // 个人中心页面
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: {
        requiresAuth: true,
        title: '个人中心 - IT技术社区'
      }
    },
    // 聊天室页面
    {
      path: '/chat',
      name: 'chatRoom',
      component: () => import('../views/chat/ChatRoom.vue'),
      meta: {
        requiresAuth: true,
        title: '技术交流聊天室 - IT技术社区'
      }
    },
    // 问题详情和提问页面暂时注释，等组件实现后再启用
    /*
    // 提问页面
    {
      path: '/question/ask',
      name: 'questionAsk',
      component: () => import('../views/question/QuestionAskView.vue'),
      meta: {
        requiresAuth: true
      }
    }
    */
    {
      path: '/question/edit/:id',
      name: 'QuestionEdit',
      component: () => import('../views/question/QuestionEditView.vue'),
      meta: {
        requiresAuth: true,
        title: '修改问题'
      }
    },
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title as string
  } else {
    document.title = 'IT技术社区'
  }

  // 检查是否需要登录
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('token')
    if (!token) {
      next({
        path: '/',
        query: { redirect: to.fullPath }
      })
      return
    }
  }
  
  next()
})

export default router 