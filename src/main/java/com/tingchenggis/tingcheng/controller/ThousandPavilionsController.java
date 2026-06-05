package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.dto.PavilionImportResult;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.entity.PavilionCollector;
import com.tingchenggis.tingcheng.exception.BusinessException;
import com.tingchenggis.tingcheng.exception.NotFoundException;
import com.tingchenggis.tingcheng.service.NavigationService;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import com.tingchenggis.tingcheng.service.PavilionExportService;
import com.tingchenggis.tingcheng.service.PavilionImportService;
import com.tingchenggis.tingcheng.service.PavilionService;
import com.tingchenggis.tingcheng.service.ThousandPavilionsService;
import com.tingchenggis.tingcheng.util.GeoUtils;
import com.tingchenggis.tingcheng.util.PavilionTypeUtils;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 遍历千亭功能控制器
 * 
 * 实现以下功能：
 * 1. 标注每个亭子的位置
 * 2. 亭间交通
 * 3. 遍历所有亭子的路线图
 * 4. 导航方案
 * 5. 各亭的多媒体导航
 * 6. 路线导航
 * 7. 旅游服务
 * 
 * @author TingChengGIS
 */
@RestController
@RequestMapping("/thousand-pavilions")
@CrossOrigin(origins = "*")
public class ThousandPavilionsController {

    private static final Logger logger = LoggerFactory.getLogger(ThousandPavilionsController.class);
    private static final String ESTIMATED_TIME = "estimatedTime";
    private static final String IMAGES_PATH = "/images/";

    private final PavilionService pavilionService;
    private final ThousandPavilionsService thousandPavilionsService;
    private final PavilionImportService pavilionImportService;
    private final PavilionExportService pavilionExportService;
    private final PavilionCollectorService collectorService;
    private final NavigationService navigationService;

    public ThousandPavilionsController(PavilionService pavilionService,
                                        ThousandPavilionsService thousandPavilionsService,
                                        PavilionImportService pavilionImportService,
                                        PavilionExportService pavilionExportService,
                                        PavilionCollectorService collectorService,
                                        NavigationService navigationService) {
        this.pavilionService = pavilionService;
        this.thousandPavilionsService = thousandPavilionsService;
        this.pavilionImportService = pavilionImportService;
        this.pavilionExportService = pavilionExportService;
        this.collectorService = collectorService;
        this.navigationService = navigationService;
    }

    /**
     * 获取所有亭子的位置标注
     */
    @GetMapping("/locations")
    public List<Map<String, Object>> getAllPavilionLocations(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean includeCollectorCounts) {
        List<Pavilion> pavilions = pavilionService.getAllPavilions();
        List<Map<String, Object>> locations = new ArrayList<>();

        Map<Long, Long> collectorCountMap = includeCollectorCounts
            ? collectorService.getCollectorCountByPavilionIds() : null;

        for (Pavilion pavilion : pavilions) {
            if (!PavilionTypeUtils.matchesFilter(pavilion.getPavilionType(), type)) {
                continue;
            }
            if (search != null && !search.isBlank()) {
                String keyword = search.trim().toLowerCase();
                boolean matched = (pavilion.getChineseName() != null && pavilion.getChineseName().toLowerCase().contains(keyword))
                    || (pavilion.getName() != null && pavilion.getName().toLowerCase().contains(keyword))
                    || (pavilion.getDescription() != null && pavilion.getDescription().toLowerCase().contains(keyword));
                if (!matched) {
                    continue;
                }
            }

            Map<String, Object> location = new HashMap<>();
            location.put("id", pavilion.getId());
            location.put("name", pavilion.getName());
            location.put("chineseName", pavilion.getChineseName());
            location.put("description", pavilion.getDescription());
            location.put("type", pavilion.getPavilionType());
            location.put("typeLabel", PavilionTypeUtils.toLabel(pavilion.getPavilionType()));
            location.put("visitorRating", pavilion.getVisitorRating());
            location.put("builtYear", pavilion.getBuiltYear());
            location.put("isOpenToPublic", pavilion.getIsOpenToPublic());
            location.put("structure", pavilion.getStructure());
            location.put("topStyle", pavilion.getTopStyle());
            location.put("street", pavilion.getStreet());
            location.put("notes", pavilion.getNotes());
            location.put("locationDesc", pavilion.getLocationDesc());
            location.put("areaSize", pavilion.getAreaSize());
            location.put("architecturalStyle", pavilion.getArchitecturalStyle());
            location.put("historicalSignificance", pavilion.getHistoricalSignificance());
            location.put("constructionPeriod", pavilion.getConstructionPeriod());
            location.put("ticketPrice", pavilion.getTicketPrice());
            location.put("lastRenovationYear", pavilion.getLastRenovationYear());
            location.put("englishName", pavilion.getName());

            if (pavilion.getLongitude() != null && pavilion.getLatitude() != null) {
                location.put("longitude", pavilion.getLongitude());
                location.put("latitude", pavilion.getLatitude());
            }
            if (pavilion.getLongitudeGcj() != null && pavilion.getLatitudeGcj() != null) {
                location.put("longitudeGcj", pavilion.getLongitudeGcj());
                location.put("latitudeGcj", pavilion.getLatitudeGcj());
            }

            if (collectorCountMap != null) {
                location.put("collectorCount", collectorCountMap.getOrDefault(pavilion.getId(), 0L));
            }

            locations.add(location);
        }

        return locations;
    }

    /**
     * 获取两个亭子之间的交通路线
     */
    @GetMapping("/route/{fromId}/{toId}")
    public Map<String, Object> getRouteBetweenPavilions(@PathVariable Long fromId, @PathVariable Long toId) {
        Pavilion fromPavilion = pavilionService.getPavilionById(fromId)
            .orElseThrow(() -> new NotFoundException("起始亭子不存在: " + fromId));
        Pavilion toPavilion = pavilionService.getPavilionById(toId)
            .orElseThrow(() -> new NotFoundException("目标亭子不存在: " + toId));

        Map<String, Object> routeInfo = new HashMap<>();
        routeInfo.put("from", fromPavilion.getChineseName());
        routeInfo.put("to", toPavilion.getChineseName());

        if (fromPavilion.getLatitude() != null && fromPavilion.getLongitude() != null &&
            toPavilion.getLatitude() != null && toPavilion.getLongitude() != null) {

            double distance = GeoUtils.haversineKm(
                fromPavilion.getLongitude(), fromPavilion.getLatitude(),
                toPavilion.getLongitude(), toPavilion.getLatitude()
            );
            routeInfo.put("distance", Math.round(distance * 100.0) / 100.0);
            routeInfo.put(ESTIMATED_TIME, Math.round(distance * 10));
        }

        routeInfo.put("transportation", "步行");
        routeInfo.put("scenicSpots", "沿途风景优美");

        return routeInfo;
    }

    /**
     * 获取遍历所有亭子的最优路线（简化版旅行商问题）
     */
    @GetMapping("/traverse-all")
    public Map<String, Object> getTraverseAllRoute() {
        List<Pavilion> pavilions = pavilionService.getAllPavilions();
        Map<String, Object> traverseInfo = new HashMap<>();

        if (pavilions.isEmpty()) {
            traverseInfo.put("routes", new ArrayList<>());
            traverseInfo.put("totalDistance", 0.0);
            traverseInfo.put(ESTIMATED_TIME, 0);
            return traverseInfo;
        }

        // 简化算法：按顺序连接所有亭子（实际应用中应使用更复杂的算法）
        List<Map<String, Object>> routes = new ArrayList<>();
        double totalDistance = 0.0;

        for (int i = 0; i < pavilions.size() - 1; i++) {
            Pavilion from = pavilions.get(i);
            Pavilion to = pavilions.get(i + 1);

            if (from.getLatitude() != null && from.getLongitude() != null &&
                to.getLatitude() != null && to.getLongitude() != null) {

                double distance = GeoUtils.haversineKm(
                    from.getLongitude(), from.getLatitude(),
                    to.getLongitude(), to.getLatitude()
                );

                Map<String, Object> routeSegment = new HashMap<>();
                routeSegment.put("fromId", from.getId());
                routeSegment.put("fromName", from.getChineseName());
                routeSegment.put("toId", to.getId());
                routeSegment.put("toName", to.getChineseName());
                routeSegment.put("distance", Math.round(distance * 100.0) / 100.0);
                
                routes.add(routeSegment);
                totalDistance += distance;
            }
        }

        traverseInfo.put("routes", routes);
        traverseInfo.put("totalDistance", Math.round(totalDistance * 100.0) / 100.0);
        traverseInfo.put(ESTIMATED_TIME, Math.round(totalDistance * 10)); // 估算总时间
        traverseInfo.put("totalPavilions", pavilions.size());
        traverseInfo.put("routeOrder", "按创建顺序访问");

        return traverseInfo;
    }

    /**
     * 获取导航方案
     */
    @PostMapping("/navigation-plan")
    public Map<String, Object> getNavigationPlan(@RequestBody NavigationRequest request) {
        Map<String, Object> plan = new HashMap<>();
        
        // 默认按类型过滤
        List<Pavilion> pavilions = pavilionService.getAllPavilions();
        if (request.getType() != null && !request.getType().isEmpty()) {
            pavilions.removeIf(p -> !p.getPavilionType().equals(request.getType()));
        }

        plan.put("pavilions", pavilions);
        plan.put("planType", request.getPlanType() != null ? request.getPlanType() : "standard");
        plan.put("duration", pavilions.size() * 30); // 每个亭子预计停留30分钟
        plan.put("recommended", true);

        return plan;
    }

    /**
     * 获取特定亭子的多媒体导航信息
     */
    @GetMapping("/multimedia/{pavilionId}")
    public Map<String, Object> getPavilionMultimedia(@PathVariable Long pavilionId) {
        Pavilion pavilion = pavilionService.getPavilionById(pavilionId)
            .orElseThrow(() -> new NotFoundException("亭子不存在: " + pavilionId));
        Map<String, Object> multimediaInfo = new HashMap<>();

        multimediaInfo.put("id", pavilion.getId());
        multimediaInfo.put("name", pavilion.getChineseName());
        multimediaInfo.put("description", pavilion.getDescription());
        multimediaInfo.put("historicalSignificance", pavilion.getHistoricalSignificance());
        multimediaInfo.put("constructionPeriod", pavilion.getConstructionPeriod());
        multimediaInfo.put("rating", pavilion.getVisitorRating());

        String slug = pavilion.getId() != null ? "pavilion-" + pavilion.getId() : "default";
        List<String> images = new ArrayList<>();
        for (String suffix : Arrays.asList("-1.svg", "-2.svg")) {
            String path = IMAGES_PATH + slug + suffix;
            if (new ClassPathResource("static" + path).exists()) {
                images.add(path);
            }
        }
        images.add(IMAGES_PATH + "pavilion-default.svg");
        multimediaInfo.put("imageGallery", images);
        multimediaInfo.put("audioGuide", "/audio/guides/" + pavilion.getId() + ".txt");
        multimediaInfo.put("videoTour", null);
        multimediaInfo.put("virtualTour", "/vr/" + slug + "/index.html");
        multimediaInfo.put("textGuide", buildTextGuide(pavilion));

        return multimediaInfo;
    }

    /**
     * 获取逐向实时导航
     */
    @GetMapping("/navigation/{fromId}/{toId}")
    public Map<String, Object> getRealTimeNavigation(
            @PathVariable Long fromId,
            @PathVariable Long toId,
            @RequestParam(defaultValue = "WALKING") String mode) {
        logger.info("获取实时导航: fromId={}, toId={}, mode={}", fromId, toId, mode);

        Pavilion from = pavilionService.getPavilionById(fromId)
            .orElseThrow(() -> new NotFoundException("起始亭子不存在: " + fromId));
        Pavilion to = pavilionService.getPavilionById(toId)
            .orElseThrow(() -> new NotFoundException("目标亭子不存在: " + toId));

        if (from.getLongitude() == null || from.getLatitude() == null ||
            to.getLongitude() == null || to.getLatitude() == null) {
            throw new BusinessException("亭子位置信息不完整");
        }

        Map<String, Object> nav = navigationService.getTurnByTurnNavigation(
            from.getLongitude(), from.getLatitude(),
            to.getLongitude(), to.getLatitude(),
            mode);

        nav.put("fromName", from.getChineseName());
        nav.put("toName", to.getChineseName());
        nav.put("fromId", fromId);
        nav.put("toId", toId);

        return nav;
    }

    /**
     * 旅游服务信息
     */
    @GetMapping("/tourism-services")
    public Map<String, Object> getTourismServices() {
        Map<String, Object> services = new HashMap<>();
        
        services.put("ticketInfo", "部分亭子需要门票，详见具体介绍");
        services.put("openingHours", "08:00-18:00");
        services.put("facilities", Arrays.asList("停车场", "休息区", "洗手间", "餐饮"));
        services.put("accessibility", "无障碍通道");
        services.put("guides", "提供语音导游服务");
        services.put("emergency", "紧急联系电话：123456789");
        services.put("souvenirs", "纪念品商店");
        
        // 统计信息
        List<Pavilion> pavilions = pavilionService.getAllPavilions();
        services.put("totalPavilions", pavilions.size());
        services.put("openPavilions", pavilions.stream().filter(Pavilion::getIsOpenToPublic).count());
        
        return services;
    }

    /**
     * 导航请求类
     */
    public static class NavigationRequest {
        private String type;
        private String planType;
        private String startTime;
        private String endTime;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

    private String buildTextGuide(Pavilion pavilion) {
        StringBuilder guide = new StringBuilder();
        guide.append("欢迎来到").append(pavilion.getChineseName()).append("。");
        if (pavilion.getDescription() != null) {
            guide.append(pavilion.getDescription());
        }
        if (pavilion.getHistoricalSignificance() != null) {
            guide.append(" ").append(pavilion.getHistoricalSignificance());
        }
        return guide.toString();
    }

    private void normalizePavilionFields(Pavilion pavilion) {
        pavilion.setPavilionType(PavilionTypeUtils.normalize(pavilion.getPavilionType()));
        if (pavilion.getName() == null || pavilion.getName().isBlank()) {
            pavilion.setName(pavilion.getChineseName() != null ? pavilion.getChineseName() : "Pavilion");
        }
        if (pavilion.getLongitude() != null && pavilion.getLatitude() != null) {
            pavilion.setGeomWkt(String.format("POINT(%s %s)", pavilion.getLongitude(), pavilion.getLatitude()));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPavilion(@RequestBody Pavilion pavilion) {
        if (pavilion == null) {
            throw new BusinessException("请求体不能为空");
        }
        if ((pavilion.getChineseName() == null || pavilion.getChineseName().isBlank())
            && (pavilion.getName() == null || pavilion.getName().isBlank())) {
            throw new BusinessException("亭子名称不能为空");
        }
        logger.info("创建新亭子: {}", pavilion.getChineseName());
        normalizePavilionFields(pavilion);

        Pavilion savedPavilion = pavilionService.createPavilion(pavilion);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "亭子创建成功");
        response.put("data", savedPavilion);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新亭子
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePavilion(@PathVariable Long id, @RequestBody Pavilion pavilion) {
        logger.info("更新亭子 ID: {}", id);

        Pavilion existingPavilion = pavilionService.getPavilionById(id)
            .orElseThrow(() -> new NotFoundException("亭子不存在: " + id));
        normalizePavilionFields(pavilion);

        // 仅更新非空字段
        if (pavilion.getChineseName() != null) existingPavilion.setChineseName(pavilion.getChineseName());
        if (pavilion.getPavilionType() != null) existingPavilion.setPavilionType(pavilion.getPavilionType());
        if (pavilion.getBuiltYear() != null) existingPavilion.setBuiltYear(pavilion.getBuiltYear());
        if (pavilion.getLongitude() != null) existingPavilion.setLongitude(pavilion.getLongitude());
        if (pavilion.getLatitude() != null) existingPavilion.setLatitude(pavilion.getLatitude());
        if (pavilion.getDescription() != null) existingPavilion.setDescription(pavilion.getDescription());
        if (pavilion.getArchitecturalStyle() != null) existingPavilion.setArchitecturalStyle(pavilion.getArchitecturalStyle());
        if (pavilion.getVisitorRating() != null) existingPavilion.setVisitorRating(pavilion.getVisitorRating());
        if (pavilion.getIsOpenToPublic() != null) existingPavilion.setIsOpenToPublic(pavilion.getIsOpenToPublic());
        if (pavilion.getStructure() != null) existingPavilion.setStructure(pavilion.getStructure());
        if (pavilion.getTopStyle() != null) existingPavilion.setTopStyle(pavilion.getTopStyle());
        if (pavilion.getStreet() != null) existingPavilion.setStreet(pavilion.getStreet());
        if (pavilion.getNotes() != null) existingPavilion.setNotes(pavilion.getNotes());
        if (pavilion.getLocationDesc() != null) existingPavilion.setLocationDesc(pavilion.getLocationDesc());
        if (pavilion.getAreaSize() != null) existingPavilion.setAreaSize(pavilion.getAreaSize());
        if (pavilion.getHistoricalSignificance() != null) existingPavilion.setHistoricalSignificance(pavilion.getHistoricalSignificance());
        if (pavilion.getConstructionPeriod() != null) existingPavilion.setConstructionPeriod(pavilion.getConstructionPeriod());
        if (pavilion.getTicketPrice() != null) existingPavilion.setTicketPrice(pavilion.getTicketPrice());
        if (pavilion.getLastRenovationYear() != null) existingPavilion.setLastRenovationYear(pavilion.getLastRenovationYear());
        if (pavilion.getName() != null) existingPavilion.setName(pavilion.getName());

        Pavilion updatedPavilion = pavilionService.updatePavilion(id, existingPavilion);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "亭子更新成功");
        response.put("data", updatedPavilion);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除亭子
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePavilion(@PathVariable Long id) {
        logger.info("删除亭子 ID: {}", id);

        if (pavilionService.getPavilionById(id).isEmpty()) {
            throw new NotFoundException("亭子不存在: " + id);
        }
        pavilionService.deletePavilion(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "亭子删除成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 智能游览规划
     */
    @GetMapping("/smart-tour")
    public Map<String, Object> getSmartTourPlan(
            @RequestParam(required = false) String startId,
            @RequestParam(required = false) String endId,
            @RequestParam(defaultValue = "240") int duration,
            @RequestParam(required = false) String preference) {
        logger.info("智能游览规划: startId={}, endId={}, duration={}, preference={}",
            startId, endId, duration, preference);
        return thousandPavilionsService.getSmartTourPlan(startId, endId, duration, preference);
    }

    /**
     * 获取附近设施信息
     */
    @GetMapping("/nearby-facilities/{pavilionId}")
    public Map<String, Object> getNearbyFacilities(
            @PathVariable Long pavilionId,
            @RequestParam(defaultValue = "1.0") double radius) {
        logger.info("获取亭子{}附近{}公里范围内的设施", pavilionId, radius);
        return thousandPavilionsService.getNearbyFacilities(pavilionId, radius);
    }

    /**
     * 获取实时天气信息
     */
    @GetMapping("/weather")
    public Map<String, Object> getWeatherInfo() {
        logger.info("获取实时天气信息");
        return thousandPavilionsService.getWeatherInfo();
    }

    /**
     * 生成可分享的路线
     */
    @PostMapping("/share-route")
    public Map<String, Object> generateShareableRoute(@RequestBody ShareRouteRequest request) {
        logger.info("生成可分享路线: {}个亭子", request.getPavilionIds().size());
        return thousandPavilionsService.generateShareableRoute(request.getPavilionIds(), request.getRouteName());
    }

    /**
     * 获取VR体验信息
     */
    @GetMapping("/vr-experience/{pavilionId}")
    public Map<String, Object> getVRExperience(@PathVariable Long pavilionId) {
        logger.info("获取亭子{}的VR体验信息", pavilionId);
        return thousandPavilionsService.getVRExperience(pavilionId);
    }

    /**
     * 获取最优遍历路线（带详细信息）
     */
    @GetMapping("/optimal-route")
    public Map<String, Object> getOptimalRouteWithDetails() {
        logger.info("获取最优遍历路线详情");
        List<Long> routeIds = thousandPavilionsService.getOptimalTraversalRoute();
        List<Map<String, Object>> routeDetails = new ArrayList<>();
        double totalDistance = 0.0;

        for (int i = 0; i < routeIds.size(); i++) {
            Long id = routeIds.get(i);
            Optional<Pavilion> pavilionOpt = pavilionService.getPavilionById(id);
            if (pavilionOpt.isEmpty()) continue;

            Pavilion pavilion = pavilionOpt.get();
            Map<String, Object> detail = new HashMap<>();
            detail.put("order", i + 1);
            detail.put("id", pavilion.getId());
            detail.put("name", pavilion.getChineseName());
            detail.put("type", pavilion.getPavilionType());
            detail.put("latitude", pavilion.getLatitude());
            detail.put("longitude", pavilion.getLongitude());

            if (i > 0) {
                double distance = thousandPavilionsService.calculateDistance(routeIds.get(i - 1), id);
                detail.put("distanceFromPrevious", Math.round(distance * 100.0) / 100.0);
                totalDistance += distance;
            }

            routeDetails.add(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("route", routeDetails);
        result.put("totalPavilions", routeDetails.size());
        result.put("totalDistance", Math.round(totalDistance * 100.0) / 100.0);
        result.put("algorithm", "2-opt TSP Optimization");

        return result;
    }

    /**
     * 多亭子间路线规划
     */
    @PostMapping("/multi-route")
    public Map<String, Object> planMultiPointRoute(@RequestBody MultiPointRouteRequest request) {
        logger.info("多点路线规划: {}个亭子", request.getPavilionIds().size());

        List<Long> ids = request.getPavilionIds();
        List<Map<String, Object>> routeDetails = new ArrayList<>();
        double totalDistance = 0.0;

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            Optional<Pavilion> pavilionOpt = pavilionService.getPavilionById(id);
            if (pavilionOpt.isEmpty()) continue;

            Pavilion pavilion = pavilionOpt.get();
            Map<String, Object> detail = new HashMap<>();
            detail.put("order", i + 1);
            detail.put("id", pavilion.getId());
            detail.put("name", pavilion.getChineseName());
            detail.put("latitude", pavilion.getLatitude());
            detail.put("longitude", pavilion.getLongitude());

            if (i > 0) {
                double distance = thousandPavilionsService.calculateDistance(ids.get(i - 1), id);
                detail.put("distanceFromPrevious", Math.round(distance * 100.0) / 100.0);
                detail.put("travelTime", (int) Math.ceil(distance / 4.0 * 60));
                totalDistance += distance;
            }

            routeDetails.add(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("route", routeDetails);
        result.put("totalPavilions", routeDetails.size());
        result.put("totalDistance", Math.round(totalDistance * 100.0) / 100.0);
        result.put("totalWalkingTime", (int) Math.ceil(totalDistance / 4.0 * 60));
        result.put("startTime", "08:00");
        result.put("endTime", String.format("%02d:00", 8 + (int) Math.ceil(totalDistance / 4.0 + routeDetails.size() * 0.5)));

        return result;
    }

    /**
     * 获取亭子的采集记录
     */
    @GetMapping("/{pavilionId}/collectors")
    public ResponseEntity<Map<String, Object>> getPavilionCollectors(@PathVariable Long pavilionId) {
        List<PavilionCollector> collectors = collectorService.getCollectorsByPavilionId(pavilionId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", collectors);
        response.put("count", collectors.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 添加采集记录
     */
    @PostMapping("/{pavilionId}/collectors")
    public ResponseEntity<Map<String, Object>> createPavilionCollector(
            @PathVariable Long pavilionId,
            @RequestBody PavilionCollector collector) {
        if (collector == null) {
            throw new BusinessException("采集记录不能为空");
        }
        logger.info("添加采集记录: pavilionId={}, collectorName={}", pavilionId, collector.getCollectorName());
        if (pavilionService.getPavilionById(pavilionId).isEmpty()) {
            throw new NotFoundException("亭子不存在: " + pavilionId);
        }
        collector.setId(null);
        collector.setPavilionId(pavilionId);
        PavilionCollector saved = collectorService.createCollector(collector);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "采集记录添加成功");
        resp.put("data", saved);
        return ResponseEntity.ok(resp);
    }

    /**
     * 导入亭子数据 (Excel/GeoJSON/CSV)
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importPavilions(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        PavilionImportResult result = pavilionImportService.importAuto(file);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", String.format("导入完成: 成功%d, 失败%d",
            result.getSuccessCount(), result.getErrorCount()));
        resp.put("data", result);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/export/geojson")
    public ResponseEntity<byte[]> exportGeoJson() {
        byte[] data = pavilionExportService.exportGeoJson();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chuzhou-pavilions.geojson")
            .contentType(MediaType.APPLICATION_JSON)
            .body(data);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = pavilionExportService.exportExcel();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chuzhou-pavilions.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(data);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] data = pavilionExportService.exportCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chuzhou-pavilions.csv")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(data);
    }

    @GetMapping("/export/excel-template")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        byte[] data = pavilionExportService.exportExcelTemplate();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tingcheng-import-template.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(data);
    }

    @GetMapping("/export/csv-template")
    public ResponseEntity<byte[]> downloadCsvTemplate() {
        byte[] data = pavilionExportService.exportCsvTemplate();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tingcheng-import-template.csv")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(data);
    }

    /**
     * 请求类：分享路线
     */
    public static class ShareRouteRequest {
        private List<Long> pavilionIds;
        private String routeName;

        public List<Long> getPavilionIds() { return pavilionIds; }
        public void setPavilionIds(List<Long> pavilionIds) { this.pavilionIds = pavilionIds; }
        public String getRouteName() { return routeName; }
        public void setRouteName(String routeName) { this.routeName = routeName; }
    }

    /**
     * 请求类：多点路线
     */
    public static class MultiPointRouteRequest {
        private List<Long> pavilionIds;

        public List<Long> getPavilionIds() { return pavilionIds; }
        public void setPavilionIds(List<Long> pavilionIds) { this.pavilionIds = pavilionIds; }
    }
}