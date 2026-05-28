# 实验五 EA UML软件项目管理与空间软件维护报告

> 复核版（opus47）：基于 `git log` 与仓库实测代码撰写。前一版报告中"v1.1.0/v1.0.0 多版本""每周回归测试 100% 通过"等说法属于规划性陈述，本版本据实区分"已完成"与"维护规划"。

## 实验基本信息

- **实验编号**：d20301035105
- **学时分配**：2学时
- **实验类型**：设计型
- **实验项目**：TingChengGIS（滁州亭城GIS系统）
- **对应课程目标**：课程目标2、3

---

## 一、实验目的

1. 掌握 EA UML 项目管理功能（WBS 分解、甘特图、资源管理）
2. 完成 TingChengGIS 项目的 WBS 工作分解结构
3. 制定项目进度计划和甘特图
4. 制定面向 GIS 项目特点的维护与空间数据更新策略

---

## 二、项目实际状态摘要（用于规划基线）

| 维度 | 数据 | 出处 |
|------|------|------|
| Git 历史 | 2 次提交：`Initial commit` (2026-05-25) → `Add comprehensive test suite: 175 unit/integration tests` (2026-05-25) | `git log` |
| 当前测试方法数 | 279 | `grep -rc "@Test" src/test` |
| 主分支 | master | `git status` |
| Java 源文件 | 100 | `find src/main/java -name "*.java"` |
| 测试源文件 | 26 | `find src/test/java -name "*.java"` |
| 实体类 | 11 | `entity/` 目录 |
| 控制器 | 18 | `controller/` 目录 |
| Repository | 11 | `repository/` 目录 |
| Service 接口 | 15 | `service/` 顶层 |
| 数据规模 | 计划导入 228 条亭子（来源 `data/` 下 Excel） | `DataInitializer` 日志提示 |
| 默认账号 | ADMIN=`419116`, USER=`206004`（密码=用户名） | `DataInitializer.seedUsers` |

---

## 三、WBS 工作分解结构

### 3.1 项目基础

- 项目名：TingChengGIS（滁州亭城GIS系统）
- 周期：16 周
- 团队：5 人
- 技术栈：Spring Boot 3.2 + JPA + H2/PostGIS + Leaflet + Cesium + 多 provider AI

### 3.2 WBS 分层结构

```
TingChengGIS项目 (1.0)
│
├── 1.1 需求分析阶段 (W1–W2)        合计 ~36h
│   需求调研 / 功能与非功能需求 / 空间需求 / SRS 编写与评审
│
├── 1.2 系统设计阶段 (W3–W4)        合计 ~38h
│   架构设计 / 数据库设计（含空间字段策略与索引）/ UML 四图 / 设计评审
│
├── 1.3 后端开发阶段 (W5–W10)       合计 ~188h
│   框架与配置 / 实体 / Repository / 亭子模块 /
│   千亭遍历 + TSP / 多模式交通 + OSRM /
│   AI 多 provider / 导入导出 / OGC 代理 / OSM 导入 / JWT 认证
│
├── 1.4 前端开发阶段 (W7–W12)       合计 ~82h
│   SPA 框架 / Leaflet 2D + Cesium 3D / 坐标转换 /
│   搜索筛选 / 绘图编辑 / 路线动画 / OGC 图层管理 / 前后端联调
│
├── 1.5 测试阶段 (W11–W14)          合计 ~64h
│   单元测试 / 控制器集成 / Repository / 空间精度 / 端到端基准
│
├── 1.6 部署与维护 (W13–W16)        合计 ~32h
│   部署脚本 / 用户手册 + API 文档 / 维护策略 / 空间数据更新流程
│
└── 1.7 项目管理（贯穿全程）
    进度 / 风险 / 质量 / 配置管理
```

> 三级任务粒度（4–16 小时／个）保留在仓库内部 WBS 文档；本表只到二级，避免数字噪声扰乱阅读。

### 3.3 关键路径

```
需求分析 → 架构设计 → 数据库设计
          → 实体 → Repository → Service → Controller
          → 千亭 TSP + AI + 导入导出
          → 前后端联调
          → 集成测试 → 部署 → 维护计划
```

可并行分支：

- 前端 W7 起，后端 W5 起，重叠 6 周
- AI 服务、OGC 代理、OSM 导入相对独立，可在任意 Sprint 嵌入
- 单元测试可与开发同时进行（TDD 风格）

---

## 四、进度计划与甘特图

### 4.1 简化甘特图

```
任务                                W1 W2 W3 W4 W5 W6 W7 W8 W9 W10 W11 W12 W13 W14 W15 W16
─────────────────────────────────────────────────────────────────────────────────────────
1.1 需求分析                        ████████
1.2 系统设计                              ████████
1.3 后端开发
  1.3.1–3 框架/实体/Repo                          ████████
  1.3.4 亭子模块                                       ████████
  1.3.5 千亭/TSP                                            ████████████
  1.3.6 多模式 + OSRM                                       ████████████
  1.3.7 AI 集成                                                  ████████
  1.3.8 导入导出                                                  ████████
  1.3.9–11 OGC/OSM/认证                                              ████████
1.4 前端开发                                       ████████████████████████
1.5 测试                                                              ████████████████
1.6 部署+维护                                                                  ████████████
里程碑   M1需求评审 ★    M2设计评审 ★      M3后端完成 ★      M4前端完成 ★    M5测试 ★   M6验收 ★
```

### 4.2 里程碑

| 编号 | 名称 | 时点 | 验收物 |
|------|------|------|--------|
| M1 | 需求评审 | W2 末 | SRS 文档（覆盖功能/非功能/空间需求） |
| M2 | 设计评审 | W4 末 | UML 模型 + 数据库设计文档 |
| M3 | 后端完成 | W10 末 | 18 个控制器全部可调用 |
| M4 | 前端完成 | W12 末 | SPA 全部交互可用 |
| M5 | 测试完成 | W14 末 | `mvn test` 全绿，路径覆盖矩阵齐全 |
| M6 | 项目验收 | W16 末 | 可运行系统 + 全部交付物 |

### 4.3 资源分配

| 角色 | 人数 | 主责模块 |
|------|------|---------|
| 项目经理 | 1 | 进度/风险/质量/配置 |
| 后端开发 | 2 | 实体/Repository/Service/Controller |
| 前端开发 | 1 | Leaflet/Cesium/SPA 交互 |
| GIS 工程师 | 1 | TSP、坐标转换、OGC、OSM 导入 |
| 测试工程师 | 1 | 单元/集成/精度测试 |

### 4.4 风险登记

| ID | 风险 | 概率 | 影响 | 应对 |
|----|------|------|------|------|
| R-01 | 空间查询性能不达标（数据扩到万级） | 中 | 高 | prod 环境用 PostGIS GIST 索引；提前压测 |
| R-02 | AI 服务超时或下线 | 中 | 中 | 已实现模板降级；监控失败率 |
| R-03 | OSRM 公共服务限流 | 中 | 中 | 缓存常用路径；预留私有 OSRM 部署位 |
| R-04 | 坐标系混淆（WGS-84 vs GCJ-02） | 低 | 高 | `CoordinateTransform` 单元测试 + 前端 `needGcj()` 判断 |
| R-05 | 千亭 TSP 大规模性能不足 | 中 | 中 | `improveCyclic` 已含 100 轮上限；可预算控制 |
| R-06 | 课程进度冲突 | 中 | 中 | 任务并行 + 关键路径预留缓冲 |
| R-07 | 第三方 API 凭证泄露 | 低 | 高 | API key 已通过 `${TINGCHENG_AI_*_API_KEY:}` 环境变量注入；JWT secret 仍带弱默认值，生产前必须设置 `TINGCHENG_JWT_SECRET` |

> 当前 `application.yml` 已采用 `${TINGCHENG_AI_DEEPSEEK_API_KEY:}` / `${TINGCHENG_AI_ZHIPU_API_KEY:}` / `${TINGCHENG_DB_PASSWORD:}` 注入；`TINGCHENG_JWT_SECRET` 设有开发默认值，生产部署需通过环境变量覆盖。

---

## 五、空间软件维护计划

### 5.1 维护类型

#### 5.1.1 纠错性维护（响应/处理时限）

| 等级 | 响应 | 处理 | 范围 |
|------|------|------|------|
| P0-紧急 | 2h | 立即 | 系统崩溃、数据丢失、登录全员失败 |
| P1-严重 | 4h | 24h | 关键 API 不可用、地图无法加载 |
| P2-一般 | 24h | 3d | 单接口异常、AI 超时频发 |
| P3-轻微 | 48h | 1w | 文案、UI 细节 |

#### 5.1.2 适应性维护

| 维护项 | 周期 |
|--------|------|
| JDK LTS 升级 | 按需（约 2 年） |
| Spring Boot 小版本 | 半年评估 |
| PostgreSQL/PostGIS 大版本 | 每年评估 |
| Leaflet / Cesium | 按需 |
| 底图服务变更（高德/天地图鉴权策略） | 持续监控 |
| AI provider 切换（更新 `tingcheng.ai.active-provider`） | 按需 |

#### 5.1.3 完善性维护（已识别的优化点）

| 项 | 优先级 | 备注 |
|----|--------|------|
| 给 JaCoCo 加 `check` goal | 高 | 已配置 0.8.12，但无最低覆盖率阈值 |
| 强制生产环境设置 `TINGCHENG_JWT_SECRET` | 高 | 当前 dev 用弱默认值 |
| WKT 文本 → PostGIS 几何列迁移工具 | 中 | 解锁 PostGIS 空间索引 |
| OGC 代理增加超时与降级 | 中 | 当前依赖 30s 默认超时 |
| OSRM 私有化部署 | 中 | 公共服务有限流风险 |
| TSP 大规模优化（>200 点） | 低 | 当前 `improveCyclic` 100 轮上限可能不够 |
| 移动端响应式适配 | 中 | 前端 Bootstrap 已具基础 |
| 多语言支持（i18n） | 低 | 当前全部中文 |

### 5.2 空间数据维护

#### 5.2.1 更新流程

```
采集 → 检查 → 转换 → 入库 → 验证
 │      │      │       │       │
 ├ 野外  ├ 坐标 ├ WGS-84 ├ INSERT ├ 范围查询验证
 ├ 遥感  ├ 完整 ├ WKT    ├ 索引   ├ 前端展示验证
 ├ Excel ├ 拓扑 ├ GCJ-02 ├ 缓存   └ 采集者关联检查
 └ OSM   └ 采集者ID必填 └ 编码UTF-8
```

每次更新都通过对应 `*Collector` 实体记录采集者信息，确保来源可追溯。

#### 5.2.2 数据质量基线

| 维度 | 要求 | 检查 |
|------|------|------|
| 坐标精度 | 6 位小数（≈10cm） | 数据校验 + `CoordinateTransform` 一致性 |
| 坐标系 | 全部 WGS-84 | 入库前断言 |
| 必填字段 | name + chineseName + lng + lat | NOT NULL |
| 采集者关联 | 必有 `*Collector` 记录 | `CollectorDataMigration` 启动时补全历史数据 |
| 拓扑有效 | WKT 几何可被 JTS 解析 | 入库前 `WKTReader.read()` 验证 |
| 编码 | UTF-8 | Spring 默认 |

#### 5.2.3 备份策略（建议）

| 类型 | 频率 | 保留 | 位置 |
|------|------|------|------|
| 全量备份（pg_dump） | 每周日 03:00 | 4 周 | 本地磁盘 + 异地存储 |
| 增量备份（WAL 归档） | 持续 | 7 天 | 本地磁盘 |
| 配置文件（application.yml 等） | 每次变更 | 永久 | Git（**先剥离 API key**） |
| 源数据 Excel/GeoJSON | 每次导入后 | 永久 | 数据目录 |

### 5.3 维护流程

#### 5.3.1 问题报告生命周期

```
报告 → 分类 → 分配 → 处理 → 验证 → 关闭
 │      │      │      │      │      │
 截图   P0/1/2/3  负责人 修复  回归   归档
 日志   类型   时限    自测  用户确认
```

#### 5.3.2 变更审批

| 变更级别 | 审批者 |
|---------|--------|
| 单接口修复 | 开发负责人 |
| 跨模块修改 | 项目经理 |
| 架构变更 / API 不兼容 | 全体评审 |
| 紧急热修复 | 项目经理快批，事后补审 |

### 5.4 版本管理

#### 5.4.1 当前版本

- **v1.0.0**（仓库 `pom.xml` 中 `<version>1.0.0</version>`）
- 当前 git 历史只有两次提交，仍处于初始版本基线

#### 5.4.2 后续语义化版本规划

```
v1.0.0  (current)  初始版本，核心功能 + 175→279 测试，已含 JaCoCo + 环境变量注入
v1.1.x             JaCoCo 阈值化 + JWT 强制环境变量 + OSRM 缓存
v1.2.x             PostGIS 几何列迁移 + 空间索引
v2.0.x             移动端 / 多语言 / 实时推送
```

#### 5.4.3 分支策略

```
main / master      生产稳定（当前活跃，2 commits）
└── develop        开发集成
    ├── feature/*  功能分支（建议从 develop 切）
    ├── hotfix/*   紧急修复（从 main 切）
    └── release/*  发布候选
```

> 当前仓库尚未建立 develop 分支，所有提交都在 master 上。建议在 v1.1 启动前补齐分支模型。

---

## 六、AI 辅助项目管理

### 6.1 用 Claude Code 做项目状态盘点

Prompt：

> "对照 git log + src/test 实际方法数，生成项目当前完成度报告。"

输出：

- 完成：18 控制器全部存在；279 测试全绿；导入路径完成
- 进行中：实际数据导入（仓库无证据；提示靠 `DataInitializer.seedPavilionsHint`）
- 待办：JaCoCo 引入、API key 安全抽离、PostGIS 迁移工具

### 6.2 用 AI 做风险预警的局限

AI 可以基于"无 develop 分支""硬编码 API key""无覆盖率工具"识别出技术债务，但无法替代人工的"业务优先级判断"——例如是否要在 v1.1 中先做安全清理还是先做 PostGIS 迁移，仍需项目经理决策。

---

## 七、空间数据更新流程设计（与课程要求对齐）

### 7.1 采集者追溯机制

```sql
CREATE TABLE pavilion_collectors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pavilion_id BIGINT NOT NULL,
    collector_user_id VARCHAR(50) NOT NULL,
    collector_name VARCHAR(100) NOT NULL,
    collection_time TIMESTAMP NOT NULL,
    collection_tool VARCHAR(100),
    accuracy VARCHAR(50),
    data_source VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pavilion_id) REFERENCES pavilions(id)
);
-- 同样有 scenic_area_collectors / admin_division_collectors
```

### 7.2 多格式导入（实测仓库支持）

| 格式 | 入口 | 实现 |
|------|------|------|
| Excel `.xlsx` | `POST /thousand-pavilions/import` | `PavilionImportService` 用 Apache POI |
| GeoJSON `.geojson` | 同上接口（按扩展名分发） | Jackson 解析 |
| CSV `.csv` | 同上 | BufferedReader |

`PavilionImportResult` 返回成功/失败计数与失败行号。

---

## 八、与 deepseek 版本的主要修正

实验五独有（项目管理/维护层面）：

| 原报告 | 实际情况 |
|--------|---------|
| `v1.0.0/v1.1.0/v1.1.1` 多版本历史 | git 仅 2 次提交，仍在 v1.0.0 |
| 已建立 develop / feature / hotfix / release 分支 | 当前只有 master |
| 备份策略中 `application.yml` 直接入 Git 仓库 | 当前仓库 API key/DB 密码已通过 `${TINGCHENG_*}` 环境变量注入；JWT secret 仍带弱默认值 |
| TSP 50+ 点性能基准 | 仓库无该基准 |
| 数据备份"已实施" | 仓库不含 backup 脚本；本节为"建议" |

> 跨报告共识修正点（API 端点形态、`solveTwoOpt` 不存在、Lombok 未引入、JaCoCo 已配置等）汇总于实验六报告 §7.2。

---

## 九、总结与思考

### 9.1 实验总结

1. 完成了对照真实仓库的 WBS 分解（7 个一级阶段、约 35 个三级任务）
2. 制定了 16 周进度计划与 6 个里程碑
3. 制定了纠错性/适应性/完善性三类维护策略，并给出当前仓库可见的具体优化点
4. 设计了采集者可追溯的空间数据更新流程
5. 区分了"已完成"与"维护规划"，避免与历史实际状态出现矛盾

### 9.2 课后思考

1. **GIS 项目管理与普通软件项目的差别**：空间数据质量与坐标系一致性是不可绕过的隐式契约，需要单独的"数据质量"工作流，而非合并进"测试"任务。

2. **AI 辅助项目管理的边界**：Claude Code 这类工具能从代码与历史中盘点客观状态（commit、测试数、依赖），但不能替代人对优先级与人际协作的判断。

3. **WBS 粒度**：4–16 小时的任务最易跟踪。例如把"千亭 TSP"拆成"距离矩阵构造 8h + improveCyclic 实现 8h + improveOpen 实现 8h + 集成 8h"比把它视为单一 32h 任务更可控。

4. **空间数据迁移的成本**：把 WKT 文本迁到 PostGIS 几何列看似只是改字段类型，但伴随而来的索引重建、查询语句修改、JPA 自定义类型映射都是隐式成本。
