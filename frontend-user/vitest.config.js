// Vitest 单元测试配置（用户网页端）
// 环境：jsdom（组件渲染）｜ 别名 @ → src 与 vite 工程一致
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.{test,spec}.js'],
    coverage: {
      provider: 'v8',
      // 覆盖率仅统计本轮要求的三个目标模块
      include: ['src/api/request.js', 'src/views/Login.vue', 'src/views/Cart.vue'],
      reporter: ['text', 'json-summary'],
      thresholds: {
        lines: 80,
        functions: 80,
        statements: 80,
        branches: 75
      }
    }
  }
})