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

    @Query("SELECT s FROM ScenicArea s WHERE s.longitude BETWEEN :minLng AND :maxLng AND s.latitude BETWEEN :minLat AND :maxLat")
    List<ScenicArea> findByGeographicRange(@Param("minLng") Double minLng, @Param("maxLng") Double maxLng,
                                           @Param("minLat") Double minLat, @Param("maxLat") Double maxLat);

    List<ScenicArea> findByIsOpenToPublicTrue();
}
