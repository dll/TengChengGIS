package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.AdminDivision;
import com.tingchenggis.tingcheng.entity.AdminDivisionCollector;
import com.tingchenggis.tingcheng.service.AdminDivisionCollectorService;
import com.tingchenggis.tingcheng.service.AdminDivisionService;
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
@RequestMapping("/admin-divisions")
@CrossOrigin(origins = "*")
public class AdminDivisionController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDivisionController.class);
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String COUNT = "count";

    private final AdminDivisionService adminDivisionService;
    private final AdminDivisionCollectorService collectorService;

    public AdminDivisionController(AdminDivisionService adminDivisionService,
                                    AdminDivisionCollectorService collectorService) {
        this.adminDivisionService = adminDivisionService;
        this.collectorService = collectorService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody AdminDivision adminDivision) {
        try {
            AdminDivision created = adminDivisionService.createAdminDivision(adminDivision);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put(MESSAGE, "行政区划创建成功");
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
            Optional<AdminDivision> ad = adminDivisionService.getAdminDivisionById(id);
            Map<String, Object> resp = new HashMap<>();
            if (ad.isPresent()) {
                resp.put(SUCCESS, true);
                resp.put("data", ad.get());
                return ResponseEntity.ok(resp);
            }
            resp.put(SUCCESS, false);
            resp.put(MESSAGE, "未找到区划: " + id);
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
            Page<AdminDivision> result = adminDivisionService.getAllAdminDivisions(pageable);
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
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody AdminDivision adminDivision) {
        try {
            AdminDivision updated = adminDivisionService.updateAdminDivision(id, adminDivision);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put(MESSAGE, "区划更新成功");
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
            adminDivisionService.deleteAdminDivision(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put(MESSAGE, "区划删除成功");
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

    @GetMapping("/level/{adminLevel}")
    public ResponseEntity<Map<String, Object>> findByLevel(@PathVariable String adminLevel) {
        try {
            List<AdminDivision> list = adminDivisionService.findByAdminLevel(adminLevel);
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

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<Map<String, Object>> findByParent(@PathVariable Long parentId) {
        try {
            List<AdminDivision> list = adminDivisionService.findByParentId(parentId);
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
            List<AdminDivision> list = adminDivisionService.findByNameContaining(name);
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
            Map<String, Object> s = adminDivisionService.getStats();
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

    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> tree() {
        try {
            List<Map<String, Object>> tree = adminDivisionService.getTree();
            Map<String, Object> resp = new HashMap<>();
            resp.put(SUCCESS, true);
            resp.put("data", tree);
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
        List<AdminDivision> all = adminDivisionService.getAllAdminDivisions();
        Map<Long, Long> countMap = includeCollectorCounts
            ? collectorService.getCollectorCountByAdminDivisionIds() : null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (AdminDivision ad : all) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", ad.getId());
            m.put("name", ad.getName());
            m.put("chineseName", ad.getChineseName());
            m.put("adminLevel", ad.getAdminLevel());
            m.put("parentId", ad.getParentId());
            m.put("parentName", ad.getParentName());
            m.put("geomWkt", ad.getGeomWkt());
            m.put("areaSize", ad.getAreaSize());
            m.put("population", ad.getPopulation());
            if (countMap != null) m.put("collectorCount", countMap.getOrDefault(ad.getId(), 0L));
            list.add(m);
        }
        return list;
    }

    @GetMapping("/{adminDivisionId}/collectors")
    public ResponseEntity<Map<String, Object>> getCollectors(@PathVariable Long adminDivisionId) {
        try {
            List<AdminDivisionCollector> collectors = collectorService.getCollectorsByAdminDivisionId(adminDivisionId);
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
