<template>
  <!-- C-A07 日志统计（A-019 全局统计看板 + A-018 审计日志查询） -->
  <div class="logs-page">
    <!-- 统计卡片行（A-019 GET /api/admin/stats） -->
    <el-row :gutter="16" v-loading="statsLoading">
      <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :class="card.key === 'salesAmount' ? 'amount' : ''">
            {{ formatStat(card) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审计日志查询（A-018 GET /api/admin/logs） -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
          <div class="toolbar">
            <el-select v-model="query.operatorRole" placeholder="操作角色" clearable style="width: 120px" @change="handleSearch">
              <el-option label="普通用户" value="USER" />
              <el-option label="商家" value="MERCHANT" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-select v-model="query.targetType" placeholder="目标类型" clearable style="width: 140px" @change="handleSearch">
              <el-option label="商家" value="MERCHANT" />
              <el-option label="商品" value="PRODUCT" />
              <el-option label="订单" value="ORDER" />
              <el-option label="退款" value="REFUND" />
            </el-select>
            <el-button :icon="Refresh" circle @click="handleSearch" />
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="操作人" width="110">
          <template #default="{ row }">#{{ row.operatorId }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTag(row.operatorRole)" size="small">{{ roleLabel(row.operatorRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="110">
          <template #default="{ row }">{{ row.targetType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="90">
          <template #default="{ row }">{{ row.targetId ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="action" label="动作" width="140">
          <template #default="{ row }">{{ row.action || '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" min-width="160">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无审计日志（审核/裁决/发货等敏感操作会写入日志）" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="loadList"
        @size-change="handleSearch"
      />
    </el-card>
  </div>
</template>

<script setup>
// C-A07：A-019 全局统计 + A-018 审计日志查询（T5 管理员组）
import { reactive, ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { A018_listLogs, A019_getStats } from '../../api/admin'

const loading = ref(false)
const statsLoading = ref(false)
const list = ref([])
const total = ref(0)
const stats = reactive({})

const statCards = [
  { key: 'userCount', label: '注册用户', suffix: ' 人' },
  { key: 'merchantCount', label: '入驻商家', suffix: ' 家' },
  { key: 'productCount', label: '商品总数', suffix: ' 件' },
  { key: 'orderCount', label: '订单总数', suffix: ' 单' },
  { key: 'refundCount', label: '售后总数', suffix: ' 单' },
  { key: 'salesAmount', label: '累计销售额' }
]

const ROLE_LABELS = { USER: '普通用户', MERCHANT: '商家', ADMIN: '管理员' }
const ROLE_TAGS = { USER: 'info', MERCHANT: 'warning', ADMIN: 'danger' }

function roleLabel(role) {
  return ROLE_LABELS[role] || role
}

function roleTag(role) {
  return ROLE_TAGS[role] || 'info'
}

function formatStat(card) {
  const v = stats[card.key]
  if (v === null || v === undefined) return '-'
  if (card.key === 'salesAmount') return `¥${Number(v).toFixed(2)}`
  return `${v}${card.suffix || ''}`
}

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, operatorRole: '', targetType: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await A018_listLogs({
      page: query.page,
      pageSize: query.pageSize,
      operatorRole: query.operatorRole || undefined,
      targetType: query.targetType || undefined
    })
    list.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadList()
}

async function loadStats() {
  statsLoading.value = true
  try {
    const s = await A019_getStats()
    Object.assign(stats, s || {})
  } finally {
    statsLoading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadList()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 16px;
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 8px;
}
.stat-value.amount {
  color: #e4393c;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
