package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.TravelLog;

import java.util.List;
import java.util.Optional;

public interface TravelLogService {
    TravelLog createLog(TravelLog log);
    TravelLog updateLog(Long id, TravelLog log);
    void deleteLog(Long id);
    Optional<TravelLog> getLogById(Long id);
    List<TravelLog> getAllLogs();
    List<TravelLog> getLogsByRoute(Long routeId);
    List<TravelLog> getLogsByScenic(Long scenicId);
}
