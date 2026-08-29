<template>
  <!-- 全局布局：顶部导航 + 主体（W1 骨架）
       搜索入口 / 购物车角标 / 消息入口 / 登录态展示 -->
  <div class="app-shell">
    <header class="top-bar">
      <div class="top-bar-inner">
        <router-link to="/" class="brand">
          <span class="brand-logo">京选</span>
          <span class="brand-name">京选商城</span>
        </router-link>

        <div class="search-box">
          <el-icon><Search /></el-icon>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索商品"
            @keyup.enter="goSearch"
          />
          <el-button class="search-btn" size="small" @click="goSearch">搜索</el-button>
        </div>

        <nav class="top-nav">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/orders">我的订单</router-link>
            <router-link to="/refunds">售后</router-link>
            <router-link to="/notifications">消息</router-link>
            <router-link to="/profile">
              <el-icon><User /></el-icon>
              {{ userStore.username }}
            </router-link>
            <a class="logout-link" @click.prevent="handleLogout">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">请登录</router-link>
            <router-link to="/login?tab=register" class="register-link">免费注册</router-link>
          </template>
          <router-link to="/cart" class="cart-entry">
            <el-badge :value="cartCount" :hidden="cartCount === 0">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </el-badge>
            购物车
          </router-link>
        </nav>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCartItems } from '@/api/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const cartCount = ref(0)

// 购物车角标（U-008 实时件数；未登录不显示）
async function loadCartCount() {
  if (!userStore.isLoggedIn) {
    cartCount.value = 0
    return
  }
  try {
    const list = await getCartItems({ silent: true })
    cartCount.value = (Array.isArray(list) ? list : []).reduce((sum, i) => sum + (Number(i.quantity) || 0), 0)
  } catch {
    cartCount.value = 0
  }
}

function goSearch() {
  const kw = searchKeyword.value.trim()
  router.push(kw ? { path: '/search', query: { keyword: kw } } : '/search')
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}

watch(() => userStore.isLoggedIn, () => loadCartCount())
// 路由变化时刷新角标（加购/删购物车后返回、登录跳转等场景）
watch(() => route.path, () => loadCartCount())
onMounted(() => loadCartCount())
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f5f5f5;
}
.top-bar {
  background: #fff;
  border-bottom: 2px solid #e60012;
  position: sticky;
  top: 0;
  z-index: 100;
}
.top-bar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 0 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
}
.brand-logo {
  background: #e60012;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 6px;
}
.brand-name {
  color: #e60012;
  font-size: 22px;
  font-weight: 700;
}
.search-box {
  flex: 1;
  max-width: 420px;
  height: 36px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 6px 0 14px;
  border: 2px solid #e60012;
  border-radius: 18px;
  color: #909399;
  font-size: 14px;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  background: transparent;
}
.search-btn {
  border: none;
  background: #e60012;
  color: #fff;
  border-radius: 14px;
}
.top-nav {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  margin-left: auto;
}
.top-nav a {
  color: #333;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.top-nav a:hover {
  color: #e60012;
}
.register-link {
  color: #e60012;
}
.logout-link {
  cursor: pointer;
}
.cart-entry {
  border-left: 1px solid #eee;
  padding-left: 20px;
}
.app-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}
</style>
