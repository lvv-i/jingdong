<template>
  <!-- C-A04 商家管理（A-001 全局商家列表 + 状态查看；审核操作在审核中心 C-A01） -->
  <el-card shadow="never">
    <!-- 状态筛选 tabs（A-001 ?auditStatus=） -->
    <el-tabs v-model="query.auditStatus" @tab-change="handleStatusChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane
        v-for="(v, k) in MERCHANT_AUDIT_STATUS"
        :key="k"
        :label="v.label"
        :name="k"
      />
    </el-tabs>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="shopName" label="店铺名称" min-width="180" />
      <el-table-column prop="userPhone" label="申请人手机" width="140">
        <template #default="{ row }">{{ row.userPhone || '-' }}</template>
      </el-table-column>
      <el-table-column label="入驻状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusInfo(MERCHANT_AUDIT_STATUS, row.auditStatus).tag">
            {{ statusInfo(MERCHANT_AUDIT_STATUS, row.auditStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditReason" label="最近审核意见" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.auditReason || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <!-- 待审核商家引导去审核中心处理（审核按钮集中在 C-A01） -->
          <el-button
            v-if="row.auditStatus === 'PENDING_AUDIT'"
            link
            type="primary"
            @click="$router.push('/admin/audit')"
          >
            去审核
          </el-button>
          <span v-else class="readonly-tip">-</span>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty :description="query.auditStatus ? '该状态下暂无商家' : '暂无商家数据'" />
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
  </el-card>
</template>

<script setup>
// C-A04：A-001 商家列表（T5 管理员组；状态只读，审核入口引导至审核中心）
import { reactive, ref, onMounted } from 'vue'
import { A001_listMerchants } from '../../api/admin'
import { MERCHANT_AUDIT_STATUS, statusInfo } from '../../utils/status'

const loading = ref(false)
const list = ref([])
const total = ref(0)

// 分页对齐 api-contract：page/pageSize → list/total
const query = reactive({ page: 1, pageSize: 10, auditStatus: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await A001_listMerchants({
      page: query.page,
      pageSize: query.pageSize,
      auditStatus: query.auditStatus || undefined
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

onMounted(loadList)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.readonly-tip {
  color: #c0c4cc;
}
</style>
