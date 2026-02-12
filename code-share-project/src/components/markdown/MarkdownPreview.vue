<template>
  <div class="markdown-preview">
    <div v-if="!value" class="no-content">暂无内容</div>
    <Viewer v-else :value="value" :plugins="plugins" />
  </div>
</template>

<script>
// 使用普通<script>进行组件导出
export default {
  name: 'MarkdownPreview'
}
</script>

<script setup>
import { ref, onMounted, onUpdated, nextTick, watch } from 'vue'
import { Viewer } from '@bytemd/vue-next'
import gfm from '@bytemd/plugin-gfm'
import highlight from '@bytemd/plugin-highlight'
import math from '@bytemd/plugin-math'
import mermaid from '@bytemd/plugin-mermaid'
import 'bytemd/dist/index.css'
import 'highlight.js/styles/github.css'

// 定义props
const props = defineProps({
  value: {
    type: String,
    default: ''
  }
})

// 插件配置
const plugins = [
  gfm(),
  highlight(),
  math(),
  mermaid()
]

// 设置图片处理器
const setupImageHandlers = () => {
  setTimeout(() => {
    // 获取所有渲染后的图片
    const container = document.querySelector('.markdown-preview')
    if (!container) return
    
    const images = container.querySelectorAll('.markdown-body img')
    
    // 为每个图片添加事件监听器
    images.forEach(img => {
      // 移除旧的事件监听器，避免重复添加
      img.removeEventListener('error', handleImageError)
      img.removeEventListener('click', handleImageClick)
      
      // 添加错误处理
      img.addEventListener('error', handleImageError)
      
      // 添加点击放大效果
      img.addEventListener('click', handleImageClick)
    })
  }, 100) // 等待渲染完成
}

// 处理图片加载错误
const handleImageError = (event) => {
  const img = event.target
  img.classList.add('error')
  img.setAttribute('alt', '图片加载失败')
  console.error('图片加载失败:', img.src)
}

// 处理图片点击（可以实现预览放大功能）
const handleImageClick = (event) => {
  const img = event.target
  console.log('图片点击:', img.src)
  
  // 如果有可用的图片预览组件，可以在这里调用
  if (window.ElMessageBox && window.ElMessageBox.alert) {
    window.ElMessageBox.alert(`<div style="text-align:center"><img src="${img.src}" style="max-width:100%" /></div>`, '图片预览', {
      dangerouslyUseHTMLString: true,
      showConfirmButton: false,
      showClose: true,
      customClass: 'image-preview-dialog'
    })
  }
}

// 组件挂载后设置图片处理器
onMounted(() => {
  console.log('MarkdownPreview已挂载，初始内容:', props.value ? props.value.substring(0, 20) + '...' : 'empty')
  setupImageHandlers()
})

// 组件更新后重新设置图片处理器
onUpdated(() => {
  setupImageHandlers()
})

// 监听value变化
watch(() => props.value, (newVal) => {
  console.log('MarkdownPreview接收到新内容:', newVal ? newVal.substring(0, 20) + '...' : 'empty')
  // 当内容变化时，在下一个渲染周期重新设置图片处理器
  nextTick(() => {
    setupImageHandlers()
  })
}, { immediate: true })
</script>

<style>
/* 不使用scoped，确保样式可以应用到深层嵌套的元素 */
.markdown-preview {
  width: 100%;
}

.no-content {
  color: #909399;
  text-align: center;
  padding: 20px;
  font-style: italic;
}

/* 增强预览效果的样式 */
.markdown-body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.8;
  color: #24292e;
}

/* 标题样式 */
.markdown-body h1 {
  font-size: 2em;
  margin-top: 28px;
  margin-bottom: 16px;
  font-weight: 600;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #eaecef;
}

.markdown-body h2 {
  font-size: 1.5em;
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #eaecef;
}

.markdown-body h3 {
  font-size: 1.25em;
  margin-top: 20px;
  margin-bottom: 16px;
  font-weight: 600;
}

/* 代码块样式 */
.markdown-body pre {
  margin-top: 16px;
  margin-bottom: 16px;
  border-radius: 6px;
  background-color: #f6f8fa;
  padding: 16px;
}

.markdown-body code {
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
  font-family: 'SF Mono', Monaco, Consolas, 'Courier New', monospace;
  font-size: 85%;
  padding: 0.2em 0.4em;
}

/* 列表样式 */
.markdown-body ul, .markdown-body ol {
  padding-left: 2em;
  margin-top: 0;
  margin-bottom: 16px;
}

/* 引用样式 */
.markdown-body blockquote {
  margin: 16px 0;
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
}

/* 表格样式 */
.markdown-body table {
  display: block;
  width: 100%;
  overflow: auto;
  margin-top: 0;
  margin-bottom: 16px;
  border-spacing: 0;
  border-collapse: collapse;
}

.markdown-body table tr {
  background-color: #fff;
  border-top: 1px solid #c6cbd1;
}

.markdown-body table th, .markdown-body table td {
  padding: 6px 13px;
  border: 1px solid #dfe2e5;
}

.markdown-body table th {
  font-weight: 600;
  background-color: #f6f8fa;
}

/* 图片样式 */
.markdown-body img {
  max-width: 100%;
  box-sizing: content-box;
  margin: 8px 0;
  border-radius: 4px;
}

.markdown-body img.error {
  border: 1px dashed #ff4d4f;
  padding: 8px;
  position: relative;
}

/* 水平线样式 */
.markdown-body hr {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background-color: #e1e4e8;
  border: 0;
}

/* 图片预览对话框样式 */
.image-preview-dialog {
  max-width: 90vw !important;
}

.image-preview-dialog .el-message-box__content {
  padding: 10px;
}
</style>