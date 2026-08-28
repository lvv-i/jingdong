<template>
  <!-- B-P07 订单中心：U-013 列表 + T1 五态 Tab + 分页 + firstItemImage 缩略 -->
  <div class="order-list-page">
    <h2 class="page-heading">我的订单</h2>

    <!-- 状态 Tab（T1 五态 + 全部） -->
    <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
      <el-tab-pane
        v-for="tab in ORDER_TABS"
        :key="tab.value"
        :label="tab.label"
        :name="tab.value"
      />
    </el-tabs>

    <!-- 订单列表 -->
    <div v-loading="loading" class="order-body">
      <EmptyState
        v-if="!loading && orders.length === 0"
        title="暂无相关订单"
        description="去挑选喜欢的商品吧"
      >
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </EmptyState>

      <template v-else>
        <div
          v-for="o in orders"
          :key="o.id"
          class="order-card"
          @click="$router.push(`/orders/${o.id}`)"
        >
          <div class="oc-head">
            <span class="oc-no">{{ o.orderNo }}</span>
            <span class="oc-time">{{ formatDateTime(o.createdAt) }}</span>
            <el-tag :type="orderStatus(o.status).type" size="small">
              {{ orderStatus(o.status).label }}
            </el-tag>
          </div>
          <div class="oc-body">
            <el-image
              v-if="o.firstItemImage"
              :src="o.firstItemImage"
              fit="cover"
              class="oc-thumb"
            >
              <template #error>
                <div class="oc-thumb-fallback">无图</div>
              </template>
            </el-image>
            <div v-else class="oc-thumb oc-thumb-fallback">无图</div>
            <div class="oc-info">
              <span class="oc-count">共 {{ o.itemsCount }} 件商品</span>
              <span class="oc-pay">实付 ¥{{ formatPrice(o.payAmount) }}</span>
            </div>
            <el-button type="primary" link @click.stop="$router.push(`/orders/${o.id}`)">
              查看详情
            </el-button>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="total > pageSize"
          class="pager"
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          @current-change="handlePageChange"
        />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listOrders } from '@/api/order'
import { ORDER_TABS, orderStatus } from '@/utils/status-map'
import { formatPrice, formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const activeStatus = ref('')
const orders = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)

/** U-013 订单列表（silent：失败展示空状态，不弹 toast） */
async function loadOrders() {
  loading.value = true
  try {
    const data = await getList()
    orders.value = data.list || []
    total.value = data.total || 0
  } catch {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 组装列表请求（status 空=全部） */
function getList() {
  return listOrders(
    {
      page: page.value,
      pageSize,
      status: activeStatus.value || undefined
    },
    { silent: true }
  )
}

function handleTabChange() {
  page.value = 1
  loadOrders()
}

function handlePageChange(p) {
  page.value = p
  loadOrders()
}

onMounted(loadOrders)
</script>

<style scoped>
.order-list-page {
  max-width: 900px;
}
.page-heading {
  margin: 0 0 6px;
  font-size: 20px;
  color: #333;
}
.order-body {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  min-height: 200px;
}
.order-card {
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.order-card:hover {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}
.oc-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 14px;
  background: #fafafa;
  border-radius: 6px 6px 0 0;
  font-size: 13px;
}
.oc-no {
  color: #333;
  font-weight: 600;
}
.oc-time {
  color: #999;
  flex: 1;
}
.oc-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
}
.oc-thumb {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  background: #f5f5f5;
}
.oc-thumb-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 12px;
}
.oc-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
}
.oc-count {
  color: #666;
}
.oc-pay {
  color: #e60012;
  font-weight: 700;
  font-size: 15px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
