// 路由骨架（成员B）：13 页路由表 + 登录守卫
// 依据：docs/phase1/member-b/deliverables/B-01-用户端页面清单.md（B-P01~B-P13）
// 守卫规则（W1 验收项）：
//   - 未登录访问需登录页 → 跳 /login
//   - 已登录访问 /login → 跳首页
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // B-P01 首页（公开）
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  // B-P02 搜索/列表页（公开）
  { path: '/search', name: 'Search', component: () => import('@/views/Search.vue') },
  // B-P03 商品详情（公开）
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
  // B-P04 登录/注册（公开，已登录跳首页）
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  // B-P05 购物车（需登录）
  { path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: true } },
  // B-P06 结算页（需登录）
  { path: '/checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue'), meta: { requiresAuth: true } },
  // B-P07 订单中心（需登录）
  { path: '/orders', name: 'OrderList', component: () => import('@/views/OrderList.vue'), meta: { requiresAuth: true } },
  // B-P08 订单详情（需登录）
  { path: '/orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { requiresAuth: true } },
  // B-P09 售后中心（需登录）
  { path: '/refunds', name: 'RefundCenter', component: () => import('@/views/RefundCenter.vue'), meta: { requiresAuth: true } },
  // B-P10 评价（需登录）
  { path: '/orders/:id/review', name: 'Review', component: () => import('@/views/Review.vue'), meta: { requiresAuth: true } },
  // B-P11 个人中心（需登录）
  { path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { requiresAuth: true } },
  // B-P12 地址管理（需登录）
  { path: '/addresses', name: 'AddressList', component: () => import('@/views/AddressList.vue'), meta: { requiresAuth: true } },
  // B-P13 消息通知（需登录）
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/Notifications.vue'), meta: { requiresAuth: true } },
  // 兜底：未知路径回首页
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  // 站点基础路径：本地开发为 /；GitHub Pages 部署时设为 /jingdong/（构建时 VITE_ROUTER_BASE 传入）
  history: createWebHistory(import.meta.env.VITE_ROUTER_BASE || '/'),
  routes
})

// 全局前置守卫
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 未登录访问受保护页 → 登录页（带回跳地址）
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && userStore.isLoggedIn) {
    // 已登录访问登录页 → 首页
    return { path: '/' }
  }
  return true
})

export default router
