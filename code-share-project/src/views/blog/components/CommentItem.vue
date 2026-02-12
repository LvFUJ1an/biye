<template>
  <div class="comment-item" :class="{ 'nested-comment': depth > 0 }">
    <!-- 评论内容 -->
    <div class="comment-content">
      <el-avatar 
        :size="depth > 0 ? 32 : 40" 
        :src="comment.avatarUrl || defaultAvatar"
      ></el-avatar>
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-author">{{ comment.username || '匿名用户' }}</span>
          <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
        </div>
        <div class="comment-text">{{ comment.content }}</div>
        <div class="comment-footer">
          <el-button type="text" size="small" @click="handleReply(comment)">
            回复
          </el-button>
          <!-- 添加删除按钮，仅当评论作者为当前用户时显示 -->
          <el-button 
            v-if="isCurrentUserComment(comment)" 
            type="text" 
            size="small" 
            @click="$emit('delete', comment)"
            style="color: #f56c6c;"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 回复输入框 -->
    <div v-if="replyingTo === comment.id" class="reply-input-container">
      <el-avatar :size="32" :src="userAvatar || defaultAvatar"></el-avatar>
      <div class="reply-input-wrapper">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="2"
          placeholder="回复评论..."
          :disabled="replySubmitting"
          maxlength="500"
          show-word-limit
        />
        <div class="reply-preview" v-if="replyContent">
          <div class="preview-title">预览</div>
          <div class="preview-content">{{ replyContent }}</div>
        </div>
        <div class="reply-actions">
          <el-button size="small" @click="cancelReply" :disabled="replySubmitting">取消</el-button>
          <el-button 
            type="primary" 
            size="small" 
            :disabled="!replyContent || replySubmitting" 
            @click="submitReply"
            :loading="replySubmitting"
          >
            回复
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 子评论列表 - 递归渲染 -->
    <div v-if="comment.children && comment.children.length > 0" 
         :class="{'comment-children': depth === 0, 'nested-children': depth > 0}">
      <component 
        :is="$options"
        v-for="childComment in comment.children" 
        :key="childComment.id"
        :comment="childComment"
        :default-avatar="defaultAvatar"
        :current-user-id="currentUserId"
        :depth="depth + 1"
        @reply="$emit('reply', $event)"
        @delete="$emit('delete', $event)"
        @like="$emit('like', $event)"
      />
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { StarFilled } from '@element-plus/icons-vue'

export default {
  name: 'CommentItem',
  components: { StarFilled },
  props: {
    // 评论数据
    comment: {
      type: Object,
      required: true
    },
    // 默认头像
    defaultAvatar: {
      type: String,
      default: ''
    },
    // 当前用户ID
    currentUserId: {
      type: Number,
      default: 0
    },
    // 嵌套深度，用于样式控制
    depth: {
      type: Number,
      default: 0
    }
  },
  emits: ['reply', 'delete', 'like'],
  setup(props, { emit }) {
    // 回复状态
    const replyingTo = ref(null)
    const replyContent = ref('')
    const replySubmitting = ref(false)
    const userAvatar = computed(() => localStorage.getItem('userAvatar') || '')
    
    // 检查是否为当前用户的评论
    const isCurrentUserComment = (comment) => {
      return props.currentUserId && comment.userId === props.currentUserId
    }
    
    // 处理回复点击
    const handleReply = (comment) => {
      replyingTo.value = comment.id
      replyContent.value = `@${comment.username || '匿名用户'} `
    }
    
    // 取消回复
    const cancelReply = () => {
      replyingTo.value = null
      replyContent.value = ''
    }
    
    // 提交回复
    const submitReply = () => {
      if (!replyContent.value.trim()) return
      
      replySubmitting.value = true
      
      // 将回复事件和内容传递给父组件处理
      emit('reply', {
        ...props.comment,
        replyContent: replyContent.value
      })
      
      // 回复后清空输入框
      cancelReply()
      replySubmitting.value = false
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    return {
      replyingTo,
      replyContent,
      replySubmitting,
      userAvatar,
      isCurrentUserComment,
      handleReply,
      cancelReply,
      submitReply,
      formatDate
    }
  }
}
</script>

<style scoped>
.comment-item {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  display: flex;
  gap: 16px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-author {
  font-weight: 500;
  font-size: 15px;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
  color: #333;
  word-break: break-word;
}

.comment-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.reply-input-container {
  display: flex;
  margin: 16px 0;
  gap: 12px;
}

.nested-comment .reply-input-container {
  margin-left: 40px;
}

.reply-input-wrapper {
  flex: 1;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  gap: 8px;
}

.reply-preview {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.preview-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.preview-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.comment-children {
  margin-left: 56px;
  margin-top: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
}

.nested-children {
  margin-left: 32px;
  margin-top: 16px;
  padding-left: 12px;
  border-left: 2px solid #ebeef5;
}

.nested-comment {
  margin-bottom: 16px;
  padding-bottom: 16px;
}

.nested-comment:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
}

/* 递归深度样式 - 逐层减轻颜色 */
.nested-comment .comment-author {
  font-size: 14px;
}

.nested-comment .comment-text {
  font-size: 13px;
}

@media (max-width: 768px) {
  .comment-children {
    margin-left: 20px;
    padding: 12px;
  }
  
  .nested-children {
    margin-left: 16px;
    padding-left: 8px;
  }
  
  .comment-content {
    gap: 10px;
  }
}
</style> 