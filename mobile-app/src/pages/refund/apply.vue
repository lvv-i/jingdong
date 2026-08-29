<template>
	<view class="apply-page">
		<view class="form-card">
			<view class="form-title">申请退款</view>

			<view class="form-item">
				<text class="label">订单编号</text>
				<text class="value">{{ orderId }}</text>
			</view>
			<view class="form-item">
				<text class="label">退款金额</text>
				<input class="input" type="digit" v-model="refundAmount" placeholder="请输入退款金额（不超过实付）" />
			</view>
			<view class="form-item">
				<text class="label">退款原因</text>
				<textarea class="textarea" v-model="reason" placeholder="请描述退款原因" maxlength="200" />
			</view>

			<view class="error-tips" v-if="errorMsg">{{ errorMsg }}</view>

			<button class="submit-btn" :class="{ loading: submitting }" :disabled="submitting" @click="submit">
				{{ submitting ? "提交中…" : "提交退款申请" }}
			</button>
		</view>
	</view>
</template>

<script>
import { createRefund } from "../../api/refund";

export default {
	data() {
		return {
			orderId: "",
			refundAmount: "",
			reason: "",
			submitting: false,
			errorMsg: "",
		};
	},
	onLoad(options) {
		this.orderId = options.orderId || "";
	},
	methods: {
		async submit() {
			this.errorMsg = "";
			if (!this.refundAmount || Number(this.refundAmount) <= 0) {
				this.errorMsg = "请输入有效的退款金额";
				return;
			}
			if (!this.reason.trim()) {
				this.errorMsg = "请填写退款原因";
				return;
			}
			this.submitting = true;
			try {
				const data = await createRefund({
					orderId: this.orderId,
					refundAmount: Number(this.refundAmount),
					reason: this.reason.trim(),
				});
				uni.showToast({ title: "退款申请已提交", icon: "success" });
				setTimeout(() => {
					uni.switchTab({ url: "/pages/mine/index" });
				}, 1200);
			} catch (e) {
				// 5002/5003/5004/5006 由请求层 toast
				this.errorMsg = e?.message || "提交失败";
			} finally {
				this.submitting = false;
			}
		},
	},
};
</script>

<style scoped lang="scss">
.apply-page { min-height: 100vh; background: #f5f5f5; padding: 32rpx; }
.form-card { background: #fff; border-radius: 12rpx; padding: 32rpx; }
.form-title { font-size: 34rpx; font-weight: bold; color: #333; margin-bottom: 32rpx; }
.form-item { margin-bottom: 24rpx;
	.label { font-size: 28rpx; color: #666; display: block; margin-bottom: 12rpx; }
	.value { font-size: 28rpx; color: #333; }
	.input { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; width: 100%; box-sizing: border-box; }
	.textarea { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; width: 100%; min-height: 160rpx; box-sizing: border-box; }
}
.error-tips { color: #C81623; font-size: 26rpx; margin-bottom: 24rpx; }
.submit-btn {
	background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx;
	font-size: 32rpx; text-align: center; margin-top: 16rpx;
	&.loading { opacity: 0.6; }
}
</style>