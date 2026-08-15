<template>
  <!-- C-M01 店铺仪表盘（M-015 统计 + M-001 店铺信息） -->
  <div class="dashboard">
    <!-- 店铺信息条（M-001 GET /api/merchant/shop） -->
    <el-card shadow="never" class="shop-card">
      <div class="shop-info">
        <div>
          <div class="shop-name">
            {{ shop.shopName || '未命名店铺' }}
            <el-tag v-if="shop.auditStatus" :type="statusInfo(MERCHANT_AUDIT_STATUS, shop.auditStatus).tag" size="small">
              {{ statusInfo(MERCHANT_AUDIT_STATUS, shop.auditStatus).label }}
            </el-tag>
          </div>
          <div class="shop-desc">{{ shop.description || '暂无店铺简介' }}</div>
        </div>
        <el-button :icon="Refresh" circle @click="loadAll" />
      </div>
      <!-- 入驻审核引导（T3：6003 审核中 / 6005 已驳回） -->
      <el-alert
        v-if="shop.auditStatus === 'PENDING_AUDIT'"
        type="warning"
        show-icon
        :closable="false"
        title="店铺审核中：管理员审核通常需要一定时间，通过后即可发布商品与处理订单"
        class="audit-tip"
      />
      <el-alert
        v-if="shop.auditStatus === 'REJECTED'"
        type="error"
        show-icon
        :closable="false"
        class="audit-tip"
      >
        <template #title>
          店铺被驳回：{{ shop.auditReason || '未填写原因' }}。请前往
          <router-link to="/merchant/shop">店铺设置</router-link>修改资料并重新提交
        </template>
      </el-alert>
    </el-card>

    <!-- 统计卡片行（M-015 GET /api/merchant/stats） -->
    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :class="card.key === 'todaySalesAmount' ? 'amount' : ''">
            {{ formatValue(card) }}
          </div>
          <div class="stat-sub">今日实时</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办快捷入口 -->
    <el-card shadow="never" class="todo-card">
      <template #header>待办提醒</template>
      <el-row :gutter="12">
        <el-col :span="8">
          <div class="todo-item">
            <el-badge :value="stats.pendingShipCount || 0" :hidden="!stats.pendingShipCount">
              <el-button link type="primary" @click="$router.push('/merchant/orders?status=PAID')">
                待发货订单
              </el-button>
            </el-badge>
            <div class="todo-tip">已支付订单请尽快发货</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="todo-item">
            <el-badge :value="stats.pendingRefundCount || 0" :hidden="!stats.pendingRefundCount">
              <el-button link type="primary" @click="$router.push('/merchant/refunds?status=REFUNDING')">
                待处理退款
              </el-button>
            </el-badge>
            <div class="todo-tip">退款中申请请及时处理</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="todo-item">
            <el-button link type="primary" @click="$router.push('/merchant/products')">管理商品</el-button>
            <div class="todo-tip">共 {{ stats.productCount ?? '-' }} 件商品</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
// C-M01：M-001 店铺资料 + M-015 统计（T5 商家组）
import { reactive, ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { M001_getShop, M015_getStats } from '../../api/merchant'
import { MERCHANT_AUDIT_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const shop = reactive({})
const stats = reactive({})

const statCards = [
  { key: 'todayOrderCount', label: '今日订单数', suffix: ' 单' },
  { key: 'todaySalesAmount', label: '今日销售额', prefix: '¥' },
  { key: 'pendingShipCount', label: '待发货', suffix: ' 单' },
  { key: 'pendingRefundCount', label: '待处理退款', suffix: ' 单' },
  { key: 'productCount', label: '在售商品', suffix: ' 件' }
]

function formatValue(card) {
  const v = stats[card.key]
  if (v === null || v === undefined) return '-'
  if (card.key === 'todaySalesAmount') return `¥${Number(v).toFixed(2)}`
  return `${v}${card.suffix || ''}`
}

async function loadAll() {
  loading.value = true
  try {
    const [s, st] = await Promise.all([M001_getShop(), M015_getStats()])
    Object.assign(shop, s || {})
    Object.assign(stats, st || {})
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.shop-card {
  margin-bottom: 16px;
}
.shop-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.shop-name {
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.shop-desc {
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}
.audit-tip {
  margin-top: 12px;
}
.stat-card {
  margin-bottom: 16px;
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin: 8px 0;
}
.stat-value.amount {
  color: #e4393c;
}
.stat-sub {
  color: #c0c4cc;
  font-size: 12px;
}
.todo-item {
  text-align: center;
}
.todo-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>
