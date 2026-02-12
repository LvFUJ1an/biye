import api from './api'

// 问答接口类型定义
export interface Question {
  id: number
  title: string
  content: string
  categoryId: number
  tags: string[]
  userId: number
  username: string
  avatarUrl: string
  viewCount: number
  answerCount: number
  createTime: string
  updateTime: string
  isRecommend: boolean
  isSolved: boolean
}

export interface QuestionListParams {
  current: string
  size: string
  keyword?: string
  categoryId?: number
  tagId?: number
  orderBy?: string
  orderDirection?: string
}

export interface QuestionPageResult {
  records: Question[]
  total: number
  size: number
  current: number
  pages: number
}

export interface Answer {
  id: number
  questionId: number
  content: string
  userId: number
  username: string
  avatarUrl: string
  createTime: string
  updateTime: string
  isAccepted: boolean
  likeCount: number
  commentCount: number
}

export interface AnswerListParams {
  questionId: number
  current: string
  size: string
  orderBy?: string
}

export interface AnswerPageResult {
  records: Answer[]
  total: number
  size: number
  current: number
  pages: number
}

// 问答相关服务
export default {
  /**
   * 获取问题分页列表
   * @param params 查询参数
   */
  getQuestionList(params: QuestionListParams) {
    return api.post<QuestionPageResult>('/questions/page', params)
  },

  /**
   * 获取问题详情
   * @param id 问题ID
   */
  getQuestionDetail(id: number) {
    return api.post<Question>(`/questions/getQuestionById/${id}`)
  },

  /**
   * 发布新问题
   * @param params 问题内容
   */
  createQuestion(params: {
    title: string
    content: string
    categoryId: number
    tags: string[]
  }) {
    return api.post('/questions/create', params)
  },

  /**
   * 更新问题
   * @param id 问题ID
   * @param params 更新内容
   */
  updateQuestion(id: number, params: {
    title?: string
    content?: string
    categoryId?: number
    tags?: string[]
  }) {
    return api.put(`/questions/update/${id}`, params)
  },

  /**
   * 删除问题
   * @param id 问题ID
   */
  deleteQuestion(id: number | string) {
    return api.del(`/questions/delete/${id}`)
  },

  /**
   * 获取问题回答列表
   * @param params 查询参数
   */
  getAnswerList(params: AnswerListParams) {
    return api.post<AnswerPageResult>('/questions/answers/page', params)
  },

  /**
   * 回答问题
   * @param params 回答内容
   */
  createAnswer(params: {
    questionId: number
    content: string
  }) {
    return api.post('/questions/answer', params)
  },

  /**
   * 采纳回答
   * @param answerId 回答ID
   */
  acceptAnswer(answerId: number) {
    return api.post(`/questions/acceptAnswer/${answerId}`)
  },

  /**
   * 点赞回答
   * @param answerId 回答ID
   */
  likeAnswer(answerId: number) {
    return api.post(`/questions/answers/like/${answerId}`)
  },

  /**
   * 修改问题
   * @param params 修改内容
   */
  modifyQuestion(params: {
    id: number | string,
    title: string,
    content: string
  }) {
    return api.post('/questions/modify', params)
  },

  /**
   * 设置问题为已解决
   * @param id 问题ID
   */
  solveQuestion(id: number | string) {
    return api.post(`/questions/solve/${id}`)
  }
} 