/**
 * 登录态工具（D-02 约定 + T4 JWT 方案）
 * - token 存储 key 统一 TOKEN_KEY
 * - 受保护页面清单 PROTECTED_ROUTES：未登录自动跳登录页（code=1002 响应兜底）
 */
const TOKEN_KEY = "jd_shop_token";
const USER_KEY = "jd_shop_user";

export function getToken() {
	return uni.getStorageSync(TOKEN_KEY) || "";
}

export function setToken(token) {
	uni.setStorageSync(TOKEN_KEY, token);
}

export function clearToken() {
	uni.removeStorageSync(TOKEN_KEY);
}

export function getUserInfo() {
	const raw = uni.getStorageSync(USER_KEY);
	if (!raw) return null;
	// H5 端 getStorageSync 返回字符串，需手动解析；小程序/App 端返回对象
	if (typeof raw === "string") {
		try {
			return JSON.parse(raw);
		} catch (e) {
			return null;
		}
	}
	return raw;
}

export function setUserInfo(userInfo) {
	uni.setStorageSync(USER_KEY, userInfo);
}

export function clearUserInfo() {
	uni.removeStorageSync(USER_KEY);
}

export function isLoggedIn() {
	return !!getToken();
}

export function logout() {
	clearToken();
	clearUserInfo();
}

/**
 * 受保护页面清单：访问这些页面必须登录（未登录跳 /pages/login/login）
 * X1：购物车；X3：结算/支付；X4 追加：订单/售后/地址/消息
 */
const PROTECTED_ROUTES = [
	"pages/cart/cart",
	"pages/checkout/checkout",
	"pages/order/pay",
];

export const authGuardMixin = {
	onShow() {
		const pages = getCurrentPages();
		if (!pages.length) return;
		const current = pages[pages.length - 1];
		const route = current && current.route;
		if (route && PROTECTED_ROUTES.includes(route) && !isLoggedIn()) {
			uni.showToast({ title: "请先登录", icon: "none" });
			setTimeout(() => {
				uni.reLaunch({ url: "/pages/login/login" });
			}, 600);
		}
	},
};

/** 跳转登录页（request.js 拦截 1002 时调用，token 失效强制重登） */
export function goLogin() {
	uni.showToast({ title: "登录已失效，请重新登录", icon: "none" });
	setTimeout(() => {
		uni.reLaunch({ url: "/pages/login/login" });
	}, 600);
}
