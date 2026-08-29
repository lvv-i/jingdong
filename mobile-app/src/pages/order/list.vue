<template>
	<view class="order-list-page">
		<!-- 状态 Tab 栏 -->
		<view class="tabs">
			<view
				class="tab"
				v-for="t in tabs"
				:key="t.value"
				:class="{ active: activeTab === t.value }"
				@click="switchTab(t.value)"
			>{{ t.label }}</view>
		</view>

		<!-- 未登录 -->
		<view class="empty-box" v-if="!isLogin">
			<view class="big">请先登录</view>
			<button class="btn" @click="goLogin">去登录</button>
		</view>

		<!-- 加载失败 -->
		<view class="empty-box" v-else-if="loadFailed">
			<view class="big">加载失败</view>
			<view class="sub">请确认网络或后端服务后重试</view>
			<button class="btn" @click="loadOrders">重新加载</button>
		</view>

		<!-- 空订单 -->
		<view class="empty-box" v-else-if="!orders.length && !loading">
			<view class="big">暂无订单</view>
			<view class="sub" v-if="activeTab">该状态下没有订单</view>
			<view class="sub" v-else>快去挑选心仪的商品吧</view>
			<button class="btn" @click="goHome">去逛逛</button>
		</view>

		<template v-else>
			<!-- U-013 订单列表 -->
			<view class="order-list">
				<view class="order-card" v-for="o in orders" :key="o.id" @click="goDetail(o.id)">
					<view class="card-hd">
						<text class="order-no">订单号：{{ o.orderNo }}</text>
						<text class="status" :style="{ color: statusColor(o.status) }">{{ statusLabel(o.status) }}</text>
					</view>
					<view class="card-body">
						<image class="thumb" :src="o.firstItemImage || PLACEHOLDER" mode="aspectFill" @error="imgErr" />
						<view class="info">
							<view class="items-count">共 {{ o.itemsCount || 1 }} 件</view>
							<view class="amount">¥{{ formatPriceWithComma(o.payAmount) }}</view>
						</view>
					</view>
					<view class="card-ft">
						<text class="time">{{ formatDateTime(o.createdAt) }}</text>
						<!-- 按钮随状态显隐（T1 状态机） -->
						<view class="actions">
							<view class="btn-pay" v-if="o.status === 'PENDING_PAY'" @click.stop="payOrder(o)">去支付</view>
							<view class="btn-pay" v-if="o.status === 'SHIPPED'" @click.stop="confirmOrder(o)">确认收货</view>
							<view class="btn-cancel" v-if="o.status === 'PENDING_PAY'" @click.stop="cancelOrder(o)">取消</view>
							<view class="btn-cancel" v-if="o.status === 'COMPLETED' && !o.reviewed" @click.stop="goReview(o.id)">评价</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 分页 -->
			<view class="pager" v-if="total > pageSize">
				<view class="more" v-if="hasMore" @click="loadMore">加载更多</view>
				<view class="no-more" v-else>没有更多了</view>
			</view>
		</template>
	</view>
</template>

<script>
import { isLoggedIn } from "../../utils/auth";
import { getOrders, payOrder, cancelOrder, confirmOrder } from "../../api/order";
import { formatPriceWithComma, formatDateTime } from "../../utils/format";

const PLACEHOLDER = "/static/logo.png";

// T1 订单状态映射（与 T5 U-013 一致）
const STATUS_LABELS = {
	PENDING_PAY: "待支付",
	PAID: "待发货",
	SHIPPED: "待收货",
	COMPLETED: "已完成",
	CANCELLED: "已取消",
};
const STATUS_COLORS = {
	PENDING_PAY: "#C81623",
	PAID: "#E6A23C",
	SHIPPED: "#409EFF",
	COMPLETED: "#67C23A",
	CANCELLED: "#909399",
};
const TABS = [
	{ label: "全部", value: "" },
	{ label: "待支付", value: "PENDING_PAY" },
	{ label: "待发货", value: "PAID" },
	{ label: "待收货", value: "SHIPPED" },
	{ label: "已完成", value: "COMPLETED" },
	{ label: "已取消", value: "CANCELLED" },
];

export default {
	data() {
		return {
			activeTab: "",
			orders: [],
			page: 1,
			pageSize: 10,
			total: 0,
			loading: false,
			loadFailed: false,
			PLACEHOLDER,
			tabs: TABS,
			_pendingTab: "",
		};
	},
	computed: {
		isLogin() {
			return isLoggedIn();
		},
		hasMore() {
			return this.orders.length < this.total;
		},
	},
	onLoad(options) {
		// 支持从「我的」页带入状态预设
		if (options && options.status) {
			const s = decodeURIComponent(options.status);
			if (TABS.find(t => t.value === s)) this._pendingTab = s;
		}
	},
	onShow() {
		if (isLoggedIn()) {
			if (this._pendingTab) {
				this.activeTab = this._pendingTab;
				this._pendingTab = "";
			}
			this.page = 1;
			this.orders = [];
			this.loadOrders();
		}
	},
	methods: {
		async loadOrders() {
			this.loading = true;
			this.loadFailed = false;
			try {
				const params = { page: this.page, pageSize: this.pageSize };
				if (this.activeTab) params.status = this.activeTab;
				const data = await getOrders(params, true);
				if (this.page === 1) {
					this.orders = data.list || [];
				} else {
					this.orders = this.orders.concat(data.list || []);
				}
				this.total = data.total || 0;
			} catch (e) {
				console.error("loadOrders error:", e);
				if (this.page === 1) this.loadFailed = true;
			} finally {
				this.loading = false;
			}
		},
		switchTab(value) {
			if (this.activeTab === value) return;
			this.activeTab = value;
			this.page = 1;
			this.orders = [];
			this.loadOrders();
		},
		loadMore() {
			if (!this.hasMore || this.loading) return;
			this.page++;
			this.loadOrders();
		},
		goDetail(id) {
			uni.navigateTo({ url: `/pages/order/detail?id=${id}` });
		},
		goReview(id) {
			uni.navigateTo({ url: `/pages/order/review?id=${id}` });
		},
		goHome() {
			uni.switchTab({ url: "/pages/index/index" });
		},
		goLogin() {
			uni.reLaunch({ url: "/pages/login/login" });
		},
		statusLabel(s) { return STATUS_LABELS[s] || s; },
		statusColor(s) { return STATUS_COLORS[s] || "#333"; },
		formatPriceWithComma,
		formatDateTime,
		imgErr(e) {
			e.target.src = PLACEHOLDER;
		},
		// 操作按钮
		async payOrder(o) {
			uni.showLoading({ title: "支付中" });
			try {
				await payOrder(o.id);
				uni.hideLoading();
				uni.showToast({ title: "支付成功", icon: "success" });
				// 刷新当前订单状态
				o.status = "PAID";
			} catch (e) {
				uni.hideLoading();
				// 4008 由请求层 toast
			}
		},
		async cancelOrder(o) {
			const res = await new Promise((resolve) => {
				uni.showModal({ title: "取消订单", content: "确定取消该订单吗？", success: resolve });
			});
			if (!res.confirm) return;
			try {
				await cancelOrder(o.id);
				uni.showToast({ title: "已取消", icon: "success" });
				o.status = "CANCELLED";
			} catch (e) {
				// 错误由请求层 toast
			}
		},
		async confirmOrder(o) {
			const res = await new Promise((resolve) => {
				uni.showModal({ title: "确认收货", content: "确定已收到商品吗？", success: resolve });
			});
			if (!res.confirm) return;
			try {
				await confirmOrder(o.id);
				uni.showToast({ title: "已确认收货", icon: "success" });
				o.status = "COMPLETED";
			} catch (e) {
				// 错误由请求层 toast
			}
		},
	},
};
</script>

<style scoped lang="scss">
.order-list-page {
	min-height: 100vh;
	background: #f5f5f5;
}
.tabs {
	display: flex;
	background: #fff;
	border-bottom: 1rpx solid #eee;
	.tab {
		flex: 1;
		text-align: center;
		padding: 24rpx 0;
		font-size: 28rpx;
		color: #666;
		&.active {
			color: #C81623;
			border-bottom: 4rpx solid #C81623;
			font-weight: bold;
		}
	}
}
.empty-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-top: 200rpx;
	color: #999;
	.big { font-size: 34rpx; margin-bottom: 16rpx; }
	.sub { font-size: 26rpx; margin-bottom: 40rpx; }
	.btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 60rpx; font-size: 28rpx; }
}
.order-card {
	background: #fff;
	margin: 16rpx 24rpx;
	border-radius: 12rpx;
	padding: 24rpx;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.card-hd {
	display: flex;
	justify-content: space-between;
	font-size: 26rpx;
	.order-no { color: #666; }
	.status { font-weight: bold; }
}
.card-body {
	display: flex;
	margin-top: 20rpx;
	align-items: center;
	.thumb { width: 140rpx; height: 140rpx; border-radius: 8rpx; background: #f5f5f5; flex-shrink: 0; }
	.info { margin-left: 20rpx; flex: 1;
		.items-count { font-size: 26rpx; color: #999; }
		.amount { font-size: 36rpx; font-weight: bold; color: #C81623; margin-top: 8rpx; }
	}
}
.card-ft {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 20rpx;
	.time { font-size: 24rpx; color: #999; }
	.actions { display: flex; gap: 16rpx;
		.btn-pay { background: #C81623; color: #fff; padding: 10rpx 32rpx; border-radius: 32rpx; font-size: 26rpx; }
		.btn-cancel { background: transparent; color: #999; border: 1rpx solid #ddd; padding: 10rpx 32rpx; border-radius: 32rpx; font-size: 26rpx; }
	}
}
.pager { text-align: center; padding: 32rpx; font-size: 26rpx;
	.more { color: #C81623; }
	.no-more { color: #ccc; }
}
</style>