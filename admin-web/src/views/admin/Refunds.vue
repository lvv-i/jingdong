<template>
  <!-- C-A06 争议处理（A-014 全局售后列表 + A-015 平台介入裁决；仅 ADMIN_INTERVENED 可裁决，T1 售后状态机） -->
  <el-card shadow="never">
    <!-- 状态筛选 tabs（A-014 ?status=） -->
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
      <el-table-column prop="reason" label="退款原因" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="statusInfo(REFUND_STATUS, row.status).tag">
            {{ statusInfo(REFUND_STATUS, row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商家回复" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.merchantReply || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <!-- T1 售后状态机：仅 ADMIN_INTERVENED 可裁决（A-015，adminResult + agree 必填） -->
          <el-button
            v-if="row.status === 'ADMIN_INTERVENED'"
            link
            type="primary"
            @click="openHandle(row)"
          >
            裁决
          </el-button>
          <span v-else class="readonly-tip">-</span>
        </template>
      </el-table-column>
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

    <!-- 裁决弹窗（A-015 POST /api/admin/refunds/{id}/handle：ADMIN_INTERVENED → REFUNDED/CLOSED） -->
    <el-dialog v-model="handleVisible" title="平台介入裁决" width="520px" destroy-on-close>
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="退款单号">{{ currentRow?.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          ¥{{ Number(currentRow?.refundAmount ?? 0).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRow?.reason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商家回复">{{ currentRow?.merchantReply || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="90px">
        <el-form-item label="裁决结果" prop="agree">
          <el-radio-group v-model="handleForm.agree">
            <el-radio :value="true">同意退款</el-radio>
            <el-radio :value="false">驳回关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="裁决意见" prop="adminResult">
          <el-input
            v-model="handleForm.adminResult"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请填写裁决意见（必填，T3 7002；同意退款将写退款流水）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">提交裁决</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-A06：A-014 全局售后列表 + A-015 退款裁决（T5 管理员组；T1 售后状态机 6 态）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { A014_listRefunds, A015_handleRefund } from '../../api/admin'
import { REFUND_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const handleVisible = ref(false)
const handleFormRef = ref()
const currentRow = ref(null)

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, status: '' })
const handleForm = reactive({ agree: true, adminResult: '' })
// T5 A-015：adminResult + agree 必填（前端校验与后端 7002 一致）
const handleRules = {
  agree: [{ required: true, message: '请选择裁决结果', trigger: 'change' }],
  adminResult: [{ required: true, message: '裁决意见必填', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await A014_listRefunds({
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

// 裁决退款为敏感操作二次确认（rule 第 5 节）
async function openHandle(row) {
  await ElMessageBox.confirm(
    `对退款单「${row.refundNo}」进行平台裁决？同意退款将写入退款流水，驳回则关闭售后单。`,
    '平台介入裁决',
    { type: 'warning', confirmButtonText: '开始裁决' }
  )
  currentRow.value = row
  handleForm.agree = true
  handleForm.adminResult = ''
  handleVisible.value = true
}

async function submitHandle() {
  await handleFormRef.value.validate()
  saving.value = true
  try {
    // A-015：ADMIN_INTERVENED → REFUNDED（agree=true）/ CLOSED（agree=false），写 audit_logs + payment_records
    await A015_handleRefund(currentRow.value.id, {
      adminResult: handleForm.adminResult,
      agree: handleForm.agree
    })
    ElMessage.success(handleForm.agree ? '裁决完成：同意退款' : '裁决完成：驳回关闭')
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
