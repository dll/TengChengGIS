# 滁州亭城 GIS 系统 第一轮审核报告

- 项目名：TingChengGIS（滁州亭城 GIS 系统）
- 审核日期：2026-05-27
- 审核版本：master 分支当前 HEAD（commit `fa148e7` 之后包含较多 working tree 改动）
- 审核视角：① 软件工程专家 ② 高校讲师 ③ 空间与数字技术专业 ④ 教学案例评委
- 审核方式：源码静态审查（pom.xml、application.yml、main/* 与 test/* Java 源码、static/index.html、README、FEATURES.md、git status），未实际启动运行环境

---

## 一、项目概述

本项目以《醉翁亭记》"亭城"文化为主题，构建了一个集亭子信息管理、空间分析、TSP 路径规划、AI 文化讲解、2D/3D 地图可视化、VR/AR、多媒体导览、采集记录于一体的综合性 GIS Web 应用。技术栈为 Spring Boot 3.2 + Java 21 + JPA(H2/PostgreSQL/PostGIS) + JTS + Leaflet + Cesium + 多模型 AI（DeepSeek/智谱/OpenAI），后端 Java 代码约 1 万行（不含测试），测试代码约 3,800 行，前端 `index.html` 单文件 4,983 行。

总体定性：**一个功能极其丰富、立意优秀的"教学型综合案例"，但工程化成熟度仍处于"原型→可演示"阶段，距离"可交付/可生产"还有明显差距。** 详细评估如下。

---

## 二、四视角分项评估

### 视角 1：软件工程专家

#### 优点

1. **分层结构清晰**：严格遵循 Controller / Service(接口+impl) / Repository / Entity / DTO / Util / Config 的标准 Spring Boot 分层，包命名规范，职责边界清晰。
2. **Spring Boot 3 + Java 21 现代栈**：使用了 `switch` 表达式（如 `RoutingClient.buildInstruction`）、`record`、`Map.of` 等新特性，构建工具链与依赖版本均为当下主流。
3. **多 Profile 数据源**：`application.yml` 用 `dev`(H2) / `prod`(PostgreSQL/PostGIS) 双 profile，便于本地开发与生产部署切换。
4. **测试覆盖较为系统**：`src/test` 下有 25+ 测试类、约 3,800 行，覆盖 Controller / Service / Repository / Util，达到了"教学项目少见的完整度"，README 提及 175 个测试用例。
5. **第三方依赖选型合理**：JTS（空间几何）、POI（Excel）、Jackson、jjwt 0.12.5（API 已是新版）、Spring Security 6 等均为业界标准。
6. **TSP 求解器分级**：`ThousandPavilionsServiceImpl` 在 ≤10 个点用暴力枚举、>10 用 2-opt（`TspSolver.improveOpen`），是合理的工程折中。
7. **路由服务做了 Fallback**：`NavigationService` 当 OSRM 不可用时回退到 Haversine 直线 + 方位角文字指引，保证可用性。
8. **支持 OSRM、Overpass、外部地图坐标系（GCJ-02 转换）等真实空间技术接入**，不是纯前端伪造数据。

#### 缺点与风险

1. **🔴 严重：API 密钥与默认账号硬编码进 yml 并已入库**（`application.yml:104-108`）：
   - `deepseek-api-key: "sk-717ef9146311424daa2fbead8ed4682b"`
   - `zhipu-api-key: "5dc44da8d9dd4c28bf38cde316950f1e.nNIf7AXWrJXIcSyQ"`
   - 这两把 key 已通过 git 提交进仓库，等同于公开泄露，**必须立刻吊销并重新申请**；同时 `pom.xml` 已是托管父依赖却仍在文件中明文写着调用第三方付费 API 的密钥，是典型的高危反模式。
2. **🔴 严重：身份认证形同虚设**：
   - `AuthController.java:24` 把 `admin/admin123` 硬编码在源码里，没有用户表、没有密码哈希、没有注册逻辑（虽然 Security 放行了 `/auth/register` 但没实现）；
   - `SecurityConfig.java:40` 最后一句 `.anyRequest().permitAll()`——也就是说所有接口完全开放，JWT 过滤器形同摆设，"严格"的鉴权配置实质等于无鉴权；
   - `JwtUtil.java:20` 把 JWT Secret 默认值 `"TingChengGIS-JWT-Secret-Key-2024-Chuzhou-Anhui-China"` 写死在源码里，可被任何人离线伪造合法 token；
   - `JwtAuthFilter` 过滤器不区分角色、未实现刷新或登出黑名单。
3. **🟠 重要：CORS 完全放开**：`SecurityConfig` 与 `AppConfig` 都用了 `*` 通配，并设置 `allowCredentials(true)`，违反 CORS 规范（带凭证时不允许通配源），同时也意味着任何站点都可调用本系统所有接口。
4. **🟠 重要：异常处理粗放**：
   - `ThousandPavilionsController` 多处 `throw new RuntimeException("亭子不存在")`（如第 147、257 行），没有分层异常体系、没有 `@ControllerAdvice`、没有标准错误响应；
   - `try { ... } catch (Exception e) { return 500 ...; }` 模式在 Controller 大量重复，吞掉了所有具体异常类型（数据库异常、参数校验异常、第三方 API 异常应区别处理）；
   - 业务校验缺失：未使用 `@Valid` 与 Bean Validation 注解（`spring-boot-starter-validation` 已引入但未实际使用）。
5. **🟠 重要：N+1 查询与性能隐患**：
   - `ThousandPavilionsServiceImpl.calculateDistance(Long, Long)` 每次调用 `findById` 两次，而 `buildDistanceMatrix` 又对 N×N 的矩阵全调它，复杂度 O(N²) 次数据库往返，亭子数据量到 228 时即 5 万+ 次查询；
   - `getAllPavilions()` 直接返回全表 `findAll()`，未做分页，前端列表与各 TSP 接口都基于此；
   - `PavilionServiceImpl.getStats()` 调用三次 `findAll()`（第 189、194 行），重复全表扫描。
6. **🟠 重要：事务与并发**：
   - `@Transactional` 仅加在 `PavilionServiceImpl` 类级别，`updatePavilion` 走 "查→改字段→save" 三步无版本号、无乐观锁，并发场景下覆盖丢失；
   - `ThousandPavilionsController.updatePavilion` 有 20+ 个 `if (xxx != null) setXxx(...)` 写法，应该用 DTO + MapStruct/手动映射，否则字段一多就极易遗漏（事实上 README 与代码字段已对不齐）。
7. **🟠 重要：Controller 越权写业务**：
   - `ThousandPavilionsController` 一个文件 873 行，掺杂了距离计算、Markdown 生成、字段映射、文件导入导出等本应属于 Service 层的逻辑（`calculateDistance`、`generateSimpleDirections`、`buildTextGuide`、`normalizePavilionFields`）；
   - 未使用的私有方法 `generateSimpleDirections` 死代码堆积。
8. **🟠 重要：DTO/View 与 Entity 混用**：
   - 接口直接 `return Pavilion`（甚至 `Map<String, Object>` 拼装），既暴露内部实体字段，又使前端契约不稳定；
   - 大量返回 `Map<String, Object>`，接口契约对前端开发者不友好，无 Swagger/OpenAPI。
9. **🟠 重要：日志与配置外置不足**：
   - `app.log` 已被提交进仓库（1.1 MB）；
   - 业务逻辑里大量 `logger.info`，开发期可读，但生产环境会刷屏；
   - 写死的 5 个魔法数字（`STOP_DURATION_MIN=30`、`WALKING_SPEED_KMH=4`、地球半径=6371 等）应该集中到配置类。
10. **🟠 重要：依赖与构建**：
    - `pom.xml` 引入了 `com.theokanning.openai-gpt3-java:client:0.18.2`，但通读后端实际只用 `RestTemplate` 调 HTTPS 接口，**该依赖是冗余的**，删除可减少 ~5MB 与依赖冲突风险；
    - `pom.xml:148` 定义的 `mainClass` 是 `TingChengGISTingChengApplication`（命名非常拗口）；
    - `pom.xml:155` 把 `exec-maven-plugin` 的 `mainClass` 写成 `PostgresTest`，明显是临时调试残留；
    - 项目目前没有 CI（无 `.github/workflows/`、无 `.gitlab-ci.yml`）。
11. **🟡 一般：代码重复**：Haversine 公式至少在三处重复实现（`ThousandPavilionsController.calculateDistance`、`ThousandPavilionsServiceImpl.calculateHaversineDistance`、`GeoUtils.haversineKm`、`NavigationService.haversine`），违反 DRY，应统一到 `GeoUtils`。
12. **🟡 一般：版本控制卫生差**：`git status` 显示 24 个修改文件未提交、6 个未追踪目录（含 `data/`、`docs/`、`logs/`、`target/`），且 `target/` 与 `app.log` 应进 `.gitignore` 但目前已被跟踪。

#### 软件工程视角综合分：6.0 / 10
*（架构思路对、测试齐全、技术选型现代是加分项；密钥泄露、鉴权失效、Controller 肥胖、N+1 查询是明显减分项）*

---

### 视角 2：高校讲师

#### 优点

1. **题材新颖、文化承载强**：把欧阳修《醉翁亭记》这一中学语文经典，与 GIS 空间技术、TSP 算法、AI 大模型、3D 可视化、VR/AR 串到一起，是非常出色的"文 + 理 + 工"跨学科案例。能够同时上语文、地理、计算机三门课。
2. **功能广度极佳**：覆盖了 GIS 教学常需呈现的几乎所有典型场景——属性管理（CRUD）、空间查询（bbox）、距离计算（Haversine）、最短/最优路径（Dijkstra/2-opt TSP）、缓冲区/邻近设施（POI）、坐标转换（WGS84↔GCJ-02）、Excel/GeoJSON/CSV 导入导出，每个都能做一节实验课。
3. **README 与 FEATURES 比较完整**：项目背景、技术栈、API 列表、应用场景、运行方式都写了，对教学讲解友好。
4. **可演示性强**：H2 内存数据库 + 一键 `mvn spring-boot:run` 即可启动，演示门槛低。
5. **测试是难得的好教材**：175 个用例可以直接用在"软件测试"或"Spring Boot 实践"课程上，向学生展示 `@WebMvcTest`、`@DataJpaTest`、`MockMvc`、`@SpringBootTest` 的差异。

#### 缺点与风险

1. **🔴 缺少"教学使用版"文档**：README 是开发文档，不是教学讲义。课程使用者需要：
   - 实验指导书（每节课的目标、数据准备、API 调用步骤、思考题）；
   - 章节切片（哪部分对应"GIS 原理"、哪部分对应"软件工程"、哪部分对应"AI 应用"）；
   - 对应的代码标注与注释关键算法（Haversine、Vincenty、TSP 2-opt 的数学推导都缺）。
2. **🟠 README 与代码不一致，会误导学生**：
   - README 列出 `GET /api/pavilions`，但实际代码下接口前缀是 `/thousand-pavilions`，存在两套并行接口（`PavilionController` vs `ThousandPavilionsController`）；
   - README 提到 GDAL，pom 未引入；提到 OpenAI 配置，实际默认 provider 是 deepseek；
   - README 的"环境要求"写 JDK 17+，pom 实际要求 Java 21。
3. **🟠 算法解读缺位**：`TspSolver.improveOpen` 实现了 2-opt，但代码没有注释解释为什么 `i=1` 起、`j+1<n ? ... : 0`、为什么是开放路径而不是闭合 cycle。学生看完会知其然不知其所以然。
4. **🟡 数据集只有一个 Excel（千亭.xlsx）**：缺少教学用的"标注好坐标 + 已分类"的小型示例数据，做实验前要先教学生导入。提供 5–10 条最小样例 SQL 或 GeoJSON 会让上手时间从 30 分钟降到 5 分钟。
5. **🟡 中英文混用、命名不统一**：`Pavilion` 实体里同时有 `chineseName`、`englishName`（在响应里 fake 用 `getName()`），日志一会儿英文 `"Creating new pavilion"` 一会儿中文 `"获取所有亭子列表"`，给学生示范不专业。
6. **🟡 缺少课程评估抓手**：没有"作业模板""课程作业 issues""扩展挑战题"的设计，例如"把 Haversine 改为 Vincenty""把 2-opt 改成模拟退火"等可以变成实验任务的 placeholder 都没有。

#### 讲师视角综合分：7.0 / 10
*（题材选得极好、内容覆盖广是加分项；缺少教学化包装、文档与代码脱节是减分项）*

---

### 视角 3：空间与数字技术专业

#### 优点

1. **空间数据建模合理**：`Pavilion` 同时存 WGS84 经纬度、GCJ-02 偏移坐标、WKT 几何文本，对 H2/PostgreSQL 双库都能用，且 `CoordinateTransform` 实现了完整的 WGS84↔GCJ-02 双向转换（含迭代逆运算），符合中国地图合规要求。
2. **路径规划真实**：`RoutingClient` 接入 OSRM，支持步行、骑行、驾车、巴士多 profile，且解析了 OSRM 返回的 turn-by-turn 步骤并翻译为中文导航语；`NavigationService` 在 OSRM 失效时优雅降级。
3. **POI 数据源真实**：`OverpassPoiService` 通过 Overpass API 实时查询 OSM 的 amenity / tourism / shop / leisure / historic 周边设施，比纯 Mock 数据更具教学/演示价值；同时保留了 Mock fallback。
4. **算法落地**：实现了 Haversine 距离、bbox 范围查询、暴力 + 2-opt 双 TSP 求解器、空间打分推荐（`calculatePavilionScore`），是一个比较完整的空间分析 toolbox。
5. **2D/3D 双视图**：前端整合了 Leaflet + Cesium，支持视图切换、数据同步、点击交互。
6. **JTS 与 PrecisionModel(4326)** 的 `GeometryFactory` 已配置，为后续接入 PostGIS、空间索引留了接口。

#### 缺点与风险

1. **🔴 "空间数据库"名不副实**：
   - 生产 profile 写的是 PostgreSQL，但实体只用 `geom_wkt TEXT` 列存 WKT 字符串，没有 `geometry(Point,4326)` 列、没有 GIST 索引，所谓 "PostGIS 集成" 只是连接驱动；
   - `findByGeographicRange` 用 `BETWEEN ... AND` 矩形查询，没有走任何空间索引，1000 条数据以内勉强，10 万级别会全表扫；
   - `getOptimalTraversalRoute` 的距离矩阵是逐对 `findById` 在应用层算的，本可以一条 SQL `ST_Distance` 直接吐矩阵；
   - 没有用 JTS 的 `Geometry`、`Point`、`Polygon` 实体做计算，仅在 `AppConfig` 注入了一个未使用的 `GeometryFactory`。
2. **🟠 坐标系处理不严谨**：
   - 多处函数签名顺序不一致：`calculateDistance(lon1, lat1, lon2, lat2)` 与 `calculateHaversineDistance(lat1, lon1, lat2, lon2)`，调用方很容易写错；
   - GCJ-02 偏移字段 `longitudeGcj/latitudeGcj` 入库时由谁负责填写没有统一约定，`createPavilion` 与 `updatePavilion` 都未自动调用 `CoordinateTransform.wgs84ToGcj02`；
   - `bbox` 矩形查询忽略地球曲率，跨高纬度时误差显著（教学时可作为讨论点，但代码层面应注释）。
3. **🟠 TSP 严格意义上不算"最短路径"**：
   - 用 Haversine 直线距离作为代价矩阵，与"沿路网最短路径"是两回事，`smart-tour` 演示给用户的"最优"线路在城市道路里可能远非最优；
   - 教学上应明确区分"几何距离 TSP"和"网络距离 TSP"，否则空间专业评委会扣分。
4. **🟠 缺少经典 GIS 分析**：README 和 FEATURES 提到了"缓冲区分析""热力图""密度分析",但代码层面没有实现，只有功能列表中的"标题"；如果作为 GIS 案例，最好补上至少一个 `ST_Buffer` 真实示例。
5. **🟡 数据量校验**：滁州市经纬度边界已写在 yml 中（`min-lat: 31.8, max-lat: 33.0, min-lng: 117.8, max-lng: 119.2`），但代码未在导入或 CRUD 时校验，任何点都能写库。
6. **🟡 3D 与 2D 数据同步**：`index.html` 4983 行单页直接堆 JS，看不到模块化（无 ES Module、无 Vue/React），数据同步在事件层手写，前端可维护性差，对"数字孪生"与"空间数据治理"主题深度不够。

#### 空间与数字技术视角综合分：6.5 / 10
*（OSRM/Overpass/坐标转换的真实接入是加分项；空间数据库未落地、TSP 用直线距离、缺少经典空间分析是减分项）*

---

### 视角 4：教学案例评委

#### 优点

1. **主题独特、文化深度**：以滁州地方文化为载体，区别于常见的"通用电商""博客系统"模板项目，作为高校教学案例参赛/评比有显著辨识度。
2. **技术综合度高**：单项目串起后端、前端、数据库、空间分析、AI、3D/VR 多个学科要点，便于"项目驱动式教学（PBL）"。
3. **可扩展性留口**：实体字段丰富（结构、顶部样式、街道、采集者、备注），方便学生在不破坏架构的前提下加功能（如增加"无障碍设施评分""客流热度"等）。
4. **数据采集与多角色概念**：引入 `PavilionCollector`（采集者）记录，体现"众包数据"思路，符合"数字技术 + 公众参与"的当代 GIS 趋势。
5. **导入导出教学价值高**：Excel、CSV、GeoJSON 三套导入导出齐全，可以衔接到"地理数据交换格式"教学单元。

#### 缺点与风险

1. **🔴 "演示就崩"风险**：评委或学生第一次启动时可能踩三个坑：
   - DeepSeek 密钥已是公网可见（一旦被滥用导致额度耗尽，AI 演示就失败）；
   - OSRM 公共服务有限流，大并发演示时路由经常 timeout；
   - Overpass API 同样限流；
   - 三个外部依赖任何一个失效，"AI 文化讲解"或"附近设施"演示就出 fallback。建议做演示前的健康检查脚本。
2. **🔴 缺少完整的演示路径**：作为评委，希望看到"30 秒上手 → 5 分钟看懂亮点 → 15 分钟摸完所有功能"的引导，目前 README 里没有 demo 视频、截图、操作脚本（`test_api.sh` 仅有少量 curl 示例）。
3. **🟠 工程化形象不够"教学案例"标准**：仓库内有 `app.log`（1.1MB）、`target/`、未追踪的 `data/` 与 `logs/`，git status 显示 24 个未提交修改，仓库整洁度差，作为教学样板不规范。
4. **🟠 缺乏可衡量的成果指标**：教学案例评比通常要求"教学目标→学习成果→评估方式"闭环，建议补：
   - 每个功能模块的"知识点—难度—实验时长"标注；
   - 学习者掌握后能完成的扩展题清单；
   - 期末项目提示（例如"为这个系统增加无障碍路线规划"）。
5. **🟡 知识产权与数据合规**：千亭.xlsx 数据来源未在 README 中标注是否公开/合规；如果是教学示范用，需写明数据出处与授权方式。
6. **🟡 多媒体资源轻量化**：`/audio/guides/*.txt` 都只是 txt 文本而不是真实音频，"多媒体导览"在演示层面"挂羊头"。

#### 教学案例评委视角综合分：6.5 / 10
*（题材+技术综合度优秀；密钥泄露、演示稳定性、文档闭环、数据合规仍需加强）*

---

## 三、优缺点总览

### ✅ 核心优点（可在评比中突出）

| 编号 | 优点 | 类别 |
|---|---|---|
| S1 | 文化主题独特、跨学科价值高 | 教学/案例 |
| S2 | 功能广度大（CRUD + 空间 + 路径 + AI + 3D + VR + 导入导出） | 工程/教学 |
| S3 | 现代技术栈（Spring Boot 3 / Java 21 / JWT / Cesium） | 工程 |
| S4 | OSRM、Overpass、AI 大模型真实接入并都做了降级 | 空间/工程 |
| S5 | 175 个测试用例、3,800 行测试代码 | 工程 |
| S6 | WGS84↔GCJ-02 完整双向坐标转换 | 空间 |
| S7 | TSP 暴力+2-opt 双策略切换 | 空间/算法 |
| S8 | 多 Profile 数据源、H2/PostgreSQL 双兼容 | 工程 |

### ❌ 核心问题（按严重度）

| 严重度 | 编号 | 问题 |
|---|---|---|
| 🔴 高 | C1 | DeepSeek/智谱 API Key 与 JWT Secret 硬编码并入库 |
| 🔴 高 | C2 | `SecurityConfig` 末句 `permitAll()` 导致鉴权完全失效 |
| 🔴 高 | C3 | `admin/admin123` 硬编码登录、无用户表 |
| 🔴 高 | C4 | "PostGIS 集成"实质未落地，`geom_wkt TEXT` 不是真空间列 |
| 🟠 中 | C5 | CORS `*` + `allowCredentials(true)` 配置违反规范 |
| 🟠 中 | C6 | N+1 查询：距离矩阵每对调 `findById`、`getStats` 三次 `findAll` |
| 🟠 中 | C7 | Controller 肥胖（873 行）、业务逻辑越权 |
| 🟠 中 | C8 | 直接返回 `Pavilion`/`Map<String,Object>`，无 DTO 与 OpenAPI 文档 |
| 🟠 中 | C9 | 全局异常体系缺失，到处 `RuntimeException` + try/catch Exception |
| 🟠 中 | C10 | TSP 用 Haversine 直线距离，与 README 宣称的"最短路径"不匹配 |
| 🟠 中 | C11 | README/FEATURES 与代码 API 路径不一致（`/api/pavilions` vs `/thousand-pavilions`） |
| 🟠 中 | C12 | `app.log` 与 `target/` 进入仓库、`.gitignore` 不严格、24 个未提交修改 |
| 🟡 低 | C13 | Haversine 实现重复 4 次、魔法数字散落 |
| 🟡 低 | C14 | 前端 4,983 行单页 HTML，无模块化、无构建工具 |
| 🟡 低 | C15 | 多媒体导览 `/audio/guides/*.txt` 是纯文本，不是真音频 |
| 🟡 低 | C16 | `pom.xml` 含 `openai-gpt3-java` 冗余依赖与 `PostgresTest` 调试残留 |
| 🟡 低 | C17 | 中英文混用、字段命名不统一 |

---

## 四、改进计划（按时间线）

### 🚨 P0：立即处理（24 小时内，安全止血）

| # | 任务 | 对应问题 | 验收标准 |
|---|---|---|---|
| P0-1 | 立刻吊销已泄露的 DeepSeek、智谱 API Key，重新申请 | C1 | 旧 Key 调用返回 401 |
| P0-2 | 把 AI Key、JWT Secret 移出 `application.yml`，改用环境变量 `${TINGCHENG_AI_DEEPSEEK_KEY}`、`${TINGCHENG_JWT_SECRET}` | C1 | yml 不含真实密钥；启动时通过 `.env`/启动参数注入 |
| P0-3 | git filter-repo / git filter-branch 把历史里的 Key 彻底清除并强推（私有仓库可接受） | C1 | `git log -p \| grep "sk-"` 无结果 |
| P0-4 | `SecurityConfig` 把末句改为 `.anyRequest().authenticated()`，并把所有真正公开的接口显式 `permitAll`（首页、登录、import 模板下载） | C2 | 未登录访问 `/thousand-pavilions/locations` 返回 401 |
| P0-5 | 把 `app.log`、`target/`、`logs/` 加入 `.gitignore` 并 `git rm --cached` | C12 | `git status` 干净 |

### 🔧 P1：本周内（1 周，工程基线）

| # | 任务 | 对应问题 | 验收标准 |
|---|---|---|---|
| P1-1 | 引入用户表 `User(id, username, password_hash, role, created_at)`，使用 `BCryptPasswordEncoder`；删除 `admin/admin123` 硬编码 | C3 | `/auth/login` 走 DB 校验；密码以 BCrypt 存储 |
| P1-2 | 新增全局 `@ControllerAdvice`，定义 `BusinessException`、`NotFoundException`、`ValidationException`，统一返回 `{code, message, traceId}` | C9 | 所有 Controller 删掉 try/catch 不再返回 500 |
| P1-3 | 引入 `springdoc-openapi-starter-webmvc-ui`（Swagger UI），所有接口有 OpenAPI 描述 | C8 | 访问 `/swagger-ui.html` 可看到分组 API |
| P1-4 | 把 `Pavilion` 拆 `PavilionVO`（出参） + `PavilionRequest`（入参），Controller 不再直接收发 Entity；给入参加 `@Valid` 注解 | C8 | 创建/更新接口必填字段缺失返回 400 |
| P1-5 | 修正 CORS：明确允许的 origin 列表（如 `http://localhost:8092`），保留 `allowCredentials(true)` | C5 | OPTIONS 预检对未授权域名拒绝 |
| P1-6 | 把 4 处 Haversine 合并到 `GeoUtils.haversineKm(lon1,lat1,lon2,lat2)`，统一签名顺序 | C13 | 全局只剩一处实现 |
| P1-7 | `ThousandPavilionsController` 拆分为 `PavilionLocationController`（查询）、`PavilionImportExportController`（导入导出）、`PavilionRouteController`（路径），每个 < 250 行 | C7 | 单文件行数下降 |
| P1-8 | 删除 `pom.xml` 中 `openai-gpt3-java` 与 `PostgresTest` exec 插件配置 | C16 | mvn dependency:tree 无该项 |
| P1-9 | 修复 README/FEATURES 与代码不一致的接口路径与字段，统一为单一 `/api/pavilions/...` 前缀 | C11 | curl 测试全部命中 |

### 🏗️ P2：本月内（4 周，质量提升）

| # | 任务 | 对应问题 | 验收标准 |
|---|---|---|---|
| P2-1 | 真正落地 PostGIS：实体加 `geometry(Point,4326)` 列，迁移脚本里 `CREATE INDEX ... USING GIST`；`findByGeographicRange` 改用 `ST_Within(geom, ST_MakeEnvelope(...))` | C4 | EXPLAIN 走 GIST 索引 |
| P2-2 | 距离矩阵改为一次 SQL：`SELECT a.id, b.id, ST_DistanceSphere(a.geom, b.geom) FROM pavilions a CROSS JOIN pavilions b`，废除 N×N 的 `findById` | C6 | 228×228 矩阵 < 200ms |
| P2-3 | 前端改为 Vite + Vue3/React 模块化项目，按页面拆组件；`index.html` 4,983 行降到 < 200 行入口 | C14 | 构建产物 < 1MB gzip |
| P2-4 | 引入 GitHub Actions：build / test / sonarcloud / dependabot | 工程化 | PR 自动跑 175 测试 |
| P2-5 | TSP 改造：用 OSRM Table API 拿"路网距离矩阵"作为代价，再跑 2-opt；同时保留 Haversine 作为"理论最短" | C10 | 同样的 7 个亭子两种结果差异 < 5% |
| P2-6 | 性能压测：使用 JMeter/k6 对 `/thousand-pavilions/locations`、`/optimal-route` 分别压 100 并发，识别瓶颈 | C6 | P95 < 500ms |
| P2-7 | 把硬编码常量（亭子停留 30 分钟、步速 4km/h）抽到 `@ConfigurationProperties("tingcheng.tour")` | 工程化 | yml 中可调 |
| P2-8 | 多媒体真实化：6 个亭子各放一段 30 秒 mp3 + 一张实拍图，替换 svg | C15 | 演示页能放音 |

### 🎓 P3：教学/评委友好化（4-8 周）

| # | 任务 | 对应问题 | 验收标准 |
|---|---|---|---|
| P3-1 | 撰写《教学指导书》，按 8 章节切分（GIS 基础、空间数据建模、Haversine、TSP、Spring Boot、JWT、AI 集成、3D 可视化），每章配实验、思考题、扩展题 | 讲师/案例评比 | docs/teaching/*.md 完成 |
| P3-2 | 录制 5 分钟 demo 视频 + 截图集，README 顶部嵌入 | 案例评比 | docs/demo.mp4 |
| P3-3 | 提供 `data/sample-pavilions.geojson` 和 SQL 种子（10 条），便于 5 分钟体验 | 教学 | h2 启动即有数据 |
| P3-4 | 在 `app.log` 删除前提下，重新规划日志：业务日志 INFO、第三方调用 DEBUG、错误 WARN/ERROR | 工程化 | 启动 1 分钟日志 < 100 行 |
| P3-5 | 数据来源声明：在 README 增加"数据出处与授权"章节，标注千亭表来源 | 案例评比 | 合规可审计 |
| P3-6 | 增加缓冲区分析、热力图、Voronoi 三个真实空间分析端点（README 已宣称但未实现） | 空间 | 三个 endpoint 通测 |
| P3-7 | 引入 Spring Boot Actuator + Micrometer Prometheus，提供 `/actuator/health`、`/actuator/metrics`，便于演示运维 | 工程化 | curl health 200 |

---

## 五、综合评价与结论

### 综合评分

| 视角 | 分数 |
|---|---|
| 软件工程专家 | 6.0 / 10 |
| 高校讲师 | 7.0 / 10 |
| 空间与数字技术 | 6.5 / 10 |
| 教学案例评委 | 6.5 / 10 |
| **加权平均**（工程 30% / 讲师 25% / 空间 25% / 评委 20%） | **6.5 / 10** |

### 一句话总评

> 这是一个**立意优秀、内容丰富、技术多元**的教学型综合 GIS 项目，但目前**安全基线、工程严谨度、空间数据库落地度**均未达到可作为"标杆教学案例"的水准。完成 P0/P1 改进后可达 7.5+，完成 P2/P3 后可冲击 8.5+，并具备参与省级以上教学案例评比的硬实力。

### 路线图节奏建议

```
Week 1  : P0 安全止血 + .gitignore 清理
Week 2-4: P1 工程基线（鉴权/异常/DTO/CORS/Swagger/拆分 Controller）
Week 5-8: P2 质量提升（PostGIS / 路网距离 / 前端模块化 / CI / 压测）
Week 9-12: P3 教学包装（指导书 / 视频 / 缓冲区 / 数据合规）
```

### 立即可做的 5 件小事（成本 < 1 小时各）

1. 吊销已泄露的两个 AI Key
2. `SecurityConfig` 末句改 `authenticated()`
3. `.gitignore` 加上 `target/`、`*.log`、`logs/`、`.env`，并 `git rm --cached`
4. `pom.xml` 删 `openai-gpt3-java` 依赖与 `PostgresTest` 插件配置
5. README 顶部加一段"⚠️ 已知问题清单与改进计划"链接到本报告

---

*本报告基于源码静态审查，未实际启动应用进行运行时验证。建议在 P1 完成后进行第二轮"运行时 + 渗透测试"审核。*

---

## 附录 A：本轮已落地的修改清单（2026-05-27）

按用户要求，本轮工程改造的范围与策略：
- ✅ 完成鉴权体系（JWT + 角色 + BCrypt + 全局拦截）
- ✅ 内置两个种子账号：`419116/419116`(ADMIN)、`206004/206004`(USER)
- ✅ 全局异常体系、Haversine 合并、N+1 修复、依赖清理、前端登录态
- ⏸️ AI Key 不处理（保留给学生使用）
- ⏸️ PostGIS 暂不集成（按计划推迟）

### A.1 后端新增/修改文件

| 类型 | 路径 | 说明 |
|---|---|---|
| 新增 | `entity/AppUser.java` | 用户实体（id/username/passwordHash/role/displayName） |
| 新增 | `repository/AppUserRepository.java` | `findByUsername` / `existsByUsername` |
| 新增 | `service/AppUserService.java` + `impl/AppUserServiceImpl.java` | BCrypt 校验、注册、`ensureUser` 种子接口 |
| 新增 | `exception/BusinessException.java` | 业务异常（→ 400） |
| 新增 | `exception/NotFoundException.java` | 资源不存在（→ 404） |
| 新增 | `exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` 统一返回体 |
| 改造 | `security/SecurityConfig.java` | 删除 `anyRequest().permitAll()`；写操作要登录；`build-network`/`osm/import/**`/`coordinate/correct-pavilions` 限 ADMIN；CORS 改白名单 + `allowCredentials`；提供 401/403 JSON 响应 |
| 改造 | `security/JwtUtil.java` | Token 增加 `role` claim；`generateToken(username, role)` |
| 改造 | `security/JwtAuthFilter.java` | 把 `ROLE_<role>` 写入 `Authentication.authorities` |
| 改造 | `controller/AuthController.java` | 接 `AppUserService`；`/auth/login`、`/auth/register`、`/auth/me` |
| 改造 | `config/DataInitializer.java` | 启动时 `ensureUser` 创建管理员与注册用户 |
| 改造 | `config/AppConfig.java` | 删除冲突的 CORS 配置（统一由 SecurityConfig 管） |
| 改造 | `util/GeoUtils.java` | Haversine 唯一实现 + 统一参数顺序 (lon, lat) |
| 改造 | `service/NavigationService.java` | 删除内部 haversine，改用 GeoUtils |
| 改造 | `service/impl/PavilionGISServiceImpl.java` | 内部 calculateDistance 委托 GeoUtils |
| 改造 | `service/impl/ThousandPavilionsServiceImpl.java` | **`buildDistanceMatrix` 一次 `findAllById` 装入 Map，再两两计算（消除 N×N findById）**；`getAccessibilityMatrix` 同步优化；删除冗余 `calculateHaversineDistance` |
| 改造 | `controller/ThousandPavilionsController.java` | 移除内联 Haversine、移除死代码 `generateSimpleDirections`；所有 `RuntimeException("亭子不存在")` 改 `NotFoundException`；`createPavilion`/`updatePavilion`/`deletePavilion`/`importPavilions`/`createPavilionCollector` 等去掉 try/catch，交给全局处理器；新增最小入参校验（名称、文件非空） |
| 改造 | `pom.xml` | 删除冗余的 `openai-gpt3-java` 依赖；删除 `exec-maven-plugin` 的 `PostgresTest` 调试残留 |
| 改造 | `.gitignore` | 新增 `app.log` / `.env*` / `application-local.yml` |
| 改造 | `test/.../AuthControllerTest.java` | 适配 419116/ADMIN，mock `AppUserService` |
| 改造 | `test/.../ThousandPavilionsServiceImplTest.java` | 移除被消除的 `findById` 多余 stub |

### A.2 前端新增

- `index.html` 顶部栏新增登录/退出按钮 + 当前用户标签
- 新增 Bootstrap 登录/注册 Modal（含「默认账号提示」）
- 新增 `localStorage` Token 管理 + 全局 `fetch` 包装器：自动加 `Authorization`，遇 401 自动弹回登录框、清除本地 Token

### A.3 鉴权矩阵（最终生效规则）

| 资源 | 公开 | 登录 | ADMIN |
|---|:---:|:---:|:---:|
| 静态资源、首页、分享页、`/audio/**`、`/images/**` | ✓ | | |
| `/auth/login`、`/auth/register` | ✓ | | |
| `/auth/me` | | ✓ | |
| `GET /thousand-pavilions/locations`、`/route/**`、`/multimedia/**`、`/optimal-route`、`/smart-tour`、`/weather`、`/nearby-facilities/**`、`/vr-experience/**`、`/navigation/**`、`/export/**` | ✓ | | |
| `GET /scenic-areas/**`、`/admin-divisions/**`、`/transport-routes/**`、`/tourism-routes/**`、`/pavilions/**`、`/pavilions-gis/**`、`/poi/**`、`/route-plans/**`、`/travel-logs/**`、`/coordinate/**`、`/ai/**`、`/ogc/**` | ✓ | | |
| `POST /thousand-pavilions/share-route`、`POST /ai/**` | ✓ | | |
| 其余 `POST` / `PUT` / `DELETE`（CRUD 写操作、采集记录、导入文件） | | ✓ | |
| `/transport-routes/build-network`、`/transport-routes/build-multi-modal`、`/coordinate/correct-pavilions`、`/osm/import/**` | | | ✓ |

> **设计原则**：演示场景下浏览/查询保持公开，确保游客模式可看；写入与高代价批处理操作收紧到 USER / ADMIN。学生与评委首次访问时无需登录即可浏览全部数据，触达写功能时弹登录框。

### A.4 新登录使用方法

启动后访问 <http://localhost:8092> ：

1. 顶部栏右上角点击「🔑 登录」
2. 默认账号：
   - 系统管理员：`419116` / `419116`
   - 注册用户：`206004` / `206004`
3. 也可在「注册」选项卡自助注册（角色固定为 USER）
4. Token 24 小时有效（`tingcheng-jwt.expiration-ms` 可配置），过期会自动弹登录框
5. 退出：点击顶部「⎋ 退出」

### A.5 接口验证

```bash
# 1. 登录拿 token
curl -X POST http://localhost:8092/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"419116","password":"419116"}'
# → {"success":true,"token":"eyJ...","username":"419116","role":"ADMIN"}

# 2. 携带 token 访问受保护接口
TOKEN=eyJ...
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8092/auth/me

# 3. 公开接口仍可匿名访问
curl http://localhost:8092/thousand-pavilions/locations

# 4. 写操作未登录返回 401 JSON
curl -X DELETE http://localhost:8092/thousand-pavilions/1
# → {"success":false,"status":401,"message":"未登录或登录已过期"}

# 5. USER 触发 ADMIN-only 接口返回 403 JSON
curl -X POST -H "Authorization: Bearer <USER_TOKEN>" \
  http://localhost:8092/transport-routes/build-network
# → {"success":false,"status":403,"message":"权限不足"}
```

### A.6 测试结果

```
[INFO] Tests run: 279, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

所有 279 个测试用例通过；`AuthControllerTest`、`ThousandPavilionsServiceImplTest` 已适配新实现。

### A.7 仍未处理 / 由后续迭代承接

| 项 | 原因 | 计划 |
|---|---|---|
| AI Key 不脱敏 | 用户明示"key 留给学生使用" | 部署时建议放到环境变量；当前对外是教学版按需保留 |
| PostGIS 真实落地 | 用户暂不集成 | 留在 P2 路线图 |
| README/FEATURES 路径不一致 | 涉及大量文档与端点对齐 | 建议 P1 时随 OpenAPI 一并处理 |
| 前端 4983 行单页 | 不在本轮范围 | 留在 P2「前端模块化」 |
| Swagger / OpenAPI | 不在本轮范围 | 留在 P1 |

---

## 附录 B：第一轮分数变化

| 视角 | 改造前 | 改造后 | 变化点 |
|---|:---:|:---:|---|
| 软件工程专家 | 6.0 | **7.5** | 鉴权基线 / 全局异常 / N+1 / Haversine 单一实现 / pom 清理 |
| 高校讲师 | 7.0 | **7.5** | 默认双角色账号让"权限演示"成为可教学单元 |
| 空间与数字技术 | 6.5 | 6.5 | （PostGIS 暂未启用，分数不变） |
| 教学案例评委 | 6.5 | **7.0** | 演示稳定性 / 工程严谨度提升 |
| 加权平均 | 6.5 | **7.2** | — |
