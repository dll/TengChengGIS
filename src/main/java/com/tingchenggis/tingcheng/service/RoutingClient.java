package com.tingchenggis.tingcheng.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoutingClient {

    private static final Logger logger = LoggerFactory.getLogger(RoutingClient.class);
    private static final String OSRM_URL = "https://router.project-osrm.org/route/v1/%s/%f,%f;%f,%f?geometries=geojson&overview=full";
    private static final String OSRM_URL_WITH_STEPS = "https://router.project-osrm.org/route/v1/%s/%f,%f;%f,%f?geometries=geojson&overview=full&steps=true";
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    public RoutingClient(ObjectMapper mapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(8000);
        this.restTemplate = new RestTemplate(factory);
        this.mapper = mapper;
    }

    public OsrmRoute getRoute(double lng1, double lat1, double lng2, double lat2, String profile) {
        return fetchRoute(lng1, lat1, lng2, lat2, profile, false);
    }

    public OsrmRoute getRouteWithSteps(double lng1, double lat1, double lng2, double lat2, String profile) {
        return fetchRoute(lng1, lat1, lng2, lat2, profile, true);
    }

    private OsrmRoute fetchRoute(double lng1, double lat1, double lng2, double lat2,
                                  String profile, boolean withSteps) {
        String urlTemplate = withSteps ? OSRM_URL_WITH_STEPS : OSRM_URL;
        String url = String.format(urlTemplate, profile, lng1, lat1, lng2, lat2);
        try {
            String json = restTemplate.getForObject(url, String.class);
            if (json == null) return null;
            JsonNode root = mapper.readTree(json);
            JsonNode routes = root.get("routes");
            if (routes == null || !routes.isArray() || routes.size() == 0) return null;
            JsonNode route = routes.get(0);
            OsrmRoute r = new OsrmRoute();
            r.setDistance(route.get("distance").asDouble() / 1000.0);
            r.setDuration(route.get("duration").asDouble());
            parseGeometry(route, r);

            if (withSteps) {
                List<NavigationStep> steps = parseSteps(route);
                r.setSteps(steps);
            }

            return r;
        } catch (Exception e) {
            logger.debug("OSRM route failed for {}: {}", profile, e.getMessage());
            return null;
        }
    }

    private void parseGeometry(JsonNode route, OsrmRoute r) {
        JsonNode geom = route.get("geometry");
        if (geom == null) return;
        JsonNode coords = geom.get("coordinates");
        if (coords == null || !coords.isArray()) return;

        List<double[]> pts = new ArrayList<>();
        StringBuilder wkt = new StringBuilder("LINESTRING(");
        for (int i = 0; i < coords.size(); i++) {
            JsonNode c = coords.get(i);
            double[] pt = {c.get(0).asDouble(), c.get(1).asDouble()};
            pts.add(pt);
            if (i > 0) wkt.append(",");
            wkt.append(String.format("%.6f %.6f", pt[0], pt[1]));
        }
        wkt.append(")");
        r.setCoordinates(pts);
        r.setGeometryWkt(wkt.toString());
    }

    private List<NavigationStep> parseSteps(JsonNode route) {
        List<NavigationStep> result = new ArrayList<>();
        JsonNode legs = route.get("legs");
        if (legs == null || !legs.isArray() || legs.size() == 0) return result;

        int stepNumber = 0;
        for (JsonNode leg : legs) {
            JsonNode steps = leg.get("steps");
            if (steps == null || !steps.isArray()) continue;

            for (JsonNode step : steps) {
                NavigationStep ns = new NavigationStep();
                ns.setStepNumber(++stepNumber);

                String instruction = buildInstruction(step);
                ns.setInstruction(instruction);
                ns.setManeuverType(step.path("maneuver").path("type").asText(""));
                ns.setManeuverModifier(step.path("maneuver").path("modifier").asText(""));
                ns.setStreetName(step.path("name").asText(""));
                ns.setDistanceKm(step.path("distance").asDouble() / 1000.0);
                ns.setDurationSeconds(step.path("duration").asDouble());

                JsonNode location = step.path("maneuver").path("location");
                if (location.isArray() && location.size() >= 2) {
                    ns.setLongitude(location.get(0).asDouble());
                    ns.setLatitude(location.get(1).asDouble());
                }

                result.add(ns);
            }
        }
        return result;
    }

    private String buildInstruction(JsonNode step) {
        String type = step.path("maneuver").path("type").asText("");
        String modifier = step.path("maneuver").path("modifier").asText("");
        String name = step.path("name").asText("");
        double dist = step.path("distance").asDouble();

        String dir = switch (modifier) {
            case "left" -> "左转";
            case "right" -> "右转";
            case "slight left" -> "稍向左";
            case "slight right" -> "稍向右";
            case "sharp left" -> "急左转";
            case "sharp right" -> "急右转";
            case "straight" -> "直行";
            case "uturn" -> "掉头";
            default -> modifier;
        };

        String action = switch (type) {
            case "depart" -> "出发";
            case "turn" -> dir;
            case "continue" -> "继续" + dir;
            case "new name" -> "进入" + (name.isEmpty() ? "" : name);
            case "end of road" -> "在道路尽头" + dir;
            case "merge" -> "汇入" + dir;
            case "fork" -> "靠" + (modifier.contains("left") ? "左" : "右") + "行驶";
            case "ramp" -> "上匝道" + dir;
            case "roundabout", "rotary" -> "进入环岛" + (modifier.isEmpty() ? "" : "第" + modifier + "个出口");
            case "roundabout turn" -> "环岛" + dir;
            case "arrive" -> "到达目的地";
            default -> type;
        };

        StringBuilder sb = new StringBuilder(action);
        if (!name.isEmpty() && !type.equals("depart") && !type.equals("arrive")) {
            sb.append("，进入").append(name);
        }
        if (dist >= 50) {
            sb.append("，行驶约").append(String.format("%.0f", dist)).append("米");
        }
        return sb.toString();
    }
}
