package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.NavigationService;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/nav")
@CrossOrigin(origins = "*")
public class NavigationController {

    private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);

    private final NavigationService navigationService;
    private final PavilionService pavilionService;

    public NavigationController(NavigationService navigationService, PavilionService pavilionService) {
        this.navigationService = navigationService;
        this.pavilionService = pavilionService;
    }

    @GetMapping("/turn-by-turn/{fromId}/{toId}")
    public ResponseEntity<Map<String, Object>> getTurnByTurn(
            @PathVariable Long fromId,
            @PathVariable Long toId,
            @RequestParam(defaultValue = "WALKING") String mode) {
        logger.info("逐向导航: fromId={}, toId={}, mode={}", fromId, toId, mode);

        Optional<Pavilion> fromOpt = pavilionService.getPavilionById(fromId);
        Optional<Pavilion> toOpt = pavilionService.getPavilionById(toId);

        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "亭子不存在"));
        }

        Pavilion from = fromOpt.get();
        Pavilion to = toOpt.get();

        if (from.getLongitude() == null || from.getLatitude() == null ||
            to.getLongitude() == null || to.getLatitude() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "亭子位置信息不完整"));
        }

        Map<String, Object> result = navigationService.getTurnByTurnNavigation(
            from.getLongitude(), from.getLatitude(),
            to.getLongitude(), to.getLatitude(),
            mode);

        result.put("fromName", from.getChineseName());
        result.put("toName", to.getChineseName());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/turn-by-turn/coords")
    public ResponseEntity<Map<String, Object>> getTurnByTurnByCoords(
            @RequestParam double fromLng,
            @RequestParam double fromLat,
            @RequestParam double toLng,
            @RequestParam double toLat,
            @RequestParam(defaultValue = "WALKING") String mode) {
        logger.info("逐向导航(坐标): from=({},{}), to=({},{}), mode={}", fromLng, fromLat, toLng, toLat, mode);

        Map<String, Object> result = navigationService.getTurnByTurnNavigation(
            fromLng, fromLat, toLng, toLat, mode);

        return ResponseEntity.ok(result);
    }
}
