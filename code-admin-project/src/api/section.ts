import request from '@/utils/request'

// 分页查询板块列表
export function getSectionList(data: {
  current: number,
  size: number,
  name?: string
}) {
  return request({
    url: '/sections/page',
    method: 'post',
    data
  })
}

// 根据ID获取板块
export function getSectionById(id: number) {
  return request({
    url: `/sections/${id}`,
    method: 'get'
  })
}

// 添加板块
export function addSection(data: {
  name: string,
  description: string
}) {
  return request({
    url: '/sections',
    method: 'post',
    data
  })
}

// 更新板块
export function updateSection(data: {
  id: number,
  name: string,
  description: string
  adminId?: number,
  createTime?: string
}) {
  return request({
    url: '/sections/modify',
    method: 'post',
    data
  })
}

// 删除板块
export function deleteSection(id: number) {
  return request({
    url: `/sections/${id}`,
    method: 'delete'
  })
} 