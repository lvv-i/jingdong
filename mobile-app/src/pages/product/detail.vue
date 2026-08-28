<template>
	<!-- D-P03 商品详情：P-005 详情 + 加购 U-009 + P-006 评价列表 -->
	<view class="detail-page">
		<!-- 3001 商品不存在 / 3002 已下架 -->
		<view v-if="errorCode" class="error-block">
			<text class="err-icon">⚠️</text>
			<text class="err-text">{{ errorMsg }}</text>
			<view class="err-actions">
				<button size="mini" class="btn-back" @click="goBack">返回</button>
				<button size="mini" class="btn-home" @click="goHome">回首页</button>
			</view>
		</view>

		<block v-else-if="product.id">
			<!-- 主图轮播（mainImage + images 多图） -->
			<swiper class="img-swiper" indicator-dots circular>
				<swiper-item v-for="(img, i) in bannerImages" :key="i">
					<image class="banner-img" :src="img" mode="aspectFill" @error="onBannerError" />
				</swiper-item>
			</swiper>

			<!-- 标题价格 -->
			<view class="info-panel">
				<view class="price-row">
					<text class="symbol">¥</text>
					<text class="price-num">{{ formatPrice(product.price) }}</text>
					<text v-if="product.originalPrice" class="price-original">
						¥{{ formatPrice(product.originalPrice) }}
					</text>
				</view>
				<view class="detail-title">{{ product.title }}</view>
				<view v-if="product.subTitle" class="detail-subtitle">{{ product.subTitle }}</view>
				<view class="meta-row">
					<text class="meta-item">销量 {{ formatSales(product.salesCount) }}</text>
					<text class="meta-item">库存 {{ product.stock }} 件</text>
					<text class="meta-item">{{ product.merchantName || "" }}</text>
				</view>
			</view>

			<!-- 商品详情富文本 -->
			<view class="panel">
				<view class="panel-title">商品详情</view>
				<rich-text v-if="product.detail" class="detail-rich" :nodes="product.detail" />
				<text v-else class="detail-empty">暂无详情</text>
			</view>

			<!-- 评价列表（P-006，userName 已脱敏） -->
			<view class="panel">
				<view class="panel-title">商品评价（{{ reviewTotal }}）</view>
				<view v-if="reviews.length" class="review-list">
					<view v-for="(r, i) in reviews" :key="i" class="review-item">
						<view class="ri-head">
							<text class="ri-user">{{ r.userName }}</text>
							<text class="ri-stars">{{ starText(r.rating) }}</text>
						</view>
						<text class="ri-comment">{{ r.comment }}</text>
						<text class="ri-time">{{ formatDateTime(r.reviewedAt) }}</text>
					</view>
				</view>
				<view v-else-if="!reviewLoading" class="empty-tip">暂无评价</view>
				<view v-if="reviewLoading" class="load-tip">评价加载中…</view>
				<view
					v-else-if="reviews.length < reviewTotal"
					class="load-more"
					@click="loadReviews(false)"
				>
					加载更多评价
				</view>
			</view>

			<!-- 底部操作栏：加购（未登录引导登录） -->
			<view class="bottom-bar">
				<button
					class="add-cart-btn"
					:loading="adding"
					:disabled="adding || product.stock === 0"
					@click="handleAddCart"
				>
					{{ product.stock === 0 ? "暂时缺货" : "加入购物车" }}
				</button>
			</view>
		</block>

		<!-- 加载中 -->
		<view v-else-if="loading" class="load-tip">加载中…</view>
	</view>
</template>

<script>
import { getProductDetail, getProductReviews, addCartItem } from "../../api/product";
import { isLoggedIn } from "../../utils/auth";
import { formatPrice, formatSales, formatDateTime } from "../../utils/format";

const PLACEHOLDER = "/static/logo.png";

export default {
	data() {
		return {
			id: null,
			loading: true,
			errorCode: 0,
			errorMsg: "",
			product: {},
			bannerImages: [PLACEHOLDER],
			reviews: [],
			reviewLoading: false,
			reviewPage: 1,
			reviewTotal: 0,
			adding: false,
		};
	},
	onLoad(options) {
		this.id = Number(options.id);
		this.loadDetail();
		this.loadReviews(true);
	},
	methods: {
		formatPrice,
		formatSales,
		formatDateTime,
		starText(rating) {
			return "★".repeat(rating || 0) + "☆".repeat(5 - (rating || 0));
		},
		/** P-005 商品详情：3001 不存在 / 3002 已下架 */
		async loadDetail() {
			this.loading = true;
			try {
				const data = await getProductDetail(this.id, true);
				this.product = data || {};
				const imgs = [data?.mainImage, ...(data?.images || [])].filter(Boolean);
				this.bannerImages = imgs.length ? imgs : [PLACEHOLDER];
			} catch (err) {
				this.product = {};
				if (err?.code === 3002) {
					this.errorCode = 3002;
					this.errorMsg = "商品已下架";
				} else if (err?.code === 3001) {
					this.errorCode = 3001;
					this.errorMsg = "商品不存在";
				} else {
					this.errorCode = -1;
					this.errorMsg = "商品加载失败，请稍后重试";
				}
			} finally {
				this.loading = false;
			}
		},
		/** P-006 评价列表（分页） */
		async loadReviews(reset) {
			if (this.reviewLoading) return;
			this.reviewLoading = true;
			try {
				const params = { page: reset ? 1 : this.reviewPage, pageSize: 5 };
				const data = await getProductReviews(this.id, params, true);
				const list = data?.list || [];
				this.reviewTotal = data?.total || 0;
				this.reviews = reset ? list : this.reviews.concat(list);
				this.reviewPage += 1;
			} catch {
				if (reset) this.reviews = [];
				this.reviewTotal = 0;
			} finally {
				this.reviewLoading = false;
			}
		},
		/** U-009 加购：未登录引导登录；3002 提示；成功后 toast */
		async handleAddCart() {
			if (!isLoggedIn()) {
				uni.showToast({ title: "请先登录", icon: "none" });
				setTimeout(() => {
					uni.navigateTo({ url: "/pages/login/login" });
				}, 600);
				return;
			}
			if (this.adding) return;
			this.adding = true;
			try {
				await addCartItem({ productId: this.id, quantity: 1 });
				uni.showToast({ title: "已加入购物车", icon: "success" });
			} catch (err) {
				// 业务错误（3002 等）request 层已 toast
				console.error("addCart fail", err);
			} finally {
				this.adding = false;
			}
		},
		onBannerError(e) {
			// 单图失败时替换为占位图
			const idx = e?.currentTarget?.dataset?.index;
			if (idx !== undefined) {
				this.$set(this.bannerImages, idx, PLACEHOLDER);
			}
		},
		goBack() {
			const pages = getCurrentPages();
			if (pages.length > 1) {
				uni.navigateBack();
			} else {
				uni.switchTab({ url: "/pages/index/index" });
			}
		},
		goHome() {
			uni.switchTab({ url: "/pages/index/index" });
		},
	},
};
</script>

<style lang="scss" scoped>
.detail-page {
	padding-bottom: 140rpx;
}
.error-block {
	padding: 160rpx 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 24rpx;

	.err-icon {
		font-size: 80rpx;
	}
	.err-text {
		color: #666;
		font-size: 30rpx;
	}
	.err-actions {
		display: flex;
		gap: 24rpx;

		.btn-back {
			background: #fff;
			color: #333;
		}
		.btn-home {
			background: #c81623;
			color: #fff;
		}
	}
}
.img-swiper {
	height: 600rpx;

	.banner-img {
		width: 100%;
		height: 600rpx;
		background: #f5f5f5;
	}
}
.info-panel {
	background: #fff;
	padding: 24rpx;
	margin-bottom: 16rpx;

	.price-row {
		display: flex;
		align-items: baseline;
		margin-bottom: 12rpx;

		.symbol {
			color: #c81623;
			font-size: 30rpx;
		}
		.price-num {
			color: #c81623;
			font-size: 52rpx;
			font-weight: 700;
		}
		.price-original {
			margin-left: 12rpx;
			font-size: 26rpx;
			color: #999;
			text-decoration: line-through;
		}
	}
	.detail-title {
		font-size: 32rpx;
		font-weight: 700;
		color: #333;
		line-height: 1.4;
	}
	.detail-subtitle {
		margin-top: 8rpx;
		font-size: 26rpx;
		color: #999;
	}
	.meta-row {
		display: flex;
		gap: 32rpx;
		margin-top: 16rpx;

		.meta-item {
			font-size: 24rpx;
			color: #666;
		}
	}
}
.panel {
	background: #fff;
	padding: 24rpx;
	margin-bottom: 16rpx;

	.panel-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #333;
		border-left: 8rpx solid #c81623;
		padding-left: 16rpx;
		margin-bottom: 20rpx;
	}
	.detail-rich {
		font-size: 28rpx;
		color: #444;
		line-height: 1.7;
	}
	.detail-empty {
		color: #999;
		font-size: 26rpx;
	}
}
.review-list {
	.review-item {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f0f0f0;

		.ri-head {
			display: flex;
			justify-content: space-between;
			margin-bottom: 8rpx;

			.ri-user {
				font-size: 26rpx;
				color: #333;
				font-weight: 600;
			}
			.ri-stars {
				font-size: 24rpx;
				color: #ff9900;
			}
		}
		.ri-comment {
			display: block;
			font-size: 26rpx;
			color: #444;
			line-height: 1.6;
		}
		.ri-time {
			display: block;
			margin-top: 8rpx;
			font-size: 22rpx;
			color: #999;
		}
	}
}
.empty-tip {
	padding: 40rpx 0;
	text-align: center;
	color: #999;
	font-size: 26rpx;
}
.load-tip {
	padding: 60rpx 0;
	text-align: center;
	color: #999;
	font-size: 26rpx;
}
.load-more {
	text-align: center;
	color: #c81623;
	font-size: 26rpx;
	padding: 20rpx 0;
}
.bottom-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	padding: 16rpx 24rpx;
	// 底部安全区适配（platform.js 约定：小程序手动预留）
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);

	.add-cart-btn {
		background: #c81623;
		color: #fff;
		border-radius: 44rpx;
		font-size: 30rpx;

		&[disabled] {
			background: #ccc;
			color: #fff;
		}
	}
}
</style>
