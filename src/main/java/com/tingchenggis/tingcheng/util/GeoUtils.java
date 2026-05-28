package com.tingchenggis.tingcheng.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 空间计算工具类（项目内 Haversine 与 WKT 解析的唯一实现）
 *
 * 函数命名约定：参数顺序统一为 (lon, lat) 经度在前、纬度在后，
 * 与 OSM/GeoJSON/OSRM 一致；调用时请勿与 (lat, lon) 系列混用。
 */
public final class GeoUtils {

    public static final double EARTH_RADIUS_KM = 6371.0;

    private static final Pattern WKT_COORD_PAIRS = Pattern.compile(
        "([\\d.\\-]+)\\s+([\\d.\\-]+)", Pattern.CASE_INSENSITIVE);

    private GeoUtils() {}

    /**
     * 球面 Haversine 距离（公里）
     * 参数顺序：经度1, 纬度1, 经度2, 纬度2
     */
    public static double haversineKm(double lon1, double lat1, double lon2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** 兼容老调用：(lat1, lon1, lat2, lon2) 顺序，内部委托给 haversineKm */
    public static double haversineKmLatLon(double lat1, double lon1, double lat2, double lon2) {
        return haversineKm(lon1, lat1, lon2, lat2);
    }

    /** 把任意 WKT 解析为最小外包矩形 [minLng, maxLng, minLat, maxLat]，至少需要 2 个点 */
    public static double[] parseWktBbox(String wktText) {
        if (wktText == null) return null;
        Matcher m = WKT_COORD_PAIRS.matcher(wktText);
        double minLng = Double.MAX_VALUE, maxLng = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        int count = 0;
        while (m.find()) {
            double lng = Double.parseDouble(m.group(1));
            double lat = Double.parseDouble(m.group(2));
            if (lng < minLng) minLng = lng;
            if (lng > maxLng) maxLng = lng;
            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
            count++;
        }
        return count >= 2 ? new double[]{minLng, maxLng, minLat, maxLat} : null;
    }
}
