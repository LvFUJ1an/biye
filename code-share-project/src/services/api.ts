import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

// 定义基础URL
const baseURL = 'http://localhost:18000';

// 创建axios实例
const instance: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 定义响应数据接口
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    
    // 如果token存在，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('API请求添加token:', config.url);
    } else {
      console.log('API请求无token:', config.url);
    }
    
    return config;
  },
  (error) => {
    console.error('请求拦截器捕获错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;
    
    // 如果code不等于0，说明API接口返回了错误
    if (res.code !== 0) {
      // 处理特定的错误码
      if (res.code === 401) {
        // 未授权 - 但不立即清除用户信息，而是提示用户登录状态可能失效
        console.warn('登录状态可能已过期，但不立即清除用户信息 - API路径:', response.config.url);
        ElMessage({
          message: '登录状态已过期，请重新登录',
          type: 'warning'
        });
        
        // 检查当前路由是否需要登录权限，如果需要则重定向到登录页
        const needAuth = router.currentRoute.value.meta.requiresAuth;
        if (needAuth) {
          router.push('/login');
        }
        
        // 不直接清除本地存储，让用户可以选择重新登录
        return Promise.reject(new Error(res.message || '登录状态已过期'));
      }
      
      // 其他错误处理
      ElMessage({
        message: res.message || '请求出错',
        type: 'error',
        duration: 5000
      });
      
      return Promise.reject(new Error(res.message || '请求出错'));
    }
    
    return res;
  },
  (error) => {
    console.error('API响应错误:', error);
    
    // 获取错误信息
    let message = '连接服务器失败';
    
    if (error.response) {
      const { status } = error.response;
      
      // 根据状态码处理不同错误
      switch (status) {
        case 400:
          message = '错误的请求';
          break;
        case 401:
          message = '未授权，请重新登录';
          
          // 只有在确认是服务器返回的401状态码时，提示用户但不强制登出
          console.warn('服务器返回401未授权，但不立即清除用户信息');
          
          // 检查当前路由是否需要登录权限
          const needAuth = router.currentRoute.value.meta.requiresAuth;
          if (needAuth) {
            router.push('/login');
          }
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        case 502:
          message = '网关错误';
          break;
        case 503:
          message = '服务不可用';
          break;
        case 504:
          message = '网关超时';
          break;
        default:
          message = `未知错误(${status})`;
      }
    } else if (error.request) {
      // 请求发送但没有收到响应
      message = '服务器无响应';
    } else {
      // 请求配置出错
      message = '请求配置错误';
    }
    
    // 显示错误信息
    ElMessage({
      message,
      type: 'error',
      duration: 5000
    });
    
    return Promise.reject(error);
  }
);

// 封装GET请求
export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return instance.get(url, { params, ...config });
}

// 封装POST请求
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  console.log(`API POST请求: ${url}, 数据:`, data);
  return instance.post(url, data, config);
}

// 封装PUT请求
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return instance.put(url, data, config);
}

// 封装DELETE请求
export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return instance.delete(url, config);
}

// 创建一个上传文件的辅助函数
export function uploadFile<T = any>(url: string, file: File, onProgress?: (percentage: number) => void): Promise<ApiResponse<T>> {
  const formData = new FormData();
  formData.append('file', file);
  
  return instance.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: progressEvent => {
      if (progressEvent.total && onProgress) {
        const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress(percentage);
      }
    }
  });
}

// 导出API实例和请求方法
export default {
  instance,
  get,
  post,
  put,
  del,
  uploadFile
}; 