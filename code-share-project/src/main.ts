import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/main.css'
import axios from 'axios'
import 'bytemd/dist/index.css'
// 配置axios默认值
axios.defaults.baseURL = 'http://localhost:18000'
axios.defaults.timeout = 10000

// 创建应用实例
const app = createApp(App)

// 注册全局属性
app.config.globalProperties.$axios = axios

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app')
