<template>
	<view class="detail-page">
		<!-- 加载失败 -->
		<view class="empty-box" v-if="!detail && loadFailed">
			<view class="big">加载失败</view>
			<button class="btn" @click="loadDetail">重新加载</button>
		</view>
		<!-- 订单不存在 -->
		<view class="empty-box" v-else-if="errorCode === 4001">
			<view class="big">订单不存在</view>
			<button class="btn" @click="goBack">返回</button>
		</view>

		<template v-else-if="detail">
			<!-- 状态卡片（U-014） -->
			<view class="status-card" :style="{ background: statusBgColor(detail.status) }">
				<view class="status-icon">{{ statusIcon(detail.status) }}</view>
				<view class="status-text">{{ statusLabel(detail.status) }}</view>
				<view class="status-desc">{{ statusDesc(detail.status) }}</view>
				<view class="status-amount">实付 ¥{{ formatPriceWithComma(detail.payAmount) }}</view>
			</view>

			<!-- 收货信息快照 -->
			<view class="section" v-if="detail.receiverSnapshot">
				<view class="section-title">收货信息</view>
				<view class="section-body">
					<view class="snap-item" v-for="(v, k) in splitReceiver(detail.receiverSnapshot)" :key="k">{{ v }}</view>
				</view>
			</view>

			<!-- 运单号（已发货/已收货） -->
			<view class="section" v-if="detail.shippingNo">
				<view class="section-title">物流信息</view>
				<view class="section-body">运单号：{{ detail.shippingNo }}</view>
			</view>

			<!-- 商品明细快照 -->
			<view class="section">
				<view class="section-title">商品明细</view>
				<view class="goods-item" v-for="(item, idx) in detail.items" :key="idx">
					<view class="goods-info">
						<view class="goods-name">{{ item.titleSnapshot }}</view>
						<view class="goods-meta">
							<text class="g-price">¥{{ formatPrice(item.priceSnapshot) }}</text>
							<text class="g-qty">×{{ item.quantity }}</text>
						</view>
					</view>
					<view class="goods-subtotal">¥{{ formatPrice(item.priceSnapshot * item.quantity) }}</view>
				</view>
			</view>

			<!-- 金额汇总 -->
			<view class="section">
				<view class="section-title">金额明细</view>
				<view class="amount-row">
					<text>商品总额</text><text>¥{{ formatPriceWithComma(detail.totalAmount) }}</text>
				</view>
				<view class="amount-row">
					<text>运费</text><text>¥0.00</text>
				</view>
				<view class="amount-row total-row">
					<text>实付</text><text class="red">¥{{ formatPriceWithComma(detail.payAmount) }}</text>
				</view>
			</view>

			<!-- 订单信息 -->
			<view class="section">
				<view class="section-title">订单信息</view>
				<view class="info-row"><text class="label">订单编号</text><text>{{ detail.orderNo }}</text></view>
				<view class="info-row"><text class="label">下单时间</text><text>{{ formatDateTime(detail.createdAt) }}</text></view>
				<view class="info-row" v-if="detail.paidAt"><text class="label">支付时间</text><text>{{ formatDateTime(detail.paidAt) }}</text></view>
				<view class="info-row" v-if="detail.shippedAt"><text class="label">发货时间</text><text>{{ formatDateTime(detail.shippedAt) }}</text></view>
				<view class="info-row" v-if="detail.completedAt"><text class="label">完成时间</text><text>{{ formatDateTime(detail.completedAt) }}</text></view>
				<view class="info-row" v-if="detail.shippingNo"><text class="label">运单号</text><text>{{ detail.shippingNo }}</text></view>
			</view>

			<!-- 底部操作栏（T1 状态机驱动按钮显隐） -->
			<view class="bottom-bar" v-if="showActions">
				<view class="btn-pay" v-if="detail.status === 'PENDING_PAY'" @click="payOrder">立即支付</view>
				<view class="btn-cancel" v-if="detail.status === 'PENDING_PAY'" @click="cancelOrder">取消订单</view>
				<view class="btn-pay" v-if="detail.status === 'SHIPPED'" @click="confirmOrder">确认收货</view>
				<view class="btn-cancel" v-if="detail.status === 'COMPLETED' && !detail.reviewed" @click="goReview">去评价</view>
				<view class="btn-cancel" v-if="canRefund" @click="goRefund">申请退款</view>
			</view>
		</template>
	</view>
</template>

<script>
import { getOrderDetail, payOrder, cancelOrder, confirmOrder } from "../../api/order";
import { formatPrice, formatPriceWithComma, formatDateTime } from "../../utils/format";

const PLACEHOLDER = "/static/logo.png";

// T1 状态映射
const STATUS_INFO = {
	PENDING_PAY:  { label: "待支付",  desc: "请在 30 分钟内完成支付", icon: "⏳", bg: "#FFF7E6" },
	PAID:         { label: "待发货",  desc: "商家正在备货中",          icon: "📦", bg: "#E6F7FF" },
	SHIPPED:      { label: "待收货",  desc: "商品已发出，请注意查收",   icon: "🚚", bg: "#E6F7FF" },
	COMPLETED:    { label: "已完成",  desc: "交易已完成",              icon: "✓", bg: "#F0FFF0" },
	CANCELLED:    { label: "已取消",  desc: "订单已取消",              icon: "✗", bg: "#F5F5F5" },
};

export default {
	data() {
		return {
			detail: null,
			loadFailed: false,
			errorCode: null,
		};
	},
	computed: {
		orderId() {
			return this.$route?.query?.id || this.orderIdFromOnLoad || "";
		},
		showActions() {
			if (!this.detail) return false;
			const s = this.detail.status;
			return s === "PENDING_PAY" || s === "SHIPPED" || (s === "COMPLETED" && !this.detail.reviewed) || this.canRefund;
		},
		canRefund() {
			// PAID/SHIPPED/COMPLETED 可发起退款（U-018 规则）
			if (!this.detail) return false;
			return ["PAID", "SHIPPED", "COMPLETED"].includes(this.detail.status) && !this.detail.hasRefund;
		},
	},
	onLoad(options) {
		this.orderIdFromOnLoad = options?.id || "";
		this.loadDetail();
	},
	// 从评价/退款页返回时刷新（reviewed 状态等可能已变化）
	onShow() {
		if (this.orderId && this.detail) this.loadDetail();
	},
	methods: {
		async loadDetail() {
			this.loadFailed = false;
			this.errorCode = null;
			try {
				this.detail = await getOrderDetail(this.orderId, true);
			} catch (e) {
				console.error("loadDetail error:", e);
				this.errorCode = e?.code;
				if (e?.code !== 4001) this.loadFailed = true;
			}
		},
		goBack() { uni.navigateBack(); },
		goReview() { uni.navigateTo({ url: `/pages/order/review?id=${this.orderId}` }); },
		goRefund() { uni.navigateTo({ url: `/pages/refund/apply?orderId=${this.orderId}` }); },
		statusLabel(s) { return (STATUS_INFO[s] || {}).label || s; },
		statusDesc(s) { return (STATUS_INFO[s] || {}).desc || ""; },
		statusIcon(s) { return (STATUS_INFO[s] || {}).icon || ""; },
		statusBgColor(s) { return (STATUS_INFO[s] || {}).bg || "#fff"; },
		formatPrice, formatPriceWithComma, formatDateTime,
		splitReceiver(snap) {
			if (!snap) return [];
			return snap.split(",").map(s => s.trim()).filter(Boolean);
		},
		imgErr(e) { e.target.src = PLACEHOLDER; },
		// 操作
		async payOrder() {
			uni.showLoading({ title: "支付中" });
			try {
				await payOrder(this.orderId);
				uni.hideLoading();
				uni.showToast({ title: "支付成功", icon: "success" });
				this.loadDetail();
			} catch (e) { uni.hideLoading(); }
		},
		async cancelOrder() {
			const res = await new Promise((r) => uni.showModal({ title: "取消订单", content: "确定取消该订单吗？", success: r }));
			if (!res.confirm) return;
			try {
				await cancelOrder(this.orderId);
				uni.showToast({ title: "已取消", icon: "success" });
				this.loadDetail();
			} catch (e) {}
		},
		async confirmOrder() {
			const res = await new Promise((r) => uni.showModal({ title: "确认收货", content: "确定已收到商品吗？", success: r }));
			if (!res.confirm) return;
			try {
				await confirmOrder(this.orderId);
				uni.showToast({ title: "已确认收货", icon: "success" });
				this.loadDetail();
			} catch (e) {}
		},
	},
};
</script>

<style scoped lang="scss">
.detail-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 120rpx; }
.empty-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-top: 200rpx;
	.big { font-size: 34rpx; color: #999; margin-bottom: 40rpx; }
	.btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 60rpx; }
}
.status-card {
	padding: 48rpx 32rpx;
	text-align: center;
	.status-icon { font-size: 64rpx; margin-bottom: 8rpx; }
	.status-text { font-size: 36rpx; font-weight: bold; margin-bottom: 8rpx; }
	.status-desc { font-size: 26rpx; color: #666; margin-bottom: 12rpx; }
	.status-amount { font-size: 32rpx; font-weight: bold; color: #C81623; }
}
.section {
	background: #fff;
	margin: 20rpx 24rpx;
	border-radius: 12rpx;
	padding: 24rpx;
	.section-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 16rpx; }
	.section-body { font-size: 28rpx; color: #333; line-height: 1.6; }
	.snap-item { font-size: 28rpx; color: #333; line-height: 1.8; }
}
.goods-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
	&:last-child { border-bottom: none; }
	.goods-name { font-size: 28rpx; color: #333; margin-bottom: 4rpx; }
	.goods-meta { font-size: 26rpx; color: #999;
		.g-price { color: #666; }
		.g-qty { margin-left: 20rpx; }
	}
	.goods-subtotal { font-size: 28rpx; font-weight: bold; color: #333; }
}
.amount-row {
	display: flex;
	justify-content: space-between;
	font-size: 28rpx;
	color: #666;
	line-height: 2;
	&.total-row { font-size: 32rpx; font-weight: bold; color: #333; border-top: 1rpx solid #eee; padding-top: 12rpx; margin-top: 8rpx; }
	.red { color: #C81623; }
}
.info-row {
	display: flex;
	font-size: 26rpx;
	color: #666;
	line-height: 2;
	.label { color: #999; width: 140rpx; flex-shrink: 0; }
}
.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	padding: 20rpx 32rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	display: flex;
	justify-content: flex-end;
	gap: 20rpx;
	border-top: 1rpx solid #eee;
	.btn-pay { background: #C81623; color: #fff; padding: 18rpx 48rpx; border-radius: 36rpx; font-size: 28rpx; }
	.btn-cancel { background: transparent; color: #666; border: 1rpx solid #ddd; padding: 18rpx 48rpx; border-radius: 36rpx; font-size: 28rpx; }
}
</style>