import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
  ],
  server: {
    // H5 dev 固定端口 5175（避开后端 8080 与用户端 5173/后台 5174）
    port: 5175,
    // 监听所有网卡（0.0.0.0），局域网内其他电脑可通过 http://本机IP:5175 访问
    host: true,
  },
})
