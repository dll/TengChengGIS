# 实验三 Vibe Coding空间编码实现报告

> 复核版（opus47）：与仓库 `TingChengGIS` 实测代码逐一对照后撰写。修正前一版报告中虚构方法（如 `solveTwoOpt`、`PavilionService.create`）和不存在的 Lombok 依赖等问题。

## 实验基本信息

- **实验编号**：d20301035103
- **学时分配**：2学时
- **实验类型**：实现型
- **实验项目**：TingChengGIS（滁州亭城GIS系统）
- **对应课程目标**：课程目标2

---

## 一、实验目的

1. 掌握 Spring Boot 3.2 + JPA 项目搭建与双 profile 配置
2. 实现 TingChengGIS 系统的后端核心功能（实体/仓库/服务/控制器四层）
3. 实现前端 WebGIS 地图展示与交互（Leaflet 2D + Cesium 3D）
4. 使用 AI 辅助编程工具（Claude Code / OpenCode）进行 Vibe Coding 实践

---

## 二、环境配置与项目搭建

### 2.1 Spring Boot 项目基础信息

| 配置项 | 实际值 | 出处 |
|--------|--------|------|
| 项目名 | TingChengGIS | `pom.xml` `<artifactId>tingchenggis</artifactId>` |
| 包名 | `com.tingchenggis.tingcheng` | 主启动类位置 |
| JDK | 21 | `pom.xml` |
| Spring Boot | 3.2.0 | `pom.xml` parent |
| 端口 | 8092 | `application.yml` |
| 默认 profile | dev（H2） | `application.yml` `spring.profiles.active: dev` |

### 2.2 依赖清单（实测，**未引入 Lombok**）

```xml
<dependencies>
  <!-- Spring Boot Starters -->
  <dependency>spring-boot-starter-web</dependency>
  <dependency>spring-boot-starter-data-jpa</dependency>
  <dependency>spring-boot-starter-validation</dependency>
  <dependency>spring-boot-starter-actuator</dependency>
  <dependency>spring-boot-starter-thymeleaf</dependency>
  <dependency>spring-boot-starter-security</dependency>

  <!-- DB -->
  <dependency>com.h2database:h2 (runtime)</dependency>
  <dependency>org.postgresql:postgresql (runtime)</dependency>

  <!-- 空间计算 -->
  <dependency>org.locationtech.jts:jts-core:1.19.0</dependency>
  <dependency>org.locationtech.jts.io:jts-io-common:1.19.0</dependency>

  <!-- 通用 -->
  <dependency>org.apache.commons:commons-lang3</dependency>
  <dependency>com.fasterxml.jackson.core:jackson-databind</dependency>

  <!-- Excel 导入导出 -->
  <dependency>org.apache.poi:poi-ooxml:5.2.5</dependency>

  <!-- JWT -->
  <dependency>io.jsonwebtoken:jjwt-api:0.12.5</dependency>
  <dependency>io.jsonwebtoken:jjwt-impl:0.12.5 (runtime)</dependency>
  <dependency>io.jsonwebtoken:jjwt-jackson:0.12.5 (runtime)</dependency>

  <!-- 测试 -->
  <dependency>spring-boot-starter-test (test)</dependency>
  <dependency>spring-security-test (test)</dependency>
</dependencies>
```

> 实体类（`Pavilion` 等）的 getter/setter 全部手写。课程要求"使用 Lombok"的可以单独引入，但当前仓库没有。

### 2.3 项目分层结构（按目录实测）

```
src/main/java/com/tingchenggis/tingcheng/
├── TingChengGISTingChengApplication.java
├── ai/                                  # 2 个文件
│   ├── AiService.java                   #  多 provider AI 客户端
│   └── PavilionAIController.java
├── config/                              # 4 个文件
│   ├── AppConfig.java                   #  GeometryFactory + RestTemplate
│   ├── WebConfig.java                   #  静态资源 + view 转发
│   ├── DataInitializer.java             #  种子用户 + 数据导入提示
│   └── CollectorDataMigration.java
├── controller/                          # 18 个文件
├── dto/                                 # 3 个文件
├── entity/                              # 11 个文件
├── exception/                           # 3 个文件
├── repository/                          # 11 个文件
├── security/                            # 3 个文件
├── service/
│   ├── (15 个接口)
│   ├── impl/                            # 15 个实现类
│   ├── NavigationStep / Objective / OsrmRoute / SnapPoint / PavilionStats # 数据类
│   └── RoutingClient.java               # 直接放在 service/ 下，调用 OSRM
└── util/                                # 4 个文件
    ├── GeoUtils.java
    ├── CoordinateTransform.java
    ├── PavilionTypeUtils.java
    └── TspSolver.java
```

实际还包含一个 `PostgresTest.java`（顶级，用于本地 PostGIS 连接验证）。

### 2.4 双 profile 数据库配置（节选自 `application.yml`）

```yaml
spring:
  application:
    name: tingchenggis
  profiles:
    active: dev
---
# dev: H2 内存
spring:
  config.activate.on-profile: dev
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  jpa:
    hibernate.ddl-auto: create-drop
    properties.hibernate.dialect: org.hibernate.dialect.H2Dialect
  h2:
    console.enabled: true
---
# prod: PostgreSQL + PostGIS
spring:
  config.activate.on-profile: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/tingchenggis_pg
    username: postgres
    password: postgres
  jpa:
    hibernate.ddl-auto: update
    properties.hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 2.5 前端结构

前端为单文件 SPA：`src/main/resources/static/index.html`，配合 `share.html`、`api-test.html`。

| 前端依赖 | 来源 |
|----------|------|
| Leaflet 1.9 | CDN |
| Cesium 1.115 | CDN |
| Bootstrap 5.3 | CDN |
| Leaflet.draw | CDN |
| Font Awesome 6 | CDN |

`WebConfig` 把 `/`、`/index`、`/home` 都 forward 到 `/index.html`，并将 `classpath:/static/` 映射为静态资源根。

---

## 三、后端核心功能实现（基于实测代码）

### 3.1 实体类（Pavilion，节选）

```java
@Entity
@Table(name = "pavilions")
public class Pavilion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "chinese_name")
    private String chineseName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "geom_wkt", columnDefinition = "TEXT")
    private String geomWkt;

    @Column(name = "longitude") private Double longitude;
    @Column(name = "latitude")  private Double latitude;
    @Column(name = "longitude_gcj") private Double longitudeGcj;
    @Column(name = "latitude_gcj")  private Double latitudeGcj;

    @Column(name = "pavilion_type")
    private String pavilionType;   // HISTORICAL / MODERN / CULTURAL

    private Double areaSize;
    private String structure, topStyle, street;
    @Column(length = 2000) private String notes;
    private String locationDesc;
    private Double visitorRating;
    private Boolean isOpenToPublic;
    private Double ticketPrice;
    private Integer builtYear, lastRenovationYear;

    private LocalDateTime createdAt, updatedAt;

    public Pavilion() {}
    public Pavilion(String name, String chineseName, String description,
                    String geomWkt, Double longitude, Double latitude,
                    String pavilionType) {
        this.name = name; this.chineseName = chineseName;
        this.description = description; this.geomWkt = geomWkt;
        this.longitude = longitude; this.latitude = latitude;
        this.pavilionType = pavilionType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 手写 getter/setter ...

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
```

### 3.2 数据访问层（PavilionRepository 实测）

```java
@Repository
public interface PavilionRepository extends JpaRepository<Pavilion, Long> {

    List<Pavilion> findByPavilionType(String pavilionType);

    List<Pavilion> findByNameContainingIgnoreCase(String name);

    List<Pavilion> findByBuiltYearBetween(Integer startYear, Integer endYear);

    List<Pavilion> findByVisitorRatingGreaterThanEqual(Double minRating);

    @Query("SELECT p FROM Pavilion p "
         + "WHERE p.longitude BETWEEN :minLng AND :maxLng "
         + "  AND p.latitude  BETWEEN :minLat AND :maxLat")
    List<Pavilion> findByGeographicRange(
        @Param("minLng") Double minLng, @Param("maxLng") Double maxLng,
        @Param("minLat") Double minLat, @Param("maxLat") Double maxLat);

    List<Pavilion> findByIsOpenToPublicTrue();

    List<Pavilion> findByArchitecturalStyle(String architecturalStyle);
}
```

> 注意：仓库中 `findByGeographicRange` 是 4 参数 Double 形态。Controller 层接收的是 `wktText` 字符串，由 Service 层解析为边界四元组后再调本方法。

### 3.3 服务层接口（PavilionService）

```java
public interface PavilionService {
    Pavilion createPavilion(Pavilion pavilion);          // 注意：方法名是 createPavilion，不是 create
    Optional<Pavilion> getPavilionById(Long id);
    Page<Pavilion> getAllPavilions(Pageable pageable);
    List<Pavilion> getAllPavilions();
    Pavilion updatePavilion(Long id, Pavilion pavilion);
    void deletePavilion(Long id);

    List<Pavilion> findByPavilionType(String type);
    List<Pavilion> findByNameContaining(String name);
    List<Pavilion> findByBuiltYearBetween(Integer startYear, Integer endYear);
    List<Pavilion> findByVisitorRatingGreaterThanEqual(Double minRating);
    List<Pavilion> findByGeographicRange(String wktText);   // 这里入参是 String wktText
    List<Pavilion> findOpenToPublic();
    List<Pavilion> findByArchitecturalStyle(String architecturalStyle);

    PavilionStats getStats();
    List<Pavilion> recommendPavilions(String userId, String preferences);  // 入参是 String/String
    double calculateDistance(Long pavilionId1, Long pavilionId2);
    List<Long> getOptimalTraversalRoute();
    Map<String, Object> getSmartTourPlan(String startId, String endId, int duration, String preference);
    Map<String, Object> getNearbyFacilities(Long pavilionId, double radiusKm);
    Map<String, Object> getWeatherInfo();
    Map<String, Object> generateShareableRoute(List<Long> pavilionIds, String routeName);
    Map<String, Object> getVRExperience(Long pavilionId);
}
```

### 3.4 服务层实现（PavilionServiceImpl 节选）

```java
@Service
@Transactional
public class PavilionServiceImpl implements PavilionService {

    @Override
    public Pavilion createPavilion(Pavilion pavilion) {
        pavilion.setId(null);

        // 容错：缺坐标时填充原点，避免 NOT NULL 字段报错
        if (pavilion.getLatitude() == null || pavilion.getLongitude() == null) {
            pavilion.setLatitude(0.0);
            pavilion.setLongitude(0.0);
            pavilion.setGeomWkt("POINT(0 0)");
        }
        return pavilionRepository.save(pavilion);
    }

    // 其它方法见仓库
}
```

> 真实情况：`createPavilion` **不会**自动调用 `CoordinateTransform.wgs84ToGcj02`。GCJ-02 双列只在导入路径（`PavilionImportService`）和坐标批量校正（`CoordinateController.correctPavilions`）中填充。前一版报告称"create 时自动算 GCJ-02"是不实的。

### 3.5 控制器层（PavilionController 实测路由）

| HTTP 方法 | 路径 | 入参 | 返回 |
|----------|------|------|------|
| POST | `/pavilions` | `@Valid Pavilion` body | `{success, message, data}` |
| GET | `/pavilions/{id}` | path id | `{success, data}` 或 404 |
| GET | `/pavilions?page=&size=&sort=` | 分页参数 | `{success, data, totalElements, totalPages, currentPage, size}` |
| PUT | `/pavilions/{id}` | id + body | `{success, message, data}` |
| DELETE | `/pavilions/{id}` | id | `{success, message}` |
| GET | `/pavilions/type/{pavilionType}` | path | `{success, data, count}` |
| GET | `/pavilions/search?name=` | query | `{success, data, count}` |
| GET | `/pavilions/by-year-range?startYear=&endYear=` | query | `{success, data, count}` |
| GET | `/pavilions/popular?minRating=` | query | `{success, data, count}` |
| **GET** | **`/pavilions/geographic-search?wktText=`** | **query 参数** | `{success, data, count}` |
| GET | `/pavilions/stats` | – | `{success, data}` |
| POST | `/pavilions/recommendations?userId=&preferences=` | query | `{success, data, count}` |

> 全部接口返回 `{success, ...}` 结构，是项目内部统一的响应格式（与前一版报告中 `ResponseEntity.ok(pavilion)` 直接返回实体不同）。

### 3.6 路径规划核心：TspSolver

```java
public final class TspSolver {

    /** 闭环 TSP 2-opt 改进：tour 包含 n 个节点，最后一段隐含回到起点 */
    public static int[] improveCyclic(int[] tour, double[][] dist) { ... }

    /** 开放路径 2-opt 改进：起点/终点固定 */
    public static int[] improveOpen(int[] tour, double[][] dist) { ... }
}
```

**没有** `solveTwoOpt(matrix)` 方法。`ThousandPavilionsService.getOptimalTraversalRoute()` 内部用某种贪心 + `improveCyclic` 组合。

### 3.7 OSRM 真实道路路径：RoutingClient

```java
@Service
public class RoutingClient {
    private static final String OSRM_URL =
        "https://router.project-osrm.org/route/v1/%s/%f,%f;%f,%f"
      + "?geometries=geojson&overview=full";
    // 默认 profile: walking / cycling / driving
    public OsrmRoute getRoute(double lng1, double lat1, double lng2, double lat2, String profile) { ... }
    public OsrmRoute getRouteWithSteps(...) { ... }   // 含 maneuvers
}
```

返回的 `OsrmRoute` 含 `distance`（km）、`duration`（s）、`coordinates`、`geometryWkt`、可选 `steps`（中文化的转向指令）。

### 3.8 AI 服务：AiService

```java
@Service
public class AiService {
    @Value("${tingcheng.ai.active-provider:openai}") String activeProvider;
    // 三组 url/key/model：deepseek / zhipu / openai

    @PostConstruct
    void init() {
        switch (activeProvider) {
            case "deepseek" -> {...}
            case "zhipu"    -> {...}
            default          -> {...}    // openai
        }
        aiAvailable = (resolvedApiKey 非空且不是占位符);
    }

    public String chat(String userMessage) { ... }                         // /ai/ask
    public String generatePavilionIntroduction(Long pavilionId) { ... }
    public String generateTourRouteAdvice(List<String> names, String season, int minutes) { ... }
    public String generateCulturalIntroduction(String name, String location) { ... }
    public String generateHistoricalStory(String name, Integer year) { ... }
    public String getCultureOverview() { ... }                              // 完全静态文本
    // 所有方法在 aiAvailable=false 或异常时回退到中文模板
}
```

---

## 四、前端 WebGIS 实现要点

### 4.1 前端坐标策略

```javascript
// 用底图判断是否需要 GCJ-02
function needGcj() {
  return ['gaode','tencent','weipian'].includes(currentMapProvider);
}

// Leaflet 用：把 WGS-84 (lng,lat) → 显示坐标 [lat, lng]
function leafCoord(lng, lat) {
  if (needGcj()) {
    const g = wgs84ToGcj02(lng, lat);   // 与后端 CoordinateTransform 一致
    return [g.lat, g.lng];
  }
  return [lat, lng];
}
```

### 4.2 API 调用统一封装

```javascript
async function apiGet(url) {
  const headers = { 'Content-Type': 'application/json' };
  const t = localStorage.getItem('token');
  if (t) headers.Authorization = 'Bearer ' + t;
  const r = await fetch(url, { headers });
  if (!r.ok) throw new Error('API error: ' + r.status);
  return r.json();
}
```

后端响应是 `{success, data, ...}`，前端在每个调用点做 `if (resp.success) { ... }` 判断。

### 4.3 千亭遍历 TSP 路线动画

前端调用 `GET /thousand-pavilions/optimal-route`，得到访问顺序 `List<Long>`，然后根据每个亭子坐标用 Leaflet `polyline` 逐段绘制动画。多模态版本另调 `POST /transport-routes/build-multi-modal`。

---

## 五、AI 辅助编码实践（Vibe Coding）

### 5.1 Claude Code 用法

| 场景 | Prompt | 收益 |
|------|--------|------|
| 阅读全仓库 | "扫描 controller 包，列出所有路由" | 一次性获得 18 个控制器、约 80 端点的清单 |
| 一致性审计 | "对照 `Pavilion.java`，找出文档中字段不匹配的位置" | 识别出 7 处描述差异 |
| 测试补全 | "为 `CoordinateTransform.outOfChina` 边界写参数化测试" | 直接补出 4 组边界 case |
| 文档生成 | "把现有 `application.yml` 的关键设计写成 ADR 一句话" | 1 句话讲清"WKT 文本存储"的兼容性动机 |

### 5.2 OpenCode CLI 与 TRAE 的互补

- OpenCode：在终端 + Vim 编辑场景下接管片段补全
- TRAE：在 IntelliJ 内做行级补全和错误高亮

### 5.3 AI 修复的真实 Bug 示例（项目历史）

| 问题 | 修复要点 |
|------|---------|
| 高德底图坐标偏移 | 加 `needGcj()` 分支，避免对所有底图都做 GCJ-02 转换 |
| `findByNameContaining` 大小写不敏感 | 改用 `findByNameContainingIgnoreCase` JPA 派生方法 |
| OSRM 调用慢导致接口超时 | 在 `RoutingClient` 构造器设置 connectTimeout=3s, readTimeout=8s |
| AI key 缺失时整个请求 500 | 增加 `aiAvailable` 标志 + 模板降级 |

---

## 六、运行验证

### 6.1 编译

```bash
mvn -q -DskipTests package
```

打包产物：`target/tingchenggis-1.0.0.jar`（fat jar，含嵌入式 Tomcat）。

### 6.2 启动

```bash
java -jar target/tingchenggis-1.0.0.jar
# 或
mvn spring-boot:run
```

### 6.3 启动后验证

| 验证 | 方法 | 期望 |
|------|------|------|
| 端口监听 | `curl http://localhost:8092/actuator/health` | 200 + `{"status":"UP"}` |
| 静态首页 | 浏览器访问 `http://localhost:8092` | 进入 SPA |
| H2 控制台 | 访问 `http://localhost:8092/h2-console` | JDBC URL `jdbc:h2:mem:testdb` |
| 默认登录 | POST `/auth/login` body `{"username":"419116","password":"419116"}` | 返回 JWT token |
| 公开接口 | GET `/thousand-pavilions/locations` | 数组（数据为空时返回 `[]`） |
| 数据导入 | POST `/thousand-pavilions/import`（multipart 文件） | 返回 `PavilionImportResult` |

> 注：首次启动时 `DataInitializer` 会日志提示通过 `POST /thousand-pavilions/import` 导入 `data/千亭.xlsx`（228 条），不导入则地图空白。

---

## 七、总结与思考

### 7.1 实验总结

1. 在仓库现状基础上验证了 18 个控制器、15 个 Service、11 个 Repository、11 个实体的真实层次
2. 厘清了 Service 接口方法命名（`createPavilion` 而非 `create`）和 Controller 入参形态（`wktText` body 而非四个 Double）
3. 验证了 `TspSolver` 的真实 API 表面（`improveCyclic` / `improveOpen`）
4. 验证了 AI 服务多 provider 配置 + 模板降级路径
5. 完成了打包与启动健康检查

### 7.2 与 deepseek 版本的主要修正

实验三独有（编码实现层面）：

| 原报告 | 实际情况 |
|--------|---------|
| `PavilionService.create()` 自动计算 GCJ-02 | `createPavilion()` 不计算；GCJ-02 在导入路径和坐标校正接口中显式生成 |
| TSP `bruteForceSolve` / `twoOptSolve` 私有方法 | 仓库中无这些方法名 |
| Controller 直接 `ResponseEntity.ok(entity)` | 实际全部包成 `{success, data, ...}` 统一响应 |
| TransportRouteController 提供 `/tsp-plan` 端点 | 仓库中只有 `/transport-routes`、`/build-network`、`/build-multi-modal` 等；TSP 在 `ThousandPavilionsService` |

> 跨报告共识修正点（API 端点形态、`solveTwoOpt` 不存在、Lombok 未引入、JaCoCo 已配置等）汇总于实验六报告 §7.2。

### 7.3 课后思考

1. **Vibe Coding 在 GIS 项目的体验**：AI 工具可以快速生成 JPA 派生方法、坐标转换骨架，但坐标系（GCJ-02 边界回退）和图层联动（needGcj 判断）这种"领域细节"必须人工把关。

2. **WKT 文本存储的取舍**：换来 H2/PostGIS 双环境无缝切换，代价是范围查询用 `BETWEEN` 而非空间索引——228 条数据下不是问题，扩展到万级需迁移到 PostGIS 原生几何列。

3. **AI 服务降级**：在 `aiAvailable=false` 时模板回复，能保证演示稳定。但模板内容写死了"醉翁亭/欧阳修"语境，对其他亭子类型可能会显得套话——这是后续可优化点。
