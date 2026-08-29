<template>
	<view class="edit-page">
		<view class="form-card">
			<view class="form-title">{{ isEdit ? "编辑地址" : "新增地址" }}</view>

			<view class="form-item">
				<text class="label">收货人</text>
				<input class="input" v-model="form.receiver" placeholder="请输入收货人姓名" />
			</view>
			<view class="form-item">
				<text class="label">手机号</text>
				<input class="input" type="number" v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
			</view>
			<view class="form-item">
				<text class="label">所在地区</text>
				<view class="region-row">
					<input class="input-sm" v-model="form.province" placeholder="省" />
					<input class="input-sm" v-model="form.city" placeholder="市" />
					<input class="input-sm" v-model="form.district" placeholder="区" />
				</view>
			</view>
			<view class="form-item">
				<text class="label">详细地址</text>
				<textarea class="textarea" v-model="form.detail" placeholder="街道/小区/门牌号" maxlength="100" />
			</view>
			<view class="form-item">
				<text class="label">设为默认</text>
				<switch :checked="form.isDefault === 1" @change="onDefaultSwitch" color="#C81623" />
			</view>

			<view class="error-tips" v-if="errorMsg">{{ errorMsg }}</view>

			<button class="submit-btn" :class="{ loading: saving }" :disabled="saving" @click="save">
				{{ saving ? "保存中…" : "保存" }}
			</button>
			<view class="delete-btn" v-if="isEdit" @click="doDelete">删除该地址</view>
		</view>
	</view>
</template>

<script>
import { addAddress, updateAddress, deleteAddress } from "../../api/address";

export default {
	data() {
		return {
			editId: "",
			form: {
				receiver: "",
				phone: "",
				province: "",
				city: "",
				district: "",
				detail: "",
				isDefault: 0,
			},
			saving: false,
			errorMsg: "",
		};
	},
	computed: {
		isEdit() { return !!this.editId; },
	},
	onLoad(options) {
		this.editId = options.id || "";
		if (this.editId) {
			// 编辑模式：从上一页传递数据或从 storage 恢复
			const addrStr = uni.getStorageSync("jd_edit_addr");
			if (addrStr) {
				try {
					const addr = typeof addrStr === "string" ? JSON.parse(addrStr) : addrStr;
					this.form = {
						receiver: addr.receiver || "",
						phone: addr.phone || "",
						province: addr.province || "",
						city: addr.city || "",
						district: addr.district || "",
						detail: addr.detail || "",
						isDefault: addr.isDefault || 0,
					};
				} catch (e) {}
			}
		}
	},
	methods: {
		onDefaultSwitch(e) {
			this.form.isDefault = e.detail.value ? 1 : 0;
		},
		async save() {
			this.errorMsg = "";
			if (!this.form.receiver.trim()) { this.errorMsg = "请输入收货人"; return; }
			if (!/^1\d{10}$/.test(this.form.phone)) { this.errorMsg = "请输入有效手机号"; return; }
			if (!this.form.province.trim() || !this.form.city.trim()) { this.errorMsg = "请填写所在地区"; return; }
			if (!this.form.detail.trim()) { this.errorMsg = "请填写详细地址"; return; }

			this.saving = true;
			const payload = {
				receiver: this.form.receiver.trim(),
				phone: this.form.phone.trim(),
				province: this.form.province.trim(),
				city: this.form.city.trim(),
				district: this.form.district.trim(),
				detail: this.form.detail.trim(),
				isDefault: this.form.isDefault,
			};
			try {
				if (this.isEdit) {
					await updateAddress(this.editId, payload);
				} else {
					await addAddress(payload);
				}
				uni.showToast({ title: this.isEdit ? "已更新" : "已添加", icon: "success" });
				uni.removeStorageSync("jd_edit_addr");
				setTimeout(() => uni.navigateBack(), 800);
			} catch (e) {
				// 2005/2006 由请求层 toast
				this.errorMsg = e?.message || "保存失败";
			} finally { this.saving = false; }
		},
		async doDelete() {
			const res = await new Promise((r) => uni.showModal({ title: "删除地址", content: "确定删除吗？", success: r }));
			if (!res.confirm) return;
			try {
				await deleteAddress(this.editId);
				uni.showToast({ title: "已删除", icon: "success" });
				setTimeout(() => uni.navigateBack(), 800);
			} catch (e) {}
		},
	},
};
</script>

<style scoped lang="scss">
.edit-page { min-height: 100vh; background: #f5f5f5; padding: 32rpx; }
.form-card { background: #fff; border-radius: 12rpx; padding: 32rpx; }
.form-title { font-size: 34rpx; font-weight: bold; color: #333; margin-bottom: 32rpx; }
.form-item { margin-bottom: 24rpx;
	.label { font-size: 28rpx; color: #666; display: block; margin-bottom: 12rpx; }
	.input { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; width: 100%; box-sizing: border-box; }
}
.region-row { display: flex; gap: 16rpx;
	.input-sm { flex: 1; border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; box-sizing: border-box; }
}
.textarea { border: 1rpx solid #ddd; border-radius: 8rpx; padding: 16rpx; font-size: 28rpx; width: 100%; min-height: 120rpx; box-sizing: border-box; }
.error-tips { color: #C81623; font-size: 26rpx; margin-bottom: 24rpx; }
.submit-btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx; font-size: 32rpx; text-align: center; margin-top: 16rpx; &.loading { opacity: 0.6; } }
.delete-btn { text-align: center; color: #C81623; font-size: 28rpx; margin-top: 32rpx; padding: 24rpx; }
</style>