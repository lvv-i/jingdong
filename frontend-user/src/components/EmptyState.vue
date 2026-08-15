<template>
  <!-- 空状态公共组件（P1 验收项：无数据不空白、不报错、有提示） -->
  <div class="empty-state">
    <el-empty :description="description">
      <el-button v-if="actionText" type="primary" size="small" @click="$emit('action')">
        {{ actionText }}
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  description: { type: String, default: '暂无数据' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  padding: 32px 0;
}
</style>
