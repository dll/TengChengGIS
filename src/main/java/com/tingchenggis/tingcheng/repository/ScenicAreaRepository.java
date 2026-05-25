package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.ScenicArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenicAreaRepository extends JpaRepository<ScenicArea, Long> {

    List<ScenicArea> findByAreaType(String areaType);

    List<ScenicArea> findByNameContainingIgnoreCase(String name);

    @Query(value = "SELECT * FROM scenic_areas WHERE ST_Intersects(ST_GeomFromText(geom_wkt), ST_GeomFromText(:wktText))", nativeQuery = true)
    List<ScenicArea> findByGeographicRange(@Param("wktText") String wktText);

    List<ScenicArea> findByIsOpenToPublicTrue();
}
