<template>
  <!-- C-M03 库存管理（M-004 商品列表 + M-008 快捷改库存；与商品管理联动） -->
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>库存管理</span>
        <div class="toolbar">
          <el-input
            v-model="query.keyword"
            placeholder="搜索商品标题"
            clearable
            style="width: 220px"
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
      <el-table-column label="商品" min-width="220">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              fit="cover"
              class="thumb"
            />
            <div v-else class="thumb placeholder">无图</div>
            <span class="title">{{ row.title }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="110">
        <template #default="{ row }">¥{{ Number(row.price ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="库存" width="120">
        <template #default="{ row }">
          <span :class="{ 'stock-low': row.stock !== undefined && row.stock < 10 }">
            {{ row.stock }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="salesCount" label="销量" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusInfo(PRODUCT_STATUS, row.status).tag">
            {{ statusInfo(PRODUCT_STATUS, row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <!-- M-008 仅本店，无状态限制（T5 备注） -->
          <el-button link type="primary" @click="openStockEdit(row)">改库存</el-button>
        </template>
      </el-table-column>
      <!-- 空状态（rule 第 5 节：插画 + 引导文案） -->
      <template #empty>
        <el-empty description="暂无商品数据，先去商品管理创建商品吧">
          <el-button type="primary" @click="$router.push('/merchant/products')">去创建商品</el-button>
        </el-empty>
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

    <!-- 改库存弹窗（M-008 PUT /api/merchant/products/{id}/stock） -->
    <el-dialog v-model="stockVisible" title="修改库存" width="420px" destroy-on-close>
      <el-form ref="stockFormRef" :model="stockForm" :rules="stockRules" label-width="80px">
        <el-form-item label="商品">
          <span>{{ currentRow?.title }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ currentRow?.stock }}</span>
        </el-form-item>
        <el-form-item label="新库存" prop="stock">
          <el-input-number v-model="stockForm.stock" :min="0" :max="999999" style="width: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleStockSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-M03：M-004 商品列表 + M-008 修改库存（T5 商家组；库存是 M-003/M-008 的 stock 字段）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { M004_listProducts, M008_updateStock } from '../../api/merchant'
import { PRODUCT_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const stockVisible = ref(false)
const stockFormRef = ref()
const currentRow = ref(null)

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, keyword: '' })
const stockForm = reactive({ stock: 0 })
const stockRules = {
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await M004_listProducts({
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined
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

function openStockEdit(row) {
  currentRow.value = row
  stockForm.stock = row.stock ?? 0
  stockVisible.value = true
}

async function handleStockSave() {
  await stockFormRef.value.validate()
  saving.value = true
  try {
    await M008_updateStock(currentRow.value.id, { stock: stockForm.stock })
    ElMessage.success('库存已更新')
    stockVisible.value = false
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
.stock-low {
  color: #e4393c;
  font-weight: 600;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
