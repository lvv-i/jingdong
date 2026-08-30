import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 京东风格电商平台-后台共用工程（成员C）
// dev 代理：/api -> 后端 Spring Boot（端口 8080，见 backend/src/main/resources/application.yml）
// dev 端口：5174（与用户端 frontend-user 5173 错开，见 C5 交互验收/实测记录）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    // 监听所有网卡（0.0.0.0），局域网内其他电脑可通过 http://本机IP:5174 访问
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
