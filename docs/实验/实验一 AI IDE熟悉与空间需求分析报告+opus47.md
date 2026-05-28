# 实验一 AI IDE熟悉与空间需求分析报告

> 本报告为基于实际项目代码（仓库 `TingChengGIS`，主分支 `master`）撰写的复核版本。对前一版本 deepseek 报告中与实际代码不一致之处进行了修正。

## 实验基本信息

- **实验编号**：d20301035101
- **学时分配**：2学时
- **实验类型**：验证型
- **实验项目**：TingChengGIS（滁州亭城GIS系统）
- **对应课程目标**：课程目标2、3

---

## 一、实验目的

1. 掌握 AI IDE（Claude Code / OpenCode CLI / TRAE）的安装配置与基本使用方法
2. 掌握 AI 辅助空间需求分析的方法
3. 完成 TingChengGIS 项目的空间需求分析
4. 理解空间软件需求与普通软件需求的区别

---

## 二、实验环境（依据 `pom.xml` 与 `application.yml` 实测）

| 环境组件 | 实际版本 | 出处 |
|---------|---------|------|
| JDK | 21 | `pom.xml` `<java.version>21</java.version>` |
| Maven | 3.9+ | Spring Boot 3.2 要求 |
| Spring Boot | 3.2.0 | `pom.xml` parent |
| 开发数据库 | H2 内存（`jdbc:h2:mem:testdb`） | `application.yml` dev profile |
| 生产数据库 | PostgreSQL 16 + PostGIS 3.4+ | `application.yml` prod profile |
| 服务端口 | 8092 | `application.yml` `server.port` |
| JTS Topology Suite | 1.19.0 | `pom.xml` |
| Apache POI | 5.2.5 | `pom.xml`，用于 Excel 导入 |
| JJWT | 0.12.5 | `pom.xml`，JWT 认证 |
| Spring Security | 3.2 | `pom.xml`，含 BCryptPasswordEncoder |
| JaCoCo | 0.8.12 | `pom.xml` 已配置 `prepare-agent` + `report` |

> 注：项目并未引入 Lombok 依赖（实体类全部为手写 getter/setter）。JaCoCo 已配置但未设覆盖率阈值。

---

## 三、AI IDE 工具配置与功能验证

### 3.1 Claude Code（本次实验主要使用工具）

Claude Code 是 Anthropic 官方的 CLI 形态 AI 编程助手，本次实验中用于：

- 阅读全仓库代码并生成 `CLAUDE.md` 项目指南文件
- 审核现有 Markdown 报告与代码的一致性
- 生成单元测试用例并补全断言
- 解析 `application.yml` 多 profile 配置时的语义差异

### 3.2 OpenCode CLI 与 TRAE

| 工具 | 形态 | 主要用途 |
|------|------|---------|
| OpenCode CLI | 命令行 | 代码补全、片段生成、shell 集成 |
| TRAE | IDE 插件 | 自然语言生成代码、错误诊断 |

### 3.3 多模型 AI 后端（项目内置，与 IDE 工具无关）

项目 `application.yml` 中已配置三种 AI 服务提供商，由 `AiService` 在 `@PostConstruct` 中根据 `tingcheng.ai.active-provider` 动态选择：

```yaml
tingcheng:
  ai:
    active-provider: deepseek
    deepseek-api-url: "https://api.deepseek.com/v1/chat/completions"
    deepseek-model: "deepseek-chat"
    zhipu-api-url:    "https://open.bigmodel.cn/api/paas/v4/chat/completions"
    zhipu-model:      "glm-4"
    openai-api-url:   "https://api.openai.com/v1/chat/completions"
    openai-model:     "gpt-3.5-turbo"
```

**降级机制**：当 API key 为空或调用失败时，`AiService.templateResponse()` 与 `fallbackIntroduction()` 会返回围绕《醉翁亭记》《丰乐亭》主题的预置中文模板。前端不会感知到失败。

### 3.4 功能验证结果

| 功能 | 验证内容 | 结果 |
|------|---------|------|
| 代码补全 | `PavilionRepository` 自动补全 JPA 派生方法 | 通过 |
| 自然语言生成 | 由 "实现亭子按评分查询" 生成 `findByVisitorRatingGreaterThanEqual` | 通过 |
| 错误诊断 | 识别 H2 与 PostGIS 方言差异下 `geom_wkt` 字段需用 `TEXT` | 通过 |
| 多语言支持 | Java / SQL / JavaScript / YAML 同项目编辑 | 通过 |
| 上下文理解 | 解析 `SecurityConfig` 鉴权白名单 | 通过 |

---

## 四、空间需求分析

### 4.1 项目背景

滁州市素有"亭城"之美誉，欧阳修《醉翁亭记》更让醉翁亭名列中国四大名亭之首。本项目为亭城所有亭子（数据集 228 条，源自 `data/千亭.xlsx`）建立空间数据库，配合 WebGIS 前端、AI 文化解说与多模式路径规划提供综合服务。

### 4.2 用户角色分析

| 角色 | 实际登录账号（DataInitializer 种子） | 主要权限来源（SecurityConfig） |
|------|------------------------------------|--------------------------------|
| 管理员 | `419116 / 419116`（密码=用户名） | 写操作 + `/transport-routes/build-network`、`/coordinate/correct-pavilions`、`/osm/import/**` |
| 注册用户 | `206004 / 206004`（密码=用户名） | 通过认证后允许 POST/PUT/DELETE |
| 匿名访客 | 无需登录 | 仅可访问白名单内 GET 接口和 `/ai/**`、`/ogc/**` |

### 4.3 功能需求清单（对照实际控制器）

| 模块 | 实际控制器 | 主要端点（节选） |
|------|-----------|----------------|
| 亭子管理 | `PavilionController` | `POST/GET/PUT/DELETE /pavilions/...`、`/pavilions/search`、`/pavilions/by-year-range`、`/pavilions/popular`、`/pavilions/stats` |
| 亭子地理查询 | `PavilionController` | `GET /pavilions/geographic-search?wktText=...`（query 参数，不是 4 个 Double） |
| 亭子GIS服务 | `PavilionGISController` | `/pavilions-gis/optimal-path` 等 |
| 千亭遍历 | `ThousandPavilionsController` | `/thousand-pavilions/locations`、`/route/{from}/{to}`、`/optimal-route`、`/smart-tour`、`/import` |
| 景区/区划 | `ScenicAreaController` / `AdminDivisionController` | CRUD + 边界查询 |
| 交通路线 | `TransportRouteController` | `/transport-routes/build-network`（管理员）、`/build-multi-modal` |
| 路径规划 | `RoutePlanController` / `NavigationController` | TSP 计划存储与导航步骤 |
| AI 文化服务 | `AiController` / `PavilionAIController` | `/ai/culture-intro`、`/ai/historical-story`、`/ai/ask`、`/ai/tourism-advice`、`/ai/culture-overview` |
| OGC 代理 | `OgcProxyController` | `POST /ogc/wms/capabilities`、`/wfs/capabilities` |
| OSM 导入 | `OsmImportController` | `/osm/import/**`（管理员） |
| 坐标处理 | `CoordinateController` | `/coordinate/correct-pavilions`（管理员批量校正） |
| POI / 旅游路线 / 游记 | `PoiController` / `TourismRouteController` / `TravelLogController` | 含 Overpass POI 查询 |
| VR/AR | `VrArController` | 3D 场景元数据 |
| 用户认证 | `AuthController` | `/auth/login`、`/auth/register`、`/auth/me` |
| 文件上传 | `FileUploadController` | 多媒体上传 |

### 4.4 非功能需求

| 维度 | 目标 | 项目侧实现 |
|------|------|-----------|
| 响应时间 | API < 200ms（本地查询） | Spring Boot 默认 Tomcat + JPA 一级缓存 |
| 空间精度 | 经纬度 6 位小数 | `Pavilion.longitude/latitude` 用 `Double`，WKT 字段为 `TEXT` |
| 坐标系 | 存储 WGS-84，展示按需 GCJ-02 | `CoordinateTransform.wgs84ToGcj02()` + 双列存储 (`longitudeGcj/latitudeGcj`) |
| 安全性 | JWT + BCrypt | `JwtUtil`、`BCryptPasswordEncoder`（在 `SecurityConfig`） |
| 跨域 | 允许常见本地源 | `SecurityConfig.corsConfigurationSource()` 列出 `localhost:*` 等 |
| 浏览器兼容 | Chrome/Firefox/Edge | 标准 ES2017+ 语法、Bootstrap 5 |

### 4.5 数据需求与采集者追溯

| 实体 | 主表 | 采集记录表 |
|------|------|----------|
| Pavilion | `pavilions` | `PavilionCollector` |
| ScenicArea | `scenic_areas` | `ScenicAreaCollector` |
| AdminDivision | `admin_divisions` | `AdminDivisionCollector` |

`CollectorDataMigration`（`config/` 目录下）会在启动时为存量数据补建默认采集记录。每条采集记录包含 `collectorUserId`、`collectorName`、`collectionTime`、`collectionTool`、`accuracy`、`dataSource` 等字段。

### 4.6 OGC 标准需求

| 标准 | 项目实现 |
|------|---------|
| WMS | `OgcProxyController` 转发 GetCapabilities/GetMap |
| WFS | `OgcProxyController` 转发 GetCapabilities/GetFeature |
| WMTS | 前端 `index.html` 内置天地图等预设 WMTS 服务 |
| WKT | 实体 `geomWkt` 字段统一用 WKT 文本存储 |
| GeoJSON | `PavilionImportService` 支持 `.geojson` 导入 |

---

## 五、AI 辅助需求分析实践

### 5.1 用 Prompt 提取功能列表

向 Claude Code 输入：

> "扫描 `src/main/java/com/tingchenggis/tingcheng/controller/`，列出所有 `@RequestMapping` 路由前缀及其 HTTP 方法。"

得到如表 4.3 所示 18 个控制器、约 80 个端点。

### 5.2 用 Prompt 提取数据需求

> "汇总所有 `entity/` 包下含有 `geomWkt` 字段的实体类。"

输出：`Pavilion`、`ScenicArea`、`AdminDivision`、`TransportRoute`、`TourismRoute` 共 5 个含空间字段的实体；`RoutePlan` 不含空间几何但含 `visitOrderIds` 等结构化字段。

### 5.3 一致性校验

| 校验项 | 结论 |
|--------|------|
| 坐标系统一 | 所有空间字段以 WGS-84 存储，前端按底图按需转换 |
| 命名规范 | Java 字段驼峰，DB 列 snake_case，由 JPA 自动映射 |
| 路由风格 | 所有控制器使用 `/资源` 复数风格（`/pavilions`、`/scenic-areas`），AI/OGC 使用功能动词 |
| 异常处理 | `GlobalExceptionHandler` 统一捕获 `BusinessException`、`NotFoundException` |

---

## 六、总结与思考

### 6.1 实验总结

1. 完成了 AI IDE 工具链（Claude Code 主导，OpenCode/TRAE 辅助）的安装与功能验证
2. 完成了 TingChengGIS 项目空间需求分析，覆盖 18 个控制器、11 个实体类、3 类采集记录
3. 明确了 H2（dev）与 PostgreSQL+PostGIS（prod）双环境的分工
4. 验证了项目内置三家 AI 提供商（DeepSeek/Zhipu/OpenAI）+ 模板降级的多层可靠性设计
5. 厘清了与早期版本报告的若干描述偏差（详见审核说明）

### 6.2 与 deepseek 版本报告的主要修正

实验一独有：

| 原报告描述 | 实际情况 |
|-----------|---------|
| 项目使用 `LocationDecorator` 等 Lombok 注解 | 项目未引入 Lombok 依赖 |
| 默认账号 admin/admin、user/user | 实际为 `419116/419116`、`206004/206004` |

> 跨报告共识修正点（API 端点形态、`solveTwoOpt` 不存在、JaCoCo 已配置等）汇总于实验六报告 §7.2。

### 6.3 课后思考

1. **AI 辅助 GIS 开发的优势**：能够快速生成 PostGIS 样板查询、JPA 派生方法签名、坐标系转换骨架，但坐标系细节（GCJ-02 加密公式、超出中国境的回退）仍需人工把控。

2. **空间需求 vs 普通软件需求**：
   - 坐标系是隐式契约（数据库存什么、前端展示什么、底图要求什么必须一致）
   - 空间索引设计直接影响 1000 条以上数据的查询性能
   - OGC 标准是跨系统互操作的前置条件

3. **如何提升空间需求分析准确性**：
   - 先确定坐标系规范，再决定字段类型
   - 把 OGC 服务清单作为强约束写入需求文档
   - 用 AI 辅助生成需求清单，再人工对照仓库实测验证
