package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.entity.TransportRoute;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.repository.TransportRouteRepository;
import com.tingchenggis.tingcheng.service.Objective;
import com.tingchenggis.tingcheng.service.OsrmRoute;
import com.tingchenggis.tingcheng.service.RoutingClient;
import com.tingchenggis.tingcheng.service.SnapPoint;
import com.tingchenggis.tingcheng.service.TransportRouteService;
import com.tingchenggis.tingcheng.util.GeoUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 交通线服务实现 — 多级道路网络 + TSP优化求解
 *
 * @author TingChengGIS
 * @version 2.0.0
 */
@Service
public class TransportRouteServiceImpl implements TransportRouteService {

    private static final Logger logger = LoggerFactory.getLogger(TransportRouteServiceImpl.class);
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static final String MODE_BUS = "BUS";
    public static final String MODE_TAXI = "TAXI";
    public static final String MODE_E_BIKE = "E_BIKE";
    public static final String MODE_BICYCLE = "BICYCLE";
    public static final String MODE_WALKING = "WALKING";

    public static final List<String> ALL_MODES = Arrays.asList(
        MODE_BUS, MODE_TAXI, MODE_E_BIKE, MODE_BICYCLE, MODE_WALKING
    );

    private static final Map<String, Double> SPEED_MAP = new HashMap<>();
    static {
        SPEED_MAP.put(MODE_BUS, 30.0);
        SPEED_MAP.put(MODE_TAXI, 40.0);
        SPEED_MAP.put(MODE_E_BIKE, 15.0);
        SPEED_MAP.put(MODE_BICYCLE, 12.0);
        SPEED_MAP.put(MODE_WALKING, 5.0);
    }

    /** 道路等级 → 典型速度(km/h) */
    private static final Map<String, Double> ROAD_LEVEL_SPEED = new LinkedHashMap<>();
    static {
        ROAD_LEVEL_SPEED.put(RL_EXPRESSWAY, 100.0);
        ROAD_LEVEL_SPEED.put(RL_HIGHWAY, 70.0);
        ROAD_LEVEL_SPEED.put(RL_PRIMARY, 50.0);
        ROAD_LEVEL_SPEED.put(RL_SECONDARY, 35.0);
        ROAD_LEVEL_SPEED.put(RL_TERTIARY, 25.0);
        ROAD_LEVEL_SPEED.put(RL_RESIDENTIAL, 15.0);
        ROAD_LEVEL_SPEED.put(RL_PATH, 6.0);
    }

    private final TransportRouteRepository transportRouteRepository;
    private final PavilionRepository pavilionRepository;
    private final RoutingClient routingClient;

    public TransportRouteServiceImpl(TransportRouteRepository transportRouteRepository,
                                      PavilionRepository pavilionRepository,
                                      RoutingClient routingClient) {
        this.transportRouteRepository = transportRouteRepository;
        this.pavilionRepository = pavilionRepository;
        this.routingClient = routingClient;
    }

    // ==================== 基础 CRUD ====================

    @Override
    public List<TransportRoute> getAllRoutes() {
        return transportRouteRepository.findAll();
    }

    @Override
    public TransportRoute getRouteById(Long id) {
        return transportRouteRepository.findById(id).orElse(null);
    }

    @Override
    public List<TransportRoute> getRoutesFromPavilion(Long pavilionId) {
        return transportRouteRepository.findByFromPavilionId(pavilionId);
    }

    @Override
    public TransportRoute getRouteBetweenPavilions(Long pavilionId1, Long pavilionId2) {
        List<TransportRoute> routes = transportRouteRepository.findRouteBetweenPavilions(pavilionId1, pavilionId2);
        return routes.isEmpty() ? null : routes.get(0);
    }

    @Override
    public List<TransportRoute> getRoutesByType(String routeType) {
        return transportRouteRepository.findByRouteType(routeType);
    }

    @Override
    public List<TransportRoute> getScenicRoutes() {
        return transportRouteRepository.findByIsScenicRouteTrue();
    }

    @Override
    public List<TransportRoute> getAccessibleRoutes() {
        return transportRouteRepository.findByIsAccessibleTrue();
    }

    @Override
    public List<TransportRoute> getRoutesByTransportMode(String transportMode) {
        return transportRouteRepository.findByTransportMode(transportMode);
    }

    @Override
    public List<String> getAvailableTransportModes() {
        return ALL_MODES;
    }

    @Override
    @Transactional
    public TransportRoute createRoute(TransportRoute route) {
        if (route.getGeomWkt() == null) {
            LineString ls = createLineStringFromPavilions(route.getFromPavilionId(), route.getToPavilionId());
            if (ls != null) route.setGeomWkt(ls.toText());
        }
        if (route.getTransportMode() == null) route.setTransportMode(MODE_WALKING);
        if (route.getRoadLevel() == null) route.setRoadLevel(classifyRoadLevel(route.getDistanceKm(), route.getRoadType()));
        return transportRouteRepository.save(route);
    }

    @Override
    @Transactional
    public TransportRoute updateRoute(Long id, TransportRoute route) {
        Optional<TransportRoute> existing = transportRouteRepository.findById(id);
        if (existing.isEmpty()) throw new RuntimeException("交通线不存在");
        TransportRoute er = existing.get();
        if (route.getRouteName() != null) er.setRouteName(route.getRouteName());
        if (route.getRouteType() != null) er.setRouteType(route.getRouteType());
        if (route.getDistanceKm() != null) er.setDistanceKm(route.getDistanceKm());
        if (route.getTravelTimeMinutes() != null) er.setTravelTimeMinutes(route.getTravelTimeMinutes());
        if (route.getRouteDescription() != null) er.setRouteDescription(route.getRouteDescription());
        if (route.getIsAccessible() != null) er.setIsAccessible(route.getIsAccessible());
        if (route.getIsScenicRoute() != null) er.setIsScenicRoute(route.getIsScenicRoute());
        if (route.getGeomWkt() != null) er.setGeomWkt(route.getGeomWkt());
        if (route.getTransportMode() != null) er.setTransportMode(route.getTransportMode());
        if (route.getRoadLevel() != null) er.setRoadLevel(route.getRoadLevel());
        if (route.getTrafficCondition() != null) er.setTrafficCondition(route.getTrafficCondition());
        if (route.getEstimatedFare() != null) er.setEstimatedFare(route.getEstimatedFare());
        return transportRouteRepository.save(er);
    }

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        transportRouteRepository.deleteById(id);
    }

    // ==================== 路网构建 ====================

    @Override
    @Transactional
    public Map<String, Object> buildRoadNetwork() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Pavilion> pavilions = pavilionRepository.findAll();
        if (pavilions.size() < 2) {
            result.put("success", false);
            result.put("message", "至少需要 2 个亭子才能构建路网");
            return result;
        }

        long oldCount = transportRouteRepository.count();
        transportRouteRepository.deleteAll();
        logger.info("已清除 {} 条旧路线，开始构建多级路网 ({} 个亭子)", oldCount, pavilions.size());

        double maxConnectKm = 50.0;
        int totalCreated = 0;
        int totalPairs = pavilions.size() * (pavilions.size() - 1) / 2;
        int processed = 0;

        for (int i = 0; i < pavilions.size(); i++) {
            Pavilion from = pavilions.get(i);
            for (int j = i + 1; j < pavilions.size(); j++) {
                Pavilion to = pavilions.get(j);
                processed++;
                double directDist = GeoUtils.haversineKm(
                    from.getLongitude(), from.getLatitude(),
                    to.getLongitude(), to.getLatitude());

                if (directDist > maxConnectKm) continue;

                try {
                    TransportRoute route = createRouteFromOsrm(from, to, "driving", MODE_TAXI, directDist);
                    if (route != null) {
                        transportRouteRepository.save(route);
                        totalCreated++;
                    }
                } catch (Exception e) {
                    logger.debug("OSRM route failed for {} -> {}: {}", from.getId(), to.getId(), e.getMessage());
                }

                if (processed % 100 == 0) {
                    logger.info("路网构建进度: {}/{} 对, 已创建 {} 条路线", processed, totalPairs, totalCreated);
                }
            }
        }

        result.put("success", true);
        result.put("pavilionCount", pavilions.size());
        result.put("routesCreated", totalCreated);
        result.put("pairsConsidered", processed);
        result.put("maxConnectKm", maxConnectKm);
        logger.info("路网构建完成: {} 条路线 (来自 {} 个亭子)", totalCreated, pavilions.size());
        return result;
    }

    private String classifyRoadLevel(Double distanceKm, String roadType) {
        if (roadType == null) roadType = "";
        if (roadType.contains("高速") || roadType.contains("快速")) return RL_EXPRESSWAY;
        if (roadType.contains("国道")) return RL_HIGHWAY;
        if (roadType.contains("省道") || roadType.contains("主干")) return RL_PRIMARY;
        if (roadType.contains("县道") || roadType.contains("次干")) return RL_SECONDARY;
        if (roadType.contains("乡道") || roadType.contains("支路")) return RL_TERTIARY;
        if (roadType.contains("步行") || roadType.contains("登山") || roadType.contains("绿道")) return RL_PATH;
        if (distanceKm != null) {
            if (distanceKm >= 20) return RL_HIGHWAY;
            if (distanceKm >= 5) return RL_PRIMARY;
            if (distanceKm >= 1) return RL_TERTIARY;
        }
        return RL_RESIDENTIAL;
    }

    private TransportRoute createRouteFromOsrm(Pavilion from, Pavilion to,
                                                String osrmProfile, String mode, double directDist) {
        OsrmRoute osrm = routingClient.getRoute(
            from.getLongitude(), from.getLatitude(),
            to.getLongitude(), to.getLatitude(), osrmProfile);

        double distance;
        int travelTime;
        String geomWkt;
        List<double[]> coords;

        if (osrm != null && osrm.getCoordinates() != null && osrm.getCoordinates().size() >= 2) {
            coords = osrm.getCoordinates();
            distance = osrm.getDistance();
            travelTime = (int) Math.max(1, osrm.getDuration() / 60.0);
            geomWkt = osrm.getGeometryWkt();
        } else {
            coords = List.of(
                new double[]{from.getLongitude(), from.getLatitude()},
                new double[]{to.getLongitude(), to.getLatitude()}
            );
            distance = directDist;
            double speed = SPEED_MAP.getOrDefault(mode, 30.0);
            travelTime = (int) Math.max(1, (distance / speed) * 60);
            geomWkt = String.format("LINESTRING(%.6f %.6f, %.6f %.6f)",
                from.getLongitude(), from.getLatitude(),
                to.getLongitude(), to.getLatitude());
        }

        TransportRoute route = new TransportRoute();
        route.setRouteName(from.getChineseName() + " → " + to.getChineseName());
        route.setRouteType("道路");
        route.setRoadLevel(classifyRoadLevel(distance, null));
        route.setTransportMode(mode);
        route.setFromPavilionId(from.getId());
        route.setToPavilionId(to.getId());
        route.setDistanceKm(Math.round(distance * 100.0) / 100.0);
        route.setTravelTimeMinutes(travelTime);
        route.setRouteDescription(from.getChineseName() + "至" + to.getChineseName()
            + "，距离" + String.format("%.1f", distance) + "km，约" + travelTime + "分钟");
        route.setIsAccessible(true);
        route.setIsScenicRoute(directDist < 3.0);
        route.setWaypoints(formatWaypoints(coords.toArray(new double[0][])));
        route.setElevationGain(0.0);
        route.setEstimatedFare(MODE_TAXI.equals(mode) ? Math.round((8.0 + distance * 2.0) * 100.0) / 100.0 : 0.0);
        route.setTrafficCondition("SMOOTH");
        route.setGeomWkt(geomWkt);
        return route;
    }

    // ==================== TSP 求解器 ====================

    private static final Map<String, double[]> FARE_TABLE = Map.of(
        MODE_TAXI, new double[]{8.0, 2.0},
        MODE_BUS, new double[]{2.0, 0.5}
    );

    @Override
    public Map<String, Object> getTspRoute(List<Long> pavilionIds, String mode, String objective) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (pavilionIds == null || pavilionIds.size() < 2) {
            result.put("success", false);
            result.put("message", "至少需要 2 个亭子");
            return result;
        }

        String osrmProfile = toOsrmProfile(mode);
        if (mode == null) mode = MODE_WALKING;
        Objective obj = Objective.parse(objective);

        Map<Long, Pavilion> byId = new HashMap<>();
        for (Pavilion p : pavilionRepository.findAllById(pavilionIds)) byId.put(p.getId(), p);
        List<Pavilion> pavilions = new ArrayList<>();
        for (Long id : pavilionIds) {
            Pavilion p = byId.get(id);
            if (p != null) pavilions.add(p);
        }
        if (pavilions.size() < 2) {
            result.put("success", false);
            result.put("message", "无法找到足够的亭子数据");
            return result;
        }

        int n = pavilions.size();
        double speedKmh = SPEED_MAP.getOrDefault(mode, 5.0);
        double[] fare = FARE_TABLE.getOrDefault(mode, new double[]{0.0, 0.0});
        double fareBase = fare[0], farePerKm = fare[1];

        // 距离矩阵（仅用于 TSP 求解器）—— Haversine 估算，避免 N^2 OSRM 调用
        double[][] distMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double d = GeoUtils.haversineKm(
                    pavilions.get(i).getLongitude(), pavilions.get(i).getLatitude(),
                    pavilions.get(j).getLongitude(), pavilions.get(j).getLatitude());
                distMatrix[i][j] = distMatrix[j][i] = obj.weight(d, speedKmh, fareBase, farePerKm, pavilions.get(j));
            }
        }

        int[] tour = nearestNeighborTsp(distMatrix, n);
        tour = twoOpt(tour, distMatrix, n);

        List<Map<String, Object>> segments = new ArrayList<>();
        List<double[]> allCoords = new ArrayList<>();
        double totalDistance = 0, totalDuration = 0, totalFare = 0, totalTicket = 0;

        for (int i = 0; i < n; i++) {
            Pavilion from = pavilions.get(tour[i]);
            Pavilion to = pavilions.get(tour[(i + 1) % n]);

            OsrmRoute osrm = routingClient.getRoute(
                from.getLongitude(), from.getLatitude(),
                to.getLongitude(), to.getLatitude(), osrmProfile);

            Map<String, Object> seg = new LinkedHashMap<>();
            seg.put("fromId", from.getId());
            seg.put("fromName", from.getChineseName());
            seg.put("fromLng", from.getLongitude());
            seg.put("fromLat", from.getLatitude());
            seg.put("toId", to.getId());
            seg.put("toName", to.getChineseName());
            seg.put("toLng", to.getLongitude());
            seg.put("toLat", to.getLatitude());

            double segDist;
            double segDur;
            List<double[]> segCoords;
            String geomWkt;

            if (osrm != null && osrm.getCoordinates() != null && osrm.getCoordinates().size() >= 2) {
                segCoords = osrm.getCoordinates();
                segDist = osrm.getDistance();
                segDur = osrm.getDuration();
                geomWkt = osrm.getGeometryWkt();
            } else {
                segCoords = List.of(
                    new double[]{from.getLongitude(), from.getLatitude()},
                    new double[]{to.getLongitude(), to.getLatitude()});
                segDist = GeoUtils.haversineKm(from.getLongitude(), from.getLatitude(),
                    to.getLongitude(), to.getLatitude());
                segDur = (segDist / speedKmh) * 3600;
                geomWkt = String.format(Locale.US, "LINESTRING(%.6f %.6f, %.6f %.6f)",
                    from.getLongitude(), from.getLatitude(), to.getLongitude(), to.getLatitude());
            }

            seg.put("snapFrom", SnapPoint.compute(from.getLongitude(), from.getLatitude(), segCoords));
            seg.put("snapTo", SnapPoint.compute(to.getLongitude(), to.getLatitude(), segCoords));

            double segFare = fareBase + segDist * farePerKm;
            double segTicket = to.getTicketPrice() != null ? to.getTicketPrice() : 0.0;

            seg.put("distance", round2(segDist));
            seg.put("duration", segDur);
            seg.put("fare", round2(segFare));
            seg.put("ticketPrice", segTicket);
            seg.put("geomWkt", geomWkt);
            seg.put("coordinates", segCoords);

            totalDistance += segDist;
            totalDuration += segDur;
            totalFare += segFare;
            totalTicket += segTicket;
            for (int k = (allCoords.isEmpty() ? 0 : 1); k < segCoords.size(); k++) {
                allCoords.add(segCoords.get(k));
            }
            segments.add(seg);
        }

        List<String> orderNames = new ArrayList<>();
        List<Long> orderIds = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            orderNames.add(pavilions.get(tour[i]).getChineseName());
            orderIds.add(pavilions.get(tour[i]).getId());
        }
        orderNames.add(pavilions.get(tour[0]).getChineseName());
        orderIds.add(pavilions.get(tour[0]).getId());

        result.put("success", true);
        result.put("segments", segments);
        result.put("visitOrder", orderNames);
        result.put("visitOrderIds", orderIds);
        result.put("totalDistance", round2(totalDistance));
        result.put("totalDuration", totalDuration);
        result.put("totalFare", round2(totalFare));
        result.put("totalTicket", round2(totalTicket));
        result.put("totalCost", round2(totalFare + totalTicket));
        result.put("allCoordinates", allCoords);
        result.put("mode", mode);
        result.put("objective", obj.name().toLowerCase());
        result.put("speedKmh", speedKmh);
        result.put("algorithm", "Nearest Neighbor + 2-opt");
        return result;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Override
    @Transactional
    public Map<String, Object> buildMultiModalNetwork() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Pavilion> pavilions = pavilionRepository.findAll();
        if (pavilions.size() < 2) {
            result.put("success", false);
            result.put("message", "至少需要 2 个亭子");
            return result;
        }

        long oldCount = transportRouteRepository.count();
        transportRouteRepository.deleteAll();
        logger.info("已清除 {} 条旧路线，开始构建多模式路网 ({} 个亭子)", oldCount, pavilions.size());

        double maxConnectKm = 30.0;
        String[][] profileMode = {
            {"driving", MODE_TAXI},
            {"driving", MODE_BUS},
            {"cycling", MODE_BICYCLE},
            {"foot", MODE_WALKING}
        };

        // Build the work list first so we can fan out OSRM calls concurrently.
        List<RouteJob> jobs = new ArrayList<>();
        int totalPairs = pavilions.size() * (pavilions.size() - 1) / 2;
        for (int i = 0; i < pavilions.size(); i++) {
            Pavilion from = pavilions.get(i);
            for (int j = i + 1; j < pavilions.size(); j++) {
                Pavilion to = pavilions.get(j);
                double directDist = GeoUtils.haversineKm(
                    from.getLongitude(), from.getLatitude(),
                    to.getLongitude(), to.getLatitude());
                if (directDist > maxConnectKm) continue;
                for (String[] pm : profileMode) {
                    if ("foot".equals(pm[0]) && directDist > 5.0) continue;
                    if ("cycling".equals(pm[0]) && directDist > 15.0) continue;
                    jobs.add(new RouteJob(from, to, pm[0], pm[1], directDist));
                }
            }
        }

        // 8 workers — enough to hide OSRM RTT, low enough to be polite to the public endpoint.
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8);
        java.util.concurrent.atomic.AtomicInteger created = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger done = new java.util.concurrent.atomic.AtomicInteger();
        List<TransportRoute> built = java.util.Collections.synchronizedList(new ArrayList<>(jobs.size()));

        try {
            List<java.util.concurrent.Future<?>> futures = new ArrayList<>(jobs.size());
            for (RouteJob job : jobs) {
                futures.add(pool.submit(() -> {
                    try {
                        TransportRoute route = createRouteFromOsrm(job.from, job.to, job.profile, job.mode, job.directDist);
                        if (route != null) built.add(route);
                    } catch (Exception e) {
                        logger.debug("OSRM {} {} -> {} failed: {}", job.profile, job.from.getId(), job.to.getId(), e.getMessage());
                    } finally {
                        int d = done.incrementAndGet();
                        if (d % 100 == 0) logger.info("路网构建: {}/{} 任务完成", d, jobs.size());
                    }
                }));
            }
            for (java.util.concurrent.Future<?> f : futures) {
                try { f.get(); } catch (Exception ignore) {}
            }
        } finally {
            pool.shutdown();
        }

        transportRouteRepository.saveAll(built);
        created.set(built.size());

        result.put("success", true);
        result.put("pavilionCount", pavilions.size());
        result.put("routesCreated", created.get());
        result.put("pairsConsidered", totalPairs);
        result.put("jobsScheduled", jobs.size());
        result.put("maxConnectKm", maxConnectKm);
        return result;
    }

    private record RouteJob(Pavilion from, Pavilion to, String profile, String mode, double directDist) {}

    private double getBestDistance(Pavilion from, Pavilion to, String osrmProfile) {
        List<TransportRoute> existing = transportRouteRepository
            .findRouteBetweenPavilions(from.getId(), to.getId());
        if (!existing.isEmpty() && existing.get(0).getDistanceKm() != null) {
            return existing.get(0).getDistanceKm();
        }
        return GeoUtils.haversineKm(from.getLongitude(), from.getLatitude(),
            to.getLongitude(), to.getLatitude());
    }

    private int[] nearestNeighborTsp(double[][] dist, int n) {
        int[] tour = new int[n];
        boolean[] visited = new boolean[n];
        tour[0] = 0;
        visited[0] = true;
        for (int i = 1; i < n; i++) {
            int last = tour[i - 1];
            int nearest = -1;
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && dist[last][j] < minDist) {
                    minDist = dist[last][j];
                    nearest = j;
                }
            }
            tour[i] = nearest;
            visited[nearest] = true;
        }
        return tour;
    }

    private int[] twoOpt(int[] tour, double[][] dist, int n) {
        boolean improved = true;
        int maxIter = 100;
        int iter = 0;
        while (improved && iter < maxIter) {
            improved = false;
            iter++;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double oldDist = dist[tour[i]][tour[(i + 1) % n]]
                        + dist[tour[j]][tour[(j + 1) % n]];
                    double newDist = dist[tour[i]][tour[j]]
                        + dist[tour[(i + 1) % n]][tour[(j + 1) % n]];
                    if (newDist < oldDist - 1e-10) {
                        reverse(tour, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }

    private void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int tmp = arr[start];
            arr[start] = arr[end];
            arr[end] = tmp;
            start++;
            end--;
        }
    }

    // ==================== 辅助 ====================

    @Override
    public Map<String, Object> getRouteStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<TransportRoute> allRoutes = transportRouteRepository.findAll();
        double totalDistance = allRoutes.stream()
            .mapToDouble(r -> r.getDistanceKm() != null ? r.getDistanceKm() : 0.0).sum();
        stats.put("totalRoutes", allRoutes.size());
        stats.put("scenicRoutes", transportRouteRepository.findByIsScenicRouteTrue().size());
        stats.put("accessibleRoutes", transportRouteRepository.findByIsAccessibleTrue().size());
        stats.put("totalDistance", Math.round(totalDistance * 100.0) / 100.0);

        Map<String, Integer> modeCounts = new LinkedHashMap<>();
        for (String mode : ALL_MODES)
            modeCounts.put(mode, transportRouteRepository.findByTransportMode(mode).size());
        stats.put("modeCounts", modeCounts);

        Map<String, Integer> levelCounts = new LinkedHashMap<>();
        for (String level : ROAD_LEVEL_SPEED.keySet())
            levelCounts.put(level, 0);
        for (TransportRoute r : allRoutes) {
            if (r.getRoadLevel() != null)
                levelCounts.merge(r.getRoadLevel(), 1, Integer::sum);
        }
        stats.put("roadLevelCounts", levelCounts);
        return stats;
    }

    private String toOsrmProfile(String mode) {
        if (mode == null) return "walking";
        switch (mode.toUpperCase()) {
            case MODE_BUS: case MODE_TAXI: return "driving";
            case MODE_E_BIKE: case MODE_BICYCLE: return "cycling";
            case MODE_WALKING:
            default: return "walking";
        }
    }

    private String formatWaypoints(double[][] waypoints) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < waypoints.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("[").append(waypoints[i][0]).append(",").append(waypoints[i][1]).append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    private LineString createLineStringFromPavilions(Long pavilionId1, Long pavilionId2) {
        Optional<Pavilion> p1 = pavilionRepository.findById(pavilionId1);
        Optional<Pavilion> p2 = pavilionRepository.findById(pavilionId2);
        if (p1.isPresent() && p2.isPresent()) {
            return geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(p1.get().getLongitude(), p1.get().getLatitude()),
                new Coordinate(p2.get().getLongitude(), p2.get().getLatitude())
            });
        }
        return null;
    }
}
