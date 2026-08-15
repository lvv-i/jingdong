<template>
	<view class="page-container">
		<!-- 搜索入口（占位，X2 接入 P-004） -->
		<view class="search-bar" @click="goSearch">
			<text class="search-text">搜索商品（X2 接入）</text>
		</view>
		<!-- 首页占位：X2 接入类目树 P-003 + 热销商品 P-004 -->
		<view class="empty-tip">
			<view class="big">D-P01 首页</view>
			<view>类目入口 / 轮播 / 热销商品（X2 任务接入 P-003 / P-004）</view>
		</view>
	</view>
</template>

<script>
export default {
	methods: {
		goSearch() {
			uni.showToast({ title: "搜索页 X2 接入", icon: "none" });
		},
	},
};
</script>

<style lang="scss" scoped>
.search-bar {
	margin: 20rpx;
	padding: 0 24rpx;
	height: 72rpx;
	line-height: 72rpx;
	background: #fff;
	border-radius: 36rpx;
	border: 2rpx solid #c81623;
	.search-text {
		color: #999;
		font-size: 26rpx;
	}
}
.empty-tip {
	.big {
		font-size: 32rpx;
		color: #c81623;
		margin-bottom: 12rpx;
	}
}
</style>
