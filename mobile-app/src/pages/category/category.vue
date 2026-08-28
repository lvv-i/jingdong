<template>
	<!-- D-P02 分类页：类目树导航（P-003）+ 商品列表（P-004，categoryId/keyword/sort） -->
	<view class="cate-page">
		<!-- 搜索栏（首页入口跳转聚焦） -->
		<view class="search-bar">
			<input
				v-model="keyword"
				class="search-input"
				confirm-type="search"
				placeholder="搜索商品"
				@confirm="handleSearch"
			/>
			<text class="search-btn" @click="handleSearch">搜索</text>
		</view>

		<view class="cate-body">
			<!-- 左侧类目树 -->
			<scroll-view class="cate-tree" scroll-y>
				<view
					v-for="c in topCategories"
					:key="c.id"
					class="tree-item"
					:class="{ active: activeTop === c.id && !activeSub }"
					@click="selectTop(c)"
				>
					{{ c.name }}
				</view>
			</scroll-view>

			<!-- 右侧：子类目 + 商品列表 -->
			<view class="cate-content">
				<!-- 子类目标签 -->
				<view v-if="subCategories.length" class="sub-row">
					<view
						class="sub-item"
						:class="{ active: !activeSub }"
						@click="selectSub(null)"
					>
						全部
					</view>
					<view
						v-for="s in subCategories"
						:key="s.id"
						class="sub-item"
						:class="{ active: activeSub === s.id }"
						@click="selectSub(s)"
					>
						{{ s.name }}
					</view>
				</view>

				<!-- 排序切换：综合/销量/价格升/价格降（P-004 sort） -->
				<view class="sort-row">
					<view
						v-for="s in SORTS"
						:key="s.value"
						class="sort-item"
						:class="{ active: sort === s.value }"
						@click="changeSort(s.value)"
					>
						{{ s.label }}
					</view>
				</view>

				<!-- 商品列表 -->
				<scroll-view class="product-scroll" scroll-y @scrolltolower="loadMore">
					<view v-if="products.length" class="product-list">
						<product-card v-for="p in products" :key="p.id" :product="p" />
					</view>
					<view v-else-if="!loading" class="empty-tip">
						<text>{{ keyword ? "没有找到相关商品" : "该分类暂无商品" }}</text>
					</view>
					<view v-if="loading" class="load-tip">加载中…</view>
					<view v-else-if="finished && products.length" class="load-tip">没有更多了</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script>
import ProductCard from "../../components/product-card/product-card.vue";
import { getCategories, getProducts } from "../../api/product";

const PAGE_SIZE = 10;

const SORTS = [
	{ label: "综合", value: "" },
	{ label: "销量", value: "sales" },
	{ label: "价格↑", value: "priceAsc" },
	{ label: "价格↓", value: "priceDesc" },
];

export default {
	components: { ProductCard },
	data() {
		return {
			SORTS,
			topCategories: [],
			subCategories: [],
			activeTop: null,
			activeSub: null,
			keyword: "",
			sort: "",
			products: [],
			loading: false,
			finished: false,
			page: 1,
		};
	},
	onLoad() {
		this.loadCategories();
	},
	onShow() {
		// 首页跳转带入：类目预选 + 搜索聚焦（storage 一次性消费）
		const picked = uni.getStorageSync("jd_pick_category");
		if (picked && picked.id) {
			uni.removeStorageSync("jd_pick_category");
			this.activeTop = picked.id;
			// 类目树已加载时同步子类目与商品；未加载则等待 loadCategories 完成后驱动
			if (this.topCategories.length) {
				this.selectTop({ id: picked.id });
			}
		}
		const focusTs = uni.getStorageSync("jd_focus_search");
		if (focusTs) {
			uni.removeStorageSync("jd_focus_search");
		}
	},
	methods: {
		/** P-003 类目树：顶级 + 子级分组 */
		async loadCategories() {
			try {
				const data = await getCategories(true);
				const list = data?.list || [];
				this.topCategories = list
					.filter((c) => c.parentId === 0)
					.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
				this.allCategories = list;
				// 默认选中第一个顶级类目（驱动首次商品加载；若首页带入已选中则保持）
				const picked = this.activeTop;
				if (picked && this.allCategories.some((x) => x.id === picked)) {
					this.selectTop({ id: picked });
				} else if (this.topCategories.length) {
					this.selectTop(this.topCategories[0]);
				}
			} catch {
				this.topCategories = [];
				this.allCategories = [];
			}
		},
		selectTop(c, keepList) {
			this.activeTop = c.id;
			this.activeSub = null;
			this.subCategories = (this.allCategories || []).filter(
				(x) => x.parentId === c.id
			);
			if (!keepList) this.loadProducts(true);
		},
		selectSub(s) {
			this.activeSub = s ? s.id : null;
			this.loadProducts(true);
		},
		changeSort(v) {
			this.sort = v;
			this.loadProducts(true);
		},
		handleSearch() {
			this.loadProducts(true);
		},
		/** P-004 商品列表（categoryId=选中子类目或顶级；keyword；sort） */
		async loadProducts(reset) {
			if (this.loading || (this.finished && !reset)) return;
			this.loading = true;
			try {
				const categoryId = this.activeSub || this.activeTop;
				const params = {
					page: reset ? 1 : this.page,
					pageSize: PAGE_SIZE,
					sort: this.sort || undefined,
					keyword: this.keyword.trim() || undefined,
				};
				if (categoryId) params.categoryId = categoryId;
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
		loadMore() {
			this.loadProducts(false);
		},
	},
};
</script>

<style lang="scss" scoped>
.cate-page {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #f5f5f5;
}
.search-bar {
	margin: 16rpx 20rpx;
	display: flex;
	align-items: center;
	background: #fff;
	border-radius: 36rpx;
	border: 2rpx solid #c81623;
	overflow: hidden;

	.search-input {
		flex: 1;
		height: 68rpx;
		padding: 0 24rpx;
		font-size: 26rpx;
	}
	.search-btn {
		padding: 0 32rpx;
		color: #c81623;
		font-size: 28rpx;
		font-weight: 600;
	}
}
.cate-body {
	flex: 1;
	display: flex;
	overflow: hidden;
}
.cate-tree {
	width: 176rpx;
	background: #fff;
	height: 100%;

	.tree-item {
		padding: 28rpx 12rpx;
		font-size: 26rpx;
		color: #333;
		text-align: center;

		&.active {
			background: #f5f5f5;
			color: #c81623;
			font-weight: 700;
			border-left: 6rpx solid #c81623;
		}
	}
}
.cate-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	padding: 0 16rpx;
}
.sub-row {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	padding: 12rpx 4rpx;

	.sub-item {
		padding: 8rpx 20rpx;
		background: #fff;
		border-radius: 24rpx;
		font-size: 24rpx;
		color: #666;

		&.active {
			background: #c81623;
			color: #fff;
		}
	}
}
.sort-row {
	display: flex;
	background: #fff;
	border-radius: 8rpx;
	margin: 8rpx 0;
	padding: 8rpx 0;

	.sort-item {
		flex: 1;
		text-align: center;
		font-size: 24rpx;
		color: #666;
		padding: 8rpx 0;

		&.active {
			color: #c81623;
			font-weight: 700;
		}
	}
}
.product-scroll {
	flex: 1;
	height: 0;
}
.product-list {
	padding-top: 8rpx;
}
.empty-tip {
	padding: 100rpx 0;
	text-align: center;
	color: #999;
	font-size: 26rpx;
}
.load-tip {
	padding: 20rpx 0 40rpx;
	text-align: center;
	color: #999;
	font-size: 24rpx;
}
</style>
