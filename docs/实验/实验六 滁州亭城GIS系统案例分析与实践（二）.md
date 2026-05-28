# 实验六 滁州亭城GIS系统案例分析与实践（二）

## 实验项目基本信息

- **实验编号**：d20301035107
- **学时分配**：2学时
- **实验类型**：综合型
- **每组人数**：5人
- **对应课程目标**：课程目标1、2、3

***

## 🎯 核心步骤列表

### 📋 实验概览

1. **项目初始化阶段**：创建代码仓库和项目结构
2. **核心编码阶段**：实现子项目核心功能
3. **AI辅助编码阶段**：使用TRAE/Opencode辅助编程
4. **单元测试阶段**：编写和执行单元测试
5. **总结阶段**：提交项目代码

### 🛠️ 工具清单

| 序号 | 开发工具        | 主要用途     | 验证项目  |
| -- | ----------- | -------- | ----- |
| 1  | Git         | 版本控制     | 代码管理  |
| 2  | Spring Boot | 后端框架     | API开发 |
| 3  | Leaflet     | WebGIS前端 | 地图展示  |
| 4  | TRAE        | AI辅助编程   | 代码生成  |
| 5  | Opencode    | AI辅助编程   | 代码补全  |

**亭城GIS子项目开发环境要求：**

| 环境组件        | 版本要求  | 说明              |
| ----------- | ----- | --------------- |
| JDK         | 21+   | Java开发工具包       |
| Maven       | 3.9+  | 项目构建工具          |
| PostgreSQL  | 16+   | 关系型数据库          |
| PostGIS     | 3.4+  | PostgreSQL的空间扩展 |
| Spring Boot | 3.1.0 | Java应用框架        |
| Leaflet     | 1.9+  | 轻量级地图库          |
| TRAE        | 最新版本  | AI辅助编程工具        |
| Opencode    | 最新版本  | AI辅助编程工具        |

### ✅ 成功标准

- [ ] 代码仓库创建完成
- [ ] 核心功能开发完成
- [ ] 单元测试完成
- [ ] 代码规范符合要求
- [ ] 项目可运行

***

## 1. 项目初始化

### 1.1 代码仓库创建

#### 步骤1：Git仓库初始化

```bash
# 克隆远程仓库
git clone https://gitee.com/chzuczldl/g1-textgis.git

# 进入项目目录
cd g1-textgis

# 初始化Git配置
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

#### 步骤2：分支策略制定

```
分支策略：
├── main（主分支）
│   └── 稳定的生产代码
├── develop（开发分支）
│   └── 开发中的代码
├── feature/*（功能分支）
│   ├── feature/pavilion-management
│   ├── feature/spatial-query
│   └── feature/ai-service
└── hotfix/*（修复分支）
    └── hotfix/bug-fix
```

#### 步骤3：README编写

```markdown
# TingChengGIS-TextGIS

## 项目简介
滁州亭城GIS系统文本信息管理子系统

## 技术栈
- 后端：Spring Boot 3.1.0 + PostgreSQL + PostGIS
- 前端：Vue 3 + Leaflet
- AI辅助：TRAE + Opencode

## 快速开始
1. 克隆仓库
2. 配置数据库
3. 启动后端服务
4. 启动前端服务

## 项目结构
├── backend/          # 后端代码
├── frontend/         # 前端代码
├── docs/             # 文档
└── scripts/          # 脚本
```

### 1.2 项目结构设计

#### 步骤1：后端项目结构

```
backend/
├── src/main/java/com/tingcheng/gis/
│   ├── TingChengGisApplication.java    # 启动类
│   ├── controller/                      # 控制器层
│   │   ├── PavilionController.java
│   │   ├── SpatialController.java
│   │   └── AIController.java
│   ├── service/                         # 服务层
│   │   ├── PavilionService.java
│   │   ├── SpatialService.java
│   │   └── AIService.java
│   ├── repository/                      # 数据访问层
│   │   ├── PavilionRepository.java
│   │   └── UserRepository.java
│   ├── entity/                          # 实体类
│   │   ├── Pavilion.java
│   │   └── User.java
│   ├── dto/                             # 数据传输对象
│   │   └── PavilionDTO.java
│   └── util/                            # 工具类
│       └── SpatialUtil.java
├── src/main/resources/
│   ├── application.properties           # 配置文件
│   └── schema.sql                       # 数据库脚本
└── pom.xml                              # Maven配置
```

#### 步骤2：前端项目结构

```
frontend/
├── src/
│   ├── main.js                          # 入口文件
│   ├── App.vue                          # 根组件
│   ├── components/                      # 组件
│   │   ├── MapComponent.vue
│   │   ├── SearchComponent.vue
│   │   └── PavilionDetail.vue
│   ├── views/                           # 页面
│   │   ├── HomeView.vue
│   │   └── ManageView.vue
│   ├── api/                             # API接口
│   │   └── pavilion.js
│   ├── utils/                           # 工具函数
│   │   └── mapUtils.js
│   └── assets/                          # 静态资源
├── index.html
├── package.json
└── vite.config.js
```

***

## 2. 核心功能编码

### 2.1 后端实现

#### 步骤1：实体类开发

```java
// Pavilion.java
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
    
    @Column(nullable = false)
    private Double longitude;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(columnDefinition = "GEOMETRY(Point, 4326)")
    private String geom;
    
    private String description;
}
```

#### 步骤2：数据访问层开发

```java
// PavilionRepository.java
package com.tingcheng.gis.repository;

import com.tingcheng.gis.entity.Pavilion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PavilionRepository extends JpaRepository<Pavilion, Long> {
    
    List<Pavilion> findByNameContainingOrChineseNameContaining(
        String name, String chineseName);
    
    @Query(value = "SELECT * FROM pavilion WHERE ST_DWithin(geom, " +
           "ST_SetSRID(ST_MakePoint(?1, ?2), 4326), ?3)", nativeQuery = true)
    List<Pavilion> findByLocation(double longitude, double latitude, double radius);
}
```

#### 步骤3：服务层开发

```java
// PavilionService.java
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
        return pavilionRepository
            .findByNameContainingOrChineseNameContaining(keyword, keyword);
    }
    
    public List<Pavilion> findPavilionsWithinRadius(
            double longitude, double latitude, double radius) {
        return pavilionRepository.findByLocation(longitude, latitude, radius);
    }
    
    public Pavilion savePavilion(Pavilion pavilion) {
        String geom = String.format("POINT(%f %f)", 
            pavilion.getLongitude(), pavilion.getLatitude());
        pavilion.setGeom(geom);
        return pavilionRepository.save(pavilion);
    }
}
```

#### 步骤4：控制器层开发

```java
// PavilionController.java
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
}
```

### 2.2 前端实现

#### 步骤1：地图组件开发

```vue
<!-- MapComponent.vue -->
<template>
  <div id="map" style="height: 600px;"></div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const pavilions = ref([]);
const map = ref(null);
const markers = ref([]);

onMounted(async () => {
  // 初始化地图
  map.value = L.map('map').setView([32.317, 118.317], 13);
  
  // 添加底图
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map.value);
  
  // 加载亭子数据
  await loadPavilions();
});

const loadPavilions = async () => {
  const response = await fetch('/api/pavilions');
  pavilions.value = await response.json();
  
  pavilions.value.forEach(pavilion => {
    const marker = L.marker([pavilion.latitude, pavilion.longitude])
      .addTo(map.value)
      .bindPopup(`<h3>${pavilion.chineseName}</h3>`);
    markers.value.push(marker);
  });
};
</script>
```

#### 步骤2：搜索组件开发

```vue
<!-- SearchComponent.vue -->
<template>
  <div class="search-container">
    <input v-model="keyword" placeholder="搜索亭子..." @keyup.enter="search" />
    <button @click="search">搜索</button>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const keyword = ref('');
const emit = defineEmits(['search']);

const search = () => {
  if (keyword.value) {
    emit('search', keyword.value);
  }
};
</script>
```

### 2.3 GIS功能实现

#### 步骤1：空间查询功能

```java
// SpatialService.java
package com.tingcheng.gis.service;

import org.springframework.stereotype.Service;

@Service
public class SpatialService {
    
    public double calculateDistance(
            double lon1, double lat1, double lon2, double lat2) {
        // 使用Haversine公式计算距离
        double R = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * 
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
```

#### 步骤2：空间数据可视化

```javascript
// mapUtils.js
export function createPavilionMarker(pavilion) {
  return L.marker([pavilion.latitude, pavilion.longitude], {
    title: pavilion.chineseName
  }).bindPopup(`
    <div class="pavilion-popup">
      <h3>${pavilion.chineseName}</h3>
      <p>类型：${pavilion.type}</p>
      <p>建造年代：${pavilion.buildYear || '未知'}</p>
      <p>${pavilion.description || ''}</p>
    </div>
  `);
}

export function createRadiusCircle(center, radius) {
  return L.circle([center.latitude, center.longitude], {
    color: 'blue',
    fillColor: '#3498db',
    fillOpacity: 0.2,
    radius: radius
  });
}
```

***

## 3. AI辅助编码实践

### 3.1 TRAE代码生成

#### 步骤1：使用TRAE生成代码

1. 打开TRAE插件
2. 输入自然语言描述：
   - "生成Spring Boot亭子管理CRUD接口"
   - "生成Vue地图搜索组件"
3. 选择生成的代码并集成到项目中

#### 步骤2：TRAE优化代码示例

```java
// TRAE生成的优化代码
@RestController
@RequestMapping("/api/pavilions")
@CrossOrigin(origins = "*")
public class PavilionController {
    
    private final PavilionService pavilionService;
    
    public PavilionController(PavilionService pavilionService) {
        this.pavilionService = pavilionService;
    }
    
    @GetMapping
    public ResponseEntity<List<Pavilion>> getAllPavilions() {
        return ResponseEntity.ok(pavilionService.getAllPavilions());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Pavilion> getPavilionById(@PathVariable Long id) {
        return ResponseEntity.ok(pavilionService.getPavilionById(id));
    }
}
```

### 3.2 Opencode代码补全

#### 步骤1：使用Opencode智能补全

1. 在编码过程中，Opencode自动提供代码补全建议
2. 按Tab键接受补全建议
3. 使用Ctrl+Space手动触发补全

#### 步骤2：Opencode辅助示例

```java
// 输入 "pub" 后Opencode自动补全
public 

// 输入 "Pri" 后Opencode自动补全
private 

// 输入 "@A" 后Opencode自动补全
@Autowired
```

### 3.3 错误诊断与修复

#### 步骤1：使用TRAE诊断错误

1. 当代码出现错误时，TRAE自动提示
2. 查看错误诊断信息
3. 应用修复建议

#### 步骤2：编码规范AI校验

```
TRAE编码规范检查结果：
1. 变量命名规范：通过
2. 方法长度检查：通过
3. 注释完整性：需要改进
4. 代码复杂度：通过

建议：
- 为复杂方法添加注释
- 简化部分逻辑判断
```

***

## 4. 单元测试

### 4.1 测试用例编写

#### 步骤1：服务层测试

```java
// PavilionServiceTest.java
package com.tingcheng.gis.service;

import com.tingcheng.gis.entity.Pavilion;
import com.tingcheng.gis.repository.PavilionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PavilionServiceTest {

    @Mock
    private PavilionRepository pavilionRepository;

    @InjectMocks
    private PavilionService pavilionService;

    @Test
    void testSearchPavilions() {
        Pavilion pavilion = new Pavilion();
        pavilion.setName("zuigengting");
        pavilion.setChineseName("醉翁亭");
        
        when(pavilionRepository.findByNameContainingOrChineseNameContaining(
            "醉翁", "醉翁"))
            .thenReturn(Arrays.asList(pavilion));
        
        List<Pavilion> result = pavilionService.searchPavilions("醉翁");
        
        assertEquals(1, result.size());
        assertEquals("醉翁亭", result.get(0).getChineseName());
    }
}
```

#### 步骤2：控制器层测试

```java
// PavilionControllerTest.java
package com.tingcheng.gis.controller;

import com.tingcheng.gis.entity.Pavilion;
import com.tingcheng.gis.service.PavilionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PavilionController.class)
class PavilionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PavilionService pavilionService;

    @Test
    void testSearchPavilions() throws Exception {
        Pavilion pavilion = new Pavilion();
        pavilion.setId(1L);
        pavilion.setChineseName("醉翁亭");
        
        when(pavilionService.searchPavilions("醉翁"))
            .thenReturn(Arrays.asList(pavilion));
        
        mockMvc.perform(get("/api/pavilions/search")
                .param("keyword", "醉翁"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].chineseName").value("醉翁亭"));
    }
}
```

### 4.2 测试执行

#### 步骤1：运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=PavilionServiceTest

# 生成测试报告
mvn test jacoco:report
```

#### 步骤2：测试覆盖率检查

```
测试覆盖率报告：
├── com.tingcheng.gis.controller
│   └── PavilionController: 85%
├── com.tingcheng.gis.service
│   └── PavilionService: 90%
└── com.tingcheng.gis.repository
    └── PavilionRepository: 75%

总体覆盖率：83%
```

***

## 总结与思考

### 实验总结

- 掌握项目初始化和代码管理
- 完成核心功能模块编码实现
- 掌握AI辅助编码工具的使用
- 完成单元测试
- 项目可正常运行

### 课后思考

1. 项目开发中的常见问题及解决方法
   - 如何处理依赖冲突？
   - 如何优化数据库查询？
2. AI辅助编码的优势和局限性
   - AI生成的代码质量如何保证？
   - 如何有效利用AI辅助工具？
3. 如何保证代码质量
   - 代码审查的最佳实践？
   - 如何提高测试覆盖率？
4. 团队协作开发
   - 如何有效进行代码合并？
   - 如何解决代码冲突？
5. 持续集成与部署
   - 如何建立CI/CD流程？
   - 如何自动化测试和部署？1、2、3

