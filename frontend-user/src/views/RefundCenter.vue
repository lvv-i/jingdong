<template>
  <!-- B-P09 售后中心：U-019 列表（六态）+ U-018 发起退款 + U-020 撤销 + U-021 申请介入 -->
  <div class="refund-page">
    <div class="page-header">
      <h2>售后中心</h2>
      <el-button type="danger" @click="openCreateDialog">申请退款</el-button>
    </div>

    <!-- 六态筛选（T1 退款状态机） -->
    <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
      <el-tab-pane
        v-for="tab in REFUND_TABS"
        :key="tab.value"
        :label="tab.label"
        :name="tab.value"
      />
    </el-tabs>

    <!-- 退款单列表（U-019） -->
    <div v-loading="loading" class="refund-body">
      <EmptyState
        v-if="!loading && refunds.length === 0"
        title="暂无售后记录"
        description="如需退款请点击右上角申请退款"
      />
      <template v-else>
        <div v-for="r in refunds" :key="r.id" class="refund-card">
          <div class="rc-head">
            <span class="rc-no">退款单号：{{ r.refundNo }}</span>
            <el-tag :type="refundStatus(r.status).type" size="small">
              {{ refundStatus(r.status).label }}
            </el-tag>
          </div>
          <div class="rc-body">
            <div class="rc-info">
              <div class="rc-line">
                <span class="k">关联订单</span>
                <el-link type="primary" @click="$router.push(`/orders/${r.orderId}`)">
                  {{ r.orderId }}
                </el-link>
              </div>
              <div class="rc-line"><span class="k">退款金额</span>¥{{ formatPrice(r.refundAmount) }}</div>
              <div class="rc-line"><span class="k">退款原因</span>{{ r.reason }}</div>
              <div class="rc-line"><span class="k">申请时间</span>{{ formatDateTime(r.createdAt) }}</div>
              <div v-if="r.merchantReply" class="rc-line">
                <span class="k">商家回复</span>
                <span class="rc-reply">{{ r.merchantReply }}</span>
              </div>
              <div v-if="r.adminResult" class="rc-line">
                <span class="k">平台裁决</span>
                <span class="rc-reply rc-admin">{{ r.adminResult }}</span>
              </div>
            </div>
            <div class="rc-actions">
              <!-- U-020 撤销：仅 REFUNDING（商家未处理前） -->
              <el-button
                v-if="r.status === 'REFUNDING'"
                link
                type="warning"
                :loading="r.cancelling"
                @click="handleCancelRefund(r)"
              >
                撤销退款
              </el-button>
              <!-- U-021 申请平台介入：仅 MERCHANT_REJECTED -->
              <el-button
                v-if="r.status === 'MERCHANT_REJECTED'"
                link
                type="danger"
                :loading="r.intervening"
                @click="handleIntervene(r)"
              >
                申请平台介入
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />
    </div>

    <!-- U-018 发起退款弹窗 -->
    <el-dialog v-model="createVisible" title="申请退款" width="520px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="选择订单" required>
          <el-select
            v-model="createForm.orderId"
            placeholder="请选择要退款的订单"
            style="width: 100%"
            :loading="orderOptionsLoading"
            @focus="loadRefundableOrders"
          >
            <el-option
              v-for="o in refundableOrders"
              :key="o.id"
              :label="`${o.orderNo}（¥${formatPrice(o.payAmount)}）`"
              :value="o.id"
            />
          </el-select>
          <div class="form-tip">仅已支付/已发货/已完成的订单可申请退款</div>
        </el-form-item>
        <el-form-item label="退款原因" required>
          <el-input
            v-model="createForm.reason"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="请描述退款原因"
          />
        </el-form-item>
        <el-form-item label="退款金额" required>
          <el-input-number
            v-model="createForm.refundAmount"
            :min="0.01"
            :max="999999.99"
            :precision="2"
            :step="10"
            style="width: 200px"
          />
          <div class="form-tip">金额不能超过订单实付金额</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="submitting"
          :disabled="!createForm.orderId || !createForm.reason || !createForm.refundAmount"
          @click="handleCreateRefund"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRefunds, createRefund, cancelRefund, interveneRefund } from '@/api/refund'
import { listOrders } from '@/api/order'
import { refundStatus, REFUND_TABS } from '@/utils/status-map'
import { formatPrice, formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()

const refunds = ref([])
const loading = ref(true)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const activeStatus = ref('')

// 发起退款弹窗
const createVisible = ref(false)
const submitting = ref(false)
const refundableOrders = ref([])
const orderOptionsLoading = ref(false)
const createForm = reactive({ orderId: null, reason: '', refundAmount: null })

/** U-019 售后列表（status 筛选 + 分页） */
async function loadRefunds() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    if (activeStatus.value) params.status = activeStatus.value
    const data = await getRefunds(params, { silent: true })
    refunds.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    refunds.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 可退款订单（U-013 拉全量过滤 PAID/SHIPPED/COMPLETED） */
async function loadRefundableOrders() {
  orderOptionsLoading.value = true
  try {
    const data = await listOrders({ page: 1, pageSize: 100 }, { silent: true })
    refundableOrders.value = (data?.list || []).filter((o) =>
      ['PAID', 'SHIPPED', 'COMPLETED'].includes(o.status)
    )
    // 入口 query orderId 预选（订单详情「申请退款」跳入）
    const qOrderId = Number(route.query.orderId)
    if (qOrderId && !createForm.orderId) {
      createForm.orderId = refundableOrders.value.some((o) => o.id === qOrderId) ? qOrderId : null
    }
  } catch {
    refundableOrders.value = []
  } finally {
    orderOptionsLoading.value = false
  }
}

function openCreateDialog() {
  createForm.orderId = null
  createForm.reason = ''
  createForm.refundAmount = null
  createVisible.value = true
  loadRefundableOrders()
}

/** U-018 发起退款（5002/5003/5004/5006 提示由拦截器弹出，silent 模式下按码提示） */
async function handleCreateRefund() {
  submitting.value = true
  try {
    const data = await createRefund(
      {
        orderId: createForm.orderId,
        reason: createForm.reason,
        refundAmount: createForm.refundAmount
      },
      { silent: true }
    )
    // 双兼容：新后端返回 refundNo，旧 jar 返回 refundId（A 772ddec 已改 T5 契约）
    ElMessage.success(`退款申请已提交（单号 ${data.refundNo || data.refundId}）`)
    createVisible.value = false
    activeStatus.value = ''
    page.value = 1
    loadRefunds()
  } catch (err) {
    if ([5002, 5003, 5004, 5005, 5006].includes(err.code)) {
      ElMessage.warning(err.message)
    }
    // 其他错误全局拦截器已提示
  } finally {
    submitting.value = false
  }
}

/** U-020 撤销退款（仅 REFUNDING） */
async function handleCancelRefund(r) {
  try {
    await ElMessageBox.confirm('确定撤销该退款申请吗？', '撤销退款', {
      type: 'warning',
      confirmButtonText: '确定撤销',
      cancelButtonText: '再想想'
    })
  } catch {
    return
  }
  r.cancelling = true
  try {
    await cancelRefund(r.id, { silent: true })
    ElMessage.success('退款申请已撤销')
    loadRefunds()
  } catch (err) {
    if ([5001, 5005].includes(err.code)) {
      ElMessage.warning(err.message)
      loadRefunds()
    }
  } finally {
    r.cancelling = false
  }
}

/** U-021 申请平台介入（仅 MERCHANT_REJECTED） */
async function handleIntervene(r) {
  try {
    await ElMessageBox.confirm(
      '商家已拒绝退款，确定申请平台介入裁决吗？',
      '申请平台介入',
      {
        type: 'warning',
        confirmButtonText: '申请介入',
        cancelButtonText: '再想想'
      }
    )
  } catch {
    return
  }
  r.intervening = true
  try {
    await interveneRefund(r.id, { silent: true })
    ElMessage.success('已申请平台介入，请等待裁决结果')
    loadRefunds()
  } catch (err) {
    if ([5001, 5005].includes(err.code)) {
      ElMessage.warning(err.message)
      loadRefunds()
    }
  } finally {
    r.intervening = false
  }
}

function handleTabChange() {
  page.value = 1
  loadRefunds()
}

function handlePageChange(p) {
  page.value = p
  loadRefunds()
}

onMounted(() => {
  loadRefunds()
  // 订单详情「申请退款」入口：自动弹窗
  if (route.query.orderId) openCreateDialog()
})
</script>

<style scoped>
.refund-page {
  max-width: 900px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 0;
}
.page-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
  border-left: 4px solid #e60012;
  padding-left: 10px;
}
.refund-body {
  background: #fff;
  border-radius: 6px;
  padding: 8px 20px 16px;
  min-height: 120px;
}
.refund-card {
  border-bottom: 1px solid #f5f5f5;
  padding: 12px 0;
}
.rc-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.rc-no {
  font-size: 13px;
  color: #999;
}
.rc-body {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}
.rc-line {
  font-size: 13px;
  color: #666;
  line-height: 1.8;
}
.rc-line .k {
  display: inline-block;
  width: 76px;
  color: #999;
}
.rc-reply {
  color: #e6a23c;
}
.rc-admin {
  color: #e60012;
}
.rc-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.pager {
  background: #fff;
  border-radius: 6px;
  padding: 12px 20px;
  display: flex;
  justify-content: flex-end;
}
.form-tip {
  width: 100%;
  font-size: 12px;
  color: #999;
  line-height: 1.6;
}
</style>
