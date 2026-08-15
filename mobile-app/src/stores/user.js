/**
 * Pinia user store（T4 JWT 方案：token 24h + userInfo 持久化）
 * - token/userInfo 与 utils/auth.js 双向同步，刷新页面登录态不丢
 */
import { defineStore } from "pinia";
import * as userApi from "../api/user";
import {
	getToken,
	setToken,
	clearToken,
	getUserInfo,
	setUserInfo,
	clearUserInfo,
} from "../utils/auth";

export const useUserStore = defineStore("user", {
	state: () => ({
		token: getToken(),
		userInfo: getUserInfo(),
	}),
	getters: {
		isLoggedIn: (state) => !!state.token,
		username: (state) => (state.userInfo && state.userInfo.username) || "",
		role: (state) => (state.userInfo && state.userInfo.role) || "USER",
	},
	actions: {
		/** 持久化登录结果（P-002/P-008 共用） */
		_setLoginResult(data) {
			this.token = data.token;
			this.userInfo = data.userInfo || null;
			setToken(data.token);
			setUserInfo(data.userInfo || null);
		},
		// P-002 账号密码登录
		async loginByPassword(username, password) {
			const data = await userApi.login({ username, password });
			this._setLoginResult(data);
			return data;
		},
		// P-007 发送验证码（60 秒限频后端控制，前端倒计时防抖）
		async sendSmsCode(phone) {
			return userApi.sendSmsCode(phone);
		},
		// P-008 验证码登录（demo 固定码 123456；未注册手机号自动注册）
		async loginBySms(phone, smsCode) {
			const data = await userApi.smsLogin({ phone, smsCode });
			this._setLoginResult(data);
			return data;
		},
		// P-001 注册（仅创建账号，不自动登录）
		async register(form) {
			return userApi.register(form);
		},
		// U-001 拉取个人资料（X5 使用）
		async fetchProfile() {
			const data = await userApi.getProfile();
			this.userInfo = data;
			setUserInfo(data);
			return data;
		},
		logout() {
			this.token = "";
			this.userInfo = null;
			clearToken();
			clearUserInfo();
		},
	},
});
