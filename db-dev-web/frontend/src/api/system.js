import request from './request'

export const getSystemInfo = () => {
  return request({
    url: '/system/info',
    method: 'get'
  })
}

