package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.TransportRoute;
import com.tingchenggis.tingcheng.service.TransportRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交通线控制器
 *
 * 提供交通线管理的REST API接口
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/transport-routes")
@CrossOrigin(origins = "*")
public class TransportRouteController {

    private static final Logger logger = LoggerFactory.getLogger(TransportRouteController.class);
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    private final TransportRouteService transportRouteService;

    public TransportRouteController(TransportRouteService transportRouteService) {
        this.transportRouteService = transportRouteService;
    }

    /**
     * 获取所有交通线
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoutes() {
        try {
            logger.info("获取所有交通线");

            List<TransportRoute> routes = transportRouteService.getAllRoutes();

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", routes);
            response.put("count", routes.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取交通线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取交通线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取交通路线统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRouteStats() {
        try {
            logger.info("获取交通路线统计信息");

            Map<String, Object> stats = transportRouteService.getRouteStats();

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取统计信息失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取统计信息失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取所有风景路线
     */
    @GetMapping("/scenic")
    public ResponseEntity<Map<String, Object>> getScenicRoutes() {
        try {
            logger.info("获取所有风景路线");

            List<TransportRoute> routes = transportRouteService.getScenicRoutes();

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", routes);
            response.put("count", routes.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取风景路线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取风景路线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据交通方式获取路线
     */
    @GetMapping("/by-mode/{mode}")
    public ResponseEntity<Map<String, Object>> getRoutesByMode(@PathVariable String mode) {
        try {
            List<TransportRoute> routes = transportRouteService.getRoutesByTransportMode(mode.toUpperCase());
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", routes);
            response.put("count", routes.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取路线失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取所有可用的交通方式
     */
    @GetMapping("/modes")
    public ResponseEntity<Map<String, Object>> getAvailableModes() {
        try {
            List<String> modes = transportRouteService.getAvailableTransportModes();
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", modes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取交通方式失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取从指定亭子出发的所有交通线
     */
    @GetMapping("/from/{pavilionId}")
    public ResponseEntity<Map<String, Object>> getRoutesFromPavilion(@PathVariable Long pavilionId) {
        try {
            logger.info("获取从亭子 {} 出发的交通线", pavilionId);

            List<TransportRoute> routes = transportRouteService.getRoutesFromPavilion(pavilionId);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", routes);
            response.put("count", routes.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取交通线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取交通线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取两个亭子之间的交通线
     */
    @GetMapping("/between/{pavilionId1}/{pavilionId2}")
    public ResponseEntity<Map<String, Object>> getRouteBetweenPavilions(
            @PathVariable Long pavilionId1,
            @PathVariable Long pavilionId2) {
        try {
            logger.info("获取亭子 {} 和 {} 之间的交通线", pavilionId1, pavilionId2);

            TransportRoute route = transportRouteService.getRouteBetweenPavilions(pavilionId1, pavilionId2);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", route);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取交通线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取交通线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据ID获取交通线
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRouteById(@PathVariable Long id) {
        try {
            logger.info("获取交通线 ID: {}", id);

            TransportRoute route = transportRouteService.getRouteById(id);

            if (route == null) {
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS, false);
                response.put(MESSAGE, "交通线不存在");

                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", route);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取交通线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "获取交通线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 创建交通线
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRoute(@RequestBody TransportRoute route) {
        try {
            logger.info("创建交通线: {} -> {}", route.getFromPavilionId(), route.getToPavilionId());

            TransportRoute savedRoute = transportRouteService.createRoute(route);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MESSAGE, "交通线创建成功");
            response.put("data", savedRoute);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("创建交通线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "创建交通线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 更新交通线
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRoute(@PathVariable Long id, @RequestBody TransportRoute route) {
        try {
            logger.info("更新交通线 ID: {}", id);

            TransportRoute updatedRoute = transportRouteService.updateRoute(id, route);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MESSAGE, "交通线更新成功");
            response.put("data", updatedRoute);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新交通线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "更新交通线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除交通线
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoute(@PathVariable Long id) {
        try {
            logger.info("删除交通线 ID: {}", id);

            transportRouteService.deleteRoute(id);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put(MESSAGE, "交通线删除成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除交通线失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "删除交通线失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * TSP遍历路线规划（基于OSRM实时路网，含动画数据）
     */
    @PostMapping("/tsp-plan")
    public ResponseEntity<Map<String, Object>> getTspPlan(@RequestBody TspPlanRequest request) {
        try {
            logger.info("TSP路线规划: {}个亭子, 模式={}, 目标={}",
                request.getPavilionIds().size(), request.getMode(), request.getObjective());
            Map<String, Object> plan = transportRouteService.getTspRoute(
                request.getPavilionIds(), request.getMode(), request.getObjective());
            plan.put("success", true);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            logger.error("TSP路线规划失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "TSP路线规划失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    public static class TspPlanRequest {
        private List<Long> pavilionIds;
        private String mode;
        private String objective;

        public List<Long> getPavilionIds() { return pavilionIds; }
        public void setPavilionIds(List<Long> pavilionIds) { this.pavilionIds = pavilionIds; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getObjective() { return objective; }
        public void setObjective(String objective) { this.objective = objective; }
    }

    /**
     * 多模式路网构建（每对亭子构建 driving/cycling/foot 多套路线）
     */
    @PostMapping("/build-multi-modal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> buildMultiModal() {
        try {
            Map<String, Object> network = transportRouteService.buildMultiModalNetwork();
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", network);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("构建多模式路网失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "构建多模式路网失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 构建多级道路网络
     * 基于实际亭子数据，通过OSRM为所有亭子对之间创建道路连接
     */
    @PostMapping("/build-network")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> buildRoadNetwork() {
        try {
            logger.info("构建多级道路网络");

            Map<String, Object> network = transportRouteService.buildRoadNetwork();

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, true);
            response.put("data", network);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("构建路网失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS, false);
            response.put(MESSAGE, "构建路网失败: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
