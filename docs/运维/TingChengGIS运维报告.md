# 滁州亭城GIS系统 — 运维报告

| 项目名称 | 滁州亭城GIS系统 (TingChengGIS) |
|---------|-------------------------------|
| 文档版本 | v1.0.0 |
| 编制日期 | 2026-06-04 |
| 编制人 | 运维组 |

---

## 一、运维概述

### 1.1 运维范围

| 运维阶段 | 内容 | 说明 |
|---------|------|------|
| 环境准备 | 基础设施配置 | 开发、测试、生产环境搭建 |
| 部署发布 | CI/CD 流程 | 自动化构建与部署 |
| 监控告警 | 系统监控 | 服务状态、性能指标、日志 |
| 日常运维 | 故障处理 | 问题排查、应急响应 |
| 数据管理 | 备份恢复 | 数据备份、灾备方案 |
| 安全运维 | 安全加固 | 系统安全、权限管理 |

### 1.2 技术栈

| 组件 | 技术选型 | 版本 |
|------|---------|------|
| 运行环境 | JDK | 21+ |
| 数据库 | PostgreSQL / H2 | 16+ / 内存 |
| 空间扩展 | PostGIS | 3.4+（搭配 PG 16） |
| 容器化 | Docker / Docker Compose | 24+ / V2（兼容 `version: "3.9"`） |
| 反向代理 | Nginx | 最新稳定版 |
| 服务管理 | systemd | Linux 系统服务 |
| CI/CD | GitHub Actions | 自动化流水线 |

---

## 二、环境要求与部署

### 2.1 环境要求

| 环境类型 | CPU | 内存 | 磁盘 | 操作系统 |
|---------|-----|------|------|---------|
| 开发环境 | 2核 | 4GB | 20GB | Windows / Linux / macOS |
| 测试环境 | 4核 | 8GB | 50GB | Linux (推荐) |
| 生产环境 | 8核+ | 16GB+ | 100GB+ | Linux (推荐) |

### 2.2 部署方式

#### 2.2.1 Docker 一键部署（推荐）

```bash
# 克隆项目
git clone <repo-url> tingchenggis
cd tingchenggis

# 启动服务（使用 H2 内存数据库，开发/演示用）
docker compose up -d

# 如需 PostgreSQL，编辑 docker-compose.yml 取消 db 服务注释，
# 设置 TINGCHENG_DB_PASSWORD 环境变量，将 SPRING_PROFILES_ACTIVE 改为 prod：
#   docker compose up -d

# 访问服务
# 应用首页: http://localhost:8092
# Swagger UI: http://localhost:8092/swagger-ui.html
```

#### 2.2.2 本地开发部署

```bash
# 编译项目
mvn clean install

# 运行应用
mvn spring-boot:run
```

#### 2.2.3 JAR 包手动部署

```bash
# 打包
mvn clean package -DskipTests -B

# 运行
java -jar target/tingchenggis-1.0.0.jar --spring.profiles.active=prod
```

### 2.3 生产环境部署

#### 2.3.1 systemd 服务配置

**服务文件：`/etc/systemd/system/tingchenggis.service`

```ini
[Unit]
Description=TingChengGIS
After=network.target postgresql.service

[Service]
Type=simple
User=tingcheng
WorkingDirectory=/opt/tingchenggis
EnvironmentFile=/etc/tingchenggis/env
ExecStart=/usr/bin/java -jar /opt/tingchenggis/app.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

**启动服务：

```bash
# 重载 systemd
sudo systemctl daemon-reload

# 启用并启动服务
sudo systemctl enable --now tingchenggis

# 查看服务状态
sudo systemctl status tingchenggis

# 查看服务日志
journalctl -u tingchenggis -f -n 100
```

#### 2.3.2 Nginx 反向代理配置

```nginx
# HTTP → HTTPS 重定向
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    client_max_body_size 100M;

    ssl_certificate     /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://127.0.0.1:8092;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
        proxy_send_timeout 120s;
    }
}
```

> **HTTPS 证书获取**：推荐使用 Let's Encrypt + certbot 自动签发：
> `sudo certbot --nginx -d your-domain.com`

---

## 三、配置管理

### 3.1 环境变量配置

| 变量名 | 说明 | 默认值 | 是否必填 |
|--------|------|--------|---------|
| `TINGCHENG_DB_PASSWORD` | PostgreSQL 密码 | 空 | 生产环境必填 |
| `TINGCHENG_JWT_SECRET` | JWT 签名密钥 | 开发降级密钥 | 生产环境必填 |
| `SERVER_PORT` | 服务端口 | 8092 | 否 |
| `TINGCHENG_AI_DEEPSEEK_API_KEY` | DeepSeek API Key | 空 | 否 |
| `TINGCHENG_AI_ZHIPU_API_KEY` | 智谱 API Key | 空 | 否 |
| `TINGCHENG_AI_OPENAI_API_KEY` | OpenAI API Key | 空 | 否 |

**配置方式：

方式 A — `.env` 文件（Docker 部署）：

```bash
cp .env.example .env
# 编辑 .env 填入实际值
```

方式 B — Linux 环境变量：

```bash
export TINGCHENG_DB_PASSWORD=YourStrongP@ss
export TINGCHENG_JWT_SECRET="Your-JWT-Secret-Key-At-Least-32-Chars!!"
```

方式 C — systemd EnvironmentFile（生产推荐）：

```ini
# /etc/tingchenggis/env
TINGCHENG_DB_PASSWORD=YourStrongP@ss
TINGCHENG_JWT_SECRET=Your-JWT-Secret-Key-At-Least-32-Chars!!
```

### 3.2 应用配置文件

| 配置文件 | 用途 |
|---------|------|
| `application.yml` | 主配置（dev/prod 配置以多文档 YAML 内嵌在同一文件中） |

---

## 四、CI/CD 流程

### 4.1 流水线概览

```
push/PR → [test] → [docker & E2E] → [deploy（可选）]
```

| 阶段 | 任务 | 触发条件 |
|------|------|----------|
| test | Maven 后端测试 + JAR 打包 + Vitest 前端测试 | push 到 master/develop 或 PR 到 master |
| docker & E2E | 构建 Docker 镜像 + 健康检查 + Playwright E2E 测试 | test 通过 |
| deploy | SSH 部署到服务器（需手动启用） | master 分支，需配置 Secrets |

### 4.2 GitHub Secrets 配置

在 GitHub 仓库 → **Settings → Secrets and variables → Actions** 中设置：

| Secret 名称 | 用途 | 是否必填 |
|------------|------|---------|
| `DEPLOY_HOST` | 部署服务器 IP/域名 | 仅 SSH 部署 |
| `DEPLOY_USER` | SSH 登录用户名 | 仅 SSH 部署 |
| `DEPLOY_SSH_KEY` | SSH 私钥 | 仅 SSH 部署 |
| `DOCKER_USERNAME` | Docker Hub 用户名 | 如需推送镜像 |
| `DOCKER_PASSWORD` | Docker Hub 密码/Token | 如需推送镜像 |

### 4.3 启用自动部署

编辑 `.github/workflows/ci.yml`，取消 `deploy` 作业的注释即可启用自动部署。

---

## 五、数据库管理

### 5.1 PostgreSQL 初始化

```bash
# 登录 PostgreSQL
sudo -u postgres psql
```

```sql
CREATE DATABASE tingchenggis_pg;
\c tingchenggis_pg
CREATE EXTENSION postgis;
-- production profile 使用 postgres 超级用户连接（见 application.yml）
-- 如需独立用户，执行以下语句后同步修改 application.yml 中 datasource.username
-- CREATE USER tingcheng WITH PASSWORD 'YourStrongP@ss';
-- GRANT ALL PRIVILEGES ON DATABASE tingchenggis_pg TO tingcheng;
-- GRANT ALL ON SCHEMA public TO tingcheng;
```

### 5.2 数据导入

启动应用后，通过 API 导入亭子数据：

```bash
curl -X POST -F "file=@data/千亭.xlsx" http://localhost:8092/thousand-pavilions/import
```

### 5.3 数据备份

**每日自动备份脚本：**

```bash
#!/bin/bash
# /opt/backup/backup.sh

BACKUP_DIR="/backup"
DATE=$(date +%Y%m%d)

# 备份数据库
pg_dump -U postgres tingchenggis_pg | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# 备份上传文件
tar czf $BACKUP_DIR/uploads_$DATE.tar.gz /opt/tingchenggis/data/uploads

# 保留 30 天
find $BACKUP_DIR -name 'db_*.sql.gz' -mtime +30 -delete
find $BACKUP_DIR -name 'uploads_*.tar.gz' -mtime +30 -delete
```

**设置定时任务（crontab）：

```bash
# 每天凌晨 2 点执行备份
0 2 * * * /opt/backup/backup.sh
```

### 5.4 数据恢复

```bash
# 恢复数据库
gunzip -c /backup/db_20250101.sql.gz | psql -U postgres tingchenggis_pg

# 恢复上传文件
tar xzf /backup/uploads_20250101.tar.gz -C /
```

---

## 六、监控与告警

### 6.1 健康检查

| 检查项 | 命令 | 说明 |
|-------|------|------|
| 服务状态 | `curl http://localhost:8092/actuator/health` | 检查服务是否健康 |
| JVM 内存 | `curl http://localhost:8092/actuator/metrics/jvm.memory.used` | JVM 内存使用 |
| CPU 使用率 | `curl http://localhost:8092/actuator/metrics/system.cpu.usage` | 系统 CPU 使用率 |
| 服务日志 | `journalctl -u tingchenggis -f -n 100` | 实时查看日志 |

### 6.2 自动恢复

**自动健康检查与恢复脚本：**

```bash
* * * * * root curl -sf http://localhost:8092/actuator/health || systemctl restart tingchenggis
```

### 6.3 日志管理

| 日志类型 | 位置 | 说明 |
|---------|------|------|
| 应用日志 | `logs/tingcheng.log` | 应用运行日志（Docker 部署使用 `docker logs tingchenggis` 或挂载 volume 到宿主机） |
| 系统日志 | `journalctl -u tingchenggis` | systemd 服务日志 |
| 访问日志 | Nginx 配置的日志 | HTTP 访问日志 |

---

## 七、常见问题与处理

| 问题 | 可能原因 | 解决方案 |
|------|---------|---------|
| 端口 8092 被占用 | 其他进程占用端口 | `lsof -i :8092` 查占用进程，或修改 `SERVER_PORT` |
| 数据库连接失败 | 数据库未启动或配置错误 | 检查 `pg_isready`、密码、`pg_hba.conf` |
| JDK 版本不匹配 | 运行环境版本过低 | `java -version` 确认版本为 21+ |
| OSRM 超时 | 网络问题或依赖服务不可用 | 路由、导航、TSP 功能均依赖公共 OSRM 服务；超时时这些功能不可用，其他功能不受影响 |
| Docker 构建缓慢 | 网络问题或依赖下载慢 | `mvn dependency:go-offline` 预下载依赖，利用 Docker layer 缓存 |
| GitHub Actions 未触发 | 分支不匹配或 Actions 被禁用 | 检查分支名、Actions 设置、工作流文件路径 |

### 7.1 GitHub Actions 排查

| 可能原因 | 排查方法 | 解决方案 |
|----------|----------|----------|
| 分支名不匹配 | `git branch` 查看默认分支名 | 修改 `.github/workflows/ci.yml` 中 `branches:` 为实际分支名 |
| 工作流文件路径不对 | 确认文件位于 `.github/workflows/ci.yml` | 修正路径，目录名必须是 `workflows` |
| Actions 被禁用 | GitHub 仓库 → Actions 页签 | 点击 "I understand my workflows, go ahead and enable them" |
| 语法错误 | Push 后显示黄色 ❌ | 用 `act` 本地测试，或在 GitHub 上手动编辑检查语法 |
| 无新提交 | Actions 只在 push/PR 时触发 | 推送一个新 commit 触发 |

---

## 八、安全运维

### 8.1 安全加固建议

| 安全项 | 措施 |
|-------|------|
| 密码策略 | 强制使用强密码，定期更换 |
| JWT 密钥 | 使用至少 32 字符的随机密钥 |
| HTTPS | 生产环境必须启用 HTTPS |
| 防火墙 | 限制数据库端口只允许内网访问 |
| 定期更新 | 定期更新系统和依赖包 |
| 日志审计 | 定期审计访问和错误日志 |
| 备份验证 | 定期验证备份数据的可用性 |

### 8.2 权限管理

| 角色 | 权限 |
|------|------|
| ADMIN | 系统管理、数据管理、用户管理 |
| USER | 数据查询、个人信息管理 |
| 访客 | 公开数据查询、地图浏览 |

---

## 九、应急响应

### 9.1 故障等级划分

| 等级 | 影响范围 | 响应时间 | 处理时限 |
|------|---------|---------|---------|
| 严重 | 系统完全不可用 | 立即 | 2 小时内 |
| 一般 | 核心功能异常 | 1 小时内 | 4 小时内 |
| 轻微 | 非核心功能问题 | 4 小时内 | 24 小时内 |

### 9.2 应急处理流程

1. **故障发现 → 2. 故障定级 → 3. 故障报告 → 4. 故障排查 → 5. 故障处理 → 6. 故障恢复 → 7. 总结改进

### 9.3 回滚方案

```bash
# 停止当前版本
sudo systemctl stop tingchenggis

# 备份当前版本
cp /opt/tingchenggis/app.jar /opt/tingchenggis/app.jar.backup

# 回滚到上一版本
cp /opt/tingchenggis/app.jar.previous /opt/tingchenggis/app.jar

# 启动服务
sudo systemctl start tingchenggis
```

---

## 十、附录

### 10.1 文件存储路径

| 内容 | 路径 | 说明 |
|------|------|------|
| 上传图片/视频 | `data/uploads/{日期}/{uuid}.{ext} | 用户上传文件 |
| 日志文件 | `logs/tingcheng.log` | 应用日志 |
| 亭子数据 | `data/千亭.xlsx` | 初始数据文件 |
| 配置文件 | `/etc/tingchenggis/env` | 生产环境配置 |
| 备份目录 | `/backup` | 数据备份目录 |

### 10.2 相关文档

- [需求报告](../需求/TingChengGIS需求报告.md)
- [设计报告](../需求/TingChengGIS设计报告.md)
- [实现报告](../实现/TingChengGIS实现报告.md)
- [测试报告](../测试/TingChengGIS测试报告.md)
- [运维手册](../../运维手册.md)

### 10.3 部署变体

| 部署方式 | 配置文件 | 数据库 | 适用场景 |
|---------|---------|--------|---------|
| Docker 开发/演示 | 内嵌 dev profile（H2 内存） | H2 内存 | 快速体验，数据重启丢失 |
| Docker 生产 | 取消 docker-compose.yml 中 db 注释 + prod profile | PostgreSQL 16 | 生产环境 |
| 本地 JAR 演示 | `deploy/application-demo.yml`（通过 `--spring.config.additional-location` 引用） | H2 文件持久化 | 演示环境，数据持久不丢 |
| 系统服务部署 | `/etc/tingchenggis/env` + `--spring.profiles.active=prod` | PostgreSQL 16 | 生产环境 |

### 10.4 快速参考命令

| 操作 | 命令 |
|------|------|
| 查看服务状态 | `sudo systemctl status tingchenggis` |
| 启动服务 | `sudo systemctl start tingchenggis` |
| 停止服务 | `sudo systemctl stop tingchenggis` |
| 重启服务 | `sudo systemctl restart tingchenggis` |
| 查看服务日志 | `journalctl -u tingchenggis -f` |
| 数据库备份 | `pg_dump -U postgres tingchenggis_pg \| gzip > backup.sql.gz` |
| Docker 启动 | `docker compose up -d` |
| Docker 停止 | `docker compose down` |
| Docker 日志 | `docker compose logs -f` |

---

**报告结束**
