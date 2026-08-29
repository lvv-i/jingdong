<template>
	<view class="address-page">
		<!-- 加载失败 -->
		<view class="empty-box" v-if="loadFailed">
			<view class="big">加载失败</view>
			<button class="btn" @click="loadAddr">重新加载</button>
		</view>

		<!-- 空列表 -->
		<view class="empty-box" v-else-if="!list.length && !loading">
			<view class="big">暂无收货地址</view>
			<view class="sub">去添加一个吧</view>
		</view>

		<template v-else>
			<!-- U-003 地址列表 -->
			<view class="addr-list">
				<view class="addr-card" v-for="a in list" :key="a.id" @click="selectAddr(a)">
					<view class="card-top">
						<view class="receiver">
							<text class="name">{{ a.receiver }}</text>
							<text class="phone">{{ a.phone }}</text>
						</view>
						<text class="default-tag" v-if="a.isDefault === 1">默认</text>
					</view>
					<view class="addr-text">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</view>
					<view class="card-ops">
						<text class="op" v-if="a.isDefault !== 1" @click.stop="setDefault(a)">设为默认</text>
						<text class="op" @click.stop="editAddr(a)">编辑</text>
						<text class="op del" @click.stop="delAddr(a)">删除</text>
					</view>
				</view>
			</view>
		</template>

		<!-- 底部新增按钮 -->
		<view class="bottom-bar">
			<button class="add-btn" @click="editAddr()">+ 新增地址</button>
		</view>
	</view>
</template>

<script>
import { getAddresses, setDefaultAddress, deleteAddress } from "../../api/address";

export default {
	data() {
		return {
			list: [],
			loading: false,
			loadFailed: false,
		};
	},
	onLoad(options) {
		this.selectMode = options?.select === "1";
	},
	onShow() {
		this.loadAddr();
	},
	methods: {
		async loadAddr() {
			this.loading = true;
			this.loadFailed = false;
			try {
				const data = await getAddresses(true);
				// U-003 T5 契约：data 为 {list, total}（后端已对齐）
				this.list = (data && data.list) || [];
			} catch (e) {
				this.loadFailed = true;
			} finally { this.loading = false; }
		},
		selectAddr(a) {
			if (this.selectMode) {
				// 结算页选择地址模式：回传选中的地址 id
				const pages = getCurrentPages();
				const prev = pages[pages.length - 2];
				if (prev && prev.$vm && prev.$vm.onAddressSelected) {
					prev.$vm.onAddressSelected(a);
					uni.navigateBack();
				}
			}
		},
		editAddr(a) {
			const id = a ? a.id : "";
			uni.navigateTo({ url: `/pages/mine/address-edit?id=${id}` });
		},
		async setDefault(a) {
			try {
				await setDefaultAddress(a.id);
				uni.showToast({ title: "已设为默认", icon: "success" });
				this.loadAddr();
			} catch (e) {}
		},
		async delAddr(a) {
			const res = await new Promise((r) => uni.showModal({ title: "删除地址", content: `确定删除「${a.receiver}」的地址吗？`, success: r }));
			if (!res.confirm) return;
			try {
				await deleteAddress(a.id);
				uni.showToast({ title: "已删除", icon: "success" });
				this.loadAddr();
			} catch (e) {}
		},
	},
};
</script>

<style scoped lang="scss">
.address-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 120rpx; }
.empty-box { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx;
	.big { font-size: 34rpx; color: #999; margin-bottom: 16rpx; }
	.sub { font-size: 26rpx; color: #ccc; margin-bottom: 40rpx; }
	.btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 60rpx; }
}
.addr-card { background: #fff; margin: 16rpx 24rpx; border-radius: 12rpx; padding: 24rpx; }
.card-top { display: flex; justify-content: space-between; align-items: center;
	.receiver { display: flex; align-items: center; gap: 16rpx;
		.name { font-size: 30rpx; font-weight: bold; color: #333; }
		.phone { font-size: 28rpx; color: #666; }
	}
	.default-tag { background: #C81623; color: #fff; font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 4rpx; }
}
.addr-text { font-size: 28rpx; color: #333; margin-top: 12rpx; line-height: 1.5; }
.card-ops { display: flex; justify-content: flex-end; gap: 24rpx; margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx solid #f5f5f5;
	.op { font-size: 26rpx; color: #666; }
	.op.del { color: #C81623; }
}
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 20rpx 32rpx; padding-bottom: calc(20rpx + env(safe-area-inset-bottom)); border-top: 1rpx solid #eee; }
.add-btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx; font-size: 32rpx; width: 100%; }
</style>