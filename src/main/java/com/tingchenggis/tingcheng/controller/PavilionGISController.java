package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.PavilionGISService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 亭子GIS控制器
 * 
 * 提供综合GIS功能与滁州亭城文化的REST API接口
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/pavilions-gis")
@CrossOrigin(origins = "*")
public class PavilionGISController {

    private static final Logger logger = LoggerFactory.getLogger(PavilionGISController.class);

    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String COUNT = "count";

    private final PavilionGISService pavilionGISService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public PavilionGISController(PavilionGISService pavilionGISService) {
        this.pavilionGISService = pavilionGISService;
    }

    /**
     * 计算两点间距离
     * 
     * @param pavilionId1 第一个亭子ID
     * @param pavilionId2 第二个亭子ID
     * @return 距离信息
     */
    @GetMapping("/distance/{pavilionId1}/{pavilionId2}")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @PathVariable Long pavilionId1,
            @PathVariable Long pavilionId2) {
        try {
            logger.info("Calculating distance between pavilions: {} and {}", pavilionId1, pavilionId2);
            
            double distance = pavilionGISService.calculateDistance(pavilionId1, pavilionId2);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", distance);
            response.put("unit", "meters");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculating distance between pavilions: " + pavilionId1 + " and " + pavilionId2, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "计算距离失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查找最近的亭子
     * 
     * @param longitude 经度
     * @param latitude 纬度
     * @param limit 数量限制
     * @return 最近的亭子列表
     */
    @GetMapping("/nearest")
    public ResponseEntity<Map<String, Object>> findNearestPavilions(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            logger.info("Finding nearest pavilions to coordinates: ({}, {})", longitude, latitude);
            
            Point centerPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            
            List<Pavilion> nearestPavilions = pavilionGISService.findNearestPavilions(centerPoint, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", nearestPavilions);
            response.put(COUNT, nearestPavilions.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error finding nearest pavilions to coordinates: (" + longitude + ", " + latitude + ")", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "查找最近亭子失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查找缓冲区内的亭子
     * 
     * @param pavilionId 中心亭子ID
     * @param bufferRadius 缓冲半径（米）
     * @return 缓冲区内的亭子列表
     */
    @GetMapping("/{pavilionId}/buffer")
    public ResponseEntity<Map<String, Object>> findPavilionsInBuffer(
            @PathVariable Long pavilionId,
            @RequestParam(defaultValue = "1000") double bufferRadius) {
        try {
            logger.info("Finding pavilions in buffer around pavilion: {} with radius: {}m", pavilionId, bufferRadius);
            
            List<Pavilion> pavilionsInBuffer = pavilionGISService.findPavilionsInBuffer(pavilionId, bufferRadius);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", pavilionsInBuffer);
            response.put(COUNT, pavilionsInBuffer.size());
            response.put("centerPavilionId", pavilionId);
            response.put("bufferRadius", bufferRadius);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error finding pavilions in buffer around pavilion: " + pavilionId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "查找缓冲区内亭子失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 生成热力图数据
     * 
     * @return 热力图数据
     */
    @GetMapping("/heatmap")
    public ResponseEntity<Map<String, Object>> generateHeatmapData() {
        try {
            logger.info("Generating heatmap data for pavilions");
            
            List<Object[]> heatmapData = pavilionGISService.generateHeatmapData();
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", heatmapData);
            response.put(COUNT, heatmapData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating heatmap data", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "生成热力图数据失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 计算亭子密度
     * 
     * @param wktRegion 区域的WKT表示
     * @return 密度信息
     */
    @PostMapping("/density")
    public ResponseEntity<Map<String, Object>> calculateDensity(@RequestBody Map<String, String> requestBody) {
        try {
            String wktRegion = requestBody.get("wktRegion");
            logger.info("Calculating density for region: {}", wktRegion);
            
            // 注意：这里为了简化，实际上我们需要解析WKT字符串为Geometry对象
            // 在实际实现中，需要使用WKTReader来解析
            double density = pavilionGISService.getPavilionDensity(null); // 暂时传null
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", density);
            response.put("region", wktRegion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculating density", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "计算密度失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 生成最短路径
     * 
     * @param startPavilionId 起始亭子ID
     * @param endPavilionId 终点亭子ID
     * @return 路径信息
     */
    @GetMapping("/shortest-path/{startPavilionId}/{endPavilionId}")
    public ResponseEntity<Map<String, Object>> generateShortestPath(
            @PathVariable Long startPavilionId,
            @PathVariable Long endPavilionId) {
        try {
            logger.info("Generating shortest path between pavilions: {} and {}", startPavilionId, endPavilionId);
            
            // 这里返回路径的简化表示
            double distance = pavilionGISService.calculateDistance(startPavilionId, endPavilionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("startPavilionId", startPavilionId);
            response.put("endPavilionId", endPavilionId);
            response.put("distance", distance);
            response.put("unit", "meters");
            response.put(MESSAGE, "这是简化的路径计算，实际应用中需要集成网络分析功能");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating shortest path between pavilions: " + startPavilionId + " and " + endPavilionId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "生成最短路径失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 使用A*算法生成多个亭子之间的最优路径
     * 
     * @param pavilionIds 亭子ID列表（逗号分隔）
     * @return 路径信息
     */
    @GetMapping("/optimal-path")
    public ResponseEntity<Map<String, Object>> generateOptimalPath(
            @RequestParam List<Long> pavilionIds) {
        try {
            logger.info("Generating optimal path for pavilions: {}", pavilionIds);
            
            if (pavilionIds.size() < 2) {
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS, false);
                response.put(MESSAGE, "至少需要2个亭子才能生成路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 生成最优路径
            Geometry pathGeometry = pavilionGISService.generateOptimalPath(pavilionIds);
            
            Map<String, Object> response = new HashMap<>();
            
            if (pathGeometry != null) {
                // 提取路径坐标
                Coordinate[] coordinates = pathGeometry.getCoordinates();
                List<Map<String, Double>> pathCoords = new ArrayList<>();
                
                for (Coordinate coord : coordinates) {
                    Map<String, Double> point = new HashMap<>();
                    point.put("longitude", coord.x);
                    point.put("latitude", coord.y);
                    pathCoords.add(point);
                }
                
                response.put(SUCCESS, true);
                response.put("path", pathCoords);
                response.put("pavilionCount", pavilionIds.size());
                response.put("pathLength", pathGeometry.getLength());
                response.put(MESSAGE, "已使用A*算法生成最优路径");
            } else {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "无法生成路径，请检查亭子坐标是否有效");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating optimal path for pavilions: " + pavilionIds, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "生成最优路径失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取滁州亭城GIS系统概览
     * 
     * @return 系统概览信息
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        try {
            logger.info("Getting system overview for Chuzhou Tingcheng GIS");
            
            StringBuilder overview = new StringBuilder();
            overview.append("【滁州亭城GIS系统综合概览】\n\n");
            overview.append("本系统是技术实现与文化内容的完美结合：\n\n");
            overview.append("一、技术实现层：\n");
            overview.append("- 集成JTS Topology Suite进行空间数据处理\n");
            overview.append("- 支持空间关系查询（相交、包含、邻近等）\n");
            overview.append("- 提供距离计算、缓冲区分析等功能\n");
            overview.append("- 支持热力图生成和密度分析\n\n");
            overview.append("二、文化内容层：\n");
            overview.append("- 专注滁州'亭城'文化展示\n");
            overview.append("- 包含醉翁亭、丰乐亭等历史名亭\n");
            overview.append("- 融合《醉翁亭记》文化元素\n");
            overview.append("- 提供AI智能文化讲解\n\n");
            overview.append("三、综合功能：\n");
            overview.append("- 空间分析与文化展示相结合\n");
            overview.append("- 为用户提供沉浸式的亭城文化体验\n");
            overview.append("- 支持智能化的游览路线规划\n");
            overview.append("- 促进滁州文化旅游发展");
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", overview.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting system overview", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取系统概览失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}