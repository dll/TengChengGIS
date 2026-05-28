package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.exception.BusinessException;
import com.tingchenggis.tingcheng.exception.NotFoundException;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pavilions")
@CrossOrigin(origins = "*")
public class PavilionController {

    private static final Logger logger = LoggerFactory.getLogger(PavilionController.class);

    private final PavilionService pavilionService;

    public PavilionController(PavilionService pavilionService) {
        this.pavilionService = pavilionService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPavilion(@Valid @RequestBody Pavilion pavilion) {
        logger.info("Creating pavilion: {}", pavilion.getName());
        Pavilion created = pavilionService.createPavilion(pavilion);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok("data", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPavilionById(@PathVariable Long id) {
        logger.debug("Fetching pavilion with ID: {}", id);
        Optional<Pavilion> pavilion = pavilionService.getPavilionById(id);
        if (pavilion.isEmpty()) throw new NotFoundException("未找到ID为 " + id + " 的亭子");
        return ResponseEntity.ok(ok("data", pavilion.get()));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPavilions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        logger.debug("Fetching all pavilions with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Pavilion> pavilionsPage = pavilionService.getAllPavilions(pageable);
        Map<String, Object> resp = ok("data", pavilionsPage.getContent());
        resp.put("totalElements", pavilionsPage.getTotalElements());
        resp.put("totalPages", pavilionsPage.getTotalPages());
        resp.put("currentPage", pavilionsPage.getNumber());
        resp.put("size", pavilionsPage.getSize());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePavilion(
            @PathVariable Long id, @Valid @RequestBody Pavilion pavilion) {
        logger.info("Updating pavilion with ID: {}", id);
        Pavilion updated = pavilionService.updatePavilion(id, pavilion);
        return ResponseEntity.ok(ok("data", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePavilion(@PathVariable Long id) {
        logger.info("Deleting pavilion with ID: {}", id);
        pavilionService.deletePavilion(id);
        return ResponseEntity.ok(ok(null));
    }

    @GetMapping("/type/{pavilionType}")
    public ResponseEntity<Map<String, Object>> findByPavilionType(@PathVariable String pavilionType) {
        logger.debug("Finding pavilions by type: {}", pavilionType);
        List<Pavilion> pavilions = pavilionService.findByPavilionType(pavilionType);
        return ResponseEntity.ok(ok("data", pavilions, "count", pavilions.size()));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> findByNameContaining(@RequestParam String name) {
        logger.debug("Finding pavilions containing name: {}", name);
        List<Pavilion> pavilions = pavilionService.findByNameContaining(name);
        return ResponseEntity.ok(ok("data", pavilions, "count", pavilions.size()));
    }

    @GetMapping("/by-year-range")
    public ResponseEntity<Map<String, Object>> findByBuiltYearBetween(
            @RequestParam Integer startYear, @RequestParam Integer endYear) {
        logger.debug("Finding pavilions between years: {} and {}", startYear, endYear);
        List<Pavilion> pavilions = pavilionService.findByBuiltYearBetween(startYear, endYear);
        return ResponseEntity.ok(ok("data", pavilions, "count", pavilions.size()));
    }

    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> findByVisitorRatingGreaterThanEqual(@RequestParam Double minRating) {
        logger.debug("Finding pavilions with rating >= {}", minRating);
        List<Pavilion> pavilions = pavilionService.findByVisitorRatingGreaterThanEqual(minRating);
        return ResponseEntity.ok(ok("data", pavilions, "count", pavilions.size()));
    }

    @GetMapping("/geographic-search")
    public ResponseEntity<Map<String, Object>> findByGeographicRange(
            @RequestParam(defaultValue = "") String wktText) {
        logger.debug("Finding pavilions in geographic range: {}", wktText);
        if (wktText == null || wktText.isBlank())
            throw new BusinessException("wktText 参数不能为空");
        List<Pavilion> pavilions = pavilionService.findByGeographicRange(wktText);
        return ResponseEntity.ok(ok("data", pavilions, "count", pavilions.size()));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPavilionStats() {
        logger.debug("Fetching pavilion statistics");
        return ResponseEntity.ok(ok("data", pavilionService.getStats()));
    }

    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> recommendPavilions(
            @RequestParam String userId, @RequestParam String preferences) {
        logger.info("Generating recommendations for user: {} with preferences: {}", userId, preferences);
        List<Pavilion> recommended = pavilionService.recommendPavilions(userId, preferences);
        return ResponseEntity.ok(ok("data", recommended, "count", recommended.size()));
    }

    // -- 统一响应辅助方法，与 GlobalExceptionHandler 格式一致 --

    private static Map<String, Object> ok(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", true);
        m.put("status", 200);
        if (kv != null) {
            for (int i = 0; i < kv.length; i += 2) {
                m.put(String.valueOf(kv[i]), kv[i + 1]);
            }
        }
        return m;
    }
}
