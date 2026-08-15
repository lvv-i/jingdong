<template>
  <!-- C-M04 订单处理（M-009 列表 + M-010 详情 + M-011 发货；按钮按 T1 订单状态机显隐） -->
  <el-card shadow="never">
    <!-- 状态筛选 tabs（M-009 ?status=） -->
    <el-tabs v-model="query.status" @tab-change="handleStatusChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane
        v-for="(v, k) in ORDER_STATUS"
        :key="k"
        :label="v.label"
        :name="k"
      />
    </el-tabs>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column label="买家" width="120">
        <template #default="{ row }">{{ row.userName || '-' }}</template>
      </el-table-column>
      <el-table-column label="金额" width="120">
        <template #default="{ row }">¥{{ Number(row.payAmount ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="statusInfo(ORDER_STATUS, row.status).tag">
            {{ statusInfo(ORDER_STATUS, row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="shippingNo" label="运单号" width="160">
        <template #default="{ row }">{{ row.shippingNo || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" min-width="160">
        <template #default="{ row }">{{ row.createdAt || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <!-- T1 订单状态机：PAID → SHIPPED（发货，shippingNo 必填）；其余状态只读 -->
          <el-button
            v-if="row.status === 'PAID'"
            link
            type="primary"
            @click="openShip(row)"
          >
            发货
          </el-button>
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
      <!-- 空状态（rule 第 5 节） -->
      <template #empty>
        <el-empty :description="query.status ? '该状态下暂无订单' : '暂无订单数据'" />
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
      @size-change="handleStatusChange"
    />

    <!-- 发货弹窗（M-011 POST /api/merchant/orders/{id}/ship：PAID → SHIPPED，运单号必填） -->
    <el-dialog v-model="shipVisible" title="订单发货" width="460px" destroy-on-close>
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="订单号">{{ currentRow?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="收货人">
          {{ currentRow?.receiverSnapshot?.receiver || '-' }}（{{ currentRow?.receiverSnapshot?.phone || '-' }}）
        </el-descriptions-item>
        <el-descriptions-item label="收货地址">
          {{ shipReceiverAddress }}
        </el-descriptions-item>
      </el-descriptions>
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="90px">
        <el-form-item label="物流运单号" prop="shippingNo">
          <el-input v-model="shipForm.shippingNo" placeholder="请输入快递运单号（T3 4009：运单号必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleShip">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗（M-010 GET /api/merchant/orders/{id}） -->
    <el-dialog v-model="detailVisible" title="订单详情" width="640px">
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号" :span="2">{{ detail.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="买家">{{ detail.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            ¥{{ Number(detail.payAmount ?? 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detail.status" :type="statusInfo(ORDER_STATUS, detail.status).tag">
              {{ statusInfo(ORDER_STATUS, detail.status).label }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="运单号">{{ detail.shippingNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detail.receiverSnapshot?.receiver || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货电话">{{ detail.receiverSnapshot?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailReceiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ detail.createdAt || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items || []" size="small" class="mt16">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">{{ row.title || '-' }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="单价" width="110">
            <template #default="{ row }">¥{{ Number(row.price ?? 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-M04：M-009 订单列表 + M-010 订单详情 + M-011 发货（T5 商家组；T1 订单状态机 5 态）
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { M009_listOrders, M010_getOrder, M011_shipOrder } from '../../api/merchant'
import { ORDER_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const saving = ref(false)
const detailLoading = ref(false)
const list = ref([])
const total = ref(0)
const shipVisible = ref(false)
const detailVisible = ref(false)
const shipFormRef = ref()
const currentRow = ref(null)
const detail = reactive({})

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, status: '' })
const shipForm = reactive({ shippingNo: '' })
// T5 M-011：shippingNo 必填（前端校验与后端 4009 一致）
const shipRules = {
  shippingNo: [{ required: true, message: '请输入物流运单号', trigger: 'blur' }]
}

// 收货地址拼装（receiverSnapshot 来自后端快照，前端不二次加工）
const shipReceiverAddress = computed(() => joinAddress(currentRow.value?.receiverSnapshot))
const detailReceiverAddress = computed(() => joinAddress(detail.receiverSnapshot))

function joinAddress(s) {
  if (!s) return '-'
  const parts = [s.province, s.city, s.district, s.detail].filter(Boolean)
  return parts.length ? parts.join(' ') : '-'
}

async function loadList() {
  loading.value = true
  try {
    const res = await M009_listOrders({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined
    })
    list.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    loading.value = false
  }
}

function handleStatusChange() {
  query.page = 1
  loadList()
}

function openShip(row) {
  currentRow.value = row
  shipForm.shippingNo = ''
  shipVisible.value = true
}

async function handleShip() {
  await shipFormRef.value.validate()
  saving.value = true
  try {
    // M-011：PAID → SHIPPED（T1 合法流转；4002 非法流转由后端拒绝并提示）
    await M011_shipOrder(currentRow.value.id, { shippingNo: shipForm.shippingNo })
    ElMessage.success('发货成功，订单已更新为已发货待收货')
    shipVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function openDetail(row) {
  currentRow.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    const d = await M010_getOrder(row.id)
    Object.keys(detail).forEach((k) => delete detail[k])
    Object.assign(detail, d || {})
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.mb16 {
  margin-bottom: 16px;
}
.mt16 {
  margin-top: 16px;
}
</style>
