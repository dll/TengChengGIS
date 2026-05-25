package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.service.ThousandPavilionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 遍历千亭服务实现
 *
 * 实现滁州亭城GIS系统的特色功能："遍历千亭"
 * 包含最优路径算法、智能推荐、多媒体导航等增强功能
 *
 * @author TingChengGIS
 */
@Service
public class ThousandPavilionsServiceImpl implements ThousandPavilionsService {

    private static final Logger logger = LoggerFactory.getLogger(ThousandPavilionsServiceImpl.class);
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double WALKING_SPEED_KMH = 4.0;
    private static final double STOP_DURATION_MIN = 30.0;

    private final PavilionRepository pavilionRepository;

    public ThousandPavilionsServiceImpl(PavilionRepository pavilionRepository) {
        this.pavilionRepository = pavilionRepository;
    }

    @Override
    public List<Pavilion> getAllPavilionsBasicInfo() {
        logger.info("获取所有亭子的基本信息");
        return pavilionRepository.findAll();
    }

    @Override
    public double calculateDistance(Long pavilionId1, Long pavilionId2) {
        Pavilion pavilion1 = pavilionRepository.findById(pavilionId1).orElse(null);
        Pavilion pavilion2 = pavilionRepository.findById(pavilionId2).orElse(null);

        if (pavilion1 == null || pavilion2 == null ||
            pavilion1.getLatitude() == null || pavilion1.getLongitude() == null ||
            pavilion2.getLatitude() == null || pavilion2.getLongitude() == null) {
            return 0.0;
        }

        return calculateHaversineDistance(
            pavilion1.getLatitude(), pavilion1.getLongitude(),
            pavilion2.getLatitude(), pavilion2.getLongitude()
        );
    }

    @Override
    public List<Long> getOptimalTraversalRoute() {
        List<Pavilion> pavilions = pavilionRepository.findAll();
        if (pavilions.size() <= 1) {
            return pavilions.stream().map(Pavilion::getId).collect(Collectors.toList());
        }

        List<Long> allIds = pavilions.stream().map(Pavilion::getId).collect(Collectors.toList());

        if (allIds.size() <= 10) {
            return solveTSPBruteForce(allIds);
        } else {
            return solveTSP2Opt(allIds);
        }
    }

    /**
     * 使用2-opt优化算法求解TSP问题
     */
    private List<Long> solveTSP2Opt(List<Long> ids) {
        logger.info("使用2-opt算法计算最优遍历路线");

        if (ids.size() <= 3) {
            return new ArrayList<>(ids);
        }

        List<Long> route = new ArrayList<>(ids);

        double[][] distanceMatrix = buildDistanceMatrix(ids);
        boolean improved = true;
        int iterations = 0;
        int maxIterations = 1000;

        while (improved && iterations < maxIterations) {
            improved = false;
            iterations++;

            for (int i = 1; i < route.size() - 1; i++) {
                for (int j = i + 1; j < route.size(); j++) {
                    List<Long> newRoute = twoOptSwap(route, i, j);
                    double newDistance = calculateTotalDistance(newRoute, distanceMatrix);
                    double oldDistance = calculateTotalDistance(route, distanceMatrix);

                    if (newDistance < oldDistance) {
                        route = newRoute;
                        improved = true;
                    }
                }
            }
        }

        logger.info("2-opt算法完成，迭代次数: {}, 最终距离: {} km",
            iterations, String.format("%.2f", calculateTotalDistance(route, distanceMatrix)));

        return route;
    }

    /**
     * 2-opt交换操作
     */
    private List<Long> twoOptSwap(List<Long> route, int i, int j) {
        List<Long> newRoute = new ArrayList<>();

        for (int k = 0; k < i; k++) {
            newRoute.add(route.get(k));
        }

        for (int k = j; k >= i; k--) {
            newRoute.add(route.get(k));
        }

        for (int k = j + 1; k < route.size(); k++) {
            newRoute.add(route.get(k));
        }

        return newRoute;
    }

    /**
     * 构建距离矩阵
     */
    private double[][] buildDistanceMatrix(List<Long> ids) {
        int n = ids.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = calculateDistance(ids.get(i), ids.get(j));
                }
            }
        }

        return matrix;
    }

    /**
     * 计算路线总距离
     */
    private double calculateTotalDistance(List<Long> route, double[][] distanceMatrix) {
        double total = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            int fromIndex = getIndex(route.get(i), route);
            int toIndex = getIndex(route.get(i + 1), route);
            total += distanceMatrix[fromIndex][toIndex];
        }
        return total;
    }

    private int getIndex(Long id, List<Long> route) {
        for (int i = 0; i < route.size(); i++) {
            if (route.get(i).equals(id)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 暴力求解TSP（小规模问题）
     */
    private List<Long> solveTSPBruteForce(List<Long> ids) {
        logger.info("使用暴力算法计算最优遍历路线（{}个亭子）", ids.size());

        List<List<Long>> permutations = generatePermutations(new ArrayList<>(ids));
        List<Long> bestRoute = new ArrayList<>(ids);
        double bestDistance = Double.MAX_VALUE;

        for (List<Long> permutation : permutations) {
            double distance = calculatePermutationDistance(permutation);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestRoute = new ArrayList<>(permutation);
            }
        }

        logger.info("暴力算法完成，最优距离: {} km", String.format("%.2f", bestDistance));
        return bestRoute;
    }

    private double calculatePermutationDistance(List<Long> permutation) {
        double total = 0.0;
        for (int i = 0; i < permutation.size() - 1; i++) {
            total += calculateDistance(permutation.get(i), permutation.get(i + 1));
        }
        return total;
    }

    private List<List<Long>> generatePermutations(List<Long> list) {
        List<List<Long>> result = new ArrayList<>();

        if (list.size() == 0) {
            result.add(new ArrayList<>());
            return result;
        }

        if (list.size() == 1) {
            result.add(list);
            return result;
        }

        for (int i = 0; i < list.size(); i++) {
            Long current = list.get(i);
            List<Long> remaining = new ArrayList<>(list);
            remaining.remove(i);

            List<List<Long>> permutations = generatePermutations(remaining);

            for (List<Long> permutation : permutations) {
                List<Long> newPermutation = new ArrayList<>();
                newPermutation.add(current);
                newPermutation.addAll(permutation);
                result.add(newPermutation);
            }
        }

        return result;
    }

    @Override
    public double[][] getAccessibilityMatrix() {
        List<Pavilion> pavilions = pavilionRepository.findAll();
        int n = pavilions.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = calculateDistance(
                        pavilions.get(i).getId(),
                        pavilions.get(j).getId()
                    );
                }
            }
        }

        return matrix;
    }

    @Override
    public List<Pavilion> getRecommendedTourRoute(String pavilionType, int maxDuration) {
        logger.info("获取推荐游览路线，类型：{}，最大时长：{}分钟", pavilionType, maxDuration);

        List<Pavilion> allPavilions = pavilionRepository.findAll();
        List<Pavilion> filteredPavilions;

        if (pavilionType != null && !pavilionType.isEmpty()) {
            filteredPavilions = allPavilions.stream()
                .filter(p -> pavilionType.equalsIgnoreCase(p.getPavilionType()) ||
                            p.getPavilionType() != null && p.getPavilionType().toLowerCase().contains(pavilionType.toLowerCase()))
                .collect(Collectors.toList());
        } else {
            filteredPavilions = new ArrayList<>(allPavilions);
        }

        if (filteredPavilions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Pavilion> sortedPavilions = filteredPavilions.stream()
            .sorted((p1, p2) -> {
                double score1 = calculatePavilionScore(p1);
                double score2 = calculatePavilionScore(p2);
                return Double.compare(score2, score1);
            })
            .collect(Collectors.toList());

        double totalTime = 0.0;
        List<Pavilion> recommendedRoute = new ArrayList<>();

        for (Pavilion pavilion : sortedPavilions) {
            double visitTime = STOP_DURATION_MIN;
            if (pavilion.getVisitorRating() != null) {
                visitTime = STOP_DURATION_MIN * pavilion.getVisitorRating() / 3.0;
            }

            if (totalTime + visitTime <= maxDuration) {
                recommendedRoute.add(pavilion);
                totalTime += visitTime;

                if (recommendedRoute.size() > 1) {
                    double travelTime = estimateTravelTime(
                        recommendedRoute.get(recommendedRoute.size() - 2).getId(),
                        pavilion.getId()
                    );
                    totalTime += travelTime;
                }
            }
        }

        logger.info("推荐游览路线生成完成，包含{}个亭子，总时长约{}分钟",
            recommendedRoute.size(), (int) totalTime);

        return recommendedRoute;
    }

    private double calculatePavilionScore(Pavilion pavilion) {
        double score = 0.0;

        if (pavilion.getVisitorRating() != null) {
            score += pavilion.getVisitorRating() * 20;
        }

        if ("历史文化亭".equals(pavilion.getPavilionType()) ||
            "HISTORICAL".equals(pavilion.getPavilionType())) {
            score += 15;
        }

        if (pavilion.getBuiltYear() != null) {
            int age = 2024 - pavilion.getBuiltYear();
            if (age > 500) {
                score += 10;
            } else if (age > 100) {
                score += 5;
            }
        }

        if (pavilion.getIsOpenToPublic() != null && pavilion.getIsOpenToPublic()) {
            score += 10;
        }

        return score;
    }

    @Override
    public double estimateTravelTime(Long fromId, Long toId) {
        double distance = calculateDistance(fromId, toId);
        double travelTime = (distance / WALKING_SPEED_KMH) * 60;
        return Math.ceil(travelTime);
    }

    /**
     * 计算两点间的Haversine距离
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 获取智能规划的游览路线（考虑多个因素）
     */
    public Map<String, Object> getSmartTourPlan(String startPavilionId, String endPavilionId,
                                                  int duration, String preference) {
        Map<String, Object> plan = new HashMap<>();

        List<Pavilion> allPavilions = pavilionRepository.findAll();
        List<Pavilion> availablePavilions = allPavilions.stream()
            .filter(p -> p.getIsOpenToPublic() == null || p.getIsOpenToPublic())
            .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
            .collect(Collectors.toList());

        List<Long> optimalRouteIds = getOptimalTraversalRoute();

        List<Map<String, Object>> routeDetails = new ArrayList<>();
        double totalDistance = 0.0;
        double totalTime = 0.0;
        int pavilionCount = 0;

        for (Long id : optimalRouteIds) {
            if (pavilionCount >= availablePavilions.size()) break;

            Optional<Pavilion> pavilionOpt = pavilionRepository.findById(id);
            if (pavilionOpt.isEmpty()) continue;

            Pavilion pavilion = pavilionOpt.get();

            if (startPavilionId != null && !startPavilionId.isEmpty() &&
                !String.valueOf(pavilion.getId()).equals(startPavilionId) &&
                pavilionCount == 0) {
                continue;
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("order", routeDetails.size() + 1);
            detail.put("id", pavilion.getId());
            detail.put("name", pavilion.getChineseName());
            detail.put("type", pavilion.getPavilionType());
            detail.put("latitude", pavilion.getLatitude());
            detail.put("longitude", pavilion.getLongitude());
            detail.put("description", pavilion.getDescription());
            detail.put("historicalSignificance", pavilion.getHistoricalSignificance());
            detail.put("visitDuration", pavilion.getVisitorRating() != null ?
                (int)(30 * pavilion.getVisitorRating() / 3.0) : 30);

            if (routeDetails.size() > 0) {
                Map<String, Object> prevDetail = routeDetails.get(routeDetails.size() - 1);
                double segmentDistance = calculateDistance(
                    pavilion.getId(),
                    (Long) prevDetail.get("id")
                );
                totalDistance += segmentDistance;
                totalTime += segmentDistance / WALKING_SPEED_KMH * 60;

                detail.put("distanceFromPrevious", Math.round(segmentDistance * 100.0) / 100.0);
                detail.put("travelTimeFromPrevious", (int)Math.ceil(segmentDistance / WALKING_SPEED_KMH * 60));
            }

            totalTime += (int)detail.get("visitDuration");
            routeDetails.add(detail);
            pavilionCount++;

            if (totalTime > duration) break;

            if (endPavilionId != null && !endPavilionId.isEmpty() &&
                String.valueOf(pavilion.getId()).equals(endPavilionId)) {
                break;
            }
        }

        plan.put("route", routeDetails);
        plan.put("totalPavilions", routeDetails.size());
        plan.put("totalDistance", Math.round(totalDistance * 100.0) / 100.0);
        plan.put("estimatedDuration", (int) totalTime);
        plan.put("totalWalkingTime", (int)(totalDistance / WALKING_SPEED_KMH * 60));
        plan.put("startTime", "08:00");
        plan.put("endTime", String.format("%02d:00", 8 + (int)Math.ceil(totalTime / 60)));

        return plan;
    }

    /**
     * 获取附近设施信息
     */
    public Map<String, Object> getNearbyFacilities(Long pavilionId, double radiusKm) {
        Map<String, Object> result = new HashMap<>();

        Optional<Pavilion> pavilionOpt = pavilionRepository.findById(pavilionId);
        if (pavilionOpt.isEmpty()) {
            result.put("error", "亭子不存在");
            return result;
        }

        Pavilion pavilion = pavilionOpt.get();
        if (pavilion.getLatitude() == null || pavilion.getLongitude() == null) {
            result.put("error", "亭子位置信息不完整");
            return result;
        }

        List<Map<String, Object>> facilities = new ArrayList<>();

        facilities.add(createFacility("停车场", "P", 32.315, 118.320, "琅琊山景区停车场"));
        facilities.add(createFacility("洗手间", "WC", 32.314, 118.319, "景区公共卫生间"));
        facilities.add(createFacility("餐饮", "🍽️", 32.313, 118.318, "景区餐厅"));
        facilities.add(createFacility("休息区", "🪑", 32.312, 118.317, "游客休息中心"));
        facilities.add(createFacility("医疗站", "🏥", 32.311, 118.316, "景区医务室"));
        facilities.add(createFacility("纪念品店", "🎁", 32.310, 118.315, "亭城文化纪念品店"));

        result.put("pavilion", pavilion.getChineseName());
        result.put("centerLatitude", pavilion.getLatitude());
        result.put("centerLongitude", pavilion.getLongitude());
        result.put("searchRadius", radiusKm);
        result.put("facilities", facilities);

        return result;
    }

    private Map<String, Object> createFacility(String name, String icon, double lat, double lon, String description) {
        Map<String, Object> facility = new HashMap<>();
        facility.put("name", name);
        facility.put("icon", icon);
        facility.put("latitude", lat);
        facility.put("longitude", lon);
        facility.put("description", description);
        return facility;
    }

    /**
     * 获取实时天气信息（模拟数据）
     */
    public Map<String, Object> getWeatherInfo() {
        Map<String, Object> weather = new HashMap<>();

        weather.put("temperature", 22);
        weather.put("condition", "晴朗");
        weather.put("humidity", 65);
        weather.put("windSpeed", "3级");
        weather.put("windDirection", "东南风");
        weather.put("visibility", "10公里");
        weather.put("uvIndex", "中等");
        weather.put("recommendation", "适宜出游，建议携带遮阳帽和饮用水");

        weather.put("forecast", Arrays.asList(
            createForecast("今天", "晴朗", 22, 14),
            createForecast("明天", "多云", 20, 13),
            createForecast("后天", "小雨", 18, 12)
        ));

        return weather;
    }

    private Map<String, Object> createForecast(String day, String condition, int high, int low) {
        Map<String, Object> forecast = new HashMap<>();
        forecast.put("day", day);
        forecast.put("condition", condition);
        forecast.put("highTemp", high);
        forecast.put("lowTemp", low);
        return forecast;
    }

    /**
     * 生成路线分享信息
     */
    public Map<String, Object> generateShareableRoute(List<Long> pavilionIds, String routeName) {
        Map<String, Object> shareInfo = new HashMap<>();

        List<Map<String, Object>> pavilions = new ArrayList<>();
        double totalDistance = 0.0;
        int totalDuration = 0;

        for (int i = 0; i < pavilionIds.size(); i++) {
            Long id = pavilionIds.get(i);
            Optional<Pavilion> pavilionOpt = pavilionRepository.findById(id);
            if (pavilionOpt.isEmpty()) continue;

            Pavilion pavilion = pavilionOpt.get();
            Map<String, Object> pInfo = new HashMap<>();
            pInfo.put("order", i + 1);
            pInfo.put("id", pavilion.getId());
            pInfo.put("name", pavilion.getChineseName());
            pInfo.put("type", pavilion.getPavilionType());
            pInfo.put("description", pavilion.getDescription());
            pInfo.put("highlights", getPavilionHighlights(pavilion));

            if (i > 0) {
                double distance = calculateDistance(pavilionIds.get(i - 1), id);
                totalDistance += distance;
                totalDuration += (int)(distance / WALKING_SPEED_KMH * 60);
            }

            totalDuration += 30;
            pavilions.add(pInfo);
        }

        shareInfo.put("routeName", routeName != null ? routeName : "亭城之旅");
        shareInfo.put("pavilions", pavilions);
        shareInfo.put("totalPavilions", pavilions.size());
        shareInfo.put("totalDistance", Math.round(totalDistance * 100.0) / 100.0);
        shareInfo.put("estimatedDuration", totalDuration);
        shareInfo.put("shareUrl", generateShareUrl(pavilionIds));
        shareInfo.put("qrCode", generateQRCodeData(pavilionIds));

        return shareInfo;
    }

    private List<String> getPavilionHighlights(Pavilion pavilion) {
        List<String> highlights = new ArrayList<>();

        if ("历史文化亭".equals(pavilion.getPavilionType()) ||
            "HISTORICAL".equals(pavilion.getPavilionType())) {
            highlights.add("历史文化景点");
        }
        if (pavilion.getBuiltYear() != null && pavilion.getBuiltYear() < 1900) {
            highlights.add("百年历史古迹");
        }
        if (pavilion.getVisitorRating() != null && pavilion.getVisitorRating() >= 4.5) {
            highlights.add("热门打卡点");
        }
        if (pavilion.getArchitecturalStyle() != null) {
            highlights.add(pavilion.getArchitecturalStyle() + "建筑风格");
        }

        if (highlights.isEmpty()) {
            highlights.add("值得一游");
        }

        return highlights;
    }

    private String generateShareUrl(List<Long> pavilionIds) {
        String ids = pavilionIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        return "/share/route?ids=" + ids;
    }

    private String generateQRCodeData(List<Long> pavilionIds) {
        String ids = pavilionIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        return "TINGCHENG_ROUTE:" + ids;
    }

    /**
     * 获取VR体验信息
     */
    public Map<String, Object> getVRExperience(Long pavilionId) {
        Map<String, Object> vrInfo = new HashMap<>();

        Optional<Pavilion> pavilionOpt = pavilionRepository.findById(pavilionId);
        if (pavilionOpt.isEmpty()) {
            vrInfo.put("error", "亭子不存在");
            return vrInfo;
        }

        Pavilion pavilion = pavilionOpt.get();

        vrInfo.put("pavilionId", pavilion.getId());
        vrInfo.put("pavilionName", pavilion.getChineseName());
        vrInfo.put("hasVR", true);
        vrInfo.put("vrUrl", "/vr/" + pavilion.getName().toLowerCase() + "/index.html");
        vrInfo.put("panoramaUrl", "/vr/panorama/" + pavilion.getId() + ".jpg");
        vrInfo.put("audioGuideUrl", "/audio/guides/" + pavilion.getId() + ".mp3");
        vrInfo.put("vrFeatures", Arrays.asList(
            "360°全景漫游",
            "历史场景还原",
            "语音讲解",
            "互动问答",
            "AR实景导航"
        ));
        vrInfo.put("compatibility", Arrays.asList(
            "PC浏览器",
            "移动端浏览器",
            "VR头显设备"
        ));

        return vrInfo;
    }
}