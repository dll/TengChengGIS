@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion
title TingChengGIS System

set "APP_DIR=%~dp0"
cd /d "%APP_DIR%"

set "SERVICE_URL=http://localhost:8092"
set "HEALTH_URL=%SERVICE_URL%/actuator/health"
set "IMPORT_URL=%SERVICE_URL%/thousand-pavilions/import"
set "JAVA_OPTS=-Dfile.encoding=UTF-8 -Xmx1024m"
set "SPRING_OPTS=--spring.config.additional-location=file:application-demo.yml"
set "XLSX_FILE=data\千亭.xlsx"

:check_port
echo [CHECK] Checking port 8092...
netstat -ano | findstr ":8092 " >nul 2>&1
if !errorlevel! equ 0 (
    echo [ERROR] Port 8092 is already in use!
    echo.
    netstat -ano | findstr ":8092 "
    pause
    exit /b 1
)
echo [OK] Port 8092 is available
echo.

:check_java
if exist "%APP_DIR%jre\bin\java.exe" (
    echo [OK] Using embedded JRE
    set "JAVA=%APP_DIR%jre\bin\java.exe"
) else (
    where java >nul 2>&1
    if !errorlevel! neq 0 (
        echo [ERROR] Java 21 not found!
        echo.
        echo Download JRE 21 from https://adoptium.net/temurin/releases/?version=21
        echo Extract to %APP_DIR%jre folder, or install system Java 21
        pause
        exit /b 1
    )
    set "JAVA=java"
)
echo [OK] Java ready
echo.

:check_jar
if not exist "tingchenggis.jar" (
    echo [ERROR] tingchenggis.jar not found!
    pause
    exit /b 1
)
echo [OK] Application found
echo.

:create_dirs
if not exist "data" mkdir data
if not exist "logs" mkdir logs
if not exist "temp" mkdir temp

echo ================================================================================
echo                      TingChengGIS System - Starting
echo ================================================================================
echo.
echo Service URL: %SERVICE_URL%
echo.
echo Starting application...

REM Write launcher to temp file to avoid cmd.exe nested quote issues
set "LAUNCHER=%TEMP%\tc_launcher_%RANDOM%.bat"
(
echo @echo off
echo chcp 65001 ^>nul
echo "%JAVA%" %JAVA_OPTS% -jar "tingchenggis.jar" %SPRING_OPTS%
) > "%LAUNCHER%"

start "TingChengGIS" "%LAUNCHER%"

REM Wait 20s for initial H2 + Hibernate startup, then poll
echo Waiting 20 seconds for initial startup...
timeout /t 20 /nobreak >nul

echo Checking service health...
set MAX_RETRIES=30
set RETRY_COUNT=0

:health_loop
set /a RETRY_COUNT+=1
if !RETRY_COUNT! gtr !MAX_RETRIES! (
    echo [ERROR] Service failed to start after 150 seconds.
    echo Check logs\tingcheng.log for details.
    pause
    exit /b 1
)
powershell -NoProfile -Command "try { `$r = Invoke-WebRequest -Uri '%HEALTH_URL%' -UseBasicParsing -TimeoutSec 5; if (`$r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    timeout /t 5 /nobreak >nul
    goto health_loop
)

echo [OK] Service is healthy!
echo.

REM Auto-import on first run (marker file guards against re-import)
if exist "%XLSX_FILE%" (
    if not exist "data\.imported" (
        echo [INFO] First run detected. Importing pavilion data...
        powershell -NoProfile -Command ^
            "$u='%IMPORT_URL%'; $f='%XLSX_FILE%'; ^
             $c=New-Object System.Net.Http.HttpClient; ^
             $m=New-Object System.Net.Http.MultipartFormDataContent; ^
             $s=[System.Net.Http.StreamContent]::new((Get-Item $f).OpenRead()); ^
             $m.Add($s,'file','千亭.xlsx'); ^
             $r=$c.PostAsync($u,$m).Result; ^
             if($r.IsSuccessStatusCode){exit 0}else{exit 1}" >nul 2>&1
        if !errorlevel! equ 0 (
            echo [OK] Pavilion data imported successfully!
            echo. > "data\.imported"
        ) else (
            echo [WARN] Auto-import failed. Run manually:
            echo   curl -X POST -F "file=@data/千亭.xlsx" %IMPORT_URL%
        )
        echo.
    ) else (
        echo [OK] Pavilion data already imported, skipping
        echo.
    )
)

echo ================================================================================
echo                      TingChengGIS System - Started
echo ================================================================================
echo.
echo Service URL: %SERVICE_URL%
echo H2 Console:  %SERVICE_URL%/h2-console
echo.
echo Login accounts:
echo   Admin:  419116 / 419116
echo   User:   206004 / 206004
echo.
echo To stop: run Stop-TingChengGIS.bat or close the application window
echo.
echo Opening browser...
start "" "%SERVICE_URL%"
echo.
echo ================================================================================
echo Press any key to close this window (service keeps running)...
pause >nul