import request from '@/utils/request'

// 分页查询电子书列表
export function getEbookList(data: {
  current: number,
  size: number,
  keyword?: string
}) {
  return request({
    url: '/ebook/page',
    method: 'post',
    data
  })
}

// 根据ID获取电子书
export function getEbookById(id: number) {
  return request({
    url: `/ebook/${id}`,
    method: 'get'
  })
}

// 添加电子书
export function addEbook(data: FormData) {
  return request({
    url: '/ebook',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 创建电子书 (新接口)
export function createEbook(data: FormData) {
  return request({
    url: '/ebook/create',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 修改电子书 (新接口)
export function modifyEbook(data: FormData) {
  return request({
    url: '/ebook/modify',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新电子书
export function updateEbook(data: FormData) {
  return request({
    url: '/ebook',
    method: 'put',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除电子书
export function deleteEbook(id: number) {
  return request({
    url: `/ebook/delete/${id}`,
    method: 'delete'
  })
}

// 更新电子书状态（上下架）
export function updateEbookStatus(ebookId: number, isPublished: number) {
  return request({
    url: '/ebook/status',
    method: 'post',
    data: {
      ebookId,
      isPublished
    }
  })
} 