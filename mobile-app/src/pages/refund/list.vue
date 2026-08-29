<template>
	<view class="refund-page">
		<!-- 未登录 -->
		<view class="empty-box" v-if="!isLogin">
			<view class="big">请先登录</view>
			<button class="btn" @click="goLogin">去登录</button>
		</view>

		<!-- 加载失败 -->
		<view class="empty-box" v-else-if="loadFailed">
			<view class="big">加载失败</view>
			<button class="btn" @click="loadRefunds">重新加载</button>
		</view>

		<!-- 空列表 -->
		<view class="empty-box" v-else-if="!list.length && !loading">
			<view class="big">暂无售后记录</view>
		</view>

		<template v-else>
			<!-- U-019 退款列表 -->
			<view class="refund-list">
				<view class="refund-card" v-for="r in list" :key="r.id">
					<view class="card-hd">
						<text class="refund-no">退款单号：{{ r.refundNo }}</text>
						<text class="status" :style="{ color: refundStatusColor(r.status) }">{{ refundStatusLabel(r.status) }}</text>
					</view>
					<view class="card-body">
						<view class="info-row"><text class="label">关联订单</text><text>{{ r.orderId }}</text></view>
						<view class="info-row"><text class="label">退款金额</text><text class="red">¥{{ formatPriceWithComma(r.refundAmount) }}</text></view>
						<view class="info-row"><text class="label">申请原因</text><text>{{ r.reason }}</text></view>
						<view class="info-row"><text class="label">申请时间</text><text>{{ formatDateTime(r.createdAt) }}</text></view>
						<!-- 商家回复（持久化展示） -->
						<view class="info-row" v-if="r.merchantReply">
							<text class="label">商家回复</text><text>{{ r.merchantReply }}</text>
						</view>
						<!-- 管理员裁决（持久化展示） -->
						<view class="info-row" v-if="r.adminResult">
							<text class="label">平台裁决</text><text>{{ r.adminResult }}</text>
						</view>
					</view>
					<view class="card-ft" v-if="refundActions(r).length">
						<view
							class="action-btn"
							v-for="a in refundActions(r)"
							:key="a.type"
							:class="a.cls"
							@click="a.handler(r)"
						>{{ a.label }}</view>
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
import { getRefunds, cancelRefund, interveneRefund } from "../../api/refund";
import { formatPriceWithComma, formatDateTime } from "../../utils/format";

// T1 售后单 6 态
const REFUND_LABELS = {
	REFUNDING: "退款中",
	MERCHANT_AGREED: "商家同意",
	MERCHANT_REJECTED: "商家拒绝",
	ADMIN_INTERVENED: "平台介入",
	REFUNDED: "已退款",
	CLOSED: "已关闭",
};
const REFUND_COLORS = {
	REFUNDING: "#E6A23C",
	MERCHANT_AGREED: "#67C23A",
	MERCHANT_REJECTED: "#C81623",
	ADMIN_INTERVENED: "#409EFF",
	REFUNDED: "#67C23A",
	CLOSED: "#909399",
};

export default {
	data() {
		return {
			list: [],
			page: 1,
			pageSize: 10,
			total: 0,
			loading: false,
			loadFailed: false,
		};
	},
	computed: {
		isLogin() { return isLoggedIn(); },
		hasMore() { return this.list.length < this.total; },
	},
	onShow() {
		if (isLoggedIn()) {
			this.page = 1;
			this.list = [];
			this.loadRefunds();
		}
	},
	methods: {
		async loadRefunds() {
			this.loading = true;
			this.loadFailed = false;
			try {
				const data = await getRefunds({ page: this.page, pageSize: this.pageSize }, true);
				if (this.page === 1) this.list = data.list || [];
				else this.list = this.list.concat(data.list || []);
				this.total = data.total || 0;
			} catch (e) {
				if (this.page === 1) this.loadFailed = true;
			} finally { this.loading = false; }
		},
		loadMore() {
			if (!this.hasMore || this.loading) return;
			this.page++;
			this.loadRefunds();
		},
		goLogin() { uni.reLaunch({ url: "/pages/login/login" }); },
		refundStatusLabel(s) { return REFUND_LABELS[s] || s; },
		refundStatusColor(s) { return REFUND_COLORS[s] || "#333"; },
		formatPriceWithComma, formatDateTime,
		// 按钮逻辑（T1 状态机）
		refundActions(r) {
			const actions = [];
			if (r.status === "REFUNDING") {
				actions.push({ type: "cancel", label: "撤销退款", cls: "btn-cancel", handler: this.cancelRefundFn });
			}
			if (r.status === "MERCHANT_REJECTED") {
				actions.push({ type: "intervene", label: "申请平台介入", cls: "btn-danger", handler: this.interveneFn });
			}
			return actions;
		},
		async cancelRefundFn(r) {
			const res = await new Promise((r2) => uni.showModal({ title: "撤销退款", content: "确定撤销该退款申请吗？", success: r2 }));
			if (!res.confirm) return;
			try {
				await cancelRefund(r.id);
				uni.showToast({ title: "已撤销", icon: "success" });
				r.status = "CLOSED";
			} catch (e) {}
		},
		async interveneFn(r) {
			const res = await new Promise((r2) => uni.showModal({ title: "申请平台介入", content: "平台客服将介入处理该退款单，确定吗？", success: r2 }));
			if (!res.confirm) return;
			try {
				await interveneRefund(r.id);
				uni.showToast({ title: "已提交介入申请", icon: "success" });
				r.status = "ADMIN_INTERVENED";
			} catch (e) {}
		},
	},
};
</script>

<style scoped lang="scss">
.refund-page { min-height: 100vh; background: #f5f5f5; }
.empty-box {
	display: flex; flex-direction: column; align-items: center; padding-top: 200rpx;
	.big { font-size: 34rpx; color: #999; margin-bottom: 40rpx; }
	.btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 60rpx; }
}
.refund-card {
	background: #fff; margin: 16rpx 24rpx; border-radius: 12rpx; padding: 24rpx;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.card-hd { display: flex; justify-content: space-between; font-size: 26rpx;
	.refund-no { color: #666; }
	.status { font-weight: bold; }
}
.card-body { margin-top: 16rpx; }
.info-row { display: flex; font-size: 26rpx; color: #666; line-height: 2;
	.label { color: #999; width: 140rpx; flex-shrink: 0; }
	.red { color: #C81623; }
}
.card-ft { display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx solid #f5f5f5;
	.action-btn { padding: 10rpx 32rpx; border-radius: 32rpx; font-size: 26rpx; }
	.btn-cancel { background: transparent; color: #666; border: 1rpx solid #ddd; }
	.btn-danger { background: #C81623; color: #fff; }
}
.pager { text-align: center; padding: 32rpx; font-size: 26rpx;
	.more { color: #C81623; }
	.no-more { color: #ccc; }
}
</style>