import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 京东风格电商平台-后台共用工程（成员C）
// dev 代理：/api -> 后端 Spring Boot（端口 8080，见 backend/src/main/resources/application.yml）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
