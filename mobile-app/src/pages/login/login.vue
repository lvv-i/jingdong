<template>
	<view class="login-page">
		<!-- 顶部品牌区 -->
		<view class="brand">
			<view class="logo">JD</view>
			<view class="brand-name">京东商城</view>
			<view class="brand-slogan">多端电商 · 一套后端</view>
		</view>

		<!-- 模式切换：密码登录 / 验证码登录 / 注册 -->
		<view class="mode-tabs">
			<view
				class="tab-item"
				:class="{ active: mode === 'password' }"
				@click="switchMode('password')"
			>密码登录</view>
			<view
				class="tab-item"
				:class="{ active: mode === 'sms' }"
				@click="switchMode('sms')"
			>验证码登录</view>
			<view
				class="tab-item"
				:class="{ active: mode === 'register' }"
				@click="switchMode('register')"
			>注册</view>
		</view>

		<!-- 密码登录（P-002） -->
		<view v-if="mode === 'password'" class="form">
			<view class="form-item">
				<input
					v-model="pwdForm.username"
					class="input"
					placeholder="用户名（演示账号 user001）"
					:maxlength="30"
				/>
			</view>
			<view class="form-item">
				<input
					v-model="pwdForm.password"
					class="input"
					password
					placeholder="密码"
					:maxlength="30"
				/>
			</view>
			<button class="submit-btn" :loading="submitting" :disabled="submitting" @click="onPwdLogin">
				登 录
			</button>
			<view class="tip">演示账号：user001 / user001（D-02 测试基线）</view>
		</view>

		<!-- 验证码登录（P-007/P-008，移动端主推，D-01 差异项 1） -->
		<view v-if="mode === 'sms'" class="form">
			<view class="form-item">
				<input
					v-model="smsForm.phone"
					class="input"
					type="number"
					placeholder="手机号"
					:maxlength="11"
				/>
			</view>
			<view class="form-item code-row">
				<input
					v-model="smsForm.smsCode"
					class="input code-input"
					type="number"
					placeholder="验证码"
					:maxlength="6"
				/>
				<button
					class="code-btn"
					:disabled="countdown > 0 || smsSending"
					@click="onSendCode"
				>
					{{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}
				</button>
			</view>
			<button class="submit-btn" :loading="submitting" :disabled="submitting" @click="onSmsLogin">
				登录 / 自动注册
			</button>
			<view class="tip">演示环境验证码固定为 123456（T5 P-007）</view>
		</view>

		<!-- 注册（P-001） -->
		<view v-if="mode === 'register'" class="form">
			<view class="form-item">
				<input
					v-model="regForm.username"
					class="input"
					placeholder="用户名（2-30 字符）"
					:maxlength="30"
				/>
			</view>
			<view class="form-item">
				<input
					v-model="regForm.password"
					class="input"
					password
					placeholder="密码（至少 6 位）"
					:maxlength="30"
				/>
			</view>
			<view class="form-item">
				<input
					v-model="regForm.phone"
					class="input"
					type="number"
					placeholder="手机号"
					:maxlength="11"
				/>
			</view>
			<button class="submit-btn" :loading="submitting" :disabled="submitting" @click="onRegister">
				注 册
			</button>
			<view class="tip">注册成功后自动切换至密码登录（P-001 不返回 token）</view>
		</view>
	</view>
</template>

<script>
import { useUserStore } from "../../stores/user";
import { preferredLoginMode } from "../../utils/platform";

export default {
	data() {
		return {
			// 端差异：App/小程序默认验证码登录，H5 默认密码登录（utils/platform.js）
			mode: preferredLoginMode(),
			submitting: false,
			smsSending: false,
			countdown: 0,
			timer: null,
			pwdForm: { username: "", password: "" },
			smsForm: { phone: "", smsCode: "" },
			regForm: { username: "", password: "", phone: "" },
		};
	},
	onUnload() {
		if (this.timer) clearInterval(this.timer);
	},
	methods: {
		switchMode(m) {
			this.mode = m;
		},
		/** P-002 账号密码登录 */
		async onPwdLogin() {
			if (!this.pwdForm.username.trim()) return uni.showToast({ title: "请输入用户名", icon: "none" });
			if (!this.pwdForm.password) return uni.showToast({ title: "请输入密码", icon: "none" });
			this.submitting = true;
			try {
				const store = useUserStore();
				await store.loginByPassword(this.pwdForm.username.trim(), this.pwdForm.password);
				uni.showToast({ title: "登录成功", icon: "success" });
				setTimeout(() => uni.switchTab({ url: "/pages/index/index" }), 600);
			} catch (e) {
				// 错误文案已由请求层 toast（2003 用户名不存在 / 2004 密码错误等）
			} finally {
				this.submitting = false;
			}
		},
		/** P-007 发送验证码（60 秒倒计时） */
		async onSendCode() {
			if (!/^1\d{10}$/.test(this.smsForm.phone)) {
				return uni.showToast({ title: "请输入 11 位手机号", icon: "none" });
			}
			this.smsSending = true;
			try {
				const store = useUserStore();
				await store.sendSmsCode(this.smsForm.phone);
				uni.showToast({ title: "验证码已发送（演示固定 123456）", icon: "none" });
				this.countdown = 60;
				this.timer = setInterval(() => {
					this.countdown -= 1;
					if (this.countdown <= 0) clearInterval(this.timer);
				}, 1000);
			} catch (e) {
				// 1007 限频提示由请求层 toast
			} finally {
				this.smsSending = false;
			}
		},
		/** P-008 验证码登录（未注册手机号自动注册） */
		async onSmsLogin() {
			if (!/^1\d{10}$/.test(this.smsForm.phone)) {
				return uni.showToast({ title: "请输入 11 位手机号", icon: "none" });
			}
			if (!/^\d{6}$/.test(this.smsForm.smsCode)) {
				return uni.showToast({ title: "请输入 6 位验证码", icon: "none" });
			}
			this.submitting = true;
			try {
				const store = useUserStore();
				await store.loginBySms(this.smsForm.phone, this.smsForm.smsCode);
				uni.showToast({ title: "登录成功", icon: "success" });
				setTimeout(() => uni.switchTab({ url: "/pages/index/index" }), 600);
			} catch (e) {
				// 2005 验证码错误提示由请求层 toast
			} finally {
				this.submitting = false;
			}
		},
		/** P-001 注册 */
		async onRegister() {
			if (!this.regForm.username.trim() || this.regForm.username.trim().length < 2) {
				return uni.showToast({ title: "用户名至少 2 个字符", icon: "none" });
			}
			if (!this.regForm.password || this.regForm.password.length < 6) {
				return uni.showToast({ title: "密码至少 6 位", icon: "none" });
			}
			if (!/^1\d{10}$/.test(this.regForm.phone)) {
				return uni.showToast({ title: "请输入 11 位手机号", icon: "none" });
			}
			this.submitting = true;
			try {
				const store = useUserStore();
				await store.register({
					username: this.regForm.username.trim(),
					password: this.regForm.password,
					phone: this.regForm.phone,
				});
				uni.showToast({ title: "注册成功，请登录", icon: "success" });
				// 回填用户名并切换密码登录
				this.pwdForm.username = this.regForm.username.trim();
				this.switchMode("password");
			} catch (e) {
				// 2001 用户名已存在 / 2002 手机号已注册 / 1001 参数错误，文案由请求层 toast
			} finally {
				this.submitting = false;
			}
		},
	},
};
</script>

<style lang="scss">
.login-page {
	min-height: 100vh;
	background: #fff;
	padding: 60rpx 48rpx;
	box-sizing: border-box;
}

.brand {
	text-align: center;
	margin: 40rpx 0 60rpx;
	.logo {
		width: 140rpx;
		height: 140rpx;
		line-height: 140rpx;
		margin: 0 auto 20rpx;
		border-radius: 32rpx;
		background: #c81623;
		color: #fff;
		font-size: 56rpx;
		font-weight: bold;
		text-align: center;
	}
	.brand-name {
		font-size: 40rpx;
		font-weight: bold;
		color: #333;
	}
	.brand-slogan {
		font-size: 24rpx;
		color: #999;
		margin-top: 8rpx;
	}
}

.mode-tabs {
	display: flex;
	border-bottom: 2rpx solid #eee;
	margin-bottom: 40rpx;
	.tab-item {
		flex: 1;
		text-align: center;
		padding: 24rpx 0;
		font-size: 30rpx;
		color: #666;
		position: relative;
		&.active {
			color: #c81623;
			font-weight: bold;
			&::after {
				content: "";
				position: absolute;
				left: 50%;
				transform: translateX(-50%);
				bottom: -2rpx;
				width: 72rpx;
				height: 6rpx;
				border-radius: 3rpx;
				background: #c81623;
			}
		}
	}
}

.form {
	.form-item {
		border: 2rpx solid #e5e5e5;
		border-radius: 12rpx;
		padding: 4rpx 24rpx;
		margin-bottom: 28rpx;
		display: flex;
		align-items: center;
		.input {
			flex: 1;
			height: 88rpx;
			font-size: 30rpx;
		}
	}
	.code-row {
		.code-input {
			flex: 1;
		}
		.code-btn {
			flex-shrink: 0;
			font-size: 26rpx;
			color: #c81623;
			background: none;
			border: none;
			padding: 0 0 0 24rpx;
			line-height: 88rpx;
			&::after {
				border: none;
			}
			&[disabled] {
				color: #999;
			}
		}
	}
	.submit-btn {
		margin-top: 48rpx;
		background: #c81623;
		color: #fff;
		font-size: 32rpx;
		height: 92rpx;
		line-height: 92rpx;
		border-radius: 46rpx;
		&::after {
			border: none;
		}
		&[disabled] {
			opacity: 0.7;
			color: #fff;
		}
	}
	.tip {
		margin-top: 24rpx;
		font-size: 24rpx;
		color: #999;
		text-align: center;
	}
}
</style>
