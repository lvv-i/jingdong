<template>
  <!-- C-M05 售后处理（M-012 列表 + M-013 同意 / M-014 拒绝；按钮按 T1 售后状态机显隐） -->
  <el-card shadow="never">
    <!-- 状态筛选 tabs（M-012 ?status=） -->
    <el-tabs v-model="query.status" @tab-change="handleStatusChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane
        v-for="(v, k) in REFUND_STATUS"
        :key="k"
        :label="v.label"
        :name="k"
      />
    </el-tabs>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="refundNo" label="退款单号" width="200" />
      <el-table-column prop="orderId" label="订单ID" width="90" />
      <el-table-column label="退款金额" width="120">
        <template #default="{ row }">¥{{ Number(row.refundAmount ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="退款原因" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="statusInfo(REFUND_STATUS, row.status).tag">
            {{ statusInfo(REFUND_STATUS, row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商家回复" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.merchantReply || '-' }}</template>
      </el-table-column>
      <el-table-column label="平台裁决" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.adminResult || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <!-- T1 售后状态机：仅 REFUNDING 可同意/拒绝（M-013/M-014，reply 必填） -->
          <template v-if="row.status === 'REFUNDING'">
            <el-button link type="success" @click="openHandle(row, 'agree')">同意</el-button>
            <el-button link type="danger" @click="openHandle(row, 'reject')">拒绝</el-button>
          </template>
          <span v-else class="readonly-tip">-</span>
        </template>
      </el-table-column>
      <!-- 空状态（rule 第 5 节） -->
      <template #empty>
        <el-empty :description="query.status ? '该状态下暂无售后单' : '暂无售后单数据'" />
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

    <!-- 同意/拒绝弹窗（M-013/M-014：REFUNDING → MERCHANT_AGREED / MERCHANT_REJECTED，reply 必填） -->
    <el-dialog
      v-model="handleVisible"
      :title="handleType === 'agree' ? '同意退款' : '拒绝退款'"
      width="480px"
      destroy-on-close
    >
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="退款单号">{{ currentRow?.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          ¥{{ Number(currentRow?.refundAmount ?? 0).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRow?.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="90px">
        <el-form-item :label="handleType === 'agree' ? '同意说明' : '拒绝原因'" prop="reply">
          <el-input
            v-model="handleForm.reply"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            :placeholder="handleType === 'agree' ? '如：同意退款，请用户查收' : '请填写拒绝退款的原因（T3 5007：回复必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button :type="handleType === 'agree' ? 'success' : 'danger'" :loading="saving" @click="handleSubmit">
          {{ handleType === 'agree' ? '确认同意' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-M05：M-012 售后列表 + M-013 同意退款 + M-014 拒绝退款（T5 商家组；T1 售后状态机 6 态）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { M012_listRefunds, M013_agreeRefund, M014_rejectRefund } from '../../api/merchant'
import { REFUND_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const handleVisible = ref(false)
const handleFormRef = ref()
const currentRow = ref(null)
const handleType = ref('agree') // 'agree' | 'reject'

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, status: '' })
const handleForm = reactive({ reply: '' })
// T5 M-013/M-014：reply 必填（前端校验与后端 5007 一致）
const handleRules = {
  reply: [{ required: true, message: '回复内容必填', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await M012_listRefunds({
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

// 敏感操作二次确认（rule 第 5 节）
async function openHandle(row, type) {
  const action = type === 'agree' ? '同意退款' : '拒绝退款'
  await ElMessageBox.confirm(
    `确定要${action}该退款单吗？${type === 'reject' ? '拒绝后用户可申请平台介入。' : '同意后系统将执行模拟退款。'}`,
    action,
    { type: 'warning', confirmButtonText: `确定${action}` }
  )
  currentRow.value = row
  handleType.value = type
  handleForm.reply = ''
  handleVisible.value = true
}

async function handleSubmit() {
  await handleFormRef.value.validate()
  saving.value = true
  try {
    // M-013：REFUNDING → MERCHANT_AGREED；M-014：REFUNDING → MERCHANT_REJECTED（T1 合法流转）
    const { id } = currentRow.value
    const payload = { reply: handleForm.reply }
    if (handleType.value === 'agree') {
      await M013_agreeRefund(id, payload)
      ElMessage.success('已同意退款，等待系统执行模拟退款')
    } else {
      await M014_rejectRefund(id, payload)
      ElMessage.success('已拒绝退款，用户可申请平台介入')
    }
    handleVisible.value = false
    loadList()
  } finally {
    saving.value = false
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
.readonly-tip {
  color: #c0c4cc;
}
</style>
