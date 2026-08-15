<template>
  <!-- C-A03 商品巡检（A-008 全局商品列表 + A-009 强制下架；仅 ON_SALE 可下架，T1 商品状态机） -->
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <!-- 状态筛选 tabs（A-008 ?status=） -->
        <el-tabs v-model="query.status" @tab-change="handleStatusChange" class="status-tabs">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane
            v-for="(v, k) in PRODUCT_STATUS"
            :key="k"
            :label="v.label"
            :name="k"
          />
        </el-tabs>
        <div class="toolbar">
          <el-input
            v-model="query.keyword"
            placeholder="搜索商品标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
      </div>
    </template>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image v-if="row.mainImage" :src="row.mainImage" fit="cover" class="thumb" />
            <div v-else class="thumb placeholder">无图</div>
            <span class="title">{{ row.title }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="110">
        <template #default="{ row }">¥{{ Number(row.price ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
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
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <!-- T1 商品状态机：仅 ON_SALE 可强制下架（A-009，auditReason 必填 + 写 audit_log） -->
          <el-button v-if="row.status === 'ON_SALE'" link type="danger" @click="openTakeDown(row)">
            强制下架
          </el-button>
          <span v-else class="readonly-tip">-</span>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty :description="query.status ? '该状态下暂无商品' : '暂无商品数据'" />
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

    <!-- 强制下架弹窗（A-009 POST /api/admin/products/{id}/take-down：ON_SALE → OFF_SALE，auditReason 必填） -->
    <el-dialog v-model="takeDownVisible" title="强制下架商品" width="480px" destroy-on-close>
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="商品标题">{{ currentRow?.title }}</el-descriptions-item>
        <el-descriptions-item label="所属店铺">{{ currentRow?.merchantName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="takeDownFormRef" :model="takeDownForm" :rules="takeDownRules" label-width="90px">
        <el-form-item label="下架原因" prop="auditReason">
          <el-input
            v-model="takeDownForm.auditReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请填写强制下架原因（必填，T3 7002；操作将写入审计日志）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="takeDownVisible = false">取消</el-button>
        <el-button type="danger" :loading="saving" @click="submitTakeDown">确认下架</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-A03：A-008 全局商品列表 + A-009 巡检强制下架（T5 管理员组；T1 商品状态机）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { A008_listProducts, A009_takeDownProduct } from '../../api/admin'
import { PRODUCT_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const takeDownVisible = ref(false)
const takeDownFormRef = ref()
const currentRow = ref(null)

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, status: '', keyword: '' })
const takeDownForm = reactive({ auditReason: '' })
// T5 A-009：auditReason 必填（前端校验与后端 7002 一致）
const takeDownRules = {
  auditReason: [{ required: true, message: '下架原因必填', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await A008_listProducts({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
      keyword: query.keyword || undefined
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

function handleSearch() {
  query.page = 1
  loadList()
}

// 敏感操作二次确认（rule 第 5 节：强制下架 A-009）
async function openTakeDown(row) {
  await ElMessageBox.confirm(
    `强制下架将立即在用户端隐藏商品「${row.title}」，且操作写入审计日志。确认继续？`,
    '强制下架',
    { type: 'warning', confirmButtonText: '继续' }
  )
  currentRow.value = row
  takeDownForm.auditReason = ''
  takeDownVisible.value = true
}

async function submitTakeDown() {
  await takeDownFormRef.value.validate()
  saving.value = true
  try {
    // A-009：ON_SALE → OFF_SALE（T1 合法流转），写 audit_logs(PRODUCT/TAKE_DOWN)
    await A009_takeDownProduct(currentRow.value.id, { auditReason: takeDownForm.auditReason })
    ElMessage.success('商品已强制下架')
    takeDownVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
.status-tabs {
  flex: 1;
}
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 15px;
}
.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
}
.thumb.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #909399;
  font-size: 12px;
}
.title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
