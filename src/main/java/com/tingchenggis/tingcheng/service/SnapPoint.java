package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.util.GeoUtils;

import java.util.List;

public record SnapPoint(double lng, double lat, double offsetM) {
    public static SnapPoint compute(double lng, double lat, List<double[]> path) {
        if (path == null || path.isEmpty()) return new SnapPoint(lng, lat, 0.0);
        double bestSqr = Double.MAX_VALUE;
        double bestX = path.get(0)[0], bestY = path.get(0)[1];
        for (int i = 0; i < path.size() - 1; i++) {
            double[] a = path.get(i), b = path.get(i + 1);
            double[] p = nearestOnSegment(lng, lat, a[0], a[1], b[0], b[1]);
            double dx = p[0] - lng, dy = p[1] - lat;
            double sqr = dx * dx + dy * dy;
            if (sqr < bestSqr) { bestSqr = sqr; bestX = p[0]; bestY = p[1]; }
        }
        double offsetM = GeoUtils.haversineKm(lng, lat, bestX, bestY) * 1000.0;
        return new SnapPoint(bestX, bestY, Math.round(offsetM * 100.0) / 100.0);
    }

    private static double[] nearestOnSegment(double px, double py,
                                              double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double len2 = dx * dx + dy * dy;
        if (len2 < 1e-12) return new double[]{ax, ay};
        double t = ((px - ax) * dx + (py - ay) * dy) / len2;
        t = Math.max(0, Math.min(1, t));
        return new double[]{ax + t * dx, ay + t * dy};
    }
}
