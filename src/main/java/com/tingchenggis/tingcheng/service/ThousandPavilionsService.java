package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.Pavilion;
import java.util.List;
import java.util.Map;

/**
 * 遍历千亭服务接口
 *
 * 提供智能游览规划、最优路径计算、多媒体导航等核心功能
 *
 * @author TingChengGIS
 */
public interface ThousandPavilionsService {

    /**
     * 获取所有亭子的基本信息
     */
    List<Pavilion> getAllPavilionsBasicInfo();

    /**
     * 计算两点间距离（公里）
     */
    double calculateDistance(Long pavilionId1, Long pavilionId2);

    /**
     * 获取最优遍历路线（使用2-opt优化的TSP求解）
     */
    List<Long> getOptimalTraversalRoute();

    /**
     * 获取从某个亭子到其他亭子的可达性矩阵
     */
    double[][] getAccessibilityMatrix();

    /**
     * 获取推荐游览路线
     */
    List<Pavilion> getRecommendedTourRoute(String pavilionType, int maxDuration);

    /**
     * 获取交通时间估计（分钟）
     */
    double estimateTravelTime(Long fromId, Long toId);

    /**
     * 获取智能规划的游览路线
     */
    Map<String, Object> getSmartTourPlan(String startPavilionId, String endPavilionId,
                                          int duration, String preference);

    /**
     * 获取附近设施信息
     */
    Map<String, Object> getNearbyFacilities(Long pavilionId, double radiusKm);

    /**
     * 获取实时天气信息
     */
    Map<String, Object> getWeatherInfo();

    /**
     * 生成可分享的路线信息
     */
    Map<String, Object> generateShareableRoute(List<Long> pavilionIds, String routeName);

    /**
     * 获取VR体验信息
     */
    Map<String, Object> getVRExperience(Long pavilionId);
}