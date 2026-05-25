package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.Pavilion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 亭子业务逻辑接口
 *
 * 定义亭子相关的业务操作
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
public interface PavilionService {

    /**
     * 创建新的亭子
     *
     * @param pavilion 亭子对象
     * @return 创建后的亭子
     */
    Pavilion createPavilion(Pavilion pavilion);

    /**
     * 根据ID获取亭子
     *
     * @param id 亭子ID
     * @return 亭子对象，如果不存在则返回Optional.empty()
     */
    Optional<Pavilion> getPavilionById(Long id);

    /**
     * 获取所有亭子（分页）
     *
     * @param pageable 分页参数
     * @return 分页的亭子列表
     */
    Page<Pavilion> getAllPavilions(Pageable pageable);

    /**
     * 获取所有亭子（不分页）
     *
     * @return 所有亭子列表
     */
    List<Pavilion> getAllPavilions();

    /**
     * 更新亭子
     *
     * @param id 亭子ID
     * @param pavilion 更新的数据
     * @return 更新后的亭子
     */
    Pavilion updatePavilion(Long id, Pavilion pavilion);

    /**
     * 删除亭子
     *
     * @param id 亭子ID
     */
    void deletePavilion(Long id);

    /**
     * 根据亭子类型查询
     *
     * @param pavilionType 亭子类型
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByPavilionType(String pavilionType);

    /**
     * 根据名称模糊查询
     *
     * @param name 名称关键词
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByNameContaining(String name);

    /**
     * 根据年代区间查询
     *
     * @param startYear 起始年份
     * @param endYear 结束年份
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByBuiltYearBetween(Integer startYear, Integer endYear);

    /**
     * 根据评分查询热门亭子
     *
     * @param minRating 最低评分
     * @return 评分高于指定值的亭子列表
     */
    List<Pavilion> findByVisitorRatingGreaterThanEqual(Double minRating);

    /**
     * 地理位置范围查询
     *
     * @param wktText WKT格式的空间范围描述
     * @return 在指定范围内的亭子列表
     */
    List<Pavilion> findByGeographicRange(String wktText);

    /**
     * 查询开放参观的亭子
     *
     * @return 开放参观的亭子列表
     */
    List<Pavilion> findOpenToPublic();

    /**
     * 根据建筑风格查询
     *
     * @param architecturalStyle 建筑风格
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByArchitecturalStyle(String architecturalStyle);

    /**
     * 获取亭子统计信息
     *
     * @return 亭子统计信息
     */
    PavilionStats getStats();

    /**
     * AI智能推荐亭子
     *
     * @param userId 用户ID
     * @param preferences 用户偏好
     * @return 推荐的亭子列表
     */
    List<Pavilion> recommendPavilions(String userId, String preferences);

    /**
     * 计算两个亭子之间的距离（公里）
     *
     * @param pavilionId1 亭子1 ID
     * @param pavilionId2 亭子2 ID
     * @return 距离（公里）
     */
    double calculateDistance(Long pavilionId1, Long pavilionId2);

    /**
     * 获取最优遍历路线
     *
     * @return 亭子ID列表
     */
    List<Long> getOptimalTraversalRoute();

    /**
     * 获取智能游览规划
     */
    Map<String, Object> getSmartTourPlan(String startId, String endId, int duration, String preference);

    /**
     * 获取附近设施信息
     */
    Map<String, Object> getNearbyFacilities(Long pavilionId, double radiusKm);

    /**
     * 获取天气信息
     */
    Map<String, Object> getWeatherInfo();

    /**
     * 生成可分享路线
     */
    Map<String, Object> generateShareableRoute(List<Long> pavilionIds, String routeName);

    /**
     * 获取VR体验信息
     */
    Map<String, Object> getVRExperience(Long pavilionId);
}