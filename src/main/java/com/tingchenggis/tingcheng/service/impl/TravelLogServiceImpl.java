package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.TravelLog;
import com.tingchenggis.tingcheng.repository.TravelLogRepository;
import com.tingchenggis.tingcheng.service.TravelLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TravelLogServiceImpl implements TravelLogService {

    private static final Logger logger = LoggerFactory.getLogger(TravelLogServiceImpl.class);
    private final TravelLogRepository repository;

    public TravelLogServiceImpl(TravelLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public TravelLog createLog(TravelLog log) {
        return repository.save(log);
    }

    @Override
    public TravelLog updateLog(Long id, TravelLog log) {
        Optional<TravelLog> opt = repository.findById(id);
        if (opt.isEmpty()) throw new RuntimeException("日志不存在: " + id);
        TravelLog existing = opt.get();
        if (log.getTitle() != null) existing.setTitle(log.getTitle());
        if (log.getContent() != null) existing.setContent(log.getContent());
        if (log.getLocation() != null) existing.setLocation(log.getLocation());
        if (log.getRouteId() != null) existing.setRouteId(log.getRouteId());
        if (log.getScenicId() != null) existing.setScenicId(log.getScenicId());
        if (log.getPhotoUrl() != null) existing.setPhotoUrl(log.getPhotoUrl());
        if (log.getRating() != null) existing.setRating(log.getRating());
        if (log.getAuthor() != null) existing.setAuthor(log.getAuthor());
        return repository.save(existing);
    }

    @Override
    public void deleteLog(Long id) {
        if (repository.findById(id).isEmpty()) throw new RuntimeException("日志不存在: " + id);
        repository.deleteById(id);
    }

    @Override
    public Optional<TravelLog> getLogById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<TravelLog> getAllLogs() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<TravelLog> getLogsByRoute(Long routeId) {
        return repository.findByRouteIdOrderByCreatedAtDesc(routeId);
    }

    @Override
    public List<TravelLog> getLogsByScenic(Long scenicId) {
        return repository.findByScenicIdOrderByCreatedAtDesc(scenicId);
    }
}
