package com.tingchenggis.tingcheng.util;

/**
 * WGS-84 ↔ GCJ-02 坐标转换
 * 高德、腾讯、QQ 等中国地图使用 GCJ-02（火星坐标系），
 * OSM、Google Earth 使用 WGS-84，相互之间存在 50~500 米偏移。
 */
public final class CoordinateTransform {

    private static final double PI = Math.PI;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;

    private CoordinateTransform() {}

    public static double[] wgs84ToGcj02(double lng, double lat) {
        if (outOfChina(lng, lat)) return new double[]{lng, lat};
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);
        return new double[]{lng + dLng, lat + dLat};
    }

    private static boolean outOfChina(double lng, double lat) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    private static double transformLat(double x, double y) {
        double r = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        r += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        r += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        r += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return r;
    }

    private static double transformLng(double x, double y) {
        double r = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        r += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        r += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        r += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return r;
    }

    /**
     * GCJ-02 → WGS-84 逆向转换（迭代法）
     * 通过不断逼近计算反偏移
     */
    public static double[] gcj02ToWgs84(double lng, double lat) {
        if (outOfChina(lng, lat)) return new double[]{lng, lat};
        double[] wgs = new double[]{lng, lat};
        double[] gcj;
        for (int i = 0; i < 5; i++) {
            gcj = wgs84ToGcj02(wgs[0], wgs[1]);
            wgs[0] += lng - gcj[0];
            wgs[1] += lat - gcj[1];
        }
        return wgs;
    }
}
