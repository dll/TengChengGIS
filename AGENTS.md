# AGENTS.md — 会话摘要

## 当前目标
- 完成 TingChengGIS CI/CD 流水线，提供三种发布格式（ZIP / 便携EXE / MSI）
- 当前 tag: `v1.0.0`（触发 GitHub Actions 构建中）

## 已完成的里程碑

### CI/CD 流水线 (`.github/workflows/ci.yml`)
| Job | Runner | 功能 |
|---|---|---|
| **test** | ubuntu-latest | 前端 Vitest + 后端 Maven 测试 + JAR 打包 |
| **docker** | ubuntu-latest | Docker 构建 + 健康检查 + Playwright E2E |
| **package-deploy** | ubuntu-latest | 组装 deploy ZIP（JAR + 脚本 + config + JRE） |
| **package-installer** | windows-latest | jpackage 构建便携版 EXE + MSI 安装包 |
| **release** | ubuntu-latest | 创建 GitHub Release（附件 3 种格式） |
| **deploy** | ubuntu-latest | SSH 远程部署（需配置 Secrets） |

### Bug 修复
- `RoutePlanController.java:76` — GIF 路径改为 `System.getProperty("user.dir")` 绝对路径
- `ThousandPavilionsController.getPavilionMultimedia` — `ClassPathResource.exists()` 过滤缺失 SVG
- `GlobalExceptionHandler` — 增加 `NoResourceFoundException` → 404
- `features.js` — TSP 状态从 `var` 改为 `TCG.*` 导出，解决跨文件引用
- `Start-TingChengGIS.bat` — 编码修复、CMD 嵌套引号修复（临时 launcher.bat）
- `Stop-TingChengGIS.bat` — WMIC 查找逻辑优化 + 手工停止提示

### 一键部署 (deploy/)
- `application-demo.yml` — H2 文件持久化配置（`DB_CLOSE_ON_EXIT=FALSE`）
- `Start-TingChengGIS.bat` — 端口检测 + 健康检查轮询 + 自动导入千亭.xlsx
- 嵌入式 JRE 21 支持（自动检测 `jre/bin/java.exe`）

### 文档审核与修复
- `TingChengGIS测试报告.md` — 修正计数 79→3、PG 版本 15→16、JaCoCo 阈值 80%→70%
- `TingChengGIS实现报告.md` — DataInitializer 重写、包结构补充、代码统计
- `TingChengGIS运维报告.md` — 修复 12 个问题

## 关键技术决策
- **编码**: 所有文件 UTF-8，批处理 `chcp 65001`，JVM `-Dfile.encoding=UTF-8`
- **部署**: `application-demo.yml` 外部附加配置，JRE 嵌入 `deploy/jre/`
- **安装包**: jpackage 构建 app-image（便携版）+ MSI（安装包），WiX Toolset 3.14+
- **Git 协议**: SSH 通道推送（HTTPS 被墙）

## 待处理 / 已知问题
- [ ] MSI 构建本地测试通过，等待 CI 验证（windows-latest runner）
- [ ] GIF 上传路径已修复，前端验证待确认
- [ ] GitHub 网络间歇性不可达（墙），SSH 通道为备用方案

## 关键文件索引
- `.github/workflows/ci.yml` — CI/CD 流水线定义
- `deploy/Start-TingChengGIS.bat` — Windows 启动脚本
- `deploy/README.txt` — 部署说明
- `src/main/resources/static/js/features.js` — TSP 动画 / GIF 导出 / 前端核心
- `src/main/java/com/tingchenggis/tingcheng/controller/RoutePlanController.java` — GIF 上传路径
