import axios from 'axios'
import { useRouter } from 'vue-router'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:18000', // 后端基础地址
  timeout: 15000, // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage中获取token
    const token = localStorage.getItem('admin-token')
    
    // 如果有token就在请求头中添加token
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 响应数据，直接返回data部分
    const res = response.data
    
    // 根据业务状态码处理
    if (res.code !== 0) {
      // 根据需要处理错误
      // 例如，token过期
      if (res.code === 401 || res.code === 403) {
        const router = useRouter()
        // 清除token
        localStorage.removeItem('admin-token')
        // 重定向到登录页
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '未知错误'))
    }
    
    return res
  },
  error => {
    // 响应错误处理
    console.error('响应错误:', error)
    
    // 处理网络错误、超时等
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      // 请求已发送，但服务器响应状态码不在 2xx 范围内
      const status = error.response.status
      
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          // 清除token并跳转到登录页
          localStorage.removeItem('admin-token')
          const router = useRouter()
          router.push('/login')
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `请求错误 (${status})`
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      message = '服务器无响应，请稍后重试'
    }
    
    return Promise.reject(new Error(message))
  }
)

export default request 