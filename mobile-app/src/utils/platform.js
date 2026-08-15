/**
 * 端差异适配层（.qoder/members/member-d/rules/mobile-structure.md）
 * 端差异代码只允许集中在本文件与条件编译块内，业务逻辑三端共享。
 */

/** 当前运行端：h5 / mp-weixin / app-plus */
export function getPlatform() {
	// #ifdef H5
	return "h5";
	// #endif
	// #ifdef MP-WEIXIN
	return "mp-weixin";
	// #endif
	// #ifdef APP-PLUS
	return "app-plus";
	// #endif
	return "unknown";
}

/**
 * 底部安全区占位高度（rpx）
 * - H5/App：CSS env(safe-area-inset-bottom) 由页面样式处理，返回 0
 * - 小程序：底部操作栏需要手动预留
 */
export function getSafeAreaExtra() {
	// #ifdef MP-WEIXIN
	const info = uni.getSystemInfoSync();
	return info.safeAreaInsets ? info.safeAreaInsets.bottom : 0;
	// #endif
	return 0;
}

/** 登录方式偏好：App/小程序主推短信验证码；H5 密码/验证码并重（D-01 差异项 1） */
export function preferredLoginMode() {
	const p = getPlatform();
	return p === "h5" ? "password" : "sms";
}

/** tabBar 徽标能力：H5 无原生徽标，返回 false 由页面内角标兜底 */
export function supportNativeBadge() {
	const p = getPlatform();
	return p === "mp-weixin" || p === "app-plus";
}
