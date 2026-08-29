<template>
	<view class="profile-page">
		<view class="form-card">
			<view class="form-title">个人资料</view>

			<view class="form-item">
				<text class="label">用户名</text>
				<text class="value">{{ store.username }}</text>
			</view>
			<view class="form-item">
				<text class="label">角色</text>
				<text class="value">{{ store.role === 'USER' ? '普通用户' : store.role }}</text>
			</view>
			<view class="form-item">
				<text class="label">手机号</text>
				<input class="input" type="number" v-model="phone" placeholder="请输入手机号" maxlength="11" />
			</view>
			<view class="form-item">
				<text class="label">新密码</text>
				<input class="input" type="password" v-model="password" placeholder="留空则不修改" />
			</view>
			<view class="form-item">
				<text class="label">原密码</text>
				<input class="input" type="password" v-model="oldPassword" placeholder="修改密码需填写原密码" />
			</view>

			<view class="error-tips" v-if="errorMsg">{{ errorMsg }}</view>
			<view class="success-tips" v-if="successMsg">{{ successMsg }}</view>

			<button class="submit-btn" :class="{ loading: saving }" :disabled="saving" @click="save">
				{{ saving ? '保存中…' : '保存修改' }}
			</button>
		</view>
	</view>
</template>

<script>
import { useUserStore } from "../../stores/user";
import { updateProfile } from "../../api/user";

export default {
	data() {
		return {
			store: useUserStore(),
			phone: "",
			password: "",
			oldPassword: "",
			saving: false,
			errorMsg: "",
			successMsg: "",
		};
	},
	onShow() {
		this.phone = this.store.userInfo?.phone || "";
	},
	methods: {
		async save() {
			this.errorMsg = "";
			this.successMsg = "";
			if (this.password && !this.oldPassword) {
				this.errorMsg = "修改密码必须填写原密码";
				return;
			}
			this.saving = true;
			const payload = { phone: this.phone.trim() };
			if (this.password) {
				// 后端 DTO 字段为 newPassword（U-002 与 B 端保持一致）
				payload.newPassword = this.password;
				payload.oldPassword = this.oldPassword;
			}
			try {
				await updateProfile(payload);
				this.successMsg = "资料已更新";
				this.store.fetchProfile();
				setTimeout(() => { this.successMsg = ""; }, 2000);
			} catch (e) {
				this.errorMsg = e?.message || "保存失败";
			} finally { this.saving = false; }
		},
	},
};
</script>

<style scoped lang="scss">
.profile-page { min-height: 100vh; background: #f5f5f5; padding: 32rpx; }
.form-card { background: #fff; border-radius: 12rpx; padding: 32rpx; }
.form-title { font-size: 34rpx; font-weight: bold; color: #333; margin-bottom: 32rpx; }
.form-item { margin-bottom: 24rpx;
	.label { font-size: 28rpx; color: #666; display: block; margin-bottom: 12rpx; }
	.value { font-size: 28rpx; color: #333; }
	.input { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; width: 100%; box-sizing: border-box; }
}
.error-tips { color: #C81623; font-size: 26rpx; margin-bottom: 16rpx; }
.success-tips { color: #67C23A; font-size: 26rpx; margin-bottom: 16rpx; }
.submit-btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx; font-size: 32rpx; text-align: center; margin-top: 16rpx; &.loading { opacity: 0.6; } }
</style>