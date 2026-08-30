@echo off
chcp 65001 > nul
title 京冬风格电商平台 - 一键启动

echo ============================================
echo   京冬风格电商平台 一键启动脚本
echo   课程项目 - 五端多角色电商系统
echo ============================================
echo.

REM ===== 1. MySQL 检查与启动 =====
echo [1/5] 检查 MySQL 8.0.28...
set MYSQL_DIR=D:\mysql-8.0.28-winx64
set MYSQL_DATA=%MYSQL_DIR%\data

tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I "mysqld.exe" >NUL
if %ERRORLEVEL% EQU 0 (
    echo   MySQL 已在运行中
) else (
    if exist "%MYSQL_DIR%\bin\mysqld.exe" (
        echo   启动 MySQL...
        start "MySQL" /MIN "%MYSQL_DIR%\bin\mysqld.exe" --defaults-file="%MYSQL_DIR%\my.ini"
        echo   等待 MySQL 就绪...
        timeout /t 5 /nobreak > nul
        echo   MySQL 启动完成（端口 3306）
    ) else (
        echo   [警告] 未找到 MySQL，将使用 H2 内嵌数据库模式
        echo   MySQL 安装路径：%MYSQL_DIR%
    )
)

REM ===== 2. 后端启动 =====
echo.
echo [2/5] 启动后端服务（Spring Boot 8080）...
set JAR_FILE=backend\target\jd-shop-1.0.0.jar
if exist "%JAR_FILE%" (
    start "Backend-8080" /MIN java -jar "%JAR_FILE%"
    echo   后端 PID 已启动，等待就绪...
    timeout /t 8 /nobreak > nul
) else (
    echo   [错误] 未找到 %JAR_FILE%，请先执行 mvn package
    pause
    exit /b 1
)

REM ===== 3. 用户网页端 =====
echo.
echo [3/5] 启动用户网页端（Vue 3 :5173）...
cd /d frontend-user
if exist "node_modules" (
    start "UserWeb-5173" /MIN npx vite --port 5173
    echo   用户网页端 http://localhost:5173
) else (
    echo   [警告] node_modules 未安装，跳过
)
cd /d ..

REM ===== 4. 商家/管理员后台 =====
echo.
echo [4/5] 启动后台端（Vue 3 :5174）...
cd /d admin-web
if exist "node_modules" (
    start "AdminWeb-5174" /MIN npx vite --port 5174
    echo   后台端 http://localhost:5174
    echo   商家登录：merchant001 / merchant001
    echo   管理员登录：admin001 / admin001
) else (
    echo   [警告] node_modules 未安装，跳过
)
cd /d ..

REM ===== 5. 移动端 H5 =====
echo.
echo [5/5] 启动移动端 H5（uni-app）...
cd /d mobile-app
if exist "node_modules" (
    start "MobileH5" /MIN npm run dev:h5
    echo   移动端 H5 http://localhost:5173  (默认端口)
) else (
    echo   [警告] node_modules 未安装，跳过
)
cd /d ..

REM ===== 完成 =====
echo.
echo ============================================
echo   启动完成！访问地址：
echo.
echo   用户网页端   http://localhost:5173
echo   商家后台     http://localhost:5174
echo   管理员后台   http://localhost:5174（同端口，按角色分菜单）
echo   后端 API     http://localhost:8080
echo.
echo   测试账号（密码与账号名相同）：
echo     user001 / user002       普通用户
echo     merchant001 / merchant002 商家
echo     admin001                管理员
echo.
echo   按任意键停止所有服务...
echo ============================================
pause > nul

REM 清理
taskkill /F /FI "WINDOWTITLE eq Backend-8080*" > nul 2>&1
taskkill /F /FI "WINDOWTITLE eq UserWeb-5173*" > nul 2>&1
taskkill /F /FI "WINDOWTITLE eq AdminWeb-5174*" > nul 2>&1
taskkill /F /FI "WINDOWTITLE eq MobileH5*" > nul 2>&1
echo 所有服务已停止。