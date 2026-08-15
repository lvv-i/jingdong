<template>
  <!-- C-A01 审核中心（商家入驻审核 A-001~A-003 + 商品上架审核 A-010~A-012；按钮按 T1 状态机显隐） -->
  <el-card shadow="never">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- Tab1：商家入驻审核 -->
      <el-tab-pane label="商家入驻审核" name="merchant">
        <el-tabs v-model="merchantQuery.auditStatus" @tab-change="handleMerchantStatusChange" class="sub-tabs">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane
            v-for="(v, k) in MERCHANT_AUDIT_STATUS"
            :key="k"
            :label="v.label"
            :name="k"
          />
        </el-tabs>
        <el-table :data="merchantList" v-loading="merchantLoading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="shopName" label="店铺名称" min-width="180" />
          <el-table-column prop="userPhone" label="申请人手机" width="140">
            <template #default="{ row }">{{ row.userPhone || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusInfo(MERCHANT_AUDIT_STATUS, row.auditStatus).tag">
                {{ statusInfo(MERCHANT_AUDIT_STATUS, row.auditStatus).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditReason" label="审核意见" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.auditReason || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <!-- T1 入驻状态机：仅 PENDING_AUDIT 可审核（A-002 通过 / A-003 驳回） -->
              <template v-if="row.auditStatus === 'PENDING_AUDIT'">
                <el-button link type="success" @click="openMerchantAudit(row, 'approve')">通过</el-button>
                <el-button link type="danger" @click="openMerchantAudit(row, 'reject')">驳回</el-button>
              </template>
              <span v-else class="readonly-tip">-</span>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty :description="merchantQuery.auditStatus ? '该状态下暂无商家' : '暂无商家数据'" />
          </template>
        </el-table>
        <el-pagination
          v-model:current-page="merchantQuery.page"
          v-model:page-size="merchantQuery.pageSize"
          :total="merchantTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          class="pager"
          @current-change="loadMerchants"
          @size-change="handleMerchantStatusChange"
        />
      </el-tab-pane>

      <!-- Tab2：商品上架审核 -->
      <el-tab-pane label="商品上架审核" name="product">
        <el-table :data="productList" v-loading="productLoading">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="商品标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="merchantName" label="所属店铺" width="160">
            <template #default="{ row }">{{ row.merchantName || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusInfo(PRODUCT_STATUS, row.status).tag">
                {{ statusInfo(PRODUCT_STATUS, row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="提交时间" min-width="160">
            <template #default="{ row }">{{ row.createdAt || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <!-- T1 商品状态机：PENDING_ON_SALE → ON_SALE（A-011 通过）/ DRAFT（A-012 驳回） -->
              <template v-if="row.status === 'PENDING_ON_SALE'">
                <el-button link type="success" @click="openProductAudit(row, 'approve')">通过</el-button>
                <el-button link type="danger" @click="openProductAudit(row, 'reject')">驳回</el-button>
              </template>
              <span v-else class="readonly-tip">-</span>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无待审核商品（A-010 仅返回待上架商品）" />
          </template>
        </el-table>
        <el-pagination
          v-model:current-page="productQuery.page"
          v-model:page-size="productQuery.pageSize"
          :total="productTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          class="pager"
          @current-change="loadProducts"
          @size-change="loadProducts"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 商家审核弹窗（A-002/A-003：auditReason 驳回必填、通过可选） -->
    <el-dialog
      v-model="merchantAuditVisible"
      :title="merchantAuditType === 'approve' ? '商家入驻审核通过' : '商家入驻审核驳回'"
      width="480px"
      destroy-on-close
    >
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="店铺名称">{{ merchantCurrent?.shopName }}</el-descriptions-item>
        <el-descriptions-item label="申请人手机">{{ merchantCurrent?.userPhone || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="merchantFormRef" :model="merchantForm" :rules="merchantRules" label-width="90px">
        <el-form-item label="审核意见" prop="auditReason">
          <el-input
            v-model="merchantForm.auditReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            :placeholder="merchantAuditType === 'approve' ? '审核意见（通过可选，建议填写）' : '请填写驳回原因（必填，T3 7002）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="merchantAuditVisible = false">取消</el-button>
        <el-button :type="merchantAuditType === 'approve' ? 'success' : 'danger'" :loading="saving" @click="submitMerchantAudit">
          {{ merchantAuditType === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 商品审核弹窗（A-011/A-012：auditReason 驳回必填、通过可选） -->
    <el-dialog
      v-model="productAuditVisible"
      :title="productAuditType === 'approve' ? '商品上架审核通过' : '商品上架审核驳回'"
      width="480px"
      destroy-on-close
    >
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="商品标题">{{ productCurrent?.title }}</el-descriptions-item>
        <el-descriptions-item label="所属店铺">{{ productCurrent?.merchantName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="90px">
        <el-form-item label="审核意见" prop="auditReason">
          <el-input
            v-model="productForm.auditReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            :placeholder="productAuditType === 'approve' ? '审核意见（通过可选，建议填写）' : '请填写驳回原因（必填，T3 7002）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productAuditVisible = false">取消</el-button>
        <el-button :type="productAuditType === 'approve' ? 'success' : 'danger'" :loading="saving" @click="submitProductAudit">
          {{ productAuditType === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-A01：A-001 商家列表 + A-002/A-003 入驻审核 + A-010 商品审核列表 + A-011/A-012 上架审核（T5 管理员组）
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  A001_listMerchants,
  A002_approveMerchant,
  A003_rejectMerchant,
  A010_listProductAudits,
  A011_approveProduct,
  A012_rejectProduct
} from '../../api/admin'
import { MERCHANT_AUDIT_STATUS, PRODUCT_STATUS, statusInfo } from '../../utils/status'

const activeTab = ref('merchant')
const saving = ref(false)

// ---- 商家入驻审核 ----
const merchantLoading = ref(false)
const merchantList = ref([])
const merchantTotal = ref(0)
const merchantAuditVisible = ref(false)
const merchantFormRef = ref()
const merchantCurrent = ref(null)
const merchantAuditType = ref('approve')
const merchantQuery = reactive({ page: 1, pageSize: 10, auditStatus: '' })
const merchantForm = reactive({ auditReason: '' })
// 驳回必填、通过可选（T5 A-002/A-003 备注）
const merchantRules = computed(() => ({
  auditReason: merchantAuditType.value === 'reject'
    ? [{ required: true, message: '驳回原因必填', trigger: 'blur' }]
    : []
}))

// ---- 商品上架审核 ----
const productLoading = ref(false)
const productList = ref([])
const productTotal = ref(0)
const productAuditVisible = ref(false)
const productFormRef = ref()
const productCurrent = ref(null)
const productAuditType = ref('approve')
const productQuery = reactive({ page: 1, pageSize: 10 })
const productForm = reactive({ auditReason: '' })
const productRules = computed(() => ({
  auditReason: productAuditType.value === 'reject'
    ? [{ required: true, message: '驳回原因必填', trigger: 'blur' }]
    : []
}))

async function loadMerchants() {
  merchantLoading.value = true
  try {
    const res = await A001_listMerchants({
      page: merchantQuery.page,
      pageSize: merchantQuery.pageSize,
      auditStatus: merchantQuery.auditStatus || undefined
    })
    merchantList.value = res?.list || []
    merchantTotal.value = res?.total || 0
  } finally {
    merchantLoading.value = false
  }
}

function handleMerchantStatusChange() {
  merchantQuery.page = 1
  loadMerchants()
}

async function loadProducts() {
  productLoading.value = true
  try {
    const res = await A010_listProductAudits({
      page: productQuery.page,
      pageSize: productQuery.pageSize
    })
    productList.value = res?.list || []
    productTotal.value = res?.total || 0
  } finally {
    productLoading.value = false
  }
}

function handleTabChange() {
  // 切换到商品审核 tab 时加载
  if (activeTab.value === 'product' && !productList.value.length) loadProducts()
}

function openMerchantAudit(row, type) {
  merchantCurrent.value = row
  merchantAuditType.value = type
  merchantForm.auditReason = ''
  merchantAuditVisible.value = true
}

async function submitMerchantAudit() {
  await merchantFormRef.value.validate()
  saving.value = true
  try {
    const { id } = merchantCurrent.value
    const payload = { auditReason: merchantForm.auditReason || '' }
    if (merchantAuditType.value === 'approve') {
      // A-002：PENDING_AUDIT → APPROVED（写 audit_logs）
      await A002_approveMerchant(id, payload)
      ElMessage.success('商家入驻已通过')
    } else {
      // A-003：PENDING_AUDIT → REJECTED
      await A003_rejectMerchant(id, payload)
      ElMessage.success('商家入驻已驳回')
    }
    merchantAuditVisible.value = false
    loadMerchants()
  } finally {
    saving.value = false
  }
}

function openProductAudit(row, type) {
  productCurrent.value = row
  productAuditType.value = type
  productForm.auditReason = ''
  productAuditVisible.value = true
}

async function submitProductAudit() {
  await productFormRef.value.validate()
  saving.value = true
  try {
    const { id } = productCurrent.value
    const payload = { auditReason: productForm.auditReason || '' }
    if (productAuditType.value === 'approve') {
      // A-011：PENDING_ON_SALE → ON_SALE（写 audit_logs）
      await A011_approveProduct(id, payload)
      ElMessage.success('商品上架审核已通过')
    } else {
      // A-012：PENDING_ON_SALE → DRAFT（退回草稿）
      await A012_rejectProduct(id, payload)
      ElMessage.success('商品上架审核已驳回，商品退回商家草稿')
    }
    productAuditVisible.value = false
    loadProducts()
  } finally {
    saving.value = false
  }
}

onMounted(loadMerchants)
</script>

<style scoped>
.sub-tabs {
  margin-bottom: 4px;
}
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
