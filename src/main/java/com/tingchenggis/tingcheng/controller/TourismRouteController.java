package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.TourismRoute;
import com.tingchenggis.tingcheng.service.TourismRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tourism-routes")
@CrossOrigin(origins = "*")
public class TourismRouteController {

    private static final Logger logger = LoggerFactory.getLogger(TourismRouteController.class);
    private final TourismRouteService service;

    public TourismRouteController(TourismRouteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody TourismRoute route) {
        try {
            TourismRoute saved = service.createRoute(route);
            return ResponseEntity.ok(Map.of("success", true, "data", toSummary(saved)));
        } catch (Exception e) {
            logger.error("创建路线失败", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<TourismRoute> all = service.getAllRoutes();
        List<Map<String, Object>> list = all.stream().map(this::toSummary).toList();
        return ResponseEntity.ok(Map.of("success", true, "count", list.size(), "data", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        Optional<TourismRoute> opt = service.getRouteById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("success", false, "message", "路线不存在"));
        return ResponseEntity.ok(Map.of("success", true, "data", opt.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody TourismRoute route) {
        try {
            TourismRoute updated = service.updateRoute(id, route);
            return ResponseEntity.ok(Map.of("success", true, "data", toSummary(updated)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            service.deleteRoute(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/locations")
    public List<Map<String, Object>> getLocations() {
        return service.getAllRoutes().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("description", r.getDescription());
            m.put("routeType", r.getRouteType());
            m.put("difficulty", r.getDifficulty());
            m.put("geomWkt", r.getGeomWkt());
            m.put("distance", r.getDistance());
            m.put("duration", r.getDuration());
            m.put("scenicStops", r.getScenicStops());
            m.put("color", r.getColor());
            return m;
        }).toList();
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String name) {
        List<TourismRoute> list = service.searchByName(name);
        return ResponseEntity.ok(Map.of("success", true, "data", list.stream().map(this::toSummary).toList()));
    }

    private Map<String, Object> toSummary(TourismRoute r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("name", r.getName());
        m.put("description", r.getDescription());
        m.put("routeType", r.getRouteType());
        m.put("difficulty", r.getDifficulty());
        m.put("distance", r.getDistance());
        m.put("duration", r.getDuration());
        m.put("scenicStops", r.getScenicStops());
        m.put("color", r.getColor());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }
}
