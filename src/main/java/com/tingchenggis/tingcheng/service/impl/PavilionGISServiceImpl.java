package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.service.PavilionGISService;
import com.tingchenggis.tingcheng.service.PavilionStats;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 亭子GIS服务实现类 - 简化版
 * 
 * 整合QGIS功能与滁州亭城文化内容
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@Service
@Transactional
public class PavilionGISServiceImpl implements PavilionGISService {

    private final PavilionRepository pavilionRepository;
    private final PavilionService pavilionService;

    public PavilionGISServiceImpl(PavilionRepository pavilionRepository,
                                  PavilionService pavilionService) {
        this.pavilionRepository = pavilionRepository;
        this.pavilionService = pavilionService;
    }

    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    /**
     * 计算两点间距离（使用球面余弦定律）
     * @param lon1 第一个点的经度
     * @param lat1 第一个点的纬度
     * @param lon2 第二个点的经度
     * @param lat2 第二个点的纬度
     * @return 距离（单位：千米）
     */
    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        final double R = 6371; // 地球半径（千米）
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(latRad1) * Math.cos(latRad2) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // 距离（千米）
    }

    @Override
    public Pavilion createPavilion(Pavilion pavilion) {
        return pavilionService.createPavilion(pavilion);
    }

    @Override
    public Optional<Pavilion> getPavilionById(Long id) {
        return pavilionService.getPavilionById(id);
    }

    @Override
    public Page<Pavilion> getAllPavilions(Pageable pageable) {
        return pavilionService.getAllPavilions(pageable);
    }

    @Override
    public Pavilion updatePavilion(Long id, Pavilion pavilion) {
        return pavilionService.updatePavilion(id, pavilion);
    }

    @Override
    public void deletePavilion(Long id) {
        pavilionService.deletePavilion(id);
    }

    @Override
    public List<Pavilion> findByPavilionType(String pavilionType) {
        return pavilionService.findByPavilionType(pavilionType);
    }

    @Override
    public List<Pavilion> findByNameContaining(String name) {
        return pavilionService.findByNameContaining(name);
    }

    @Override
    public List<Pavilion> findByBuiltYearBetween(Integer startYear, Integer endYear) {
        return pavilionService.findByBuiltYearBetween(startYear, endYear);
    }

    @Override
    public List<Pavilion> findByVisitorRatingGreaterThanEqual(Double minRating) {
        return pavilionService.findByVisitorRatingGreaterThanEqual(minRating);
    }

    @Override
    public List<Pavilion> findByGeographicRange(String wktText) {
        return pavilionService.findByGeographicRange(wktText);
    }

    @Override
    public List<Pavilion> findOpenToPublic() {
        return pavilionService.findOpenToPublic();
    }

    @Override
    public List<Pavilion> findByArchitecturalStyle(String architecturalStyle) {
        return pavilionService.findByArchitecturalStyle(architecturalStyle);
    }

    @Override
    public PavilionStats getStats() {
        return pavilionService.getStats();
    }

    @Override
    public List<Pavilion> recommendPavilions(String userId, String preferences) {
        return pavilionService.recommendPavilions(userId, preferences);
    }

    @Override
    public double calculateDistance(Long pavilionId1, Long pavilionId2) {
        Optional<Pavilion> pavilion1Opt = getPavilionById(pavilionId1);
        Optional<Pavilion> pavilion2Opt = getPavilionById(pavilionId2);

        if (pavilion1Opt.isPresent() && pavilion2Opt.isPresent()) {
            Pavilion pav1 = pavilion1Opt.get();
            Pavilion pav2 = pavilion2Opt.get();
            
            if (pav1.getLatitude() != null && pav1.getLongitude() != null &&
                pav2.getLatitude() != null && pav2.getLongitude() != null) {
                // 使用球面余弦定律计算两点间距离
                return calculateDistance(
                    pav1.getLongitude(), pav1.getLatitude(),
                    pav2.getLongitude(), pav2.getLatitude()
                ) * 1000; // 转换为米
            }
        }

        return -1; // 表示无法计算距离
    }

    @Override
    public List<Pavilion> findNearestPavilions(Geometry centerGeom, int limit) {
        // 如果传入的是Point类型，则提取坐标用于计算
        if (centerGeom != null && centerGeom instanceof Point) {
            Point centerPoint = (Point) centerGeom;
            final double centerX = centerPoint.getX();
            final double centerY = centerPoint.getY();
            
            List<Pavilion> allPavilions = pavilionRepository.findAll();
            
            return allPavilions.stream()
                    .filter(p -> p.getLatitude() != null && p.getLongitude() != null) // 过滤掉坐标信息为空的亭子
                    .sorted((p1, p2) -> {
                        double dist1 = calculateDistance(centerX, centerY, p1.getLongitude(), p1.getLatitude());
                        double dist2 = calculateDistance(centerX, centerY, p2.getLongitude(), p2.getLatitude());
                        return Double.compare(dist1, dist2);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
        } else {
            // 如果没有有效的几何体，则返回空列表
            return List.of();
        }
    }

    @Override
    public List<Pavilion> findPavilionsInBuffer(Long pavilionId, double bufferRadius) {
        Optional<Pavilion> centerPavilionOpt = getPavilionById(pavilionId);
        
        if (centerPavilionOpt.isPresent()) {
            Pavilion centerPavilion = centerPavilionOpt.get();
            
            if (centerPavilion.getLatitude() != null && centerPavilion.getLongitude() != null) {
                // 直接使用距离计算来模拟缓冲区
                double centerX = centerPavilion.getLongitude();
                double centerY = centerPavilion.getLatitude();
                
                // 查找在指定半径内的亭子
                List<Pavilion> allPavilions = pavilionRepository.findAll();
                
                return allPavilions.stream()
                        .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                        .filter(p -> {
                            // 计算两点间距离
                            double distance = calculateDistance(
                                centerX, centerY,
                                p.getLongitude(), p.getLatitude()
                            ) * 1000; // 转换为米
                            return distance <= bufferRadius;
                        })
                        .filter(p -> !p.getId().equals(pavilionId)) // 排除中心亭子本身
                        .collect(Collectors.toList());
            }
        }
        
        return List.of(); // 返回空列表
    }

    @Override
    public List<Pavilion> findBySpatialRelation(Geometry geom, String relation) {
        // 对于H2数据库，使用简化逻辑
        // 如果geom是Point类型，可以根据距离判断关系
        if (geom instanceof Point) {
            Point centerPoint = (Point) geom;
            double centerX = centerPoint.getX();
            double centerY = centerPoint.getY();
            
            List<Pavilion> allPavilions = pavilionRepository.findAll();
            
            return allPavilions.stream()
                    .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                    .filter(p -> {
                        try {
                            // 对于简化版本，只考虑INTERSECTS关系（距离小于阈值）
                            switch (relation.toUpperCase()) {
                                case "INTERSECTS":
                                    // 假设如果距离小于1km，则认为相交
                                    double distance = calculateDistance(
                                        centerX, centerY,
                                        p.getLongitude(), p.getLatitude()
                                    );
                                    return distance < 1.0; // 1公里内认为相交
                                default:
                                    // 其他关系暂时都按相交处理
                                    double dist = calculateDistance(
                                        centerX, centerY,
                                        p.getLongitude(), p.getLatitude()
                                    );
                                    return dist < 1.0;
                            }
                        } catch (Exception e) {
                            // 如果计算出错，跳过这个亭子
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            // 如果不是Point类型，返回空列表
            return List.of();
        }
    }

    @Override
    public List<Object[]> generateHeatmapData() {
        List<Pavilion> pavilions = pavilionRepository.findAll();
        
        return pavilions.stream()
                .filter(p -> p.getLongitude() != null && p.getLatitude() != null)
                .map(p -> {
                    Object[] data = new Object[3];
                    data[0] = p.getLongitude(); // 经度
                    data[1] = p.getLatitude(); // 纬度
                    data[2] = p.getVisitorRating() != null ? p.getVisitorRating() : 1.0; // 评分作为权重
                    return data;
                })
                .collect(Collectors.toList());
    }

    @Override
    public double getPavilionDensity(Geometry regionGeom) {
        if (regionGeom == null) {
            return 0;
        }
        
        // 对于H2数据库，使用简化密度计算
        // 如果regionGeom是Polygon或Envelope类型，可以根据边界框判断
        if (regionGeom.getGeometryType().equals("Polygon") || regionGeom.getGeometryType().equals("Point")) {
            // 提取区域边界
            double minX = regionGeom.getEnvelopeInternal().getMinX();
            double minY = regionGeom.getEnvelopeInternal().getMinY();
            double maxX = regionGeom.getEnvelopeInternal().getMaxX();
            double maxY = regionGeom.getEnvelopeInternal().getMaxY();
            
            List<Pavilion> allPavilions = pavilionRepository.findAll();
            
            long pavilionsInRegion = allPavilions.stream()
                    .filter(p -> p.getLongitude() != null && p.getLatitude() != null)
                    .filter(p -> {
                        // 检查点是否在边界框内
                        return p.getLongitude() >= minX && p.getLongitude() <= maxX &&
                               p.getLatitude() >= minY && p.getLatitude() <= maxY;
                    })
                    .count();
            
            // 简化的密度计算：亭子数量/区域面积
            double area = (maxX - minX) * (maxY - minY) * 111000 * 111000; // 粗略转换面积单位
            if (area > 0) {
                return pavilionsInRegion / area;
            }
        }
        
        return 0; // 如果区域无效，返回0
    }

    @Override
    public Geometry generateShortestPath(Long startPavilionId, Long endPavilionId) {
        // 这里是一个简化的实现，实际应用中需要使用网络分析算法
        Optional<Pavilion> startPavilionOpt = getPavilionById(startPavilionId);
        Optional<Pavilion> endPavilionOpt = getPavilionById(endPavilionId);
        
        if (startPavilionOpt.isPresent() && endPavilionOpt.isPresent()) {
            Pavilion startPavilion = startPavilionOpt.get();
            Pavilion endPavilion = endPavilionOpt.get();
            
            if (startPavilion.getLongitude() != null && startPavilion.getLatitude() != null &&
                endPavilion.getLongitude() != null && endPavilion.getLatitude() != null) {
                // 创建一个直线作为最短路径的近似
                Coordinate[] coords = {
                    new Coordinate(startPavilion.getLongitude(), startPavilion.getLatitude()),
                    new Coordinate(endPavilion.getLongitude(), endPavilion.getLatitude())
                };
                return geometryFactory.createLineString(coords);
            }
        }
        
        return null; // 如果无法创建路径，返回null
    }
    
    @Override
    public Geometry generateOptimalPath(List<Long> pavilionIds) {
        if (pavilionIds == null || pavilionIds.size() < 2) {
            return null;
        }
        
        // 获取所有亭子
        List<Pavilion> pavilions = pavilionRepository.findAllById(pavilionIds);
        
        if (pavilions.size() < 2) {
            return null;
        }
        
        // 使用简化的A*算法：基于最近邻接点构建路径
        // 构建邻接图
        Map<Long, List<Long>> adjacencyMap = new HashMap<>();
        
        for (Pavilion p1 : pavilions) {
            List<Long> neighbors = new ArrayList<>();
            for (Pavilion p2 : pavilions) {
                if (!p1.getId().equals(p2.getId())) {
                    double distance = calculateDistance(
                        p1.getLongitude(), p1.getLatitude(),
                        p2.getLongitude(), p2.getLatitude()
                    );
                    // 只考虑距离小于5km的亭子作为邻居
                    if (distance < 5.0) {
                        neighbors.add(p2.getId());
                    }
                }
            }
            adjacencyMap.put(p1.getId(), neighbors);
        }
        
        // 使用贪心算法构建路径（简化版A*）
        List<Long> path = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Long currentId = pavilionIds.get(0);
        path.add(currentId);
        visited.add(currentId);
        
        // 遍历所有目标亭子
        for (int i = 1; i < pavilionIds.size(); i++) {
            Long targetId = pavilionIds.get(i);
            
            // 使用Dijkstra算法找到从当前点到目标点的最短路径
            List<Long> segmentPath = findShortestPathDijkstra(
                adjacencyMap, currentId, targetId, visited
            );
            
            // 将路径段添加到总路径中
            if (segmentPath != null && !segmentPath.isEmpty()) {
                // 添加中间点（不包括起点，因为已经在路径中）
                for (int j = 1; j < segmentPath.size() - 1; j++) {
                    Long intermediateId = segmentPath.get(j);
                    if (!visited.contains(intermediateId)) {
                        path.add(intermediateId);
                        visited.add(intermediateId);
                    }
                }
                currentId = targetId;
            } else {
                // 如果找不到路径，直接连接
                path.add(targetId);
                currentId = targetId;
            }
        }
        
        // 构建几何路径
        if (path.size() >= 2) {
            List<Coordinate> coordinates = new ArrayList<>();
            for (Long id : path) {
                Optional<Pavilion> pavilionOpt = pavilions.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst();
                
                if (pavilionOpt.isPresent()) {
                    Pavilion pavilion = pavilionOpt.get();
                    if (pavilion.getLongitude() != null && pavilion.getLatitude() != null) {
                        coordinates.add(new Coordinate(
                            pavilion.getLongitude(),
                            pavilion.getLatitude()
                        ));
                    }
                }
            }
            
            if (coordinates.size() >= 2) {
                return geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));
            }
        }
        
        return null;
    }
    
    /**
     * Dijkstra算法实现
     */
    private List<Long> findShortestPathDijkstra(
            Map<Long, List<Long>> adjacencyMap,
            Long startId,
            Long endId,
            Set<Long> visited
    ) {
        Map<Long, Double> distances = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        PriorityQueue<Long> queue = new PriorityQueue<>(
            Comparator.comparingDouble(distances::get)
        );
        
        // 初始化距离
        for (Long nodeId : adjacencyMap.keySet()) {
            distances.put(nodeId, Double.MAX_VALUE);
            previous.put(nodeId, null);
        }
        distances.put(startId, 0.0);
        queue.add(startId);
        
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            
            if (current.equals(endId)) {
                break;
            }
            
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            List<Long> neighbors = adjacencyMap.get(current);
            if (neighbors != null) {
                for (Long neighbor : neighbors) {
                    double newDist = distances.get(current) + 1.0; // 假设每条边权重为1
                    
                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        previous.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }
        
        // 重建路径
        List<Long> path = new ArrayList<>();
        Long current = endId;
        
        if (previous.get(endId) == null && !endId.equals(startId)) {
            return null; // 没有路径
        }
        
        while (current != null && !current.equals(startId)) {
            path.add(0, current);
            current = previous.get(current);
        }
        
        path.add(0, startId);
        Collections.reverse(path);
        return path;
    }
}