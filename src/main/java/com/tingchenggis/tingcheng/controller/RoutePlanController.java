package com.tingchenggis.tingcheng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.RoutePlan;
import com.tingchenggis.tingcheng.repository.RoutePlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 路线方案管理 — 保存 / 加载 / 删除 TSP 规划结果
 */
@RestController
@RequestMapping("/route-plans")
@CrossOrigin(origins = "*")
public class RoutePlanController {

    private static final Logger logger = LoggerFactory.getLogger(RoutePlanController.class);
    private final RoutePlanRepository repository;
    private final ObjectMapper objectMapper;

    public RoutePlanController(RoutePlanRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<RoutePlan> plans = repository.findAllByOrderByCreatedAtDesc();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("count", plans.size());
        // 列表视图不返回 planJson，节省带宽
        resp.put("data", plans.stream().map(this::toSummary).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Optional<RoutePlan> opt = repository.findById(id);
        if (opt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "路线方案不存在");
            return ResponseEntity.status(404).body(resp);
        }
        RoutePlan plan = opt.get();
        resp.put("success", true);
        resp.put("data", plan);
        // 反序列化 planJson 为 plan 对象，方便前端动画直接使用
        if (plan.getPlanJson() != null) {
            try {
                resp.put("plan", objectMapper.readValue(plan.getPlanJson(), Map.class));
            } catch (Exception e) {
                logger.warn("planJson 解析失败 id={}: {}", id, e.getMessage());
            }
        }
        return ResponseEntity.ok(resp);
    }

    private final Path gifDir = Paths.get("data", "gifs");

    /** 上传 GIF 文件关联到指定方案 */
    @PostMapping("/{id}/gif")
    public ResponseEntity<Map<String, Object>> uploadGif(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Optional<RoutePlan> opt = repository.findById(id);
            if (opt.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "方案不存在");
                return ResponseEntity.status(404).body(resp);
            }
            Files.createDirectories(gifDir);
            String filename = "plan_" + id + ".gif";
            Path target = gifDir.resolve(filename);
            file.transferTo(target.toFile());

            RoutePlan plan = opt.get();
            plan.setGifPath(target.toString());
            repository.save(plan);

            resp.put("success", true);
            resp.put("gifUrl", "/route-plans/" + id + "/gif");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("GIF上传失败 id={}", id, e);
            resp.put("success", false);
            resp.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    /** 获取方案关联的 GIF 文件 */
    @GetMapping("/{id}/gif")
    public ResponseEntity<?> getGif(@PathVariable Long id) {
        try {
            Optional<RoutePlan> opt = repository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "方案不存在"));
            }
            RoutePlan plan = opt.get();
            if (plan.getGifPath() == null || !Files.exists(Paths.get(plan.getGifPath()))) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "GIF不存在"));
            }
            Resource resource = new FileSystemResource(plan.getGifPath());
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + plan.getPlanName() + ".gif\"")
                .body(resource);
        } catch (Exception e) {
            logger.error("GIF获取失败 id={}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody SaveRequest req) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            RoutePlan plan = new RoutePlan();
            plan.setPlanName(req.getPlanName() != null ? req.getPlanName()
                : "方案-" + System.currentTimeMillis());
            plan.setTransportMode(req.getMode());
            plan.setObjective(req.getObjective());
            plan.setNotes(req.getNotes());

            Map<String, Object> p = req.getPlan();
            if (p != null) {
                plan.setPlanJson(objectMapper.writeValueAsString(p));
                plan.setTotalDistance(asDouble(p.get("totalDistance")));
                plan.setTotalDuration(asDouble(p.get("totalDuration")));
                plan.setTotalFare(asDouble(p.get("totalFare")));
                plan.setTotalTicket(asDouble(p.get("totalTicket")));
                plan.setTotalCost(asDouble(p.get("totalCost")));
                Object ids = p.get("visitOrderIds");
                if (ids instanceof List<?> list) {
                    plan.setVisitOrderIds(list.stream().map(String::valueOf)
                        .collect(Collectors.joining(",")));
                    plan.setPavilionCount(Math.max(0, list.size() - 1));
                }
                Object names = p.get("visitOrder");
                if (names instanceof List<?> list) {
                    plan.setVisitOrderNames(String.join(" → ", list.stream()
                        .map(String::valueOf).collect(Collectors.toList())));
                }
            }

            RoutePlan saved = repository.save(plan);
            resp.put("success", true);
            resp.put("data", toSummary(saved));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("保存路线方案失败", e);
            resp.put("success", false);
            resp.put("message", "保存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        Optional<RoutePlan> opt = repository.findById(id);
        if (opt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "不存在");
            return ResponseEntity.status(404).body(resp);
        }
        repository.delete(opt.get());
        resp.put("success", true);
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toSummary(RoutePlan p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("planName", p.getPlanName());
        m.put("transportMode", p.getTransportMode());
        m.put("objective", p.getObjective());
        m.put("totalDistance", p.getTotalDistance());
        m.put("totalDuration", p.getTotalDuration());
        m.put("totalFare", p.getTotalFare());
        m.put("totalTicket", p.getTotalTicket());
        m.put("totalCost", p.getTotalCost());
        m.put("pavilionCount", p.getPavilionCount());
        m.put("visitOrderNames", p.getVisitOrderNames());
        m.put("notes", p.getNotes());
        m.put("createdAt", p.getCreatedAt());
        m.put("hasGif", p.getGifPath() != null);
        m.put("gifUrl", p.getGifPath() != null ? "/route-plans/" + p.getId() + "/gif" : null);
        return m;
    }

    private Double asDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return null; }
    }

    public static class SaveRequest {
        private String planName;
        private String mode;
        private String objective;
        private String notes;
        private Map<String, Object> plan;

        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getObjective() { return objective; }
        public void setObjective(String objective) { this.objective = objective; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Map<String, Object> getPlan() { return plan; }
        public void setPlan(Map<String, Object> plan) { this.plan = plan; }
    }
}
