<template>
  <!-- B-P08 订单详情（U-014 展示 + U-015 支付 + U-016 取消 + U-017 确认收货 + U-024 评价入口） -->
  <div class="order-detail-page">
    <!-- 4001 订单不存在/无权访问 -->
    <div v-if="notFound" class="error-block">
      <el-result icon="warning" title="订单不存在" sub-title="订单不存在或无权访问">
        <template #extra>
          <el-button type="primary" @click="$router.push('/orders')">去订单中心</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <!-- 状态头 -->
      <div class="status-head">
        <div>
          <el-tag :type="statusInfo.type" size="large">{{ statusInfo.label }}</el-tag>
          <span class="status-tip">{{ statusTip }}</span>
        </div>
        <div class="order-no">订单号：{{ order.orderNo || '-' }}</div>
      </div>

      <!-- 收货信息 -->
      <section class="panel">
        <div class="panel-title">收货信息</div>
        <div class="receiver-snapshot">{{ order.receiverSnapshot || '-' }}</div>
      </section>

      <!-- 商品明细（快照） -->
      <section class="panel">
        <div class="panel-title">商品明细</div>
        <div v-for="it in order.items || []" :key="it.id" class="od-item">
          <span class="odi-title">{{ it.titleSnapshot }}</span>
          <span class="odi-price">¥{{ formatPrice(it.priceSnapshot) }} × {{ it.quantity }}</span>
          <span class="odi-subtotal">¥{{ formatPrice(it.totalPrice) }}</span>
          <!-- U-024 评价入口：仅 COMPLETED 且该明细未评价（rating/reviewedAt 后端驱动） -->
          <el-button
            v-if="order.status === 'COMPLETED' && !it.reviewedAt"
            type="primary"
            link
            @click="$router.push(`/orders/${order.id}/review?itemId=${it.id}`)"
          >
            评价
          </el-button>
          <el-rate
            v-else-if="it.reviewedAt"
            :model-value="it.rating"
            disabled
            size="small"
          />
        </div>
        <div class="od-summary">
          <span>商品总额：¥{{ formatPrice(order.totalAmount) }}</span>
          <span class="od-pay">实付：<b>¥{{ formatPrice(order.payAmount) }}</b></span>
        </div>
      </section>

      <!-- 时间信息 -->
      <section class="panel">
        <div class="panel-title">订单信息</div>
        <div class="info-grid">
          <div class="info-item"><span class="k">下单时间</span>{{ formatDateTime(order.createdAt) }}</div>
          <div class="info-item"><span class="k">支付时间</span>{{ formatDateTime(order.paidAt) }}</div>
          <div class="info-item"><span class="k">发货时间</span>{{ formatDateTime(order.shippedAt) }}</div>
          <div class="info-item"><span class="k">完成时间</span>{{ formatDateTime(order.completedAt) }}</div>
          <div class="info-item"><span class="k">运单号</span>{{ order.shippingNo || '-' }}</div>
        </div>
      </section>

      <!-- 操作区：按钮显隐由后端 status 驱动（T1） -->
      <div class="action-bar">
        <template v-if="order.status === 'PENDING_PAY'">
          <el-button type="danger" size="large" :loading="paying" @click="handlePay">
            立即支付
          </el-button>
          <el-button size="large" :loading="cancelling" @click="handleCancel">
            取消订单
          </el-button>
        </template>
        <template v-else-if="order.status === 'PAID'">
          <el-tag type="primary" size="large">已支付，等待商家发货</el-tag>
        </template>
        <template v-else-if="order.status === 'SHIPPED'">
          <el-button type="success" size="large" :loading="confirming" @click="handleConfirm">
            确认收货
          </el-button>
        </template>
        <template v-else-if="order.status === 'COMPLETED'">
          <el-tag type="success" size="large">订单已完成</el-tag>
        </template>
        <!-- U-018 申请退款入口（可退款态：PAID/SHIPPED/COMPLETED） -->
        <el-button
          v-if="['PAID', 'SHIPPED', 'COMPLETED'].includes(order.status)"
          size="large"
          type="warning"
          plain
          @click="$router.push(`/refunds?orderId=${order.id}`)"
        >
          申请退款
        </el-button>
        <el-button size="large" plain @click="$router.push('/orders')">返回订单中心</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, payOrder, cancelOrder, confirmReceipt } from '@/api/order'
import { orderStatus } from '@/utils/status-map'
import { formatPrice, formatDateTime } from '@/utils/format'

const route = useRoute()

const orderId = computed(() => Number(route.params.id))
const order = ref({})
const notFound = ref(false)
const paying = ref(false)
const cancelling = ref(false)
const confirming = ref(false)

const statusInfo = computed(() => orderStatus(order.value.status))
const statusTip = computed(() => {
  if (order.value.status === 'PENDING_PAY') return '请在 30 分钟内完成支付，超时自动取消'
  return ''
})

/** U-014 订单详情（4001 → notFound） */
async function loadDetail() {
  notFound.value = false
  try {
    order.value = (await getOrderDetail(orderId.value, { silent: true })) || {}
  } catch (err) {
    if (err.code === 4001) {
      notFound.value = true
    }
    // 其他错误全局 toast 已提示
  }
}

/** U-015 模拟支付（4008 重复支付拦截 + 刷新） */
async function handlePay() {
  try {
    await ElMessageBox.confirm('确认支付本订单？（模拟支付）', '支付确认', {
      type: 'info',
      confirmButtonText: '确认支付',
      cancelButtonText: '再想想'
    })
  } catch {
    return
  }
  paying.value = true
  try {
    const data = await payOrder(orderId.value, { silent: true })
    ElMessageBox.alert(`支付成功！支付流水号：${data.paymentNo}`, '模拟支付', {
      type: 'success',
      confirmButtonText: '好的'
    })
    loadDetail()
  } catch (err) {
    if (err.code === 4008) {
      ElMessage.warning('订单已支付，请勿重复操作')
      loadDetail()
    } else if (err.code === 4002) {
      ElMessage.warning(err.message)
      loadDetail()
    }
  } finally {
    paying.value = false
  }
}

/** U-016 取消订单（仅 PENDING_PAY 显示按钮） */
async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定取消该订单吗？库存将被释放。', '取消订单', {
      type: 'warning',
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想'
    })
  } catch {
    return
  }
  cancelling.value = true
  try {
    await cancelOrder(orderId.value)
    ElMessage.success('订单已取消')
    loadDetail()
  } finally {
    cancelling.value = false
  }
}

/** U-017 确认收货：SHIPPED → COMPLETED（4002 状态不允许 → 提示 + 刷新） */
async function handleConfirm() {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      type: 'info',
      confirmButtonText: '确认收货',
      cancelButtonText: '再想想'
    })
  } catch {
    return
  }
  confirming.value = true
  try {
    await confirmReceipt(orderId.value)
    ElMessage.success('已确认收货，订单完成')
    loadDetail()
  } catch (err) {
    if (err.code === 4002) {
      ElMessage.warning(err.message)
      loadDetail()
    }
  } finally {
    confirming.value = false
  }
}

watch(orderId, loadDetail)
onMounted(loadDetail)
</script>

<style scoped>
.order-detail-page {
  max-width: 900px;
}
.error-block {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
}
.status-head {
  background: #fff;
  border-radius: 6px;
  padding: 18px 20px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.status-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.order-no {
  color: #666;
  font-size: 13px;
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
.receiver-snapshot {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}
.od-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 14px;
}
.odi-title {
  flex: 1;
  min-width: 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.odi-price {
  color: #666;
  width: 200px;
  text-align: right;
}
.odi-subtotal {
  color: #e60012;
  font-weight: 700;
  width: 110px;
  text-align: right;
}
.od-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding-top: 12px;
  font-size: 13px;
  color: #666;
}
.od-pay b {
  color: #e60012;
  font-size: 20px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.info-item {
  font-size: 13px;
  color: #666;
}
.info-item .k {
  display: inline-block;
  width: 76px;
  color: #999;
}
.action-bar {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}
</style>
