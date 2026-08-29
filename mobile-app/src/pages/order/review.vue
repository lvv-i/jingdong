<template>
	<view class="review-page">
		<view class="form-card">
			<view class="form-title">订单评价</view>

			<view class="form-item">
				<text class="label">订单编号</text>
				<text class="value">{{ orderId }}</text>
			</view>

			<!-- U-024 星级评分 1-5 -->
			<view class="form-item">
				<text class="label">评分</text>
				<view class="stars">
					<text
						class="star"
						v-for="s in 5"
						:key="s"
						:class="{ on: s <= rating }"
						@click="rating = s"
					>{{ s <= rating ? "★" : "☆" }}</text>
				</view>
			</view>

			<!-- 评价内容 (≤200 字) -->
			<view class="form-item">
				<text class="label">评价内容</text>
				<textarea
					class="textarea"
					v-model="comment"
					placeholder="分享你的使用感受（不超过 200 字）"
					maxlength="200"
				/>
				<text class="count">{{ comment.length }}/200</text>
			</view>

			<view class="error-tips" v-if="errorMsg">{{ errorMsg }}</view>

			<button class="submit-btn" :class="{ loading: submitting }" :disabled="submitting" @click="submit">
				{{ submitting ? "提交中…" : "提交评价" }}
			</button>
		</view>
	</view>
</template>

<script>
import { reviewOrder } from "../../api/order";

export default {
	data() {
		return {
			orderId: "",
			rating: 5,
			comment: "",
			submitting: false,
			errorMsg: "",
		};
	},
	onLoad(options) {
		this.orderId = options.id || "";
	},
	methods: {
		async submit() {
			this.errorMsg = "";
			if (!this.comment.trim()) {
				this.errorMsg = "请输入评价内容";
				return;
			}
			this.submitting = true;
			try {
				await reviewOrder(this.orderId, { rating: this.rating, comment: this.comment.trim() });
				uni.showToast({ title: "评价成功", icon: "success" });
				setTimeout(() => { uni.navigateBack(); }, 1200);
			} catch (e) {
				// 4001/4002/4003/4005 由请求层 toast
				this.errorMsg = e?.message || "提交失败";
			} finally {
				this.submitting = false;
			}
		},
	},
};
</script>

<style scoped lang="scss">
.review-page { min-height: 100vh; background: #f5f5f5; padding: 32rpx; }
.form-card { background: #fff; border-radius: 12rpx; padding: 32rpx; }
.form-title { font-size: 34rpx; font-weight: bold; color: #333; margin-bottom: 32rpx; }
.form-item { margin-bottom: 24rpx;
	.label { font-size: 28rpx; color: #666; display: block; margin-bottom: 12rpx; }
	.value { font-size: 28rpx; color: #333; }
	.count { font-size: 24rpx; color: #ccc; text-align: right; }
}
.stars { display: flex; gap: 12rpx;
	.star { font-size: 52rpx; color: #ddd; }
	.star.on { color: #E6A23C; }
}
.textarea { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; width: 100%; min-height: 160rpx; box-sizing: border-box; }
.error-tips { color: #C81623; font-size: 26rpx; margin-bottom: 24rpx; }
.submit-btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx; font-size: 32rpx; text-align: center; margin-top: 16rpx; &.loading { opacity: 0.6; } }
</style>