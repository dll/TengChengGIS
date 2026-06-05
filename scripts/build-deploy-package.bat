@echo off
chcp 65001 >nul
title 滁州亭城GIS - 打包脚本

echo ========================================
echo   滁州亭城GIS系统 - 部署包生成工具
echo ========================================
echo.

REM 设置目录
set PROJECT_DIR=%~dp0..
set DEPLOY_DIR=%PROJECT_DIR%\deploy
set TARGET_DIR=%PROJECT_DIR%\target

echo [1/5] 清理旧的构建产物...
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if exist "%DEPLOY_DIR%\tingchenggis.jar" del "%DEPLOY_DIR%\tingchenggis.jar"
if exist "%DEPLOY_DIR%\data\千亭.xlsx" del "%DEPLOY_DIR%\data\千亭.xlsx"
echo   已清理旧构建产物

echo.
echo [2/5] 编译项目...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo   [错误] 编译失败！
    pause
    exit /b 1
)
echo   编译成功

echo.
echo [3/5] 复制文件...

REM 复制 JAR 文件（从 target/ 到 deploy/）
copy "%TARGET_DIR%\tingchenggis-1.0.0.jar" "%DEPLOY_DIR%\tingchenggis.jar" >nul
echo   已复制主程序

REM 复制数据文件（从 data/ 到 deploy/data/）
if not exist "%DEPLOY_DIR%\data" mkdir "%DEPLOY_DIR%\data"
if exist "%PROJECT_DIR%\data\千亭.xlsx" (
    copy "%PROJECT_DIR%\data\千亭.xlsx" "%DEPLOY_DIR%\data\" >nul
    echo   已复制演示数据
)

echo.
echo [4/5] 创建运行时目录...
if not exist "%DEPLOY_DIR%\logs" mkdir "%DEPLOY_DIR%\logs"
if not exist "%DEPLOY_DIR%\temp" mkdir "%DEPLOY_DIR%\temp"
echo   目录创建完成

echo.
echo [5/5] 打包完成！
echo ========================================
echo.
echo 部署包位置: %DEPLOY_DIR%
echo.
echo 可以将 deploy 目录整个压缩分发给用户
echo 用户只需要双击"启动滁州亭城GIS.bat"即可运行
echo.
echo ========================================
echo.
pause
