<template>
  <!-- C-M02 商品管理（M-003 新建 / M-004 列表 / M-005 编辑 / M-006 提交上架 / M-007 下架；按钮按 T1 商品状态机显隐） -->
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <!-- 状态筛选 tabs（M-004 ?status=） -->
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
          <el-button type="primary" :icon="Plus" @click="openCreate">新建商品</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="220">
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
      <el-table-column prop="salesCount" label="销量" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusInfo(PRODUCT_STATUS, row.status).tag">
            {{ statusInfo(PRODUCT_STATUS, row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <!-- T1 商品状态机按钮显隐（禁止流转入口一律不渲染）：
               DRAFT：编辑 / 提交上架（→ PENDING_ON_SALE）
               PENDING_ON_SALE：只读（等待管理员审核，无操作入口）
               ON_SALE：下架（→ OFF_SALE）
               OFF_SALE：编辑 / 重新提交上架（→ PENDING_ON_SALE，重新走审核） -->
          <template v-if="row.status === 'DRAFT'">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleSubmit(row)">提交上架</el-button>
          </template>
          <template v-else-if="row.status === 'ON_SALE'">
            <el-button link type="danger" @click="handleOff(row)">下架</el-button>
          </template>
          <template v-else-if="row.status === 'OFF_SALE'">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleSubmit(row)">重新提交</el-button>
          </template>
          <span v-else class="readonly-tip">等待审核</span>
        </template>
      </el-table-column>
      <!-- 空状态（rule 第 5 节：插画 + 引导文案） -->
      <template #empty>
        <el-empty :description="query.status ? '该状态下暂无商品' : '暂无商品，创建第一件商品开始经营吧'">
          <el-button v-if="!query.status" type="primary" @click="openCreate">新建商品</el-button>
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
      @size-change="handleStatusChange"
    />

    <!-- 新建/编辑弹窗（M-003 POST / M-005 PUT：字段与 T5 请求参数一致） -->
    <el-dialog
      v-model="formVisible"
      :title="editingId ? '编辑商品' : '新建商品'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="请输入商品标题（必填）" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subTitle" maxlength="60" show-word-limit placeholder="卖点短句，可选" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="类目" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                node-key="id"
                check-strictly
                placeholder="选择类目（必填）"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :max="999999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="主图 URL" prop="mainImage">
          <el-input v-model="form.mainImage" placeholder="粘贴图片链接，如 https://xxx.jpg（必填，T3 3006）" />
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="图文详情文字描述（可选）"
          />
        </el-form-item>
        <el-form-item label="轮播图 URL">
          <div class="images-editor">
            <div v-for="(url, idx) in form.images" :key="idx" class="image-row">
              <el-input v-model="form.images[idx]" placeholder="图片链接" />
              <el-button link type="danger" :icon="Delete" @click="removeImage(idx)" />
            </div>
            <el-button link type="primary" :icon="Plus" @click="addImage">添加图片</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ editingId ? '保存修改' : '保存草稿' }}
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-M02：M-003 新建 + M-004 列表 + M-005 编辑 + M-006 提交上架审核 + M-007 下架（T5 商家组）
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import {
  M003_createProduct,
  M004_listProducts,
  M005_updateProduct,
  M006_submitProduct,
  M007_offProduct
} from '../../api/merchant'
import { P003_categories } from '../../api/auth'
import { PRODUCT_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const formVisible = ref(false)
const formRef = ref()
const editingId = ref(null)
const categories = ref([])

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, status: '', keyword: '' })

// 表单字段与 T5 M-003 请求参数一致（categoryId/title/price/stock/mainImage 必填）
const form = reactive({
  categoryId: null,
  title: '',
  subTitle: '',
  price: null,
  originalPrice: null,
  stock: 0,
  mainImage: '',
  detail: '',
  images: []
})

// 必填校验对齐 T3 3006（标题/价格/库存/主图/类目）
const formRules = {
  categoryId: [{ required: true, message: '请选择商品类目', trigger: 'change' }],
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }],
  mainImage: [{ required: true, message: '请填写主图链接', trigger: 'blur' }]
}

// P-003 返回平铺 list[{id,parentId,name}]，转为树供 el-tree-select 使用
const categoryTree = computed(() => buildTree(categories.value))

function buildTree(list) {
  const map = {}
  const roots = []
  list.forEach((item) => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach((item) => {
    const node = map[item.id]
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

function addImage() {
  form.images.push('')
}

function removeImage(idx) {
  form.images.splice(idx, 1)
}

async function loadList() {
  loading.value = true
  try {
    const res = await M004_listProducts({
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

async function loadCategories() {
  try {
    const res = await P003_categories()
    categories.value = res?.list || []
  } catch {
    categories.value = []
  }
}

function resetForm() {
  Object.assign(form, {
    categoryId: null,
    title: '',
    subTitle: '',
    price: null,
    originalPrice: null,
    stock: 0,
    mainImage: '',
    detail: '',
    images: []
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  formVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  resetForm()
  Object.assign(form, {
    categoryId: row.categoryId ?? null,
    title: row.title || '',
    subTitle: row.subTitle || '',
    price: row.price ?? null,
    originalPrice: row.originalPrice ?? null,
    stock: row.stock ?? 0,
    mainImage: row.mainImage || '',
    detail: row.detail || '',
    images: Array.isArray(row.images) ? [...row.images] : []
  })
  formVisible.value = true
}

function buildPayload() {
  return {
    categoryId: form.categoryId,
    title: form.title,
    subTitle: form.subTitle || '',
    price: form.price,
    originalPrice: form.originalPrice || null,
    stock: form.stock,
    mainImage: form.mainImage,
    detail: form.detail || '',
    images: form.images.filter((u) => u.trim())
  }
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      // M-005：仅 DRAFT/OFF_SALE 可编辑（后端校验 3005）
      await M005_updateProduct(editingId.value, payload)
      ElMessage.success('商品已更新')
    } else {
      // M-003：创建为草稿 DRAFT（images 写入 product_images）
      await M003_createProduct(payload)
      ElMessage.success('商品已创建为草稿，可在列表提交上架审核')
    }
    formVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

// M-006：DRAFT / OFF_SALE → PENDING_ON_SALE（T1 合法流转；重新上架走重新审核）
async function handleSubmit(row) {
  await ElMessageBox.confirm(
    '提交后需等待管理员审核，通过后商品才在用户端可见。确认提交上架申请？',
    '提交上架审核',
    { type: 'warning', confirmButtonText: '确认提交' }
  )
  await M006_submitProduct(row.id)
  ElMessage.success('已提交上架申请，等待管理员审核')
  loadList()
}

// M-007：ON_SALE → OFF_SALE（商家主动下架；敏感操作二次确认）
async function handleOff(row) {
  await ElMessageBox.confirm(
    '下架后商品将在用户端不可见，重新上架需再次提交审核。确认下架该商品？',
    '商品下架',
    { type: 'warning', confirmButtonText: '确认下架' }
  )
  await M007_offProduct(row.id)
  ElMessage.success('商品已下架')
  loadList()
}

onMounted(() => {
  loadList()
  loadCategories()
})
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
.readonly-tip {
  color: #c0c4cc;
  font-size: 12px;
}
.images-editor {
  width: 100%;
}
.image-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
</style>
