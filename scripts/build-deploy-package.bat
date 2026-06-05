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

echo [1/5] 清理旧的部署包...
if exist "%DEPLOY_DIR%" (
    rmdir /s /q "%DEPLOY_DIR%"
    echo   已清理旧部署包
)
mkdir "%DEPLOY_DIR%"
mkdir "%DEPLOY_DIR%\data"

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

REM 复制 JAR 文件
copy "%TARGET_DIR%\tingchenggis-1.0.0.jar" "%DEPLOY_DIR%\tingchenggis.jar" >nul
echo   已复制主程序

REM 复制数据文件
if exist "%PROJECT_DIR%\data\千亭.xlsx" (
    copy "%PROJECT_DIR%\data\千亭.xlsx" "%DEPLOY_DIR%\data\" >nul
    echo   已复制演示数据
)

REM 复制配置文件
copy "%PROJECT_DIR%\deploy\application-demo.yml" "%DEPLOY_DIR%\" >nul
echo   已复制配置文件

REM 复制启动脚本
copy "%PROJECT_DIR%\deploy\启动滁州亭城GIS.bat" "%DEPLOY_DIR%\" >nul
echo   已复制启动脚本

REM 复制说明文档
copy "%PROJECT_DIR%\deploy\README.txt" "%DEPLOY_DIR%\" >nul
copy "%PROJECT_DIR%\deploy\打包说明.md" "%DEPLOY_DIR%\" >nul
echo   已复制说明文档

echo.
echo [4/5] 创建临时目录...
mkdir "%DEPLOY_DIR%\logs" 2>nul
mkdir "%DEPLOY_DIR%\temp" 2>nul
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
