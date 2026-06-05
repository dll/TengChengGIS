
===============================================================================
                      TingChengGIS System - Quick Start
===============================================================================

【Quick Start】
1. Double click "Start-TingChengGIS.bat" to start
2. Wait for health check, browser will open automatically
3. If browser doesn't open, manually visit http://localhost:8092

【Login Info】
Admin: 419116 / 419116
User:  206004 / 206004

【Optional: Embedded JRE (Recommended)】
For completely no-install experience on Windows:

1. Download JRE 21
   Download: https://adoptium.net/temurin/releases/?version=21
   Select: Windows x64 JRE

2. Extract the ZIP file
3. Rename folder to "jre"
4. Copy "jre" folder to this directory (same level as Start-TingChengGIS.bat)
5. Double click "Start-TingChengGIS.bat"

Final structure:
TingChengGIS/
├── Start-TingChengGIS.bat
├── Stop-TingChengGIS.bat
├── tingchenggis.jar
├── application-demo.yml
├── jre/              ← Put here
│   └── bin/
│       └── java.exe
├── data/
│   └── 千亭.xlsx
└── logs/

【Data Persistence】
- Application data is stored in data/ folder (H2 file database)
- Data survives restarts
- Pavilion data (千亭.xlsx) is auto-imported on first run
- To re-import, delete data/*.mv.db files and restart

【Notes】
1. Keep the application window open after startup
2. First startup is slower (database initialization + health check)
3. Default JVM memory: 1024MB (edit -Xmx1024m in Start-TingChengGIS.bat to change)
4. Logs saved in logs/tingcheng.log
5. Use Stop-TingChengGIS.bat to stop gracefully

【Troubleshooting】
If you have issues, check logs/tingcheng.log
Common issues:
- Port 8092 in use: the script will detect and warn you
- Java not found: follow embedded JRE instructions above
- Pavilion data not loaded: run import manually:
  curl -X POST -F "file=@data/千亭.xlsx" http://localhost:8092/thousand-pavilions/import

===============================================================================
