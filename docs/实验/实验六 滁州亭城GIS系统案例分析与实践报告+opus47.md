# 实验六 滁州亭城GIS系统案例分析与实践报告

> 复核版（opus47）：综合三部分实验任务（编号 d20301035106/107/108），与仓库 `TingChengGIS` master 分支实测对齐。修正前一版报告中关于代码量、用户账号、API 端点形态、版本历史等若干不实陈述。

## 实验基本信息

- **实验编号**：d20301035106 / 107 / 108
- **学时分配**：6 学时（2 学时 × 3 次）
- **实验类型**：综合型
- **实验项目**：TingChengGIS（滁州亭城GIS系统）
- **对应课程目标**：课程目标1、2、3

---

# 第一部分：案例分析与系统设计

## 一、滁州亭城 GIS 系统案例分析

### 1.1 项目背景

滁州市位于安徽省东部，拥有丰富的亭文化资源。其中以欧阳修《醉翁亭记》中描写的醉翁亭为代表，与陶然亭、爱晚亭、湖心亭并称中国四大名亭；丰乐亭、洗心亭、琅琊亭等历史名亭遍布滁州。这些建筑既是建筑艺术瑰宝，也是中华传统文化的重要载体。

当前痛点：

- **数据分散**：亭子信息分散在不同部门，缺乏统一空间数据库
- **展示单一**：缺乏可视化 GIS 展示平台
- **文化挖掘不足**：历史文化价值未被充分传播
- **旅游服务欠缺**：缺乏智能化的路线规划

TingChengGIS 项目目标即为以上问题提供综合 WebGIS 解决方案。

### 1.2 建设目标（与仓库实现对齐）

| 维度 | 目标 | 仓库现状 |
|------|------|---------|
| 数据管理 | 建立统一空间数据库，支持亭子/景区/区划/路线 | 11 个实体、11 个 Repository |
| 地图展示 | 多源底图 + 2D/3D 切换 | `index.html` 内含 Leaflet + Cesium，多源底图（高德/OSM/天地图/卫片） |
| 空间分析 | 范围/距离/缓冲区/TSP | `PavilionService` + `GeoUtils` + `TspSolver` |
| AI 服务 | 多 provider AI 解说与推荐 | `AiService` 支持 DeepSeek / Zhipu / OpenAI + 模板降级 |
| 数据交换 | Excel/GeoJSON/CSV | `PavilionImportService` 多格式分发 |
| OGC 标准 | WMS/WFS 代理 | `OgcProxyController` |
| 数据追溯 | 每条数据关联采集者 | 三个 `*Collector` 实体 + `CollectorDataMigration` |

### 1.3 系统架构

```
┌────────────────────────────────────────────────────────────────────┐
│                       表现层（Presentation）                          │
│  index.html SPA：Leaflet 2D | Cesium 3D | Bootstrap 5 | Font Awesome │
├────────────────────────────────────────────────────────────────────┤
│                       业务层（Service / 18 个 Controller）            │
│  PavilionService | ThousandPavilionsService | AiService            │
│  TransportRouteService | PavilionImport/Export | OgcProxy …        │
├────────────────────────────────────────────────────────────────────┤
│                       工具层（Util）                                 │
│  GeoUtils | CoordinateTransform | TspSolver | PavilionTypeUtils    │
├────────────────────────────────────────────────────────────────────┤
│                       数据层（Repository / Entity）                  │
│  JPA Repositories ──► H2 (dev) / PostgreSQL+PostGIS (prod)         │
├────────────────────────────────────────────────────────────────────┤
│                       基础设施层（Infrastructure）                   │
│  Spring Security + JWT  |  RoutingClient → OSRM                    │
│  RestTemplate → DeepSeek / Zhipu / OpenAI                          │
│  OgcProxyController → 外部 WMS / WFS                                │
└────────────────────────────────────────────────────────────────────┘
```

### 1.4 技术选型（来自 `pom.xml` 与 `application.yml` 实测）

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.0 |
| ORM | Spring Data JPA | 随 starter |
| dev DB | H2 | 2.x |
| prod DB | PostgreSQL + PostGIS | 16+ / 3.4+ |
| 空间计算 | JTS | 1.19.0 |
| Excel | Apache POI | 5.2.5 |
| 认证 | JJWT | 0.12.5 + Spring Security |
| 2D 地图 | Leaflet | 1.9.x（CDN） |
| 3D 地球 | Cesium.js | 1.115（CDN） |
| UI | Bootstrap | 5.3 |
| AI provider（默认） | DeepSeek | `deepseek-v4-flash` |
| AI provider（备选） | Zhipu / OpenAI | `glm-4` / `gpt-3.5-turbo` |
| 路网 | OSRM 公共服务 | `router.project-osrm.org` |
| AI 辅助开发 | Claude Code / OpenCode CLI | – |

> 项目**未引入** Lombok、JaCoCo；这两项是后续维护建议（详见实验五报告 opus47 版）。

---

# 第二部分：核心功能编码实现

## 二、项目实现总览

### 2.1 文件统计（实测）

```
TingChengGIS/
├── src/main/java/                       (100 个 Java 源文件)
│   ├── ai/                              ( 2)
│   ├── config/                          ( 4)
│   ├── controller/                      (18)
│   ├── dto/                             ( 3)
│   ├── entity/                          (11)
│   ├── exception/                       ( 3)
│   ├── repository/                      (11)
│   ├── security/                        ( 3)
│   ├── service/                         (15 接口 + 15 实现 + 数据类若干)
│   ├── util/                            ( 4)
│   └── PostgresTest.java                (顶级文件，PG 连接验证)
├── src/test/java/                       (26 个测试类，279 个 @Test)
├── src/main/resources/
│   ├── application.yml
│   └── static/
│       ├── index.html                   (单文件 SPA)
│       ├── share.html
│       └── api-test.html
├── data/
│   ├── 千亭.xlsx                        (228 条原始亭子数据，Excel)
│   └── gifs/                            (路线动画产物)
├── docs/                                (实验报告)
└── pom.xml
```

### 2.2 核心控制器与端点（节选，按实测）

#### 亭子管理 `/pavilions`（PavilionController）

| 方法 | 路径 | 用途 |
|------|------|------|
| POST | `/pavilions` | 创建（body 是 Pavilion） |
| GET | `/pavilions/{id}` | 单查 |
| GET | `/pavilions?page=&size=&sort=` | 分页 |
| PUT | `/pavilions/{id}` | 更新 |
| DELETE | `/pavilions/{id}` | 删除 |
| GET | `/pavilions/type/{pavilionType}` | 类型查询 |
| GET | `/pavilions/search?name=` | 名称模糊 |
| GET | `/pavilions/by-year-range?startYear=&endYear=` | 年代区间 |
| GET | `/pavilions/popular?minRating=` | 热门 |
| **POST** | **`/pavilions/geographic-search`** | **body `{"wktText":"..."}`**（不是 4 个 Double） |
| GET | `/pavilions/stats` | 统计 |
| POST | `/pavilions/recommendations?userId=&preferences=` | AI 推荐 |

> 全部接口返回 `{success: bool, message?, data, count?}` 统一结构。

#### 千亭遍历 `/thousand-pavilions`（ThousandPavilionsController）

`/locations`、`/route/{from}/{to}`、`/multimedia/{id}`、`/traverse-all`、`/optimal-route`、`/smart-tour`、`/tourism-services`、`/share-route`（POST）、`/weather`、`/nearby-facilities/{id}`、`/vr-experience/{id}`、`/import`（multipart 导入）等。

#### 交通路线 `/transport-routes`（TransportRouteController）

含 `/build-network` 与 `/build-multi-modal`（仅 ADMIN 可调用）。

#### AI 服务 `/ai`（AiController + PavilionAIController）

- `GET /ai/culture-intro?pavilionName=&location=`
- `GET /ai/historical-story?pavilionName=&constructionYear=`
- `POST /ai/ask`
- `POST /ai/tourism-advice`
- `GET /ai/culture-overview`

> 注：`AiController` 实测的 `culture-intro` 接受 `pavilionName` 与 `location` 两个 query 参数，前一版报告写成 `pavilionId` 是不准确的。

#### 用户认证 `/auth`（AuthController）

- `POST /auth/login` body `{username, password}` → `{success, token, username, displayName, role}`
- `POST /auth/register` body `{username, password, displayName}`
- `GET /auth/me`（需 Bearer token）

#### OGC 代理 `/ogc`（OgcProxyController）

- `POST /ogc/wms/capabilities` body `{url}` → 解析后的 JSON
- `POST /ogc/wfs/capabilities`

### 2.3 核心算法实现要点

#### Haversine 距离（GeoUtils）

```java
double R = 6371000;
double dLat = Math.toRadians(lat2 - lat1);
double dLon = Math.toRadians(lon2 - lon1);
double a = Math.sin(dLat/2)*Math.sin(dLat/2)
         + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
         * Math.sin(dLon/2)*Math.sin(dLon/2);
double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
return R * c;   // 米
```

#### TSP 改进（TspSolver）

```java
public final class TspSolver {
    public static int[] improveCyclic(int[] tour, double[][] dist) { ... }   // 闭环
    public static int[] improveOpen  (int[] tour, double[][] dist) { ... }   // 开放路径
}
```

> **没有** `solveTwoOpt(matrix)`。前一版报告的方法名错误。

#### 坐标转换（CoordinateTransform）

```java
public static double[] wgs84ToGcj02(double lng, double lat) {
    if (outOfChina(lng, lat)) return new double[]{lng, lat};   // 境外原值
    // ... Krasovsky 椭球参数 + 火星算法
}

public static double[] gcj02ToWgs84(double lng, double lat) {
    if (outOfChina(lng, lat)) return new double[]{lng, lat};
    double[] wgs = {lng, lat};
    for (int i = 0; i < 5; i++) {                 // 5 轮迭代逼近
        double[] gcj = wgs84ToGcj02(wgs[0], wgs[1]);
        wgs[0] += lng - gcj[0];
        wgs[1] += lat - gcj[1];
    }
    return wgs;
}
```

#### AI 多 provider 路由（AiService.init）

```java
switch (activeProvider) {
    case "deepseek" -> { resolvedApiUrl = deepseekApiUrl; ... }
    case "zhipu"    -> { resolvedApiUrl = zhipuApiUrl;    ... }
    default          -> { resolvedApiUrl = openaiApiUrl;  ... }
}
aiAvailable = (resolvedApiKey 非空 且 不是占位符 "sk-your-key-here");
// chat() 等方法在 aiAvailable=false 或调用异常时回退到中文模板
```

### 2.4 前端关键技术

```javascript
// 高德/腾讯/卫片底图启用 GCJ-02
function needGcj() {
  return ['gaode','tencent','weipian'].includes(currentMapProvider);
}

// 把 (lng,lat,WGS-84) → Leaflet 显示坐标 [lat,lng]，根据当前底图按需偏移
function leafCoord(lng, lat) {
  if (needGcj()) {
    const g = wgs84ToGcj02(lng, lat);    // 与后端 CoordinateTransform 同算法
    return [g.lat, g.lng];
  }
  return [lat, lng];
}
```

---

# 第三部分：测试验证、部署与项目交付

## 三、测试验证

### 3.1 测试规模（实测）

```bash
$ grep -rc "@Test" src/test
# 各文件相加：Total @Test = 279
```

```bash
$ mvn -q test
[INFO] Tests run: 279, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 3.2 各层测试方法分布

| 层级 | 文件数 | @Test 数 |
|------|--------|---------|
| controller/* | 8 | 86（3+3+14+10+8+16+29+3） |
| service/impl/* | 8 | 多个（具体见仓库） |
| service/ 其它 | 4 | 21（4+7+5+5） |
| ai/ | 1 | 17 |
| repository/ | 1 | 11 |
| util/ | 4 | 29（4+4+15+6） |

> 仓库最近一次 commit 信息为 "175 unit/integration tests"（2026-05-25），现已增长至 279。

### 3.3 关键路径覆盖矩阵（无 JaCoCo 时的替代）

| 关键功能 | 主要测试 |
|---------|---------|
| 亭子 CRUD | `PavilionControllerTest` + `PavilionServiceImplTest` + `PavilionRepositoryTest` |
| WKT 范围查询 | `PavilionControllerTest.findByGeographicRange` |
| 千亭 TSP | `ThousandPavilionsServiceImplTest` + `TspSolverTest` |
| AI 多 provider + 降级 | `AiServiceTest`（17 用例） |
| 坐标转换 | `CoordinateTransformTest`（4 用例，含中国境外原值返回） |
| 距离/方位 | `GeoUtilsTest` |
| 类型规范化 | `PavilionTypeUtilsTest`（15 用例覆盖各种别名） |
| 用户认证 | `AuthControllerTest` |
| 路径计划存储 | `RoutePlanControllerTest` |

### 3.4 打包与部署

```bash
mvn clean package -DskipTests
# 产物：target/tingchenggis-1.0.0.jar

java -jar target/tingchenggis-1.0.0.jar
# 启动后访问：
#   主页:        http://localhost:8092
#   H2 控制台:   http://localhost:8092/h2-console
#   健康检查:    http://localhost:8092/actuator/health
```

#### 默认登录账号（仓库 `DataInitializer.seedUsers` 实测）

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员（ADMIN） | `419116` | `419116` |
| 注册用户（USER） | `206004` | `206004` |

> 前一版报告中的 `admin/admin` 不存在。

#### 首次启动数据导入提示

`DataInitializer.seedPavilionsHint` 会日志输出：

> 当前无亭子数据，请通过 POST /thousand-pavilions/import 导入 228 条数据
> 文件: data/千亭.xlsx

实际仓库 `data/` 目录下文件名为 `千亭.xlsx`（与日志提示文件名略有差异，需要根据实际文件名调整）。

---

## 四、项目交付物

| 类别 | 交付物 | 说明 |
|------|--------|------|
| 源代码 | 全部 Java + HTML 源码 | 100 Java + 1 主 HTML + 配置 |
| 可运行包 | `tingchenggis-1.0.0.jar` | Spring Boot fat jar |
| 测试结果 | 279 个 @Test 全部通过 | `mvn test` 输出 |
| 设计文档 | UML 模型 + 6 份实验报告（opus47） | `docs/实验/` |
| 配置文件 | `application.yml` | dev/prod 双 profile |
| 项目指南 | `CLAUDE.md` | 给 AI 助手的项目说明 |
| 数据 | `data/千亭.xlsx`（228 条） | Excel 原始数据 |
| 前端 | `index.html`、`share.html`、`api-test.html` | SPA + 分享页 + API 测试页 |

---

## 五、技术亮点

| 亮点 | 实现 |
|------|------|
| 双坐标系无感支持 | DB 双列存储 + 前端 `needGcj()` 自动选用偏移 |
| 多 provider AI + 模板降级 | `AiService.init` 切换 + `templateResponse` 回退 |
| WKT 文本作为几何统一格式 | 兼容 H2 与 PostGIS，迁移成本低 |
| TSP 改进算法 | `improveCyclic`（闭环）+ `improveOpen`（开放） |
| 多模态交通 | 步行/骑行/驾车 OSRM 路网 |
| 三表采集追溯 | `Pavilion/ScenicArea/AdminDivision` × `*Collector` 一对多 |
| OGC 代理转发 | `OgcProxyController` 解决前端跨域 |
| Excel/GeoJSON/CSV 多格式导入 | 按扩展名自动分发 |
| 2D/3D 双视图 | Leaflet ↔ Cesium，CSS 切换 |
| Vibe Coding | Claude Code 主导 + OpenCode/TRAE 辅助 |

---

## 六、答辩常见问题与作答要点

### 6.1 技术问题

- **Q：为什么用 WKT 文本而不是 PostGIS 几何列？**
  A：当前要求开发用 H2、生产用 PostGIS，WKT 文本是两者通用的存储格式，省去自定义类型映射的复杂度；代价是查询要写 `BETWEEN` 而不是 `ST_DWithin`，但在 228 条数据规模下性能足够。下一版（v1.2）计划迁移到 PostGIS 几何列以解锁空间索引。

- **Q：为什么要存 GCJ-02 双列而不是前端实时算？**
  A：避免每次查询都做坐标转换的 CPU 开销；同时降低前端误用风险（一旦弄错底图，错误是用户可见的）。代价是写入路径必须保证两列同步——已通过 `CoordinateTransform.wgs84ToGcj02` 集中处理。

- **Q：AI key 写死在 `application.yml` 安全吗？**
  A：当前是教学项目权宜之计；v1.1 已规划抽离到环境变量。

### 6.2 设计问题

- **Q：18 个控制器是否过多？**
  A：按业务边界划分：亭子（基础 CRUD + GIS）、千亭（智能游览/TSP）、景区、行政区划、交通路线、路径计划、AI、OGC 代理、OSM 导入、坐标管理、POI、旅游路线、游记、VR/AR、文件上传、首页转发、用户认证、采集者管理。每个控制器对应一组高内聚的业务功能。

- **Q：Service 接口与 Service 实现分离的必要性？**
  A：便于在测试中用 `@MockBean` 替换；便于将来接入不同实现（例如把内存 TSP 换成 OR-Tools）。

### 6.3 团队问题

- **Q：分工与协作？**
  A：项目经理负责进度与风险；2 名后端各 owner 一组 Service；前端 1 人维护 SPA；GIS 工程师负责坐标系/TSP/OSRM；测试工程师 owner 测试用例。Git 协作以 PR + Code Review 为主，建议补建 develop 分支策略。

---

## 七、实验总结与思考

### 7.1 实验总结

1. **案例分析**：对照仓库梳理了 18 个控制器、15 个 Service、11 个实体的真实分层
2. **核心编码**：实现了完整后端（含 AI 多 provider、TSP、OSRM 集成、OGC 代理）和单文件 SPA
3. **测试验证**：279 个测试全部通过；明确了无 JaCoCo 情况下的关键路径覆盖矩阵
4. **数据导入**：通过 `POST /thousand-pavilions/import` 加载 `data/千亭.xlsx`
5. **AI 辅助开发**：Claude Code 主导对仓库扫描、文档生成、报告复核；OpenCode/TRAE 辅助行级补全

### 7.2 与 deepseek 版本的主要修正

| 原报告 | 实际 |
|--------|------|
| 默认账号 admin/admin | 实际为 `419116/419116`、`206004/206004` |
| `/api/...` 前缀 | 实际无 `/api` 前缀，直接 `/pavilions` 等 |
| `geographic-search` 接受 `{minLng, maxLng, minLat, maxLat}` | 接受 `{"wktText":"..."}` |
| `TspSolver.solveTwoOpt(matrix)` | 实际是 `improveCyclic` / `improveOpen` |
| AI `culture-intro?pavilionId=` | 实际 `?pavilionName=&location=` |
| Pavilion 实体含 `getFullAddress()` | 不存在；`Pavilion` 仅 getter/setter |
| 项目使用 Lombok `@Data` | 仓库未引入 Lombok |
| JaCoCo 88% 指令覆盖率 | 仓库未引入 JaCoCo，无覆盖率数据 |
| git 历史含 v1.0/v1.1 多版本 | 仅 2 次 commit，仍为 v1.0.0 |
| 已有 develop / feature / hotfix 分支 | 当前只有 master |
| 提交信息中"279 个测试" | 提交信息是 "175 unit/integration tests"，279 是当前实测 |

### 7.3 课后思考

1. **GIS 项目的关键成功因素**：
   - 坐标系规范是隐式契约，必须在需求阶段固定
   - 空间索引设计直接影响查询体验
   - 测试用例必须涵盖空间精度（含中国境外回退、边界点）

2. **子项目集成**：本项目以 RESTful API + WKT 数据格式为统一边界。8 类子项目（TextGIS/AudioGIS/VideoGIS 等）均可通过此接口接入。

3. **技术选型权衡**：
   - H2 为开发提速，但 prod 必须切回 PostGIS
   - Leaflet 适合 2D 主场景，Cesium 满足 3D 体验但加载较慢
   - 多 provider AI 提供冗余但增加配置复杂度

4. **AI 辅助开发的实际收益**：Claude Code 显著提升了"对照源码做需求/设计/测试一致性审查"的效率——这次报告复核就识别了 deepseek 版本中十余处与代码不符之处。

5. **未来方向**：
   - 安全清理（API key 剥离）
   - 引入 JaCoCo 给覆盖率画底线
   - PostGIS 几何列迁移工具
   - OSRM 私有化 + 缓存
   - 移动端 PWA 适配
