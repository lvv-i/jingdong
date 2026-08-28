<template>
  <!-- B-P02 搜索/列表页：keyword + categoryId 筛选、sort 四选项、分页（P-004） -->
  <div class="search-page">
    <div class="search-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品"
        class="keyword-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="SearchIcon" @click="handleSearch" />
        </template>
      </el-input>

      <el-select
        v-model="categoryId"
        placeholder="全部类目"
        clearable
        class="category-select"
        @change="handleCategoryChange"
      >
        <el-option-group v-for="top in categories" :key="top.id" :label="top.name">
          <el-option v-for="sub in top.children" :key="sub.id" :label="sub.name" :value="sub.id" />
        </el-option-group>
      </el-select>
    </div>

    <div class="sort-bar">
      <el-radio-group v-model="sort" @change="handleSortChange">
        <el-radio-button value="">综合</el-radio-button>
        <el-radio-button value="sales">销量</el-radio-button>
        <el-radio-button value="priceAsc">价格升</el-radio-button>
        <el-radio-button value="priceDesc">价格降</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="loading" class="result-grid">
      <el-skeleton v-for="i in 8" :key="i" animated>
        <template #template>
          <el-skeleton-item variant="image" class="skeleton-img" />
          <el-skeleton-item variant="text" style="width: 90%" />
          <el-skeleton-item variant="text" style="width: 50%" />
        </template>
      </el-skeleton>
    </div>
    <div v-else-if="products.length" class="result-grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
    <EmptyState
      v-else
      title="没有找到相关商品"
      description="换个关键词或类目试试"
      class="empty-block"
    />

    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getCategories, getProducts } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

const SearchIcon = Search
const categories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const sort = ref('')
const products = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(8)
const loading = ref(true)

/** 从 URL query 初始化（支持首页类目入口 / 顶部搜索跳入） */
function initFromQuery() {
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId ? Number(route.query.categoryId) : null
  sort.value = route.query.sort || ''
  page.value = Number(route.query.page) || 1
}

/** 同步当前筛选状态到 URL（保持可分享/刷新不丢） */
function syncQuery() {
  const query = {}
  if (keyword.value) query.keyword = keyword.value
  if (categoryId.value) query.categoryId = categoryId.value
  if (sort.value) query.sort = sort.value
  if (page.value > 1) query.page = page.value
  router.replace({ path: '/search', query })
}

/** P-004 商品列表 */
async function loadProducts() {
  loading.value = true
  try {
    const data = await getProducts(
      {
        page: page.value,
        pageSize: pageSize.value,
        categoryId: categoryId.value || undefined,
        keyword: keyword.value || undefined,
        sort: sort.value || undefined
      },
      { silent: true }
    )
    products.value = data.list || []
    total.value = data.total || 0
  } catch {
    /* 后端未部署/加载失败时展示空状态 */
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  syncQuery()
  loadProducts()
}

function handleSortChange() {
  page.value = 1
  syncQuery()
  loadProducts()
}

function handleCategoryChange() {
  page.value = 1
  syncQuery()
  loadProducts()
}

/** P-003 类目树（筛选下拉分组用） */
async function loadCategories() {
  try {
    const data = await getCategories({ silent: true })
    const list = data.list || []
    categories.value = list
      .filter((c) => c.parentId === 0)
      .map((top) => ({
        ...top,
        children: list.filter((c) => c.parentId === top.id)
      }))
  } catch {
    /* 类目加载失败不影响商品列表 */
  }
}

onMounted(() => {
  initFromQuery()
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.search-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.keyword-input {
  flex: 1;
  max-width: 480px;
}
.category-select {
  width: 200px;
}
.sort-bar {
  background: #fff;
  border-radius: 6px;
  padding: 10px 16px;
  margin-bottom: 14px;
}
.result-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.skeleton-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 6px;
}
.empty-block {
  background: #fff;
  border-radius: 6px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
</style>
