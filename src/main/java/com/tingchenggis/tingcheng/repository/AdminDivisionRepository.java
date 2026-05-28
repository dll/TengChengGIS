package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.AdminDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminDivisionRepository extends JpaRepository<AdminDivision, Long> {

    List<AdminDivision> findByAdminLevel(String adminLevel);

    List<AdminDivision> findByParentId(Long parentId);

    List<AdminDivision> findByParentIdOrderByName(Long parentId);

    List<AdminDivision> findByNameContainingIgnoreCase(String name);

    long countByParentId(Long parentId);

    @Query("SELECT a FROM AdminDivision a WHERE a.geomWkt IS NOT NULL")
    List<AdminDivision> findAllWithGeometry();

    @Query("SELECT a FROM AdminDivision a WHERE a.longitude BETWEEN :minLng AND :maxLng AND a.latitude BETWEEN :minLat AND :maxLat")
    List<AdminDivision> findByGeographicRange(@Param("minLng") Double minLng, @Param("maxLng") Double maxLng,
                                              @Param("minLat") Double minLat, @Param("maxLat") Double maxLat);
}
