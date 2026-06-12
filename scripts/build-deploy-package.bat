@echo off
chcp 65001 >nul
setlocal EnableExtensions
title TingChengGIS - Build Deploy Package

echo ================================================================================
echo                   TingChengGIS - Windows Deploy Package Builder
echo ================================================================================
echo.

set "PROJECT_DIR=%~dp0.."
set "DEPLOY_DIR=%PROJECT_DIR%\deploy"
set "TARGET_DIR=%PROJECT_DIR%\target"
set "OUTPUT_DIR=%PROJECT_DIR%\output"

REM ── Step 1: Detect version from pom.xml ──────────────────────────────────────
echo [1/6] Detecting project version...
for /f %%a in ('powershell -NoProfile -Command "$xml=[xml](Get-Content '%PROJECT_DIR%\pom.xml' -Raw -Encoding UTF8); $xml.project.version"') do set "APP_VERSION=%%a"
if not defined APP_VERSION (
    set "APP_VERSION=1.0.0"
    echo   [WARN] Could not detect version, using 1.0.0
) else (
    echo   [OK] Detected version: %APP_VERSION%
)
echo.

REM ── Step 2: Clean old artifacts ──────────────────────────────────────────────
echo [2/6] Cleaning old build artifacts...
if exist "%DEPLOY_DIR%\tingchenggis.jar" del "%DEPLOY_DIR%\tingchenggis.jar"
if exist "%DEPLOY_DIR%\VERSION" del "%DEPLOY_DIR%\VERSION"
if exist "%OUTPUT_DIR%\TingChengGIS.zip" del "%OUTPUT_DIR%\TingChengGIS.zip"
echo   [OK] Cleanup done
echo.

REM ── Step 3: Compile project ──────────────────────────────────────────────────
echo [3/6] Compiling project (mvn clean package -DskipTests)...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests -B
if %errorlevel% neq 0 (
    echo   [ERROR] Build failed!
    pause
    exit /b 1
)
echo   [OK] Build successful
echo.

REM ── Step 4: Copy files to deploy/ ────────────────────────────────────────────
echo [4/6] Copying files to deploy directory...

REM Find the built JAR (handle versioned filename)
set "JAR_FILE="
for %%f in ("%TARGET_DIR%\tingchenggis-*.jar") do set "JAR_FILE=%%f"
if not defined JAR_FILE (
    echo   [ERROR] No JAR file found in target/!
    pause
    exit /b 1
)
copy "%JAR_FILE%" "%DEPLOY_DIR%\tingchenggis.jar" >nul
echo   [OK] Copied JAR: %JAR_FILE%

REM Copy demo data
if not exist "%DEPLOY_DIR%\data" mkdir "%DEPLOY_DIR%\data"
if exist "%PROJECT_DIR%\data\千亭.xlsx" (
    copy "%PROJECT_DIR%\data\千亭.xlsx" "%DEPLOY_DIR%\data\" >nul
    echo   [OK] Copied demo data
)

REM Create runtime directories
if not exist "%DEPLOY_DIR%\logs" mkdir "%DEPLOY_DIR%\logs" >nul
if not exist "%DEPLOY_DIR%\temp" mkdir "%DEPLOY_DIR%\temp" >nul
echo   [OK] Runtime directories created

REM Write version file
echo %APP_VERSION% > "%DEPLOY_DIR%\VERSION"
echo   [OK] Version file written
echo.

REM ── Step 5: Check embedded JRE ──────────────────────────────────────────────
echo [5/6] Checking embedded JRE...
if exist "%DEPLOY_DIR%\jre\bin\java.exe" (
    echo   [OK] JRE found at deploy\jre - included in package
) else (
    echo   [WARN] JRE not found at deploy\jre.
    echo   The package will still work if user has Java 17+ installed.
    echo   To embed JRE, download from https://adoptium.net/temurin/releases/?version=21
    echo   and extract to: %DEPLOY_DIR%\jre
)
echo.

REM ── Step 6: Validate deploy package ──────────────────────────────────────────
echo [6/6] Validating deploy package...
set "VALIDATION_ERROR=0"
if not exist "%DEPLOY_DIR%\tingchenggis.jar" (
    echo   [ERROR] Missing: tingchenggis.jar
    set VALIDATION_ERROR=1
)
if not exist "%DEPLOY_DIR%\Start-TingChengGIS.bat" (
    echo   [ERROR] Missing: Start-TingChengGIS.bat
    set VALIDATION_ERROR=1
)
if not exist "%DEPLOY_DIR%\README.txt" (
    echo   [WARN] Missing: README.txt
)
if not exist "%DEPLOY_DIR%\application-demo.yml" (
    echo   [WARN] Missing: application-demo.yml
)
if %VALIDATION_ERROR% neq 0 (
    echo   [ERROR] Validation failed! Fix errors above and retry.
    pause
    exit /b 1
)
echo   [OK] All required files present
echo.

REM ── Package into ZIP ────────────────────────────────────────────────────────
echo [6/6] Packaging into ZIP...
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%" >nul

REM Use PowerShell Compress-Archive for ZIP creation (all on one line, no ^ inside quotes)
powershell -NoProfile -Command "$src='%DEPLOY_DIR%'; $out='%OUTPUT_DIR%\TingChengGIS-v%APP_VERSION%.zip'; Compress-Archive -Path ($src+'\*') -DestinationPath $out -Force; Write-Host ('  Created: ' + $out); $size=(Get-Item $out).Length / 1MB; Write-Host ('  Size: ' + [math]::Round($size,1) + ' MB'); exit 0"
if %errorlevel% neq 0 (
    echo   [ERROR] Failed to create ZIP package!
    pause
    exit /b 1
)
echo   [OK] ZIP package created
echo.

REM ── Summary ──────────────────────────────────────────────────────────────────
echo ================================================================================
echo                            BUILD COMPLETE
echo ================================================================================
echo   Version:    %APP_VERSION%
echo   Deploy:     %DEPLOY_DIR%
echo   Package:    %OUTPUT_DIR%\TingChengGIS-v%APP_VERSION%.zip
echo.
echo   To distribute, send the ZIP file to users.
echo   Users just need to unzip and double-click Start-TingChengGIS.bat
echo.
echo ================================================================================
pause
