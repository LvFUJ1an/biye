<template>
  <div class="markdown-edit" :class="{ 'uploading': uploadingImages }">
    <Editor 
      :value="value" 
      :plugins="plugins" 
      :locale="locale" 
      :uploadImages="uploadImages" 
      @change="handleChange"
    />
  </div>
</template>
 
<script>
// 使用普通<script>进行组件导出
export default {
  name: 'MarkdownEdit'
}
</script>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Editor } from '@bytemd/vue-next'
import gfm from '@bytemd/plugin-gfm'
import highlight from '@bytemd/plugin-highlight'
import math from '@bytemd/plugin-math'
import mermaid from '@bytemd/plugin-mermaid'
import { ElMessage, ElNotification } from 'element-plus'
import 'bytemd/dist/index.css'
import 'highlight.js/styles/github.css'

// 组件定义名称
defineOptions({
  name: 'MarkdownEdit'
})

// 定义props
const props = defineProps({
  value: {
    type: String,
    default: ''
  }
})

// 定义事件
const emit = defineEmits(['update:value', 'change'])

// 上传状态
const uploadingImages = ref(false)

// 插件配置
const plugins = [
  gfm(),
  highlight(),
  math(),
  mermaid()
]

// 本地化配置
const locale = {
  "bold": "加粗",
  "italic": "斜体",
  "quote": "引用",
  "code": "代码",
  "link": "链接",
  "image": "图片",
  "heading": "标题",
  "unordered-list": "无序列表",
  "ordered-list": "有序列表",
  "check-list": "任务列表",
  "indent-increase": "增加缩进",
  "indent-decrease": "减少缩进",
  "undo": "撤销",
  "redo": "重做",
  "fullscreen": "全屏",
  "upload": "上传图片",
  "side-by-side": "并列预览",
  "write": "编辑",
  "preview": "预览",
  "toc": "目录",
  "help": "帮助",
  "code-block": "代码块",
  "paste": "粘贴",
  "cut": "剪切",
  "copy": "复制",
  "hr": "水平线",
  "strikethrough": "删除线",
  "table": "表格",
  "edit": "编辑",
  "view": "查看",
  "hide-preview": "隐藏预览",
  "hide-editor": "隐藏编辑器",
  "error": "错误",
  "info": "信息",
  "warning": "警告",
  "success": "成功",
  "math-block": "数学公式块",
  "math-inline": "行内数学公式",
  "mermaid": "Mermaid 图表",
  "footnote-ref": "脚注引用",
  "footnote-def": "脚注定义",
  "drop-upload": "拖拽上传图片",
  "loading": "加载中...",
  "uploading": "上传中..."
}

// 处理内容变更
const handleChange = (value) => {
  console.log('MarkdownEdit内容更改:', value ? value.substring(0, 20) + '...' : 'empty')
  emit('update:value', value)
  emit('change', value)
}

// 图片上传处理函数
const uploadImages = async (files) => {
  // 设置上传状态标记
  uploadingImages.value = true
  const baseUrl = 'http://localhost:18000' // 本地开发环境基础URL
  const uploadUrl = `${baseUrl}/file/blogImage` // 指定的博客图片上传接口
  
  // 从localStorage获取token而不是全局变量
  const token = localStorage.getItem('token')

  if (!token) {
    ElMessage.error('未登录或登录已过期，请重新登录后再上传图片')
    uploadingImages.value = false
    return []
  }
  
  // 显示上传开始的消息
  ElNotification({
    title: '图片上传',
    message: `正在上传 ${files.length} 张图片...`,
    type: 'info',
    duration: 2000
  })
  
  try {
    const results = await Promise.all(
      files.map(async (file) => {
        try {
          // 验证文件类型和大小
          if (!file.type.startsWith('image/')) {
            ElMessage.error(`文件 ${file.name} 不是图片类型`)
            return null
          }
          
          if (file.size > 5 * 1024 * 1024) { // 5MB限制
            ElMessage.error(`图片 ${file.name} 超过5MB限制`)
            return null
          }
          
          // 创建表单数据
          const formData = new FormData()
          formData.append('file', file)
          
          // 发送上传请求
          const response = await fetch(uploadUrl, {
            method: 'POST',
            headers: {
              Authorization: `Bearer ${token}`
            },
            body: formData
          })
          
          // 解析响应
          const data = await response.json()
          
          // 处理响应结果
          if (data.code === 0) {
            console.log('图片上传成功:', data.data)
            return {
              url: data.data, // 直接使用返回的图片URL
              alt: file.name,
              title: file.name
            }
          } else {
            console.error('图片上传失败:', data.message)
            ElMessage.error(`图片 ${file.name} 上传失败: ${data.message}`)
            return null
          }
        } catch (error) {
          console.error('上传图片错误:', error)
          ElMessage.error(`图片 ${file.name} 上传发生错误`)
          return null
        }
      })
    )
    
    // 过滤掉失败的上传（null值）
    const successfulUploads = results.filter(Boolean)
    
    // 显示上传结果消息
    if (successfulUploads.length > 0) {
      ElNotification({
        title: '上传成功',
        message: `成功上传了 ${successfulUploads.length} 张图片`,
        type: 'success',
        duration: 3000
      })
    }
    
    return successfulUploads
  } catch (error) {
    console.error('图片上传过程发生错误:', error)
    ElMessage.error('图片上传失败，请重试')
    return []
  } finally {
    uploadingImages.value = false
  }
}

// 监听props.value变化
watch(() => props.value, (newVal) => {
  console.log('MarkdownEdit接收到新值:', newVal ? newVal.substring(0, 20) + '...' : 'empty')
}, { immediate: true })

// 组件挂载后
onMounted(() => {
  console.log('MarkdownEdit组件已挂载，初始内容:', props.value ? props.value.substring(0, 20) + '...' : 'empty')
})
</script>

<style>
.markdown-edit {
  width: 100%;
  height: 100%;
  position: relative;
}

.markdown-edit .bytemd {
  height: calc(100vh - 150px);
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.markdown-edit .bytemd-toolbar {
  border-bottom: 1px solid #eaecef;
  background-color: #fafbfc;
  padding: 6px 12px;
}

.markdown-edit .bytemd-toolbar-left {
  display: flex;
  flex-wrap: wrap;
}

.markdown-edit .bytemd-toolbar-right {
  margin-left: 10px;
}

.markdown-edit .bytemd-toolbar-icon {
  margin-right: 10px;
  color: #606266;
}

.markdown-edit .bytemd-status {
  background-color: #f8f9fa;
  color: #909399;
  border-top: 1px solid #eaecef;
}

.markdown-edit .bytemd-editor {
  font-family: 'SF Mono', Monaco, Consolas, 'Courier New', monospace;
}

/* 定义上传状态时的覆盖层 */
.markdown-edit.uploading::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.markdown-edit.uploading::before {
  content: '正在上传图片...';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px 20px;
  border-radius: 4px;
  z-index: 11;
}
</style>