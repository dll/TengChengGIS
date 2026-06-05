@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion
title TingChengGIS System - Stop

echo ================================================================================
echo                      TingChengGIS System - Stopping
echo ================================================================================
echo.

REM Find the PID of java process running tingchenggis.jar
set "PID="
for /f "skip=1 tokens=2 delims=," %%a in ('
    wmic process where "name='java.exe' and commandline like '%%tingchenggis%%'" get processid^,commandline /format:csv 2^>nul
') do (
    if not defined PID (
        set "PID=%%a"
    )
)

if defined PID (
    echo [INFO] Stopping process PID=%PID%...
    taskkill /f /pid !PID! >nul 2>&1
    if !errorlevel! equ 0 (
        echo [OK] Application stopped
    ) else (
        echo [FAILED] Automatic stop failed. Please manually close the TingChengGIS window.
        echo ================================================================================
        pause
        exit /b 1
    )
) else (
    echo [INFO] No running TingChengGIS process found
    echo.
    echo [HINT] If the application is still running, try:
    echo         1. Press Ctrl+C in the console window where TingChengGIS is running
    echo         2. Or close the console window directly
)

echo.
echo ================================================================================
pause
