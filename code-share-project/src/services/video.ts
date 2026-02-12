import api from './api'

// 视频接口类型定义
export interface Video {
  id: number
  title: string
  description: string
  videoPath: string
  coverPath: string
  userId: number
  isPublished: number
  createTime: string
  updateTime: string
  downloadCount: number | null
  viewCount: number
  userName: string
  avatarUrl: string
}

// 视频列表查询参数
export interface VideoListParams {
  current: number | string
  size: number | string
  keyword?: string
  categoryId?: number | string
  excludeId?: number | string
}

// 视频列表返回结果
export interface VideoPageResult {
  records: Video[]
  total: number
  size: number
  current: number
  pages: number
}

// 视频相关服务
export default {
  /**
   * 获取视频分页列表
   * @param params 查询参数
   */
  getVideoList(params: VideoListParams) {
    return api.post<VideoPageResult>('/video/page', params)
  },

  /**
   * 获取视频详情
   * @param id 视频ID
   */
  getVideoDetail(id: number | string) {
    return api.post(`/video/getVideoById/${id}`)
  },

  /**
   * 创建新视频
   * @param formData 表单数据
   */
  createVideo(formData: FormData) {
    return api.post('/video/create', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 修改视频
   * @param formData 表单数据
   */
  updateVideo(formData: FormData) {
    return api.post('/video/modify', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除视频
   * @param id 视频ID
   */
  deleteVideo(id: number | string) {
    return api.post(`/video/delete/${id}`)
  },

  /**
   * 观看视频（增加播放量）
   * @param id 视频ID
   */
  viewVideo(id: number | string) {
    return api.post(`/video/view/${id}`)
  },

  /**
   * 下载视频（增加下载量）
   * @param id 视频ID
   */
  downloadVideo(id: number | string) {
    return api.post(`/video/download/${id}`)
  }
} 