import request from './request'

/**
 * 登录
 */
export function login(username, password) {
  return request.post('/auth/login', {
    username,
    password
  })
}

/**
 * 登出
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 验证Token
 */
export function validateToken() {
  return request.get('/auth/validate')
}

