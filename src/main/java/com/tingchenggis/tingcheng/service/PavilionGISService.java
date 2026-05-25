package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.Pavilion;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * 亭子GIS服务接口
 * 
 * 整合QGIS功能与滁州亭城文化内容
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
public interface PavilionGISService {

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
     * 计算两点间距离
     * 
     * @param pavilionId1 第一个亭子ID
     * @param pavilionId2 第二个亭子ID
     * @return 距离（米）
     */
    double calculateDistance(Long pavilionId1, Long pavilionId2);

    /**
     * 获取最近的亭子
     * 
     * @param centerGeom 中心点几何对象
     * @param limit 返回数量限制
     * @return 最近的亭子列表
     */
    List<Pavilion> findNearestPavilions(Geometry centerGeom, int limit);

    /**
     * 获取亭子缓冲区内的其他亭子
     * 
     * @param pavilionId 中心亭子ID
     * @param bufferRadius 缓冲半径（米）
     * @return 缓冲区内的亭子列表
     */
    List<Pavilion> findPavilionsInBuffer(Long pavilionId, double bufferRadius);

    /**
     * 执行空间关系查询
     * 
     * @param geom 查询几何对象
     * @param relation 空间关系类型（INTERSECTS, WITHIN, CONTAINS等）
     * @return 符合空间关系的亭子列表
     */
    List<Pavilion> findBySpatialRelation(Geometry geom, String relation);

    /**
     * 生成亭子分布热力图数据
     * 
     * @return 热力图数据
     */
    List<Object[]> generateHeatmapData();

    /**
     * 获取亭子密度分析
     * 
     * @param regionGeom 区域几何对象
     * @return 密度值
     */
    double getPavilionDensity(Geometry regionGeom);

    /**
     * 生成最短路径
     * 
     * @param startPavilionId 起始亭子ID
     * @param endPavilionId 终点亭子ID
     * @return 路径几何对象
     */
    Geometry generateShortestPath(Long startPavilionId, Long endPavilionId);
    
    /**
     * 使用A*算法生成多个亭子之间的最优路径
     * 
     * @param pavilionIds 亭子ID列表
     * @return 路径几何对象
     */
    Geometry generateOptimalPath(List<Long> pavilionIds);
}