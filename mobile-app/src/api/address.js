/**
 * 收货地址接口模块（T5 U-003~U-007）
 */
import { get, post, put, del } from "../utils/request";

// U-003 地址列表：T5 v1.1 后端返回 {list,total}；兼容历史裸数组形态，统一归一化为数组
export const getAddresses = (silent) =>
	get("/api/addresses", null, silent).then((d) =>
		Array.isArray(d) ? d : (d && d.list) || []
	);

/**
 * U-004 新增地址
 * @param {Object} data {receiver, phone, province, city, district, detail, isDefault 0/1}
 * @returns {Promise<{id}>}
 * 错误码：1001/2006（超 20 个）
 */
export const addAddress = (data) => post("/api/addresses", data);

/**
 * U-005 修改地址
 * 错误码：1001/2005（校验归属本人）
 */
export const updateAddress = (id, data) => put(`/api/addresses/${id}`, data);

/**
 * U-006 删除地址（软删除）
 * 错误码：2005
 */
export const deleteAddress = (id) => del(`/api/addresses/${id}`);

/**
 * U-007 设置默认地址
 * 错误码：2005
 */
export const setDefaultAddress = (id) => put(`/api/addresses/${id}/default`);
