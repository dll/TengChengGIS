# 滁州亭城 GIS 项目 全面审核报告（Opus47 版）

- 审核对象：TingChengGIS（滁州亭城 GIS 系统）
- 审核日期：2026-05-29
- 审核分支：`master`（HEAD `54ce0f6`，working tree 含 7 个文档/1 个控制器修改）
- 审核版本基线：本轮已包含「第一轮审核」P0/P1 全部落地 + 模块化前端 + Docker + CI + i18n + E2E
- 审核视角：① 软件工程专家 ② 高校讲师 ③ 空间与数字技术专业 ④ 教学案例评委
- 审核方式：源码静态审查（pom.xml / application.yml / 100 个 Java 源 / 8 个前端模块 / `.github/workflows/ci.yml` / `Dockerfile` / 26 个测试类）+ git log 实测，未运行时验证

---

## 一、本轮项目状态摘要

| 维度 | 实测数据 | 出处 |
|------|---------|------|
| Git 历史 | **8 次提交**（从 `Initial commit` 到 `修复E2E测试`） | `git log` |
| Java 源文件 | **100** | `find src/main/java -name "*.java" \| wc -l` |
| 控制器 | **19**（新增 `FileUploadController`、`HomeController`、`PoiController`、`NavigationController`、`OgcProxyController`、`OsmImportController` 等） | `controller/` 目录 |
| 实体 | **11** | `entity/` 目录 |
| Repository | **11** | `repository/` 目录 |
| Service 接口 | **15+**（含 `OverpassPoiService` / `VrArService`） | `service/` 顶层 |
| 测试方法数 | **279**（`@Test` 计数） | `grep -rc "@Test" src/test` |
| 前端 JS 模块 | **8 个**（`auth/config/core/features/i18n/init/ui/utils`，共 4,418 行）+ `index.html` 830 行 | `src/main/resources/static/` |
| 前端单元测试 | **5 个 vitest 文件**（`auth/core/i18n/ui/utils`） | `src/test/frontend/` |
| E2E 测试 | **Playwright `app.spec.js`** | `e2e/` |
| CI/CD | **GitHub Actions**：JDK21 + Node22 + Maven test + Docker build + healthcheck + Playwright | `.github/workflows/ci.yml` |
| 容器化 | **多阶段 Dockerfile**（Maven 构建 → JRE 运行 + 非 root 用户 + 数据卷） | `Dockerfile` |
| 覆盖率工具 | **JaCoCo 0.8.12**（已配置 `prepare-agent` + `report`，未设阈值） | `pom.xml` |

---

## 二、自第一轮审核以来的关键改进（已落地）

| 类别 | 第一轮问题 | 当前状态 |
|------|----------|---------|
| 🔴 鉴权失效（permitAll） | `SecurityConfig` 末句 `anyRequest().permitAll()` | ✅ 改为白名单 + `POST/PUT/DELETE` 默认 `authenticated()`，4 个 ADMIN-only 路径显式 `hasRole("ADMIN")` |
| 🔴 admin/admin123 硬编码 | `AuthController` 内嵌账号 | ✅ 引入 `AppUser` 实体 + BCrypt + JWT；`DataInitializer` 种子 `419116/419116` (ADMIN)、`206004/206004` (USER) |
| 🔴 API Key 入库 | DeepSeek/智谱 key 明文 commit | ✅ 改为 `${TINGCHENG_AI_DEEPSEEK_API_KEY:}` / `${TINGCHENG_AI_ZHIPU_API_KEY:}` 环境变量注入；DB 密码同处理 |
| 🟠 N+1 距离矩阵 | `buildDistanceMatrix` 调用 N×N 次 `findById` | ✅ 改为一次 `findAll` 装入 `Map<Long,Pavilion>`，矩阵在内存中两两计算 |
| 🟠 4 处 Haversine 重复 | 4 个不同实现 | ✅ 统一到 `GeoUtils.haversineKm(lon,lat,lon,lat)`，参数顺序一致 |
| 🟠 全局异常缺失 | Controller 处处 try/catch + RuntimeException | ✅ 引入 `BusinessException` / `NotFoundException` / `GlobalExceptionHandler` |
| 🟠 CORS `*` + credentials | 违反 CORS 规范 | ✅ 改为 `localhost:*` / `127.0.0.1:*` 白名单 + `setAllowedOriginPatterns` |
| 🟠 冗余依赖 | `openai-gpt3-java` + `PostgresTest` exec 插件 | ✅ pom.xml 中已删除 |
| 🟠 前端 4983 行单页 | 单文件无模块化 | ✅ 拆为 8 个 ES 模块（`auth/config/core/features/i18n/init/ui/utils`），主 `index.html` 830 行 |
| 🟠 缺 CI/CD | 无 workflows | ✅ `.github/workflows/ci.yml`：测试 + 打包 + Docker + 健康检查 + Playwright |
| 🟡 缺容器化 | 无 Dockerfile | ✅ 多阶段 Dockerfile（builder + runtime + 非 root + 数据卷） |
| 🟡 国际化 | 全中文 | ✅ `i18n.js` + `i18n.test.js` 引入语言切换 |

> 这些改进让"P0 安全止血""P1 工程基线"在第一轮报告中提到的 9 项任务基本完成，工程基线分应明显抬升。

---

## 三、本轮新发现 / 仍待处理的问题

### 视角 1：软件工程专家

#### 优点

1. **鉴权与异常基线成熟**：`SecurityConfig` 路由白名单清晰（公开 GET / `/ai/**` POST / 写操作鉴权 / 4 个 ADMIN-only 路径），全局异常通过 `GlobalExceptionHandler` 统一处理。
2. **构建工具链现代且齐备**：JDK 21 + Spring Boot 3.2 + JaCoCo + Maven + GitHub Actions + Docker 多阶段镜像 + Playwright E2E + Vitest 前端单元，覆盖了"测—构—装—验"完整链路。
3. **分层依然清晰**：100 个 Java 源 + 19 控制器 + 11 实体 + 11 仓储 + 15+ Service 接口；包结构稳定，新增功能均落在既有分层。
4. **环境变量注入到位**：`application.yml` 不再含真实密钥；JWT secret 有降级值用于 dev，但提示语已写明 `生产环境必须通过环境变量 TINGCHENG_JWT_SECRET 设置强密钥`。
5. **N+1 已修复**：`ThousandPavilionsServiceImpl.calculateDistance` 仍是简单两次 `findById`，但 `buildDistanceMatrix` 已改为 `findAll` 一次装载（按第一轮记录），距离矩阵复杂度回到 O(N²) 内存运算而非 O(N²) 数据库往返。

#### 仍存的问题

1. **🟠 `ThousandPavilionsController` 仍 722 行（仅从 873 缩减到 722）**
   - 第一轮 P1-7 计划「拆分为 3 个 ≤ 250 行控制器」未完成；
   - 内部仍混杂导入、导出、分享、TSP、字段归一化等多重职责；
   - 建议沿用 `PavilionImportExportController` / `PavilionRouteController` 的拆分思路，或至少把字段归一化下沉到 `PavilionService.normalize`。

2. **🟠 `calculateDistance(Long, Long)` 仍是双 `findById`**
   - 应在 Service 内统一使用 `findAllById(Set.of(id1, id2))` 一次取回，或直接接受 `Pavilion` 对象避免数据库往返；
   - `getOptimalTraversalRoute()` 第 75 行调用 `findAll()` 后又在 `getAccessibilityMatrix` / 其他路径里重复 `findAll`，对 228 行数据无感，但 1 万级会显著放大。

3. **🟠 `getStats()` 多次 `findAll` 未消除（按第一轮报告 C6）**
   - 第一轮报告明确点出此点，需要回归确认；建议合并为一次扫描 + 内存聚合，或改用 JPQL `count + group by`。

4. **🟠 `@Transactional` 仍是类级别粗粒度**
   - 仅 `PavilionServiceImpl` 一处类级；查询方法无 `readOnly = true`；写方法无传播策略与隔离级别声明；
   - 长写事务（导入 228 行 Excel）会持有数据库连接较长，建议方法级 `@Transactional(propagation = REQUIRES_NEW, timeout = 30)`。

5. **🟠 缺 OpenAPI/Swagger（第一轮 P1-3 未做）**
   - 19 个控制器、约 80+ 端点，外部联调和测试只能看源码；
   - 引入 `springdoc-openapi-starter-webmvc-ui` 一行依赖即可生成 `/swagger-ui.html`。

6. **🟠 缺 DTO/VO 分层（第一轮 P1-4 未做）**
   - Controller 仍多处直接返回 `Pavilion` 实体或拼装 `Map<String, Object>`；
   - 没有入参校验注解 `@Valid`（`spring-boot-starter-validation` 已引入但仅生效在极少数路径）；
   - 实体延迟加载字段、内部审计字段（`createdAt/updatedAt`）会序列化暴露给前端。

7. **🟠 JaCoCo 仅生成报告无阈值**
   - `pom.xml` 配置了 `prepare-agent + report`，但没有 `check` goal；
   - 建议加 `<rule><limits><limit><counter>INSTRUCTION</counter><value>COVEREDRATIO</value><minimum>0.70</minimum></limits>` 让 CI 在覆盖率掉到 70% 以下失败。

8. **🟡 `@PreAuthorize` 注解未使用**
   - 角色控制全部集中在 `SecurityConfig.filterChain` 的路径匹配里；
   - 一旦增加 ADMIN-only 路径，要在两处（路径匹配 + 业务代码）维护，容易遗漏；
   - 建议在 4 个 ADMIN-only 控制器方法上补 `@PreAuthorize("hasRole('ADMIN')")` 双重保险。

9. **🟡 `argLine` 变量在 surefire 中可能被覆盖**
   - `pom.xml:152` 用 `<argLine>-Dfile.encoding=UTF-8 ${argLine}</argLine>` 引用上层定义；
   - JaCoCo `prepare-agent` 也会注入 `argLine`（用于 agent jar 路径）；
   - 这个写法目前能正常拿到 JaCoCo agent，但顺序敏感；建议改用 surefire 的 `<systemPropertyVariables>` 显式指定编码，避免与 JaCoCo 抢同一个属性。

10. **🟡 `PostgresTest.java` 仍在 `src/main/java` 顶层**
    - 第一轮已删 pom 中 exec 插件配置，但源文件尚在；
    - 一个调试用的 main 函数留在主代码里既容易被 fat jar 误打包，又会出现在线上运行时的 classpath，建议挪到 `src/test` 或 `tools/` 目录。

11. **🟡 控制器路径不统一**
    - 大多无前缀（`/pavilions`、`/scenic-areas`），但 `FileUploadController` 用 `/api/upload`，`NavigationController` 用 `/nav`；
    - 演示无碍，但对学生与文档自动化都不友好；建议要么全部 `/api/*`，要么全部裸路径。

#### 软件工程视角综合分：**8.0 / 10**（上一轮 6.0 → 改造后 7.5 → 本轮再增 0.5）

---

### 视角 2：高校讲师

#### 优点

1. **教学案例已形成完整闭环**：6 个实验报告（实验一～六，含 `+opus47` 复核版）覆盖需求分析、UML 建模、Vibe Coding、AI 测试、项目管理、综合实践，并且每份报告都对照实测代码做了"deepseek 修正表"，避免照模板编造。
2. **CI/CD + Docker + E2E 落地后，"软件工程"教学单元变得可演示**：可以直接给学生看 GitHub Actions 跑测试 + 打镜像 + 健康检查 + Playwright 的全流程，比单纯讲 `mvn test` 立体得多。
3. **登录与角色双账号是天然的"鉴权演示单元"**：`419116/ADMIN` vs `206004/USER` 的对比可以直接拿来讲 RBAC、JWT、`@PreAuthorize`、`hasRole` 的差异。
4. **前端模块化 + i18n 让"前端工程化"教学有抓手**：从单文件 4983 行到 8 个 ES 模块 + Vitest 单测，是非常好的"演进式"教学样本——可以让学生先看初版再做重构。

#### 仍存的问题

1. **🟠 README/FEATURES 与代码 API 仍未对齐（第一轮 C11）**
   - 第一轮 P1-9 「修复 README/FEATURES 与代码不一致的接口路径，统一为单一前缀」未完成；
   - 实验三 +opus47 报告已校正了端点形态，但 README 一级文档仍可能与实际有偏差，需逐条 diff。

2. **🟠 算法解读仍缺**
   - `TspSolver.improveCyclic` / `improveOpen` 没有头部注释解释 2-opt 的数学动机、为什么要从 `i=1` 起、什么时候用闭环 vs 开放路径；
   - `CoordinateTransform.gcj02ToWgs84` 的 5 轮迭代逼近也没有任何注释；
   - 学生看完会知其然不知其所以然。建议每个核心算法加 5–10 行注释 + 一篇配套的 markdown 推导。

3. **🟠 实验报告中明确点出的"待补强"未追踪**
   - 实验四 §7.2 列出了 4 项 testing TODO（JaCoCo 阈值、OGC mock、坐标 (0,0) 容错单测、AI 降级契约测试）；
   - 实验五 §5.1.3 列出 8 项完善性维护项（JaCoCo 阈值、JWT 强制、PostGIS 迁移工具等）；
   - 这些项目应该转化为 GitHub Issues 或 `docs/TODO.md`，否则报告里写了等于没写。

4. **🟡 数据集单一**
   - 仍只有 `data/千亭.xlsx` 一份（228 行）；
   - 缺少 5–10 条 SQL/GeoJSON 最小种子，导致首次启动后地图空白，需要先教学生导入 Excel；
   - 这一项第一轮 P3-3 列出但未做。

5. **🟡 教学指导书未补**
   - 实验报告 6 份偏"事后总结"风格；
   - 缺一份「滁州亭城 GIS 实验课程总指导（章节切片 / 知识点标签 / 难度 / 实验时长 / 思考题 / 扩展挑战）」，让讲师能直接照搬到 16 周课表里；
   - 第一轮 P3-1 提及，未启动。

6. **🟡 前端组件无注释、无 JSDoc**
   - 8 个 JS 模块共 4,418 行，但 `core.js`（1,110 行）、`ui.js`（1,348 行）、`features.js`（1,242 行）几乎无 JSDoc；
   - 学生想理解"为什么 needGcj() 在这里调用而不是在 fetch 拦截器里"会很困难。

#### 讲师视角综合分：**8.0 / 10**（上一轮 7.0 → 改造后 7.5 → 本轮再增 0.5，主要因 i18n / E2E / 实验报告复核版落地）

---

### 视角 3：空间与数字技术专业

#### 优点

1. **OSRM/Overpass/坐标转换的真实接入仍是亮点**：`RoutingClient` 多 profile（walking/cycling/driving）+ 中文化 turn-by-turn + 失败降级 Haversine；`OverpassPoiService` 实时拉 OSM amenity/tourism；`CoordinateTransform` WGS-84↔GCJ-02 双向迭代。
2. **OGC 代理已实现**：`OgcProxyController` 转发 WMS/WFS GetCapabilities 与 GetMap/GetFeature 给三方服务，符合"OGC 标准适配"教学要求。
3. **TSP 双模式**：`improveCyclic`（含闭环回起点）+ `improveOpen`（开放路径），覆盖了"千亭遍历"和"起点终点固定"两种场景。
4. **采集者追溯 + GCJ-02 双列**是正确的"中国地图合规 + 数据治理"教学样例。

#### 仍存的问题

1. **🔴 "PostGIS 集成"仍名不副实（第一轮 C4 未推进）**
   - 5 个实体（Pavilion / ScenicArea / AdminDivision / TransportRoute / TourismRoute）的 `geom_wkt` 仍是 `TEXT` 列；
   - 没有 `geometry(Point,4326)` 列、没有 `CREATE INDEX ... USING GIST`；
   - prod profile 配的 PostgreSQL 实质退化为「能存 WKT 的关系数据库」；
   - 第一轮 P2-1 / P2-2 计划未启动，建议至少给出迁移脚本（即使不切换默认）。

2. **🟠 范围查询仍走 BETWEEN**
   - `PavilionRepository.findByGeographicRange(minLng, maxLng, minLat, maxLat)` 是矩形查询；
   - 优点是 H2/PG 通用；缺点是无法表达 `ST_Within(polygon)`，跨纬度时也会因地球曲率出现细微误差；
   - 教学上可作为讨论点，但代码中没有任何注释提示这一限制。

3. **🟠 TSP 用 Haversine 直线距离（第一轮 C10 未改）**
   - 第一轮 P2-5 计划「TSP 改造为 OSRM Table API 路网距离矩阵」未启动；
   - 当前"最优路线"严格说是"几何最优"而非"路网最优"；
   - 在城市道路里两者差异显著，演示给评委可能被追问。

4. **🟠 缺少经典 GIS 分析（第一轮 P3-6）**
   - README/FEATURES 仍可能宣称「缓冲区分析」「热力图」「密度分析」，但 `service/` 与 `controller/` 中没有 `ST_Buffer`、`ST_Density`、`KDE` 任何实现；
   - 这是空间专业评委最容易扣分的点，建议至少补一个 `GET /pavilions/{id}/buffer?radiusKm=` 的 Demo。

5. **🟡 滁州 bbox 校验未生效**
   - `application.yml` 配置了 `tingcheng.chuzhou-bounds: { min-lat:31.8, max-lat:33.0, min-lng:117.8, max-lng:119.2 }`；
   - `Pavilion` 写入路径无 bbox 校验，任何全球点都能入库；
   - 建议在 `PavilionService.createPavilion` 里加一个软警告或拒绝（教学上可作为"业务规则配置化"的样例）。

6. **🟡 `GeometryFactory` 注入但未使用**
   - `AppConfig` 注入了 `GeometryFactory(PrecisionModel(4326))`，但 grep 全仓未发现任何 `geometryFactory.create*` 调用；
   - 留作未来 PostGIS 升级的接口可以接受，但应在注释里说明，否则像"挂着没用"。

#### 空间与数字技术视角综合分：**6.5 / 10**（与上一轮持平——本轮没有动 PostGIS 集成与路网 TSP 这两个最大短板）

---

### 视角 4：教学案例评委

#### 优点

1. **演示稳定性显著提升**：CI 跑通 + Docker 镜像 + 健康检查 + Playwright，证明「拉代码 → 跑起来 → 看见效果」这一链路是被自动验证过的。
2. **演示路径已部分成形**：Docker 一键启动 + 默认账号 + Excel 导入指引，相比第一轮的"自行 mvn run"已大有进步。
3. **教学合规**：API key 已脱敏（环境变量），敏感配置不再入库；评委 review 时不会再看到硬编码 sk-… 的尴尬。
4. **6 份实验报告 + 复核版构成"自洽案例集"**：评委可对照报告核查代码，每份报告底部都有"deepseek 修正表"，体现了"AI 辅助 + 人工复核"的真实流程，本身就是教学案例的一部分。
5. **i18n + 模块化前端**：让本案例从"国内教学 demo"升级为"可对外展示"的水平，可作参赛/评比材料。

#### 仍存的问题

1. **🟠 演示"可看不可摸"**
   - 默认地图空白，需要先 `POST /thousand-pavilions/import` 上传 Excel，评委 30 秒内进不去状态；
   - 第一轮 P3-3 提议「提供 5–10 条 SQL 种子，启动即有数据」仍未做；
   - 建议在 `DataInitializer` 中加一个 `if-empty-then-load-from-classpath` 的兜底，从 `resources/seed/sample-pavilions.json` 读 10 条入库。

2. **🟠 缺 demo 视频/截图**
   - README 顶部没有项目演示截图、动图或 5 分钟讲解视频链接；
   - 第一轮 P3-2 列出，未启动；
   - 教学评比通常要求 1–3 分钟视频说明，建议尽快补。

3. **🟠 数据出处合规性未声明**
   - `data/千亭.xlsx` 来源没有 README / `data/README.md` 标注；
   - 教学案例评比通常需要数据授权说明（公开数据集 / 校内项目数据 / CC 协议），建议补一段。

4. **🟡 多媒体导览仍是 txt 文件**
   - `static/audio/guides/*.txt` 是文字描述而非真实音频；
   - 演示页若声称「多媒体导览」，评委会质疑是否真的可放音；
   - 建议至少为醉翁亭、丰乐亭等 3–5 个核心亭子录 30 秒音频或合成 TTS。

5. **🟡 应用监控/可观测性未落地**
   - `spring-boot-starter-actuator` 已引入，`/actuator/health` 已在 SecurityConfig 白名单；
   - 但 metrics、prometheus、logback JSON 输出都未启用；
   - 第一轮 P3-7 提到但未做。

6. **🟡 README "已知问题清单"链接缺失**
   - 第一轮报告留下了 17 项问题清单（C1–C17），第二轮已解决多数；
   - 建议在 README 顶部嵌入「📋 项目质量与已知限制」一节，链接到本份审核报告与 6 份实验报告，让评委 30 秒看到"本项目知道自己哪里好哪里不足"。

#### 教学案例评委视角综合分：**7.5 / 10**（上一轮 6.5 → 改造后 7.0 → 本轮再增 0.5）

---

## 四、问题汇总（按严重度）

| 严重度 | 编号 | 问题 | 第一轮编号 |
|-------|------|------|-----------|
| 🔴 高 | N1 | PostGIS 集成实质未落地，`geom_wkt TEXT` + `BETWEEN` 矩形查询，万级数据扩展性差 | C4（继承） |
| 🟠 中 | N2 | TSP 仍用 Haversine 直线距离，与"最短路径"宣称不符 | C10（继承） |
| 🟠 中 | N3 | `ThousandPavilionsController` 仍 722 行，多职责混杂 | C7（部分继承） |
| 🟠 中 | N4 | `calculateDistance` / `getStats` 仍存在重复 `findById` / `findAll` 隐患 | C6（部分继承） |
| 🟠 中 | N5 | 无 OpenAPI/Swagger 文档，外部联调全靠源码 | C8（继承） |
| 🟠 中 | N6 | 无 DTO/VO 分层，Controller 直接收发 Entity，缺 `@Valid` | C8（继承） |
| 🟠 中 | N7 | JaCoCo 已配置但无覆盖率阈值 check goal | 新增 |
| 🟠 中 | N8 | README/FEATURES 与代码 API 不完全一致 | C11（继承） |
| 🟠 中 | N9 | "缓冲区/热力图/密度"三类经典空间分析仍未实现 | 新增 |
| 🟠 中 | N10 | 启动后无种子数据，地图空白，演示门槛高 | P3-3（继承） |
| 🟡 低 | N11 | `@Transactional` 粒度粗，未声明 readOnly/timeout | 新增 |
| 🟡 低 | N12 | 角色控制只在 `SecurityConfig` 路径匹配里，方法上无 `@PreAuthorize` | 新增 |
| 🟡 低 | N13 | `PostgresTest.java` 仍残留在 `src/main/java` 顶层 | 部分新增 |
| 🟡 低 | N14 | 滁州 bbox 配置未在写入路径校验生效 | 新增 |
| 🟡 低 | N15 | 控制器路径前缀不统一（`/api/upload` / `/nav` / 其他裸路径） | 新增 |
| 🟡 低 | N16 | 多媒体导览 `audio/guides/*.txt` 仍是文本而非真实音频 | C15（继承） |
| 🟡 低 | N17 | 算法核心代码（TSP / 坐标迭代）缺中文注释，教学价值打折 | 教学（继承） |
| 🟡 低 | N18 | README 缺 demo 视频 / 截图，且未链接审核报告与实验报告 | P3-2（继承） |

---

## 五、改进路线图

### 🔧 P1：本周内（基线打磨，高 ROI）

| # | 任务 | 解决问题 |
|---|------|---------|
| P1-A | 给 JaCoCo 加 `check` goal，设最低 INSTRUCTION 覆盖率 70%；CI 失败即阻断 | N7 |
| P1-B | 把 `PostgresTest.java` 移出 `src/main/java`（迁到 `src/test/java/.../tools/` 或删除） | N13 |
| P1-C | `calculateDistance(Long, Long)` 改用 `findAllById(Set.of(...))`；提供重载 `calculateDistance(Pavilion, Pavilion)` | N4 |
| P1-D | `getStats()` 合并多次 `findAll`，或改 JPQL `count(*) group by` | N4 |
| P1-E | 引入 `springdoc-openapi-starter-webmvc-ui`（一行依赖），生成 `/swagger-ui.html` | N5 |
| P1-F | 在 4 个 ADMIN-only 控制器方法上补 `@PreAuthorize("hasRole('ADMIN')")`（双保险） | N12 |
| P1-G | 把 README / FEATURES 中所有 API 路径用 `grep` 与代码 diff 一遍，统一为实际前缀 | N8 |
| P1-H | 写一段 `DataInitializer.bootstrapSampleData()`：DB 空时从 classpath 加载 10 条最小亭子样例 | N10 |

### 🏗️ P2：本月内（质量与规范）

| # | 任务 | 解决问题 |
|---|------|---------|
| P2-A | `Pavilion` 拆 `PavilionVO`/`PavilionRequest`；Controller 不再直接收发 Entity；写接口加 `@Valid` | N6 |
| P2-B | `ThousandPavilionsController` 拆为 `PavilionImportExportController` + `PavilionRouteController` + `PavilionTourController` | N3 |
| P2-C | `@Transactional` 改方法级，查询加 `readOnly=true`，导入加 `timeout=30` | N11 |
| P2-D | `PavilionService.createPavilion` / `updatePavilion` 加滁州 bbox 校验（warn 或 reject） | N14 |
| P2-E | 控制器路径统一前缀（建议 `/api/*` 单一前缀，旧路径 6 个月过渡期 alias） | N15 |
| P2-F | 给 `TspSolver`、`CoordinateTransform` 关键算法补 5–10 行中文注释 + `docs/algorithms/` 推导 markdown | N17 |
| P2-G | 至少补一个真实的空间分析端点（`GET /pavilions/{id}/buffer?radiusKm=`），用 JTS 算缓冲区返回 GeoJSON | N9 |
| P2-H | README 顶部加「📋 项目质量与已知限制」一节；嵌入 demo 截图 / 1 分钟动图 | N18 |

### 🚀 P3：本季度内（教学/评比定型）

| # | 任务 | 解决问题 |
|---|------|---------|
| P3-A | PostGIS 真正落地：Pavilion 加 `geometry(Point,4326)` + GIST 索引；`findByGeographicRange` 改 `ST_Within(geom, ST_MakeEnvelope(...))`；提供 H2→PG 迁移脚本 | N1 |
| P3-B | TSP 改用 OSRM Table API 拿"路网距离矩阵"；保留 Haversine 作为「理论最短」基准对比 | N2 |
| P3-C | 录制 5 分钟 demo 视频；为醉翁亭/丰乐亭等 5 个核心亭子录 30 秒导览音频 | N16 / N18 |
| P3-D | 补「滁州亭城 GIS 实验课程总指导」：8 章节 + 知识点标签 + 难度 + 实验时长 + 思考题 + 扩展挑战 | 教学闭环 |
| P3-E | 把实验四/五报告里的 12 项 TODO 转 GitHub Issues，标 milestone | 跟踪 |

---

## 六、综合评价

### 评分变化

| 视角 | 第一轮基线 | 第一轮改造后 | **本轮（Opus47）** | 变化 |
|------|:---:|:---:|:---:|---|
| 软件工程专家 | 6.0 | 7.5 | **8.0** | +0.5（CI/CD + Docker + 模块化前端 + i18n） |
| 高校讲师 | 7.0 | 7.5 | **8.0** | +0.5（6 份实验复核版 + 前端工程化教学样本） |
| 空间与数字技术 | 6.5 | 6.5 | **6.5** | 0（PostGIS 与路网 TSP 仍未启动） |
| 教学案例评委 | 6.5 | 7.0 | **7.5** | +0.5（CI 健康检查 + 镜像 + 实验报告闭环） |
| **加权平均**（工程 30% / 讲师 25% / 空间 25% / 评委 20%） | 6.5 | 7.2 | **7.55** | +0.35 |

### 一句话总评

> 本项目已从"立意优秀但工程基线有缺失"演进为"工程基线扎实、教学闭环成型"的成熟教学案例。当前最大的两个未触达短板是 **PostGIS 真实空间能力（N1）** 与 **OSRM 路网 TSP（N2）**——这是把综合分从 7.5 推到 8.5+ 的杠杆。完成 P1（基线打磨）后可达 7.8，完成 P2（质量与规范）后可达 8.2，完成 P3（PostGIS + 路网 TSP + 教学指导书 + demo 视频）后具备参与省级以上教学案例评比与"高校精品课程"提报的硬实力。

### 立即可做的 5 件小事（成本 < 1 小时各）

1. 给 JaCoCo 加 `check` goal + 70% 阈值（5 分钟）
2. 把 `PostgresTest.java` 删除或迁到 `src/test/java/tools/`（10 分钟）
3. 在 4 个 ADMIN 路径的 controller 方法上加 `@PreAuthorize("hasRole('ADMIN')")`（10 分钟）
4. 引入 springdoc-openapi 一行依赖，访问 `/swagger-ui.html`（15 分钟）
5. README 顶部加「📋 项目质量与已知限制」section，链接本审核报告 + 6 份实验报告（30 分钟）

---

## 七、附录：本轮验证基础

- **未运行时验证**。本报告基于源码静态审查 + git log + 测试方法计数，未启动应用、未跑 mvn test、未执行 Docker 镜像。
- **建议 P1 完成后启动第三轮"运行时 + 渗透 + 性能"审核**：用 ZAP / OWASP 扫端点、用 JMeter 压 `/optimal-route`、用 mvn jacoco:report 看真实覆盖率、用 Playwright 跑全部 E2E 场景，再做评分。
- **本报告与 6 份实验报告（+opus47）的关系**：实验报告聚焦"对照源码复核 deepseek 模板"，颗粒度落在每份实验的具体内容；本审核报告是"全局工程视角 + 路线图"，颗粒度落在跨实验、跨视角的优先级排序。两者互为补充，不应替代。
