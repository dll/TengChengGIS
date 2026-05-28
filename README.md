# 滁州亭城GIS系统 (TingChengGIS)

![CI](https://github.com/dll/TengChengGIS/actions/workflows/ci.yml/badge.svg)

滁州亭城GIS系统是一个综合性的地理信息系统，将先进的GIS技术与滁州深厚的"亭城"文化相结合。该系统融合了空间信息技术、大数据分析和人工智能，专注于滁州独特的亭文化资源展示与管理。

> 📖 **运维手册**：详细的生产部署、CI/CD 配置、数据库备份等请参见 [`运维手册.md`](运维手册.md)。

## 项目概述

本项目旨在通过现代GIS技术展现滁州作为"亭城"的文化魅力，特别是围绕欧阳修《醉翁亭记》所体现的历史文化底蕴。系统不仅具备基本的GIS功能，还集成了AI文化讲解功能，为用户提供沉浸式的文化体验。

## 综合功能特性

### 1. 技术实现层 (Technical Layer)
- **Spring Boot**: 构建现代化Web应用
- **PostgreSQL/PostGIS**: 存储空间数据
- **JTS Topology Suite**: 处理空间数据
- **GDAL**: 处理地理数据
- **RESTful API**: 提供标准化接口
- **空间分析功能**:
  - 距离计算
  - 缓冲区分析
  - 空间关系查询
  - 热力图生成
  - 密度分析
  - 最短路径计算

### 2. 文化内容层 (Cultural Content Layer)
- **滁州亭文化数据库**: 包含醉翁亭、丰乐亭等历史名亭
- **《醉翁亭记》文化元素**: 深度融入经典文学作品
- **AI智能文化讲解**: 基于大模型的文化内容生成
- **智能推荐系统**: 个性化游览路线推荐

## 核心功能

### 1. 亭子信息管理
- 添加、编辑、删除亭子信息
- 亭子基本信息：名称、中文名、描述、历史意义等
- 空间位置信息：经纬度坐标、几何形状等
- 文化属性：建筑风格、建造年代、开放情况等

### 2. 空间查询与分析
- 按类型、名称、年代等条件查询
- 地理位置范围查询
- 亭子统计分析
- 文化内容智能生成

### 3. AI智能服务
- 基于《醉翁亭记》文化背景的智能介绍生成
- 亭子历史故事创作
- 游览路线智能推荐
- 文化知识问答

### 4. 空间分析功能
- 距离计算
- 缓冲区分析
- 空间关系查询
- 热力图生成
- 密度分析
- 最短路径计算

### 5. 遍历千亭功能
- **标注每个亭子的位置**：在2D和3D地图上显示亭子位置
- **亭间交通**：计算亭子之间的路线和距离
- **遍历所有亭子的路线图**：支持多点路径规划
- **导航方案**：提供导航和路线指引
- **各亭的多媒体导航**：提供视频、音频、图片等多媒体内容
- **路线导航**：可视化路径规划
- **旅游服务**：综合旅游信息服务

### 6. 2D/3D地图视图
- **2D地图**：使用Leaflet.js实现传统地图视图
- **3D地图**：使用Cesium.js实现3D地球视图
- **视图切换**：支持一键切换2D/3D视图
- **数据同步**：确保2D/3D视图中数据一致
- **交互功能**：点击地图元素获取详细信息，选择亭子进行路径规划

## 项目结构

```
TingChengGIS/
├── src/main/java/com/tingchenggis/tingcheng/
│   ├── controller/     # 控制器层
│   ├── entity/         # 实体类
│   ├── repository/     # 数据访问层
│   ├── service/        # 业务逻辑层
│   │   └── impl/       # 业务逻辑实现
│   ├── dto/            # 数据传输对象
│   ├── ai/             # AI服务层
│   ├── config/         # 配置类
│   └── TingChengGISTingChengApplication.java  # 主应用类
├── src/main/resources/
│   └── application.yml # 配置文件
├── pom.xml             # Maven依赖配置
└── README.md           # 项目说明
```

## 系统架构

```
┌─────────────────────────────────────┐
│           应用展示层                 │
├─────────────────────────────────────┤
│  Web界面 | 移动端 | API接口          │
├─────────────────────────────────────┤
│           业务逻辑层                 │
├─────────────────────────────────────┤
│  亭子管理 | 空间分析 | AI服务        │
├─────────────────────────────────────┤
│           技术支撑层                 │
├─────────────────────────────────────┤
│  Spring Boot | JTS | PostGIS | AI   │
├─────────────────────────────────────┤
│           数据存储层                 │
├─────────────────────────────────────┤
│       PostgreSQL/PostGIS            │
└─────────────────────────────────────┘
```

## API接口

### 亭子管理接口
- `GET /pavilions` - 获取所有亭子（分页）
- `GET /pavilions/{id}` - 根据ID获取特定亭子
- `POST /pavilions` - 创建新的亭子
- `PUT /pavilions/{id}` - 更新亭子信息
- `DELETE /pavilions/{id}` - 删除亭子

### GIS查询接口
- `GET /pavilions-gis/distance/{id1}/{id2}` - 计算两亭距离
- `GET /pavilions-gis/nearest?lat=&lng=&limit=` - 查询最近亭子
- `GET /pavilions-gis/heatmap` - 热力图数据
- `POST /pavilions-gis/density` - 密度分析

### 千亭综合接口 (Thousand Pavilions)
- `GET /thousand-pavilions/locations` - 所有亭子位置
- `GET /thousand-pavilions/route/{fromId}/{toId}` - 两亭间路线
- `GET /thousand-pavilions/traverse-all` - 遍历所有亭子的最优路线
- `GET /thousand-pavilions/optimal-route` - TSP 最优路径
- `POST /thousand-pavilions/navigation-plan` - 导航方案
- `GET /thousand-pavilions/navigation/{fromId}/{toId}` - 导航指引
- `POST /thousand-pavilions/multi-route` - 多路线规划
- `POST /thousand-pavilions/import` - 上传Excel导入千亭数据
- `GET /thousand-pavilions/export/geojson` - 导出GeoJSON
- `GET /thousand-pavilions/smart-tour` - 智能游览推荐

### AI服务接口
- `GET /ai/chat?question=&pavilionName=` - AI智能对话
- `GET /ai/pavilion/{pavilionId}` - 亭子AI文化介绍
- `POST /ai/tour-advice` - 游览建议生成
- `GET /ai/status` - AI服务状态

### 交通路线接口
- `GET /transport-routes/from/{pavilionId}` - 某亭出发路线
- `GET /transport-routes/between/{id1}/{id2}` - 两亭间交通
- `GET /transport-routes/by-mode/{mode}` - 按交通方式查询
- `POST /transport-routes/tsp-plan` - TSP多亭路线规划
- `POST /transport-routes/build-network` - 构建路网（管理员）
- `POST /transport-routes/build-multi-modal` - 多模式路网（管理员）

### 导航接口
- `GET /nav/turn-by-turn/{fromId}/{toId}` - 逐向导航
- `GET /nav/turn-by-turn/coords?fromLng=&fromLat=&toLng=&toLat=` - 坐标导航

### VR/AR体验接口
- `GET /vr-ar/experience/{pavilionId}` - VR体验数据
- `GET /vr-ar/ar-overlay/{pavilionId}` - AR叠加数据
- `GET /vr-ar/3d-scene/{pavilionId}` - 3D场景数据

### 认证接口
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `POST /auth/change-password` - 修改密码
- `GET /auth/me` - 当前用户信息

### 辅助功能接口
- `POST /api/upload/photo` - 上传图片
- `GET /api/upload/file/**` - 获取上传文件
- `POST /coordinate/transform` - 坐标转换 (WGS-84 ↔ GCJ-02)
- `POST /coordinate/correct-pavilions` - 批量纠正坐标（管理员）
- `POST /ogc/wms/map` - OGC WMS地图请求
- `POST /ogc/wfs/features` - OGC WFS要素查询
- `POST /osm/import/all` - 导入所有OSM数据
- `POST /osm/import/scenic` - 导入景区数据

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+
- Docker (可选，用于容器化部署)

### 本地运行
1. 克隆项目
   ```bash
   git clone <repository-url>
   cd TingChengGIS
   ```

2. 配置AI API密钥（可选，默认使用内置模板）
   在 `application.yml` 中设置 `tingcheng.ai.deepseek-api-key` 或通过环境变量 `TINGCHENG_AI_DEEPSEEK_API_KEY` 注入

3. 构建项目
   ```bash
   mvn clean install
   ```

4. 运行应用
   ```bash
   mvn spring-boot:run
   ```

5. 访问应用
   - 应用启动后，默认访问地址: http://localhost:8092
   - H2数据库控制台: http://localhost:8092/h2-console
   - API端点示例: http://localhost:8092/thousand-pavilions/locations

## 特色功能展示

### AI文化讲解
系统能够基于《醉翁亭记》的文化背景，为每个亭子生成富含文化内涵的介绍，让用户深入了解滁州"亭城"的历史底蕴。

### 智能游览规划
根据用户的兴趣偏好和时间安排，系统能够推荐最佳的亭子游览路线，结合季节特色提供个性化的游览建议。

### 空间数据分析
通过GIS技术，系统可以分析亭子在滁州的分布规律，展示不同历史时期亭子建设的特点，为文化研究提供数据支持。

## 滁州亭城文化特色

### 历史底蕴
- **醉翁亭**: 因欧阳修《醉翁亭记》而闻名天下
- **丰乐亭**: 欧阳修任滁州知州时所建
- **琅琊山**: "环滁皆山也"的真实写照

### 文化价值
- **文学价值**: 传承《醉翁亭记》的文化内涵
- **历史价值**: 展现宋代文化繁荣景象
- **旅游价值**: 促进滁州文化旅游发展

## 应用场景

1. **文化旅游**: 为游客提供滁州亭子文化的深度体验
2. **教育科研**: 为研究滁州历史文化提供数字化平台
3. **城市规划**: 为滁州"亭城"品牌建设提供数据支撑
4. **文化传播**: 通过现代技术手段推广滁州传统文化

## 项目意义

本系统是技术与文化的完美结合，既展现了现代GIS技术的强大功能，又传承和弘扬了滁州深厚的历史文化底蕴。通过空间信息技术与传统文化的融合，为用户提供了全新的文化体验方式，实现了：

1. **技术创新**: 将GIS技术应用于文化领域
2. **文化传承**: 保护和传播滁州亭城文化
3. **教育价值**: 为学生和研究者提供学习平台
4. **社会价值**: 促进文化旅游和经济发展

## 贡献

欢迎提交Issue和Pull Request来改进项目，特别是关于滁州历史文化内容的补充和完善。

## 许可证

本项目采用 MIT 许可证。

## 致谢

本项目灵感来源于欧阳修《醉翁亭记》，致敬这位伟大的文学家及其为滁州留下的宝贵文化遗产。