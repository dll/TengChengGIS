package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.TravelLog;
import com.tingchenggis.tingcheng.service.TravelLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/travel-logs")
@CrossOrigin(origins = "*")
public class TravelLogController {

    private static final Logger logger = LoggerFactory.getLogger(TravelLogController.class);
    private final TravelLogService service;

    public TravelLogController(TravelLogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody TravelLog log) {
        try {
            TravelLog saved = service.createLog(log);
            return ResponseEntity.ok(Map.of("success", true, "data", toMap(saved)));
        } catch (Exception e) {
            logger.error("创建日志失败", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long scenicId) {
        List<TravelLog> logs;
        if (routeId != null) {
            logs = service.getLogsByRoute(routeId);
        } else if (scenicId != null) {
            logs = service.getLogsByScenic(scenicId);
        } else {
            logs = service.getAllLogs();
        }
        return ResponseEntity.ok(Map.of("success", true, "count", logs.size(), "data", logs.stream().map(this::toMap).toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        Optional<TravelLog> opt = service.getLogById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("success", false, "message", "日志不存在"));
        return ResponseEntity.ok(Map.of("success", true, "data", toMap(opt.get())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody TravelLog log) {
        try {
            TravelLog updated = service.updateLog(id, log);
            return ResponseEntity.ok(Map.of("success", true, "data", toMap(updated)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            service.deleteLog(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> toMap(TravelLog l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("title", l.getTitle());
        m.put("content", l.getContent());
        m.put("location", l.getLocation());
        m.put("routeId", l.getRouteId());
        m.put("scenicId", l.getScenicId());
        m.put("photoUrl", l.getPhotoUrl());
        m.put("rating", l.getRating());
        m.put("author", l.getAuthor());
        m.put("createdAt", l.getCreatedAt());
        m.put("updatedAt", l.getUpdatedAt());
        return m;
    }
}
