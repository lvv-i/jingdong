/**
 * 请求层封装（T5 通用约定 + .qoder/members/member-d/skills/mobile-api-integration.md）
 * - baseURL：联调环境 http://localhost:8080（D-02 约定）
 * - 统一返回格式 {code, message, data, total}：仅 code=200 成功
 * - 请求拦截：注入 Authorization: Bearer <token>（白名单接口 P-001~P-008 无 token 不注入）
 * - 响应拦截：1002 跳登录；其他业务错误 toast message 并 reject
 */
import { getToken, goLogin } from "./auth";

// 联调环境（D-02 约定）；支持环境变量 VITE_API_BASE 覆盖
// 开发：默认 localhost:8080；生产/局域网：设置 VITE_API_BASE=http://192.168.x.x:8080
const BASE_URL = import.meta.env.VITE_API_BASE || "http://localhost:8080";

// T5 白名单接口（无需登录）：P-001~P-008
const WHITELIST = [
	"/api/users/register",
	"/api/users/login",
	"/api/users/sms-code",
	"/api/users/login/sms",
	"/api/categories",
	"/api/products",
];

function isWhitelist(url) {
	const path = url.split("?")[0];
	if (WHITELIST.includes(path)) return true;
	// 商品详情/评价为 GET 公共接口
	if (/^\/api\/products\/\d+(\/reviews)?$/.test(path)) return true;
	return false;
}

/**
 * 通用请求
 * @param {Object} options { url, method='GET', data, header, silent } 
 *   silent=true：列表/展示类静默降级（无后端时不 toast，页面自行兜底空状态）
 * @returns {Promise<any>} 成功时 resolve data（已解包）
 */
export function request(options) {
	const { url, method = "GET", data, header = {}, silent = false } = options || {};
	return new Promise((resolve, reject) => {
		const headers = { "Content-Type": "application/json", ...header };
		const token = getToken();
		if (token && !isWhitelist(url)) {
			headers.Authorization = `Bearer ${token}`;
		}
		uni.request({
			url: BASE_URL + url,
			method,
			data,
			header: headers,
			timeout: 10000,
			success: (res) => {
				const body = res.data || {};
				if (body.code === 200) {
					resolve(body.data);
					return;
				}
				// 1002 未登录：清 token 跳登录（T3 错误码）
				if (body.code === 1002) {
					goLogin();
					reject(body);
					return;
				}
				if (!silent) {
					uni.showToast({ title: body.message || "操作失败", icon: "none" });
				}
				reject(body);
			},
			fail: (err) => {
				if (!silent) {
					uni.showToast({ title: "网络异常，请确认后端已启动", icon: "none" });
				}
				reject(err);
			},
		});
	});
}

export const get = (url, params, silent) =>
	request({ url: buildQuery(url, params), method: "GET", silent });

export const post = (url, data, silent) => request({ url, method: "POST", data, silent });

export const put = (url, data, silent) => request({ url, method: "PUT", data, silent });

export const del = (url, silent) => request({ url, method: "DELETE", silent });

/** 拼接 query 参数（分页 page/pageSize 等） */
function buildQuery(url, params) {
	if (!params) return url;
	const qs = Object.keys(params)
		.filter((k) => params[k] !== undefined && params[k] !== null && params[k] !== "")
		.map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
		.join("&");
	return qs ? `${url}?${qs}` : url;
}
