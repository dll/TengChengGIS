package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.AdminDivisionCollector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminDivisionCollectorRepository extends JpaRepository<AdminDivisionCollector, Long> {

    List<AdminDivisionCollector> findByAdminDivisionId(Long adminDivisionId);

    List<AdminDivisionCollector> findByAdminDivisionIdOrderByCollectionTimeDesc(Long adminDivisionId);

    long countByAdminDivisionId(Long adminDivisionId);

    void deleteByAdminDivisionId(Long adminDivisionId);

    @Query("SELECT ac.adminDivisionId, COUNT(ac) FROM AdminDivisionCollector ac GROUP BY ac.adminDivisionId")
    List<Object[]> countGroupByAdminDivisionId();
}
