import request from '@/utils/request'

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
}

// 分页查询视频列表
export function getVideoList(data: {
  current: number,
  size: number,
  keyword?: string
}) {
  return request({
    url: '/video/page',
    method: 'post',
    data
  })
}

// 上传视频
export function createVideo(data: FormData) {
  return request({
    url: '/video/create',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新视频状态（上下架）
export function updateVideoStatus(videoId: number, isPublished: number) {
  return request({
    url: '/video/status',
    method: 'post',
    data: {
      videoId,
      isPublished
    }
  })
}

// 修改视频
export function modifyVideo(data: FormData) {
  return request({
    url: '/video/modify',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除视频
export function deleteVideo(id: number) {
  return request({
    url: `/video/delete/${id}`,
    method: 'delete'
  })
} 