import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 用户网页端工程配置（成员B）
// 依据：.qoder/members/member-b/rules/frontend-structure.md
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    // 监听所有网卡（0.0.0.0），局域网内其他电脑可通过 http://本机IP:5173 访问
    host: true,
    // 开发代理：/api → 后端 8080（T5 契约统一前缀 /api）
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
