
===============================================================================
                      TingChengGIS System - Quick Start
===============================================================================

【Quick Start】
1. Unzip the deploy package to any folder
2. Double click "Start-TingChengGIS.bat" to start
3. Wait for health check, browser will open automatically
4. If browser doesn't open, manually visit http://localhost:8092

【Login Info】
Admin: 419116 / 419116
User:  206004 / 206004

【Embedded JRE】
JRE 21 (Eclipse Temurin) is bundled in the jre/ folder.
No manual Java installation required — just unzip and run.

Directory structure after unzip:
TingChengGIS/
├── Start-TingChengGIS.bat
├── Stop-TingChengGIS.bat
├── tingchenggis.jar
├── application-demo.yml
├── jre/              ← Embedded JRE (included)
│   └── bin/
│       └── java.exe
├── data/
│   └── 千亭.xlsx     ← Import this file on first run
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
