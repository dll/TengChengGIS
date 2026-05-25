package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.RoutePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {
    List<RoutePlan> findAllByOrderByCreatedAtDesc();
}
