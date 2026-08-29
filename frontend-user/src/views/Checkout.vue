<template>
  <!-- B-P06 结算页：地址选择 U-003 + 勾选项快照核对 + 备注 + 提交订单 U-012 -->
  <div class="checkout-page">
    <!-- 收货地址（U-003） -->
    <section class="panel">
      <div class="panel-title">收货地址</div>
      <div v-if="addressLoading" class="panel-body">
        <el-skeleton :rows="2" animated />
      </div>
      <div v-else-if="addresses.length" class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-item"
          :class="{ active: addressId === addr.id }"
          @click="addressId = addr.id"
        >
          <div class="addr-main">
            <span class="addr-receiver">{{ addr.receiver }}</span>
            <span class="addr-phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.isDefault === 1" size="small" type="danger" effect="plain">默认</el-tag>
          </div>
          <div class="addr-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
        </div>
      </div>
      <EmptyState v-else title="还没有收货地址" description="请先到地址管理添加收货地址">
        <el-button type="primary" @click="$router.push('/addresses')">去添加</el-button>
      </EmptyState>
    </section>

    <!-- 商品核对（购物车勾选项快照） -->
    <section class="panel">
      <div class="panel-title">商品核对</div>
      <div v-if="itemsLoading" class="panel-body">
        <el-skeleton :rows="3" animated />
      </div>
      <template v-else-if="items.length">
        <div v-for="item in items" :key="item.id" class="checkout-item">
          <span class="ci-title">{{ item.title }}</span>
          <span class="ci-price">¥{{ formatPrice(item.price) }} × {{ item.quantity }}</span>
          <span class="ci-subtotal">¥{{ formatPrice(item.price * item.quantity) }}</span>
        </div>
        <div class="checkout-summary">
          <span>共 {{ totalCount }} 件</span>
          <span class="summary-total">合计：<b>¥{{ formatPrice(totalAmount) }}</b></span>
        </div>
      </template>
      <EmptyState v-else title="没有待结算的商品" description="请回购物车勾选商品后结算">
        <el-button type="danger" @click="$router.push('/cart')">回购物车</el-button>
      </EmptyState>
    </section>

    <!-- 备注 -->
    <section class="panel">
      <div class="panel-title">订单备注</div>
      <el-input
        v-model="remark"
        type="textarea"
        :rows="2"
        maxlength="200"
        show-word-limit
        placeholder="选填，给商家留言"
      />
    </section>

    <!-- 提交栏 -->
    <div class="submit-bar">
      <div class="submit-info">
        <span>共 <b class="hl">{{ totalCount }}</b> 件</span>
        <span class="summary-total">应付：<b>¥{{ formatPrice(totalAmount) }}</b></span>
      </div>
      <el-button
        type="danger"
        size="large"
        :loading="submitting"
        :disabled="!addressId || !items.length"
        @click="handleSubmit"
      >
        提交订单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddresses } from '@/api/address'
import { getCartItems } from '@/api/cart'
import { createOrder } from '@/api/order'
import { formatPrice } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

const addresses = ref([])
const addressLoading = ref(true)
const addressId = ref(null)

const items = ref([])
const itemsLoading = ref(true)
const remark = ref('')
const submitting = ref(false)

const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
const totalAmount = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

/** U-003 地址列表；默认选中默认地址 */
async function loadAddresses() {
  try {
    const addrData = await getAddresses({ silent: true })
    // 双兼容：旧 jar 裸数组 / 新后端 PageResult{list,total}（A 已按 T5 契约修复）
    addresses.value = Array.isArray(addrData) ? addrData : (addrData && addrData.list) || []
    const def = addresses.value.find((a) => a.isDefault === 1)
    addressId.value = def ? def.id : addresses.value[0]?.id ?? null
  } catch {
    addresses.value = []
  } finally {
    addressLoading.value = false
  }
}

/** U-008 读取勾选项（结算快照：仅 selected=1） */
async function loadCheckoutItems() {
  try {
    const cartData = await getCartItems({ silent: true })
    // 双兼容：旧 jar 裸数组 / 新后端 PageResult{list,total}（A 772ddec 已按 T5 契约修复）
    const list = Array.isArray(cartData) ? cartData : (cartData && cartData.list) || []
    items.value = list.filter((i) => i.selected === 1)
  } catch {
    items.value = []
  } finally {
    itemsLoading.value = false
  }
}

/** U-012 提交订单；4003~4007 弹窗提示并刷新 */
async function handleSubmit() {
  submitting.value = true
  try {
    const data = await createOrder(
      { addressId: addressId.value, cartItemIds: items.value.map((i) => i.id), remark: remark.value || undefined },
      { silent: true }
    )
    const orders = data?.orders || []
    if (orders.length > 1) {
      ElMessage.success(`已按商家拆分为 ${orders.length} 个订单`)
    } else {
      ElMessage.success('下单成功')
    }
    // 跳转第一个订单详情（PENDING_PAY 可直接支付）
    router.replace(`/orders/${orders[0].orderId}`)
  } catch (err) {
    handleOrderError(err.code, err.message)
  } finally {
    submitting.value = false
  }
}

/** 下单错误分支（T3 4003~4007）：提示并刷新购物车快照；其余错误请求为 silent，须在此兜底提示 */
function handleOrderError(code, message) {
  if ([4003, 4004, 4005, 4006, 4007].includes(code)) {
    ElMessageBox.alert(message, '下单失败', { type: 'warning', confirmButtonText: '知道了' })
    loadCheckoutItems()
    if (code === 4005) loadAddresses()
    return
  }
  // 1002 由全局拦截器跳登录；其余（3001/3002/网络等）给用户可见反馈
  if (code !== 1002) {
    ElMessage.error(message || '下单失败，请稍后重试')
  }
}

onMounted(() => {
  loadAddresses()
  loadCheckoutItems()
})
</script>

<style scoped>
.checkout-page {
  max-width: 900px;
}
.panel {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 14px;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  border-left: 4px solid #e60012;
  padding-left: 10px;
  margin-bottom: 14px;
}
.panel-body {
  padding: 8px 0;
}
.address-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.address-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
}
.address-item.active {
  border-color: #e60012;
  background: #fff5f5;
}
.addr-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.addr-receiver {
  font-weight: 600;
  color: #333;
}
.addr-phone {
  color: #666;
  font-size: 13px;
}
.addr-detail {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}
.checkout-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 14px;
}
.ci-title {
  flex: 1;
  min-width: 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ci-price {
  color: #666;
  width: 200px;
  text-align: right;
}
.ci-subtotal {
  color: #e60012;
  font-weight: 700;
  width: 110px;
  text-align: right;
}
.checkout-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding-top: 12px;
  font-size: 13px;
  color: #666;
}
.summary-total b {
  color: #e60012;
  font-size: 20px;
}
.submit-bar {
  background: #fff;
  border-radius: 6px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
}
.submit-info {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 13px;
  color: #666;
}
.hl {
  color: #e60012;
}
</style>
