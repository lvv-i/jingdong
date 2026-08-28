<template>
  <!-- B-P10 评价页：U-014 拉取未评价明细 + U-024 发表评价（1-5 星 + ≤200 字） -->
  <div class="review-page">
    <div class="page-header">
      <h2>发表评价</h2>
      <el-button plain @click="$router.push(`/orders/${orderId}`)">返回订单详情</el-button>
    </div>

    <!-- 4001 订单不存在/无权访问 -->
    <div v-if="notFound" class="error-block">
      <el-result icon="warning" title="订单不存在" sub-title="订单不存在或无权访问">
        <template #extra>
          <el-button type="primary" @click="$router.push('/orders')">去订单中心</el-button>
        </template>
      </el-result>
    </div>

    <!-- 无可评价明细 -->
    <div v-else-if="!loading && unreviewedItems.length === 0" class="done-block">
      <EmptyState title="没有待评价的商品" description="本订单商品已全部评价完毕">
        <el-button type="primary" @click="$router.push(`/orders/${orderId}`)">返回订单详情</el-button>
      </EmptyState>
    </div>

    <template v-else>
      <!-- 未评价明细选择 -->
      <section class="panel">
        <div class="panel-title">选择评价商品</div>
        <div v-loading="loading" class="item-select">
          <div
            v-for="it in unreviewedItems"
            :key="it.id"
            class="item-option"
            :class="{ active: currentItem?.id === it.id }"
            @click="selectItem(it)"
          >
            <span class="io-title">{{ it.titleSnapshot }}</span>
            <span class="io-price">¥{{ formatPrice(it.priceSnapshot) }} × {{ it.quantity }}</span>
          </div>
        </div>
      </section>

      <!-- 评价表单（U-024） -->
      <section v-if="currentItem" class="panel">
        <div class="panel-title">评价「{{ currentItem.titleSnapshot }}」</div>
        <div class="rating-row">
          <span class="rating-label">商品评分</span>
          <el-rate v-model="form.rating" :texts="['很差', '较差', '一般', '满意', '超赞']" show-text />
        </div>
        <div class="comment-row">
          <span class="rating-label">评价内容</span>
          <el-input
            v-model="form.comment"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="分享你的使用体验（不超过 200 字）"
          />
        </div>
        <div class="submit-row">
          <el-button
            type="danger"
            :loading="submitting"
            :disabled="!form.rating || !form.comment.trim()"
            @click="handleSubmit"
          >
            提交评价
          </el-button>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, createReview } from '@/api/order'
import { formatPrice } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()

const orderId = computed(() => Number(route.params.id))
const order = ref({})
const loading = ref(true)
const notFound = ref(false)
const currentItem = ref(null)
const submitting = ref(false)

const form = reactive({ rating: 0, comment: '' })

/** 未评价明细（reviewedAt 为空 = 未评价，后端驱动） */
const unreviewedItems = computed(
  () => (order.value.items || []).filter((i) => !i.reviewedAt)
)

/** U-014 订单详情（4001 → notFound；query itemId 预选） */
async function loadDetail() {
  loading.value = true
  try {
    order.value = (await getOrderDetail(orderId.value, { silent: true })) || {}
    const items = unreviewedItems.value
    if (!items.length) return
    const qItemId = Number(route.query.itemId)
    const target = items.find((i) => i.id === qItemId) || items[0]
    selectItem(target)
  } catch (err) {
    if (err.code === 4001) notFound.value = true
  } finally {
    loading.value = false
  }
}

function selectItem(item) {
  currentItem.value = item
  form.rating = 0
  form.comment = ''
}

/** U-024 发表评价（成功后刷新明细；全部评价完提示返回） */
async function handleSubmit() {
  if (!form.rating) {
    ElMessage.warning('请先选择评分')
    return
  }
  if (!form.comment.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  submitting.value = true
  try {
    await createReview(
      orderId.value,
      {
        orderItemId: currentItem.value.id,
        rating: form.rating,
        comment: form.comment.trim()
      },
      { silent: true }
    )
    ElMessage.success('评价成功，感谢你的分享')
    await loadDetail()
    if (unreviewedItems.value.length === 0) {
      ElMessage.info('本订单商品已全部评价完毕')
    }
  } catch (err) {
    if ([4001, 4002, 4003].includes(err.code)) {
      ElMessage.warning(err.message)
      loadDetail()
    }
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.review-page {
  max-width: 900px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 14px;
}
.page-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
  border-left: 4px solid #e60012;
  padding-left: 10px;
}
.error-block,
.done-block {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
}
.panel {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 14px;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  border-left: 4px solid #e60012;
  padding-left: 10px;
  margin-bottom: 14px;
}
.item-select {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.item-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 10px 14px;
  cursor: pointer;
  font-size: 14px;
}
.item-option.active {
  border-color: #e60012;
  background: #fff5f5;
}
.io-title {
  flex: 1;
  min-width: 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.io-price {
  color: #666;
  flex-shrink: 0;
  margin-left: 16px;
}
.rating-row,
.comment-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}
.rating-label {
  width: 80px;
  flex-shrink: 0;
  color: #666;
  font-size: 14px;
  line-height: 32px;
}
.comment-row .el-textarea {
  flex: 1;
}
.submit-row {
  display: flex;
  justify-content: flex-end;
}
</style>
