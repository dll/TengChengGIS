package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.PavilionCollector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采集记录数据访问接口
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
@Repository
public interface PavilionCollectorRepository extends JpaRepository<PavilionCollector, Long> {

    List<PavilionCollector> findByPavilionId(Long pavilionId);

    List<PavilionCollector> findByPavilionIdOrderByCollectionTimeDesc(Long pavilionId);

    long countByPavilionId(Long pavilionId);

    void deleteByPavilionId(Long pavilionId);

    @Query("SELECT pc.pavilionId, COUNT(pc) FROM PavilionCollector pc GROUP BY pc.pavilionId")
    List<Object[]> countGroupByPavilionId();
}
