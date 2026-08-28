<template>
  <!-- B-P05 购物车：列表 U-008、改数量/勾选 U-010、删除 U-011、合计去结算 -->
  <div class="cart-page">
    <div class="cart-header">
      <h2>我的购物车</h2>
    </div>

    <div v-if="loading" class="cart-panel">
      <el-skeleton :rows="4" animated />
    </div>

    <div v-else-if="items.length" class="cart-panel">
      <!-- 表头 -->
      <div class="cart-row cart-row-head">
        <div class="col-check">
          <el-checkbox
            :model-value="allSelected"
            :indeterminate="someSelected"
            @change="handleSelectAll"
          />
          <span>全选</span>
        </div>
        <div class="col-item">商品信息</div>
        <div class="col-price">单价</div>
        <div class="col-quantity">数量</div>
        <div class="col-subtotal">小计</div>
        <div class="col-action">操作</div>
      </div>

      <!-- 商品行 -->
      <div v-for="item in items" :key="item.id" class="cart-row">
        <div class="col-check">
          <el-checkbox
            :model-value="item.selected === 1"
            @change="(val) => handleSelectOne(item, val)"
          />
        </div>
        <div class="col-item">
          <div class="item-title" @click="goDetail(item.productId)">{{ item.title }}</div>
          <div v-if="item.quantity > item.stock" class="item-warn">库存仅 {{ item.stock }} 件</div>
        </div>
        <div class="col-price">¥{{ formatPrice(item.price) }}</div>
        <div class="col-quantity">
          <el-input-number
            :model-value="item.quantity"
            :min="1"
            :max="999"
            size="small"
            @change="(val) => handleQuantity(item, val)"
          />
        </div>
        <div class="col-subtotal">¥{{ formatPrice(item.price * item.quantity) }}</div>
        <div class="col-action">
          <el-button link type="danger" :loading="item.deleting" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="cart-footer">
        <div class="col-check">
          <el-checkbox
            :model-value="allSelected"
            :indeterminate="someSelected"
            @change="handleSelectAll"
          />
          <span>全选</span>
        </div>
        <div class="footer-summary">
          <span>已选 <b class="hl">{{ selectedCount }}</b> 件</span>
          <span class="summary-total">
            合计：<b class="total-amount">¥{{ formatPrice(selectedAmount) }}</b>
          </span>
        </div>
        <el-button
          type="danger"
          size="large"
          :disabled="selectedCount === 0"
          @click="goCheckout"
        >
          去结算
        </el-button>
      </div>
    </div>

    <EmptyState v-else title="购物车还是空的" desc="去挑选喜欢的商品吧">
      <el-button type="danger" @click="$router.push('/')">去逛逛</el-button>
    </EmptyState>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartItems, updateCartItem, deleteCartItem } from '@/api/cart'
import { formatPrice } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const items = ref([])
const loading = ref(true)

const allSelected = computed(() => items.value.length > 0 && items.value.every((i) => i.selected === 1))
const someSelected = computed(() => items.value.some((i) => i.selected === 1) && !allSelected.value)
const selectedCount = computed(() =>
  items.value.filter((i) => i.selected === 1).reduce((s, i) => s + i.quantity, 0)
)
const selectedAmount = computed(() =>
  items.value.filter((i) => i.selected === 1).reduce((s, i) => s + i.price * i.quantity, 0)
)

/** U-008 购物车列表 */
async function loadCart() {
  loading.value = true
  try {
    items.value = await getCartItems({ silent: true })
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

/** U-010 勾选（单个） */
async function handleSelectOne(item, val) {
  try {
    await updateCartItem(item.id, { selected: val ? 1 : 0 })
    item.selected = val ? 1 : 0
  } catch {
    /* 失败保持原状（toast 已由全局拦截器弹出） */
  }
}

/** U-010 全选/取消全选 */
async function handleSelectAll(val) {
  const targets = val ? items.value.filter((i) => i.selected === 0) : items.value.filter((i) => i.selected === 1)
  try {
    await Promise.all(targets.map((i) => updateCartItem(i.id, { selected: val ? 1 : 0 })))
    items.value.forEach((i) => (i.selected = val ? 1 : 0))
  } catch {
    loadCart()
  }
}

/** U-010 改数量（接口失败回退刷新） */
async function handleQuantity(item, val) {
  if (!val) return
  try {
    await updateCartItem(item.id, { quantity: val })
    item.quantity = val
  } catch {
    loadCart()
  }
}

/** U-011 删除（二次确认） */
async function handleDelete(item) {
  try {
    await ElMessageBox.confirm(`确定删除「${item.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  item.deleting = true
  try {
    await deleteCartItem(item.id)
    items.value = items.value.filter((i) => i.id !== item.id)
    ElMessage.success('已删除')
  } finally {
    item.deleting = false
  }
}

function goDetail(productId) {
  router.push(`/product/${productId}`)
}

function goCheckout() {
  router.push('/checkout')
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
}
.cart-header {
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.cart-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0 0 12px;
  border-left: 4px solid #e60012;
  padding-left: 10px;
}
.cart-row {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.cart-row-head {
  color: #999;
  font-size: 13px;
  padding: 8px 0;
}
.col-check {
  width: 90px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.col-item {
  flex: 1;
  min-width: 0;
}
.item-title {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-title:hover {
  color: #e60012;
}
.item-warn {
  margin-top: 4px;
  font-size: 12px;
  color: #e6a23c;
}
.col-price,
.col-subtotal {
  width: 120px;
  color: #333;
  font-size: 14px;
}
.col-subtotal {
  color: #e60012;
  font-weight: 700;
}
.col-quantity {
  width: 140px;
}
.col-action {
  width: 80px;
  text-align: center;
}
.cart-footer {
  display: flex;
  align-items: center;
  padding: 14px 0 4px;
}
.footer-summary {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 13px;
  color: #666;
}
.hl {
  color: #e60012;
}
.total-amount {
  color: #e60012;
  font-size: 20px;
}
</style>
