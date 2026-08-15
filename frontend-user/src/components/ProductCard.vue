<template>
  <!-- 商品卡片（首页热销 / 搜索列表共用，京东风格） -->
  <router-link :to="`/product/${product.id}`" class="product-card">
    <div class="card-img-wrap">
      <el-image :src="product.mainImage" fit="cover" class="card-img" lazy>
        <template #error>
          <div class="img-fallback"><el-icon :size="36"><Picture /></el-icon></div>
        </template>
      </el-image>
    </div>
    <div class="card-info">
      <p class="card-title" :title="product.title">{{ product.title }}</p>
      <div class="card-price-row">
        <span class="card-price">¥{{ formatPrice(product.price) }}</span>
        <span v-if="product.originalPrice" class="card-original">¥{{ formatPrice(product.originalPrice) }}</span>
      </div>
      <div class="card-meta">
        <span>已售 {{ product.salesCount ?? 0 }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { formatPrice } from '@/utils/format'

defineProps({
  product: { type: Object, required: true }
})
</script>

<style scoped>
.product-card {
  display: block;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  text-decoration: none;
  transition: box-shadow 0.2s, transform 0.2s;
}
.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}
.card-img-wrap {
  width: 100%;
  aspect-ratio: 1;
  background: #f7f7f7;
}
.card-img {
  width: 100%;
  height: 100%;
}
.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f7f7f7;
}
.card-info {
  padding: 10px 12px 12px;
}
.card-title {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  height: 40px;
  margin: 0 0 6px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.card-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.card-price {
  color: #e60012;
  font-size: 18px;
  font-weight: 700;
}
.card-original {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
}
.card-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}
</style>
