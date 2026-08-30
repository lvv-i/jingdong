/**
 * 启动 ngrok 隧道：把本机后端 8080 暴露到固定公网域名
 * 用法：node .qoder/start-ngrok.cjs
 * 公网地址：https://blooming-bulgur-rifling.ngrok-free.dev（ngrok 免费版 dev domain，永久不变）
 * 依赖：npm install -g @ngrok/ngrok（已安装）
 */
const fs = require("fs");
const path = require("path");

const TOKEN_FILE = path.join(__dirname, "ngrok-authtoken.txt");
if (!fs.existsSync(TOKEN_FILE)) {
  console.error("[ngrok] 未找到 authtoken：请将 token 写入 .qoder/ngrok-authtoken.txt");
  process.exit(1);
}
const authtoken = fs.readFileSync(TOKEN_FILE, "utf8").trim();

// 使用全局安装的 @ngrok/ngrok
const ngrokPath = path.join(
  process.env.APPDATA || "",
  "npm",
  "node_modules",
  "@ngrok",
  "ngrok"
);
let ngrok;
try {
  ngrok = require(ngrokPath);
} catch (e) {
  console.error("[ngrok] 未找到全局 @ngrok/ngrok，请先执行 npm install -g @ngrok/ngrok");
  process.exit(1);
}

(async () => {
  try {
    const listener = await ngrok.forward({
      addr: "http://localhost:8080",
      authtoken,
      domain: "blooming-bulgur-rifling.ngrok-free.dev",
    });
    console.log("[ngrok] 隧道已启动");
    console.log("[ngrok] 公网地址: " + listener.url());
    console.log("[ngrok] 前端 API 指向该地址，按 Ctrl+C 停止");
  } catch (e) {
    console.error("[ngrok] 隧道启动失败: " + (e && e.message ? e.message : e));
    process.exit(1);
  }
})();

// 保持进程存活（隧道会话随进程存在）
setInterval(() => {}, 1 << 30);
