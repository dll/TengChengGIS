package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.service.impl.OsmDataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/osm")
@CrossOrigin(origins = "*")
public class OsmImportController {

    private static final Logger logger = LoggerFactory.getLogger(OsmImportController.class);
    private final OsmDataImportService osmImportService;

    public OsmImportController(OsmDataImportService osmImportService) {
        this.osmImportService = osmImportService;
    }

    @PostMapping("/import/all")
    public ResponseEntity<Map<String, Object>> importAll() {
        try {
            Map<String, Object> result = osmImportService.importAll();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", result);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("OSM import all failed", e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    @PostMapping("/import/scenic")
    public ResponseEntity<Map<String, Object>> importScenic() {
        try {
            Map<String, Object> result = osmImportService.importScenicAreas();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", result);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("OSM scenic import failed", e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    @PostMapping("/import/admin")
    public ResponseEntity<Map<String, Object>> importAdmin() {
        try {
            Map<String, Object> result = osmImportService.importAdminDivisions();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", result);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("OSM admin import failed", e);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }
}
