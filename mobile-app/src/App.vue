<script>
export default {
	onLaunch() {
		// 应用启动：登录态持久化在 stores/user 初始化时从本地恢复（无需额外动作）
		console.log("[mobile-app] App Launch");
	},
};
</script>

<style lang="scss">
/* 全局基础样式 */
page {
	background-color: #f5f5f5;
	font-size: 28rpx;
	color: #333;
	height: 100%;
}

/* 京冬红主色工具类 */
.text-primary {
	color: #c81623;
}
.bg-primary {
	background-color: #c81623;
}

/* 通用布局 */
.page-container {
	min-height: 100vh;
	box-sizing: border-box;
}

/* 空状态 */
.empty-tip {
	padding: 120rpx 0;
	text-align: center;
	color: #999;
	font-size: 26rpx;
}
</style>
