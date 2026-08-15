// Pinia 用户状态（成员B）
// token/userInfo 持久化到 localStorage；刷新页面登录态不丢（W1 验收项）
import { defineStore } from 'pinia'

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: JSON.parse(localStorage.getItem(USER_INFO_KEY) || 'null')
  }),
  getters: {
    /** 是否已登录 */
    isLoggedIn: (state) => !!state.token,
    /** 用户名（导航展示） */
    username: (state) => state.userInfo?.username || '',
    /** 角色：USER/MERCHANT/ADMIN（T4） */
    role: (state) => state.userInfo?.role || '',
    /** 店铺 ID（仅商家） */
    shopId: (state) => state.userInfo?.shopId ?? null
  },
  actions: {
    /** 登录成功后写入（T5 P-002/P-008 返回 {token, userInfo}） */
    setLogin({ token, userInfo }) {
      this.token = token
      this.userInfo = userInfo
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
    },
    /** 退出登录：清内存与 localStorage */
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_INFO_KEY)
    }
  }
})
