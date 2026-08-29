<template>
	<view class="pay-page">
		<!-- 支付成功态（U-015：展示 paymentNo 流水号） -->
		<view class="pay-success" v-if="paySuccess">
			<view class="s-icon">✓</view>
			<view class="s-title">支付成功</view>
			<view class="s-no">
				<text class="s-label">支付流水号</text>
				<text class="s-value">{{ paymentNo }}</text>
			</view>
			<view class="s-amount">
				实付金额：<text class="s-price">¥{{ formatPrice(detail.payAmount) }}</text>
			</view>
			<button class="s-btn" @click="goHome">返回首页</button>
		</view>

		<template v-else>
			<!-- 加载失败 -->
			<view class="empty-box" v-if="loadFailed">
				<view class="big">加载失败</view>
				<view class="sub">请确认网络或后端服务后重试</view>
				<button class="retry-btn" @click="loadDetail">重新加载</button>
			</view>

			<template v-else-if="detail">
				<!-- 状态卡 -->
				<view class="status-card">
					<view class="status-name">{{ statusText }}</view>
					<view class="status-desc">{{ statusDesc }}</view>
				</view>

				<!-- 收货信息快照 -->
				<view class="section" v-if="addrParts.length">
					<view class="section-title">收货信息</view>
					<view class="addr-line">
						<text class="addr-receiver">{{ addrParts[0] }}</text>
						<text class="addr-phone">{{ addrParts[1] || "" }}</text>
					</view>
					<view class="addr-detail">{{ addrParts[2] || detail.receiverSnapshot }}</view>
				</view>

				<!-- 商品明细（快照） -->
				<view class="section">
					<view class="section-title">商品明细</view>
					<view class="goods-item" v-for="item in detail.items" :key="item.id">
						<view class="g-title">{{ item.titleSnapshot }}</view>
						<view class="g-meta">
							<text>¥{{ formatPrice(item.priceSnapshot) }} × {{ item.quantity }}</text>
							<text class="g-subtotal">¥{{ formatPrice(item.totalPrice) }}</text>
						</view>
					</view>
				</view>

				<!-- 金额 -->
				<view class="section">
					<view class="amount-row">
						<text>商品总额</text>
						<text>¥{{ formatPrice(detail.totalAmount) }}</text>
					</view>
					<view class="amount-row">
						<text>运费</text>
						<text>¥0.00</text>
					</view>
					<view class="amount-row total">
						<text>实付金额</text>
						<text class="pay-amount">¥{{ formatPrice(detail.payAmount) }}</text>
					</view>
				</view>

				<!-- 订单信息 -->
				<view class="section">
					<view class="section-title">订单信息</view>
					<view class="info-row">
						<text class="i-label">订单号</text>
						<text class="i-value">{{ detail.orderNo }}</text>
					</view>
					<view class="info-row">
						<text class="i-label">下单时间</text>
						<text class="i-value">{{ formatDateTime(detail.createdAt) }}</text>
					</view>
					<view class="info-row" v-if="detail.paidAt">
						<text class="i-label">支付时间</text>
						<text class="i-value">{{ formatDateTime(detail.paidAt) }}</text>
					</view>
					<view class="info-row" v-if="detail.shippingNo">
						<text class="i-label">运单号</text>
						<text class="i-value">{{ detail.shippingNo }}</text>
					</view>
				</view>

				<!-- 底部操作栏：仅待支付可操作 -->
				<view class="action-bar" v-if="detail.status === 'PENDING_PAY'">
					<view class="cancel-btn" @click="handleCancel">取消订单</view>
					<button class="pay-btn" :loading="paying" @click="handlePay">立即支付 ¥{{ formatPrice(detail.payAmount) }}</button>
				</view>
			</template>
		</template>
	</view>
</template>

<script>
import { getOrderDetail, payOrder, cancelOrder } from "../../api/order";
import { formatPrice, formatDateTime } from "../../utils/format";

const STATUS_TEXT = {
	PENDING_PAY: "待支付",
	PAID: "已支付",
	SHIPPED: "已发货",
	COMPLETED: "已完成",
	CANCELLED: "已取消",
};
const STATUS_DESC = {
	PENDING_PAY: "订单已创建，请尽快完成支付",
	PAID: "支付完成，商家正在备货",
	SHIPPED: "商品已发出，请注意查收",
	COMPLETED: "交易完成，感谢您的购买",
	CANCELLED: "订单已取消",
};

export default {
	data() {
		return {
			orderId: null,
			detail: null,
			loadFailed: false,
			paying: false,
			cancelling: false,
			paySuccess: false,
			paymentNo: "",
		};
	},
	computed: {
		statusText() {
			return STATUS_TEXT[this.detail && this.detail.status] || this.detail.status;
		},
		statusDesc() {
			return STATUS_DESC[this.detail && this.detail.status] || "";
		},
		// 快照格式：收件人,电话,省市区详情
		addrParts() {
			const snap = (this.detail && this.detail.receiverSnapshot) || "";
			return snap ? snap.split(",") : [];
		},
	},
	onLoad(query) {
		this.orderId = query && query.id ? Number(query.id) : null;
		if (!this.orderId) {
			uni.showToast({ title: "订单参数错误", icon: "none" });
			setTimeout(() => uni.navigateBack(), 600);
			return;
		}
		this.loadDetail();
	},
	methods: {
		formatPrice,
		formatDateTime,
		// U-014 订单详情
		async loadDetail() {
			this.loadFailed = false;
			try {
				this.detail = await getOrderDetail(this.orderId, true);
			} catch (e) {
				this.loadFailed = true;
			}
		},
		// U-015 模拟支付（4008 防重复：已支付/已取消时提示并刷新）
		async handlePay() {
			if (this.paying) return;
			this.paying = true;
			try {
				const data = await payOrder(this.orderId);
				this.paymentNo = (data && data.paymentNo) || "";
				this.paySuccess = true;
				if (this.detail) this.detail.status = "PAID";
			} catch (e) {
				// 提示由请求层 toast；4008 等状态类错误刷新详情
				if (e && e.code === 4008) {
					this.loadDetail();
				}
			} finally {
				this.paying = false;
			}
		},
		// U-016 取消订单（仅 PENDING_PAY，二次确认）
		handleCancel() {
			uni.showModal({
				title: "取消订单",
				content: "确定取消这笔订单吗？取消后库存将释放。",
				confirmColor: "#C81623",
				success: async (res) => {
					if (!res.confirm) return;
					this.cancelling = true;
					try {
						await cancelOrder(this.orderId);
						uni.showToast({ title: "订单已取消", icon: "none" });
						this.loadDetail();
					} catch (e) {
						if (e && e.code === 4008) this.loadDetail();
					} finally {
						this.cancelling = false;
					}
				},
			});
		},
		goHome() {
			uni.switchTab({ url: "/pages/index/index" });
		},
	},
};
</script>

<style lang="scss" scoped>
.pay-page {
	min-height: 100vh;
	background: #f5f5f5;
	padding: 20rpx 20rpx 180rpx;
}

/* 支付成功 */
.pay-success {
	padding: 120rpx 40rpx;
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
		font-size: 36rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 32rpx;
	}
	.s-no {
		background: #fff;
		border-radius: 16rpx;
		padding: 24rpx;
		margin-bottom: 20rpx;
		.s-label {
			font-size: 24rpx;
			color: #999;
			margin-right: 16rpx;
		}
		.s-value {
			font-size: 26rpx;
			color: #333;
			word-break: break-all;
		}
	}
	.s-amount {
		font-size: 26rpx;
		color: #333;
		margin-bottom: 40rpx;
		.s-price {
			color: #c81623;
			font-size: 34rpx;
			font-weight: bold;
		}
	}
	.s-btn {
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
}

/* 空态 */
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

/* 状态卡 */
.status-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 32rpx 24rpx;
	margin-bottom: 20rpx;
	text-align: center;
	.status-name {
		font-size: 36rpx;
		color: #c81623;
		font-weight: bold;
	}
	.status-desc {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #999;
	}
}

/* 通用区块 */
.section {
	background: #fff;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
	padding: 24rpx;
	.section-title {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 16rpx;
	}
}

/* 收货信息 */
.addr-line {
	display: flex;
	align-items: center;
	.addr-receiver {
		font-size: 30rpx;
		color: #333;
		font-weight: bold;
	}
	.addr-phone {
		font-size: 26rpx;
		color: #666;
		margin-left: 20rpx;
	}
}
.addr-detail {
	margin-top: 8rpx;
	font-size: 26rpx;
	color: #666;
	line-height: 1.5;
}

/* 商品明细 */
.goods-item {
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
	&:last-child {
		border-bottom: none;
	}
	.g-title {
		font-size: 26rpx;
		color: #333;
		line-height: 1.4;
	}
	.g-meta {
		margin-top: 10rpx;
		display: flex;
		justify-content: space-between;
		font-size: 24rpx;
		color: #666;
		.g-subtotal {
			color: #333;
		}
	}
}

/* 金额 */
.amount-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 26rpx;
	color: #333;
	padding: 8rpx 0;
	&.total {
		margin-top: 8rpx;
		border-top: 1rpx solid #f0f0f0;
		padding-top: 16rpx;
		.pay-amount {
			color: #c81623;
			font-size: 34rpx;
			font-weight: bold;
		}
	}
}

/* 订单信息 */
.info-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 10rpx 0;
	font-size: 26rpx;
	.i-label {
		color: #999;
		flex-shrink: 0;
	}
	.i-value {
		color: #333;
		word-break: break-all;
		text-align: right;
	}
}

/* 底部操作栏 */
.action-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	display: flex;
	align-items: center;
	justify-content: flex-end;
	padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
	.cancel-btn {
		font-size: 26rpx;
		color: #666;
		padding: 16rpx 32rpx;
		margin-right: 20rpx;
		border: 1rpx solid #ddd;
		border-radius: 36rpx;
	}
	.pay-btn {
		background: #c81623;
		color: #fff;
		font-size: 28rpx;
		line-height: 72rpx;
		height: 72rpx;
		padding: 0 48rpx;
		border-radius: 36rpx;
		&::after {
			border: none;
		}
	}
}
</style>
