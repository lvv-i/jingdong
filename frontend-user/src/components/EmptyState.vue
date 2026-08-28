<template>
  <!-- 空状态公共组件（P1 验收项：无数据不空白、不报错、有提示）
       title：可选大标题；description：描述；actionText + @action 或默认 slot：操作按钮 -->
  <div class="empty-state">
    <el-empty :description="description">
      <div v-if="title" class="empty-title">{{ title }}</div>
      <template v-if="$slots.default">
        <slot />
      </template>
      <el-button v-else-if="actionText" type="primary" size="small" @click="$emit('action')">
        {{ actionText }}
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  description: { type: String, default: '暂无数据' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  padding: 32px 0;
}
.empty-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 4px;
}
</style>
