<template>
	<view class="checkout-page">
		<!-- 下单成功：按商家拆单展示，逐单去支付 -->
		<view class="success-box" v-if="orderSuccess">
			<view class="s-icon">✓</view>
			<view class="s-title">下单成功，请尽快完成支付</view>
			<view class="order-card" v-for="o in orders" :key="o.orderId">
				<view class="o-line">
					<text class="o-label">订单号</text>
					<text class="o-no">{{ o.orderNo }}</text>
				</view>
				<view class="o-line">
					<text class="o-label">应付金额</text>
					<text class="o-price">¥{{ formatPrice(o.payAmount) }}</text>
				</view>
				<view class="o-actions">
					<button class="pay-btn" @click="goPay(o.orderId)">去支付</button>
				</view>
			</view>
			<view class="s-tip">多笔订单需分别支付；取消订单请在支付页操作</view>
			<button class="home-btn" @click="goHome">返回首页</button>
		</view>

		<template v-else>
			<!-- 加载失败 -->
			<view class="empty-box" v-if="loadFailed">
				<view class="big">加载失败</view>
				<view class="sub">请确认网络或后端服务后重试</view>
				<button class="retry-btn" @click="loadCheckout">重新加载</button>
			</view>

			<!-- 结算商品失效（已被删除/下单） -->
			<view class="empty-box" v-else-if="loaded && !items.length">
				<view class="big">结算商品已失效</view>
				<view class="sub">商品可能已被删除或已下单，请返回购物车重新选择</view>
				<button class="retry-btn" @click="goBack">返回购物车</button>
			</view>

			<template v-else>
				<!-- U-003 收货地址选择 -->
				<view class="section addr-section">
					<view class="section-title">收货地址</view>
					<template v-if="addresses.length">
						<view class="addr-item" v-for="a in addresses" :key="a.id" @click="selectedAddressId = a.id">
							<view class="check" :class="{ on: a.id === selectedAddressId }">
								<text v-if="a.id === selectedAddressId" class="check-icon">✓</text>
							</view>
							<view class="addr-info">
								<view class="addr-line1">
									<text class="receiver">{{ a.receiver }}</text>
									<text class="phone">{{ a.phone }}</text>
									<text class="default-tag" v-if="a.isDefault === 1">默认</text>
								</view>
								<view class="addr-line2">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</view>
							</view>
						</view>
					</template>
					<view class="addr-empty" v-else>
						<text>暂无收货地址</text>
						<button class="add-addr-btn" @click="goMine">去「我的」添加地址</button>
					</view>
				</view>

				<!-- 商品清单（来自勾选的购物车项） -->
				<view class="section">
					<view class="section-title">商品清单</view>
					<view class="goods-item" v-for="item in items" :key="item.id">
						<image class="thumb" :src="PLACEHOLDER" mode="aspectFill" />
						<view class="g-info">
							<view class="g-title">{{ item.title }}</view>
							<view class="g-meta">¥{{ formatPrice(item.price) }} × {{ item.quantity }}</view>
						</view>
					</view>
					<view class="amount-line">
						<text>共 {{ totalCount }} 件</text>
						<text>合计：<text class="amount">¥{{ formatPrice(totalPrice) }}</text></text>
					</view>
				</view>

				<!-- 订单备注 -->
				<view class="section remark-section">
					<view class="section-title">订单备注</view>
					<input class="remark-input" v-model="remark" placeholder="选填，给商家留言（50 字以内）" maxlength="50" />
				</view>

				<!-- 底部提交栏 -->
				<view class="submit-bar">
					<view class="sb-total">
						实付：<text class="sb-price">¥{{ formatPrice(totalPrice) }}</text>
					</view>
					<button class="submit-btn" :class="{ disabled: !selectedAddress }" :loading="submitting" @click="submitOrder">
						提交订单
					</button>
				</view>
			</template>
		</template>
	</view>
</template>

<script>
import { getAddresses } from "../../api/address";
import { getCartItems } from "../../api/cart";
import { createOrder } from "../../api/order";
import { formatPrice } from "../../utils/format";

const PLACEHOLDER = "/static/logo.png";
const CHECKOUT_KEY = "jd_checkout_ids";

export default {
	data() {
		return {
			ids: [],
			addresses: [],
			selectedAddressId: null,
			items: [],
			remark: "",
			loaded: false,
			loadFailed: false,
			submitting: false,
			orderSuccess: false,
			orders: [],
			PLACEHOLDER,
		};
	},
	computed: {
		selectedAddress() {
			return this.addresses.find((a) => a.id === this.selectedAddressId) || null;
		},
		totalCount() {
			return this.items.reduce((s, i) => s + i.quantity, 0);
		},
		totalPrice() {
			return this.items.reduce((s, i) => s + Number(i.price) * i.quantity, 0);
		},
	},
	onLoad() {
		this.ids = uni.getStorageSync(CHECKOUT_KEY) || [];
		if (!this.ids.length) {
			uni.showToast({ title: "请先勾选要结算的商品", icon: "none" });
			setTimeout(() => uni.navigateBack(), 600);
			return;
		}
		this.loadCheckout();
	},
	methods: {
		formatPrice,
		async loadCheckout() {
			this.loadFailed = false;
			try {
				const [addrData, cartData] = await Promise.all([
					getAddresses(true),
					getCartItems(true),
				]);
				// 双兼容：旧 jar 裸数组 / 新后端 PageResult{list,total}（A 772ddec 已改回 T5 契约）
				this.addresses = Array.isArray(addrData) ? addrData : (addrData && addrData.list) || [];
				const list = Array.isArray(cartData) ? cartData : (cartData && cartData.list) || [];
				this.items = list.filter((i) => this.ids.includes(i.id));
				// 默认地址优先，否则第一条
				const def = this.addresses.find((a) => a.isDefault === 1);
				this.selectedAddressId = (def || this.addresses[0] || {}).id || null;
				this.loaded = true;
			} catch (e) {
				this.loadFailed = true;
			}
		},
		// U-012 提交订单（按商家拆单）
		async submitOrder() {
			if (!this.selectedAddress) {
				uni.showToast({ title: "请先选择收货地址", icon: "none" });
				return;
			}
			if (this.submitting) return;
			this.submitting = true;
			try {
				const data = await createOrder({
					addressId: this.selectedAddress.id,
					cartItemIds: this.items.map((i) => i.id),
					remark: this.remark || undefined,
				});
				this.orders = data.orders || [];
				this.orderSuccess = true;
				uni.removeStorageSync(CHECKOUT_KEY); // 下单成功后清空勾选传递
			} catch (e) {
				// 提示由请求层 toast；按错误码做后续动作（与后端 U-012 实际错误码对齐）
				// 后端实际：4003 购物车项失效 / 4004 购物车为空 / 4005 地址无效 /
				//          4006 库存不足 / 3001 商品不存在 / 3002 商品已下架 / 4007 价格变化(pay)
				const code = e && e.code;
				if (code === 4004) {
					// 购物车为空：返回购物车刷新
					setTimeout(() => uni.navigateBack(), 800);
				} else if (code === 4005) {
					// 地址无效：刷新结算页（重新拉取地址与商品清单）
					this.loadCheckout();
				} else if ([3001, 3002, 4003, 4006, 4007].includes(code)) {
					// 商品失效/下架/库存不足/价格变化：刷新结算清单
					this.loadCheckout();
				}
			} finally {
				this.submitting = false;
			}
		},
		goPay(orderId) {
			uni.navigateTo({ url: `/pages/order/pay?id=${orderId}` });
		},
		goHome() {
			uni.switchTab({ url: "/pages/index/index" });
		},
		goMine() {
			uni.switchTab({ url: "/pages/mine/index" });
		},
		goBack() {
			uni.navigateBack();
		},
	},
};
</script>

<style lang="scss" scoped>
.checkout-page {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 160rpx;
}

/* 下单成功 */
.success-box {
	padding: 80rpx 40rpx;
	text-align: center;
	.s-icon {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		background: #e1251b;
		color: #fff;
		font-size: 64rpx;
		line-height: 120rpx;
		margin: 0 auto 32rpx;
	}
	.s-title {
		font-size: 34rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 40rpx;
	}
	.order-card {
		background: #fff;
		border-radius: 16rpx;
		padding: 24rpx;
		margin-bottom: 24rpx;
		text-align: left;
		.o-line {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 12rpx;
			.o-label {
				font-size: 26rpx;
				color: #999;
			}
			.o-no {
				font-size: 26rpx;
				color: #333;
			}
			.o-price {
				font-size: 32rpx;
				color: #c81623;
				font-weight: bold;
			}
		}
		.o-actions {
			text-align: right;
			margin-top: 16rpx;
			.pay-btn {
				display: inline-block;
				background: #c81623;
				color: #fff;
				font-size: 26rpx;
				padding: 0 40rpx;
				border-radius: 32rpx;
				line-height: 60rpx;
				height: 60rpx;
				&::after {
					border: none;
				}
			}
		}
	}
	.s-tip {
		font-size: 24rpx;
		color: #999;
		margin: 24rpx 0;
	}
	.home-btn {
		background: #fff;
		color: #333;
		font-size: 28rpx;
		border-radius: 40rpx;
		width: 320rpx;
		margin-top: 16rpx;
		&::after {
			border: 1rpx solid #ddd;
		}
	}
}

/* 空态/失败 */
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
.retry-btn {
	width: 320rpx;
	margin: 0 auto;
	background: #c81623;
	color: #fff;
	border-radius: 40rpx;
	font-size: 28rpx;
	&::after {
		border: none;
	}
}

/* 区块 */
.section {
	background: #fff;
	border-radius: 16rpx;
	margin: 20rpx;
	padding: 24rpx;
	.section-title {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 20rpx;
	}
}

/* 地址 */
.addr-item {
	display: flex;
	align-items: flex-start;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
	&:last-child {
		border-bottom: none;
	}
	.check {
		width: 40rpx;
		height: 40rpx;
		border: 2rpx solid #ccc;
		border-radius: 50%;
		margin-right: 20rpx;
		margin-top: 4rpx;
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
	.addr-info {
		flex: 1;
		min-width: 0;
		.addr-line1 {
			display: flex;
			align-items: center;
			.receiver {
				font-size: 30rpx;
				color: #333;
				font-weight: bold;
			}
			.phone {
				font-size: 26rpx;
				color: #666;
				margin-left: 20rpx;
			}
			.default-tag {
				margin-left: 16rpx;
				font-size: 20rpx;
				color: #c81623;
				border: 1rpx solid #c81623;
				border-radius: 6rpx;
				padding: 2rpx 8rpx;
			}
		}
		.addr-line2 {
			margin-top: 8rpx;
			font-size: 26rpx;
			color: #666;
			line-height: 1.5;
		}
	}
}
.addr-empty {
	padding: 24rpx 0;
	text-align: center;
	font-size: 26rpx;
	color: #999;
	.add-addr-btn {
		margin-top: 24rpx;
		display: inline-block;
		background: #c81623;
		color: #fff;
		font-size: 26rpx;
		line-height: 64rpx;
		height: 64rpx;
		padding: 0 40rpx;
		border-radius: 32rpx;
		&::after {
			border: none;
		}
	}
}

/* 商品清单 */
.goods-item {
	display: flex;
	align-items: center;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
	&:last-child {
		border-bottom: none;
	}
	.thumb {
		width: 120rpx;
		height: 120rpx;
		border-radius: 12rpx;
		background: #f7f7f7;
		flex-shrink: 0;
	}
	.g-info {
		flex: 1;
		margin-left: 20rpx;
		min-width: 0;
		.g-title {
			font-size: 26rpx;
			color: #333;
			line-height: 1.4;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			overflow: hidden;
		}
		.g-meta {
			margin-top: 10rpx;
			font-size: 26rpx;
			color: #666;
		}
	}
}
.amount-line {
	margin-top: 20rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 26rpx;
	color: #333;
	.amount {
		color: #c81623;
		font-size: 34rpx;
		font-weight: bold;
	}
}

/* 备注 */
.remark-input {
	background: #f7f7f7;
	border-radius: 12rpx;
	padding: 16rpx 20rpx;
	font-size: 26rpx;
	height: 64rpx;
}

/* 提交栏 */
.submit-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
	.sb-total {
		font-size: 26rpx;
		color: #333;
		.sb-price {
			color: #c81623;
			font-size: 36rpx;
			font-weight: bold;
		}
	}
	.submit-btn {
		background: #c81623;
		color: #fff;
		font-size: 28rpx;
		line-height: 72rpx;
		height: 72rpx;
		padding: 0 56rpx;
		border-radius: 36rpx;
		&.disabled {
			background: #ccc;
		}
		&::after {
			border: none;
		}
	}
}
</style>
