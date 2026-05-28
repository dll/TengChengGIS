# 实验二 EA UML软件建模与空间数据结构设计报告

> 复核版（opus47）：依据仓库 `TingChengGIS` 实测代码，修正前一版报告中实体字段、关系和 Repository 方法签名上的若干偏差。

## 实验基本信息

- **实验编号**：d20301035102
- **学时分配**：2学时
- **实验类型**：设计型
- **实验项目**：TingChengGIS（滁州亭城GIS系统）
- **对应课程目标**：课程目标2

---

## 一、实验目的

1. 掌握 EA UML 软件的安装配置与基本操作
2. 掌握面向对象分析方法（用例图、类图、时序图、包图）
3. 完成 TingChengGIS 系统的空间数据库设计
4. 理解 PostGIS 空间数据类型和 OGC 标准

---

## 二、面向对象设计

### 2.1 用例图设计

#### 2.1.1 参与者识别

| 参与者 | 描述 | 主要操作 |
|--------|------|---------|
| 匿名访客 | 未登录用户 | 浏览地图、查询亭子、AI 问答（受 `SecurityConfig` 白名单控制） |
| 注册用户（USER） | 已登录普通用户，种子账号 `206004` | 全部 GET 接口 + 写操作（POST/PUT/DELETE） |
| 管理员（ADMIN） | 管理员账号 `419116` | 上述权限 + 路网重建、坐标批量校正、OSM 导入 |
| 数据采集员 | 在采集记录中可识别的用户 | 通过采集者追溯字段关联到亭子/景区/区划 |
| 外部 AI 服务 | DeepSeek/Zhipu/OpenAI | 由 `AiService` 通过 RESTful HTTP 调用 |
| 外部 OSRM | `router.project-osrm.org` | 由 `RoutingClient` 调用 |
| 外部 OGC 服务 | WMS/WFS/WMTS 服务方 | 由 `OgcProxyController` 代理转发 |

#### 2.1.2 核心用例清单

**普通用户/访客用例：**

| 编号 | 用例名称 | 关联控制器 |
|------|---------|-----------|
| UC-01 | 浏览多源底图 | 前端 `index.html` |
| UC-02 | 名称模糊搜索亭子 | `PavilionController.findByNameContaining` |
| UC-03 | 查看亭子详情 | `PavilionController.getPavilionById` |
| UC-04 | WKT 范围查询 | `PavilionController.findByGeographicRange`（POST 体 `wktText`） |
| UC-05 | 距离计算 | `PavilionService.calculateDistance(id1, id2)` |
| UC-06 | 千亭 TSP 最优路线 | `ThousandPavilionsService.getOptimalTraversalRoute()` |
| UC-07 | AI 文化解说 | `AiController.cultureIntro` / `historicalStory` |
| UC-08 | VR 体验数据 | `VrArController` |
| UC-09 | 2D/3D 视图切换 | 前端 Leaflet ↔ Cesium |
| UC-10 | Excel/GeoJSON/CSV 导入导出 | `PavilionImportService` / `PavilionExportService` |

**管理员附加用例：**

| 编号 | 用例 | 关联端点 |
|------|------|---------|
| UC-11 | 亭子 CRUD | `PavilionController` |
| UC-12 | 景区 CRUD | `ScenicAreaController` |
| UC-13 | 区划 CRUD | `AdminDivisionController` |
| UC-14 | 路网构建 | `POST /transport-routes/build-network`（仅 ADMIN） |
| UC-15 | 多模态路网构建 | `POST /transport-routes/build-multi-modal`（仅 ADMIN） |
| UC-16 | 坐标批量校正 | `POST /coordinate/correct-pavilions`（仅 ADMIN） |
| UC-17 | OSM 数据导入 | `/osm/import/**`（仅 ADMIN） |
| UC-18 | 用户管理 | `AppUserService.register / ensureUser` |

**用例关系：**

- `UC-04 范围查询` <<include>> `UC-01 浏览地图`
- `UC-06 TSP 最优路线` <<include>> `UC-05 距离计算`
- `UC-07 AI 文化解说` <<extend>> `UC-03 查看亭子详情`

### 2.2 类图设计

> 以下字段以仓库实体类源码为准，删去前一版报告中虚构的 `getFullAddress()` 等方法。

#### 2.2.1 Pavilion（亭子，`entity/Pavilion.java`）

```
┌──────────────────────────────────────┐
│            Pavilion                  │
├──────────────────────────────────────┤
│ - id: Long  (PK, IDENTITY)           │
│ - name: String  (NOT NULL)           │
│ - chineseName: String                │
│ - description: String (length=2000)  │
│ - historicalSignificance: String     │
│ - constructionPeriod: String         │
│ - architecturalStyle: String         │
│ - geomWkt: String (TEXT)             │
│ - longitude / latitude: Double       │
│ - longitudeGcj / latitudeGcj: Double │
│ - pavilionType: String               │
│ - areaSize: Double                   │
│ - structure / topStyle / street: String │
│ - notes: String (length=2000)        │
│ - locationDesc: String               │
│ - visitorRating: Double              │
│ - isOpenToPublic: Boolean            │
│ - ticketPrice: Double                │
│ - builtYear / lastRenovationYear: Integer │
│ - createdAt / updatedAt: LocalDateTime │
├──────────────────────────────────────┤
│ + getter/setter (手写，无 Lombok)     │
│ + @PreUpdate preUpdate()             │
└──────────────────────────────────────┘
```

> 实测要点：实体没有 `@PrePersist`，`createdAt` 由两个构造器手动赋值；`@PreUpdate preUpdate()` 仅刷新 `updatedAt`。

#### 2.2.2 ScenicArea（景区）

```
- id, name, chineseName, description
- areaType, areaSize
- geomWkt, boundaryWkt   (两套几何：中心点+边界)
- longitude, latitude
- address, openingHours, ticketPrice, visitorRating
- isOpenToPublic, notes
- createdAt, updatedAt
```

#### 2.2.3 AdminDivision（行政区划，自引用树）

```
- id, name, chineseName
- adminLevel: Integer    (1=省,2=市,3=区县…)
- parentId: Long         (自引用)
- parentName: String
- geomWkt, longitude, latitude
- areaSize, population, adminCode
```

#### 2.2.4 TransportRoute（交通路线/边）

```
- id, routeName, routeType
- fromPavilionId, toPavilionId
- distanceKm, travelTimeMinutes
- routeDescription (TEXT)
- isAccessible, isScenicRoute
- roadType, waypoints (TEXT, 途经点JSON)
- elevationGain
- transportMode  (WALK / BIKE / DRIVE / BUS)
- roadLevel, trafficCondition, estimatedFare
- geomWkt
```

#### 2.2.5 RoutePlan（TSP 计划结果存储）

```
- id, planName
- transportMode, objective    (DISTANCE / DURATION / COST)
- visitOrderIds (TEXT)        (访问顺序的亭子 ID 列表)
- visitOrderNames (TEXT)
- planJson (TEXT)             (完整方案 JSON)
- totalDistance, totalDuration, totalFare, totalTicket, totalCost
- pavilionCount
- gifPath                     (动画产物路径)
```

#### 2.2.6 其他实体

| 实体 | 关键字段 |
|------|---------|
| `TourismRoute` | id, routeName, scenicStops, distanceKm, geomWkt 等共 11 字段 |
| `TravelLog` | id, title, content (TEXT), location, routeId, scenicId, photoUrl, rating, author |
| `AppUser` | id, username, passwordHash, role (`ADMIN`/`USER`), displayName, createdAt |
| `PavilionCollector` / `ScenicAreaCollector` / `AdminDivisionCollector` | 三表结构一致：collectorUserId、collectorName、collectionTime、collectionTool、accuracy、dataSource |

#### 2.2.7 关系图

```
Pavilion 1 ──── *  PavilionCollector
ScenicArea 1 ── *  ScenicAreaCollector
AdminDivision 1 ── * AdminDivisionCollector
AdminDivision * ── 1 AdminDivision     (parentId 自引用)

TransportRoute * ── 2 Pavilion          (fromPavilionId / toPavilionId)
TourismRoute  * ── *  ScenicArea         (scenicStops 列表存储)

TravelLog * ── 1 TourismRoute            (routeId)
TravelLog * ── 1 ScenicArea              (scenicId)
```

### 2.3 时序图设计

#### 2.3.1 亭子模糊搜索

```
用户 → 前端: 输入"醉翁"
前端 → PavilionController: GET /pavilions/search?name=醉翁
PavilionController → PavilionService: findByNameContaining("醉翁")
PavilionService → PavilionRepository: findByNameContainingIgnoreCase("醉翁")
PavilionRepository → DB: SELECT * FROM pavilions WHERE LOWER(name) LIKE '%醉翁%'
DB → PavilionRepository: 行集
PavilionRepository → PavilionService: List<Pavilion>
PavilionService → PavilionController: List<Pavilion>
PavilionController → 前端: { success, data, count } JSON
前端 → Leaflet: 在地图上标注结果
```

#### 2.3.2 WKT 范围查询

```
用户 → 前端: 矩形框选 → 生成 WKT POLYGON
前端 → PavilionController: POST /pavilions/geographic-search
       Body: { "wktText": "POLYGON((118.3 32.2, 118.4 32.2, ...))" }
PavilionController → PavilionService: findByGeographicRange(wktText)
PavilionService → PavilionRepository: 解析 WKT 边界 → 调 findByGeographicRange(minLng,maxLng,minLat,maxLat)
PavilionRepository → DB: WHERE longitude BETWEEN ? AND ? AND latitude BETWEEN ? AND ?
DB → ... → 前端
前端 → Leaflet: 渲染要素
```

> 注：`PavilionController` 接收的是 `wktText` 字符串；`PavilionRepository.findByGeographicRange` 是带 4 个 Double 参数的底层方法。两层命名近似但参数形态不同——前一版报告把这两层混淆了。

#### 2.3.3 TSP 千亭遍历

```
用户 → 前端: 选择亭子集合，点击"最优路线"
前端 → ThousandPavilionsController: GET /thousand-pavilions/optimal-route
Controller → ThousandPavilionsService: getOptimalTraversalRoute()
Service → PavilionRepository: 取全部坐标
Service → 自身: 构建距离矩阵 dist[][]
Service → TspSolver: improveCyclic(tour, dist)   (2-opt 优化)
TspSolver → Service: 优化后的访问顺序
Service → Controller: List<Long> orderedIds
Controller → 前端: JSON
前端 → Leaflet: 路线动画绘制
```

> 实测：`util/TspSolver.java` 提供两个静态方法 `improveCyclic`（含闭环回到起点）和 `improveOpen`（开放路径）。**没有** `solveTwoOpt` 方法——前一版报告这一项是误写。

### 2.4 包图设计

```
com.tingchenggis.tingcheng/
├── controller/   18 个 *Controller
├── service/      接口层 + service/impl/ 实现层
├── repository/   11 个 *Repository
├── entity/       11 个实体类
├── dto/          PavilionDto, CoordinateDTO, PavilionImportResult
├── ai/           AiService, PavilionAIController
├── security/     SecurityConfig, JwtUtil, JwtAuthFilter
├── config/       AppConfig, WebConfig, DataInitializer, CollectorDataMigration
├── util/         GeoUtils, CoordinateTransform, PavilionTypeUtils, TspSolver
├── exception/    BusinessException, NotFoundException, GlobalExceptionHandler
└── TingChengGISTingChengApplication.java
```

依赖方向：`controller → service → repository → entity`；`service` 横向依赖 `util`、`ai`、`config`；`security` 拦截 `controller` 入口。

---

## 三、空间数据库设计

### 3.1 概念模型与实体映射

| UML 类 | DB 表名 | 备注 |
|--------|---------|------|
| Pavilion | `pavilions` | 主体表 |
| PavilionCollector | `pavilion_collectors` | 采集追溯 |
| ScenicArea | `scenic_areas` | |
| ScenicAreaCollector | `scenic_area_collectors` | |
| AdminDivision | `admin_divisions` | 自引用层级 |
| AdminDivisionCollector | `admin_division_collectors` | |
| TransportRoute | `transport_routes` | 边表 |
| TourismRoute | `tourism_routes` | 含 scenicStops |
| RoutePlan | `route_plans` | TSP 计算结果存储 |
| TravelLog | `travel_logs` | 游记 |
| AppUser | `app_users` | 用户 |

### 3.2 关键表结构

#### 3.2.1 pavilions

| 字段 | 类型 | 约束 | 来源 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | `@GeneratedValue` |
| name | VARCHAR(255) | NOT NULL | |
| chinese_name | VARCHAR(255) | | |
| description | TEXT | length=2000 | |
| historical_significance | TEXT | length=2000 | |
| construction_period | VARCHAR | | |
| architectural_style | VARCHAR | | |
| pavilion_type | VARCHAR | | HISTORICAL/MODERN/CULTURAL |
| geom_wkt | TEXT | | WKT 文本（兼容 H2 与 PostGIS）|
| longitude / latitude | DOUBLE | | WGS-84 |
| longitude_gcj / latitude_gcj | DOUBLE | | GCJ-02，由 `CoordinateTransform` 计算 |
| area_size | DOUBLE | | |
| structure / top_style / street | VARCHAR | | 凉亭调查字段 |
| notes | TEXT | length=2000 | |
| location_desc | VARCHAR | | |
| visitor_rating | DOUBLE | | 1-5 |
| is_open_to_public | BOOLEAN | | |
| ticket_price | DOUBLE | | |
| built_year / last_renovation_year | INTEGER | | |
| created_at / updated_at | TIMESTAMP | | |

> 关键设计：**几何字段以 WKT 文本存储**，不使用 `GEOMETRY(Point,4326)` 列类型。这是为兼容 H2（dev）与 PostGIS（prod）双环境，在仓库 `Pavilion.java` 注释中亦有说明。

#### 3.2.2 transport_routes

字段 18 个，关键字段：`from_pavilion_id`、`to_pavilion_id` 作为外键引用 `pavilions(id)`；`geom_wkt` 存储 LINESTRING；`waypoints` 为 TEXT 列存储途经点 JSON。

#### 3.2.3 索引策略

```sql
-- H2/PostgreSQL 通用索引（建议）
CREATE INDEX idx_pavilions_type ON pavilions(pavilion_type);
CREATE INDEX idx_pavilions_year ON pavilions(built_year);
CREATE INDEX idx_pavilions_rating ON pavilions(visitor_rating);
CREATE INDEX idx_admin_parent ON admin_divisions(parent_id);
CREATE INDEX idx_transport_mode ON transport_routes(transport_mode);

-- 仅 PostGIS（生产环境，需先把 geom_wkt 反序列化为 geom 列）
CREATE INDEX idx_pavilions_geom ON pavilions USING GIST(geom);
```

> 当前项目 `ddl-auto: create-drop`（dev）/ `update`（prod），上述索引未在实体注解中声明，需在生产部署阶段单独执行。

### 3.3 空间查询示例

#### 矩形范围查询（实际仓库实现）

```sql
SELECT * FROM pavilions
 WHERE longitude BETWEEN :minLng AND :maxLng
   AND latitude  BETWEEN :minLat AND :maxLat;
```

对应 `PavilionRepository.findByGeographicRange(minLng,maxLng,minLat,maxLat)`。

#### Haversine 距离（应用层实现，`util/GeoUtils.java`）

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

#### 升级到 PostGIS 时的等价查询

```sql
SELECT * FROM pavilions
 WHERE ST_DWithin(
   ST_SetSRID(ST_MakePoint(longitude, latitude), 4326),
   ST_SetSRID(ST_MakePoint(:lng, :lat), 4326),
   :radiusMeters
 );
```

### 3.4 OGC 标准适配

| OGC 标准 | 项目实现 |
|---------|---------|
| WMS GetCapabilities/GetMap | `OgcProxyController.wmsCapabilities` 等代理转发 |
| WFS GetCapabilities/GetFeature | `OgcProxyController.wfsCapabilities` |
| WMTS | 前端 `index.html` 中预设天地图 WMTS URL |
| WKT | 实体 `geomWkt` 字段（统一用文本）|
| GeoJSON | `PavilionImportService` 接收 `.geojson`，`PavilionExportService` 输出 GeoJSON |

### 3.5 坐标系统设计

| 坐标系 | 用途 | 转换函数（`util/CoordinateTransform.java`） |
|--------|------|-------------------------------------------|
| WGS-84 (EPSG:4326) | DB 存储、PostGIS 计算、OSM 底图 | `wgs84ToGcj02(lng, lat)` |
| GCJ-02（火星） | 高德/腾讯/卫片底图 | `gcj02ToWgs84(lng, lat)`（迭代逼近，5 轮） |

> **重要事实**：`CoordinateTransform` 的两个公开方法在判断 `outOfChina(lng,lat)` 为真时，**返回入参原值**而非抛异常。前一版报告中"超出范围抛 IllegalArgumentException"是错误的。

---

## 四、总结与思考

### 4.1 实验总结

1. 对照仓库源码完成了 11 个实体类的字段确认与 ER 关系梳理
2. 厘清了"WKT 文本 + 经纬度 Double + GCJ-02 双列"的兼容性设计动机
3. 明确了 `TspSolver` 的真实方法签名（`improveCyclic` / `improveOpen`），并区分了 `controller.findByGeographicRange(wktText)` 与 `repository.findByGeographicRange(4参数)` 两层语义
4. 设计了从 H2 到 PostGIS 的查询升级路径（`BETWEEN` → `ST_DWithin`）

### 4.2 与 deepseek 版本的主要修正

| 原报告 | 实际情况 |
|--------|---------|
| `Pavilion` 含 `getFullAddress() / calculateGcjCoordinates()` 方法 | 实体类无此方法，GCJ-02 由外部 `CoordinateTransform.wgs84ToGcj02` 计算并通过 setter 写入 |
| `TspSolver.solveTwoOpt(matrix)` | 方法不存在，实际为 `improveCyclic` / `improveOpen` |
| `Repository.findByGeographicRange` 在 Controller 中直接接受 4 个 Double 参数 | Controller 接受 POST 体 `{"wktText": "..."}`，由 Service 层解析后再调 4 参数 Repository 方法 |
| 实体使用 `@PrePersist` + `@PreUpdate` 双重生命周期注解 | 仅 `@PreUpdate`；`createdAt` 在构造器内手动赋值 |
| 项目使用 Lombok `@Data` | `pom.xml` 未引入 Lombok；getter/setter 全部手写 |

### 4.3 课后思考

1. **空间数据库与普通数据库**：空间数据库的"范围查询""邻近查询"在普通数据库需要拼复合索引甚至空间填充曲线（如 GeoHash）才能近似达到。本项目以 `BETWEEN` 在小数据集（228 条）下能满足，但要扩展到万级数据应迁移 PostGIS。

2. **OGC 标准的价值**：当前以 `OgcProxyController` 代理三方 WMS/WFS，与本系统数据是分离的。下一步如果要把本系统亭子层暴露为 WMS/WFS，需要引入 GeoServer 或 GeoTools。

3. **坐标系混淆**是 GIS 项目最常见的隐式错误。本项目用"双列 + 转换函数"对前端透明，但代价是写入路径必须保证两列同步——这是将来要重点测试的不变量。
