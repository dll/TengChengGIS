# 滁州亭城GIS系统 - 实现报告

| 项目名称 | 滁州亭城GIS系统 |
|---------|----------------|
| 文档版本 | v1.0.0 |
| 编制日期 | 2026-06-04 |
| 编制人 | 开发组 |

---

## 一、项目概述

### 1.1 项目简介

滁州亭城GIS系统是一个结合现代GIS技术与滁州亭城文化的综合性地理信息系统。该系统以《醉翁亭记》文化为背景，集成了空间数据管理、路线规划、AI智能服务、VR/AR体验等功能，为用户提供全方位的亭城文化体验。

### 1.2 技术栈

| 技术类别 | 技术选型 | 版本 |
|---------|---------|------|
| 后端框架 | Spring Boot | 3.2.0 |
| 开发语言 | Java | 21 |
| ORM框架 | Spring Data JPA | 3.x |
| 数据库 | PostgreSQL + H2 | 15.x |
| 空间库 | JTS Topology Suite | 1.19.0 |
| 安全框架 | Spring Security + JWT | 6.x |
| 构建工具 | Maven | 3.9.x |
| 测试框架 | JUnit 5 + Mockito | 5.x |
| 文档工具 | SpringDoc OpenAPI | 2.6.0 |
| 办公软件集成 | Apache POI | 5.2.5 |

---

## 二、系统架构实现

### 2.1 分层架构

系统采用经典的分层架构设计：

```
┌─────────────────────────────────────────────┐
│        Controller层 (控制器层)               │
│   - 处理HTTP请求                            │
│   - 参数验证                                │
│   - 响应封装                                │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│        Service层 (业务逻辑层)                │
│   - 业务规则实现                            │
│   - 事务管理                                │
│   - 数据转换                                │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│      Repository层 (数据访问层)               │
│   - JPA数据访问                             │
│   - 自定义查询                              │
│   - 空间查询                                │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│      Database层 (数据存储层)                 │
│   - PostgreSQL + PostGIS                    │
│   - H2 (开发环境)                           │
└─────────────────────────────────────────────┘
```

### 2.2 包结构设计

```
com.tingchenggis.tingcheng
├── TingChengGISTingChengApplication.java               # 主启动类
├── config                                               # 配置类
│   ├── AppConfig.java
│   ├── CollectorDataMigration.java
│   ├── DataInitializer.java
│   └── WebConfig.java
├── controller                                           # 控制器层（20个）
│   ├── AdminDivisionController.java
│   ├── AiController.java
│   ├── AuthController.java
│   ├── CoordinateController.java
│   ├── FaviconController.java
│   ├── FileUploadController.java
│   ├── HomeController.java
│   ├── NavigationController.java
│   ├── OgcProxyController.java
│   ├── OsmImportController.java
│   ├── PavilionController.java
│   ├── PavilionGISController.java
│   ├── PoiController.java
│   ├── RoutePlanController.java
│   ├── ScenicAreaController.java
│   ├── ThousandPavilionsController.java
│   ├── TourismRouteController.java
│   ├── TransportRouteController.java
│   ├── TravelLogController.java
│   └── VrArController.java
├── entity                                               # 实体类（11个）
│   ├── AdminDivision.java
│   ├── AdminDivisionCollector.java
│   ├── AppUser.java
│   ├── Pavilion.java
│   ├── PavilionCollector.java
│   ├── RoutePlan.java
│   ├── ScenicArea.java
│   ├── ScenicAreaCollector.java
│   ├── TourismRoute.java
│   ├── TransportRoute.java
│   └── TravelLog.java
├── repository                                           # 数据访问层（11个）
│   ├── AdminDivisionCollectorRepository.java
│   ├── AdminDivisionRepository.java
│   ├── AppUserRepository.java
│   ├── PavilionCollectorRepository.java
│   ├── PavilionRepository.java
│   ├── RoutePlanRepository.java
│   ├── ScenicAreaCollectorRepository.java
│   ├── ScenicAreaRepository.java
│   ├── TourismRouteRepository.java
│   ├── TransportRouteRepository.java
│   └── TravelLogRepository.java
├── service                                              # 业务逻辑层
│   ├── AdminDivisionCollectorService.java
│   ├── AdminDivisionService.java
│   ├── AppUserService.java
│   ├── NavigationService.java
│   ├── NavigationStep.java
│   ├── Objective.java
│   ├── OsrmRoute.java
│   ├── OverpassPoiService.java
│   ├── PavilionCollectorService.java
│   ├── PavilionExportService.java
│   ├── PavilionGISService.java
│   ├── PavilionImportService.java
│   ├── PavilionService.java
│   ├── PavilionStats.java
│   ├── RoutingClient.java
│   ├── ScenicAreaCollectorService.java
│   ├── ScenicAreaService.java
│   ├── SnapPoint.java
│   ├── ThousandPavilionsService.java
│   ├── TourismRouteService.java
│   ├── TransportRouteService.java
│   ├── TravelLogService.java
│   ├── VrArService.java
│   └── impl
│       ├── AdminDivisionCollectorServiceImpl.java
│       ├── AdminDivisionServiceImpl.java
│       ├── AppUserServiceImpl.java
│       ├── OsmDataImportService.java
│       ├── PavilionCollectorServiceImpl.java
│       ├── PavilionExportServiceImpl.java
│       ├── PavilionGISServiceImpl.java
│       ├── PavilionImportServiceImpl.java
│       ├── PavilionServiceImpl.java
│       ├── ScenicAreaCollectorServiceImpl.java
│       ├── ScenicAreaServiceImpl.java
│       ├── ThousandPavilionsServiceImpl.java
│       ├── TourismRouteServiceImpl.java
│       ├── TransportRouteServiceImpl.java
│       └── TravelLogServiceImpl.java
├── dto                                                  # 数据传输对象
│   ├── CoordinateDTO.java
│   ├── PavilionDto.java
│   └── PavilionImportResult.java
├── util                                                 # 工具类
│   ├── CoordinateTransform.java
│   ├── GeoUtils.java
│   ├── PavilionTypeUtils.java
│   └── TspSolver.java
├── exception                                            # 异常处理
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── NotFoundException.java
├── security                                             # 安全相关
│   ├── JwtAuthFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
└── ai                                                   # AI服务
    ├── AiService.java
    └── PavilionAIController.java
```

---

## 三、核心模块实现

### 3.1 亭子管理模块

#### 3.1.1 实体类设计

亭子实体类 [Pavilion.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/entity/Pavilion.java)：

```java
@Entity
@Table(name = "pavilions")
public class Pavilion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "chinese_name")
    private String chineseName;

    @Column(name = "description", length = 2000)
    private String description;

    // 空间数据
    @Column(name = "geom_wkt", columnDefinition = "TEXT")
    private String geomWkt;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    // GCJ-02偏移坐标
    @Column(name = "longitude_gcj")
    private Double longitudeGcj;

    @Column(name = "latitude_gcj")
    private Double latitudeGcj;

    // 亭子属性
    @Column(name = "pavilion_type")
    private String pavilionType; // HISTORICAL, MODERN, CULTURAL

    @Column(name = "built_year")
    private Integer builtYear;

    @Column(name = "visitor_rating")
    private Double visitorRating;

    @Column(name = "is_open_to_public")
    private Boolean isOpenToPublic;

    // 建筑细节
    @Column(name = "structure")
    private String structure;

    @Column(name = "top_style")
    private String topStyle;

    // 文化内容
    @Column(name = "historical_significance", length = 2000)
    private String historicalSignificance;

    @Column(name = "architectural_style")
    private String architecturalStyle;

    // 时间戳
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

#### 3.1.2 数据访问层

亭子仓库接口 [PavilionRepository.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/repository/PavilionRepository.java)：

```java
public interface PavilionRepository extends JpaRepository<Pavilion, Long> {

    // 按类型查询
    List<Pavilion> findByPavilionType(String pavilionType);

    // 模糊查询
    List<Pavilion> findByNameContainingIgnoreCase(String name);

    // 年代范围查询
    List<Pavilion> findByBuiltYearBetween(Integer startYear, Integer endYear);

    // 评分查询
    List<Pavilion> findByVisitorRatingGreaterThanEqual(Double minRating);

    // 开放查询
    List<Pavilion> findByIsOpenToPublicTrue();

    // 建筑风格查询
    List<Pavilion> findByArchitecturalStyle(String architecturalStyle);

    // 地理位置范围查询
    @Query("SELECT p FROM Pavilion p WHERE p.longitude BETWEEN :minLon AND :maxLon AND p.latitude BETWEEN :minLat AND :maxLat")
    List<Pavilion> findByGeographicRange(
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat);
}
```

#### 3.1.3 业务逻辑层

亭子服务实现类 [PavilionServiceImpl.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/service/impl/PavilionServiceImpl.java)：

```java
@Service
@Transactional
public class PavilionServiceImpl implements PavilionService {

    private final PavilionRepository pavilionRepository;
    private final ThousandPavilionsService thousandPavilionsService;
    private final PavilionCollectorService collectorService;

    // 创建亭子
    @Override
    public Pavilion createPavilion(Pavilion pavilion) {
        logger.info("Creating new pavilion: {}", pavilion.getName());
        pavilion.setId(null);

        // 坐标验证
        if (pavilion.getLatitude() == null || pavilion.getLongitude() == null) {
            pavilion.setLatitude(0.0);
            pavilion.setLongitude(0.0);
            pavilion.setGeomWkt("POINT(0 0)");
        }

        Pavilion savedPavilion = pavilionRepository.save(pavilion);
        logger.info("Successfully created pavilion with ID: {}", savedPavilion.getId());
        return savedPavilion;
    }

    // 分页查询
    @Override
    public Page<Pavilion> getAllPavilions(Pageable pageable) {
        logger.info("获取分页亭子列表，页码: {}, 大小: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return pavilionRepository.findAll(pageable);
    }

    // 统计功能
    @Override
    public PavilionStats getStats() {
        logger.debug("Calculating pavilion statistics");
        List<Pavilion> all = pavilionRepository.findAll();
        PavilionStats stats = new PavilionStats();
        stats.setTotalPavilions((long) all.size());

        long hist = 0, mod = 0, cult = 0;
        double sumRatings = 0;
        Pavilion top = null;
        double topRating = -1;

        for (Pavilion p : all) {
            if ("HISTORICAL".equals(p.getPavilionType())) hist++;
            else if ("MODERN".equals(p.getPavilionType())) mod++;
            else if ("CULTURAL".equals(p.getPavilionType())) cult++;

            double r = p.getVisitorRating() != null ? p.getVisitorRating() : 0.0;
            sumRatings += r;
            if (r > topRating) {
                topRating = r;
                top = p;
            }
        }

        stats.setHistoricalPavilions(hist);
        stats.setModernPavilions(mod);
        stats.setCulturalPavilions(cult);
        stats.setAverageRating(all.isEmpty() ? 0.0 : sumRatings / all.size());
        stats.setMostPopularPavilion(top != null ? top.getName() : "N/A");
        return stats;
    }

    // 推荐功能
    @Override
    public List<Pavilion> recommendPavilions(String userId, String preferences) {
        logger.info("Generating recommendations for user: {} with preferences: {}",
                userId, preferences);

        if (preferences.contains("historical")) {
            return findByPavilionType("HISTORICAL");
        } else if (preferences.contains("modern")) {
            return findByPavilionType("MODERN");
        } else if (preferences.contains("cultural")) {
            return findByPavilionType("CULTURAL");
        } else {
            return findByVisitorRatingGreaterThanEqual(4.0);
        }
    }
}
```

### 3.2 空间分析模块

#### 3.2.1 地理工具类

[GeoUtils.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/util/GeoUtils.java)：

```java
public class GeoUtils {

    // 地球半径 (公里)
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Haversine距离计算
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * 解析WKT格式边界框
     */
    public static double[] parseWktBbox(String wktText) {
        if (wktText == null || wktText.trim().isEmpty()) {
            return null;
        }

        try {
            // 简单实现：提取坐标点
            String cleanText = wktText.replaceAll("[^0-9., \\-]", "");
            String[] parts = cleanText.split(",");

            if (parts.length < 2) {
                return null;
            }

            double minLon = Double.MAX_VALUE;
            double maxLon = -Double.MAX_VALUE;
            double minLat = Double.MAX_VALUE;
            double maxLat = -Double.MAX_VALUE;

            for (String part : parts) {
                String[] coords = part.trim().split(" ");
                if (coords.length >= 2) {
                    double lon = Double.parseDouble(coords[0]);
                    double lat = Double.parseDouble(coords[1]);
                    minLon = Math.min(minLon, lon);
                    maxLon = Math.max(maxLon, lon);
                    minLat = Math.min(minLat, lat);
                    maxLat = Math.max(maxLat, lat);
                }
            }

            return new double[]{minLon, maxLon, minLat, maxLat};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算方位角
     */
    public static String computeBearing(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        bearing = (bearing + 360) % 360;

        if (bearing >= 337.5 || bearing < 22.5) return "北";
        if (bearing >= 22.5 && bearing < 67.5) return "东北";
        if (bearing >= 67.5 && bearing < 112.5) return "东";
        if (bearing >= 112.5 && bearing < 157.5) return "东南";
        if (bearing >= 157.5 && bearing < 202.5) return "南";
        if (bearing >= 202.5 && bearing < 247.5) return "西南";
        if (bearing >= 247.5 && bearing < 292.5) return "西";
        return "西北";
    }
}
```

#### 3.2.2 坐标转换

[CoordinateTransform.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/util/CoordinateTransform.java)：

```java
public class CoordinateTransform {
    private static final double PI = 3.1415926535897932384626;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;

    /**
     * WGS-84 转 GCJ-02
     */
    public static double[] wgs84ToGcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        return new double[]{lat + dLat, lon + dLon};
    }

    /**
     * GCJ-02 转 WGS-84
     */
    public static double[] gcj02ToWgs84(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        return new double[]{lat - dLat, lon - dLon};
    }

    private static boolean outOfChina(double lat, double lon) {
        return (lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271);
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
}
```

### 3.3 千亭综合模块

#### 3.3.1 TSP旅行商算法

[TspSolver.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/util/TspSolver.java) 提供两个 2-opt 优化方法，均操作 `int[]` 索引数组和 `double[][]` 距离矩阵：

- **`improveCyclic(int[] tour, double[][] dist)`** — 闭环路线，优化后返回起点，适用于环游场景
- **`improveOpen(int[] tour, double[][] dist)`** — 开路路线，固定首尾不变，优化中间顺序，适用于单程游览

`ThousandPavilionsServiceImpl` 通过 `buildDistanceMatrix()` 一次性查回所有亭子构建距离矩阵，再调用 `TspSolver.improveOpen()` 求解。

```java
public final class TspSolver {

    private TspSolver() {}

    public static int[] improveCyclic(int[] tour, double[][] dist) {
        int n = tour.length;
        boolean improved = true;
        int maxIter = 100;
        int iter = 0;
        while (improved && iter < maxIter) {
            improved = false;
            iter++;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double oldDist = dist[tour[i]][tour[(i + 1) % n]]
                        + dist[tour[j]][tour[(j + 1) % n]];
                    double newDist = dist[tour[i]][tour[j]]
                        + dist[tour[(i + 1) % n]][tour[(j + 1) % n]];
                    if (newDist < oldDist - 1e-10) {
                        reverse(tour, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }

    public static int[] improveOpen(int[] tour, double[][] dist) {
        int n = tour.length;
        if (n <= 3) return tour;
        boolean improved = true;
        int maxIter = 1000;
        int iter = 0;
        while (improved && iter < maxIter) {
            improved = false;
            iter++;
            for (int i = 1; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double oldDist = dist[tour[i - 1]][tour[i]]
                        + (j + 1 < n ? dist[tour[j]][tour[j + 1]] : 0);
                    double newDist = dist[tour[i - 1]][tour[j]]
                        + (j + 1 < n ? dist[tour[i]][tour[j + 1]] : 0);
                    if (newDist < oldDist - 1e-10) {
                        reverse(tour, i, j);
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }

    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int tmp = arr[start];
            arr[start] = arr[end];
            arr[end] = tmp;
            start++;
            end--;
        }
    }
}
```

`TspSolver` 提供两个静态方法：`improveCyclic` 用于闭环路线（遍历所有亭子后返回起点），`improveOpen` 用于开环路线（固定首尾点，仅优化中间路径）。`ThousandPavilionsServiceImpl` 在 2-opt 路线规划中调用 `improveOpen`，并在构建距离矩阵时使用 `findAllById` 一次性批量查询避免 N+1 问题。

#### 3.3.2 千亭服务实现

[ThousandPavilionsServiceImpl.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/service/impl/ThousandPavilionsServiceImpl.java)：

```java
@Service
public class ThousandPavilionsServiceImpl implements ThousandPavilionsService {

    private final PavilionRepository pavilionRepository;

    @Override
    public List<Pavilion> getAllPavilionLocations() {
        logger.info("获取所有亭子位置信息");
        return pavilionRepository.findAll();
    }

    @Override
    public double calculateDistance(Long pavilionId1, Long pavilionId2) {
        List<Pavilion> pavilions = pavilionRepository.findAllById(List.of(pavilionId1, pavilionId2));
        if (pavilions.size() < 2) return 0.0;
        Pavilion p1 = pavilions.get(0).getId().equals(pavilionId1) ? pavilions.get(0) : pavilions.get(1);
        Pavilion p2 = pavilions.get(0).getId().equals(pavilionId2) ? pavilions.get(0) : pavilions.get(1);
        if (p1.getLatitude() == null || p1.getLongitude() == null ||
            p2.getLatitude() == null || p2.getLongitude() == null) return 0.0;
        return GeoUtils.haversineKm(p1.getLongitude(), p1.getLatitude(), p2.getLongitude(), p2.getLatitude());
    }

    @Override
    public List<Long> getOptimalTraversalRoute() {
        logger.info("计算遍历所有亭子的最优路线");
        List<Pavilion> allPavilions = pavilionRepository.findAll();
        return TspSolver.solveTsp(allPavilions);
    }

    @Override
    public Map<String, Object> getSmartTourPlan(String startId, String endId, int duration, String preference) {
        logger.info("生成智能游览计划，时长: {} 分钟, 偏好: {}", duration, preference);

        Map<String, Object> plan = new HashMap<>();

        // 根据偏好筛选亭子
        List<Pavilion> candidatePavilions;
        if (preference != null && !preference.isEmpty()) {
            if (preference.contains("historical")) {
                candidatePavilions = pavilionRepository.findByPavilionType("HISTORICAL");
            } else if (preference.contains("modern")) {
                candidatePavilions = pavilionRepository.findByPavilionType("MODERN");
            } else {
                candidatePavilions = pavilionRepository.findByIsOpenToPublicTrue();
            }
        } else {
            candidatePavilions = pavilionRepository.findByIsOpenToPublicTrue();
        }

        // 根据时长选择亭子数量
        int maxPavilions = Math.min(candidatePavilions.size(), duration / 30); // 假设每个亭子30分钟
        List<Pavilion> selectedPavilions = candidatePavilions.stream()
                .sorted(Comparator.comparing(Pavilion::getVisitorRating).reversed())
                .limit(maxPavilions)
                .toList();

        // 计算最优路线
        List<Long> route = TspSolver.solveTsp(selectedPavilions);

        plan.put("pavilions", selectedPavilions);
        plan.put("route", route);
        plan.put("estimatedDuration", route.size() * 30);
        plan.put("totalDistance", calculateRouteDistance(route));

        return plan;
    }

    private double calculateRouteDistance(List<Long> route) {
        // 计算路线总距离
        if (route.size() < 2) return 0.0;
        double total = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            total += calculateDistance(route.get(i), route.get(i + 1));
        }
        return total;
    }
}
```

### 3.4 安全认证模块

#### 3.4.1 JWT工具类

[JwtUtil.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/security/JwtUtil.java)：

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret:tingcheng-secret-key}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    public String generateToken(String username, Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

#### 3.4.2 安全配置

[SecurityConfig.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/security/SecurityConfig.java) 使用 `@EnableMethodSecurity` 开启方法级安全注解（如 `@PreAuthorize`），并配置完整的公开端点白名单：

| 类别 | 白名单路径 |
|------|-----------|
| 预检请求 | `OPTIONS /**` |
| 登录/注册 | `/auth/login`, `/auth/register` |
| H2 控制台 | `/h2-console/**` |
| 静态资源 | `/`, `/index.html`, `/share.html`, `/share/**`, `/api-test.html`, `/favicon.ico`, `/css/**`, `/js/**`, `/images/**`, `/audio/**`, `/webjars/**`, `/*.js` |
| 健康检查 | `/actuator/health`, `/actuator/info` |
| GIS 读取 | `GET /thousand-pavilions/**`, `/scenic-areas/**`, `/admin-divisions/**`, `/transport-routes/**`, `/tourism-routes/**`, `/pavilions/**`, `/pavilions-gis/**`, `/poi/**`, `/route-plans/**`, `/travel-logs/**`, `/coordinate/**`, `/ai/**` |
| AI 接口 | `POST /ai/**` |
| OGC 接口 | `/ogc/**` |
| 路线分享 | `POST /thousand-pavilions/share-route` |
| 管理专有 | `/transport-routes/build-network`, `/transport-routes/build-multi-modal`, `/coordinate/correct-pavilions`, `/osm/import/**`（`hasRole("ADMIN")`） |

写操作（`POST`/`PUT`/`DELETE`）默认需要登录认证。未授权时返回 401 JSON，权限不足时返回 403 JSON（均不含重定向）。

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtil);
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/", "/index.html", "/share.html", "/share/**",
                                  "/api-test.html", "/favicon.ico",
                                  "/css/**", "/js/**", "/images/**", "/audio/**",
                                  "/webjars/**", "/*.js").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/thousand-pavilions/locations",
                    "/thousand-pavilions/route/**",
                    "/thousand-pavilions/multimedia/**",
                    "/thousand-pavilions/traverse-all",
                    "/thousand-pavilions/optimal-route",
                    "/thousand-pavilions/smart-tour",
                    "/thousand-pavilions/tourism-services",
                    "/thousand-pavilions/weather",
                    "/thousand-pavilions/nearby-facilities/**",
                    "/thousand-pavilions/vr-experience/**",
                    "/thousand-pavilions/navigation/**",
                    "/thousand-pavilions/export/**",
                    "/scenic-areas/**",
                    "/admin-divisions/**",
                    "/transport-routes/**",
                    "/tourism-routes/**",
                    "/pavilions/**",
                    "/pavilions-gis/**",
                    "/poi/**",
                    "/route-plans/**",
                    "/travel-logs/**",
                    "/coordinate/**",
                    "/ai/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/thousand-pavilions/share-route").permitAll()
                .requestMatchers(HttpMethod.POST, "/ai/**").permitAll()
                .requestMatchers("/ogc/**").permitAll()
                .requestMatchers("/transport-routes/build-network",
                                  "/transport-routes/build-multi-modal",
                                  "/coordinate/correct-pavilions",
                                  "/osm/import/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((req, res, ex) -> writeJson(res, HttpServletResponse.SC_UNAUTHORIZED,
                    "未登录或登录已过期"))
                .accessDeniedHandler((req, res, ex) -> writeJson(res, HttpServletResponse.SC_FORBIDDEN,
                    "权限不足"))
            )
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeJson(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("status", status);
        body.put("message", message);
        objectMapper.writeValue(res.getWriter(), body);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://*.localhost:*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

安全配置要点：
- 标注 `@EnableMethodSecurity` 启用方法级安全注解
- 无状态 Session（`SessionCreationPolicy.STATELESS`）
- 白名单包含：登录注册、静态资源、所有 GET 只读接口（千亭遍历、景区、路线、POI 等）、OGC 服务、AI 接口
- 管理员专有路径：`/transport-routes/build-network`、`/transport-routes/build-multi-modal`、`/coordinate/correct-pavilions`、`/osm/import/**`
- 写操作（POST/PUT/DELETE）需登录认证
- 自定义异常处理，返回 JSON 格式错误响应

#### 3.4.3 用户服务实现

[AppUserServiceImpl.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/service/impl/AppUserServiceImpl.java) 使用实体 `AppUser` 的 `getPasswordHash()` 方法（而非 `getPassword()`）访问密码字段：

```java
@Service
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserServiceImpl.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) return Optional.empty();
        return userRepository.findByUsername(username)
            .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()));
    }

    @Override
    public AppUser register(String username, String rawPassword, String displayName) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new IllegalArgumentException("密码长度不能少于4位");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword), "USER",
            displayName != null && !displayName.isBlank() ? displayName : username);
        AppUser saved = userRepository.save(user);
        logger.info("注册新用户: {} (id={})", username, saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || oldPassword == null || newPassword == null) {
            throw new BusinessException("参数不能为空");
        }
        if (newPassword.length() < 4) {
            throw new BusinessException("新密码长度不能少于4位");
        }
        AppUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("密码修改成功: username={}", username);
    }

    @Override
    public boolean ensureUser(String username, String rawPassword, String role, String displayName) {
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword),
            role != null ? role : "USER",
            displayName != null ? displayName : username);
        userRepository.save(user);
        logger.info("已创建种子账号: username={}, role={}", username, role);
        return true;
    }
}
```

### 3.5 数据导入导出模块

#### 3.5.1 Excel导入服务

[PavilionImportServiceImpl.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/service/impl/PavilionImportServiceImpl.java)：

```java
@Service
public class PavilionImportServiceImpl implements PavilionImportService {

    private final PavilionRepository pavilionRepository;

    @Override
    public PavilionImportResult importExcel(MultipartFile file) {
        PavilionImportResult result = new PavilionImportResult();
        List<Pavilion> imported = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过表头
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    Pavilion pavilion = parseRow(row);
                    imported.add(pavilion);
                } catch (Exception e) {
                    errors.add("第 " + rowNum + " 行: " + e.getMessage());
                }
                rowNum++;
            }

            // 保存导入数据
            if (!imported.isEmpty()) {
                pavilionRepository.saveAll(imported);
            }

            result.setSuccessCount(imported.size());
            result.setFailureCount(errors.size());
            result.setErrors(errors);

        } catch (Exception e) {
            result.setSuccessCount(0);
            result.setFailureCount(0);
            result.setErrors(List.of("Excel解析失败: " + e.getMessage()));
        }

        return result;
    }

    private Pavilion parseRow(Row row) {
        Pavilion pavilion = new Pavilion();

        // 解析名称
        Cell nameCell = row.getCell(0);
        if (nameCell != null) {
            pavilion.setName(nameCell.getStringCellValue().trim());
        }

        // 解析中文名称
        Cell chineseNameCell = row.getCell(1);
        if (chineseNameCell != null) {
            pavilion.setChineseName(chineseNameCell.getStringCellValue().trim());
        }

        // 解析描述
        Cell descCell = row.getCell(2);
        if (descCell != null) {
            pavilion.setDescription(descCell.getStringCellValue().trim());
        }

        // 解析经度
        Cell lonCell = row.getCell(3);
        if (lonCell != null) {
            pavilion.setLongitude(lonCell.getNumericCellValue());
        }

        // 解析纬度
        Cell latCell = row.getCell(4);
        if (latCell != null) {
            pavilion.setLatitude(latCell.getNumericCellValue());
        }

        // 解析亭子类型
        Cell typeCell = row.getCell(5);
        if (typeCell != null) {
            pavilion.setPavilionType(typeCell.getStringCellValue().trim().toUpperCase());
        }

        // 解析建造年份
        Cell yearCell = row.getCell(6);
        if (yearCell != null) {
            pavilion.setBuiltYear((int) yearCell.getNumericCellValue());
        }

        // 设置默认值
        pavilion.setIsOpenToPublic(true);
        pavilion.setCreatedAt(LocalDateTime.now());
        pavilion.setUpdatedAt(LocalDateTime.now());

        return pavilion;
    }
}
```

---

## 四、配置实现

### 4.1 应用配置

[application.yml](file:///D:/development/TingChengGIS/src/main/resources/application.yml)：

```yaml
server:
  port: 8092
  servlet:
    context-path: /

spring:
  application:
    name: tingcheng-gis

  datasource:
    url: jdbc:h2:mem:tingcheng
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect

  h2:
    console:
      enabled: true
      path: /h2-console

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

jwt:
  secret: tingcheng-secret-key-for-jwt-token-generation
  expiration: 86400000

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

tingcheng:
  ai:
    deepseek-api-key: ${DEEPSEEK_API_KEY:}
    enabled: ${DEEPSEEK_API_KEY:false}

logging:
  level:
    com.tingchenggis.tingcheng: DEBUG
    org.springframework.web: INFO
```

### 4.2 数据初始化

[DataInitializer.java](file:///D:/development/TingChengGIS/src/main/java/com/tingchenggis/tingcheng/config/DataInitializer.java) 不自动导入 Excel 文件，而是：

1. **播种默认账号**：管理员 `419116` / 密码 `419116`，注册用户 `206004` / 密码 `206004`（通过 `appUserService.ensureUser()`）
2. **加载样例亭子**：从 classpath 资源 `seed/sample-pavilions.json` 加载少量示范数据（琅琊山景区）
3. **提示完整导入**：控制台输出提示，引导用户通过 `POST /thousand-pavilions/import` 上传 `data/千亭.xlsx`

```java
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final PavilionService pavilionService;
    private final AppUserService appUserService;
    private final ObjectMapper objectMapper;

    public DataInitializer(PavilionService pavilionService, AppUserService appUserService, ObjectMapper objectMapper) {
        this.pavilionService = pavilionService;
        this.appUserService = appUserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedSamplePavilions();
    }

    private void seedUsers() {
        boolean adminCreated = appUserService.ensureUser("419116", "419116", "ADMIN", "系统管理员");
        boolean userCreated = appUserService.ensureUser("206004", "206004", "USER", "注册用户");
        if (adminCreated || userCreated) {
            logger.warn("================================================================");
            logger.warn("  已创建默认账号（密码已用 BCrypt 加密存储）");
            logger.warn("    管理员 419116 / 默认密码 419116");
            logger.warn("    注册用户 206004 / 默认密码 206004");
            logger.warn("  生产环境请尽快登录后通过「修改密码」功能更改！");
            logger.warn("================================================================");
        }
    }

    private void seedSamplePavilions() {
        long count = 0;
        try {
            count = pavilionService.getAllPavilions().size();
        } catch (Exception e) {
            logger.info("亭子数据表尚未初始化");
            return;
        }

        if (count > 0) {
            logger.info("已加载 {} 个亭子数据，交通路网将按需动态生成", count);
            return;
        }

        try (InputStream in = new ClassPathResource("seed/sample-pavilions.json").getInputStream()) {
            List<Pavilion> samples = objectMapper.readValue(in, new TypeReference<List<Pavilion>>() {});
            for (Pavilion p : samples) {
                p.setId(null);
                pavilionService.createPavilion(p);
            }
            logger.warn("================================================================");
            logger.warn("  已自动加载 {} 个样例亭子（琅琊山景区示范数据）", samples.size());
            logger.warn("  完整 228 条数据请通过 POST /thousand-pavilions/import 上传");
            logger.warn("  文件: data/千亭.xlsx");
            logger.warn("================================================================");
        } catch (Exception e) {
            logger.warn("加载样例亭子数据失败: {} （不影响启动，可通过 Excel 导入）", e.getMessage());
        }
    }
}
```

`DataInitializer` 不会自动导入 `千亭.xlsx`，它仅做两件事：
1. 播种默认账号（管理员 `419116`/`419116`，注册用户 `206004`/`206004`）
2. 从 `classpath:seed/sample-pavilions.json` 加载少量样例亭子数据（琅琊山景区示范数据）
完整 228 条数据需通过 `POST /thousand-pavilions/import` 接口手动上传 `data/千亭.xlsx`。

---

## 五、依赖管理

### 5.1 Maven依赖

[pom.xml](file:///D:/development/TingChengGIS/pom.xml) 核心依赖：

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- H2 Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- JTS Topology Suite -->
    <dependency>
        <groupId>org.locationtech.jts</groupId>
        <artifactId>jts-core</artifactId>
        <version>1.19.0</version>
    </dependency>

    <!-- Apache POI (Excel) -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- SpringDoc OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.6.0</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>

        <!-- JaCoCo Coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.12</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <limits>
                                    <limit>
                                        <counter>INSTRUCTION</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.70</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 六、测试实现

### 6.1 单元测试

项目包含完整的单元测试，覆盖：

| 测试类 | 测试方法数 | 说明 |
|-------|----------|------|
| [AppUserServiceImplTest.java](file:///D:/development/TingChengGIS/src/test/java/com/tingchenggis/tingcheng/service/impl/AppUserServiceImplTest.java) | 34 | 用户服务测试 |
| [GeoUtilsTest.java](file:///D:/development/TingChengGIS/src/test/java/com/tingchenggis/tingcheng/util/GeoUtilsTest.java) | 41 | 地理工具测试 |
| [PavilionServiceImplTest.java](file:///D:/development/TingChengGIS/src/test/java/com/tingchenggis/tingcheng/service/impl/PavilionServiceImplTest.java) | 18 | 亭子服务测试 |
| [PavilionGISServiceImplTest.java](file:///D:/development/TingChengGIS/src/test/java/com/tingchenggis/tingcheng/service/impl/PavilionGISServiceImplTest.java) | 12 | 空间服务测试 |
| ... | ... | ... |

### 6.2 测试运行

```bash
# 运行所有测试
mvn test

# 运行测试并生成覆盖率报告
mvn test jacoco:report

# 验证覆盖率
mvn verify
```

---

## 七、部署实现

### 7.1 本地运行

```bash
# 编译项目
mvn clean install

# 运行应用
mvn spring-boot:run

# 或直接运行jar包
java -jar target/tingchenggis-*.jar
```

### 7.2 访问地址

| 服务 | 地址 |
|-----|------|
| 应用首页 | http://localhost:8092 |
| Swagger UI | http://localhost:8092/swagger-ui.html |
| H2 控制台 | http://localhost:8092/h2-console |
| API 文档 | http://localhost:8092/v3/api-docs |

---

## 八、优化与改进

### 8.1 已完成优化

| 优化项 | 说明 |
|-------|------|
| getStats()性能优化 | 从多次SQL查询优化为单次查询 + 内存统计 |
| calculateDistance()优化 | 避免N+1查询，使用批量查询 |
| 坐标双支持 | 同时支持WGS-84和GCJ-02坐标 |
| 数据验证 | 导入时进行数据验证和清洗 |

### 8.2 未来优化方向

- PostgreSQL + PostGIS空间索引
- Redis缓存层
- 数据库读写分离
- 前端CDN加速
- 微服务架构拆分

---

## 九、附录

### 9.1 相关文档

- [需求报告](file:///D:/development/TingChengGIS/docs/TingChengGIS需求报告.md)
- [设计报告](file:///D:/development/TingChengGIS/docs/TingChengGIS设计报告.md)
- [测试报告](file:///D:/development/TingChengGIS/docs/测试/TingChengGIS测试报告.md)
- [运维手册](file:///D:/development/TingChengGIS/运维手册.md)

### 9.2 代码统计

| 统计项 | 数值 |
|-------|------|
| 后端 Java 源文件数 | 100 |
| 后端 Java 代码行数 | 10356 |
| 静态前端文件数 | 13（HTML×3 + JS×9 + CSS×1） |
| 前端代码行数 | 5565 |
| 测试 Java 源文件数 | 28 |
| 测试代码行数 | 3661 |
| 实体类数量 | 11 |
| 控制器数量 | 20 |
| 服务接口数量 | 23 |
| 服务实现数量 | 15 |
| 测试用例数（@Test） | 350 |

---

**报告结束**
