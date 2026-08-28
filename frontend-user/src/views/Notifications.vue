<template>
  <!-- B-P13 消息通知：U-022 列表（readStatus 筛选）+ U-023 单条已读 + U-025 全部已读 -->
  <div class="notify-page">
    <div class="page-header">
      <h2>消息通知</h2>
      <el-button
        type="primary"
        plain
        :disabled="unreadCount === 0"
        :loading="markingAll"
        @click="handleMarkAll"
      >
        全部已读（{{ unreadCount }}）
      </el-button>
    </div>

    <!-- 筛选（readStatus：0未读/1已读/空=全部） -->
    <el-tabs v-model="activeFilter" @tab-change="handleFilterChange">
      <el-tab-pane v-for="tab in NOTIFY_TABS" :key="tab.value" :label="tab.label" :name="tab.value" />
    </el-tabs>

    <!-- 通知列表（U-022） -->
    <div v-loading="loading" class="notify-body">
      <EmptyState
        v-if="!loading && notifications.length === 0"
        title="暂无消息"
        description="订单、售后等消息会在这里通知你"
      />
      <template v-else>
        <div
          v-for="n in notifications"
          :key="n.id"
          class="notify-item"
          :class="{ unread: n.readStatus === 0 }"
          @click="handleRead(n)"
        >
          <div class="ni-head">
            <span class="ni-title">
              <span v-if="n.readStatus === 0" class="ni-dot" />
              {{ n.title }}
            </span>
            <span class="ni-time">{{ formatDateTime(n.createdAt) }}</span>
          </div>
          <div class="ni-content">{{ n.content }}</div>
        </div>
      </template>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotifications, markRead, markAllRead } from '@/api/notification'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const NOTIFY_TABS = [
  { label: '全部', value: '' },
  { label: '未读', value: '0' },
  { label: '已读', value: '1' }
]

const notifications = ref([])
const loading = ref(true)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const activeFilter = ref('')
const markingAll = ref(false)

const unreadCount = computed(
  () => notifications.value.filter((n) => n.readStatus === 0).length
)

/** U-022 通知列表（readStatus 筛选 + 分页） */
async function loadNotifications() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize }
    if (activeFilter.value !== '') params.readStatus = Number(activeFilter.value)
    const data = await getNotifications(params, { silent: true })
    notifications.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    notifications.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** U-023 单条已读（点击未读项标记已读后刷新） */
async function handleRead(n) {
  if (n.readStatus === 1) return
  n.reading = true
  try {
    await markRead(n.id, { silent: true })
    n.readStatus = 1
    if (activeFilter.value === '0') loadNotifications()
  } catch {
    /* 失败保持未读 */
  } finally {
    n.reading = false
  }
}

/** U-025 全部已读 */
async function handleMarkAll() {
  markingAll.value = true
  try {
    await markAllRead({ silent: true })
    ElMessage.success('全部消息已标记为已读')
    loadNotifications()
  } finally {
    markingAll.value = false
  }
}

function handleFilterChange() {
  page.value = 1
  loadNotifications()
}

function handlePageChange(p) {
  page.value = p
  loadNotifications()
}

onMounted(loadNotifications)
</script>

<style scoped>
.notify-page {
  max-width: 900px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 0;
}
.page-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
  border-left: 4px solid #e60012;
  padding-left: 10px;
}
.notify-body {
  background: #fff;
  border-radius: 6px;
  padding: 8px 20px 16px;
  min-height: 140px;
}
.notify-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.notify-item.unread {
  background: #fffaf5;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}
.ni-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.ni-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333;
  font-weight: 600;
}
.ni-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e60012;
  display: inline-block;
}
.ni-time {
  font-size: 12px;
  color: #999;
}
.ni-content {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}
.pager {
  background: #fff;
  border-radius: 6px;
  padding: 12px 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
