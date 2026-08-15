/**
 * 路由表 + 角色守卫（C2 任务书）
 * 菜单依据：docs/phase1/member-c/deliverables/C-01-后台页面清单.md（商家 6 页 + 管理员 7 页）
 * meta.role：MERCHANT / ADMIN；/login 公开
 */
import { createRouter, createWebHistory } from 'vue-router'

const Layout = () => import('../layout/index.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  // ===== 商家后台（仅 MERCHANT） =====
  {
    path: '/merchant',
    component: Layout,
    redirect: '/merchant/dashboard',
    meta: { role: 'MERCHANT', title: '商家后台' },
    children: [
      { path: 'dashboard', name: 'MerchantDashboard', component: () => import('../views/merchant/Dashboard.vue'), meta: { role: 'MERCHANT', title: '店铺仪表盘', icon: 'DataLine' } },
      { path: 'products', name: 'MerchantProducts', component: () => import('../views/merchant/Products.vue'), meta: { role: 'MERCHANT', title: '商品管理', icon: 'Goods' } },
      { path: 'stock', name: 'MerchantStock', component: () => import('../views/merchant/Stock.vue'), meta: { role: 'MERCHANT', title: '库存管理', icon: 'Box' } },
      { path: 'orders', name: 'MerchantOrders', component: () => import('../views/merchant/Orders.vue'), meta: { role: 'MERCHANT', title: '订单处理', icon: 'List' } },
      { path: 'refunds', name: 'MerchantRefunds', component: () => import('../views/merchant/Refunds.vue'), meta: { role: 'MERCHANT', title: '售后处理', icon: 'RefreshLeft' } },
      { path: 'shop', name: 'MerchantShop', component: () => import('../views/merchant/Shop.vue'), meta: { role: 'MERCHANT', title: '店铺设置', icon: 'Shop' } }
    ]
  },
  // ===== 管理员后台（仅 ADMIN） =====
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/audit',
    meta: { role: 'ADMIN', title: '管理员后台' },
    children: [
      { path: 'audit', name: 'AdminAudit', component: () => import('../views/admin/Audit.vue'), meta: { role: 'ADMIN', title: '审核中心', icon: 'Checked' } },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/Categories.vue'), meta: { role: 'ADMIN', title: '类目管理', icon: 'Menu' } },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/Products.vue'), meta: { role: 'ADMIN', title: '商品巡检', icon: 'Search' } },
      { path: 'merchants', name: 'AdminMerchants', component: () => import('../views/admin/Merchants.vue'), meta: { role: 'ADMIN', title: '商家管理', icon: 'OfficeBuilding' } },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue'), meta: { role: 'ADMIN', title: '用户管理', icon: 'User' } },
      { path: 'refunds', name: 'AdminRefunds', component: () => import('../views/admin/Refunds.vue'), meta: { role: 'ADMIN', title: '争议处理', icon: 'Warning' } },
      { path: 'logs', name: 'AdminLogs', component: () => import('../views/admin/Logs.vue'), meta: { role: 'ADMIN', title: '日志统计', icon: 'Document' } }
    ]
  },
  // ===== 兜底 =====
  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：未登录跳登录；角色与路由 meta.role 不匹配 → 提示并停留
router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  let userInfo = null
  try {
    userInfo = JSON.parse(localStorage.getItem('admin_user') || 'null')
  } catch (e) {
    userInfo = null
  }

  if (to.path === '/login') {
    // 已登录访问登录页 → 按角色回自己首页
    if (token && userInfo) {
      return userInfo.role === 'MERCHANT' ? '/merchant/dashboard' : '/admin/audit'
    }
    return true
  }

  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 角色不匹配（如商家访问 /admin/**）：提示并停留各自首页（对应 1003 语义）
  if (to.meta.role && userInfo && userInfo.role !== to.meta.role) {
    import('element-plus').then(({ ElMessage }) => {
      ElMessage.warning('无权限访问该模块')
    })
    return userInfo.role === 'MERCHANT' ? '/merchant/dashboard' : '/admin/audit'
  }

  return true
})

// 标题同步
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 后台管理` : '京东风格电商平台 · 后台管理'
})

export default router
