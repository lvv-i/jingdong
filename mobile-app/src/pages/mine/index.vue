<template>
	<view class="page-container">
		<!-- 我的：登录态展示（X5 接入 U-001/U-002） -->
		<view v-if="isLogin" class="user-card">
			<view class="avatar">{{ initial }}</view>
			<view class="info">
				<view class="name">{{ username }}</view>
				<view class="role">角色：{{ roleText }}</view>
			</view>
			<button class="logout-btn" @click="onLogout">退出登录</button>
		</view>
		<view v-else class="empty-tip">
			<view class="big">D-P07 我的</view>
			<view>未登录状态（X5 接入个人资料 / 地址 / 售后入口）</view>
			<button class="login-btn" @click="goLogin">去登录</button>
		</view>
	</view>
</template>

<script>
import { useUserStore } from "../../stores/user";

export default {
	computed: {
		isLogin() {
			return this.store.isLoggedIn;
		},
		username() {
			return this.store.username || "移动端用户";
		},
		roleText() {
			return this.store.role === "USER" ? "普通用户" : this.store.role;
		},
		initial() {
			return (this.username || "U").slice(0, 1).toUpperCase();
		},
	},
	data() {
		return {
			store: useUserStore(),
		};
	},
	methods: {
		onLogout() {
			this.store.logout();
			uni.showToast({ title: "已退出登录", icon: "none" });
			setTimeout(() => uni.switchTab({ url: "/pages/index/index" }), 600);
		},
		goLogin() {
			uni.reLaunch({ url: "/pages/login/login" });
		},
	},
};
</script>

<style lang="scss" scoped>
.user-card {
	display: flex;
	align-items: center;
	padding: 40rpx 32rpx;
	background: #c81623;
	.avatar {
		width: 100rpx;
		height: 100rpx;
		line-height: 100rpx;
		border-radius: 50%;
		background: #fff;
		color: #c81623;
		font-size: 44rpx;
		font-weight: bold;
		text-align: center;
	}
	.info {
		flex: 1;
		margin-left: 24rpx;
		color: #fff;
		.name {
			font-size: 34rpx;
			font-weight: bold;
		}
		.role {
			font-size: 24rpx;
			opacity: 0.85;
			margin-top: 8rpx;
		}
	}
	.logout-btn {
		flex-shrink: 0;
		font-size: 26rpx;
		color: #fff;
		background: rgba(255, 255, 255, 0.2);
		border-radius: 32rpx;
		padding: 0 32rpx;
		height: 64rpx;
		line-height: 64rpx;
		&::after {
			border: none;
		}
	}
}
.empty-tip {
	.big {
		font-size: 32rpx;
		color: #c81623;
		margin-bottom: 12rpx;
	}
}
.login-btn {
	margin: 40rpx auto 0;
	width: 320rpx;
	background: #c81623;
	color: #fff;
	border-radius: 40rpx;
	font-size: 28rpx;
	&::after {
		border: none;
	}
}
</style>
