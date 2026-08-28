// 站内通知接口（U-022/U-023/U-025，需登录）
import request from './request'

/**
 * U-022 通知列表（readStatus 过滤：0未读/1已读，空=全部）
 * @param {Object} params {page, pageSize, readStatus?}
 * @returns data.list: {id, title, content, readStatus, createdAt} + data.total
 */
export function getNotifications(params, config) {
  return request.get('/notifications', { params, ...config })
}

/** U-023 标记单条已读（校验接收者本人） */
export function markRead(id, config) {
  return request.put(`/notifications/${id}/read`, null, config)
}

/** U-025 全部通知置已读（B 增补） */
export function markAllRead(config) {
  return request.put('/notifications/read-all', null, config)
}
