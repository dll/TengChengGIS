
# 滁州亭城GIS系统 - Windows一键打包说明

## 📦 最终部署包结构

```
TingChengGIS/
├── Start-TingChengGIS.bat      ← Double-click to start (ONLY one file needed)
├── tingchenggis.jar            ← Main application
├── README.txt                  ← User guide
├── jre/                        ← Optional: Embedded JRE (Recommended)
│   └── bin/
│       └── java.exe
├── data/                       ← Data folder (auto-created)
│   └── 千亭.xlsx
├── logs/                       ← Logs folder (auto-created)
└── temp/                       ← Temp folder (auto-created)
```

## 🚀 User Steps

### Option 1: Embedded JRE (Recommended, completely no-install)

1. User extracts `TingChengGIS.zip`
2. Double click `Start-TingChengGIS.bat`
3. Wait 35 seconds, browser opens `http://localhost:8092` automatically

### Option 2: Use system Java

If user has Java 21 installed, no need to download JRE - just double click to start.

---

## 🔧 How to create full package (with embedded JRE)

> JRE 21 (Eclipse Temurin) is already pre-bundled at `deploy/jre/`.
> To update the JRE, download from https://adoptium.net/temurin/releases/?version=21
> and extract to `deploy/jre/`.

### Final structure after running build script:

```
deploy/
├── Start-TingChengGIS.bat
├── Stop-TingChengGIS.bat
├── tingchenggis.jar
├── README.txt
├── application-demo.yml
├── VERSION
├── jre/              ← Embedded JRE (pre-included)
│   ├── bin/
│   │   └── java.exe
│   └── ...
├── data/
│   └── 千亭.xlsx
├── logs/             ← Auto-created at runtime
└── temp/             ← Auto-created at runtime

output/
└── TingChengGIS-v{version}.zip   ← Distribution package
```

### Test startup

1. Go to `deploy/` directory
2. Double click `Start-TingChengGIS.bat`
3. Verify application starts normally
4. Browser opens http://localhost:8092 automatically after ~35s

---

## 📝 Startup Script Features

`Start-TingChengGIS.bat` functions:

1. **Auto JRE detection**: First try embedded `jre/`, fallback to system Java
2. **Environment check**: Verify required files exist
3. **Background start**: Application runs in new window, main window can close
4. **Auto open browser**: Waits 35 seconds then automatically opens access URL
5. **Directory management**: Auto-creates data, logs, temp directories

---

## 🎯 Login Info

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| User | testuser | password |

---

## ⚠️ Notes

1. **Port conflict**: Ensure port 8092 is not used
2. **Keep window open**: Don't close application window after startup
3. **First startup**: First start is slow (30-60s) - needs to initialize database
4. **Data location**: User data saved in `data/` folder
5. **Log location**: Runtime logs in `logs/tingcheng.log`

---

## 🔧 Troubleshooting

### Issue: Window flashes and disappears immediately

Check:
1. Whether JRE or system Java 21 exists
2. `tingchenggis.jar` file exists
3. Right-click script, choose "Edit" to see error

### Issue: Browser doesn't open automatically

Solution:
- Manually enter in browser: `http://localhost:8092`

### Issue: Access shows 404 or error

Solution:
- Wait longer (first startup ~60 seconds)
- Check `logs/tingcheng.log` for error info

---

## 📁 Current deploy directory

✅ Ready:
- `Start-TingChengGIS.bat` - One-click startup script (NO encoding issues)
- `tingchenggis.jar` - Main app (fixed favicon and encoding issues)
- `README.txt` - User guide
- `data/千亭.xlsx` - Demo data

Optional addition:
- `jre/` - Embedded JRE (recommended to add)

---

## 🚀 One-Click Package (Recommended)

Use the automated build script (no manual steps needed):

```bash
# Batch version (double-click to run):
scripts\build-deploy-package.bat

# PowerShell version:
powershell -File scripts\build-deploy-package.ps1
```

The script automates all 6 steps:
1. Detect version from `pom.xml`
2. Clean old artifacts
3. Compile project (`mvn clean package -DskipTests`)
4. Copy JAR + demo data to `deploy/`
5. Check embedded JRE (pre-included in `deploy/jre`)
6. Validate & package → `output/TingChengGIS-v{version}.zip`

## 🚀 Manual Package Workflow

```bash
# 1. Compile project
mvn clean package -DskipTests

# 2. Copy JAR to deploy
copy target\tingchenggis-*.jar deploy\tingchenggis.jar

# 3. Download and extract JRE to deploy/jre
#    (already done - jre/ is pre-bundled)

# 4. Test startup
cd deploy
Start-TingChengGIS.bat

# 5. Package and distribute
# Compress deploy folder to TingChengGIS.zip
```

---

## ✨ Version Info

- Version: 1.0.5
- Java: 21+
- Fixes:
  - ✅ Favicon 404 error
  - ✅ Chinese encoding issue
  - ✅ One-click startup (ENGLISH ONLY, no encoding problems)
  - ✅ Embedded JRE support
