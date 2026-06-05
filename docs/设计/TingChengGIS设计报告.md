# 滁州亭城GIS系统 设计报告

| 项目名称 | 滁州亭城GIS系统 (TingChengGIS) |
|---------|------------------------------|
| 文档版本 | v1.0.0 |
| 编制日期 | 2026-06-04 |
| 编 制 人 | 系统设计组 |

---

## 一、系统架构设计

### 1.1 总体架构

系统采用经典分层架构（表现层 — 业务逻辑层 — 数据访问层 — 数据存储层），后端 Spring Boot 单体应用，前端单页 HTML + 模块化 JS 混合。

```
┌────────────────────────────────────────────────────────────┐
│                   表现层 (Presentation Layer)              │
│  ┌────────────────────┐  ┌──────────────────────────┐    │
│  │  单页 HTML(index) + │  │  RESTful API (JSON)      │    │
│  │  模块化 JS(8个模块)  │  │  Swagger/OpenAPI 文档    │    │
│  │  Leaflet 2D/Cesium 3D│  │                          │    │
│  └────────────────────┘  └──────────────────────────┘    │
├────────────────────────────────────────────────────────────┤
│                  业务逻辑层 (Service Layer)                 │
│  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐      │
│  │Pavilion│ │GIS    │ │AI     │ │Routing│ │Auth/  │      │
│  │Service │ │Service│ │Service│ │Client │ │User   │      │
│  ├───────┤ ├───────┤ ├───────┤ ├───────┤ ├───────┤      │
│  │Thousand│ │Import/│ │Tsp    │ │Collect│ │Coordinate     │
│  │Pavilion│ │Export │ │Solver │ │Service│ │Transform│      │
│  └───────┘ └───────┘ └───────┘ └───────┘ └───────┘      │
├────────────────────────────────────────────────────────────┤
│                  数据访问层 (Data Access Layer)             │
│  ┌──────────────────────────────────────────────────┐     │
│  │  JPA Repository 接口: Pavilion, AppUser,         │     │
│  │  TransportRoute, AdminDivision, ScenicArea,      │     │
│  │  TravelLog, TourismRoute, RoutePlan, *Collector  │     │
│  └──────────────────────────────────────────────────┘     │
├────────────────────────────────────────────────────────────┤
│                  数据存储层 (Data Layer)                     │
│  ┌───────────────────────┐  ┌──────────────────────────┐ │
│  │  Dev: H2 (内存数据库)  │  │  Prod: PostgreSQL +      │ │
│  │  ddl-auto: create-drop │  │  PostGIS 16 (空间数据库)   │ │
│  └───────────────────────┘  └──────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

**关键架构决策**：

- **坐标双存储**：WKT 文本 (`geom_wkt`) 用于跨数据库移植（H2 ↔ PostGIS），`longitude`/`latitude` 双精度字段用于基本定位查询，`*_gcj` 字段用于高德/腾讯地图 GCJ-02 坐标渲染
- **双数据库策略**：开发环境使用 H2 内存数据库 + `create-drop`（免配置启动），生产环境使用 PostgreSQL + PostGIS 16（空间索引、GIS 函数）
- **坐标转换**：后端 `CoordinateTransform.java` 实现 WGS-84 ↔ GCJ-02 迭代式转换，前端 `leafCoord()` / `gcj02Wgs()` 提供近似转换
- **OGC 服务**：WMS/WFS 通过 `/ogc/**` 端点提供标准 GIS 服务

### 1.2 技术架构

#### 1.2.1 后端技术栈

| 技术组件 | 版本 | 用途 |
|---------|------|------|
| 开发语言 | Java 21 | 后端开发语言 |
| Web框架 | Spring Boot 3.x | 应用框架 |
| ORM框架 | Spring Data JPA | 数据持久化 |
| 安全框架 | Spring Security + JWT | 安全认证 |
| 空间库 | JTS Topology Suite | 空间数据处理 |
| 数据库驱动 | PostGIS JDBC | PostgreSQL空间扩展 |
| 构建工具 | Maven 3.9+ | 项目构建 |
| 测试框架 | JUnit 5 + Mockito | 单元测试 |

#### 1.2.2 前端技术栈

| 技术组件 | 版本 | 用途 |
|---------|------|------|
| HTML/CSS/JS | 原生 ES6+ | 前端界面与交互逻辑 |
| Leaflet | 1.9.4 (CDN) | 2D 地图渲染 |
| Cesium | 1.115 (CDN) | 3D 地球视图 |
| Bootstrap | 5.3.2 (CDN) | UI 组件与布局 |
| leaflet.markercluster | 1.5.3 (CDN) | 地图标注聚类 |
| Leaflet Draw | — | 数字化绘图（点/线/面） |
| Web Speech API | 浏览器内置 | 语音播报 |
| Web Share API | 浏览器内置 | 原生分享 |
| Canvas 2D | 浏览器内置 | 旅行地图绘制 |
| navigator.mediaDevices | 浏览器内置 | 相机/麦克风调用 |

### 1.3 部署架构

系统为单体 Spring Boot 应用，部署方式灵活（直接运行 JAR / Docker / CI/CD）。

```
                    ┌──────────────────────┐
                    │   Spring Boot App     │
                    │   :8092 / H2 (dev)    │
                    │   :8092 / PG (prod)   │
                    └──────────────────────┘
```

- **开发环境**：`mvn spring-boot:run` 启动，H2 内存数据库 + `create-drop` 自动建表
- **生产环境**：`java -jar app.jar --spring.profiles.active=prod`，连接 PostgreSQL/PostGIS
- **CI/CD**：GitHub Actions 自动构建 → Docker 镜像 (`eclipse-temurin:21-jre-alpine`) → 推送 Docker Hub
- **外部依赖**：OSRM 公共路由服务 (`router.project-osrm.org`)、AI 大模型 API（DeepSeek/Zhipu/OpenAI）

---

## 二、功能模块设计

### 2.1 模块划分

系统划分为以下核心模块：

| 模块名称 | 功能描述 | 优先级 |
|---------|---------|-------|
| 认证授权模块 | 用户注册、登录、权限控制 | 高 |
| 亭子管理模块 | 亭子CRUD、查询、统计 | 高 |
| 千亭综合模块 | 地图展示、路线规划、导航 | 高 |
| 空间分析模块 | 距离计算、缓冲区、热力图 | 高 |
| 数据导入导出模块 | Excel、GeoJSON导入导出 | 中 |
| AI智能服务模块 | AI对话、文化介绍生成 | 中 |
| VR/AR体验模块 | VR、AR、3D场景 | 低 |
| 交通路线模块 | 路线查询、路网管理 | 中 |
| 导航服务模块 | 逐向导航、路线指引 | 高 |
| 系统管理模块 | 系统配置、日志管理 | 中 |

### 2.2 模块依赖关系

```
┌─────────────────────────────────────────────────────────┐
│                      千亭综合模块                          │
│  ┌──────────────────────────────────────────────┐    │
│  │ 地图展示  │  路线规划  │  导航服务  │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
              ↑
┌─────────────────────────────────────────────────────────┐
│                      空间分析模块                          │
│  ┌──────────────────────────────────────────────┐    │
│  │ 距离计算  │  缓冲区分析 │  热力图    │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
              ↑
┌─────────────────────────────────────────────────────────┐
│                      亭子管理模块                          │
│  ┌──────────────────────────────────────────────┐    │
│  │ 亭子CRUD  │  查询统计  │  数据导入导出 │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
              ↑
┌─────────────────────────────────────────────────────────┐
│                      认证授权模块                          │
│  ┌──────────────────────────────────────────────┐    │
│  │ 用户注册  │  登录认证  │  权限控制  │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

---

## 三、数据库设计

### 3.1 数据库选型

- **主数据库**：PostgreSQL 15 + PostGIS 3
- **原因**：
  - 强大的空间数据支持
  - 成熟的GIS扩展
  - 优秀的查询性能
  - 开源免费
  - 丰富的生态系统

### 3.2 ER图设计

系统共 11 张 JPA 实体表，核心关系如下：

```
┌─────────────────┐         ┌──────────────────┐
│  pavilions     │         │  app_users      │
│  (亭子)        │         │  (用户)          │
├─────────────────┤         ├──────────────────┤
│ PK id          │         │ PK id           │
│ name           │         │ username        │
│ chinese_name   │         │ password_hash   │
│ longitude      │         │ role            │
│ latitude       │         └──────────────────┘
│ longitude_gcj  │
│ latitude_gcj   │
│ geom_wkt       │
│ pavilion_type  │
│ ...            │
└─────────────────┘
     │                    ┌──────────────────────┐
     │                    │  transport_routes   │
     ├───────────────────→│  (交通路线)           │
     │                    ├──────────────────────┤
     │                    │ PK id               │
     │                    │ FK from_pavilion_id  │
     │                    │ FK to_pavilion_id    │
     │                    │ distance_km          │
     │                    │ transport_mode       │
     │                    └──────────────────────┘
     │
     ├───────────────────┐
     │                    │
     │   ┌────────────────▼──────┐   ┌──────────────────┐
     │   │  route_plans         │   │  travel_logs     │
     │   │  (路线计划)           │   │  (游览日志)       │
     │   ├───────────────────────┤   ├──────────────────┤
     │   │ PK id                │   │ PK id           │
     │   │ visit_order_ids      │   │ title           │
     │   │ total_distance       │   │ location        │
     │   │ gif_path             │   │ photo_url       │
     │   └───────────────────────┘   │ author          │
     │                               └──────────────────┘
     │
     │   ┌──────────────────┐   ┌──────────────────┐
     │   │  scenic_areas   │   │ tourism_routes   │
     │   │  (景区)          │   │  (旅游路线)       │
     │   └──────────────────┘   └──────────────────┘
     │
     │   ┌──────────────────────────────┐
     │   │  admin_divisions             │
     │   │  (行政区划)                   │
     │   └──────────────────────────────┘
     │
     │   ┌──────────────────────────────────────┐
     │   │  pavilion_collectors /               │
     │   │  scenic_area_collectors /            │
     │   │  admin_division_collectors           │
     │   │  (外业采集记录, 3张泛化表)             │
     │   └──────────────────────────────────────┘
```
┌─────────────────┐         ┌──────────────────┐
│    Pavilion  │         │    AppUser    │
│    (亭子)    │         │     (用户)     │
├─────────────────┤         ├──────────────────┤
│ PK id         │         │ PK id          │
│    name       │         │    username    │
│    chineseName│         │    password    │
│    description │         │    displayName  │
│    longitude   │         │    role        │
│    latitude    │         └──────────────────┘
│    geomWkt     │
│    pavilionType│
│    builtYear   │
│    rating      │
│    ...         │
└─────────────────┘
         │
         │
         │
┌─────────────────┐         ┌──────────────────┐
│ TransportRoute │         │  AdminDivision  │
│  (交通路线)     │         │   (行政区划)     │
├─────────────────┤         ├──────────────────┤
│ PK id         │         │ PK id          │
│ FK fromPavilion│         │    name        │
│ FK toPavilion  │         │    code        │
│    transportMode│         │    level       │
│    distanceKm   │         │    geomWkt     │
│    durationMinutes│        └──────────────────┘
└─────────────────┘
         │
         │
┌─────────────────┐
│   ScenicArea   │
│    (景区)       │
├─────────────────┤
│ PK id         │
│    name        │
│    description  │
│    geomWkt     │
└─────────────────┘
```

### 3.3 表结构设计

#### 3.3.1 亭子表 (pavilions)

```sql
CREATE TABLE pavilions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    chinese_name VARCHAR(255),
    description TEXT,
    historical_significance TEXT,
    construction_period VARCHAR(255),
    architectural_style VARCHAR(255),

    -- 空间坐标: WKT 文本跨数据库兼容 + 双精度字段快速查询
    geom_wkt TEXT,
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,

    -- GCJ-02 偏移坐标（高德/腾讯地图用）
    longitude_gcj DOUBLE PRECISION,
    latitude_gcj DOUBLE PRECISION,

    pavilion_type VARCHAR(50),          -- HISTORICAL / MODERN / CULTURAL
    area_size DOUBLE PRECISION,          -- 平方米
    structure VARCHAR(255),              -- 结构类型
    top_style VARCHAR(255),              -- 顶部样式
    street VARCHAR(255),                 -- 所在街道
    notes TEXT,                          -- 备注
    location_desc VARCHAR(255),          -- 位置描述
    visitor_rating DOUBLE PRECISION,     -- 评分 (1-5)
    is_open_to_public BOOLEAN DEFAULT TRUE,
    ticket_price DOUBLE PRECISION,
    built_year INTEGER,
    last_renovation_year INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pavilion_type ON pavilions(pavilion_type);
CREATE INDEX idx_pavilion_built_year ON pavilions(built_year);
-- 空间查询通过 Java 层 JTS 解析 geom_wkt 处理,
-- PostGIS 环境可额外添加 GIST 索引:
-- CREATE INDEX idx_pavilion_geom ON pavilions USING GIST (ST_GeomFromText(geom_wkt, 4326));
```

#### 3.3.2 用户表 (app_users)

```sql
CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- BCrypt 加密
    role VARCHAR(50) NOT NULL DEFAULT 'USER',  -- USER / ADMIN
    display_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_app_user_username ON app_users(username);
```

#### 3.3.3 行政区划表 (admin_divisions)

```sql
CREATE TABLE admin_divisions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    chinese_name VARCHAR(255),
    admin_level INTEGER NOT NULL,          -- 行政级别
    parent_id BIGINT,                      -- 父级 ID
    parent_name VARCHAR(255),
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    geom_wkt TEXT,
    area_size DOUBLE PRECISION,
    population INTEGER,
    admin_code VARCHAR(50),               -- 行政区划代码
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admin_division_code ON admin_divisions(admin_code);
CREATE INDEX idx_admin_division_parent ON admin_divisions(parent_id);
```

#### 3.3.4 景区表 (scenic_areas)

```sql
CREATE TABLE scenic_areas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    chinese_name VARCHAR(255),
    description TEXT,
    area_type VARCHAR(50),
    area_size DOUBLE PRECISION,
    geom_wkt TEXT,
    boundary_wkt TEXT,                    -- 边界 WKT
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    address VARCHAR(255),
    opening_hours VARCHAR(255),
    ticket_price DOUBLE PRECISION,
    visitor_rating DOUBLE PRECISION,
    is_open_to_public BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.3.5 交通路线表 (transport_routes)

```sql
CREATE TABLE transport_routes (
    id BIGSERIAL PRIMARY KEY,
    route_name VARCHAR(255),
    route_type VARCHAR(50),
    from_pavilion_id BIGINT,
    to_pavilion_id BIGINT,
    distance_km DOUBLE PRECISION,
    travel_time_minutes INTEGER,
    route_description TEXT,
    is_accessible BOOLEAN DEFAULT TRUE,
    is_scenic_route BOOLEAN DEFAULT FALSE,
    road_type VARCHAR(50),
    waypoints TEXT,                        -- 途经点 JSON
    elevation_gain DOUBLE PRECISION,
    transport_mode VARCHAR(50),
    road_level VARCHAR(50),
    traffic_condition VARCHAR(50),
    estimated_fare DOUBLE PRECISION,
    geom_wkt TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_pavilion_id) REFERENCES pavilions(id),
    FOREIGN KEY (to_pavilion_id) REFERENCES pavilions(id)
);

CREATE INDEX idx_transport_route_from ON transport_routes(from_pavilion_id);
CREATE INDEX idx_transport_route_to ON transport_routes(to_pavilion_id);
```

#### 3.3.6 游览日志表 (travel_logs)

```sql
CREATE TABLE travel_logs (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    location VARCHAR(500),
    route_id BIGINT,
    scenic_id BIGINT,
    photo_url VARCHAR(500),
    rating INTEGER,
    author VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.3.7 路线计划表 (route_plans)

```sql
CREATE TABLE route_plans (
    id BIGSERIAL PRIMARY KEY,
    plan_name VARCHAR(255),
    transport_mode VARCHAR(50),
    objective VARCHAR(255),
    visit_order_ids TEXT,                  -- 亭子访问顺序 ID 列表 (JSON)
    visit_order_names TEXT,
    plan_json TEXT,
    total_distance DOUBLE PRECISION,
    total_duration INTEGER,
    total_fare DOUBLE PRECISION,
    total_ticket DOUBLE PRECISION,
    total_cost DOUBLE PRECISION,
    pavilion_count INTEGER,
    notes TEXT,
    gif_path VARCHAR(500),                -- TSP 路线动图路径
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.3.8 旅游路线表 (tourism_routes)

```sql
CREATE TABLE tourism_routes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    route_type VARCHAR(50),
    difficulty VARCHAR(50),
    geom_wkt TEXT,
    distance DOUBLE PRECISION,
    duration INTEGER,
    scenic_stops TEXT,                     -- 沿途景点停靠点 (JSON)
    color VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 3.3.9 外业采集数据表（3张泛化采集表）

系统支持 GIS 外业数据采集，通过三张结构相同的采集表记录亭子、景区、行政区划的现场采集信息：

```sql
CREATE TABLE pavilion_collectors (
    id BIGSERIAL PRIMARY KEY,
    pavilion_id BIGINT,
    collector_user_id BIGINT,
    collector_name VARCHAR(100),
    collection_time TIMESTAMP,
    collection_tool VARCHAR(100),
    accuracy DOUBLE PRECISION,
    data_source VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE scenic_area_collectors (
    id BIGSERIAL PRIMARY KEY,
    scenic_area_id BIGINT,
    collector_user_id BIGINT,
    collector_name VARCHAR(100),
    collection_time TIMESTAMP,
    collection_tool VARCHAR(100),
    accuracy DOUBLE PRECISION,
    data_source VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin_division_collectors (
    id BIGSERIAL PRIMARY KEY,
    admin_division_id BIGINT,
    collector_user_id BIGINT,
    collector_name VARCHAR(100),
    collection_time TIMESTAMP,
    collection_tool VARCHAR(100),
    accuracy DOUBLE PRECISION,
    data_source VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.4 数据量预估

| 数据表 | 初始数据量 | 年增长率 |
|-------|----------|---------|
| pavilions（亭子） | 228 | 10% |
| app_users（用户） | 1000 | 50% |
| transport_routes（交通路线） | 500 | 20% |
| admin_divisions（行政区划） | 50 | 5% |
| scenic_areas（景区） | 20 | 10% |
| travel_logs（游览日志） | 100 | 30% |
| route_plans（路线计划） | 50 | 20% |

### 3.5 种子数据机制

`DataInitializer`（`CommandLineRunner`）在应用启动时自动执行：

1. **用户账户**：预置 `admin/419116`（管理员）和 `user/206004`（普通用户），密码 = 用户名
2. **样例亭子**：从 `classpath:seed/sample-pavilions.json` 读取 10 个琅琊山样例亭子（醉翁亭、丰乐亭等），仅在数据库为空时加载
3. **导入提示**：打印日志提示通过 `POST /thousand-pavilions/import` 导入 `data/千亭.xlsx`（228 条完整数据）

> 种子数据使开发环境启动后即有可展示内容，无需手动导入。

---

## 四、接口设计

### 4.1 RESTful API设计规范

| 规范项 | 规范内容 |
|-------|---------|
| URL风格 | 小写字母，名词复数，使用连字符分隔 |
| HTTP方法 | GET（查询）、POST（创建）、PUT（更新）、DELETE（删除） |
| 状态码 | 200成功、201创建成功、400参数错误、401未认证、403无权限、404不存在、500服务器错误 |
| 请求格式 | JSON |
| 响应格式 | 统一响应结构 |

### 4.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

### 4.3 核心接口设计

系统包含 **20个 Controller，134个 REST 端点**。以下按功能模块列出核心接口（GET需认证=游客可用，带 🔒 标记=需登录，带 🔑 标记=需管理员）：

#### 4.3.1 认证接口 `/auth`

| 接口 | 方法 | 说明 | 权限 |
|-----|------|------|------|
| /auth/register | POST | 用户注册 | 公开 |
| /auth/login | POST | 用户登录，返回 JWT | 公开 |
| /auth/me | GET | 获取当前用户信息 | 🔒 登录 |
| /auth/change-password | POST | 修改密码 | 🔒 登录 |

#### 4.3.2 亭子管理 `/pavilions`

| 接口 | 方法 | 说明 | 权限 |
|-----|------|------|------|
| /pavilions | GET | 分页列表（page/size/sort） | 公开 |
| /pavilions/{id} | GET | 亭子详情 | 公开 |
| /pavilions | POST | 创建亭子 | 🔑 管理 |
| /pavilions/{id} | PUT | 更新亭子 | 🔑 管理 |
| /pavilions/{id} | DELETE | 删除亭子 | 🔑 管理 |
| /pavilions/type/{type} | GET | 按类型筛选 | 公开 |
| /pavilions/search?name= | GET | 按名称搜索 | 公开 |
| /pavilions/by-year-range | GET | 按年份范围筛选 | 公开 |
| /pavilions/popular | GET | 按评分筛选 | 公开 |
| /pavilions/geographic-search | GET | 地理范围查询（WKT区域） | 公开 |
| /pavilions/stats | GET | 亭子统计信息 | 公开 |
| /pavilions/recommendations | POST | AI 推荐 | 🔒 登录 |

#### 4.3.3 GIS空间分析 `/pavilions-gis`

| 接口 | 方法 | 说明 |
|-----|------|------|
| /pavilions-gis/distance/{id1}/{id2} | GET | 计算两亭距离（米） |
| /pavilions-gis/nearest?lng=&lat=&limit= | GET | 最近亭子查询 |
| /pavilions-gis/{id}/buffer?radius= | GET | 缓冲区查询 |
| /pavilions-gis/heatmap | GET | 热力图数据 |
| /pavilions-gis/density | POST | 密度分析（WKT区域） |
| /pavilions-gis/shortest-path/{start}/{end} | GET | 最短路径 |
| /pavilions-gis/optimal-path?ids= | GET | A* 最优路径 |
| /pavilions-gis/overview | GET | 系统概览文本 |

#### 4.3.4 千亭综合 `/thousand-pavilions`

| 接口 | 方法 | 说明 | 权限 |
|-----|------|------|------|
| /thousand-pavilions/locations | GET | 所有亭子位置（支持类型/搜索过滤） | 公开 |
| /thousand-pavilions/route/{fromId}/{toId} | GET | 两亭间路线信息 | 公开 |
| /thousand-pavilions/traverse-all | GET | 遍历所有亭子的 TSP 路线 | 公开 |
| /thousand-pavilions/navigation-plan | POST | 导航方案生成 | 公开 |
| /thousand-pavilions/navigation/{fromId}/{toId} | GET | 逐向导航 | 公开 |
| /thousand-pavilions/optimal-route | GET | TSP最优路径（2-opt） | 公开 |
| /thousand-pavilions/smart-tour | GET | 智能游览规划 | 🔒 登录 |
| /thousand-pavilions/nearby-facilities/{id} | GET | 附近设施查询 | 公开 |
| /thousand-pavilions/weather | GET | 天气信息 | 公开 |
| /thousand-pavilions/multimedia/{id} | GET | 多媒体信息（图片/音频/视频） | 公开 |
| /thousand-pavilions/vr-experience/{id} | GET | VR 体验数据 | 公开 |
| /thousand-pavilions/tourism-services | GET | 旅游服务信息 | 公开 |
| /thousand-pavilions/share-route | POST | 生成可分享路线 | 🔒 登录 |
| /thousand-pavilions/multi-route | POST | 多点路线规划 | 🔒 登录 |
| /thousand-pavilions | POST | 创建亭子 | 🔑 管理 |
| /thousand-pavilions/{id} | PUT | 更新亭子 | 🔑 管理 |
| /thousand-pavilions/{id} | DELETE | 删除亭子 | 🔑 管理 |
| /thousand-pavilions/import | POST | 导入数据（Excel/GeoJSON/CSV） | 🔑 管理 |
| /thousand-pavilions/export/geojson | GET | 导出 GeoJSON | 🔑 管理 |
| /thousand-pavilions/export/excel | GET | 导出 Excel | 🔑 管理 |
| /thousand-pavilions/export/csv | GET | 导出 CSV | 🔑 管理 |
| /thousand-pavilions/export/excel-template | GET | 下载导入模板 | 公开 |
| /thousand-pavilions/{id}/collectors | GET | 采集记录列表 | 公开 |
| /thousand-pavilions/{id}/collectors | POST | 添加采集记录 | 🔒 登录 |

#### 4.3.5 AI 智能 `/ai`

| 接口 | 方法 | 说明 |
|-----|------|------|
| /ai/chat?message= | GET | AI 对话 |
| /ai/pavilion/{id} | GET | AI 亭子文化介绍 |
| /ai/tour-advice | POST | AI 游览建议 |
| /ai/status | GET | AI 服务状态 |
| /ai/culture-intro?name=&location= | GET | AI 文化介绍生成 |
| /ai/historical-story?name=&location= | GET | AI 历史故事生成 |
| /ai/ask | POST | AI 问答 |
| /ai/tourism-advice | POST | AI 旅游建议 |
| /ai/culture-overview | GET | AI 滁州亭城文化概览 |

#### 4.3.6 导航 `/nav`

| 接口 | 方法 | 说明 |
|-----|------|------|
| /nav/turn-by-turn/{fromId}/{toId} | GET | 逐向导航（亭子ID） |
| /nav/turn-by-turn/coords?from=&to= | GET | 逐向导航（坐标） |

#### 4.3.7 路线计划 `/route-plans`

| 接口 | 方法 | 说明 |
|-----|------|------|
| /route-plans | GET | 列表（TSP保存路线） |
| /route-plans/{id} | GET | 详情 |
| /route-plans | POST | 保存路线计划 |
| /route-plans/{id} | DELETE | 删除 |
| /route-plans/{id}/gif | GET | 获取路线 GIF |
| /route-plans/{id}/gif | POST | 上传路线 GIF |

#### 4.3.8 交通路线 `/transport-routes`

| 接口 | 方法 | 说明 | 权限 |
|-----|------|------|------|
| /transport-routes | GET | 所有交通路线 | 公开 |
| /transport-routes/{id} | GET | 单条路线 | 公开 |
| /transport-routes/modes | GET | 交通方式列表 | 公开 |
| /transport-routes/by-mode/{mode} | GET | 按方式筛选 | 公开 |
| /transport-routes/stats | GET | 路线统计 | 公开 |
| /transport-routes/scenic | GET | 景观路线 | 公开 |
| /transport-routes/from/{id} | GET | 从某亭出发的路线 | 公开 |
| /transport-routes/between/{id1}/{id2} | GET | 两亭间路线 | 公开 |
| /transport-routes | POST | 创建路线 | 🔑 管理 |
| /transport-routes/{id} | PUT | 更新路线 | 🔑 管理 |
| /transport-routes/{id} | DELETE | 删除路线 | 🔑 管理 |
| /transport-routes/tsp-plan | POST | TSP 路线规划 | 🔑 管理 |
| /transport-routes/build-network | POST | 构建路网（OSRM） | 🔑 管理 |
| /transport-routes/build-multi-modal | POST | 构建多模式路网 | 🔑 管理 |

#### 4.3.9 坐标转换 `/coordinate`

| 接口 | 方法 | 说明 | 权限 |
|-----|------|------|------|
| /coordinate/transform?lng=&lat= | GET | 单点 WGS-84 → GCJ-02 | 公开 |
| /coordinate/correct-pavilions | POST | 批量纠正 GCJ-02 坐标 | 🔑 管理 |

#### 4.3.10 其他管理接口

| 路径 | 方法 | 说明 | 权限 |
|-----|------|------|------|
| /scenic-areas/... | CRUD | 景区管理 | 🔑 管理(写) / 公开(读) |
| /admin-divisions/... | CRUD | 行政区划管理 | 🔑 管理(写) / 公开(读) |
| /tourism-routes/... | CRUD | 旅游路线 | 🔑 管理(写) / 公开(读) |
| /travel-logs/... | CRUD | 游览日志 | 🔒 登录 |
| /osm/import/all\|scenic\|admin | POST | OSM 数据导入 | 🔑 管理 |
| /api/upload/photo | POST | 图片上传 | 🔒 登录 |
| /ogc/wms/\* /wfs/\* | POST | OGC 代理服务 | 公开 |
| /vr-ar/\* | GET | VR/AR 体验数据 | 公开 |
| /poi/nearby | GET | 附近 POI 查询 | 公开 |

---

## 五、核心算法设计

### 5.1 TSP旅行商算法

#### 5.1.1 算法实现

系统采用 **2-opt 局部搜索启发式算法**，同时支持闭合环路（旅行商回路）和开放路径（多点路径规划）两种模式：

| 场景 | 方法 | 说明 |
|-----|------|------|
| 闭合回路（起点=终点） | `TspSolver.improveCyclic()` | 最大 100 次迭代，反转子路径优化 |
| 开放路径（多点→终点） | `TspSolver.improveOpen()` | 最大 1000 次迭代，优化内部节点顺序 |

#### 5.1.2 2-opt算法流程

```
1. 初始化：生成初始路径（最近邻贪心）
2. 迭代优化：
   a. 选择两条未相邻边 (i,i+1) 和 (j,j+1)
   b. 尝试交换：反转 i+1 到 j 之间的子路径
   c. 如果新路径距离 < 原距离 - ε，则接受
   d. 重复直到无法改进或达到最大迭代次数
3. 返回优化路径
```

#### 5.1.3 复杂度分析

- **时间复杂度**：O(n²) 每次迭代
- **空间复杂度**：O(n) 用于存储路径序列

### 5.2 辅助工具类

#### 5.2.1 Haversine距离计算

```java
public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double R = 6371; // 地球半径(km)
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}
```

#### 5.2.2 GeoUtils 工具方法

`GeoUtils.round2(double val)` — 保留两位小数，用于前端显示；
`GeoUtils.computeBearing(lat1, lng1, lat2, lng2)` — 计算两点间方位角；
`GeoUtils.parseWkt(String wkt)` — 解析 WKT 文本为 JTS Geometry 对象。`

### 5.3 空间查询策略

系统采用 **Java 层 JTS（Java Topology Suite）** 处理空间查询，确保兼容 H2 和 PostGIS 两种数据库：

```java
// Java 层空间查询（跨数据库兼容）
public List<Pavilion> findWithinRange(double centerLng, double centerLat, double radiusKm) {
    // 读取所有 WKT 几何 → JTS Geometry 对象 → 内存中过滤
    return pavilionRepository.findAll().stream()
        .filter(p -> {
            Geometry geom = new WKTReader().read(p.getGeomWkt());
            Point center = gf.createPoint(new Coordinate(centerLng, centerLat));
            return geom.distance(center) * 111.32 < radiusKm;
        })
        .collect(Collectors.toList());
}
```

**PostGIS 优化**（生产环境）：可额外添加 GIST 空间索引进行数据库级空间查询：

```sql
CREATE INDEX idx_pavilion_geom ON pavilions USING GIST (
    ST_SetSRID(ST_GeomFromText(geom_wkt), 4326));
```

**H2 兼容说明**：开发环境使用 H2 时，空间查询通过 Java JTS 在应用层完成；切换到 PostGIS 生产环境后，可启用原生 SQL 空间查询以获得更好的性能。

---

## 六、安全设计

### 6.1 认证设计

#### 6.1.1 JWT Token设计

```json
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user_id",
  "username": "username",
  "role": "USER",
  "exp": 1234567890
}

Signature: HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

#### 6.1.2 认证流程

```
1. 用户提交用户名密码
2. 服务端 BCrypt 验证凭证
3. 生成 JWT Token（含用户 ID、用户名、角色）
4. 返回 Token 给客户端
5. 客户端存储到 localStorage，后续请求 Header: Authorization: Bearer <token>
6. JwtAuthenticationFilter 从请求头提取并验证 Token
7. 提取用户信息设置到 SecurityContext 进行授权
```

### 6.2 授权设计

#### 6.2.1 @PreAuthorize 方法级授权

使用 `@EnableMethodSecurity` + `@PreAuthorize` 注解：

```java
@RestController
@RequestMapping("/pavilions")
public class PavilionController {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Pavilion> createPavilion(@RequestBody Pavilion pavilion) {
        // 只有管理员可以创建亭子
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePavilion(@PathVariable Long id) {
        // 只有管理员可以删除亭子
    }
}
```

**ADMIN-only 端点**（共 6 个，分布在 3 个 Controller）：
- `CoordinateController`: `POST /coordinate/correct-pavilions`
- `TransportRouteController`: `POST /transport-routes/build-network`, `POST /transport-routes/build-multi-modal`
- `OsmImportController`: `POST /osm/import/all`, `POST /osm/import/scenic`, `POST /osm/import/admin`

#### 6.2.2 安全白名单（SecurityConfig.filterChain）

系统采用 **URL 模式白名单** 机制：大多数 GET 端点 + AI 服务 + OGC 服务对公众开放，写入操作需要认证：

```java
// SecurityConfig 关键配置
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/pavilions/**", "/thousand-pavilions/**",
                     "/pavilions-gis/**", "/ai/**", "/ogc/**", "/nav/**",
                     "/admin-divisions/**", "/scenic-areas/**", "/tourism-routes/**",
                     "/vr-ar/**", "/poi/**", "/share/**")
        .permitAll()
    .requestMatchers("/travel-logs/**", "/route-plans/**", "/api/upload/**")
        .authenticated()
    .anyRequest().authenticated()
)
```

> 白名单中的 GET 端点无需认证即可访问；写入操作（POST/PUT/DELETE）由 `@PreAuthorize` 进一步控制权限。

### 6.3 数据安全

| 安全项 | 措施 |
|-------|------|
| 密码加密 | BCryptPasswordEncoder加密存储 |
| SQL注入 | JPA参数化查询 |
| XSS防护 | 输出自动转义 |
| CSRF防护 | Spring Security CSRF Token |
| 敏感数据 | 日志脱敏 |

---

## 七、性能设计

### 7.1 缓存策略

当前系统 **未配置缓存层**。所有请求直接访问数据库。后续可引入：

| 缓存对象 | 建议方案 | 过期时间 |
|--------|---------|---------|
| 亭子列表/详情 | Spring Cache + Caffeine | 30分钟 |
| 统计数据 | Spring Cache + Caffeine | 10分钟 |
| TSP计算结果 | Spring Cache + Caffeine | 5分钟 |
| OSRM路由响应 | 本地内存缓存 | 10分钟 |

### 7.2 数据库优化

| 优化项 | 措施 |
|-------|------|
| 索引优化 | 常用查询字段索引（type, year, name 等） |
| 查询优化 | 避免 N+1 查询，使用 JOIN FETCH |
| 分页查询 | Spring Data 分页 + Pageable |

### 7.3 前端性能优化

- CSS/JS 文件合并压缩
- Leaflet 瓦片地图懒加载
- CDN 加速（Bootstrap/Leaflet/Cesium 静态资源）
- 标注聚类（`leaflet.markercluster`）减少 DOM 节点数

---

## 八、部署设计

### 8.1 环境划分

| 环境 | 用途 | 数据库 | 启动方式 |
|-----|------|-------|---------|
| 开发环境 | 本地开发调试 | H2 内存数据库（`ddl-auto: create-drop`） | `mvn spring-boot:run` |
| 测试环境 | CI 集成测试 | H2 内存 + JaCoCo 覆盖率检查 | `mvn verify` |
| 生产环境 | 正式运行 | PostgreSQL + PostGIS 16 | `java -jar app.jar --spring.profiles.active=prod` |

### 8.2 Docker部署

采用多阶段构建，减少最终镜像体积：

```dockerfile
# Stage 1: 构建
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -B -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: 运行
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S tingcheng && adduser -S tingcheng -G tingcheng
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
USER tingcheng
EXPOSE 8092
ENV SPRING_PROFILES_ACTIVE=dev SERVER_PORT=8092
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> 构建后的 JAR 在 `target/*.jar`，通过通配符复制到运行镜像。

### 8.3 Docker Compose

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8092:8092"
    volumes:
      - uploads:/app/data/uploads
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SERVER_PORT=8092

  # 生产环境使用 PostgreSQL:
  # db:
  #   image: postgis/postgis:16-3.4
  #   environment:
  #     - POSTGRES_DB=tingchenggis_pg
  #     - POSTGRES_USER=postgres
  #     - POSTGRES_PASSWORD=${TINGCHENG_DB_PASSWORD}
  #   volumes:
  #     - postgres_data:/var/lib/postgresql/data

volumes:
  uploads:
  # postgres_data:
```

> 说明：默认开发配置（H2）无需数据库容器。生产环境启用 PostgreSQL 时需要取消注释 `db` 服务并设置密码环境变量。

---

## 九、测试设计

### 9.1 测试策略

| 测试类型 | 范围 | 工具 | 覆盖率目标 |
|---------|------|------|---------|
| 单元测试 | Service + Util 层 | JUnit 5 + Mockito | 70% Instruction（JaCoCo check） |
| 前端测试 | 模块化 JS 方法 | Vitest + jsdom | — |
| E2E 测试 | 核心用户流程 | Playwright | 5 个核心场景 |
| 性能测试 | API 基准 | JMeter（可选） | — |

JaCoCo 覆盖率在 `mvn verify` 阶段强制执行：`<counter>INSTRUCTION</counter><minimum>0.70</minimum>`。

### 9.2 测试框架

- 后端单元测试：JUnit 5 + Mockito + Spring Boot Test（**350 个测试用例**，0 失败）
- 前端测试：Vitest 2.1 + jsdom 25（**50 个测试用例**）
- E2E 测试：Playwright（**5 个场景**：页面加载、亭子数据、地图展示、路线、环境准备）
- CI 集成：GitHub Actions 自动执行 `mvn verify` + Playwright E2E

---

## 十、附录

### 10.1 参考文档

- [需求报告](../需求/TingChengGIS需求报告.md)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [PostGIS文档](https://postgis.net/documentation/)
- [JTS文档](https://locationtech.github.io/jts/)
- [Leaflet](https://leafletjs.com/)
- [CesiumJS](https://cesium.com/platform/cesiumjs/)

### 10.2 设计文档更新记录

| 版本 | 日期 | 更新内容 | 更新人 |
|-----|------|---------|-------|
| v1.0.0 | 2026-06-04 | 初始版本 | 系统设计组 |
