<template>
  <el-container class="layout">
    <!-- 侧边菜单：按角色渲染（C-01 页面清单） -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span v-if="role === 'MERCHANT'">商家后台</span>
        <span v-else>管理员后台</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#ffd04b"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-title">{{ route.meta.title }}</div>
        <div class="header-user">
          <el-tag :type="role === 'MERCHANT' ? 'warning' : 'danger'" size="small">
            {{ role === 'MERCHANT' ? '商家' : '管理员' }}
          </el-tag>
          <span class="username">{{ userStore.username }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
// C2 任务书：角色化布局（侧边菜单按 role 渲染两套菜单树，页面清单见 C-01）
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const role = computed(() => userStore.role)

// 菜单树 = 路由表中当前角色前缀下的子路由（title/icon 来自 meta）
const menus = computed(() => {
  const parentPath = role.value === 'MERCHANT' ? '/merchant' : '/admin'
  const parentRoute = router.getRoutes().find((r) => r.path === parentPath)
  if (!parentRoute || !parentRoute.children) return []
  return parentRoute.children
    .map((c) => ({
      path: `${parentPath}/${c.path}`,
      title: c.meta?.title || '',
      icon: c.meta?.icon || 'Menu'
    }))
    .filter((m) => m.title)
})

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background-color: #1f2d3d;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #18222f;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
}
.username {
  font-size: 14px;
}
.main {
  background: #f0f2f5;
}
</style>
