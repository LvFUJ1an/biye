import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

// 消息提示
export const message = {
  /**
   * 成功消息提示
   * @param message 消息内容
   * @param duration 显示时间，默认2000ms
   */
  success(message: string, duration: number = 2000) {
    ElMessage({
      type: 'success',
      message,
      duration
    })
  },

  /**
   * 警告消息提示
   * @param message 消息内容
   * @param duration 显示时间，默认2000ms
   */
  warning(message: string, duration: number = 2000) {
    ElMessage({
      type: 'warning',
      message,
      duration
    })
  },

  /**
   * 错误消息提示
   * @param message 消息内容
   * @param duration 显示时间，默认2000ms
   */
  error(message: string, duration: number = 2000) {
    ElMessage({
      type: 'error',
      message,
      duration
    })
  },

  /**
   * 普通消息提示
   * @param message 消息内容
   * @param duration 显示时间，默认2000ms
   */
  info(message: string, duration: number = 2000) {
    ElMessage({
      type: 'info',
      message,
      duration
    })
  }
}

// 确认对话框
export const confirm = {
  /**
   * 确认对话框
   * @param title 标题
   * @param content 内容
   * @param type 类型：success/warning/info/error
   * @param confirmText 确认按钮文本
   * @param cancelText 取消按钮文本
   * @returns Promise
   */
  async show(
    content: string,
    title: string = '提示',
    type: 'success' | 'warning' | 'info' | 'error' = 'warning',
    confirmText: string = '确定',
    cancelText: string = '取消'
  ) {
    try {
      await ElMessageBox.confirm(content, title, {
        confirmButtonText: confirmText,
        cancelButtonText: cancelText,
        type
      })
      return true
    } catch (err) {
      return false
    }
  }
}

// 通知
export const notification = {
  /**
   * 成功通知
   * @param title 标题
   * @param message 消息内容
   * @param duration 显示时间，默认3000ms
   */
  success(title: string, message: string, duration: number = 3000) {
    ElNotification({
      title,
      message,
      type: 'success',
      duration
    })
  },

  /**
   * 警告通知
   * @param title 标题
   * @param message 消息内容
   * @param duration 显示时间，默认3000ms
   */
  warning(title: string, message: string, duration: number = 3000) {
    ElNotification({
      title,
      message,
      type: 'warning',
      duration
    })
  },

  /**
   * 错误通知
   * @param title 标题
   * @param message 消息内容
   * @param duration 显示时间，默认3000ms
   */
  error(title: string, message: string, duration: number = 3000) {
    ElNotification({
      title,
      message,
      type: 'error',
      duration
    })
  },

  /**
   * 普通通知
   * @param title 标题
   * @param message 消息内容
   * @param duration 显示时间，默认3000ms
   */
  info(title: string, message: string, duration: number = 3000) {
    ElNotification({
      title,
      message,
      type: 'info',
      duration
    })
  }
} 