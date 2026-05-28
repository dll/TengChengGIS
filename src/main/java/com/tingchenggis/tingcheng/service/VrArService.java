package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VrArService {

    private static final Logger logger = LoggerFactory.getLogger(VrArService.class);

    private final PavilionRepository pavilionRepository;
    private final OverpassPoiService overpassPoiService;

    public VrArService(PavilionRepository pavilionRepository,
                        OverpassPoiService overpassPoiService) {
        this.pavilionRepository = pavilionRepository;
        this.overpassPoiService = overpassPoiService;
    }

    public Map<String, Object> getVrExperience(Long pavilionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<Pavilion> opt = pavilionRepository.findById(pavilionId);
        if (opt.isEmpty()) {
            result.put("error", "亭子不存在");
            return result;
        }

        Pavilion p = opt.get();
        String slug = p.getName() != null ? p.getName().toLowerCase().replaceAll("\\s+", "-") : "pavilion-" + p.getId();

        result.put("pavilionId", p.getId());
        result.put("pavilionName", p.getChineseName());
        result.put("latitude", p.getLatitude());
        result.put("longitude", p.getLongitude());
        result.put("hasVR", true);

        List<Map<String, Object>> scenes = new ArrayList<>();

        Map<String, Object> aerial = new LinkedHashMap<>();
        aerial.put("id", "aerial");
        aerial.put("name", "鸟瞰全景");
        aerial.put("type", "3d_globe");
        aerial.put("latitude", p.getLatitude() != null ? p.getLatitude() : 32.3);
        aerial.put("longitude", p.getLongitude() != null ? p.getLongitude() : 118.3);
        aerial.put("altitude", 500);
        aerial.put("pitch", -45);
        aerial.put("heading", 0);
        aerial.put("description", "从空中俯瞰" + p.getChineseName() + "及周边环境");
        scenes.add(aerial);

        Map<String, Object> ground = new LinkedHashMap<>();
        ground.put("id", "ground");
        ground.put("name", "地面视角");
        ground.put("type", "3d_globe");
        ground.put("latitude", p.getLatitude() != null ? p.getLatitude() : 32.3);
        ground.put("longitude", p.getLongitude() != null ? p.getLongitude() : 118.3);
        ground.put("altitude", 5);
        ground.put("pitch", 0);
        ground.put("heading", 45);
        ground.put("description", "在地面近距离欣赏" + p.getChineseName());
        scenes.add(ground);

        result.put("scenes", scenes);

        List<Map<String, Object>> nearbyPois = overpassPoiService.queryNearbyPois(
            p.getLatitude() != null ? p.getLatitude() : 32.3,
            p.getLongitude() != null ? p.getLongitude() : 118.3,
            0.5
        );

        List<Map<String, Object>> arMarkers = new ArrayList<>();
        for (Map<String, Object> poi : nearbyPois) {
            Map<String, Object> marker = new LinkedHashMap<>();
            marker.put("name", poi.get("name"));
            marker.put("category", poi.get("category"));
            marker.put("latitude", poi.get("latitude"));
            marker.put("longitude", poi.get("longitude"));
            marker.put("distance", poi.get("distance"));
            marker.put("bearing", GeoUtils.computeBearing(
                p.getLatitude() != null ? p.getLatitude() : 32.3,
                p.getLongitude() != null ? p.getLongitude() : 118.3,
                (double) poi.get("latitude"),
                (double) poi.get("longitude")
            ));
            arMarkers.add(marker);
        }

        if (arMarkers.isEmpty()) {
            Map<String, Object> marker = new LinkedHashMap<>();
            marker.put("name", "琅琊山景区");
            marker.put("category", "attraction");
            marker.put("latitude", 32.3);
            marker.put("longitude", 118.3);
            marker.put("distance", 0.3);
            marker.put("bearing", "东北");
            arMarkers.add(marker);
        }

        result.put("arMarkers", arMarkers);
        result.put("totalArMarkers", arMarkers.size());

        List<Map<String, Object>> features = new ArrayList<>();
        features.add(Map.of("name", "360°全景漫游", "icon", "360", "available", true));
        features.add(Map.of("name", "3D场景浏览", "icon", "3d", "available", true));
        features.add(Map.of("name", "AR实景导航", "icon", "ar", "available", arMarkers.size() > 0));
        features.add(Map.of("name", "语音讲解", "icon", "audio", "available", true));
        features.add(Map.of("name", "历史场景还原", "icon", "history", "available", false));
        result.put("features", features);

        result.put("compatibility", Arrays.asList("PC浏览器", "移动端浏览器", "VR头显设备"));

        return result;
    }

    public Map<String, Object> getArOverlayData(Long pavilionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<Pavilion> opt = pavilionRepository.findById(pavilionId);
        if (opt.isEmpty()) {
            result.put("error", "亭子不存在");
            return result;
        }

        Pavilion p = opt.get();
        double lat = p.getLatitude() != null ? p.getLatitude() : 32.3;
        double lng = p.getLongitude() != null ? p.getLongitude() : 118.3;

        List<Map<String, Object>> pois = overpassPoiService.queryNearbyPois(lat, lng, 0.5);

        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{\"type\":\"FeatureCollection\",\"features\":[");

        int count = 0;
        for (Map<String, Object> poi : pois) {
            if (count > 0) geoJson.append(",");
            geoJson.append("{\"type\":\"Feature\",");
            geoJson.append("\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
            geoJson.append(poi.get("longitude")).append(",").append(poi.get("latitude"));
            geoJson.append("]},");
            geoJson.append("\"properties\":{");
            geoJson.append("\"name\":\"").append(poi.get("name")).append("\",");
            geoJson.append("\"category\":\"").append(poi.get("category")).append("\",");
            geoJson.append("\"distance\":").append(poi.get("distance"));
            geoJson.append("}}");
            count++;
        }

        if (count == 0) {
            geoJson.append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[118.3,32.3]},");
            geoJson.append("\"properties\":{\"name\":\"琅琊山景区\",\"category\":\"attraction\",\"distance\":0.3}}");
        }

        geoJson.append("]}");

        result.put("pavilionId", p.getId());
        result.put("pavilionName", p.getChineseName());
        result.put("centerLatitude", lat);
        result.put("centerLongitude", lng);
        result.put("geoJson", geoJson.toString());
        result.put("totalMarkers", count > 0 ? count : 1);

        return result;
    }

    public Map<String, Object> get3dSceneData(Long pavilionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<Pavilion> opt = pavilionRepository.findById(pavilionId);
        if (opt.isEmpty()) {
            result.put("error", "亭子不存在");
            return result;
        }

        Pavilion p = opt.get();
        double lat = p.getLatitude() != null ? p.getLatitude() : 32.3;
        double lng = p.getLongitude() != null ? p.getLongitude() : 118.3;

        result.put("pavilionId", p.getId());
        result.put("pavilionName", p.getChineseName());
        result.put("center", Map.of("latitude", lat, "longitude", lng));
        result.put("altitude", 500);
        result.put("pitch", -30);
        result.put("heading", 0);

        Map<String, Object> buildings = new LinkedHashMap<>();
        buildings.put("url", "/api/poi/nearby?lat=" + lat + "&lon=" + lng + "&radius=0.3");
        buildings.put("type", "3d_tiles");
        buildings.put("color", "#4A90D9");
        buildings.put("opacity", 0.8);
        result.put("buildings", buildings);

        Map<String, Object> terrain = new LinkedHashMap<>();
        terrain.put("type", "cesium_world_terrain");
        terrain.put("exaggeration", 1.0);
        result.put("terrain", terrain);

        List<Map<String, Object>> viewpoints = new ArrayList<>();

        Map<String, Object> v1 = new LinkedHashMap<>();
        v1.put("name", "正南视角");
        v1.put("latitude", lat - 0.005);
        v1.put("longitude", lng);
        v1.put("altitude", 50);
        v1.put("pitch", -10);
        v1.put("heading", 0);
        viewpoints.add(v1);

        Map<String, Object> v2 = new LinkedHashMap<>();
        v2.put("name", "正东视角");
        v2.put("latitude", lat);
        v2.put("longitude", lng + 0.005);
        v2.put("altitude", 50);
        v2.put("pitch", -10);
        v2.put("heading", 270);
        viewpoints.add(v2);

        Map<String, Object> v3 = new LinkedHashMap<>();
        v3.put("name", "俯瞰全景");
        v3.put("latitude", lat);
        v3.put("longitude", lng);
        v3.put("altitude", 300);
        v3.put("pitch", -45);
        v3.put("heading", 0);
        viewpoints.add(v3);

        result.put("viewpoints", viewpoints);

        return result;
    }

}
