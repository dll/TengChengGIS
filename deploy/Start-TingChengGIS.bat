@echo off
chcp 65001 >nul
setlocal EnableExtensions
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
if not errorlevel 1 (
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
    echo [OK] Using embedded JRE (Temurin 21)
    set "JAVA=%APP_DIR%jre\bin\java.exe"
    echo [OK] Java ready
    echo.
    goto :check_jar
)

echo [INFO] Embedded JRE not found, checking system Java...
where java >nul 2>&1
if errorlevel 1 goto :no_java_found

set "JAVA=java"
powershell -NoProfile -Command "try { $v = (java -version 2>&1).ToString(); if ($v -match 'version \"(\d+)') { $major = [int]$Matches[1]; if ($major -ge 17) { exit 0 } else { Write-Host ('[WARN] Java version ' + $major + ' found, need 17+'); exit 1 } } else { exit 1 } } catch { exit 1 }"
if not errorlevel 1 (
    echo [OK] System Java 17+ found
    echo [OK] Java ready
    echo.
    goto :check_jar
)

:no_java_found
echo.
echo ================================================================
echo   Java 17+ is required but not available.
echo ================================================================
echo.
where java >nul 2>&1
if not errorlevel 1 (
    echo   Current Java:
    java -version 2>&1
)
echo.
echo   Solution 1: Auto-download JRE 21 (recommended, ~55MB)
echo     Press Y to download and install JRE 21 automatically
echo.
echo   Solution 2: Download pre-built package from GitHub Releases
echo     https://github.com/dll/TengChengGIS/releases/latest
echo.
echo   Solution 3: Manual download
echo     https://adoptium.net/temurin/releases/?version=21
echo     Extract to: %APP_DIR%jre
echo.
choice /c YN /n /m "Download JRE 21 automatically? (Y/N): "
if errorlevel 2 (
    echo.
    echo   Exiting. Please download JRE manually.
    pause
    exit /b 1
)

REM Auto-download JRE 21 from Adoptium API
echo Downloading JRE 21 (Temurin for Windows x64)...
echo This may take a few minutes depending on your internet speed.
powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; $url='https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse'; $zip='%TEMP%\jre21.zip'; Write-Host '  Downloading...'; Invoke-WebRequest -Uri $url -OutFile $zip; Write-Host '  Extracting...'; $extract='%TEMP%\jre21-extract'; if (Test-Path $extract) { Remove-Item -Recurse -Force $extract }; Expand-Archive -Path $zip -DestinationPath $extract; $jre=Get-ChildItem $extract -Recurse -Directory | Where-Object { Test-Path ('{0}\bin\java.exe' -f $_.FullName) } | Select-Object -First 1; if (-not $jre) { $jre=Get-ChildItem $extract | Select-Object -First 1 }; $target='%APP_DIR%jre'; if (Test-Path $target) { Remove-Item -Recurse -Force $target }; Move-Item $jre.FullName $target; Remove-Item $zip -Force; Remove-Item -Recurse -Force $extract; if (Test-Path ('{0}\bin\java.exe' -f $target)) { Write-Host '  Done!'; exit 0 } else { exit 1 }"
if errorlevel 1 (
    echo [ERROR] Failed to download JRE 21.
    echo.
    echo Manual download: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)
echo [OK] JRE 21 downloaded and installed!
set "JAVA=%APP_DIR%jre\bin\java.exe"
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
if %RETRY_COUNT% gtr %MAX_RETRIES% (
    echo [ERROR] Service failed to start after 150 seconds.
    echo Check logs\tingcheng.log for details.
    pause
    exit /b 1
)
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri '%HEALTH_URL%' -UseBasicParsing -TimeoutSec 5; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
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
        if not errorlevel 1 (
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