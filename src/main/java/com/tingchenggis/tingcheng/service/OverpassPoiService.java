package com.tingchenggis.tingcheng.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OverpassPoiService {

    private static final Logger logger = LoggerFactory.getLogger(OverpassPoiService.class);
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final int MAX_RESULTS = 50;
    private static final int TIMEOUT_SECONDS = 25;

    private final ObjectMapper objectMapper;

    public OverpassPoiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> queryNearbyPois(double lat, double lon, double radiusKm) {
        int radiusMeters = (int) (radiusKm * 1000);
        String query = buildOverpassQuery(lat, lon, radiusMeters);

        try {
            JsonNode root = queryOverpass(query);
            JsonNode elements = root.get("elements");
            if (elements == null || !elements.isArray()) {
                logger.warn("Overpass returned no elements for POI query at ({}, {})", lat, lon);
                return Collections.emptyList();
            }

            List<Map<String, Object>> pois = new ArrayList<>();
            for (JsonNode el : elements) {
                try {
                    Map<String, Object> poi = parseElement(el, lat, lon);
                    if (poi != null) {
                        pois.add(poi);
                    }
                } catch (Exception e) {
                    logger.debug("Skipping POI element: {}", e.getMessage());
                }
            }

            pois.sort(Comparator.comparingDouble(p -> (double) p.get("distance")));
            return pois;
        } catch (Exception e) {
            logger.error("Overpass POI query failed at ({}, {}): {}", lat, lon, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String buildOverpassQuery(double lat, double lon, int radiusMeters) {
        String coord = String.format(Locale.US, "%f,%f", lat, lon);
        return "[out:json][timeout:" + TIMEOUT_SECONDS + "];("
            + "nwr[\"amenity\"](" + coord + ";" + radiusMeters + ");"
            + "nwr[\"tourism\"](" + coord + ";" + radiusMeters + ");"
            + "nwr[\"shop\"](" + coord + ";" + radiusMeters + ");"
            + "nwr[\"leisure\"](" + coord + ";" + radiusMeters + ");"
            + "nwr[\"highway\"=\"bus_stop\"](" + coord + ";" + radiusMeters + ");"
            + "nwr[\"historic\"](" + coord + ";" + radiusMeters + ");"
            + ");out center " + MAX_RESULTS + ";";
    }

    private JsonNode queryOverpass(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = new URI(OVERPASS_URL + "?data=" + encoded);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("User-Agent", "TingChengGIS/1.0");
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new BusinessException("Overpass API returned HTTP " + code);
        }
        return objectMapper.readTree(conn.getInputStream());
    }

    private Map<String, Object> parseElement(JsonNode el, double centerLat, double centerLon) {
        JsonNode tags = el.get("tags");
        if (tags == null) return null;

        String type = el.has("type") ? el.get("type").asText() : "node";
        String name = extractName(tags);
        if (name == null) {
            name = inferDefaultName(tags);
        }

        double[] coords = extractCoordinates(el, type);
        if (coords == null) return null;

        double lat = coords[0];
        double lon = coords[1];
        double distance = calculateDistance(centerLat, centerLon, lat, lon);

        String category = determineCategory(tags);
        String icon = determineIcon(category);

        Map<String, Object> poi = new LinkedHashMap<>();
        poi.put("name", name);
        poi.put("category", category);
        poi.put("icon", icon);
        poi.put("latitude", lat);
        poi.put("longitude", lon);
        poi.put("distance", Math.round(distance * 100.0) / 100.0);
        poi.put("osmType", type);
        poi.put("osmId", el.has("id") ? el.get("id").asLong() : 0);

        String openingHours = tags.has("opening_hours") ? tags.get("opening_hours").asText() : null;
        if (openingHours != null) {
            poi.put("openingHours", openingHours);
        }

        String phone = tags.has("phone") ? tags.get("phone").asText()
            : tags.has("contact:phone") ? tags.get("contact:phone").asText() : null;
        if (phone != null) {
            poi.put("phone", phone);
        }

        String website = tags.has("website") ? tags.get("website").asText()
            : tags.has("contact:website") ? tags.get("contact:website").asText() : null;
        if (website != null) {
            poi.put("website", website);
        }

        String cuisine = tags.has("cuisine") ? tags.get("cuisine").asText() : null;
        if (cuisine != null) {
            poi.put("cuisine", cuisine);
        }

        return poi;
    }

    private String extractName(JsonNode tags) {
        if (tags.has("name:zh")) return tags.get("name:zh").asText();
        if (tags.has("name")) return tags.get("name").asText();
        if (tags.has("short_name")) return tags.get("short_name").asText();
        return null;
    }

    private String inferDefaultName(JsonNode tags) {
        for (String key : new String[]{"amenity", "tourism", "shop", "leisure", "highway", "historic"}) {
            if (tags.has(key)) {
                return mapTagToChinese(key, tags.get(key).asText());
            }
        }
        return "未知设施";
    }

    private double[] extractCoordinates(JsonNode el, String type) {
        if (type.equals("node")) {
            if (el.has("lat") && el.has("lon")) {
                return new double[]{el.get("lat").asDouble(), el.get("lon").asDouble()};
            }
        }
        JsonNode center = el.get("center");
        if (center != null && center.has("lat") && center.has("lon")) {
            return new double[]{center.get("lat").asDouble(), center.get("lon").asDouble()};
        }
        return null;
    }

    private String determineCategory(JsonNode tags) {
        if (tags.has("amenity")) return tags.get("amenity").asText();
        if (tags.has("tourism")) return tags.get("tourism").asText();
        if (tags.has("shop")) return tags.get("shop").asText();
        if (tags.has("leisure")) return tags.get("leisure").asText();
        if (tags.has("highway")) return tags.get("highway").asText();
        if (tags.has("historic")) return tags.get("historic").asText();
        return "other";
    }

    private String determineIcon(String category) {
        return switch (category) {
            case "parking" -> "P";
            case "toilets" -> "WC";
            case "restaurant", "cafe", "fast_food", "food_court", "bar", "pub" -> "\uD83C\uDF7D\uFE0F";
            case "hotel", "hostel", "motel", "guest_house" -> "\uD83C\uDFE8";
            case "pharmacy" -> "\uD83D\uDC8A";
            case "hospital", "clinic", "doctors" -> "\uD83C\uDFE5";
            case "bank", "atm" -> "\uD83C\uDFE6";
            case "fuel" -> "\u26FD";
            case "bus_stop", "bus_station", "taxi" -> "\uD83D\uDE8D";
            case "museum" -> "\uD83C\uDFF0";
            case "viewpoint" -> "\uD83D\uDD0D";
            case "park", "garden", "playground", "pitch", "fitness_centre", "sports_centre" -> "\uD83C\uDFD9\uFE0F";
            case "souvenir", "gift" -> "\uD83C\uDF81";
            case "supermarket", "convenience", "mall", "department_store" -> "\uD83D\uDED2";
            case "library" -> "\uD83D\uDCDA";
            case "theatre", "cinema", "arts_centre" -> "\uD83C\uDFAD";
            case "place_of_worship" -> "\u26EA";
            case "post_office", "post_box" -> "\uD83D\uDCEB";
            case "police" -> "\uD83D\uDE93";
            case "fire_station" -> "\uD83D\uDD25";
            case "memorial", "monument" -> "\uD83D\uDDFB";
            case "information" -> "\u2139\uFE0F";
            default -> "\uD83D\uDCCD";
        };
    }

    private String mapTagToChinese(String key, String value) {
        return switch (key + ":" + value) {
            case "amenity:parking" -> "停车场";
            case "amenity:restaurant" -> "餐厅";
            case "amenity:cafe" -> "咖啡馆";
            case "amenity:fast_food" -> "快餐";
            case "amenity:toilets" -> "洗手间";
            case "amenity:pharmacy" -> "药店";
            case "amenity:hospital" -> "医院";
            case "amenity:clinic" -> "诊所";
            case "amenity:bank" -> "银行";
            case "amenity:atm" -> "ATM取款机";
            case "amenity:fuel" -> "加油站";
            case "amenity:bus_station" -> "公交站";
            case "amenity:taxi" -> "出租车";
            case "amenity:bench" -> "休息区";
            case "amenity:drinking_water" -> "饮用水";
            case "amenity:fountain" -> "喷泉";
            case "amenity:police" -> "派出所";
            case "amenity:fire_station" -> "消防站";
            case "amenity:post_office" -> "邮局";
            case "amenity:library" -> "图书馆";
            case "amenity:theatre" -> "剧院";
            case "amenity:place_of_worship" -> "宗教场所";
            case "tourism:hotel" -> "酒店";
            case "tourism:hostel" -> "青年旅舍";
            case "tourism:motel" -> "汽车旅馆";
            case "tourism:guest_house" -> "民宿";
            case "tourism:information" -> "信息中心";
            case "tourism:museum" -> "博物馆";
            case "tourism:viewpoint" -> "观景台";
            case "tourism:attraction" -> "景点";
            case "shop:souvenir" -> "纪念品店";
            case "shop:gift" -> "礼品店";
            case "shop:convenience" -> "便利店";
            case "shop:supermarket" -> "超市";
            case "shop:mall" -> "商场";
            case "shop:department_store" -> "百货商场";
            case "leisure:park" -> "公园";
            case "leisure:garden" -> "花园";
            case "leisure:playground" -> "游乐场";
            case "leisure:pitch" -> "运动场";
            case "leisure:fitness_centre" -> "健身中心";
            case "leisure:sports_centre" -> "体育中心";
            case "highway:bus_stop" -> "公交站";
            case "historic:memorial" -> "纪念碑";
            case "historic:monument" -> "纪念碑";
            case "historic:ruins" -> "遗迹";
            default -> value;
        };
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
