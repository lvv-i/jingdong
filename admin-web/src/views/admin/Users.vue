<template>
  <!-- C-A05 用户管理（A-016 用户列表 + A-017 禁用/启用；禁用为敏感操作需二次确认） -->
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
        <div class="toolbar">
          <el-select v-model="query.role" placeholder="按角色筛选" clearable style="width: 130px" @change="handleSearch">
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-input
            v-model="query.keyword"
            placeholder="搜索用户名/手机号"
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
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="phone" label="手机号" width="140">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="roleTag(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'" size="small">
            {{ row.status === 'NORMAL' ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <!-- A-017：禁用/启用（NORMAL → DISABLED / DISABLED → NORMAL） -->
          <el-button
            v-if="row.status === 'NORMAL'"
            link
            type="danger"
            @click="handleToggleStatus(row)"
          >
            禁用
          </el-button>
          <el-button v-else link type="success" @click="handleToggleStatus(row)">
            启用
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无用户数据" />
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
  </el-card>
</template>

<script setup>
// C-A05：A-016 用户列表 + A-017 禁用/启用（T5 管理员组）
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { A016_listUsers, A017_updateUserStatus } from '../../api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, keyword: '', role: '' })

const ROLE_LABELS = { USER: '普通用户', MERCHANT: '商家', ADMIN: '管理员' }
const ROLE_TAGS = { USER: 'info', MERCHANT: 'warning', ADMIN: 'danger' }

function roleLabel(role) {
  return ROLE_LABELS[role] || role
}

function roleTag(role) {
  return ROLE_TAGS[role] || 'info'
}

async function loadList() {
  loading.value = true
  try {
    const res = await A016_listUsers({
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      role: query.role || undefined
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

// 禁用为敏感操作二次确认（rule 第 5 节：禁用用户 A-017）
async function handleToggleStatus(row) {
  const disabling = row.status === 'NORMAL'
  await ElMessageBox.confirm(
    disabling
      ? `禁用用户「${row.username}」后，其登录态将失效，无法继续使用平台。确认禁用？`
      : `确认启用用户「${row.username}」？`,
    disabling ? '禁用用户' : '启用用户',
    { type: 'warning', confirmButtonText: disabling ? '确认禁用' : '确认启用' }
  )
  // A-017：status 参数 NORMAL/DISABLED（T5 请求体约定）
  await A017_updateUserStatus(row.id, { status: disabling ? 'DISABLED' : 'NORMAL' })
  ElMessage.success(disabling ? '用户已禁用' : '用户已启用')
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
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
