package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.TourismRoute;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TourismRouteService {
    TourismRoute createRoute(TourismRoute route);
    TourismRoute updateRoute(Long id, TourismRoute route);
    void deleteRoute(Long id);
    Optional<TourismRoute> getRouteById(Long id);
    List<TourismRoute> getAllRoutes();
    List<TourismRoute> searchByName(String name);
    List<TourismRoute> findByRouteType(String type);
    Map<String, Object> getStats();
}
