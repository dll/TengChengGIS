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
        String url = String.format(OSRM_URL, profile, lng1, lat1, lng2, lat2);
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
            JsonNode geom = route.get("geometry");
            if (geom != null) {
                JsonNode coords = geom.get("coordinates");
                if (coords != null && coords.isArray()) {
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
            }
            return r;
        } catch (Exception e) {
            logger.debug("OSRM route failed for {}: {}", profile, e.getMessage());
            return null;
        }
    }
}
