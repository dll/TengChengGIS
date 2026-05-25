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

    @Query(value = "SELECT * FROM admin_divisions WHERE ST_Intersects(ST_GeomFromText(geom_wkt), ST_GeomFromText(:wktText))", nativeQuery = true)
    List<AdminDivision> findByGeographicRange(@Param("wktText") String wktText);
}
