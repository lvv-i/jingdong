<template>
	<!-- D-P01 首页：搜索入口 + 类目宫格（P-003）+ 热销商品（P-004 sort=sales） -->
	<view class="page-container">
		<!-- 搜索入口：跳分类页并聚焦搜索（D-01 无独立搜索页） -->
		<view class="search-bar" @click="goSearch">
			<text class="search-icon">🔍</text>
			<text class="search-text">搜索商品</text>
		</view>

		<!-- 类目入口（P-003 顶级类目，前 8 个） -->
		<view v-if="topCategories.length" class="cate-grid">
			<view
				v-for="c in topCategories"
				:key="c.id"
				class="cate-item"
				@click="goCategory(c)"
			>
				<text class="cate-name">{{ c.name }}</text>
			</view>
		</view>
		<view v-else-if="!cateLoading" class="empty-tip">
			<text>暂无类目</text>
		</view>

		<!-- 热销商品（P-004 sort=sales 分页） -->
		<view class="section-head">
			<text class="sh-title">热销商品</text>
		</view>
		<view v-if="products.length" class="product-list">
			<product-card v-for="p in products" :key="p.id" :product="p" />
		</view>
		<view v-else-if="!loading" class="empty-tip">
			<text>暂无商品，敬请期待</text>
		</view>

		<!-- 加载状态 -->
		<view v-if="loading" class="load-tip">加载中…</view>
		<view v-else-if="finished && products.length" class="load-tip">没有更多了</view>
	</view>
</template>

<script>
import ProductCard from "../../components/product-card/product-card.vue";
import { getCategories, getProducts } from "../../api/product";

const PAGE_SIZE = 10;

export default {
	components: { ProductCard },
	data() {
		return {
			cateLoading: true,
			topCategories: [],
			products: [],
			loading: false,
			finished: false,
			page: 1,
		};
	},
	onLoad() {
		this.loadCategories();
		this.loadProducts(true);
	},
	// 触底加载更多（商品分页）
	onReachBottom() {
		this.loadProducts(false);
	},
	methods: {
		/** P-003 类目树（取顶级类目前 8 个） */
		async loadCategories() {
			try {
				const data = await getCategories(true);
				const list = data?.list || [];
				this.topCategories = list
					.filter((c) => c.parentId === 0)
					.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
					.slice(0, 8);
			} catch {
				this.topCategories = [];
			} finally {
				this.cateLoading = false;
			}
		},
		/** P-004 热销商品列表（sort=sales，触底分页） */
		async loadProducts(reset) {
			if (this.loading || (this.finished && !reset)) return;
			this.loading = true;
			try {
				const params = { page: reset ? 1 : this.page, pageSize: PAGE_SIZE, sort: "sales" };
				const data = await getProducts(params, true);
				const list = data?.list || [];
				const total = data?.total || 0;
				if (reset) {
					this.products = list;
					this.page = 1;
				} else {
					this.products = this.products.concat(list);
				}
				this.page += 1;
				this.finished = this.products.length >= total;
			} catch {
				if (reset) this.products = [];
			} finally {
				this.loading = false;
			}
		},
		goSearch() {
			uni.switchTab({ url: "/pages/category/category" });
			// 聚焦搜索（分类页 onShow 读取 focusSearch 标记）
			uni.setStorageSync("jd_focus_search", Date.now());
		},
		goCategory(c) {
			uni.setStorageSync("jd_pick_category", { id: c.id, name: c.name });
			uni.switchTab({ url: "/pages/category/category" });
		},
	},
};
</script>

<style lang="scss" scoped>
.search-bar {
	margin: 20rpx;
	padding: 0 28rpx;
	height: 72rpx;
	line-height: 72rpx;
	background: #fff;
	border-radius: 36rpx;
	border: 2rpx solid #c81623;
	display: flex;
	align-items: center;
	gap: 12rpx;

	.search-icon {
		font-size: 28rpx;
	}
	.search-text {
		color: #999;
		font-size: 26rpx;
	}
}
.cate-grid {
	display: flex;
	flex-wrap: wrap;
	margin: 0 20rpx 20rpx;
	background: #fff;
	border-radius: 12rpx;
	padding: 24rpx 0 8rpx;

	.cate-item {
		width: 25%;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 12rpx 0 20rpx;

		.cate-name {
			font-size: 26rpx;
			color: #333;
		}
	}
}
.section-head {
	padding: 20rpx 24rpx 12rpx;

	.sh-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #333;
		border-left: 8rpx solid #c81623;
		padding-left: 16rpx;
	}
}
.product-list {
	padding: 0 20rpx;
}
.empty-tip {
	padding: 60rpx 0;
	text-align: center;
	color: #999;
	font-size: 26rpx;
}
.load-tip {
	padding: 24rpx 0 40rpx;
	text-align: center;
	color: #999;
	font-size: 24rpx;
}
</style>
