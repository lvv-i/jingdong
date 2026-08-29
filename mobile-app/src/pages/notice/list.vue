<template>
	<view class="notice-page">
		<!-- 未登录 -->
		<view class="empty-box" v-if="!isLogin">
			<view class="big">请先登录</view>
			<button class="btn" @click="goLogin">去登录</button>
		</view>

		<!-- 加载失败 -->
		<view class="empty-box" v-else-if="loadFailed">
			<view class="big">加载失败</view>
			<button class="btn" @click="loadNotices">重新加载</button>
		</view>

		<!-- 空列表 -->
		<view class="empty-box" v-else-if="!list.length && !loading">
			<view class="big">暂无消息</view>
		</view>

		<template v-else>
			<!-- 筛选条：全部 / 未读 / 已读 + 全部已读 -->
			<view class="filter-bar">
				<view class="filter-tabs">
					<view class="ftab" v-for="t in readTabs" :key="t.value" :class="{ active: readFilter === t.value }" @click="switchFilter(t.value)">{{ t.label }}</view>
				</view>
				<view class="all-read" v-if="unreadCount" @click="markAllRead">全部已读</view>
			</view>

			<!-- U-022 通知列表 -->
			<view class="notice-list">
				<view class="notice-item" v-for="n in list" :key="n.id" :class="{ unread: n.readStatus === 0 }" @click="readItem(n)">
					<view class="item-hd">
						<text class="dot" v-if="n.readStatus === 0">●</text>
						<text class="title">{{ n.title }}</text>
						<text class="time">{{ formatDateTime(n.createdAt) }}</text>
					</view>
					<view class="item-body">{{ n.content }}</view>
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
import { getNotifications, markRead, markAllRead } from "../../api/notice";
import { formatDateTime } from "../../utils/format";

const READ_TABS = [
	{ label: "全部", value: "" },
	{ label: "未读", value: "0" },
	{ label: "已读", value: "1" },
];

export default {
	data() {
		return {
			list: [],
			page: 1,
			pageSize: 10,
			total: 0,
			loading: false,
			loadFailed: false,
			readFilter: "",
			readTabs: READ_TABS,
			unreadCount: 0,
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
			this.loadNotices();
		}
	},
	methods: {
		async loadNotices() {
			this.loading = true;
			this.loadFailed = false;
			try {
				const params = { page: this.page, pageSize: this.pageSize };
				if (this.readFilter !== "") params.readStatus = this.readFilter;
				const data = await getNotifications(params, true);
				if (this.page === 1) this.list = data.list || [];
				else this.list = this.list.concat(data.list || []);
				this.total = data.total || 0;
				this.unreadCount = this.list.filter(n => n.readStatus === 0).length;
			} catch (e) {
				if (this.page === 1) this.loadFailed = true;
			} finally { this.loading = false; }
		},
		switchFilter(v) {
			if (this.readFilter === v) return;
			this.readFilter = v;
			this.page = 1;
			this.list = [];
			this.loadNotices();
		},
		loadMore() {
			if (!this.hasMore || this.loading) return;
			this.page++;
			this.loadNotices();
		},
		goLogin() { uni.reLaunch({ url: "/pages/login/login" }); },
		formatDateTime,
		// U-023 标已读
		async readItem(n) {
			if (n.readStatus === 1) return;
			try {
				await markRead(n.id);
				n.readStatus = 1;
				this.unreadCount = Math.max(0, this.unreadCount - 1);
			} catch (e) {}
		},
		// U-025 全部已读
		async markAllRead() {
			try {
				await markAllRead();
				this.list.forEach(n => { n.readStatus = 1; });
				this.unreadCount = 0;
				uni.showToast({ title: "已全部标为已读", icon: "success" });
			} catch (e) {}
		},
	},
};
</script>

<style scoped lang="scss">
.notice-page { min-height: 100vh; background: #f5f5f5; }
.empty-box {
	display: flex; flex-direction: column; align-items: center; padding-top: 200rpx;
	.big { font-size: 34rpx; color: #999; margin-bottom: 40rpx; }
	.btn { background: #C81623; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 60rpx; }
}
.filter-bar {
	display: flex; justify-content: space-between; align-items: center;
	background: #fff; padding: 16rpx 24rpx; border-bottom: 1rpx solid #eee;
	.filter-tabs { display: flex; gap: 32rpx;
		.ftab { font-size: 28rpx; color: #666; }
		.ftab.active { color: #C81623; font-weight: bold; }
	}
	.all-read { font-size: 26rpx; color: #C81623; }
}
.notice-list { padding: 16rpx 0; }
.notice-item {
	background: #fff; margin: 8rpx 24rpx; border-radius: 12rpx; padding: 24rpx;
	&.unread { border-left: 6rpx solid #C81623; }
	.item-hd { display: flex; align-items: center; font-size: 26rpx;
		.dot { color: #C81623; margin-right: 8rpx; }
		.title { flex: 1; font-weight: bold; color: #333; }
		.time { color: #ccc; font-size: 24rpx; }
	}
	.item-body { font-size: 26rpx; color: #666; margin-top: 12rpx; line-height: 1.6; }
}
.pager { text-align: center; padding: 32rpx; font-size: 26rpx;
	.more { color: #C81623; }
	.no-more { color: #ccc; }
}
</style>