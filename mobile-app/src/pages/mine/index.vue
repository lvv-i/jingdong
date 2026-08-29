<template>
	<view class="mine-page">
		<!-- 未登录 -->
		<view class="no-login" v-if="!isLogin">
			<view class="avatar-placeholder">👤</view>
			<view class="no-login-text">登录后享受更多权益</view>
			<button class="login-btn" @click="goLogin">去登录</button>
		</view>

		<template v-else>
			<!-- 用户卡片（U-001 驱动） -->
			<view class="user-card" @click="goProfile">
				<view class="avatar">{{ initial }}</view>
				<view class="info">
					<view class="name">{{ username }}</view>
					<view class="role">角色：{{ roleText }}</view>
				</view>
				<view class="arrow">›</view>
			</view>

			<!-- 功能入口宫格 -->
			<view class="menu-section">
				<view class="section-title">我的订单</view>
				<view class="menu-grid">
					<view class="menu-item" @click="goOrders('')">
						<view class="icon">📋</view>
						<view class="label">全部订单</view>
					</view>
					<view class="menu-item" @click="goOrders('PENDING_PAY')">
						<view class="icon">💳</view>
						<view class="label">待支付</view>
					</view>
					<view class="menu-item" @click="goOrders('SHIPPED')">
						<view class="icon">📦</view>
						<view class="label">待收货</view>
					</view>
					<view class="menu-item" @click="goRefunds">
						<view class="icon">🔄</view>
						<view class="label">售后/退款</view>
					</view>
				</view>
			</view>

			<view class="menu-section">
				<view class="section-title">我的服务</view>
				<view class="menu-list">
					<view class="menu-row" @click="goAddress">
						<view class="row-left">📍 收货地址</view>
						<view class="row-right">›</view>
					</view>
					<view class="menu-row" @click="goNotices">
						<view class="row-left">🔔 消息通知</view>
						<view class="row-right">›</view>
					</view>
					<view class="menu-row" @click="goProfile">
						<view class="row-left">✏️ 个人资料</view>
						<view class="row-right">›</view>
					</view>
				</view>
			</view>

			<!-- 退出登录 -->
			<view class="logout-area">
				<button class="logout-btn" @click="onLogout">退出登录</button>
			</view>
		</template>
	</view>
</template>

<script>
import { useUserStore } from "../../stores/user";

export default {
	data() {
		return {
			store: useUserStore(),
		};
	},
	computed: {
		isLogin() { return this.store.isLoggedIn; },
		username() { return this.store.username || "移动端用户"; },
		roleText() { return this.store.role === "USER" ? "普通用户" : this.store.role; },
		initial() { return (this.username || "U").slice(0, 1).toUpperCase(); },
	},
	onShow() {
		// 刷新 profile
		if (this.isLogin) {
			this.store.fetchProfile().catch(() => {});
		}
	},
	methods: {
		goLogin() { uni.reLaunch({ url: "/pages/login/login" }); },
		goOrders(status) {
			uni.navigateTo({ url: `/pages/order/list?status=${encodeURIComponent(status)}` });
		},
		goRefunds() { uni.navigateTo({ url: "/pages/refund/list" }); },
		goAddress() { uni.navigateTo({ url: "/pages/mine/address" }); },
		goNotices() { uni.navigateTo({ url: "/pages/notice/list" }); },
		goProfile() { uni.navigateTo({ url: "/pages/mine/profile" }); },
		onLogout() {
			this.store.logout();
			uni.showToast({ title: "已退出登录", icon: "none" });
			setTimeout(() => uni.switchTab({ url: "/pages/index/index" }), 600);
		},
	},
};
</script>

<style scoped lang="scss">
.mine-page { min-height: 100vh; background: #f5f5f5; }
.no-login {
	display: flex; flex-direction: column; align-items: center; padding-top: 160rpx;
	.avatar-placeholder { font-size: 96rpx; margin-bottom: 24rpx; }
	.no-login-text { font-size: 28rpx; color: #999; margin-bottom: 40rpx; }
	.login-btn { background: #C81623; color: #fff; border: none; border-radius: 40rpx; padding: 16rpx 80rpx; font-size: 28rpx; }
}
.user-card {
	display: flex; align-items: center; padding: 48rpx 32rpx;
	background: linear-gradient(135deg, #C81623, #E0343A);
	.avatar { width: 100rpx; height: 100rpx; line-height: 100rpx; border-radius: 50%; background: rgba(255,255,255,0.3); color: #fff; font-size: 44rpx; font-weight: bold; text-align: center; }
	.info { flex: 1; margin-left: 24rpx; color: #fff;
		.name { font-size: 36rpx; font-weight: bold; }
		.role { font-size: 24rpx; opacity: 0.85; margin-top: 8rpx; }
	}
	.arrow { font-size: 40rpx; color: rgba(255,255,255,0.6); }
}
.menu-section {
	background: #fff; margin: 20rpx 24rpx; border-radius: 12rpx; padding: 24rpx;
	.section-title { font-size: 28rpx; color: #999; margin-bottom: 16rpx; }
}
.menu-grid { display: flex; flex-wrap: wrap;
	.menu-item { width: 25%; display: flex; flex-direction: column; align-items: center; padding: 16rpx 0;
		.icon { font-size: 44rpx; margin-bottom: 8rpx; }
		.label { font-size: 24rpx; color: #333; }
	}
}
.menu-list { .menu-row { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #f5f5f5; &:last-child { border-bottom: none; }
	.row-left { font-size: 28rpx; color: #333; }
	.row-right { font-size: 32rpx; color: #ccc; }
}}
.logout-area { padding: 48rpx 32rpx; .logout-btn { background: transparent; color: #999; border: 1rpx solid #ddd; border-radius: 8rpx; font-size: 28rpx; width: 100%; padding: 20rpx; }}
</style>