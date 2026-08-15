<template>
  <!-- C-M06 店铺设置（M-001 资料展示 + M-002 编辑；入驻状态按 T1 三态显隐按钮） -->
  <div class="shop-page">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>店铺资料</span>
          <!-- T1 入驻状态机：仅 APPROVED 可编辑；REJECTED 可修改重提（修改后重新提交审核） -->
          <el-button
            v-if="shop.auditStatus === 'APPROVED'"
            type="primary"
            :icon="Edit"
            @click="openEdit"
          >
            编辑资料
          </el-button>
          <el-button
            v-else-if="shop.auditStatus === 'REJECTED'"
            type="primary"
            :icon="Edit"
            @click="openEdit"
          >
            修改并重新提交
          </el-button>
        </div>
      </template>

      <!-- 审核状态横幅（T3：6003 审核中 / 6005 已驳回的页面级引导，不裸弹错误码） -->
      <el-alert
        v-if="shop.auditStatus === 'PENDING_AUDIT'"
        type="warning"
        show-icon
        :closable="false"
        title="店铺审核中，通过后即可发布商品与处理订单。审核期间资料不可修改。"
        class="mb16"
      />
      <el-alert
        v-if="shop.auditStatus === 'REJECTED'"
        type="error"
        show-icon
        :closable="false"
        class="mb16"
      >
        <template #title>
          入驻申请被驳回：{{ shop.auditReason || '未填写原因' }}。请修改资料后重新提交审核。
        </template>
      </el-alert>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="店铺名称">{{ shop.shopName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入驻状态">
          <el-tag v-if="shop.auditStatus" :type="statusInfo(MERCHANT_AUDIT_STATUS, shop.auditStatus).tag">
            {{ statusInfo(MERCHANT_AUDIT_STATUS, shop.auditStatus).label }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="主营类目">{{ categoryName }}</el-descriptions-item>
        <el-descriptions-item label="店铺简介">{{ shop.description || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="shop.auditReason" label="审核意见" :span="2">
          {{ shop.auditReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 编辑弹窗（M-002 PUT /api/merchant/shop：shopName/categoryId/description） -->
    <el-dialog
      v-model="editVisible"
      :title="shop.auditStatus === 'REJECTED' ? '修改店铺资料并重新提交' : '编辑店铺资料'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" maxlength="50" show-word-limit placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="主营类目" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            node-key="id"
            check-strictly
            placeholder="请选择主营类目"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="店铺简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="介绍店铺特色、经营范围等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// C-M06：M-001 店铺资料 + M-002 编辑店铺（T5 商家组）；类目选择用 P-003 公共类目树
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { M001_getShop, M002_updateShop } from '../../api/merchant'
import { P003_categories } from '../../api/auth'
import { MERCHANT_AUDIT_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const saving = ref(false)
const editVisible = ref(false)
const formRef = ref()
const shop = reactive({})
const categories = ref([])

const form = reactive({ shopName: '', categoryId: null, description: '' })

// 必填校验与 T5 M-002 请求参数一致（shopName/categoryId 必填）
const rules = {
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择主营类目', trigger: 'change' }]
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

const categoryName = computed(() => {
  const found = categories.value.find((c) => c.id === shop.categoryId)
  return found ? found.name : (shop.categoryId ? `类目#${shop.categoryId}` : '-')
})

async function loadShop() {
  loading.value = true
  try {
    const s = await M001_getShop()
    Object.assign(shop, s || {})
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await P003_listCategories()
    categories.value = res?.list || []
  } catch {
    categories.value = []
  }
}

function openEdit() {
  form.shopName = shop.shopName || ''
  form.categoryId = shop.categoryId ?? null
  form.description = shop.description || ''
  editVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    // M-002：非 APPROVED 不可编辑（后端校验 6002/6003/6005）；REJECTED 修改即重新提交
    await M002_updateShop({
      shopName: form.shopName,
      categoryId: form.categoryId,
      description: form.description
    })
    ElMessage.success(
      shop.auditStatus === 'REJECTED' ? '已重新提交入驻申请，等待审核' : '店铺资料已保存'
    )
    editVisible.value = false
    loadShop()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadShop()
  loadCategories()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb16 {
  margin-bottom: 16px;
}
</style>
