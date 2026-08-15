<template>
  <!-- B-P03 商品详情：主图轮播 + 详情 + 加购（登录后可见）+ 评价列表（P-005/P-006） -->
  <div class="detail-page">
    <!-- 3001 商品不存在 / 3002 非 ON_SALE -->
    <div v-if="notFound" class="error-block">
      <el-result icon="warning" :title="notFoundTitle" :sub-title="notFoundDesc">
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <div class="detail-top">
        <!-- 左侧主图轮播 -->
        <div class="gallery">
          <el-carousel v-if="galleryImages.length" height="420px" trigger="click" arrow="always">
            <el-carousel-item v-for="(img, i) in galleryImages" :key="i">
              <el-image :src="img" fit="contain" class="gallery-img" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="gallery-empty">
            <el-icon :size="48"><Picture /></el-icon>
          </div>
        </div>

        <!-- 右侧商品信息 -->
        <div class="info-panel">
          <h1 class="p-title">{{ product.title }}</h1>
          <p v-if="product.subTitle" class="p-subtitle">{{ product.subTitle }}</p>

          <div class="price-box">
            <div class="price-row">
              <span class="price-label">京选价</span>
              <span class="price-now">¥{{ formatPrice(product.price) }}</span>
              <span v-if="product.originalPrice" class="price-original">
                ¥{{ formatPrice(product.originalPrice) }}
              </span>
            </div>
            <div class="meta-row">
              <span>库存 {{ product.stock ?? 0 }} 件</span>
              <span class="divider">|</span>
              <span>已售 {{ product.salesCount ?? 0 }}</span>
              <span v-if="product.merchantName" class="divider">|</span>
              <span v-if="product.merchantName">店铺：{{ product.merchantName }}</span>
            </div>
          </div>

          <div class="buy-row">
            <span class="buy-label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="999" />
          </div>

          <div class="action-row">
            <!-- 加购按钮：登录后可见（T5 U-009 需登录） -->
            <el-button
              v-if="userStore.isLoggedIn"
              type="danger"
              size="large"
              class="add-cart-btn"
              :loading="adding"
              @click="handleAddCart"
            >
              加入购物车
            </el-button>
            <el-button
              v-else
              type="danger"
              size="large"
              class="add-cart-btn"
              @click="goLogin"
            >
              登录后加入购物车
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品详情（富文本） -->
      <div class="detail-section">
        <div class="section-tab">商品详情</div>
        <div v-if="product.detail" class="detail-html" v-html="product.detail"></div>
        <EmptyState v-else title="暂无详情" />
      </div>

      <!-- 评价列表（P-006，userName 已脱敏） -->
      <div class="review-section">
        <div class="section-tab">商品评价（{{ reviewTotal }}）</div>
        <div v-if="reviewLoading" class="review-loading">
          <el-skeleton :rows="3" animated />
        </div>
        <template v-else-if="reviews.length">
          <div v-for="r in reviews" :key="r.reviewedAt + r.userName" class="review-item">
            <div class="review-head">
              <span class="review-user">{{ r.userName }}</span>
              <el-rate :model-value="r.rating" disabled />
              <span class="review-time">{{ formatDateTime(r.reviewedAt) }}</span>
            </div>
            <p class="review-comment">{{ r.comment }}</p>
          </div>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="reviewPage"
              :page-size="reviewPageSize"
              :total="reviewTotal"
              layout="prev, pager, next"
              background
              @current-change="loadReviews"
            />
          </div>
        </template>
        <EmptyState v-else title="暂无评价" desc="购买后评价，分享你的体验" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail, getProductReviews } from '@/api/product'
import { addCartItem } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import { formatPrice, formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const productId = computed(() => Number(route.params.id))
const product = ref({})
const notFound = ref(false)
const notFoundTitle = ref('')
const notFoundDesc = ref('')

const quantity = ref(1)
const adding = ref(false)

const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewPageSize = ref(5)
const reviewLoading = ref(false)

/** 轮播图：多图优先，缺省主图 */
const galleryImages = computed(() => {
  if (product.value.images?.length) return product.value.images
  return product.value.mainImage ? [product.value.mainImage] : []
})

/** P-005 商品详情；3001 不存在 / 3002 非 ON_SALE */
async function loadDetail() {
  notFound.value = false
  try {
    const data = await getProductDetail(productId.value, { silent: true })
    product.value = data || {}
  } catch (err) {
    if (err.code === 3001) {
      notFoundTitle.value = '商品不存在'
      notFoundDesc.value = '该商品可能已下架或删除'
    } else if (err.code === 3002) {
      notFoundTitle.value = '商品已下架'
      notFoundDesc.value = '该商品当前不在售，看看其他商品吧'
    } else {
      notFoundTitle.value = '加载失败'
      notFoundDesc.value = err.message || '请稍后重试'
    }
    notFound.value = true
  }
}

/** P-006 评价列表 */
async function loadReviews() {
  reviewLoading.value = true
  try {
    const data = await getProductReviews(
      productId.value,
      { page: reviewPage.value, pageSize: reviewPageSize.value },
      { silent: true }
    )
    reviews.value = data.list || []
    reviewTotal.value = data.total || 0
  } finally {
    reviewLoading.value = false
  }
}

/** U-009 加入购物车（仅 ON_SALE；3002 提示） */
async function handleAddCart() {
  adding.value = true
  try {
    await addCartItem({ productId: productId.value, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } finally {
    adding.value = false
  }
}

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

watch(productId, () => {
  loadDetail()
  loadReviews()
})

onMounted(() => {
  loadDetail()
  loadReviews()
})
</script>

<style scoped>
.error-block {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
}
.detail-top {
  display: flex;
  gap: 24px;
  background: #fff;
  border-radius: 6px;
  padding: 24px;
  margin-bottom: 16px;
}
.gallery {
  width: 420px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: #f7f7f7;
}
.gallery-img {
  width: 100%;
  height: 100%;
}
.gallery-empty {
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}
.info-panel {
  flex: 1;
  min-width: 0;
}
.p-title {
  font-size: 22px;
  color: #333;
  margin: 0 0 8px;
  line-height: 1.4;
}
.p-subtitle {
  color: #999;
  font-size: 14px;
  margin: 0 0 16px;
}
.price-box {
  background: #fff5f5;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.price-label {
  font-size: 13px;
  color: #999;
}
.price-now {
  color: #e60012;
  font-size: 30px;
  font-weight: 700;
}
.price-original {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
}
.meta-row {
  margin-top: 10px;
  font-size: 13px;
  color: #666;
  display: flex;
  gap: 8px;
}
.divider {
  color: #ddd;
}
.buy-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.buy-label {
  font-size: 14px;
  color: #666;
}
.add-cart-btn {
  width: 220px;
}
.detail-section,
.review-section {
  background: #fff;
  border-radius: 6px;
  padding: 20px 24px;
  margin-bottom: 16px;
}
.section-tab {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  border-left: 4px solid #e60012;
  padding-left: 10px;
  margin-bottom: 16px;
}
.detail-html {
  color: #555;
  font-size: 14px;
  line-height: 1.8;
}
.review-loading {
  padding: 12px 0;
}
.review-item {
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.review-user {
  font-size: 13px;
  color: #999;
}
.review-time {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}
.review-comment {
  font-size: 14px;
  color: #333;
  margin: 0;
  line-height: 1.6;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 16px;
}
</style>
