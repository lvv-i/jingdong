<template>
	<!-- 商品卡片（首页/分类页/搜索结果共用）：懒加载 + 加载失败占位 -->
	<view class="product-card" @click="goDetail">
		<image
			class="pc-img"
			:src="imgSrc"
			mode="aspectFill"
			lazy-load
			@error="onImgError"
		/>
		<view class="pc-body">
			<view class="pc-title">{{ product.title }}</view>
			<view class="pc-bottom">
				<view class="pc-price">
					<text class="symbol">¥</text>
					<text class="num">{{ formatPrice(product.price) }}</text>
					<text v-if="product.originalPrice" class="original">
						¥{{ formatPrice(product.originalPrice) }}
					</text>
				</view>
				<view class="pc-sales">{{ formatSales(product.salesCount) }} 人付款</view>
			</view>
		</view>
	</view>
</template>

<script>
import { formatPrice, formatSales } from "../../utils/format";

// 商品主图加载失败占位（本地 1x1 透明底 + 文字由样式呈现）
const PLACEHOLDER = "/static/logo.png";

export default {
	name: "ProductCard",
	props: {
		product: { type: Object, required: true },
	},
	data() {
		return { imgSrc: this.product.mainImage || PLACEHOLDER };
	},
	methods: {
		formatPrice,
		formatSales,
		onImgError() {
			if (this.imgSrc !== PLACEHOLDER) this.imgSrc = PLACEHOLDER;
		},
		goDetail() {
			uni.navigateTo({ url: `/pages/product/detail?id=${this.product.id}` });
		},
	},
};
</script>

<style lang="scss" scoped>
.product-card {
	background: #fff;
	border-radius: 12rpx;
	overflow: hidden;
	margin-bottom: 16rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

	.pc-img {
		width: 100%;
		height: 340rpx;
		background: #f5f5f5;
		display: block;
	}
	.pc-body {
		padding: 16rpx;
	}
	.pc-title {
		font-size: 28rpx;
		color: #333;
		line-height: 1.4;
		height: 78rpx;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		margin-bottom: 12rpx;
	}
	.pc-bottom {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.pc-price {
		display: flex;
		align-items: baseline;
		.symbol {
			color: #c81623;
			font-size: 24rpx;
		}
		.num {
			color: #c81623;
			font-size: 36rpx;
			font-weight: 700;
		}
		.original {
			margin-left: 8rpx;
			font-size: 22rpx;
			color: #999;
			text-decoration: line-through;
		}
	}
	.pc-sales {
		font-size: 22rpx;
		color: #999;
	}
}
</style>
