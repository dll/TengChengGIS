package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.service.VrArService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/vr-ar")
@CrossOrigin(origins = "*")
public class VrArController {

    private static final Logger logger = LoggerFactory.getLogger(VrArController.class);

    private final VrArService vrArService;

    public VrArController(VrArService vrArService) {
        this.vrArService = vrArService;
    }

    @GetMapping("/experience/{pavilionId}")
    public ResponseEntity<Map<String, Object>> getVrExperience(@PathVariable Long pavilionId) {
        logger.info("VR体验: pavilionId={}", pavilionId);
        Map<String, Object> result = vrArService.getVrExperience(pavilionId);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ar-overlay/{pavilionId}")
    public ResponseEntity<Map<String, Object>> getArOverlay(@PathVariable Long pavilionId) {
        logger.info("AR叠加层: pavilionId={}", pavilionId);
        Map<String, Object> result = vrArService.getArOverlayData(pavilionId);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/3d-scene/{pavilionId}")
    public ResponseEntity<Map<String, Object>> get3dScene(@PathVariable Long pavilionId) {
        logger.info("3D场景: pavilionId={}", pavilionId);
        Map<String, Object> result = vrArService.get3dSceneData(pavilionId);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
