package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.service.OverpassPoiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/poi")
@CrossOrigin(origins = "*")
public class PoiController {

    private static final Logger logger = LoggerFactory.getLogger(PoiController.class);

    private final OverpassPoiService overpassPoiService;

    public PoiController(OverpassPoiService overpassPoiService) {
        this.overpassPoiService = overpassPoiService;
    }

    @GetMapping("/nearby")
    public List<Map<String, Object>> getNearbyPois(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "1.0") double radius) {
        logger.info("查询附近POI: lat={}, lon={}, radius={}km", lat, lon, radius);
        return overpassPoiService.queryNearbyPois(lat, lon, radius);
    }
}
