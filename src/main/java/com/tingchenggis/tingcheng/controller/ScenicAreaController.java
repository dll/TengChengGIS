package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.ScenicArea;
import com.tingchenggis.tingcheng.entity.ScenicAreaCollector;
import com.tingchenggis.tingcheng.service.ScenicAreaCollectorService;
import com.tingchenggis.tingcheng.service.ScenicAreaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/scenic-areas")
@CrossOrigin(origins = "*")
public class ScenicAreaController {

    private static final Logger logger = LoggerFactory.getLogger(ScenicAreaController.class);
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String COUNT = "count";

    private final ScenicAreaService scenicAreaService;
    private final ScenicAreaCollectorService collectorService;

    public ScenicAreaController(ScenicAreaService scenicAreaService,
                                 ScenicAreaCollectorService collectorService) {
        this.scenicAreaService = scenicAreaService;
        this.collectorService = collectorService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ScenicArea scenicArea) {
        try {
            ScenicArea created = scenicAreaService.createScenicArea(scenicArea);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put(MESSAGE, "景区创建成功");
            resp.put("data", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, "创建失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            Optional<ScenicArea> sa = scenicAreaService.getScenicAreaById(id);
            Map<String, Object> resp = new HashMap<>();
            if (sa.isPresent()) {
                resp.put(SUCCESS, true);
                resp.put("data", sa.get());
                return ResponseEntity.ok(resp);
            }
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, "未找到景区: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
            Page<ScenicArea> result = scenicAreaService.getAllScenicAreas(pageable);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put("data", result.getContent());
            resp.put("totalElements", result.getTotalElements());
            resp.put("totalPages", result.getTotalPages());
            resp.put("currentPage", result.getNumber());
            resp.put("size", result.getSize());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody ScenicArea scenicArea) {
        try {
            ScenicArea updated = scenicAreaService.updateScenicArea(id, scenicArea);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put(MESSAGE, "景区更新成功");
            resp.put("data", updated);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            scenicAreaService.deleteScenicArea(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put(MESSAGE, "景区删除成功");
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/type/{areaType}")
    public ResponseEntity<Map<String, Object>> findByType(@PathVariable String areaType) {
        try {
            List<ScenicArea> list = scenicAreaService.findByAreaType(areaType);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put("data", list);
            resp.put(COUNT, list.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String name) {
        try {
            List<ScenicArea> list = scenicAreaService.findByNameContaining(name);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put("data", list);
            resp.put(COUNT, list.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        try {
            Map<String, Object> s = scenicAreaService.getStats();
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put("data", s);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PostMapping("/geographic-search")
    public ResponseEntity<Map<String, Object>> geographicSearch(@RequestBody Map<String, String> body) {
        try {
            List<ScenicArea> list = scenicAreaService.findByGeographicRange(body.get("wktText"));
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put("data", list);
            resp.put(COUNT, list.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/locations")
    public List<Map<String, Object>> getLocations(
            @RequestParam(required = false, defaultValue = "false") boolean includeCollectorCounts) {
        List<ScenicArea> all = scenicAreaService.getAllScenicAreas();
        Map<Long, Long> countMap = includeCollectorCounts
            ? collectorService.getCollectorCountByScenicAreaIds() : null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (ScenicArea sa : all) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", sa.getId());
            m.put("name", sa.getName());
            m.put("chineseName", sa.getChineseName());
            m.put("areaType", sa.getAreaType());
            m.put("longitude", sa.getLongitude());
            m.put("latitude", sa.getLatitude());
            m.put("geomWkt", sa.getGeomWkt());
            m.put("areaSize", sa.getAreaSize());
            m.put("visitorRating", sa.getVisitorRating());
            if (countMap != null) m.put("collectorCount", countMap.getOrDefault(sa.getId(), 0L));
            list.add(m);
        }
        return list;
    }

    @GetMapping("/{scenicAreaId}/collectors")
    public ResponseEntity<Map<String, Object>> getCollectors(@PathVariable Long scenicAreaId) {
        try {
            List<ScenicAreaCollector> collectors = collectorService.getCollectorsByScenicAreaId(scenicAreaId);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put("data", collectors);
            resp.put(COUNT, collectors.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}
