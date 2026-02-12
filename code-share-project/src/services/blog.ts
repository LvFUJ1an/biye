import api from './api'

/**
 * 获取博客列表
 * @param params 查询参数
 * @returns 博客列表数据
 */
export function getBlogList(params?: any) {
  return api.post('/blog/list', params)
}

/**
 * 获取博客详情
 * @param id 博客ID
 * @returns 博客详情数据
 */
export function getBlogDetail(id: string | number) {
  return api.get(`/blog/${id}`)
}

/**
 * 创建博客
 * @param data 博客数据
 * @returns 创建结果
 */
export function createBlog(data: any) {
  return api.post('/blog/create', data)
}

/**
 * 更新博客
 * @param data 博客数据（包含id）
 * @returns 更新结果
 */
export function updateBlog(data: any) {
  return api.post(`/blog/update`, data)
}

/**
 * 删除博客
 * @param id 博客ID
 * @returns 删除结果
 */
export function deleteBlog(id: string | number) {
  return api.del(`/blog/${id}`)
}

/**
 * 获取博客排行榜数据（热门文章和热门作者）
 * @returns 排行榜数据，包含热门文章和热门作者
 */
export function getBlogRank() {
  return api.post('/blog/rank')
}

/**
 * 获取博客历史版本
 * @param id 博客ID
 * @returns 博客历史版本列表
 */
export function getBlogHistoryVersions(id: string | number) {
  return api.post(`/blog/getHistoryBlogVersion/${id}`)
}

/**
 * 还原博客历史版本
 * @param id 历史博客版本ID
 * @returns 还原结果
 */
export function restoreBlogVersion(id: string | number) {
  return api.post(`/blog/restore/${id}`)
}

export default {
  getBlogList,
  getBlogDetail,
  createBlog,
  updateBlog,
  deleteBlog,
  getBlogRank,
  getBlogHistoryVersions,
  restoreBlogVersion
} 