/**
 * 消息通知接口模块（T5 U-022/U-023/U-025）
 */
import { get, put } from "../utils/request";

/**
 * U-022 通知列表
 * @param {Object} params {page, pageSize, readStatus} readStatus 0=未读 1=已读，不传=全部
 * @returns {Promise<{list:[{id,title,content,readStatus,createdAt}], total}>}
 */
export const getNotifications = (params, silent) => get("/api/notifications", params, silent);

/**
 * U-023 标记单条已读
 * 错误码：1004 通知不存在
 */
export const markRead = (id) => put(`/api/notifications/${id}/read`);

/**
 * U-025 全部已读
 */
export const markAllRead = () => put("/api/notifications/read-all");