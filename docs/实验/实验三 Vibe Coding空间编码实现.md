# 实验三 Vibe Coding空间编码实现

## 实验项目基本信息

- **实验编号**：d20301035103
- **学时分配**：2学时
- **实验类型**：实现型
- **每组人数**：5人
- **对应课程目标**：课程目标2

***

## 🎯 核心步骤列表

### 📋 实验概览

1. **环境配置阶段**：搭建Spring Boot开发环境
2. **后端实现阶段**：实现亭子管理、空间查询、AI服务接口
3. **前端实现阶段**：实现WebGIS地图展示和交互功能
4. **AI辅助编码阶段**：使用TRAE/Opencode辅助编程
5. **测试验证阶段**：验证核心功能实现

### 🛠️ 工具清单

| 序号 | 开发工具       | 主要用途           | 验证项目             |
| -- | ---------- | -------------- | ---------------- |
| 1  | PostgreSQL | 关系型数据库         | 空间数据存储           |
| 2  | PostGIS    | PostgreSQL空间扩展 | 空间数据类型、空间索引、空间查询 |
| 3  | Leaflet    | 轻量级地图库         | WebGIS前端实现       |
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
| Leaflet     | 1.9+  | 轻量级开源地图库，用于WebGIS前端实现         |
| TRAE        | 最新版本  | AI辅助编程工具，提高开发效率               |
| Opencode    | 最新版本  | AI辅助编程工具，代码补全和智能提示            |

### ✅ 成功标准

- [ ] Spring Boot项目搭建完成
- [ ] 数据库连接配置成功
- [ ] 核心API接口实现完成
- [ ] WebGIS前端展示实现
- [ ] AI辅助编码工具使用熟练
- [ ] 核心功能测试通过
- [ ] 代码提交到远程仓库

***

## 💡 Vibe Coding理念说明

### 什么是Vibe Coding

\*\*Vibe Coding（氛围编程）\*\*是一种现代编程理念，强调在编程过程中营造良好的开发氛围和体验。它不仅仅是关于代码本身，更是关于整个开发过程中的感受、效率和协作。

#### 核心要素

1. **沉浸式开发体验**
   - 使用AI辅助工具（TRAE/Opencode）提升编码流畅度
   - 减少重复性工作，专注于创造性编程
   - 保持代码的"氛围感"——优雅、简洁、易读
2. **协作式编程氛围**
   - 团队成员共享代码风格和最佳实践
   - 实时代码审查，保持代码质量
   - 使用Git进行版本控制，营造协作氛围
3. **智能化编程辅助**
   - 利用AI工具生成代码、优化代码、诊断错误
   - 让编程过程更加流畅和愉悦
   - 减少认知负担，提高开发效率
4. **空间数据编程氛围**
   - 结合GIS专业知识，编写符合空间数据特性的代码
   - 使用PostGIS空间函数，体现空间编程的专业性
   - 在WebGIS前端中营造良好的用户体验氛围

#### Vibe Coding在GIS开发中的应用

| 应用场景 | 技术栈                   | 氛围营造        |
| ---- | --------------------- | ----------- |
| 后端开发 | Spring Boot + PostGIS | 高效的空间数据处理氛围 |
| 前端开发 | Leaflet + Vue         | 流畅的地图交互氛围   |
| AI辅助 | TRAE/Opencode         | 智能编码氛围      |
| 团队协作 | Git远程仓库               | 协作开发氛围      |

#### 本实验的Vibe Coding实践

在本实验中，我们将通过以下方式实践Vibe Coding理念：

1. **使用TRAE进行代码生成**：通过自然语言描述生成Spring Boot代码
2. **使用Opencode进行代码补全**：提高编码效率，减少重复输入
3. **Git协作开发**：通过远程仓库进行团队协作
4. **空间数据编程**：使用PostGIS进行空间数据查询和处理

***

## 1. 环境配置与项目搭建

### 1.1 Spring Boot项目初始化

#### 步骤1：创建Spring Boot项目

1. 使用IntelliJ IDEA创建Spring Boot项目
2. 选择Maven构建工具
3. 配置项目JDK版本为21（确保已安装JDK 21并配置JAVA\_HOME环境变量）
4. 添加必要的依赖：
   - Spring Web
   - Spring Data JPA
   - PostgreSQL Driver
   - H2 Database (用于测试)
   - Lombok

#### 步骤2：数据库配置

1. 配置application.properties文件：
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/tingcheng
   spring.datasource.username=postgres
   spring.datasource.password=123456
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```
2. 启用PostGIS扩展：
   ```sql
   CREATE EXTENSION postgis;
   ```

### 1.2 项目结构搭建

#### 步骤1：创建包结构

1. controller包：控制器层，处理HTTP请求
2. service包：服务层，处理业务逻辑
3. repository包：数据访问层，操作数据库
4. entity包：实体类，定义数据模型
5. dto包：数据传输对象，用于前后端数据交互
6. util包：工具类，提供通用功能

#### 步骤2：配置TRAE和Opencode

1. 在IDE中安装TRAE和Opencode插件
2. 配置AI辅助编程工具
3. 测试代码生成功能

***

## 2. 后端核心功能实现

### 2.1 实体类实现

#### 步骤1：实现Pavilion实体类

```java
package com.tingcheng.gis.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "pavilion")
public class Pavilion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "chinese_name", nullable = false)
    private String chineseName;
    
    @Column(nullable = false)
    private String type;
    
    @Column(name = "build_year")
    private Integer buildYear;
    
    private Double length;
    private Double width;
    private Double height;
    private Double area;
    private String material;
    private String status;
    
    @Column(name = "historical_value")
    private Integer historicalValue;
    
    @Column(name = "artistic_value")
    private Integer artisticValue;
    
    private String description;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(columnDefinition = "GEOMETRY(Point, 4326)")
    private String geom;
}
```

#### 步骤2：实现其他实体类

- User实体类
- SearchHistory实体类
- Route实体类

### 2.2 数据访问层实现

#### 步骤1：实现PavilionRepository

```java
package com.tingcheng.gis.repository;

import com.tingcheng.gis.entity.Pavilion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PavilionRepository extends JpaRepository<Pavilion, Long> {
    
    // 根据关键词搜索亭子
    List<Pavilion> findByNameContainingOrChineseNameContaining(String name, String chineseName);
    
    // 范围查询
    @Query(value = "SELECT * FROM pavilion WHERE ST_DWithin(geom, ST_SetSRID(ST_MakePoint(?1, ?2), 4326), ?3)", nativeQuery = true)
    List<Pavilion> findByLocation(double longitude, double latitude, double radius);
}
```

### 2.3 服务层实现

#### 步骤1：实现PavilionService

```java
package com.tingcheng.gis.service;

import com.tingcheng.gis.entity.Pavilion;
import com.tingcheng.gis.repository.PavilionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PavilionService {
    
    @Autowired
    private PavilionRepository pavilionRepository;
    
    public List<Pavilion> searchPavilions(String keyword) {
        return pavilionRepository.findByNameContainingOrChineseNameContaining(keyword, keyword);
    }
    
    public List<Pavilion> findPavilionsWithinRadius(double longitude, double latitude, double radius) {
        return pavilionRepository.findByLocation(longitude, latitude, radius);
    }
    
    public Pavilion getPavilionById(Long id) {
        return pavilionRepository.findById(id).orElse(null);
    }
    
    public Pavilion savePavilion(Pavilion pavilion) {
        // 生成geom字段
        String geom = String.format("POINT(%f %f)", pavilion.getLongitude(), pavilion.getLatitude());
        pavilion.setGeom(geom);
        return pavilionRepository.save(pavilion);
    }
    
    public void deletePavilion(Long id) {
        pavilionRepository.deleteById(id);
    }
}
```

#### 步骤2：实现其他服务类

- SpatialService：空间计算服务
- AIService：AI智能服务

### 2.4 控制器层实现

#### 步骤1：实现PavilionController

```java
package com.tingcheng.gis.controller;

import com.tingcheng.gis.entity.Pavilion;
import com.tingcheng.gis.service.PavilionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pavilions")
public class PavilionController {
    
    @Autowired
    private PavilionService pavilionService;
    
    @GetMapping("/search")
    public List<Pavilion> searchPavilions(@RequestParam String keyword) {
        return pavilionService.searchPavilions(keyword);
    }
    
    @GetMapping("/within-radius")
    public List<Pavilion> findWithinRadius(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam double radius) {
        return pavilionService.findPavilionsWithinRadius(longitude, latitude, radius);
    }
    
    @GetMapping("/{id}")
    public Pavilion getPavilion(@PathVariable Long id) {
        return pavilionService.getPavilionById(id);
    }
    
    @PostMapping
    public Pavilion addPavilion(@RequestBody Pavilion pavilion) {
        return pavilionService.savePavilion(pavilion);
    }
    
    @PutMapping("/{id}")
    public Pavilion updatePavilion(@PathVariable Long id, @RequestBody Pavilion pavilion) {
        pavilion.setId(id);
        return pavilionService.savePavilion(pavilion);
    }
    
    @DeleteMapping("/{id}")
    public void deletePavilion(@PathVariable Long id) {
        pavilionService.deletePavilion(id);
    }
}
```

#### 步骤2：实现其他控制器

- SpatialController：空间查询控制器
- AIController：AI服务控制器

***

## 3. 前端WebGIS实现

### 3.1 前端项目搭建

#### 步骤1：创建前端项目

1. 使用Vite创建Vue项目
2. 安装必要的依赖：
   - leaflet
   - axios
   - vue-router

#### 步骤2：配置前端环境

1. 配置API基础路径
2. 配置地图服务

### 3.2 地图展示实现

#### 步骤1：实现基础地图

```html
<template>
  <div id="map" style="height: 600px;"></div>
</template>

<script setup>
import { onMounted } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

onMounted(() => {
  // 初始化地图
  const map = L.map('map').setView([32.317, 118.317], 13);
  
  // 添加底图
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);
  
  // 加载亭子数据
  fetch('/api/pavilions')
    .then(response => response.json())
    .then(data => {
      data.forEach(pavilion => {
        // 添加亭子标记
        L.marker([pavilion.latitude, pavilion.longitude])
          .addTo(map)
          .bindPopup(`
            <h3>${pavilion.chineseName}</h3>
            <p>${pavilion.description}</p>
            <button onclick="showDetails(${pavilion.id})">查看详情</button>
          `);
      });
    });
});
</script>
```

#### 步骤2：实现搜索功能

```html
<template>
  <div class="search-container">
    <input v-model="keyword" placeholder="搜索亭子..." />
    <button @click="search">搜索</button>
  </div>
  <div id="map" style="height: 600px;"></div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import L from 'leaflet';

const keyword = ref('');
let map = null;
let markers = [];

onMounted(() => {
  map = L.map('map').setView([32.317, 118.317], 13);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
  loadPavilions();
});

const search = () => {
  if (keyword.value) {
    fetch(`/api/pavilions/search?keyword=${keyword.value}`)
      .then(response => response.json())
      .then(data => {
        clearMarkers();
        data.forEach(pavilion => {
          addMarker(pavilion);
        });
      });
  }
};

const loadPavilions = () => {
  fetch('/api/pavilions')
    .then(response => response.json())
    .then(data => {
      data.forEach(pavilion => {
        addMarker(pavilion);
      });
    });
};

const addMarker = (pavilion) => {
  const marker = L.marker([pavilion.latitude, pavilion.longitude])
    .addTo(map)
    .bindPopup(`
      <h3>${pavilion.chineseName}</h3>
      <p>${pavilion.description}</p>
    `);
  markers.push(marker);
};

const clearMarkers = () => {
  markers.forEach(marker => map.removeLayer(marker));
  markers = [];
};
</script>
```

### 3.3 空间查询功能实现

#### 步骤1：实现范围查询

```html
<template>
  <div class="radius-search">
    <h3>范围查询</h3>
    <div>
      <label>经度: <input v-model="longitude" type="number" step="0.000001" /></label>
      <label>纬度: <input v-model="latitude" type="number" step="0.000001" /></label>
      <label>半径(米): <input v-model="radius" type="number" /></label>
      <button @click="searchByRadius">查询</button>
    </div>
  </div>
  <div id="map" style="height: 600px;"></div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import L from 'leaflet';

const longitude = ref(118.317);
const latitude = ref(32.317);
const radius = ref(1000);
let map = null;
let markers = [];
let circle = null;

onMounted(() => {
  map = L.map('map').setView([latitude.value, longitude.value], 13);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
});

const searchByRadius = () => {
  // 清除之前的标记
  clearMarkers();
  
  // 添加搜索范围圆
  if (circle) map.removeLayer(circle);
  circle = L.circle([latitude.value, longitude.value], {
    color: 'blue',
    fillColor: '#3498db',
    fillOpacity: 0.2,
    radius: radius.value
  }).addTo(map);
  
  // 发送范围查询请求
  fetch(`/api/pavilions/within-radius?longitude=${longitude.value}&latitude=${latitude.value}&radius=${radius.value}`)
    .then(response => response.json())
    .then(data => {
      data.forEach(pavilion => {
        L.marker([pavilion.latitude, pavilion.longitude])
          .addTo(map)
          .bindPopup(`
            <h3>${pavilion.chineseName}</h3>
            <p>${pavilion.description}</p>
          `);
      });
    });
};

const clearMarkers = () => {
  markers.forEach(marker => map.removeLayer(marker));
  markers = [];
};
</script>
```

***

## 4. AI辅助编码实践

### 4.1 TRAE代码生成

#### 步骤1：使用TRAE生成代码

1. 打开TRAE插件
2. 输入自然语言描述："生成Spring Boot亭子管理API控制器"
3. 选择生成的代码并集成到项目中

#### 步骤2：使用TRAE优化代码

1. 选择需要优化的代码
2. 右键选择"TRAE - 优化代码"
3. 应用优化建议

### 4.2 Opencode代码补全

#### 步骤1：使用Opencode智能补全

1. 在编码过程中，Opencode会自动提供代码补全建议
2. 按Tab键接受补全建议

#### 步骤2：使用Opencode代码提示

1. 输入代码时，Opencode会提供智能提示
2. 选择合适的提示加速编码

### 4.3 错误诊断与修复

#### 步骤1：使用TRAE诊断错误

1. 当代码出现错误时，TRAE会自动提示
2. 查看错误诊断信息

#### 步骤2：使用TRAE修复错误

1. 右键选择"TRAE - 修复错误"
2. 应用修复建议

***

## 5. 测试与验证

### 5.1 功能测试

#### 步骤1：API接口测试

1. 使用Postman测试API接口
2. 测试亭子搜索功能
3. 测试范围查询功能
4. 测试CRUD操作

#### 步骤2：前端功能测试

1. 测试地图加载
2. 测试亭子标记显示
3. 测试搜索功能
4. 测试范围查询功能

### 5.2 性能测试

#### 步骤1：空间查询性能测试

1. 测试不同半径的范围查询性能
2. 测试大量数据的搜索性能

#### 步骤2：API响应时间测试

1. 测试不同API接口的响应时间
2. 优化性能瓶颈

### 5.3 代码质量检查

#### 步骤1：代码规范检查

1. 使用IDE的代码检查工具
2. 确保代码符合Java规范

#### 步骤2：代码提交

1. 提交代码到远程仓库
2. 确保代码版本控制规范

***

## 总结与思考

### 实验总结

- 掌握Spring Boot项目搭建和配置
- 掌握PostgreSQL和PostGIS的使用
- 实现了亭子管理、空间查询、AI服务等核心功能
- 实现了WebGIS前端展示和交互功能
- 掌握了AI辅助编码工具的使用
- 完成了TingChengGIS子项目的核心功能实现
- 体验了Vibe Coding编程理念，营造了良好的开发氛围

### 课后思考

1. Vibe Coding理念在GIS开发中的应用
   - 如何在空间数据处理中营造良好的编程氛围
   - AI辅助工具如何提升编程体验和效率
   - 团队协作如何营造积极的开发氛围
2. Spring Boot与WebGIS的集成方式
   - 如何优化前后端交互
   - 如何提高空间查询性能
3. 空间数据处理的最佳实践
   - 空间索引的设计与优化
   - 空间查询的性能优化
4. AI辅助编程在GIS开发中的应用
   - 如何利用AI工具提高开发效率
   - AI辅助编程的局限性和解决方案
5. 项目部署与维护
   - 如何部署Spring Boot应用
   - 如何监控和维护空间数据库
6. 扩展功能设计
   - 如何添加用户认证和授权
   - 如何实现更复杂的空间分析功能
   - 如何集成更多AI服务

