#Requires -Version 7.0
<#
.SYNOPSIS
    TingChengGIS Windows Deploy Package Builder (One-Click Packaging)
.DESCRIPTION
    Automates the full deploy package creation:
      1. Compile project via Maven
      2. Copy JAR + data files to deploy/
      3. Check embedded JRE (already in deploy/jre)
      4. Validate package integrity
      5. Package into TingChengGIS-v{version}.zip
#>

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $PSCommandPath)
$DeployDir   = Join-Path $ProjectRoot "deploy"
$TargetDir   = Join-Path $ProjectRoot "target"
$OutputDir   = Join-Path $ProjectRoot "output"
$AppVersion  = "1.0.0"

Write-Host "=" * 80
Write-Host "  TingChengGIS - Windows Deploy Package Builder (PowerShell)"
Write-Host "=" * 80
Write-Host ""

# ── Step 1: Detect version ──────────────────────────────────────────────────
Write-Host "[1/6] Detecting project version..."
$pomPath = Join-Path $ProjectRoot "pom.xml"
if (Test-Path $pomPath) {
    $pomXml = [xml](Get-Content $pomPath -Raw)
    $AppVersion = $pomXml.project.version
    if (-not $AppVersion) { $AppVersion = "1.0.0" }
}
Write-Host "  [OK] Version: $AppVersion"
Write-Host ""

# ── Step 2: Clean old artifacts ─────────────────────────────────────────────
Write-Host "[2/6] Cleaning old build artifacts..."
$filesToRemove = @(
    (Join-Path $DeployDir "tingchenggis.jar"),
    (Join-Path $DeployDir "VERSION")
)
foreach ($f in $filesToRemove) {
    if (Test-Path $f) { Remove-Item -Path $f -Force }
}
$zipPattern = Join-Path $OutputDir "TingChengGIS-v*.zip"
if (Test-Path $zipPattern) { Remove-Item -Path $zipPattern -Force }
Write-Host "  [OK] Cleanup done"
Write-Host ""

# ── Step 3: Compile project ─────────────────────────────────────────────────
Write-Host "[3/6] Compiling project (mvn clean package -DskipTests)..."
Push-Location $ProjectRoot
try {
    $mvnResult = Start-Process -FilePath "mvn" -ArgumentList "clean package -DskipTests -B" -NoNewWindow -Wait -PassThru
    if ($mvnResult.ExitCode -ne 0) {
        Write-Host "  [ERROR] Build failed with exit code $($mvnResult.ExitCode)!"
        throw "Maven build failed"
    }
}
finally {
    Pop-Location
}
Write-Host "  [OK] Build successful"
Write-Host ""

# ── Step 4: Copy files ──────────────────────────────────────────────────────
Write-Host "[4/6] Copying files to deploy directory..."

# Find JAR
$jarFile = Get-ChildItem -Path $TargetDir -Filter "tingchenggis-*.jar" | Select-Object -First 1
if (-not $jarFile) {
    Write-Host "  [ERROR] No JAR file found in target/!"
    throw "JAR not found"
}
Copy-Item -Path $jarFile.FullName -Destination (Join-Path $DeployDir "tingchenggis.jar") -Force
Write-Host "  [OK] Copied JAR: $($jarFile.Name)"

# Copy demo data
$demoData = Join-Path $ProjectRoot "data" "千亭.xlsx"
$dataDir = Join-Path $DeployDir "data"
if (-not (Test-Path $dataDir)) { New-Item -ItemType Directory -Path $dataDir -Force | Out-Null }
if (Test-Path $demoData) {
    Copy-Item -Path $demoData -Destination $dataDir -Force
    Write-Host "  [OK] Copied demo data"
}

# Create runtime directories
foreach ($dir in @("logs", "temp")) {
    $p = Join-Path $DeployDir $dir
    if (-not (Test-Path $p)) { New-Item -ItemType Directory -Path $p -Force | Out-Null }
}
Write-Host "  [OK] Runtime directories created"

# Write version file
Set-Content -Path (Join-Path $DeployDir "VERSION") -Value $AppVersion -Encoding UTF8
Write-Host "  [OK] Version file written"
Write-Host ""

# ── Step 5: Check embedded JRE ─────────────────────────────────────────────
Write-Host "[5/6] Checking embedded JRE..."
$jreJava = Join-Path $DeployDir "jre" "bin" "java.exe"
if (Test-Path $jreJava) {
    Write-Host "  [OK] JRE found at deploy\jre (embedded, included in package)"
}
else {
    Write-Host "  [WARN] JRE not found at deploy\jre."
    Write-Host "  The package will still work if user has Java 17+ installed."
    Write-Host "  To embed JRE, download from https://adoptium.net/temurin/releases/?version=21"
    Write-Host "  and extract to: $DeployDir\jre"
}
Write-Host ""

# ── Step 6: Validate deploy package ─────────────────────────────────────────
Write-Host "[6/6] Validating deploy package..."
$errors = 0
$requiredFiles = @(
    "tingchenggis.jar",
    "Start-TingChengGIS.bat",
    "application-demo.yml"
)
foreach ($f in $requiredFiles) {
    $fp = Join-Path $DeployDir $f
    if (-not (Test-Path $fp)) {
        Write-Host "  [ERROR] Missing: $f"
        $errors++
    }
}
$optionalFiles = @("README.txt", "jre\bin\java.exe")
foreach ($f in $optionalFiles) {
    $fp = Join-Path $DeployDir $f
    if (-not (Test-Path $fp)) {
        Write-Host "  [WARN] Missing optional: $f"
    }
}
if ($errors -gt 0) {
    Write-Host "  [ERROR] Validation failed with $errors error(s)!"
    throw "Validation failed"
}
Write-Host "  [OK] All required files present"
Write-Host ""

# ── Package into ZIP ─────────────────────────────────────────────────────────
Write-Host "[6/6] Packaging into ZIP..."
if (-not (Test-Path $OutputDir)) { New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null }

$zipPath = Join-Path $OutputDir "TingChengGIS-v$AppVersion.zip"
$items = Get-ChildItem -Path $DeployDir -Exclude @("logs", "temp", "*.mv.db", "*.trace.db")
# Exclude database files from data/
$compressItems = @()
foreach ($item in $items) {
    if ($item.PSIsContainer) {
        $compressItems += $item.FullName
    }
    elseif ($item.Extension -notin @(".mv.db", ".trace.db")) {
        $compressItems += $item.FullName
    }
}
Compress-Archive -Path $compressItems -DestinationPath $zipPath -Force

$size = [math]::Round((Get-Item $zipPath).Length / 1MB, 1)
Write-Host "  [OK] Created: $zipPath"
Write-Host "  [OK] Size: ${size} MB"
Write-Host ""

# ── Summary ─────────────────────────────────────────────────────────────────
Write-Host "=" * 80
Write-Host "                         BUILD COMPLETE"
Write-Host "=" * 80
Write-Host "  Version:    $AppVersion"
Write-Host "  Deploy:     $DeployDir"
Write-Host "  Package:    $zipPath"
Write-Host "  Size:       ${size} MB"
Write-Host ""
Write-Host "  To distribute, send the ZIP file to users."
Write-Host "  Users just need to unzip and double-click Start-TingChengGIS.bat"
Write-Host ""
Write-Host "=" * 80
