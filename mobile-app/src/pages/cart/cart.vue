<template>
	<view class="cart-page">
		<!-- 未登录：受保护页兜底（authGuardMixin 会跳登录，此处兜底展示） -->
		<view class="empty-box" v-if="!isLogin">
			<view class="big">请先登录</view>
			<view class="sub">购物车需要登录后使用</view>
			<button class="login-btn" @click="goLogin">去登录</button>
		</view>

		<template v-else>
			<!-- 加载失败：点击重试 -->
			<view class="empty-box" v-if="loadFailed">
				<view class="big">加载失败</view>
				<view class="sub">请确认网络或后端服务后重试</view>
				<button class="login-btn" @click="loadCart">重新加载</button>
			</view>

			<!-- 空购物车 -->
			<view class="empty-box" v-else-if="!list.length">
				<view class="big">购物车还是空的</view>
				<view class="sub">去挑选心仪的商品吧</view>
				<button class="login-btn" @click="goHome">去逛逛</button>
			</view>

			<template v-else>
				<!-- U-008 购物车列表 -->
				<view class="cart-list">
					<view class="cart-item" v-for="item in list" :key="item.id">
						<view class="check" :class="{ on: item.selected === 1 }" @click="toggleSelect(item)">
							<text v-if="item.selected === 1" class="check-icon">✓</text>
						</view>
						<image class="thumb" :src="PLACEHOLDER" mode="aspectFill" />
						<view class="info">
							<view class="title">{{ item.title }}</view>
							<view class="meta">
								<text class="price">¥{{ formatPrice(item.price) }}</text>
								<text class="stock" v-if="item.stock === 0">暂时缺货</text>
								<text class="stock" v-else-if="item.quantity >= item.stock">仅剩{{ item.stock }}件</text>
							</view>
							<view class="ops">
								<!-- U-010 数量步进（上限 999 与库存） -->
								<view class="stepper">
									<view class="step-btn" :class="{ disabled: item.quantity <= 1 || item.updating }" @click="changeQuantity(item, -1)">−</view>
									<view class="step-num">{{ item.quantity }}</view>
									<view class="step-btn" :class="{ disabled: item.quantity >= 999 || item.quantity >= item.stock || item.updating }" @click="changeQuantity(item, 1)">＋</view>
								</view>
								<!-- U-011 删除（二次确认） -->
								<view class="del" @click="removeItem(item)">删除</view>
							</view>
						</view>
					</view>
				</view>

				<!-- 底部结算栏：全选 / 合计 / 去结算 -->
				<view class="settle-bar">
					<view class="all" @click="toggleAll">
						<view class="check" :class="{ on: allSelected }">
							<text v-if="allSelected" class="check-icon">✓</text>
						</view>
						<text>全选</text>
					</view>
					<view class="total">
						合计：<text class="total-price">¥{{ formatPrice(totalPrice) }}</text>
					</view>
					<view class="checkout-btn" :class="{ disabled: !selectedCount }" @click="goCheckout">
						去结算{{ selectedCount ? `(${selectedCount})` : "" }}
					</view>
				</view>
			</template>
		</template>
	</view>
</template>

<script>
import { isLoggedIn } from "../../utils/auth";
import { getCartItems, updateCartItem, deleteCartItem } from "../../api/cart";
import { formatPrice } from "../../utils/format";

const PLACEHOLDER = "/static/logo.png";
// 勾选商品 id 传递结算页的 storage key
const CHECKOUT_KEY = "jd_checkout_ids";

export default {
	data() {
		return {
			isLogin: false,
			list: [],
			loadFailed: false,
			PLACEHOLDER,
		};
	},
	computed: {
		selectedCount() {
			return this.list.filter((i) => i.selected === 1).length;
		},
		totalPrice() {
			return this.list
				.filter((i) => i.selected === 1)
				.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0);
		},
		allSelected() {
			return this.list.length > 0 && this.list.every((i) => i.selected === 1);
		},
	},
	onShow() {
		this.isLogin = isLoggedIn();
		if (this.isLogin) {
			this.loadCart();
		}
	},
	methods: {
		formatPrice,
		async loadCart() {
			this.loadFailed = false;
			try {
				const data = await getCartItems(true);
				// U-008 T5 契约：data 为 {list, total}（后端已对齐）
				this.list = (data && data.list) || [];
			} catch (e) {
				this.list = [];
				this.loadFailed = true;
			}
		},
		// U-010 勾选/取消勾选
		async toggleSelect(item) {
			if (item.updating) return;
			item.updating = true;
			const target = item.selected === 1 ? 0 : 1;
			try {
				await updateCartItem(item.id, { selected: target });
				item.selected = target;
			} catch (e) {
				// 失败提示由请求层 toast，本地不变
			} finally {
				item.updating = false;
			}
		},
		// U-010 修改数量（本地步进，后端校验 1-999）
		async changeQuantity(item, delta) {
			const q = item.quantity + delta;
			if (q < 1 || q > 999) return;
			if (q > item.stock) {
				uni.showToast({ title: `库存不足，仅剩 ${item.stock} 件`, icon: "none" });
				return;
			}
			if (item.updating) return;
			item.updating = true;
			try {
				await updateCartItem(item.id, { quantity: q });
				item.quantity = q;
			} catch (e) {
				// 失败提示由请求层 toast
			} finally {
				item.updating = false;
			}
		},
		// U-011 删除（二次确认）
		removeItem(item) {
			uni.showModal({
				title: "删除商品",
				content: `确定将「${item.title}」移出购物车吗？`,
				confirmColor: "#C81623",
				success: async (res) => {
					if (!res.confirm) return;
					try {
						await deleteCartItem(item.id);
						this.list = this.list.filter((i) => i.id !== item.id);
						uni.showToast({ title: "已删除", icon: "none" });
					} catch (e) {
						// 失败提示由请求层 toast
					}
				},
			});
		},
		// 全选 / 取消全选（批量调用 U-010）
		async toggleAll() {
			if (!this.list.length) return;
			const target = this.allSelected ? 0 : 1;
			this.list.forEach((i) => (i.selected = target)); // 乐观更新
			try {
				await Promise.all(
					this.list.map((i) => updateCartItem(i.id, { selected: target }))
				);
			} catch (e) {
				this.loadCart(); // 失败回滚：重载列表
			}
		},
		// 去结算：勾选项 id 写入 storage，跳结算页
		goCheckout() {
			if (!this.selectedCount) {
				uni.showToast({ title: "请先勾选要结算的商品", icon: "none" });
				return;
			}
			const ids = this.list.filter((i) => i.selected === 1).map((i) => i.id);
			uni.setStorageSync(CHECKOUT_KEY, ids);
			uni.navigateTo({ url: "/pages/checkout/checkout" });
		},
		goLogin() {
			uni.reLaunch({ url: "/pages/login/login" });
		},
		goHome() {
			uni.switchTab({ url: "/pages/index/index" });
		},
	},
};
</script>

<style lang="scss" scoped>
.cart-page {
	min-height: 100vh;
	background: #f5f5f5;
}

.empty-box {
	padding: 200rpx 40rpx;
	text-align: center;
	color: #999;
	.big {
		font-size: 32rpx;
		color: #333;
		margin-bottom: 16rpx;
	}
	.sub {
		font-size: 26rpx;
		margin-bottom: 40rpx;
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

.cart-list {
	padding: 20rpx 20rpx 160rpx;
}
.cart-item {
	display: flex;
	align-items: center;
	background: #fff;
	border-radius: 16rpx;
	padding: 24rpx 20rpx;
	margin-bottom: 20rpx;
	.check {
		width: 40rpx;
		height: 40rpx;
		border: 2rpx solid #ccc;
		border-radius: 50%;
		margin-right: 20rpx;
		flex-shrink: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		&.on {
			background: #c81623;
			border-color: #c81623;
		}
	}
	.check-icon {
		color: #fff;
		font-size: 26rpx;
		line-height: 1;
	}
	.thumb {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		background: #f7f7f7;
		flex-shrink: 0;
	}
	.info {
		flex: 1;
		margin-left: 20rpx;
		min-width: 0;
		.title {
			font-size: 28rpx;
			color: #333;
			line-height: 1.4;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			overflow: hidden;
		}
		.meta {
			margin-top: 12rpx;
			display: flex;
			align-items: baseline;
			.price {
				color: #c81623;
				font-size: 30rpx;
				font-weight: bold;
			}
			.stock {
				margin-left: 16rpx;
				font-size: 22rpx;
				color: #999;
			}
		}
		.ops {
			margin-top: 16rpx;
			display: flex;
			align-items: center;
			justify-content: space-between;
			.stepper {
				display: flex;
				align-items: center;
				.step-btn {
					width: 56rpx;
					height: 56rpx;
					background: #f5f5f5;
					border-radius: 8rpx;
					text-align: center;
					line-height: 56rpx;
					font-size: 32rpx;
					color: #333;
					&.disabled {
						color: #ccc;
					}
				}
				.step-num {
					min-width: 72rpx;
					text-align: center;
					font-size: 28rpx;
					color: #333;
				}
			}
			.del {
				font-size: 24rpx;
				color: #999;
				padding: 8rpx 16rpx;
			}
		}
	}
}

.settle-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	/* #ifdef H5 */
	bottom: 50px; // H5 端 tabBar 高度
	/* #endif */
	background: #fff;
	display: flex;
	align-items: center;
	padding: 16rpx 20rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
	.all {
		display: flex;
		align-items: center;
		font-size: 26rpx;
		color: #333;
		.check {
			width: 40rpx;
			height: 40rpx;
			border: 2rpx solid #ccc;
			border-radius: 50%;
			margin-right: 12rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			&.on {
				background: #c81623;
				border-color: #c81623;
			}
		}
		.check-icon {
			color: #fff;
			font-size: 26rpx;
			line-height: 1;
		}
	}
	.total {
		flex: 1;
		text-align: right;
		font-size: 26rpx;
		color: #333;
		margin-right: 20rpx;
		.total-price {
			color: #c81623;
			font-size: 34rpx;
			font-weight: bold;
		}
	}
	.checkout-btn {
		background: #c81623;
		color: #fff;
		font-size: 28rpx;
		padding: 20rpx 44rpx;
		border-radius: 40rpx;
		&.disabled {
			background: #ccc;
		}
	}
}
</style>
