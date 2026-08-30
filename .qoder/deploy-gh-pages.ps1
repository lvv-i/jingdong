# 商城网页版公网部署脚本：构建 frontend-user 并发布到 GitHub Pages
# 用法：powershell -NoProfile -ExecutionPolicy Bypass -File .qoder\deploy-gh-pages.ps1
# 前置：后端需通过 ngrok 隧道对外（先运行 node .qoder\start-ngrok.cjs）
#       公网后端固定为 https://blooming-bulgur-rifling.ngrok-free.dev
# 发布地址：https://lvv-i.github.io/jingdong/
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot   # 仓库根目录
$fe   = Join-Path $root "frontend-user"
$tmp  = Join-Path $root ".qoder\_tmp_ghpages"

# 1) 构建：API 指向 ngrok 公网后端；站点子路径 /jingdong/
Push-Location $fe
$env:VITE_API_BASE    = "https://blooming-bulgur-rifling.ngrok-free.dev"
$env:VITE_ROUTER_BASE = "/jingdong/"
npm run build -- --base=/jingdong/
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "构建失败，退出" }

# 2) 组装 gh-pages 发布内容（404.html 支持 history 路由直接访问）
Copy-Item "$fe\dist\index.html" "$fe\dist\404.html" -Force
if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
New-Item -ItemType Directory -Path $tmp | Out-Null
Copy-Item "$fe\dist\*" $tmp -Recurse -Force
Set-Content -Path "$tmp\.nojekyll" -Value "" -NoNewline

# 3) 推送到 gh-pages 分支（需代理访问 GitHub）
git -C $tmp init -b gh-pages | Out-Null
git -C $tmp remote add origin https://github.com/lvv-i/jingdong.git
git -C $tmp add -A
git -C $tmp commit -m "deploy: GitHub Pages 商城网页版 $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
git -C $tmp -c http.proxy=http://127.0.0.1:7892 -c https.proxy=http://127.0.0.1:7892 push origin gh-pages --force

Write-Output ""
Write-Output "部署完成：https://lvv-i.github.io/jingdong/"
