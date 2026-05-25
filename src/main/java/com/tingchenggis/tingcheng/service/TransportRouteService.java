package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.TransportRoute;

import java.util.List;
import java.util.Map;

/**
 * 交通线服务接口
 *
 * 支持多级道路网络和TSP路径优化
 *
 * @author TingChengGIS
 * @version 2.0.0
 */
public interface TransportRouteService {

    /** 道路等级常量 */
    String RL_EXPRESSWAY = "EXPRESSWAY";
    String RL_HIGHWAY = "HIGHWAY";
    String RL_PRIMARY = "PRIMARY";
    String RL_SECONDARY = "SECONDARY";
    String RL_TERTIARY = "TERTIARY";
    String RL_RESIDENTIAL = "RESIDENTIAL";
    String RL_PATH = "PATH";

    List<TransportRoute> getAllRoutes();

    TransportRoute getRouteById(Long id);

    List<TransportRoute> getRoutesFromPavilion(Long pavilionId);

    TransportRoute getRouteBetweenPavilions(Long pavilionId1, Long pavilionId2);

    List<TransportRoute> getRoutesByType(String routeType);

    List<TransportRoute> getScenicRoutes();

    List<TransportRoute> getAccessibleRoutes();

    TransportRoute createRoute(TransportRoute route);

    TransportRoute updateRoute(Long id, TransportRoute route);

    void deleteRoute(Long id);

    Map<String, Object> getRouteStats();

    List<TransportRoute> getRoutesByTransportMode(String transportMode);

    List<String> getAvailableTransportModes();

    /**
     * 为所有亭子构建多级道路网络
     * 基于 OSRM 真实路网数据，分类道路等级
     */
    Map<String, Object> buildRoadNetwork();

    /**
     * TSP 路径优化（Nearest Neighbor + 2-opt）
     * 基于 OSRM 真实路网计算距离矩阵，然后求解最优访问顺序
     * @param objective 优化目标 distance/time/cost
     */
    Map<String, Object> getTspRoute(List<Long> pavilionIds, String mode, String objective);

    /** 兼容老接口：默认按距离优化 */
    default Map<String, Object> getTspRoute(List<Long> pavilionIds, String mode) {
        return getTspRoute(pavilionIds, mode, "distance");
    }

    /** 多模式路网构建：每对亭子构建 driving/cycling/walking 三套路线 */
    Map<String, Object> buildMultiModalNetwork();
}
