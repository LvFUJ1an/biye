<template>
  <div class="blog-editor-container">
    <div class="editor-header">
      <div class="container">
        <div class="header-left">
          <div class="editor-type-indicator">
            <el-tag type="info" size="small" v-if="isEditMode">编辑模式</el-tag>
            <el-tag type="success" size="small" v-else>创建模式</el-tag>
          </div>
          <el-input 
            v-model="blogTitle" 
            placeholder="请输入文章标题" 
            class="title-input"
            maxlength="100"
            show-word-limit
          />
          <el-select 
            v-model="selectedCategory" 
            placeholder="请选择分类" 
            class="category-select"
            :loading="loadingCategories"
          >
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
            <template #empty>
              <el-empty v-if="!loadingCategories && !categoryError" description="暂无分类数据" />
              <div v-else-if="categoryError" class="select-error">{{ categoryError }}</div>
              <div v-else class="select-loading">加载中...</div>
            </template>
          </el-select>
        </div>
        <div class="header-right">
          <div class="save-status" v-if="lastSaved">
            <el-icon v-if="hasChanges" class="edit-icon"><Edit /></el-icon>
            <el-icon v-else><Check /></el-icon>
            <span>{{ hasChanges ? '编辑中...' : '已保存 · ' + formatSaveTime(lastSaved) }}</span>
          </div>
          <el-button @click="togglePreview" type="info" plain>
            {{ isPreviewMode ? '返回编辑' : '预览' }}
          </el-button>
          <el-button @click="saveDraft" :loading="savingDraft">保存草稿</el-button>
          <el-button type="primary" @click="publishBlog" :loading="publishing">{{ isEditMode ? '更新文章' : '发布文章' }}</el-button>
        </div>
      </div>
    </div>
    
    <div class="editor-body container">
      <!-- 编辑模式 -->
      <div v-if="!isPreviewMode" class="editor-wrapper">
        <markdown-edit
          :value="editorContent"
          @update:value="updateContent"
          ref="markdownEditor"
        />
      </div>
      
      <!-- 预览模式 -->
      <div v-else>
        <div class="preview-article">
          <h1 class="preview-title">{{ blogTitle || '无标题文章' }}</h1>
          <div class="preview-meta">
            <span class="preview-category">
              <el-tag size="small" type="success">
                {{ getCategoryName(selectedCategory) || '未分类' }}
              </el-tag>
            </span>
            <span class="preview-date">{{ formatDate(new Date()) }}</span>
          </div>
          <div class="preview-content">
            <markdown-preview :value="editorContent" />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 发布确认对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      :title="isEditMode ? '更新文章' : '发布文章'"
      width="500px"
      class="publish-dialog"
    >
      <div class="publish-form">
        <p class="dialog-tips">{{ isEditMode ? '更新后将应用最新的修改内容' : '发布后将展示在技术社区首页' }}</p>
        
        <el-form label-position="top">
          <el-form-item :label="isEditMode ? '确认更新' : '确认发布'">
            <p>{{ isEditMode ? '您确定要更新这篇文章吗？' : '您确定要发布这篇文章吗？' }}</p>
            <p>标题: {{ blogTitle }}</p>
            <p>分类: {{ getCategoryName(selectedCategory) || '未分类' }}</p>
            <p v-if="isEditMode">ID: {{ blogId }}</p>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPublish" :loading="publishing">
            {{ isEditMode ? '确认更新' : '确认发布' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue'
import { Editor } from '@bytemd/vue-next'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Plus, Edit, Check } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import gfm from '@bytemd/plugin-gfm'
import highlight from '@bytemd/plugin-highlight'
import math from '@bytemd/plugin-math'
import mermaid from '@bytemd/plugin-mermaid'
import 'bytemd/dist/index.css'
import 'highlight.js/styles/github.css'
import api from '../../services/api'
import MarkdownPreview from '../../components/markdown/MarkdownPreview.vue'
import MarkdownEdit from '../../components/markdown/MarkdownEdit.vue'
import { onBeforeRouteLeave } from 'vue-router'
import blogService from '../../services/blog'

const router = useRouter()
const route = useRoute()

// 是否为编辑模式（根据URL参数判断）
const isEditMode = computed(() => Boolean(route.query.id))
const blogId = computed(() => route.query.id as string)

// 定义组件属性类型
interface Props {
  value: string;
  onChange?: (v: string) => void;
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  onChange: (value: string) => {}
});

// 通过emit传递内容变更
const emit = defineEmits(['update:value', 'change']);

// 编辑器内容 - 用于内部状态管理
const editorContent = ref(props.value);

// 处理内容变更
const updateContent = (value: string) => {
  editorContent.value = value;
  emit('update:value', value);
  emit('change', value);
  props.onChange(value);
  hasChanges.value = true;
};

// 监听props.value变化
watch(() => props.value, (newValue) => {
  if (newValue !== editorContent.value) {
    editorContent.value = newValue;
  }
}, { immediate: true });

// 编辑器内容
const blogTitle = ref('')
const selectedCategory = ref('')
const summary = ref('')
const coverImage = ref('')

// 分类数据
const categories = ref([])
const loadingCategories = ref(false)
const categoryError = ref('')

// 预览模式控制
const isPreviewMode = ref(false)

// 自动保存相关
const lastSaved = ref(new Date())
const autoSaveInterval = ref<number | null>(null)
const autoSaveDelay = 30000 // 30秒自动保存一次
const hasChanges = ref(false)

// 标签相关
const dynamicTags = ref<string[]>([])
const inputTagVisible = ref(false)
const inputTagValue = ref('')
const tagInputRef = ref<HTMLInputElement>()

// 状态控制
const savingDraft = ref(false)
const publishing = ref(false)
const publishDialogVisible = ref(false)

// 在变量定义部分添加一个临时存储sectionId的变量
const tempSectionId = ref<number | null>(null);

// 在其他变量定义部分添加
const skipSaveOnUnmount = ref(false)

// ByteMD 编辑器配置
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

const plugins = [
  gfm(),
  highlight(),
  math(),
  mermaid()
]

// 上传配置
const uploadAction = 'http://localhost:18000/file/upload'
const uploadHeaders = {
  get Authorization() {
    return `Bearer ${localStorage.getItem('token')}`
  }
}

// 获取分类数据
const fetchCategories = async () => {
  loadingCategories.value = true
  categoryError.value = ''
  
  try {
    const response = await api.post('/sections/list') as any
    
    // 如果请求成功
    if (response && response.code === 0) {
      categories.value = response.data || []
      
      // 分类数据加载完成后，设置已有博客的分类
      if (isEditMode.value && tempSectionId.value !== null) {
        // 从分类列表中查找匹配的分类
        const foundCategory = categories.value.find(cat => cat.id === tempSectionId.value)
        if (foundCategory) {
          console.log('找到匹配分类:', foundCategory.name, foundCategory.id)
          selectedCategory.value = foundCategory.id.toString()
        } else {
          console.warn('未找到匹配的分类，ID:', tempSectionId.value)
          selectedCategory.value = ''
        }
      }
    } else {
      categoryError.value = (response && response.message) || '获取分类数据失败'
      ElMessage.warning(categoryError.value)
    }
  } catch (err) {
    console.error('获取分类数据出错:', err)
    categoryError.value = '网络错误，请稍后重试'
    ElMessage.error(categoryError.value)
  } finally {
    loadingCategories.value = false
  }
}

// 获取分类名称
const getCategoryName = (id: string | number) => {
  if (!id) return ''
  const category = categories.value.find(item => item.id === Number(id))
  return category ? category.name : ''
}

// 格式化日期
const formatDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 切换预览模式
const togglePreview = () => {
  isPreviewMode.value = !isPreviewMode.value
}

// 图片上传处理函数
const uploadImages = async (files: File[]) => {
  const results = []
  
  for (const file of files) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      const response = await fetch(uploadAction, {
        method: 'POST',
        headers: {
          Authorization: uploadHeaders.Authorization
        },
        body: formData
      })
      
      const data = await response.json()
      
      if (data.code === 0) {
        results.push({
          url: data.data.url,
          alt: file.name
        })
      } else {
        ElMessage.error(`图片 ${file.name} 上传失败: ${data.message}`)
      }
    } catch (error) {
      console.error('上传图片错误:', error)
      ElMessage.error(`图片 ${file.name} 上传失败`)
    }
  }
  
  return results
}

// 封面图片上传前的校验
const beforeCoverUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  
  return true
}

// 封面图片上传成功回调
const handleCoverSuccess = (response: any, file: File) => {
  if (response.code === 0) {
    coverImage.value = response.data.url
  } else {
    ElMessage.error('封面上传失败: ' + response.message)
  }
}

// 格式化保存时间显示
const formatSaveTime = (date: Date) => {
  // 如果是今天，只显示时间
  const now = new Date()
  if (
    date.getDate() === now.getDate() &&
    date.getMonth() === now.getMonth() &&
    date.getFullYear() === now.getFullYear()
  ) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  // 否则显示日期和时间
  return date.toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取博客详情
const fetchBlogDetail = async () => {
  if (!isEditMode.value || !blogId.value) return
  
  try {
    const response = await blogService.getBlogDetail(blogId.value)
    
    if (response && response.code === 0 && response.data) {
      // 填充博客数据到表单
      blogTitle.value = response.data.title
      editorContent.value = response.data.content
      
      // 保存原始sectionId，等待分类数据加载完成后再设置
      tempSectionId.value = response.data.sectionId || null
      
      // 重置修改状态
      hasChanges.value = false
      lastSaved.value = new Date(response.data.updateTime || response.data.createTime)
      
      // 更新页面标题
      document.title = `编辑: ${blogTitle.value} - IT技术社区`
      
      console.log('编辑博客数据加载成功:', blogTitle.value, '待设置的分类ID:', tempSectionId.value)
    } else {
      ElMessage.error(response?.message || '获取博客数据失败')
      router.push('/profile') // 获取失败时返回个人中心
    }
  } catch (error) {
    console.error('获取博客详情出错:', error)
    ElMessage.error('获取博客数据失败，请稍后重试')
    router.push('/profile') // 出错时返回个人中心
  }
}

// 自动保存函数
const autoSave = async () => {
  if (!hasChanges.value) return;
  
  if (!blogTitle.value.trim() || !editorContent.value.trim()) return;
  
  try {
    // 构建请求参数
    const params: {
      id?: string | number;
      title: string;
      content: string;
      sectionId: number | null;
      status: number;
      authorId?: number;
    } = {
      title: blogTitle.value,
      content: editorContent.value,
      sectionId: selectedCategory.value ? parseInt(selectedCategory.value) : null,
      status: 0 // 0-草稿
    };
    
    // 编辑模式下添加ID，如果没有ID，则不进行自动保存
    if (isEditMode.value) {
      if (!blogId.value) {
        console.warn('编辑模式下缺少博客ID，跳过自动保存');
        return;
      }
      params.id = blogId.value;
    }
    
    // 如果有用户ID，则添加到请求中
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      try {
        const parsedUserInfo = JSON.parse(userInfo);
        if (parsedUserInfo && parsedUserInfo.id) {
          params.authorId = parsedUserInfo.id;
        }
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    
    console.log(`自动保存开始 - 模式: ${isEditMode.value ? '编辑' : '创建'}, ID: ${params.id || '新文章'}`);
    
    // 根据是否为编辑模式选择API
    let response;
    if (isEditMode.value && blogId.value) {
      response = await blogService.updateBlog(params);
      console.log('调用更新API:', params);
    } else {
      response = await blogService.createBlog(params);
      console.log('调用创建API:', params);
    }
    
    if (response && response.code === 0) {
      hasChanges.value = false;
      lastSaved.value = new Date();
      console.log('自动保存成功:', lastSaved.value.toLocaleTimeString());
      
      // 如果是新创建的博客，更新URL以支持后续的编辑操作
      if (!isEditMode.value && response.data && response.data.id) {
        const newId = response.data.id || response.data;
        console.log('新博客已创建，ID:', newId);
        
        // 转换为编辑模式（通过路由更新）
        document.title = `编辑: ${blogTitle.value} - IT技术社区`;
        // 使用replace而非push，避免导航历史堆积
        router.replace(`/editor/blog?id=${newId}`);
      }
    }
  } catch (error) {
    console.error('自动保存错误:', error);
  }
};

// 保存草稿
const saveDraft = async () => {
  if (!blogTitle.value.trim()) {
    ElMessage.warning('请输入文章标题');
    return;
  }
  
  if (!editorContent.value.trim()) {
    ElMessage.warning('请输入文章内容');
    return;
  }
  
  savingDraft.value = true;
  
  try {
    // 构建请求参数
    const params: {
      id?: string | number;
      title: string;
      content: string;
      sectionId: number | null;
      status: number;
      authorId?: number;
    } = {
      title: blogTitle.value,
      content: editorContent.value,
      sectionId: selectedCategory.value ? parseInt(selectedCategory.value) : null,
      status: 0 // 0-草稿
    };
    
    // 编辑模式下添加ID
    if (isEditMode.value) {
      if (!blogId.value) {
        ElMessage.warning('编辑模式下缺少博客ID，无法保存');
        savingDraft.value = false;
        return;
      }
      params.id = blogId.value;
    }
    
    // 如果有用户ID，则添加到请求中
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      try {
        const parsedUserInfo = JSON.parse(userInfo);
        if (parsedUserInfo && parsedUserInfo.id) {
          params.authorId = parsedUserInfo.id;
        }
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    
    console.log(`保存草稿 - 模式: ${isEditMode.value ? '编辑' : '创建'}, ID: ${params.id || '新文章'}`);
    
    // 根据是否为编辑模式选择API
    let response;
    if (isEditMode.value && blogId.value) {
      response = await blogService.updateBlog(params);
      console.log('调用更新API:', params);
    } else {
      response = await blogService.createBlog(params);
      console.log('调用创建API:', params);
    }
    
    if (response && response.code === 0) {
      ElMessage.success(isEditMode.value ? '草稿更新成功' : '草稿保存成功');
      hasChanges.value = false;
      lastSaved.value = new Date();
      
      // 如果是新创建的博客，更新URL以支持后续的编辑操作
      if (!isEditMode.value && response.data && response.data.id) {
        const newId = response.data.id || response.data;
        console.log('新博客已创建，ID:', newId);
        
        // 转换为编辑模式（通过路由更新）
        document.title = `编辑: ${blogTitle.value} - IT技术社区`;
        // 使用replace而非push，避免导航历史堆积
        router.replace(`/editor/blog?id=${newId}`);
      }
    } else {
      ElMessage.error((response && response.message) || '保存失败');
    }
  } catch (error) {
    console.error('保存草稿错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    savingDraft.value = false;
  }
};

// 发布文章 - 显示确认对话框
const publishBlog = () => {
  if (!blogTitle.value.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  
  if (!editorContent.value.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }
  
  if (!selectedCategory.value) {
    ElMessage.warning('请选择文章分类')
    return
  }
  
  publishDialogVisible.value = true
}

// 确认发布文章
const confirmPublish = async () => {
  publishing.value = true;
  
  try {
    // 构建请求参数
    const params: {
      id?: string | number;
      title: string;
      content: string;
      sectionId: number | null;
      status: number;
      authorId?: number;
    } = {
      title: blogTitle.value,
      content: editorContent.value,
      sectionId: selectedCategory.value ? parseInt(selectedCategory.value) : null,
      status: 1 // 1-已发布
    };
    
    // 编辑模式下添加ID
    if (isEditMode.value) {
      if (!blogId.value) {
        ElMessage.warning('编辑模式下缺少博客ID，无法发布');
        publishing.value = false;
        return;
      }
      params.id = blogId.value;
    }
    
    // 如果有用户ID，则添加到请求中
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      try {
        const parsedUserInfo = JSON.parse(userInfo);
        if (parsedUserInfo && parsedUserInfo.id) {
          params.authorId = parsedUserInfo.id;
        }
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    
    console.log(`发布博客 - 模式: ${isEditMode.value ? '编辑' : '创建'}, ID: ${params.id || '新文章'}`);
    
    // 根据是否为编辑模式选择API
    let response;
    if (isEditMode.value && blogId.value) {
      response = await blogService.updateBlog(params);
      console.log('调用更新API:', params);
    } else {
      response = await blogService.createBlog(params);
      console.log('调用创建API:', params);
    }
    
    if (response && response.code === 0) {
      ElMessage.success(isEditMode.value ? '文章更新成功' : '文章发布成功');
      publishDialogVisible.value = false;
      hasChanges.value = false;
      
      // 发布成功后的跳转处理
      if (isEditMode.value) {
        // 更新成功后，跳转回个人中心
        router.push('/profile');
      } else {
        // 新发布的文章，跳转到文章详情页
        let articleId = response.data.id || response.data;
        router.push(`/article/${articleId}`);
      }
    } else {
      ElMessage.error((response && response.message) || '发布失败');
    }
  } catch (error) {
    console.error('发布文章错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    publishing.value = false;
  }
};

// 标签相关方法
const handleTagClose = (tag: string) => {
  dynamicTags.value.splice(dynamicTags.value.indexOf(tag), 1)
}

const showTagInput = () => {
  inputTagVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

const handleTagInputConfirm = () => {
  if (inputTagValue.value) {
    if (dynamicTags.value.length >= 5) {
      ElMessage.warning('最多只能添加5个标签')
    } else if (!dynamicTags.value.includes(inputTagValue.value)) {
      dynamicTags.value.push(inputTagValue.value)
    }
  }
  
  inputTagVisible.value = false
  inputTagValue.value = ''
}

// 监控预览模式变化
watch(() => isPreviewMode.value, (newVal) => {
  if (newVal) {
    console.log('切换到预览模式，content:', editorContent.value ? editorContent.value.substring(0, 100) + '...' : 'empty')
  }
})

// 组件挂载时添加样式处理
onMounted(async () => {
  // 检查登录状态
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessageBox.alert('请先登录后再进行创作', '提示', {
      confirmButtonText: '确定',
      callback: () => {
        router.push('/')
      }
    })
    return
  }
  
  // 隐藏导航栏
  const navHeader = document.querySelector('.nav-header') as HTMLElement
  if (navHeader) {
    navHeader.style.display = 'none'
  }
  
  // 获取分类数据
  await fetchCategories()
  
  // 如果是编辑模式，获取博客详情
  if (isEditMode.value) {
    await fetchBlogDetail()
  }
  
  // 启动自动保存
  autoSaveInterval.value = window.setInterval(autoSave, autoSaveDelay)
})

// 组件卸载前还原样式
onBeforeUnmount(() => {
  if (autoSaveInterval.value) {
    clearInterval(autoSaveInterval.value)
  }
  
  // 只有当没有设置跳过保存标志时，才进行自动保存
  if (!skipSaveOnUnmount.value && hasChanges.value && blogTitle.value.trim() && editorContent.value.trim()) {
    autoSave()
  }
  
  // 恢复导航栏
  const navHeader = document.querySelector('.nav-header') as HTMLElement
  if (navHeader) {
    navHeader.style.display = 'flex'
  }
})

// 监听路由变化，如果用户离开编辑页面，提示保存
onBeforeRouteLeave((to, from, next) => {
  if (hasChanges.value && blogTitle.value.trim() && editorContent.value.trim()) {
    ElMessageBox.confirm('您有未保存的更改，是否保存后再离开？', '提示', {
      confirmButtonText: '保存并离开',
      cancelButtonText: '直接离开',
      type: 'warning'
    }).then(async () => {
      await saveDraft();
      // 保存后跳转到个人中心
      if (to.path !== '/profile') {
        next('/profile');
      } else {
        next();
      }
    }).catch(() => {
      // 直接离开时设置标志，防止组件卸载时自动保存
      skipSaveOnUnmount.value = true;
      // 直接离开时也跳转到个人中心
      if (to.path !== '/profile') {
        next('/profile');
      } else {
        next();
      }
    });
  } else {
    // 如果没有更改，也默认跳转到个人中心
    if (to.path !== '/profile') {
      next('/profile');
    } else {
      next();
    }
  }
});
</script>

<style scoped>
@import 'highlight.js/styles/github.css';

.blog-editor-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.editor-header {
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  padding: 12px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.container {
  width: 100%;
  margin: 0 auto;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.editor-type-indicator {
  margin-right: 10px;
}

.title-input {
  flex: 1;
}

.title-input :deep(.el-input__inner) {
  font-size: 16px;
  height: 44px;
  padding-left: 16px;
  border-radius: 4px;
}

.category-select {
  width: 140px;
}

.editor-header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-body {
  flex: 1;
  padding: 24px 20px;
  margin-top: 8px;
}

.editor-wrapper,
.preview-wrapper {
  width: 100%;
  height: 100%;
}

.preview-article {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  padding: 30px;
  margin-bottom: 30px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.preview-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.preview-meta {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  color: #666;
  gap: 16px;
}

.preview-date {
  font-size: 14px;
  color: #909399;
}

.preview-content {
  margin-top: 20px;
  border-top: 1px solid #eaecef;
  padding-top: 20px;
}

/* ByteMD 编辑器样式覆盖 */
:deep(.bytemd) {
  height: calc(100vh - 150px);
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

:deep(.bytemd-toolbar) {
  border-bottom: 1px solid #eaecef;
  background-color: #fafbfc;
  padding: 6px 12px;
}

:deep(.bytemd-toolbar-left) {
  display: flex;
  flex-wrap: wrap;
}

:deep(.bytemd-toolbar-right) {
  margin-left: 10px;
}

:deep(.bytemd-toolbar-icon) {
  margin-right: 10px;
  color: #606266;
}

:deep(.bytemd-status) {
  background-color: #f8f9fa;
  color: #909399;
  border-top: 1px solid #eaecef;
}

:deep(.bytemd-editor) {
  font-family: 'SF Mono', Monaco, Consolas, 'Courier New', monospace;
}

:deep(.markdown-body) {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
}

/* 发布对话框样式 */
.dialog-tips {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.tag-input {
  width: 100px;
}

/* 封面上传样式 */
.cover-uploader {
  width: 100%;
}

.cover-image {
  width: 240px;
  height: 135px;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cover-placeholder {
  width: 240px;
  height: 135px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
  cursor: pointer;
  transition: all 0.3s;
}

.cover-placeholder:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.cover-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.save-status {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
  padding: 0 8px;
  height: 28px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.save-status .el-icon {
  font-size: 14px;
  color: #67c23a;
}

.save-status .el-icon.is-loading,
.save-status .edit-icon {
  color: #909399;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% { transform: rotate(0); }
  100% { transform: rotate(360deg); }
}

/* 发布对话框样式修复 */
:deep(.publish-dialog .el-dialog__body) {
  padding: 20px 24px;
}

:deep(.publish-dialog .el-dialog__header) {
  padding-bottom: 16px;
  margin-bottom: 0;
  border-bottom: 1px solid #eaecef;
}

:deep(.publish-dialog .el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
}

:deep(.publish-dialog .el-dialog__footer) {
  padding-top: 16px;
  border-top: 1px solid #eaecef;
}

.markdown-content {
  padding: 10px 0;
  min-height: 200px;
}

.select-error {
  color: #f56c6c;
  text-align: center;
  padding: 10px 0;
}

.select-loading {
  color: #909399;
  text-align: center;
  padding: 10px 0;
}
</style> 