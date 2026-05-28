package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.TravelLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelLogRepository extends JpaRepository<TravelLog, Long> {
    List<TravelLog> findAllByOrderByCreatedAtDesc();
    List<TravelLog> findByRouteIdOrderByCreatedAtDesc(Long routeId);
    List<TravelLog> findByScenicIdOrderByCreatedAtDesc(Long scenicId);
    List<TravelLog> findByAuthorContainingIgnoreCase(String author);
}
