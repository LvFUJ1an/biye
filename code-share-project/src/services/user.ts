import api from './api'

/**
 * 获取用户个人中心详细信息
 * @returns 用户详细信息，包括博客、视频、问题等
 */
export function getUserDetail() {
  return api.get('/blog/userDetail')
}

/**
 * 关注/取消关注用户
 * @param userId 要关注/取消关注的用户ID
 * @returns 关注状态，0表示关注成功，1表示取消关注成功
 */
export function follow(userId: number | string) {
  return api.post(`/follow/${userId}`)
}

export default {
  getUserDetail,
  follow
} 