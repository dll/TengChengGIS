package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.Pavilion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 亭子数据访问层
 * 
 * 提供对亭子数据的CRUD操作
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@Repository
public interface PavilionRepository extends JpaRepository<Pavilion, Long> {

    /**
     * 根据亭子类型查找亭子
     * 
     * @param pavilionType 亭子类型（HISTORICAL, MODERN, CULTURAL）
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByPavilionType(String pavilionType);

    /**
     * 根据名称模糊查询亭子
     * 
     * @param name 名称关键词
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByNameContainingIgnoreCase(String name);

    /**
     * 根据年代区间查找历史亭子
     * 
     * @param startYear 起始年份
     * @param endYear 结束年份
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByBuiltYearBetween(Integer startYear, Integer endYear);

    /**
     * 根据评分查找热门亭子
     * 
     * @param minRating 最低评分
     * @return 评分高于指定值的亭子列表
     */
    List<Pavilion> findByVisitorRatingGreaterThanEqual(Double minRating);

    /**
     * 根据地理位置范围查找亭子
     * 
     * @param wktText WKT格式的空间范围描述
     * @return 在指定范围内的亭子列表
     */
    @Query(value = "SELECT * FROM pavilions WHERE ST_Intersects(geometry, ST_GeomFromText(:wktText))", nativeQuery = true)
    List<Pavilion> findByGeographicRange(@Param("wktText") String wktText);

    /**
     * 查找开放参观的亭子
     * 
     * @return 开放参观的亭子列表
     */
    List<Pavilion> findByIsOpenToPublicTrue();

    /**
     * 根据建筑风格查找亭子
     * 
     * @param architecturalStyle 建筑风格
     * @return 符合条件的亭子列表
     */
    List<Pavilion> findByArchitecturalStyle(String architecturalStyle);
}