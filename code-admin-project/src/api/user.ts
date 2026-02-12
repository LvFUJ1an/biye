import request from '@/utils/request'

// 登录接口
export function login(data: {
  username: string
  password: string
}) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 获取用户信息接口
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 登出接口
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 获取用户列表 - 管理员接口
export function getUserList(data: {
  keyword?: string,
  pageSize: number,
  currentPage: number
}) {
  return request({
    url: '/admin/user/list',
    method: 'post',
    data
  })
}

// 启用用户 - 管理员接口
export function enableUser(id: number) {
  return request({
    url: `/admin/user/enable/${id}`,
    method: 'put'
  })
}

// 禁用用户 - 管理员接口
export function disableUser(id: number) {
  return request({
    url: `/admin/user/disable/${id}`,
    method: 'put'
  })
}

// 重置用户密码 - 管理员接口
export function resetUserPassword(id: number) {
  return request({
    url: `/admin/user/resetPassword/${id}`,
    method: 'put'
  })
}

// 删除用户 - 管理员接口
export function deleteUser(id: number) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
} 