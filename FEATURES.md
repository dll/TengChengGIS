# 滁州亭城GIS系统功能说明

## 已实现功能

### 1. 遍历千亭功能
- **标注每个亭子的位置**：在2D和3D地图上显示亭子位置
- **亭间交通**：计算亭子之间的路线和距离
- **遍历所有亭子的路线图**：支持多点路径规划
- **导航方案**：提供导航和路线指引
- **各亭的多媒体导航**：提供视频、音频、图片等多媒体内容
- **路线导航**：可视化路径规划
- **旅游服务**：综合旅游信息服务

### 2. 2D/3D地图视图
- **2D地图**：使用Leaflet.js实现传统地图视图
- **3D地图**：使用Cesium.js实现3D地球视图
- **视图切换**：支持一键切换2D/3D视图
- **数据同步**：确保2D/3D视图中数据一致

### 3. 亭子数据管理
- **亭子信息显示**：包括名称、类型、描述、评分等
- **分类管理**：历史文化亭、现代景观亭、文化主题亭
- **空间数据**：经纬度坐标及空间关系

### 4. 导游服务
- **路线规划**：支持多点路径规划和最优路线推荐
- **智能导览**：AI导游功能
- **多媒体展示**：视频、音频、图片等多媒体内容

### 5. 用户交互
- **数据表格**：展示亭子信息的表格视图
- **地图交互**：点击地图元素获取详细信息
- **路径显示**：可视化展示选择的路径
- **搜索筛选**：支持按类型筛选亭子

## API端点

### 亭子相关
- `GET /thousand-pavilions/locations` - 获取所有亭子位置（支持 `type`、`search` 筛选）
- `GET /thousand-pavilions/route/{fromId}/{toId}` - 获取两亭之间的路线
- `GET /thousand-pavilions/multimedia/{id}` - 获取亭子多媒体信息
- `GET /thousand-pavilions/traverse-all` - 遍历所有亭子路线
- `GET /thousand-pavilions/optimal-route` - 最优遍历路线（TSP）
- `GET /thousand-pavilions/smart-tour` - 智能游览规划
- `GET /thousand-pavilions/tourism-services` - 旅游服务信息
- `POST /thousand-pavilions/share-route` - 生成可分享路线
- `GET /thousand-pavilions/weather` - 天气信息
- `GET /thousand-pavilions/nearby-facilities/{id}` - 附近设施
- `GET /thousand-pavilions/vr-experience/{id}` - VR 体验信息
- `POST/PUT/DELETE /thousand-pavilions` - 亭子增删改

### AI 服务
- `GET /ai/culture-intro` - 文化介绍
- `POST /ai/ask` - 智能问答
- `GET /ai/culture-overview` - 文化概览

### GIS 分析
- `GET /pavilions-gis/optimal-path` - 多点最优路径
- `GET /transport-routes` - 交通线数据

## 技术栈

- **后端**：Spring Boot 3.x, Java 21
- **数据库**：PostgreSQL/PostGIS (生产环境), H2 (开发环境)
- **前端**：HTML5, CSS3, JavaScript, Bootstrap 5
- **地图**：Leaflet.js (2D), Cesium.js (3D)
- **构建工具**：Maven

## 运行方式

```bash
mvn spring-boot:run
```

访问 http://localhost:8092 查看应用

## 项目特色

1. 结合《醉翁亭记》文化背景，打造滁州亭城GIS系统
2. 支持2D/3D地图视图切换
3. 提供全面的亭子信息管理和导航服务
4. 集成AI导游功能
5. 多媒体内容展示