# 实验四 AI辅助测试与空间软件测试

## 实验项目基本信息

- **实验编号**：d20301035104
- **学时分配**：2学时
- **实验类型**：验证型
- **每组人数**：5人
- **对应课程目标**：课程目标2

***

## 🎯 核心步骤列表

### 📋 实验概览

1. **测试用例设计阶段**：设计功能测试用例和空间精度测试用例
2. **单元测试阶段**：使用JUnit进行后端单元测试
3. **集成测试阶段**：测试API接口和数据库交互
4. **AI辅助测试阶段**：使用TRAE生成测试用例和检测异常
5. **测试报告阶段**：生成测试报告并分析结果

### 🛠️ 工具清单

| 序号 | 开发工具     | 主要用途       | 验证项目      |
| -- | -------- | ---------- | --------- |
| 1  | JUnit    | Java单元测试框架 | 后端服务测试    |
| 2  | Postman  | API测试工具    | 接口测试      |
| 3  | PostGIS  | 空间数据库      | 空间数据测试    |
| 4  | TRAE     | AI辅助测试     | 用例生成、异常检测 |
| 5  | Opencode | AI辅助编程     | 测试代码补全    |

**亭城GIS子项目测试环境要求：**

| 环境组件       | 版本要求  | 说明                       |
| ---------- | ----- | ------------------------ |
| JUnit      | 5.10+ | Java单元测试框架               |
| Mockito    | 5.5+  | Mock框架，用于模拟依赖            |
| Postman    | 最新版本  | API接口测试工具                |
| PostgreSQL | 16+   | 关系型数据库，存储测试数据            |
| PostGIS    | 3.4+  | PostgreSQL的空间扩展，支持空间查询测试 |
| TRAE       | 最新版本  | AI辅助测试工具，生成测试用例          |
| Opencode   | 最新版本  | AI辅助编程工具，测试代码补全          |

### ✅ 成功标准

- [ ] 测试用例设计完成
- [ ] 单元测试执行通过
- [ ] 集成测试执行通过
- [ ] AI辅助测试完成
- [ ] 测试报告生成
- [ ] 测试结果分析完成

***

## 1. 测试用例设计

### 1.1 功能测试用例设计

#### 步骤1：亭子管理功能测试用例

| 用例编号   | 用例名称 | 前置条件    | 输入数据                                           | 预期结果         |
| ------ | ---- | ------- | ---------------------------------------------- | ------------ |
| TC-001 | 添加亭子 | 系统正常运行  | name="醉翁亭", longitude=118.317, latitude=32.317 | 成功添加，返回亭子ID  |
| TC-002 | 查询亭子 | 亭子已存在   | id=1                                           | 返回亭子详细信息     |
| TC-003 | 更新亭子 | 亭子已存在   | id=1, name="新醉翁亭"                              | 成功更新，返回更新后信息 |
| TC-004 | 删除亭子 | 亭子已存在   | id=1                                           | 成功删除，返回成功消息  |
| TC-005 | 搜索亭子 | 系统有亭子数据 | keyword="醉翁"                                   | 返回匹配的亭子列表    |

#### 步骤2：空间查询功能测试用例

| 用例编号   | 用例名称     | 前置条件    | 输入数据                                             | 预期结果       |
| ------ | -------- | ------- | ------------------------------------------------ | ---------- |
| TC-010 | 范围查询-正常  | 系统有亭子数据 | longitude=118.317, latitude=32.317, radius=1000  | 返回范围内的亭子列表 |
| TC-011 | 范围查询-边界  | 系统有亭子数据 | longitude=118.317, latitude=32.317, radius=0     | 返回空列表      |
| TC-012 | 范围查询-大范围 | 系统有亭子数据 | longitude=118.317, latitude=32.317, radius=10000 | 返回大量亭子数据   |

#### 步骤3：AI服务功能测试用例

| 用例编号   | 用例名称   | 前置条件    | 输入数据               | 预期结果     |
| ------ | ------ | ------- | ------------------ | -------- |
| TC-020 | AI亭子推荐 | 系统正常运行  | userId=1, count=5  | 返回推荐亭子列表 |
| TC-021 | AI路线规划 | 系统有亭子数据 | startId=1, endId=5 | 返回最优路线   |

### 1.2 空间精度测试用例设计

#### 步骤1：坐标精度测试用例

| 用例编号   | 测试项目      | 测试方法        | 精度要求      | 验证标准         |
| ------ | --------- | ----------- | --------- | ------------ |
| SP-001 | 经度存储精度    | 存储后读取比较     | 小数点后6位    | 误差<0.000001° |
| SP-002 | 纬度存储精度    | 存储后读取比较     | 小数点后6位    | 误差<0.000001° |
| SP-003 | GEOM字段正确性 | PostGIS函数验证 | EPSG:4326 | 坐标系正确        |

#### 步骤2：距离计算精度测试用例

| 用例编号   | 测试项目   | 测试方法           | 精度要求 | 验证标准     |
| ------ | ------ | -------------- | ---- | -------- |
| SP-010 | 短距离计算  | ST\_Distance函数 | 1米   | 误差<1米    |
| SP-011 | 中距离计算  | ST\_Distance函数 | 10米  | 误差<10米   |
| SP-012 | 范围查询精度 | ST\_DWithin函数  | 10米  | 边界误差<10米 |

### 1.3 性能测试用例设计

| 用例编号   | 测试项目    | 测试条件    | 性能指标    | 验证标准    |
| ------ | ------- | ------- | ------- | ------- |
| PT-001 | API响应时间 | 单次请求    | <200ms  | 95%请求满足 |
| PT-002 | 空间查询性能  | 1000条数据 | <500ms  | 95%请求满足 |
| PT-003 | 并发处理能力  | 100并发用户 | <1000ms | 90%请求满足 |

***

## 2. 单元测试实现

### 2.1 后端服务单元测试

#### 步骤1：PavilionService测试

```java
package com.tingcheng.gis.service;

import com.tingcheng.gis.entity.Pavilion;
import com.tingcheng.gis.repository.PavilionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PavilionServiceTest {

    @Mock
    private PavilionRepository pavilionRepository;

    @InjectMocks
    private PavilionService pavilionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchPavilions() {
        // 准备测试数据
        Pavilion pavilion = new Pavilion();
        pavilion.setName("zuigengting");
        pavilion.setChineseName("醉翁亭");
        
        when(pavilionRepository.findByNameContainingOrChineseNameContaining("醉翁", "醉翁"))
            .thenReturn(Arrays.asList(pavilion));
        
        // 执行测试
        List<Pavilion> result = pavilionService.searchPavilions("醉翁");
        
        // 验证结果
        assertEquals(1, result.size());
        assertEquals("醉翁亭", result.get(0).getChineseName());
        verify(pavilionRepository, times(1))
            .findByNameContainingOrChineseNameContaining("醉翁", "醉翁");
    }

    @Test
    void testGetPavilionById() {
        // 准备测试数据
        Pavilion pavilion = new Pavilion();
        pavilion.setId(1L);
        pavilion.setName("zuigengting");
        
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(pavilion));
        
        // 执行测试
        Pavilion result = pavilionService.getPavilionById(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testSavePavilion() {
        // 准备测试数据
        Pavilion pavilion = new Pavilion();
        pavilion.setName("test");
        pavilion.setLongitude(118.317);
        pavilion.setLatitude(32.317);
        
        when(pavilionRepository.save(any(Pavilion.class))).thenReturn(pavilion);
        
        // 执行测试
        Pavilion result = pavilionService.savePavilion(pavilion);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getGeom());
        assertTrue(result.getGeom().contains("POINT"));
    }
}
```

#### 步骤2：SpatialService测试

```java
package com.tingcheng.gis.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpatialServiceTest {

    @Test
    void testCalculateDistance() {
        // 测试距离计算
        // 滁州到南京约50公里
        double distance = SpatialService.calculateDistance(
            118.317, 32.317,  // 滁州
            118.778, 32.057   // 南京
        );
        
        // 验证距离在合理范围内
        assertTrue(distance > 45000 && distance < 55000, 
            "距离应该在45000-55000米之间，实际为: " + distance);
    }

    @Test
    void testIsWithinRadius() {
        // 测试范围判断
        boolean result = SpatialService.isWithinRadius(
            118.317, 32.317,  // 中心点
            118.320, 32.320,  // 目标点
            1000              // 半径1000米
        );
        
        assertTrue(result, "目标点应该在范围内");
    }
}
```

### 2.2 控制器单元测试

#### 步骤1：PavilionController测试

```java
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
        // 准备测试数据
        Pavilion pavilion = new Pavilion();
        pavilion.setId(1L);
        pavilion.setName("zuigengting");
        pavilion.setChineseName("醉翁亭");
        
        when(pavilionService.searchPavilions("醉翁"))
            .thenReturn(Arrays.asList(pavilion));
        
        // 执行测试
        mockMvc.perform(get("/api/pavilions/search")
                .param("keyword", "醉翁"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].chineseName").value("醉翁亭"));
    }

    @Test
    void testFindWithinRadius() throws Exception {
        // 准备测试数据
        Pavilion pavilion = new Pavilion();
        pavilion.setId(1L);
        pavilion.setChineseName("醉翁亭");
        
        when(pavilionService.findPavilionsWithinRadius(118.317, 32.317, 1000))
            .thenReturn(Arrays.asList(pavilion));
        
        // 执行测试
        mockMvc.perform(get("/api/pavilions/within-radius")
                .param("longitude", "118.317")
                .param("latitude", "32.317")
                .param("radius", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].chineseName").value("醉翁亭"));
    }
}
```

***

## 3. 集成测试实现

### 3.1 API接口集成测试

#### 步骤1：使用Postman测试API

1. **测试亭子CRUD接口**
   - POST `/api/pavilions` - 添加亭子
   - GET `/api/pavilions/{id}` - 查询亭子
   - PUT `/api/pavilions/{id}` - 更新亭子
   - DELETE `/api/pavilions/{id}` - 删除亭子
2. **测试空间查询接口**
   - GET `/api/pavilions/search?keyword=醉翁` - 搜索亭子
   - GET `/api/pavilions/within-radius?longitude=118.317&latitude=32.317&radius=1000` - 范围查询

#### 步骤2：Postman测试脚本

```javascript
// 测试添加亭子
pm.test("添加亭子成功", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("pavilionId", jsonData.id);
});

// 测试范围查询
pm.test("范围查询返回正确数据", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.length).to.be.greaterThan(0);
    jsonData.forEach(function(item) {
        pm.expect(item.chineseName).to.exist;
        pm.expect(item.longitude).to.exist;
        pm.expect(item.latitude).to.exist;
    });
});
```

### 3.2 数据库集成测试

#### 步骤1：空间数据测试

```sql
-- 测试空间数据插入
INSERT INTO pavilion (name, chinese_name, longitude, latitude, geom)
VALUES ('test', '测试亭', 118.317, 32.317, 
        ST_SetSRID(ST_MakePoint(118.317, 32.317), 4326));

-- 验证空间数据正确性
SELECT name, ST_AsText(geom) as geom_text, ST_SRID(geom) as srid
FROM pavilion WHERE name = 'test';

-- 测试空间查询
SELECT * FROM pavilion 
WHERE ST_DWithin(geom, ST_SetSRID(ST_MakePoint(118.317, 32.317), 4326), 1000);

-- 测试距离计算
SELECT name, 
       ST_Distance(geom, ST_SetSRID(ST_MakePoint(118.317, 32.317), 4326)) * 111000 as distance_meters
FROM pavilion
ORDER BY distance_meters
LIMIT 5;
```

#### 步骤2：空间索引测试

```sql
-- 检查空间索引是否存在
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'pavilion';

-- 创建空间索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_pavilion_geom ON pavilion USING GIST(geom);

-- 测试空间查询性能
EXPLAIN ANALYZE
SELECT * FROM pavilion 
WHERE ST_DWithin(geom, ST_SetSRID(ST_MakePoint(118.317, 32.317), 4326), 1000);
```

***

## 4. AI辅助测试实践

### 4.1 TRAE生成测试用例

#### 步骤1：使用TRAE生成边界测试用例

1. 打开TRAE插件
2. 输入自然语言描述："为PavilionService的searchPavilions方法生成边界测试用例"
3. TRAE生成的测试用例示例：

```java
@Test
void testSearchPavilions_EmptyKeyword() {
    // 测试空关键词
    List<Pavilion> result = pavilionService.searchPavilions("");
    assertTrue(result.isEmpty());
}

@Test
void testSearchPavilions_NullKeyword() {
    // 测试null关键词
    assertThrows(IllegalArgumentException.class, () -> {
        pavilionService.searchPavilions(null);
    });
}

@Test
void testSearchPavilions_SpecialCharacters() {
    // 测试特殊字符
    List<Pavilion> result = pavilionService.searchPavilions("'; DROP TABLE pavilion;--");
    assertTrue(result.isEmpty());
}
```

#### 步骤2：使用TRAE生成异常测试用例

```java
@Test
void testSavePavilion_NullLongitude() {
    // 测试经度为null
    Pavilion pavilion = new Pavilion();
    pavilion.setName("test");
    pavilion.setLatitude(32.317);
    
    assertThrows(IllegalArgumentException.class, () -> {
        pavilionService.savePavilion(pavilion);
    });
}

@Test
void testSavePavilion_InvalidCoordinates() {
    // 测试无效坐标
    Pavilion pavilion = new Pavilion();
    pavilion.setName("test");
    pavilion.setLongitude(200.0);  // 无效经度
    pavilion.setLatitude(32.317);
    
    assertThrows(IllegalArgumentException.class, () -> {
        pavilionService.savePavilion(pavilion);
    });
}
```

### 4.2 TRAE检测空间数据异常

#### 步骤1：坐标异常检测

```java
@Test
void testCoordinateValidation() {
    // 测试坐标范围验证
    assertThrows(IllegalArgumentException.class, () -> {
        Pavilion pavilion = new Pavilion();
        pavilion.setLongitude(181.0);  // 超出范围
        pavilion.setLatitude(32.317);
        pavilionService.savePavilion(pavilion);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        Pavilion pavilion = new Pavilion();
        pavilion.setLongitude(118.317);
        pavilion.setLatitude(91.0);  // 超出范围
        pavilionService.savePavilion(pavilion);
    });
}
```

#### 步骤2：拓扑错误检测

```sql
-- 检测自相交多边形
SELECT name, ST_IsValid(geom) as is_valid, ST_IsValidReason(geom) as reason
FROM pavilion
WHERE NOT ST_IsValid(geom);

-- 检测重复点
SELECT name, ST_NPoints(geom) as point_count
FROM pavilion
WHERE ST_NPoints(geom) != ST_NPoints(ST_RemoveRepeatedPoints(geom));
```

### 4.3 AI辅助测试优化

#### 步骤1：测试优先级排序

使用TRAE分析测试用例的重要性：

```java
// 高优先级测试：核心功能
@Tag("high-priority")
@Test
void testSearchPavilions_CoreFunction() {
    // 核心搜索功能测试
}

// 中优先级测试：边界条件
@Tag("medium-priority")
@Test
void testSearchPavilions_BoundaryCondition() {
    // 边界条件测试
}

// 低优先级测试：性能
@Tag("low-priority")
@Test
void testSearchPavilions_Performance() {
    // 性能测试
}
```

#### 步骤2：回归测试选择

```java
// 使用TRAE识别受影响的测试用例
// 当修改PavilionService时，自动选择相关测试
@Tag("pavilion-service")
class PavilionServiceTest {
    // 所有PavilionService相关测试
}
```

***

## 5. 测试报告生成

### 5.1 测试报告结构

#### 步骤1：测试概述

```
测试项目：亭城GIS系统测试报告
测试日期：2024年XX月XX日
测试人员：XXX
测试环境：Spring Boot 3.1.0 + PostgreSQL 16 + PostGIS 3.4
```

#### 步骤2：测试执行情况

| 测试类型   | 用例总数 | 通过数 | 失败数 | 通过率  |
| ------ | ---- | --- | --- | ---- |
| 单元测试   | 25   | 24  | 1   | 96%  |
| 集成测试   | 15   | 14  | 1   | 93%  |
| 空间精度测试 | 10   | 10  | 0   | 100% |
| 性能测试   | 5    | 4   | 1   | 80%  |

#### 步骤3：缺陷统计

| 缺陷编号    | 缺陷描述          | 严重程度 | 状态  |
| ------- | ------------- | ---- | --- |
| BUG-001 | 范围查询边界条件处理不正确 | 中    | 已修复 |
| BUG-002 | 并发请求响应时间过长    | 低    | 待修复 |

### 5.2 空间数据精度测试结果

#### 步骤1：精度指标统计

| 测试项目   | 测试次数 | 平均误差       | 最大误差       | 是否达标 |
| ------ | ---- | ---------- | ---------- | ---- |
| 坐标存储精度 | 100  | 0.0000001° | 0.0000005° | 是    |
| 距离计算精度 | 50   | 0.5米       | 2米         | 是    |
| 范围查询精度 | 30   | 5米         | 8米         | 是    |

#### 步骤2：误差分析

```
坐标存储精度分析：
- 经度误差主要来源于浮点数精度限制
- 纬度误差与经度误差类似
- 误差在可接受范围内（<0.000001°）

距离计算精度分析：
- 短距离计算误差较小（<1米）
- 中长距离计算误差略有增加（<10米）
- 使用ST_Distance函数精度较高

范围查询精度分析：
- 边界条件处理需要优化
- 建议增加边界缓冲区
```

### 5.3 测试结论

#### 步骤1：通过/不通过判定

```
测试结论：通过

判定依据：
1. 单元测试通过率96%，满足>90%的要求
2. 集成测试通过率93%，满足>85%的要求
3. 空间精度测试全部通过
4. 性能测试通过率80%，基本满足要求
```

#### 步骤2：风险评估

```
风险等级：低

风险项：
1. 并发性能需要进一步优化
2. 边界条件处理需要完善

建议措施：
1. 增加缓存机制提升性能
2. 完善边界条件测试用例
```

#### 步骤3：改进建议

```
改进建议：
1. 增加更多边界条件测试用例
2. 优化空间查询性能
3. 增加压力测试
4. 完善AI辅助测试流程
5. 建立持续集成测试环境
```

***

## 总结与思考

### 实验总结

- 掌握测试用例设计方法，包括功能测试、空间精度测试、性能测试
- 掌握JUnit单元测试框架的使用
- 掌握Postman API接口测试方法
- 掌握PostGIS空间数据测试方法
- 掌握AI辅助测试工具的使用
- 完成了亭城GIS系统的测试工作
- 生成了完整的测试报告

### 课后思考

1. 单元测试与集成测试的区别和联系
   - 单元测试侧重于单个模块的功能验证
   - 集成测试侧重于模块间的交互验证
   - 如何在项目中平衡单元测试和集成测试的比例？
2. 空间软件测试的特殊性
   - 空间数据精度要求高
   - 空间查询性能测试复杂
   - 如何设计更有效的空间数据测试用例？
3. AI辅助测试的优势和局限
   - AI可以快速生成测试用例
   - AI可以检测潜在异常
   - AI辅助测试的准确性如何保证？
4. 测试驱动开发（TDD）在GIS项目中的应用
   - 如何在GIS项目中实践TDD？
   - TDD对代码质量的影响？
5. 持续集成与自动化测试
   - 如何建立持续集成测试环境？
   - 自动化测试在GIS项目中的实践？
6. 测试报告的质量评估
   - 如何编写高质量的测试报告？
   - 测试报告对项目决策的影响？

