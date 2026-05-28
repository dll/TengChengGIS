package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.TourismRoute;
import com.tingchenggis.tingcheng.exception.NotFoundException;
import com.tingchenggis.tingcheng.repository.TourismRouteRepository;
import com.tingchenggis.tingcheng.service.TourismRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TourismRouteServiceImpl implements TourismRouteService {

    private static final Logger logger = LoggerFactory.getLogger(TourismRouteServiceImpl.class);
    private final TourismRouteRepository repository;

    public TourismRouteServiceImpl(TourismRouteRepository repository) {
        this.repository = repository;
    }

    @Override
    public TourismRoute createRoute(TourismRoute route) {
        return repository.save(route);
    }

    @Override
    public TourismRoute updateRoute(Long id, TourismRoute route) {
        Optional<TourismRoute> opt = repository.findById(id);
        if (opt.isEmpty()) throw new NotFoundException("路线不存在: " + id);
        TourismRoute existing = opt.get();
        if (route.getName() != null) existing.setName(route.getName());
        if (route.getDescription() != null) existing.setDescription(route.getDescription());
        if (route.getRouteType() != null) existing.setRouteType(route.getRouteType());
        if (route.getDifficulty() != null) existing.setDifficulty(route.getDifficulty());
        if (route.getGeomWkt() != null) existing.setGeomWkt(route.getGeomWkt());
        if (route.getDistance() != null) existing.setDistance(route.getDistance());
        if (route.getDuration() != null) existing.setDuration(route.getDuration());
        if (route.getScenicStops() != null) existing.setScenicStops(route.getScenicStops());
        if (route.getColor() != null) existing.setColor(route.getColor());
        return repository.save(existing);
    }

    @Override
    public void deleteRoute(Long id) {
        if (repository.findById(id).isEmpty()) throw new NotFoundException("路线不存在: " + id);
        repository.deleteById(id);
    }

    @Override
    public Optional<TourismRoute> getRouteById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<TourismRoute> getAllRoutes() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<TourismRoute> searchByName(String name) {
        return repository.findByNameContaining(name);
    }

    @Override
    public List<TourismRoute> findByRouteType(String type) {
        return repository.findByRouteType(type);
    }

    @Override
    public Map<String, Object> getStats() {
        List<TourismRoute> all = repository.findAll();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("totalDistance", all.stream().mapToDouble(r -> r.getDistance() != null ? r.getDistance() : 0).sum());
        return stats;
    }
}
