<template>
	<view class="page-container">
		<!-- 购物车：受保护页面（未登录自动跳登录页，见 utils/auth.js PROTECTED_ROUTES） -->
		<view class="empty-tip" v-if="!isLogin">
			<view class="big">请先登录</view>
			<view>购物车需要登录后使用，正在跳转登录页…</view>
			<button class="login-btn" @click="goLogin">去登录</button>
		</view>
		<view class="empty-tip" v-else>
			<view class="big">D-P04 购物车</view>
			<view>勾选 / 改数量 / 去结算（X3 任务接入 U-008~U-011）</view>
		</view>
	</view>
</template>

<script>
import { isLoggedIn } from "../../utils/auth";

export default {
	data() {
		return {
			isLogin: false,
		};
	},
	onShow() {
		this.isLogin = isLoggedIn();
	},
	methods: {
		goLogin() {
			uni.reLaunch({ url: "/pages/login/login" });
		},
	},
};
</script>

<style lang="scss" scoped>
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
