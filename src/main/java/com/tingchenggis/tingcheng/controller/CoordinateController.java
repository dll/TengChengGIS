package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.util.CoordinateTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 坐标偏移修正 — 为所有亭子批量计算 GCJ-02 坐标
 * 解决 WGS-84 数据在高德/腾讯地图上的偏移问题
 */
@RestController
@RequestMapping("/coordinate")
@CrossOrigin(origins = "*")
public class CoordinateController {

    private static final Logger logger = LoggerFactory.getLogger(CoordinateController.class);
    private final PavilionRepository pavilionRepository;

    public CoordinateController(PavilionRepository pavilionRepository) {
        this.pavilionRepository = pavilionRepository;
    }

    @PostMapping("/correct-pavilions")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Map<String, Object>> correctPavilions(
            @RequestParam(defaultValue = "false") boolean force) {
        Map<String, Object> resp = new LinkedHashMap<>();
        List<Pavilion> all = pavilionRepository.findAll();
        List<Pavilion> dirty = new ArrayList<>();
        int skipped = 0;
        for (Pavilion p : all) {
            if (p.getLongitude() == null || p.getLatitude() == null) { skipped++; continue; }
            if (!force && p.getLongitudeGcj() != null && p.getLatitudeGcj() != null) { skipped++; continue; }
            double[] gcj = CoordinateTransform.wgs84ToGcj02(p.getLongitude(), p.getLatitude());
            p.setLongitudeGcj(gcj[0]);
            p.setLatitudeGcj(gcj[1]);
            dirty.add(p);
        }
        pavilionRepository.saveAll(dirty);
        logger.info("亭子坐标纠偏完成: 更新 {} 条, 跳过 {} 条", dirty.size(), skipped);
        resp.put("success", true);
        resp.put("total", all.size());
        resp.put("updated", dirty.size());
        resp.put("skipped", skipped);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/transform")
    public ResponseEntity<Map<String, Object>> transform(
            @RequestParam double lng,
            @RequestParam double lat) {
        Map<String, Object> resp = new LinkedHashMap<>();
        double[] gcj = CoordinateTransform.wgs84ToGcj02(lng, lat);
        resp.put("success", true);
        resp.put("wgs84", new double[]{lng, lat});
        resp.put("gcj02", gcj);
        return ResponseEntity.ok(resp);
    }
}
