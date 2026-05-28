# 实验六 滁州亭城GIS系统案例分析与实践（一）

## 实验项目基本信息

- **实验编号**：d20301035106
- **学时分配**：2学时
- **实验类型**：综合型
- **每组人数**：5人
- **对应课程目标**：课程目标1、2、3

***

## 🎯 核心步骤列表

### 📋 实验概览

1. **案例分析阶段**：分析滁州亭城GIS系统案例
2. **需求分析阶段**：完成子项目需求分析
3. **架构设计阶段**：完成系统架构设计
4. **建模阶段**：完成EA UML建模
5. **总结阶段**：提交设计文档

### 🛠️ 工具清单

| 序号 | 开发工具       | 主要用途           | 验证项目      |
| -- | ---------- | -------------- | --------- |
| 1  | EA UML     | 系统建模           | 用例图、类图    |
| 2  | PostgreSQL | 空间数据库          | 数据设计      |
| 3  | PostGIS    | PostgreSQL空间扩展 | 空间数据类型    |
| 4  | TRAE       | AI辅助分析         | 需求分析、设计辅助 |
| 5  | Opencode   | AI辅助编程         | 代码补全、智能提示 |

**亭城GIS子项目分析环境要求：**

| 环境组件       | 版本要求 | 说明              |
| ---------- | ---- | --------------- |
| EA UML     | 15+  | UML建模工具         |
| PostgreSQL | 16+  | 关系型数据库，数据设计     |
| PostGIS    | 3.4+ | PostgreSQL的空间扩展 |
| TRAE       | 最新版本 | AI辅助分析工具        |
| Opencode   | 最新版本 | AI辅助编程工具        |

### ✅ 成功标准

- [ ] 案例分析完成
- [ ] 子项目选题确定
- [ ] 需求分析完成
- [ ] 架构设计完成
- [ ] UML模型完成

***

## 1. 滁州亭城GIS系统案例分析

### 1.1 系统概述学习

#### 步骤1：项目背景了解

1. **项目背景**
   - 滁州市拥有丰富的亭文化资源
   - 醉翁亭、丰乐亭等历史名亭具有重要文化价值
   - 需要建立GIS系统进行亭文化资源管理
2. **建设目标**
   - 建立亭城GIS数据库
   - 开发亭子信息管理系统
   - 提供空间查询和分析功能
   - 支持AI智能服务
3. **服务对象**
   - 文物保护部门
   - 旅游管理部门
   - 普通游客
   - 研究人员

#### 步骤2：功能模块分析

1. **地图展示功能**
   - 底图加载（OpenStreetMap/天地图）
   - 亭子位置标注
   - 地图缩放、平移
   - 图层控制
2. **位置搜索功能**
   - 关键词搜索
   - 范围查询
   - 附近亭子推荐
3. **亭子信息管理功能**
   - 亭子信息录入
   - 亭子信息编辑
   - 亭子信息删除
   - 亭子信息查询
4. **AI智能服务功能**
   - AI亭子推荐
   - AI路线规划
   - AI智能问答

#### 步骤3：技术架构分析

1. **前端技术栈**
   - Vue 3 + Vite
   - Leaflet地图库
   - Axios HTTP客户端
2. **后端技术栈**
   - Spring Boot 3.1.0
   - Spring Data JPA
   - PostgreSQL + PostGIS
3. **GIS技术**
   - PostGIS空间数据库
   - Leaflet前端地图
   - OGC标准服务
4. **数据存储方案**
   - PostgreSQL关系型数据库
   - PostGIS空间扩展
   - 空间索引优化

### 1.2 数据模型分析

#### 步骤1：核心实体分析

1. **Pavilion（亭子）实体**
   - 基本信息：名称、类型、建造年代
   - 空间信息：经度、纬度、几何形状
   - 属性信息：长度、宽度、高度、面积
   - 文化价值：历史价值、艺术价值
2. **User（用户）实体**
   - 基本信息：用户名、密码、邮箱
   - 权限信息：角色、权限
3. **SearchHistory（搜索历史）实体**
   - 搜索记录：关键词、搜索时间
   - 用户关联：用户ID

#### 步骤2：空间数据模型分析

```sql
-- 亭子表空间字段设计
CREATE TABLE pavilion (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    chinese_name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    build_year INTEGER,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    geom GEOMETRY(Point, 4326),  -- 空间字段
    -- 其他属性字段
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建空间索引
CREATE INDEX idx_pavilion_geom ON pavilion USING GIST(geom);
```

***

## 2. TingChengGIS子项目选题

### 2.1 可选子项目类型

#### 步骤1：了解子项目类型

| 分组  | 子项目类型             | 功能描述                                      | 远程仓库          |
| --- | ----------------- | ----------------------------------------- | ------------- |
| 第1组 | 文本子系统（TextGIS）    | 地名地址管理、景点资讯、用户评论、交通指南、AI智能搜索              | g1-textgis    |
| 第2组 | 音频子系统（AudioGIS）   | 语音讲解、语音导航、音频资讯、语音搜索、AI语音交互                | g2-audiogis   |
| 第3组 | 视频子系统（VideoGIS）   | 视频地图、视频直播、视频点播、视频监控、AI视频分析                | g3-videogis   |
| 第4组 | 虚拟子系统（VirtualGIS） | 三维地图、虚拟游览、时空对比、数据可视化、AI智能导览               | g4-virtualgis |
| 第5组 | 混合子系统（MixedGIS）   | AR导航、AR景点识别、AR测量、AR寻宝、AI物体识别              | g5-mixedgis   |
| 第6组 | AI子系统（AIGIS）      | 智能问答、智能推荐、图像识别、时空分析、AI服务接口                | g6-aigis      |
| 第7组 | CI/CD流水线组（OpsGIS） | Jenkins流水线构建、自动化测试、代码质量检查（SonarLint）、持续部署 | g7-opsgis     |
| 第8组 | 跨平台应用组（PortalGIS） | Web应用、移动端应用、VR/AR应用的跨平台集成与统一用户体验          | g8-portalgis  |

#### 步骤2：子项目详细说明

**1. TextGIS（文本子系统）**

- 功能：地名地址管理、景点资讯、用户评论、交通指南、AI智能搜索
- 技术：Spring Boot + Vue + PostGIS
- 特点：基础功能完善，适合入门

**2. AudioGIS（音频子系统）**

- 功能：语音讲解、语音导航、音频资讯、语音搜索、AI语音交互
- 技术：Spring Boot + Vue + Web Audio API
- 特点：多媒体处理，增加用户体验

**3. VideoGIS（视频子系统）**

- 功能：视频地图、视频直播、视频点播、视频监控、AI视频分析
- 技术：Spring Boot + Vue + Video.js
- 特点：视频处理，需要存储优化

**4. VirtualGIS（虚拟子系统）**

- 功能：三维地图、虚拟游览、时空对比、数据可视化、AI智能导览
- 技术：Three.js + WebXR
- 特点：VR技术，沉浸式体验

**5. MixedGIS（混合子系统）**

- 功能：AR导航、AR景点识别、AR测量、AR寻宝、AI物体识别
- 技术：AR.js + WebXR
- 特点：MR技术，创新性强

**6. AIGIS（AI子系统）**

- 功能：智能问答、智能推荐、图像识别、时空分析、AI服务接口
- 技术：Spring Boot + AI API
- 特点：AI集成，智能化服务

**7. OpsGIS（CI/CD流水线组）**

- 功能：Jenkins流水线构建、自动化测试、代码质量检查（SonarLint）、持续部署
- 技术：Jenkins + SonarQube + Docker
- 特点：DevOps实践，自动化运维

**8. PortalGIS（跨平台应用组）**

- 功能：Web应用、移动端应用、VR/AR应用的跨平台集成与统一用户体验
- 技术：Flutter/React Native + Web
- 特点：跨平台开发，统一用户体验

### 2.2 选题确定

#### 步骤1：选择子项目

1. **考虑因素**
   - 组员技术背景
   - 项目可行性
   - 创新性要求
   - 时间限制
2. **选题流程**
   - 小组讨论
   - 技术评估
   - 确定选题
   - 填写选题表

#### 步骤2：填写项目选题表

| 项目信息 | 内容                                           |
| ---- | -------------------------------------------- |
| 项目名称 | TingChengGIS-TextGIS                         |
| 项目类型 | TextGIS                                      |
| 小组成员 | 组长：XXX，组员：XXX、XXX、XXX、XXX                    |
| 技术栈  | Spring Boot + Vue + PostGIS                  |
| 远程仓库 | <https://gitee.com/chzuczldl/g1-textgis.git> |
| 预期目标 | 完成亭子信息管理、空间查询、AI推荐功能                         |

***

## 3. 需求分析与架构设计

### 3.1 需求分析

#### 步骤1：功能需求分析

**核心功能列表：**

| 功能模块 | 功能点   | 优先级 | 描述            |
| ---- | ----- | --- | ------------- |
| 亭子管理 | 添加亭子  | 高   | 录入亭子基本信息和空间信息 |
| 亭子管理 | 查询亭子  | 高   | 根据条件查询亭子信息    |
| 亭子管理 | 编辑亭子  | 中   | 修改亭子信息        |
| 亭子管理 | 删除亭子  | 中   | 删除亭子记录        |
| 空间查询 | 范围查询  | 高   | 查询指定范围内的亭子    |
| 空间查询 | 关键词搜索 | 高   | 根据关键词搜索亭子     |
| 空间查询 | 附近推荐  | 中   | 推荐附近的亭子       |
| AI服务 | 智能推荐  | 中   | AI推荐感兴趣的亭子    |
| AI服务 | 路线规划  | 中   | AI规划游览路线      |

#### 步骤2：非功能需求分析

| 需求类型 | 需求描述    | 指标要求                |
| ---- | ------- | ------------------- |
| 性能要求 | API响应时间 | <200ms              |
| 性能要求 | 空间查询性能  | <500ms              |
| 可用性  | 系统可用性   | >99%                |
| 安全性  | 数据安全    | 数据加密存储              |
| 兼容性  | 浏览器兼容   | Chrome、Firefox、Edge |

#### 步骤3：需求文档编写

使用TRAE辅助编写需求规格说明书：

```
需求规格说明书结构：
1. 引言
   1.1 目的
   1.2 范围
   1.3 定义
2. 总体描述
   2.1 产品视角
   2.2 产品功能
   2.3 用户特征
   2.4 约束条件
3. 具体需求
   3.1 功能需求
   3.2 非功能需求
   3.3 接口需求
```

### 3.2 架构设计

#### 步骤1：系统架构设计

```
系统分层架构：
┌─────────────────────────────────────────┐
│              表现层（Presentation）         │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐    │
│  │ 地图组件 │ │ 搜索组件 │ │ 管理组件 │    │
│  └─────────┘ └─────────┘ └─────────┘    │
├─────────────────────────────────────────┤
│              业务层（Business）            │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐    │
│  │亭子服务 │ │空间服务 │ │ AI服务  │    │
│  └─────────┘ └─────────┘ └─────────┘    │
├─────────────────────────────────────────┤
│              数据层（Data）               │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐    │
│  │PostgreSQL│ │ PostGIS │ │ 缓存层  │    │
│  └─────────┘ └─────────┘ └─────────┘    │
└─────────────────────────────────────────┘
```

#### 步骤2：技术选型

| 层次    | 技术选型            | 版本    | 说明              |
| ----- | --------------- | ----- | --------------- |
| 前端框架  | Vue             | 3.x   | 渐进式JavaScript框架 |
| 构建工具  | Vite            | 5.x   | 下一代前端构建工具       |
| 地图库   | Leaflet         | 1.9.x | 轻量级地图库          |
| 后端框架  | Spring Boot     | 3.1.0 | Java应用框架        |
| ORM框架 | Spring Data JPA | 3.1.0 | 数据持久化框架         |
| 数据库   | PostgreSQL      | 16+   | 关系型数据库          |
| 空间扩展  | PostGIS         | 3.4+  | 空间数据库扩展         |

***

## 4. EA UML建模

### 4.1 用例图绘制

#### 步骤1：识别参与者

1. **普通用户**
   - 浏览地图
   - 搜索亭子
   - 查看亭子详情
2. **管理员**
   - 管理亭子信息
   - 管理用户
   - 系统配置
3. **AI系统**
   - 智能推荐
   - 路线规划

#### 步骤2：识别用例

```
用例列表：
├── 地图浏览
│   ├── 查看底图
│   ├── 缩放地图
│   └── 平移地图
├── 亭子搜索
│   ├── 关键词搜索
│   ├── 范围查询
│   └── 附近推荐
├── 亭子管理
│   ├── 添加亭子
│   ├── 编辑亭子
│   ├── 删除亭子
│   └── 查看详情
└── AI服务
    ├── 智能推荐
    └── 路线规划
```

### 4.2 类图绘制

#### 步骤1：识别实体类

```
实体类：
├── Pavilion（亭子）
│   ├── id: Long
│   ├── name: String
│   ├── chineseName: String
│   ├── longitude: Double
│   ├── latitude: Double
│   └── geom: String
├── User（用户）
│   ├── id: Long
│   ├── username: String
│   ├── password: String
│   └── role: String
└── SearchHistory（搜索历史）
    ├── id: Long
    ├── keyword: String
    └── searchTime: LocalDateTime
```

#### 步骤2：识别控制类

```
控制类：
├── PavilionController
│   ├── searchPavilions()
│   ├── getPavilion()
│   ├── addPavilion()
│   ├── updatePavilion()
│   └── deletePavilion()
├── SpatialController
│   ├── findWithinRadius()
│   └── findNearby()
└── AIController
    ├── recommend()
    └── planRoute()
```

#### 步骤3：识别服务类

```
服务类：
├── PavilionService
│   ├── searchPavilions()
│   ├── getPavilionById()
│   ├── savePavilion()
│   └── deletePavilion()
├── SpatialService
│   ├── findWithinRadius()
│   └── calculateDistance()
└── AIService
    ├── recommend()
    └── planRoute()
```

### 4.3 时序图绘制

#### 步骤1：绘制搜索亭子时序图

```
用户 → 前端: 输入搜索关键词
前端 → PavilionController: GET /api/pavilions/search
PavilionController → PavilionService: searchPavilions(keyword)
PavilionService → PavilionRepository: findByNameContaining()
PavilionRepository → PostgreSQL: SELECT * FROM pavilion
PostgreSQL → PavilionRepository: 返回结果
PavilionRepository → PavilionService: List<Pavilion>
PavilionService → PavilionController: List<Pavilion>
PavilionController → 前端: JSON响应
前端 → 用户: 显示搜索结果
```

***

## 总结与思考

### 实验总结

- 掌握滁州亭城GIS系统案例分析方法
- 完成TingChengGIS子项目需求分析
- 完成系统架构设计
- 完成EA UML建模
- 确定子项目选题和技术栈

### 课后思考

1. GIS系统开发的关键成功因素
   - 空间数据质量如何保证？
   - 空间查询性能如何优化？
2. 子项目与主系统的集成方式
   - 如何设计统一的接口规范？
   - 如何实现数据共享？
3. 技术选型的考虑因素
   - 如何评估技术的适用性？
   - 如何平衡技术先进性和稳定性？
4. 需求分析的方法和技巧
   - 如何准确获取用户需求？
   - 如何处理需求变更？
5. 架构设计的原则
   - 如何设计可扩展的架构？
   - 如何保证系统的安全性？

