<template>
  <!-- B-P01 首页：轮播 + 类目树（P-003）+ 热销推荐（P-004，sort 默认综合） -->
  <div class="home-page">
    <div class="home-layout">
      <!-- 左侧类目树（两级） -->
      <aside class="category-panel">
        <div class="panel-title">全部商品分类</div>
        <div v-if="categoryLoading" class="category-skeleton">加载中...</div>
        <template v-else-if="categories.length">
          <div v-for="top in categories" :key="top.id" class="category-group">
            <div class="top-cat" @click="goCategory(top.id, top.name)">
              {{ top.name }}
            </div>
            <div v-if="top.children?.length" class="sub-cats">
              <span
                v-for="sub in top.children"
                :key="sub.id"
                class="sub-cat"
                @click="goCategory(sub.id, sub.name)"
              >{{ sub.name }}</span>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无类目" :image-size="60" />
      </aside>

      <!-- 右侧：轮播 + 热销推荐 -->
      <section class="main-panel">
        <el-carousel v-if="carouselImages.length" height="220px" class="home-carousel">
          <el-carousel-item v-for="(img, i) in carouselImages" :key="i">
            <el-image :src="img" fit="cover" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>

        <div class="hot-section">
          <div class="section-header">
            <h2 class="section-title">热销推荐</h2>
            <router-link to="/search" class="more-link">查看更多</router-link>
          </div>

          <div v-if="loading" class="grid">
            <el-skeleton v-for="i in 8" :key="i" animated>
              <template #template>
                <el-skeleton-item variant="image" class="skeleton-img" />
                <el-skeleton-item variant="text" style="width: 90%" />
                <el-skeleton-item variant="text" style="width: 50%" />
              </template>
            </el-skeleton>
          </div>
          <div v-else-if="products.length" class="grid">
            <ProductCard v-for="p in products" :key="p.id" :product="p" />
          </div>
          <EmptyState v-else title="暂无商品" description="商品即将上架，敬请期待" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getProducts } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const categories = ref([])
const categoryLoading = ref(true)
const products = ref([])
const loading = ref(true)
const carouselImages = ref([])

/** P-003 类目树：仅 ENABLED，组装两级结构 */
async function loadCategories() {
  try {
    const data = await getCategories({ silent: true })
    const list = data.list || []
    // 顶级类目（parentId === 0）下挂子类目
    categories.value = list
      .filter((c) => c.parentId === 0)
      .map((top) => ({
        ...top,
        children: list.filter((c) => c.parentId === top.id)
      }))
  } catch {
    /* 后端未部署/加载失败时展示空状态 */
  } finally {
    categoryLoading.value = false
  }
}

/** P-004 热销推荐：默认 sort（综合） */
async function loadProducts() {
  try {
    const data = await getProducts({ page: 1, pageSize: 8 }, { silent: true })
    products.value = data.list || []
    // 轮播取前 5 张主图
    carouselImages.value = products.value.slice(0, 5).map((p) => p.mainImage).filter(Boolean)
  } catch {
    /* 后端未部署/加载失败时展示空状态 */
  } finally {
    loading.value = false
  }
}

function goCategory(id, name) {
  router.push({ path: '/search', query: { categoryId: id, categoryName: name } })
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.home-layout {
  display: flex;
  gap: 16px;
}
.category-panel {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 6px;
  padding: 12px 0;
}
.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: #333;
  padding: 0 12px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.category-skeleton {
  padding: 12px;
  color: #909399;
  font-size: 13px;
}
.category-group {
  padding: 6px 12px;
}
.top-cat {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  padding: 4px 0;
}
.top-cat:hover {
  color: #e60012;
}
.sub-cats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px 0 8px;
}
.sub-cat {
  font-size: 12px;
  color: #666;
  cursor: pointer;
}
.sub-cat:hover {
  color: #e60012;
}
.main-panel {
  flex: 1;
  min-width: 0;
}
.home-carousel {
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 16px;
}
.carousel-img {
  width: 100%;
  height: 100%;
}
.hot-section {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.section-title {
  font-size: 18px;
  color: #333;
  margin: 0;
  border-left: 4px solid #e60012;
  padding-left: 10px;
}
.more-link {
  font-size: 13px;
  color: #909399;
  text-decoration: none;
}
.more-link:hover {
  color: #e60012;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.skeleton-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 6px;
}
</style>
