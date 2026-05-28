package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NavigationService {

    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);

    private final RoutingClient routingClient;

    private static final Map<String, String> MODE_TO_OSRM = Map.of(
        "WALKING", "foot",
        "BICYCLE", "cycling",
        "E_BIKE", "cycling",
        "DRIVING", "driving",
        "TAXI", "driving",
        "BUS", "driving"
    );

    public NavigationService(RoutingClient routingClient) {
        this.routingClient = routingClient;
    }

    public Map<String, Object> getTurnByTurnNavigation(double fromLng, double fromLat,
                                                         double toLng, double toLat,
                                                         String mode) {
        Map<String, Object> result = new LinkedHashMap<>();
        String osrmProfile = MODE_TO_OSRM.getOrDefault(mode != null ? mode.toUpperCase() : "", "foot");

        OsrmRoute osrm = routingClient.getRouteWithSteps(fromLng, fromLat, toLng, toLat, osrmProfile);

        if (osrm == null || osrm.getCoordinates() == null || osrm.getCoordinates().size() < 2) {
            logger.warn("OSRM navigation failed for mode {}, falling back to direct route", osrmProfile);
            return buildFallbackNavigation(fromLng, fromLat, toLng, toLat, mode);
        }

        result.put("success", true);
        result.put("fromLng", fromLng);
        result.put("fromLat", fromLat);
        result.put("toLng", toLng);
        result.put("toLat", toLat);
        result.put("mode", mode != null ? mode : "WALKING");
        result.put("totalDistanceKm", Math.round(osrm.getDistance() * 100.0) / 100.0);
        result.put("totalDurationSeconds", Math.round(osrm.getDuration()));
        result.put("totalDurationMinutes", (int) Math.ceil(osrm.getDuration() / 60.0));
        result.put("estimatedArrival", System.currentTimeMillis() + (long) (osrm.getDuration() * 1000));
        result.put("geometryWkt", osrm.getGeometryWkt());
        result.put("geometry", osrm.getCoordinates());

        List<NavigationStep> steps = osrm.getSteps();
        if (steps != null && !steps.isEmpty()) {
            List<Map<String, Object>> stepList = new ArrayList<>();
            for (NavigationStep s : steps) {
                Map<String, Object> sm = new LinkedHashMap<>();
                sm.put("stepNumber", s.getStepNumber());
                sm.put("instruction", s.getInstruction());
                sm.put("maneuverType", s.getManeuverType());
                sm.put("maneuverModifier", s.getManeuverModifier());
                sm.put("streetName", s.getStreetName());
                sm.put("distanceKm", Math.round(s.getDistanceKm() * 1000.0) / 1000.0);
                sm.put("durationSeconds", Math.round(s.getDurationSeconds()));
                sm.put("latitude", s.getLatitude());
                sm.put("longitude", s.getLongitude());
                stepList.add(sm);
            }
            result.put("steps", stepList);
            result.put("totalSteps", stepList.size());
        } else {
            result.put("steps", Collections.emptyList());
            result.put("totalSteps", 0);
        }

        return result;
    }

    private Map<String, Object> buildFallbackNavigation(double fromLng, double fromLat,
                                                         double toLng, double toLat,
                                                         String mode) {
        Map<String, Object> result = new LinkedHashMap<>();
        double distance = GeoUtils.haversineKm(fromLng, fromLat, toLng, toLat);

        Map<String, Double> speedMap = Map.of(
            "WALKING", 5.0, "BICYCLE", 12.0, "E_BIKE", 15.0,
            "DRIVING", 40.0, "TAXI", 40.0, "BUS", 30.0
        );
        double speed = speedMap.getOrDefault(mode != null ? mode.toUpperCase() : "", 5.0);
        double durationSec = (distance / speed) * 3600;

        result.put("success", true);
        result.put("fromLng", fromLng);
        result.put("fromLat", fromLat);
        result.put("toLng", toLng);
        result.put("toLat", toLat);
        result.put("mode", mode != null ? mode : "WALKING");
        result.put("totalDistanceKm", GeoUtils.round2(distance));
        result.put("totalDurationSeconds", Math.round(durationSec));
        result.put("totalDurationMinutes", (int) Math.ceil(durationSec / 60.0));
        result.put("estimatedArrival", System.currentTimeMillis() + (long) (durationSec * 1000));
        result.put("isFallback", true);

        String bearing = GeoUtils.computeBearing(fromLat, fromLng, toLat, toLng);
        List<Map<String, Object>> steps = new ArrayList<>();
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("stepNumber", 1);
        step.put("instruction", "向" + bearing + "方向出发，直行约" + String.format("%.0f", distance * 1000) + "米");
        step.put("maneuverType", "depart");
        step.put("distanceKm", Math.round(distance * 1000.0) / 1000.0);
        step.put("durationSeconds", Math.round(durationSec));
        step.put("latitude", toLat);
        step.put("longitude", toLng);
        steps.add(step);

        Map<String, Object> arrive = new LinkedHashMap<>();
        arrive.put("stepNumber", 2);
        arrive.put("instruction", "到达目的地");
        arrive.put("maneuverType", "arrive");
        arrive.put("distanceKm", 0.0);
        arrive.put("durationSeconds", 0);
        arrive.put("latitude", toLat);
        arrive.put("longitude", toLng);
        steps.add(arrive);

        result.put("steps", steps);
        result.put("totalSteps", 2);

        return result;
    }

}
