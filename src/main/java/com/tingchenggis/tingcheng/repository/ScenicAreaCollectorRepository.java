package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.ScenicAreaCollector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenicAreaCollectorRepository extends JpaRepository<ScenicAreaCollector, Long> {

    List<ScenicAreaCollector> findByScenicAreaId(Long scenicAreaId);

    List<ScenicAreaCollector> findByScenicAreaIdOrderByCollectionTimeDesc(Long scenicAreaId);

    long countByScenicAreaId(Long scenicAreaId);

    void deleteByScenicAreaId(Long scenicAreaId);

    @Query("SELECT sc.scenicAreaId, COUNT(sc) FROM ScenicAreaCollector sc GROUP BY sc.scenicAreaId")
    List<Object[]> countGroupByScenicAreaId();
}
