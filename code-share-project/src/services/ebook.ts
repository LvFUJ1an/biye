import api from './api'

// 电子书接口类型定义
export interface Ebook {
  id: number
  title: string
  description: string
  coverUrl: string
  filePath: string
  categoryId: number
  adminId: number
  adminName: string
  downloadCount: number
  viewCount: number
  createTime: string
  updateTime: string
  isNew?: boolean
  catalog?: any[]
}

export interface EbookListParams {
  current: string
  size: string
  keyword?: string
  categoryId?: number
  orderBy?: string
  orderDirection?: string
}

export interface EbookPageResult {
  records: Ebook[]
  total: number
  size: number
  current: number
  pages: number
}

export interface EbookCommentParams {
  ebookId: number
  content: string
  score: number
}

export interface EbookReportParams {
  ebookId: number
  reason: string
  description: string
}

// 电子书相关服务
export default {
  /**
   * 获取电子书分页列表
   * @param params 查询参数
   */
  getEbookList(params: EbookListParams) {
    return api.post<EbookPageResult>('/ebook/page', params)
  },

  /**
   * 获取电子书详情
   * @param id 电子书ID
   */
  getEbookDetail(id: number) {
    return api.get<Ebook>(`/ebook/getEbookById/${id}`)
  },

  /**
   * 记录电子书浏览
   * @param id 电子书ID
   */
  viewEbook(id: number) {
    return api.post(`/ebook/view/${id}`)
  },

  /**
   * 下载电子书
   * @param id 电子书ID
   */
  downloadEbook(id: number) {
    return api.post(`/ebook/download/${id}`)
  },

  /**
   * 获取相关电子书推荐
   * @param params 查询参数
   */
  getRelatedEbooks(params: { id: number, categoryId: number, limit?: number }) {
    return api.post<Ebook[]>('/ebook/related', params)
  },

  /**
   * 获取电子书评论
   * @param ebookId 电子书ID
   */
  getEbookComments(ebookId: number) {
    return api.get(`/ebook/comments/${ebookId}`)
  },

  /**
   * 评论电子书
   * @param params 评论参数
   */
  commentEbook(params: EbookCommentParams) {
    return api.post('/ebook/comment', params)
  },

  /**
   * 举报电子书
   * @param params 举报参数
   */
  reportEbook(params: EbookReportParams) {
    return api.post('/ebook/report', params)
  }
} 