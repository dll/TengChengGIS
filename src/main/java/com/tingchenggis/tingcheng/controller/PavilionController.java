package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 亭子控制器
 * 
 * 提供亭子相关的REST API接口
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/pavilions")
@CrossOrigin(origins = "*")
public class PavilionController {

    private static final Logger logger = LoggerFactory.getLogger(PavilionController.class);

    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String COUNT = "count";

    private final PavilionService pavilionService;

    public PavilionController(PavilionService pavilionService) {
        this.pavilionService = pavilionService;
    }

    /**
     * 创建新的亭子
     * 
     * @param pavilion 亭子对象
     * @return 创建成功的亭子
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPavilion(@Valid @RequestBody Pavilion pavilion) {
        try {
            logger.info("Creating pavilion: {}", pavilion.getName());
            
            Pavilion createdPavilion = pavilionService.createPavilion(pavilion);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MESSAGE, "亭子创建成功");
        response.put("data", createdPavilion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating pavilion: ", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "创建亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取亭子
     * 
     * @param id 亭子ID
     * @return 对应的亭子
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPavilionById(@PathVariable Long id) {
        try {
            logger.debug("Fetching pavilion with ID: {}", id);
            
            Optional<Pavilion> pavilion = pavilionService.getPavilionById(id);
            
            if (pavilion.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS, true);
                response.put("data", pavilion.get());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS, false);
                response.put(MESSAGE, "未找到ID为 " + id + " 的亭子");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error fetching pavilion with ID: " + id, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有亭子（分页）
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @param sort 排序字段
     * @return 分页的亭子列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPavilions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        try {
            logger.debug("Fetching all pavilions with pagination - page: {}, size: {}", page, size);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
            Page<Pavilion> pavilionsPage = pavilionService.getAllPavilions(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", pavilionsPage.getContent());
            response.put("totalElements", pavilionsPage.getTotalElements());
            response.put("totalPages", pavilionsPage.getTotalPages());
            response.put("currentPage", pavilionsPage.getNumber());
            response.put("size", pavilionsPage.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching pavilions: ", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取亭子列表失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新亭子
     * 
     * @param id 亭子ID
     * @param pavilion 更新的数据
     * @return 更新后的亭子
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePavilion(
            @PathVariable Long id, 
            @Valid @RequestBody Pavilion pavilion) {
        
        try {
            logger.info("Updating pavilion with ID: {}", id);
            
            Pavilion updatedPavilion = pavilionService.updatePavilion(id, pavilion);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MESSAGE, "亭子更新成功");
        response.put("data", updatedPavilion);
            
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException e) {
            logger.warn("Pavilion not found for update: " + id);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error updating pavilion with ID: " + id, e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "更新亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除亭子
     * 
     * @param id 亭子ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePavilion(@PathVariable Long id) {
        try {
            logger.info("Deleting pavilion with ID: {}", id);
            
            pavilionService.deletePavilion(id);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MESSAGE, "亭子删除成功");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Pavilion not found for deletion: " + id);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error deleting pavilion with ID: " + id, e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "删除亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据亭子类型查询
     * 
     * @param pavilionType 亭子类型
     * @return 符合条件的亭子列表
     */
    @GetMapping("/type/{pavilionType}")
    public ResponseEntity<Map<String, Object>> findByPavilionType(@PathVariable String pavilionType) {
        try {
            logger.debug("Finding pavilions by type: {}", pavilionType);
            
            List<Pavilion> pavilions = pavilionService.findByPavilionType(pavilionType);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("data", pavilions);
        response.put(COUNT, pavilions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error finding pavilions by type: " + pavilionType, e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "查询亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据名称模糊查询
     * 
     * @param name 名称关键词
     * @return 符合条件的亭子列表
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> findByNameContaining(@RequestParam String name) {
        try {
            logger.debug("Finding pavilions containing name: {}", name);
            
            List<Pavilion> pavilions = pavilionService.findByNameContaining(name);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("data", pavilions);
        response.put(COUNT, pavilions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error finding pavilions by name: " + name, e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "查询亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据年代区间查询
     * 
     * @param startYear 起始年份
     * @param endYear 结束年份
     * @return 符合条件的亭子列表
     */
    @GetMapping("/by-year-range")
    public ResponseEntity<Map<String, Object>> findByBuiltYearBetween(
            @RequestParam Integer startYear, 
            @RequestParam Integer endYear) {
        try {
            logger.debug("Finding pavilions between years: {} and {}", startYear, endYear);
            
            List<Pavilion> pavilions = pavilionService.findByBuiltYearBetween(startYear, endYear);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("data", pavilions);
        response.put(COUNT, pavilions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error finding pavilions by year range: {} to {}", startYear, endYear, e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "按年代查询亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据评分查询热门亭子
     * 
     * @param minRating 最低评分
     * @return 评分高于指定值的亭子列表
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> findByVisitorRatingGreaterThanEqual(@RequestParam Double minRating) {
        try {
            logger.debug("Finding pavilions with rating >= {}", minRating);
            
            List<Pavilion> pavilions = pavilionService.findByVisitorRatingGreaterThanEqual(minRating);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("data", pavilions);
        response.put(COUNT, pavilions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error finding popular pavilions with rating >= {}", minRating, e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "查询热门亭子失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 地理位置范围查询
     * 
     * @param wktText WKT格式的空间范围描述
     * @return 在指定范围内的亭子列表
     */
    @PostMapping("/geographic-search")
    public ResponseEntity<Map<String, Object>> findByGeographicRange(@RequestBody Map<String, String> requestBody) {
        try {
            String wktText = requestBody.get("wktText");
            logger.debug("Finding pavilions in geographic range: {}", wktText);
            
            List<Pavilion> pavilions = pavilionService.findByGeographicRange(wktText);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("data", pavilions);
        response.put(COUNT, pavilions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error performing geographic search", e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "执行地理位置查询失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取亭子统计数据
     * 
     * @return 亭子统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPavilionStats() {
        try {
            logger.debug("Fetching pavilion statistics");
            
            com.tingchenggis.tingcheng.service.PavilionStats stats = pavilionService.getStats();
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching pavilion statistics", e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "获取亭子统计数据失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * AI智能推荐亭子
     * 
     * @param userId 用户ID
     * @param preferences 用户偏好
     * @return 推荐的亭子列表
     */
    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> recommendPavilions(
            @RequestParam String userId, 
            @RequestParam String preferences) {
        try {
            logger.info("Generating recommendations for user: {} with preferences: {}", userId, preferences);
            
            List<Pavilion> recommendedPavilions = pavilionService.recommendPavilions(userId, preferences);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put("data", recommendedPavilions);
        response.put(COUNT, recommendedPavilions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating recommendations for user: {}", userId, e);
            
            Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, "生成推荐失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}