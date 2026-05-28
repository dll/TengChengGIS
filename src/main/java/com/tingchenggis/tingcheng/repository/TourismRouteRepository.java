package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.TourismRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourismRouteRepository extends JpaRepository<TourismRoute, Long> {
    List<TourismRoute> findAllByOrderByCreatedAtDesc();
    List<TourismRoute> findByNameContaining(String name);
    List<TourismRoute> findByRouteType(String routeType);
}
