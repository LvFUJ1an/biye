import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/styles/index.css'

// 引入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
   .use(ElementPlus, {
     locale: zhCn,
     size: 'default'
   })
   .mount('#app')
