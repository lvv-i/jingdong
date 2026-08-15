<template>
  <!-- C-A02 类目管理（A-004 树 + A-005 新增 / A-006 编辑启停 / A-007 删除） -->
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>类目管理</span>
        <div class="toolbar">
          <el-button type="primary" :icon="Plus" @click="openCreate(null)">新增一级类目</el-button>
        </div>
      </div>
    </template>

    <div v-loading="loading">
      <el-tree
        :data="tree"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        :props="{ label: 'name', children: 'children' }"
      >
        <template #default="{ data }">
          <div class="tree-node">
            <span class="node-name">{{ data.name }}</span>
            <el-tag v-if="data.status === 'DISABLED'" type="info" size="small">已停用</el-tag>
            <span class="node-actions">
              <el-button link type="primary" size="small" @click="openCreate(data)">添加子类目</el-button>
              <el-button link type="primary" size="small" @click="openEdit(data)">编辑</el-button>
              <el-button
                link
                type="danger"
                size="small"
                @click="handleDelete(data)"
              >删除</el-button>
            </span>
          </div>
        </template>
      </el-tree>
      <el-empty v-if="!loading && !tree.length" description="暂无类目，先创建一级类目吧" />
    </div>

    <!-- 新增类目弹窗（A-005 POST：parentId/name/sortOrder） -->
    <el-dialog
      v-model="createVisible"
      :title="parentNode ? `在「${parentNode.name}」下新增子类目` : '新增一级类目'"
      width="440px"
      destroy-on-close
    >
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="类目名称" prop="name">
          <el-input v-model="createForm.name" maxlength="20" show-word-limit placeholder="请输入类目名称" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="createForm.sortOrder" :min="0" :max="9999" style="width: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑类目弹窗（A-006 PUT：name/sortOrder/status） -->
    <el-dialog v-model="editVisible" title="编辑类目" width="440px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="类目名称" prop="name">
          <el-input v-model="editForm.name" maxlength="20" show-word-limit placeholder="请输入类目名称" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="editForm.sortOrder" :min="0" :max="9999" style="width: 200px" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="editForm.status"
            active-value="NORMAL"
            inactive-value="DISABLED"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
// C-A02：A-004 类目树 + A-005 新增 + A-006 编辑/启停 + A-007 删除（T5 管理员组）
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  A004_listCategories,
  A005_createCategory,
  A006_updateCategory,
  A007_deleteCategory
} from '../../api/admin'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const createVisible = ref(false)
const editVisible = ref(false)
const createFormRef = ref()
const editFormRef = ref()
const parentNode = ref(null)
const editingNode = ref(null)

const createForm = reactive({ name: '', sortOrder: 0 })
const editForm = reactive({ name: '', sortOrder: 0, status: 'NORMAL' })

const createRules = {
  name: [{ required: true, message: '请输入类目名称', trigger: 'blur' }]
}
const editRules = {
  name: [{ required: true, message: '请输入类目名称', trigger: 'blur' }]
}

// A-004 返回平铺 list[{id,parentId,name,sortOrder,status}]，转树
const tree = computed(() => buildTree(list.value))

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

async function loadList() {
  loading.value = true
  try {
    const res = await A004_listCategories()
    list.value = res?.list || []
  } finally {
    loading.value = false
  }
}

function openCreate(parent) {
  parentNode.value = parent
  createForm.name = ''
  createForm.sortOrder = 0
  createVisible.value = true
}

async function submitCreate() {
  await createFormRef.value.validate()
  saving.value = true
  try {
    // A-005：parentId 为空则一级类目
    await A005_createCategory({
      parentId: parentNode.value?.id ?? null,
      name: createForm.name,
      sortOrder: createForm.sortOrder
    })
    ElMessage.success('类目已创建')
    createVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

function openEdit(node) {
  editingNode.value = node
  editForm.name = node.name
  editForm.sortOrder = node.sortOrder ?? 0
  editForm.status = node.status || 'NORMAL'
  editVisible.value = true
}

async function submitEdit() {
  await editFormRef.value.validate()
  saving.value = true
  try {
    // A-006：name/sortOrder/status（启停用）
    await A006_updateCategory(editingNode.value.id, {
      name: editForm.name,
      sortOrder: editForm.sortOrder,
      status: editForm.status
    })
    ElMessage.success('类目已更新')
    editVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(node) {
  // 敏感操作二次确认（rule 第 5 节）
  await ElMessageBox.confirm(
    `确认删除类目「${node.name}」吗？删除为软删除，该类目下有商品时后端会拒绝。`,
    '删除类目',
    { type: 'warning', confirmButtonText: '确认删除' }
  )
  // A-007：软删除；有商品时后端拒绝并提示
  await A007_deleteCategory(node.id)
  ElMessage.success('类目已删除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 8px;
}
.node-name {
  flex: 1;
}
.node-actions {
  opacity: 0;
  transition: opacity 0.2s;
}
.tree-node:hover .node-actions {
  opacity: 1;
}
</style>
