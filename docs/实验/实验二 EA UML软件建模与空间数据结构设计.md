# 实验二EA UML软件建模与空间数据结构设计

## 实验项目基本信息

- **实验编号**：d20301035102
- **学时分配**：2学时
- **实验类型**：设计型
- **每组人数**：5人
- **对应课程目标**：课程目标2

***

## 🎯 核心步骤列表

### 📋 实验概览

1. **环境准备阶段**：安装EA UML软件和开发环境
2. **界面熟悉阶段**：掌握EA UML基本操作
3. **面向对象设计阶段**：完成用例图、类图、时序图、包图
4. **数据库设计阶段**：完成空间数据库概念设计和逻辑设计

### 🛠️ 工具清单

| 序号 | 开发工具       | 主要用途           | 验证项目             |
| -- | ---------- | -------------- | ---------------- |
| 1  | EA UML     | UML建模工具        | 用例图、类图、时序图、包图绘制  |
| 2  | PostgreSQL | 关系型数据库         | 表结构设计、空间数据存储     |
| 3  | PostGIS    | PostgreSQL空间扩展 | 空间数据类型、空间索引、空间查询 |
| 4  | TRAE       | AI辅助编程         | 代码生成、错误诊断        |
| 5  | Opencode   | AI辅助编程         | 代码补全、智能提示        |

**亭城GIS子项目开发环境要求：**

| 环境组件        | 版本要求  | 说明                            |
| ----------- | ----- | ----------------------------- |
| JDK         | 21+   | Java开发工具包，支持Spring Boot 3.1.0 |
| Maven       | 3.9+  | 项目构建工具，管理项目依赖                 |
| PostgreSQL  | 16+   | 关系型数据库，存储亭子信息                 |
| PostGIS     | 3.4+  | PostgreSQL的空间扩展，支持空间查询        |
| Spring Boot | 3.1.0 | Java应用框架，快速开发Web应用            |
| TRAE        | 最新版本  | AI辅助编程工具，提高开发效率               |

### ✅ 成功标准

- [ ] EA UML软件成功安装并正常运行
- [ ] 开发环境配置完成（JDK 21、Maven 3.9、PostgreSQL 16、PostGIS 3.4）
- [ ] 面向对象设计完成（用例图、类图、时序图、包图）
- [ ] 空间数据库设计完成（概念设计、逻辑设计）
- [ ] 设计文档提交

***

## 1. EA UML软件安装与配置

### 1.1 软件安装

1. 下载EA UML安装包
2. 运行安装程序
3. 完成许可证激活

### 1.2 界面配置

1. 项目浏览器使用
2. 工具箱使用
3. 属性窗口使用
4. 图表窗口使用

***

## 2. 面向对象设计

### 2.1 用例图绘制

**2.1.1 参与者识别**

- 普通用户：系统的普通使用者，查询亭子、查看地图、获取AI服务
- 系统管理员：系统的管理维护者，管理亭子信息、维护系统数据
- AI服务：外部AI服务提供者，提供文化介绍生成、路线推荐服务

**2.1.2 用例识别**

**普通用户用例：**

- UC01 查看地图：用户查看基础地图和亭子标记
- UC02 搜索亭子：用户按关键词搜索亭子
- UC03 查看亭子详情：用户查看亭子的详细信息
- UC04 计算距离：用户计算两点或两亭子间距离
- UC05 范围查询：用户查询指定范围内的亭子
- UC06 获取文化介绍：用户获取AI生成的文化介绍
- UC07 获取路线推荐：用户获取游览路线推荐

**系统管理员用例：**

- UC08 添加亭子：管理员添加新的亭子信息
- UC09 编辑亭子：管理员编辑已有亭子信息
- UC10 删除亭子：管理员删除亭子信息

**2.1.3 用例关系**

- 包含关系：UC06获取文化介绍 include UC03查看亭子详情
- 包含关系：UC07获取路线推荐 include UC05范围查询
- 扩展关系：UC03查看亭子详情 extend UC06获取文化介绍

### 2.2 类图绘制

**2.2.1 类识别**

**实体类：**

- Pavilion：亭子实体类，包含亭子的所有属性信息
- Coordinate：坐标类，存储经纬度信息
- User：用户类，存储用户基本信息
- SearchHistory：搜索历史类，记录用户搜索行为
- Route：路线类，存储AI推荐的游览路线

**控制类：**

- PavilionController：亭子管理控制器，处理亭子相关的HTTP请求
- SpatialController：空间查询控制器，处理空间查询请求
- AIController：AI服务控制器，处理AI服务请求

**服务类：**

- PavilionService：亭子管理服务，处理亭子业务逻辑
- SpatialService：空间查询服务，处理空间计算和查询
- AIService：AI智能服务，调用AI接口生成内容

**数据访问类：**

- PavilionRepository：亭子数据访问，负责数据库操作

**2.2.2 类关系设计**

- Pavilion包含Coordinate：组合关系，坐标不能独立存在
- PavilionController依赖PavilionService：依赖关系
- PavilionService依赖PavilionRepository：依赖关系
- PavilionService依赖Pavilion：依赖关系
- SpatialService依赖Pavilion：依赖关系
- AIService依赖Pavilion：依赖关系
- User关联SearchHistory：关联关系，一个User可以有多个SearchHistory

**2.2.3 类图元素**

**Pavilion类：**

- 属性：id: Long, name: String, chineseName: String, type: String, buildYear: Integer, length: Double, width: Double, height: Double, area: Double, material: String, status: String, historicalValue: Integer, artisticValue: Integer, description: String, longitude: Double, latitude: Double
- 方法：getId(), getName(), setName(), getCoordinate(), setCoordinate()

**Coordinate类：**

- 属性：longitude: Double, latitude: Double
- 方法：getLongitude(), getLatitude(), setLongitude(), setLatitude()

### 2.3 时序图绘制

**2.3.1 搜索亭子时序图**

1. 用户输入搜索关键词
2. PavilionController接收searchPavilions(keyword)请求
3. PavilionService调用searchPavilions(keyword)
4. PavilionRepository执行findByKeyword(keyword)
5. PavilionRepository返回List<Pavilion>
6. PavilionService返回List<Pavilion>
7. PavilionController返回搜索结果
8. 用户显示搜索结果

**2.3.2 范围查询时序图**

1. 用户选择中心点和半径
2. SpatialController接收findWithinRadius(longitude, latitude, radius)请求
3. SpatialService调用findPavilionsWithinRadius(center, radius)
4. PavilionRepository执行findByLocation(longitude, latitude, radius)
5. PavilionRepository返回List<Pavilion>
6. SpatialService返回List<Pavilion>
7. SpatialController返回查询结果
8. 用户显示范围内亭子

**2.3.3 获取文化介绍时序图**

1. 用户点击获取文化介绍
2. AIController接收generateCultureIntro(pavilionId)请求
3. PavilionService调用getPavilionById(id)
4. PavilionRepository执行findById(id)
5. PavilionRepository返回Pavilion
6. AIService调用generateCultureIntro(name, location, history)
7. AI服务执行API调用
8. AI服务返回文化介绍
9. AIService返回介绍内容
10. AIController返回文化介绍
11. 用户显示文化介绍

### 2.4 包图绘制

**2.4.1 包结构设计**

- controller包：控制器层，包含PavilionController、SpatialController、AIController
- service包：服务层，包含PavilionService、SpatialService、AIService
- repository包：数据访问层，包含PavilionRepository
- entity包：实体类，包含Pavilion、Coordinate、User、SearchHistory、Route
- dto包：数据传输对象，包含PavilionDto、RouteDto、SearchResultDto
- util包：工具类，包含CoordinateUtil、DistanceCalculator

**2.4.2 分层架构**

- 表示层（controller）：接收请求，返回响应，依赖业务层
- 业务层（service）：处理业务逻辑，依赖数据层
- 数据层（repository）：数据访问和持久化，依赖实体层
- 实体层（entity）：定义数据实体，无依赖

## 3. 数据库设计

### 3.1 空间数据库设计

#### 3.1.1 实体转化

将面向对象设计中的类转换为数据库表：

- Pavilion类 → pavilion表
- User类 → user表
- SearchHistory类 → search\_history表
- Route类 → route表
- Route与Pavilion的多对多关系 → route\_pavilion关联表

#### 3.1.2 属性转化

将类的属性转换为表的字段：

- Pavilion的属性 → pavilion表的字段
- User的属性 → user表的字段
- SearchHistory的属性 → search\_history表的字段
- Route的属性 → route表的字段

#### 3.1.3 关系转化

将类关系转换为外键约束：

- User与SearchHistory的一对多关系 → search\_history表添加user\_id外键
- Route与Pavilion的多对多关系 → route\_pavilion关联表

#### 3.2.1 表结构设计

**亭子表（pavilion）：**

| 字段名               | 数据类型                  | 描述                | 约束            |
| ----------------- | --------------------- | ----------------- | ------------- |
| id                | SERIAL                | 亭子ID              | PRIMARY KEY   |
| name              | VARCHAR(255)          | 亭子名称              | NOT NULL      |
| chinese\_name     | VARCHAR(255)          | 亭子中文名             | NOT NULL      |
| type              | VARCHAR(50)           | 亭子类型（亭、阁、楼、台）     | NOT NULL      |
| build\_year       | INTEGER               | 建造年代              | <br />        |
| length            | DECIMAL(6,2)          | 亭子长度（米）           | <br />        |
| width             | DECIMAL(6,2)          | 亭子宽度（米）           | <br />        |
| height            | DECIMAL(6,2)          | 亭子高度（米）           | <br />        |
| area              | DECIMAL(10,2)         | 亭子面积（平方米）         | <br />        |
| material          | VARCHAR(50)           | 亭子材质              | <br />        |
| status            | VARCHAR(20)           | 亭子状态（完好、一般、破损、重建） | <br />        |
| historical\_value | INTEGER               | 历史价值评分（1-5分）      | CHECK(1-5)    |
| artistic\_value   | INTEGER               | 艺术价值评分（1-5分）      | CHECK(1-5)    |
| description       | TEXT                  | 亭子描述              | <br />        |
| longitude         | DOUBLE PRECISION      | 经度（WGS84，6位小数）    | NOT NULL      |
| latitude          | DOUBLE PRECISION      | 纬度（WGS84，6位小数）    | NOT NULL      |
| geom              | GEOMETRY(Point, 4326) | 空间几何字段（PostGIS）   | NOT NULL      |
| created\_at       | TIMESTAMP             | 创建时间              | DEFAULT NOW() |
| updated\_at       | TIMESTAMP             | 更新时间              | DEFAULT NOW() |

#### 3.2.2 索引设计

- 主键索引：id字段
- 普通索引：name、chinese\_name、type、status字段
- 空间索引：geom字段（GIST索引）

#### 3.2.3 空间索引设计

```sql
CREATE INDEX idx_pavilion_geom ON pavilion USING GIST (geom);
```

#### 3.2.4 OGC标准适配

- 支持WMS（Web Map Service）地图服务
- 支持WFS（Web Feature Service）要素服务
- 支持WMTS（Web Map Tile Service）瓦片服务

#### 步骤3：PostGIS应用

#### 3.3.1 空间数据类型

- GEOMETRY：通用几何类型
- POINT：点类型（亭子位置）
- LINESTRING：线类型（路线）
- POLYGON：面类型（区域）

#### 3.3.2 空间函数使用

- ST\_PointFromText：从文本创建点
- ST\_Distance：计算两点距离
- ST\_DWithin：判断点是否在指定范围内
- ST\_Buffer：创建缓冲区
- ST\_AsGeoJSON：转换为GeoJSON格式

#### 3.3.3 空间查询验证

```sql
-- 查询指定半径范围内的亭子
SELECT * FROM pavilion 
WHERE ST_DWithin(geom, ST_SetSRID(ST_MakePoint(118.317, 32.317), 4326), 1000);

-- 计算两点距离
SELECT ST_Distance(
  ST_SetSRID(ST_MakePoint(118.317, 32.317), 4326),
  ST_SetSRID(ST_MakePoint(118.318, 32.318), 4326)
) * 111000 AS distance_meters;
```

***

## 总结与思考

### 实验总结

- 掌握EA UML软件的安装配置和基本操作
- 掌握面向对象分析方法，包括用例图、类图、时序图、包图的绘制方法
- 完成空间数据库概念设计和逻辑设计，包括实体转换、属性转换、关系转换
- 掌握PostGIS空间数据类型（GEOMETRY、POINT、LINESTRING、POLYGON）和空间函数（ST\_Distance、ST\_DWithin、ST\_Buffer等）
- 理解OGC标准（WMS、WFS、WMTS）在空间数据库中的应用
- 掌握空间索引设计（GIST索引）和空间查询优化方法

### 课后思考

1. 面向对象分析方法的优势和应用场景
   - 面向对象分析侧重于对象建模和交互，适合复杂业务逻辑系统
   - 在亭城GIS项目中，如何有效应用面向对象方法进行系统设计？
2. 空间数据库与普通数据库的主要区别
   - 空间数据库支持空间数据类型（GEOMETRY、POINT等）
   - 空间数据库提供空间索引（GIST）和空间函数（ST\_Distance等）
   - 空间数据库遵循OGC标准，支持空间查询和空间分析
   - 在亭城GIS项目中，如何有效利用PostGIS的空间特性？
3. OGC标准在空间数据库中的作用
   - OGC标准定义了空间数据的交换格式和接口规范
   - WMS、WFS、WMTS等标准服务实现空间数据的互操作性
   - OGC标准确保不同GIS系统之间的数据共享和集成
   - 在亭城GIS项目中，如何设计符合OGC标准的接口？
4. 如何在实际项目中选择合适的建模方法
   - 根据项目复杂度选择合适的建模方法
   - 根据团队技术背景选择合适的建模工具
   - 根据系统特点选择合适的数据库设计方法
   - 在亭城GIS项目中，如何有效应用面向对象设计方法？

