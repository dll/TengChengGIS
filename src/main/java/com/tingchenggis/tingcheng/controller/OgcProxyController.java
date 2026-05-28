package com.tingchenggis.tingcheng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ogc")
@CrossOrigin(origins = "*")
public class OgcProxyController {

    private static final Logger logger = LoggerFactory.getLogger(OgcProxyController.class);
    private static final int TIMEOUT_MS = 30000;

    private final ObjectMapper objectMapper;

    public OgcProxyController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** 代理 WMS GetCapabilities（返回解析后的 JSON） */
    @PostMapping("/wms/capabilities")
    public ResponseEntity<Map<String, Object>> wmsCapabilities(@RequestBody Map<String, String> req) {
        String url = req.get("url");
        if (url == null || url.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "URL不能为空"));
        try {
            String xml = httpGet(url, MediaType.TEXT_XML_VALUE);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("service", parseWmsCapabilities(xml));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("WMS Capabilities 获取失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 代理 WMS GetMap（返回图片） */
    @PostMapping("/wms/map")
    public ResponseEntity<byte[]> wmsMap(@RequestBody Map<String, String> req) {
        String url = req.get("url");
        if (url == null || url.isBlank())
            return ResponseEntity.badRequest().body(null);
        try {
            byte[] data = httpGetBytes(url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            logger.error("WMS GetMap 失败: {}", e.getMessage());
            return ResponseEntity.status(502).body(null);
        }
    }

    /** 代理 WFS GetFeature（返回 GeoJSON） */
    @PostMapping("/wfs/features")
    public ResponseEntity<Map<String, Object>> wfsFeatures(@RequestBody Map<String, String> req) {
        String url = req.get("url");
        if (url == null || url.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "URL不能为空"));
        try {
            String json = httpGet(url, MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", objectMapper.readValue(json, Map.class));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("WFS GetFeature 失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 通用 HTTP GET 代理 */
    @PostMapping("/proxy")
    public ResponseEntity<byte[]> proxy(@RequestBody Map<String, String> req) {
        String url = req.get("url");
        if (url == null || url.isBlank())
            return ResponseEntity.badRequest().body(null);
        try {
            String accept = req.getOrDefault("accept", MediaType.APPLICATION_JSON_VALUE);
            byte[] data = httpGetBytes(url, accept);
            String ct = detectContentType(data);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(ct));
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            logger.error("代理请求失败: {}", e.getMessage());
            return ResponseEntity.status(502).body(null);
        }
    }

    /** 获取推荐 OGC 服务列表 */
    @GetMapping("/presets")
    public ResponseEntity<Map<String, Object>> presets() {
        List<Map<String, Object>> services = new ArrayList<>();

        Map<String, Object> gs = new LinkedHashMap<>();
        gs.put("name", "本地GeoServer（WMS/WFS推荐）");
        gs.put("url", "http://localhost:8080/geoserver");
        gs.put("wmsUrl", "http://localhost:8080/geoserver/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
        gs.put("wfsUrl", "http://localhost:8080/geoserver/wfs?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetCapabilities");
        gs.put("type", "WMS+WFS");
        gs.put("description", "自行部署的GeoServer，可发布景区、区划等GIS数据为WMS/WFS服务");
        gs.put("note", "支持同时添加WMS底图和WFS矢量图层");
        services.add(gs);

        Map<String, Object> osm = new LinkedHashMap<>();
        osm.put("name", "OSM WMS 地形图");
        osm.put("url", "https://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
        osm.put("type", "WMS");
        osm.put("description", "OpenStreetMap WMS - 含 OSM_WMS(标准)/TOPO-WMS(地形) 等图层");
        osm.put("layers", "OSM_WMS,TOPO-WMS");
        services.add(osm);

        Map<String, Object> gsDemo = new LinkedHashMap<>();
        gsDemo.put("name", "GeoServer演示（全球保护区）");
        gsDemo.put("url", "https://demo.geo-solutions.it/geoserver/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
        gsDemo.put("type", "WMS");
        gsDemo.put("description", "GeoServer官方演示 - 含 topp:states(美国州界)、world:protected_areas(保护区) 等专题图层");
        gsDemo.put("note", "可用于测试WMS/WFS连接和图层加载功能");
        services.add(gsDemo);

        Map<String, Object> tdt = new LinkedHashMap<>();
        tdt.put("name", "天地图 矢量/影像 WMTS");
        tdt.put("url", "https://t0.tianditu.gov.cn/vec_c/wmts?SERVICE=WMTS&REQUEST=GetCapabilities");
        tdt.put("imgUrl", "https://t0.tianditu.gov.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetCapabilities");
        tdt.put("type", "WMTS");
        tdt.put("description", "国家地理信息公共服务平台 - 矢量底图 + 卫星影像（需申请tk）");
        tdt.put("note", "需在URL后加 &tk=您的密钥，天地图官网免费申请");
        services.add(tdt);

        Map<String, Object> bing = new LinkedHashMap<>();
        bing.put("name", "Bing Maps 卫星影像");
        bing.put("url", "https://dev.virtualearth.net/REST/v1/Imagery/Metadata/Aerial");
        bing.put("type", "WMTS");
        bing.put("description", "Bing Maps 卫星影像底图（需申请Key）");
        bing.put("note", "需在 Bing Maps Dev Center 申请 Key");
        services.add(bing);

        // 6th: OSM 标准地图 WMS (auto-load default)
        Map<String, Object> osmStd = new LinkedHashMap<>();
        osmStd.put("name", "OSM WMS 标准地图");
        osmStd.put("url", "https://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
        osmStd.put("type", "WMS");
        osmStd.put("description", "OpenStreetMap 标准街道底图（OSM_WMS图层）");
        osmStd.put("layers", "OSM_WMS");
        services.add(osmStd);

        return ResponseEntity.ok(Map.of("success", true, "data", services));
    }

    /** 代理 WFS GetCapabilities（返回解析后的 JSON） */
    @PostMapping("/wfs/capabilities")
    public ResponseEntity<Map<String, Object>> wfsCapabilities(@RequestBody Map<String, String> req) {
        String url = req.get("url");
        if (url == null || url.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "URL不能为空"));
        try {
            String xml = httpGet(url, MediaType.TEXT_XML_VALUE);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            List<Map<String, Object>> types = parseWfsCapabilities(xml);
            result.put("types", types);
            if (!types.isEmpty() && types.get(0).containsKey("_serviceTitle")) {
                result.put("_serviceTitle", types.get(0).remove("_serviceTitle"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("WFS Capabilities 获取失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ---- private helpers ----

    private String httpGet(String url, String accept) throws Exception {
        URI uri = new URI(url);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "TingChengGIS/1.0");
        if (accept != null) conn.setRequestProperty("Accept", accept);
        int code = conn.getResponseCode();
        if (code != 200) throw new BusinessException("OGC 服务返回 HTTP " + code);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        try (InputStream in = conn.getInputStream()) {
            int n;
            while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        }
        return bos.toString(StandardCharsets.UTF_8);
    }

    private byte[] httpGetBytes(String url) throws Exception {
        return httpGetBytes(url, null);
    }

    private byte[] httpGetBytes(String url, String accept) throws Exception {
        URI uri = new URI(url);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "TingChengGIS/1.0");
        if (accept != null) conn.setRequestProperty("Accept", accept);
        int code = conn.getResponseCode();
        if (code != 200) throw new BusinessException("OGC 服务返回 HTTP " + code);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        try (InputStream in = conn.getInputStream()) {
            int n;
            while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }

    private String detectContentType(byte[] data) {
        if (data.length > 3 && data[0] == (byte)0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G')
            return "image/png";
        if (data.length > 2 && data[0] == (byte)0xFF && data[1] == (byte)0xD8)
            return "image/jpeg";
        String s = new String(data, 0, Math.min(data.length, 200), StandardCharsets.UTF_8).trim();
        if (s.startsWith("<")) return "application/xml";
        if (s.startsWith("{") || s.startsWith("[")) return "application/json";
        return "application/octet-stream";
    }

    /** 简易 WMS Capabilities XML 解析（提取关键图层信息） */
    private List<Map<String, Object>> parseWmsCapabilities(String xml) {
        List<Map<String, Object>> layers = new ArrayList<>();
        try {
            String serviceTitle = extractXmlTag(xml, "Title");
            String serviceAbstract = extractXmlTag(xml, "Abstract");

            // 提取所有 Layer 节点
            int idx = 0;
            while (true) {
                int start = xml.indexOf("<Layer", idx);
                if (start < 0) break;
                int end = xml.indexOf("</Layer>", start);
                if (end < 0) break;
                String layerXml = xml.substring(start, end + 8);
                idx = end + 8;

                String name = extractXmlTag(layerXml, "Name");
                if (name == null) continue;

                Map<String, Object> l = new LinkedHashMap<>();
                l.put("name", name);
                l.put("title", extractXmlTag(layerXml, "Title"));
                l.put("abstract", extractXmlTag(layerXml, "Abstract"));
                l.put("queryable", layerXml.contains("queryable=\"1\""));
                layers.add(l);
            }

            if (!layers.isEmpty()) {
                layers.get(0).put("_serviceTitle", serviceTitle);
                layers.get(0).put("_serviceAbstract", serviceAbstract);
            }
        } catch (Exception e) {
            logger.warn("WMS Capabilities 解析失败: {}", e.getMessage());
        }
        return layers;
    }

    /** 简易 WFS Capabilities XML 解析（提取要素类型信息） */
    private List<Map<String, Object>> parseWfsCapabilities(String xml) {
        List<Map<String, Object>> types = new ArrayList<>();
        try {
            String serviceTitle = extractXmlTag(xml, "Title");
            int idx = 0;
            while (true) {
                int start = xml.indexOf("<FeatureType", idx);
                if (start < 0) {
                    start = xml.indexOf("<FeatureType ", idx);
                    if (start < 0) break;
                }
                int end = xml.indexOf("</FeatureType>", start);
                if (end < 0) break;
                String ftXml = xml.substring(start, end + 14);
                idx = end + 14;

                // Try both with and without namespace prefix
                String name = extractXmlTag(ftXml, "Name");
                if (name == null) name = extractXmlTag(ftXml, "ows:Name");
                if (name == null) continue;

                String title = extractXmlTag(ftXml, "Title");
                if (title == null) title = extractXmlTag(ftXml, "ows:Title");

                String abs = extractXmlTag(ftXml, "Abstract");
                if (abs == null) abs = extractXmlTag(ftXml, "ows:Abstract");

                // Default SRS
                String srs = extractXmlTag(ftXml, "DefaultSRS");
                if (srs == null) srs = extractXmlTag(ftXml, "ows:DefaultSRS");
                if (srs == null) srs = extractXmlTag(ftXml, "DefaultCRS");
                if (srs == null) srs = extractXmlTag(ftXml, "ows:DefaultCRS");

                // LatLongBoundingBox
                String bbox = null;
                int bs = ftXml.indexOf("<LatLongBoundingBox");
                if (bs >= 0) {
                    int be = ftXml.indexOf("/>", bs);
                    if (be < 0) be = ftXml.indexOf(">", bs);
                    if (be > bs) bbox = ftXml.substring(bs, be + (ftXml.indexOf("/>", bs) >= 0 ? 2 : 1));
                }
                if (bbox == null) {
                    bs = ftXml.indexOf("<ows:WGS84BoundingBox");
                    if (bs >= 0) {
                        int be = ftXml.indexOf("</ows:WGS84BoundingBox>", bs);
                        if (be >= 0) bbox = ftXml.substring(bs, be + 23);
                    }
                }

                Map<String, Object> ft = new LinkedHashMap<>();
                ft.put("name", name);
                ft.put("title", title != null ? title : name);
                ft.put("abstract", abs);
                ft.put("srs", srs);
                ft.put("bbox", bbox);
                types.add(ft);
            }

            if (!types.isEmpty() && serviceTitle != null) {
                types.get(0).put("_serviceTitle", serviceTitle);
            }
        } catch (Exception e) {
            logger.warn("WFS Capabilities 解析失败: {}", e.getMessage());
        }
        return types;
    }

    private String extractXmlTag(String xml, String tag) {
        int s = xml.indexOf("<" + tag + ">");
        if (s < 0) {
            s = xml.indexOf("<" + tag + " ");
            if (s >= 0) s = xml.indexOf(">", s) + 1;
        } else s += tag.length() + 2;
        if (s < 0) return null;
        int e = xml.indexOf("</" + tag + ">", s);
        if (e < 0) return null;
        String val = xml.substring(s, e).trim();
        return val.isEmpty() ? null : val;
    }
}
