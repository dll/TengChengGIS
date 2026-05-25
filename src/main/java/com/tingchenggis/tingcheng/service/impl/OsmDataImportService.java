package com.tingchenggis.tingcheng.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.AdminDivision;
import com.tingchenggis.tingcheng.entity.ScenicArea;
import com.tingchenggis.tingcheng.service.AdminDivisionService;
import com.tingchenggis.tingcheng.service.ScenicAreaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class OsmDataImportService {

    private static final Logger logger = LoggerFactory.getLogger(OsmDataImportService.class);
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private final ScenicAreaService scenicAreaService;
    private final AdminDivisionService adminDivisionService;
    private final ObjectMapper objectMapper;

    private static final double CHUZHOU_SOUTH = 31.5;
    private static final double CHUZHOU_WEST = 117.0;
    private static final double CHUZHOU_NORTH = 33.0;
    private static final double CHUZHOU_EAST = 119.0;

    public OsmDataImportService(ScenicAreaService scenicAreaService,
                                 AdminDivisionService adminDivisionService,
                                 ObjectMapper objectMapper) {
        this.scenicAreaService = scenicAreaService;
        this.adminDivisionService = adminDivisionService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> importAll() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenic", importScenicAreas());
        result.put("admin", importAdminDivisions());
        return result;
    }

    public Map<String, Object> importScenicAreas() {
        Map<String, Object> result = new LinkedHashMap<>();
        int created = 0;
        int errors = 0;

        String bbox = String.format(Locale.US, "%f,%f,%f,%f",
            CHUZHOU_SOUTH, CHUZHOU_WEST, CHUZHOU_NORTH, CHUZHOU_EAST);

        String query = "[out:json][timeout:60];("
            + "way[\"tourism\"=\"attraction\"](" + bbox + ");"
            + "way[\"leisure\"=\"park\"](" + bbox + ");"
            + "way[\"tourism\"=\"zoo\"](" + bbox + ");"
            + "way[\"tourism\"=\"theme_park\"](" + bbox + ");"
            + "relation[\"tourism\"=\"attraction\"](" + bbox + ");"
            + "relation[\"leisure\"=\"park\"](" + bbox + ");"
            + ");out geom;";

        try {
            JsonNode root = queryOverpass(query);
            JsonNode elements = root.get("elements");
            if (elements == null || !elements.isArray()) {
                result.put("message", "No elements in response");
                return result;
            }

            for (JsonNode el : elements) {
                try {
                    String osmType = el.has("type") ? el.get("type").asText() : "";
                    if (!"way".equals(osmType) && !"relation".equals(osmType)) continue;

                    JsonNode tags = el.get("tags");
                    if (tags == null) continue;

                    String name = tags.has("name") ? tags.get("name").asText()
                        : tags.has("name:zh") ? tags.get("name:zh").asText() : "未命名";
                    String type = tags.has("tourism") ? tags.get("tourism").asText()
                        : tags.has("leisure") ? tags.get("leisure").asText() : "attraction";

                    ScenicArea sa = new ScenicArea();
                    sa.setName(name);
                    sa.setChineseName(name);
                    sa.setAreaType(mapOsmType(type));
                    sa.setDescription(tags.has("description") ? tags.get("description").asText() : null);
                    sa.setAddress(tags.has("addr:full") ? tags.get("addr:full").asText() : null);
                    sa.setOpeningHours(tags.has("opening_hours") ? tags.get("opening_hours").asText() : null);
                    sa.setIsOpenToPublic(true);
                    sa.setTicketPrice(0.0);
                    sa.setNotes("OSM导入 - " + type);

                    String wkt = extractGeometryWkt(el);
                    if (wkt != null) {
                        sa.setGeomWkt(wkt);
                        double[] centroid = computeCentroid(el);
                        sa.setLongitude(centroid[0]);
                        sa.setLatitude(centroid[1]);
                    }

                    scenicAreaService.createScenicArea(sa);
                    created++;
                } catch (Exception e) {
                    errors++;
                }
            }
        } catch (Exception e) {
            logger.error("OSM scenic import failed: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }

        result.put("created", created);
        result.put("errors", errors);
        return result;
    }

    public Map<String, Object> importAdminDivisions() {
        Map<String, Object> result = new LinkedHashMap<>();
        int created = 0;
        int errors = 0;

        String bbox = String.format(Locale.US, "%f,%f,%f,%f",
            CHUZHOU_SOUTH, CHUZHOU_WEST, CHUZHOU_NORTH, CHUZHOU_EAST);

        String query = "[out:json][timeout:60];("
            + "relation[\"boundary\"=\"administrative\"][\"admin_level\"=\"5\"](" + bbox + ");"
            + "relation[\"boundary\"=\"administrative\"][\"admin_level\"=\"6\"](" + bbox + ");"
            + "relation[\"boundary\"=\"administrative\"][\"admin_level\"=\"8\"](" + bbox + ");"
            + ");out geom;";

        try {
            JsonNode root = queryOverpass(query);
            JsonNode elements = root.get("elements");
            if (elements == null || !elements.isArray()) {
                result.put("message", "No elements in response");
                return result;
            }

            for (JsonNode el : elements) {
                try {
                    JsonNode tags = el.get("tags");
                    if (tags == null) continue;

                    String name = tags.has("name") ? tags.get("name").asText()
                        : tags.has("name:zh") ? tags.get("name:zh").asText() : "未命名";
                    int adminLevel = tags.has("admin_level") ? tags.get("admin_level").asInt() : 8;
                    String adminCode = tags.has("ref") ? tags.get("ref").asText() : null;

                    AdminDivision ad = new AdminDivision();
                    ad.setName(name);
                    ad.setChineseName(name);
                    ad.setAdminLevel(mapAdminLevel(adminLevel));
                    ad.setAdminCode(adminCode);
                    ad.setNotes("OSM导入 - admin_level=" + adminLevel);

                    String wkt = extractGeometryWkt(el);
                    if (wkt != null) {
                        ad.setGeomWkt(wkt);
                    }

                    adminDivisionService.createAdminDivision(ad);
                    created++;
                } catch (Exception e) {
                    errors++;
                }
            }

            resolveParentRelationships();
        } catch (Exception e) {
            logger.error("OSM admin import failed: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }

        result.put("created", created);
        result.put("errors", errors);
        return result;
    }

    private void resolveParentRelationships() {
        List<AdminDivision> all = adminDivisionService.getAllAdminDivisions();
        for (AdminDivision child : all) {
            if (child.getParentId() != null) continue;
            for (AdminDivision candidate : all) {
                if (candidate.getId().equals(child.getId())) continue;
                if (isAdminLevelParent(candidate.getAdminLevel(), child.getAdminLevel())
                    && containsGeometry(candidate.getGeomWkt(), child.getGeomWkt())) {
                    child.setParentId(candidate.getId());
                    child.setParentName(candidate.getName());
                    adminDivisionService.updateAdminDivision(child.getId(), child);
                    break;
                }
            }
        }
    }

    private boolean isAdminLevelParent(String parentLevel, String childLevel) {
        Map<String, Integer> order = Map.of("CITY", 5, "DISTRICT", 6, "COUNTY", 6, "STREET", 8, "TOWN", 8);
        int p = order.getOrDefault(parentLevel, 99);
        int c = order.getOrDefault(childLevel, 99);
        return p < c;
    }

    private boolean containsGeometry(String parentWkt, String childWkt) {
        if (parentWkt == null || childWkt == null) return false;
        double[] parentCenter = parseWktCentroid(parentWkt);
        double[] childCenter = parseWktCentroid(childWkt);
        if (parentCenter == null || childCenter == null) return false;
        return Math.abs(parentCenter[0] - childCenter[0]) < 0.5
            && Math.abs(parentCenter[1] - childCenter[1]) < 0.5;
    }

    private JsonNode queryOverpass(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = new URI(OVERPASS_URL + "?data=" + encoded);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("User-Agent", "TingChengGIS/1.0");
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("Overpass API returned HTTP " + code);
        }
        return objectMapper.readTree(conn.getInputStream());
    }

    /**
     * Extract WKT geometry from an OSM element.
     * Overpass "out geom;" returns geometry as [{lat, lon}, ...] for ways
     * and [[{lat, lon}, ...], ...] for relations (one array per member).
     * We also handle GeoJSON-style geometry with "coordinates" key.
     */
    private String extractGeometryWkt(JsonNode element) {
        JsonNode geom = element.get("geometry");
        if (geom == null || !geom.isArray()) return null;
        if (geom.size() == 0) return null;

        try {
            // Overpass format: check if first element has "lat"/"lon" fields
            JsonNode first = geom.get(0);
            if (first.has("lat") && first.has("lon")) {
                return overpassGeomToWkt(geom);
            }
            // GeoJSON format: check for "coordinates" key
            if (geom.has("coordinates")) {
                return jsonCoordsToWkt(geom.get("coordinates"));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert Overpass API geometry array (array of {lat, lon}) to WKT.
     * For ways: [{lat, lon}, {lat, lon}, ...] -> POLYGON
     * For relations: [[{lat, lon}, ...], [{lat, lon}, ...]] -> MULTIPOLYGON
     */
    private String overpassGeomToWkt(JsonNode geom) {
        if (isOverpassMultiPolygon(geom)) {
            StringBuilder sb = new StringBuilder("MULTIPOLYGON(");
            for (int i = 0; i < geom.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("(");
                appendOverpassRing(sb, geom.get(i));
                sb.append(")");
            }
            sb.append(")");
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder("POLYGON((");
            appendOverpassRing(sb, geom);
            sb.append("))");
            return sb.toString();
        }
    }

    private boolean isOverpassMultiPolygon(JsonNode geom) {
        if (geom.size() == 0) return false;
        JsonNode first = geom.get(0);
        if (!first.isArray()) return false;
        if (first.size() == 0) return false;
        return first.get(0).has("lat") || first.get(0).isArray();
    }

    private void appendOverpassRing(StringBuilder sb, JsonNode ring) {
        List<double[]> pts = new ArrayList<>();
        for (JsonNode pt : ring) {
            double lon = pt.has("lon") ? pt.get("lon").asDouble() : pt.get(0).asDouble();
            double lat = pt.has("lat") ? pt.get("lat").asDouble() : pt.get(1).asDouble();
            pts.add(new double[]{lon, lat});
        }
        // Close ring if not already closed
        if (!pts.isEmpty()) {
            double[] first = pts.get(0);
            double[] last = pts.get(pts.size() - 1);
            if (first[0] != last[0] || first[1] != last[1]) {
                pts.add(new double[]{first[0], first[1]});
            }
        }
        for (int i = 0; i < pts.size(); i++) {
            if (i > 0) sb.append(",");
            double[] p = pts.get(i);
            sb.append(String.format(Locale.US, "%f %f", p[0], p[1]));
        }
    }

    // GeoJSON format handlers (fallback for non-Overpass sources)

    private String jsonCoordsToWkt(JsonNode coords) {
        StringBuilder sb = new StringBuilder();
        if (isMultiPolygon(coords)) {
            sb.append("MULTIPOLYGON(");
            for (int i = 0; i < coords.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("(");
                appendGeoJsonRing(sb, coords.get(i));
                sb.append(")");
            }
            sb.append(")");
        } else {
            sb.append("POLYGON((");
            appendGeoJsonRing(sb, coords);
            sb.append("))");
        }
        return sb.toString();
    }

    private boolean isMultiPolygon(JsonNode coords) {
        if (coords.size() == 0) return false;
        JsonNode first = coords.get(0);
        if (!first.isArray() || first.size() == 0) return false;
        JsonNode inner = first.get(0);
        return inner.isArray() && inner.size() > 0 && inner.get(0).isArray();
    }

    private void appendGeoJsonRing(StringBuilder sb, JsonNode ring) {
        List<double[]> pts = flattenRing(ring);
        for (int i = 0; i < pts.size(); i++) {
            if (i > 0) sb.append(",");
            double[] p = pts.get(i);
            sb.append(String.format(Locale.US, "%f %f", p[0], p[1]));
        }
    }

    private List<double[]> flattenRing(JsonNode node) {
        List<double[]> result = new ArrayList<>();
        if (node.isArray() && node.size() > 0) {
            JsonNode first = node.get(0);
            if (first.isNumber()) {
                if (node.size() >= 2)
                    result.add(new double[]{node.get(0).asDouble(), node.get(1).asDouble()});
            } else if (first.isArray()) {
                JsonNode inner = first.get(0);
                if (inner == null || !inner.isArray()) {
                    for (JsonNode p : node) {
                        if (p.isArray() && p.size() >= 2)
                            result.add(new double[]{p.get(0).asDouble(), p.get(1).asDouble()});
                    }
                } else {
                    for (JsonNode p : first) {
                        if (p.isArray() && p.size() >= 2)
                            result.add(new double[]{p.get(0).asDouble(), p.get(1).asDouble()});
                    }
                }
            }
        }
        return result;
    }

    private double[] computeCentroid(JsonNode element) {
        List<double[]> pts = collectAllCoords(element);
        if (pts.isEmpty()) {
            // fallback: use Chuzhou center
            return new double[]{(CHUZHOU_WEST + CHUZHOU_EAST) / 2, (CHUZHOU_SOUTH + CHUZHOU_NORTH) / 2};
        }
        double sumX = 0, sumY = 0;
        for (double[] p : pts) {
            sumX += p[0];
            sumY += p[1];
        }
        return new double[]{sumX / pts.size(), sumY / pts.size()};
    }

    private List<double[]> collectAllCoords(JsonNode element) {
        List<double[]> result = new ArrayList<>();
        JsonNode geom = element.get("geometry");
        if (geom == null || !geom.isArray()) return result;

        // Overpass format: geometry array of {lat, lon} objects or nested arrays
        collectOverpassCoords(geom, result);
        return result;
    }

    private void collectOverpassCoords(JsonNode node, List<double[]> out) {
        if (node.size() == 0) return;
        JsonNode first = node.get(0);
        if (first.has("lat") && first.has("lon")) {
            for (JsonNode pt : node) {
                out.add(new double[]{pt.get("lon").asDouble(), pt.get("lat").asDouble()});
            }
        } else if (first.isArray()) {
            for (JsonNode child : node) {
                collectOverpassCoords(child, out);
            }
        }
    }

    private double[] parseWktCentroid(String wkt) {
        if (wkt == null || !wkt.contains("(")) return null;
        try {
            int start = wkt.indexOf("((");
            if (start < 0) start = wkt.indexOf("(");
            int end = wkt.lastIndexOf("))");
            if (end < 0) end = wkt.lastIndexOf(")");
            if (start < 0 || end <= start) return null;
            String coords = wkt.substring(start + 1, end);
            coords = coords.replace("(", "").replace(")", "");
            String[] parts = coords.split(",");
            double sumX = 0, sumY = 0;
            int count = 0;
            for (String part : parts) {
                String[] xy = part.trim().split("\\s+");
                if (xy.length >= 2) {
                    sumX += Double.parseDouble(xy[0]);
                    sumY += Double.parseDouble(xy[1]);
                    count++;
                }
            }
            return count > 0 ? new double[]{sumX / count, sumY / count} : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String mapOsmType(String osmType) {
        return switch (osmType) {
            case "park", "garden", "nature_reserve" -> "公园";
            case "zoo" -> "动物园";
            case "theme_park" -> "主题公园";
            case "museum" -> "博物馆";
            case "viewpoint" -> "观景点";
            default -> "风景区";
        };
    }

    private String mapAdminLevel(int level) {
        return switch (level) {
            case 4, 5 -> "CITY";
            case 6, 7 -> "DISTRICT";
            case 8, 9, 10 -> "STREET";
            default -> "STREET";
        };
    }
}
