import { createSSRApp } from "vue";
import * as Pinia from "pinia";
import App from "./App.vue";
import { authGuardMixin } from "./utils/auth";

export function createApp() {
	const app = createSSRApp(App);
	app.use(Pinia.createPinia());
	// 全局登录守卫：受保护页面未登录自动跳登录页（utils/auth.js 维护白名单）
	app.mixin(authGuardMixin);
	return {
		app,
		Pinia,
	};
}
