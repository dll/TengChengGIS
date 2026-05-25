package com.tingchenggis.tingcheng.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void haversineKm_zeroDistance() {
        double d = GeoUtils.haversineKm(118.3, 32.3, 118.3, 32.3);
        assertEquals(0.0, d, 1e-10);
    }

    @Test
    void haversineKm_knownDistance() {
        double d = GeoUtils.haversineKm(118.3, 32.3, 118.5, 32.4);
        assertTrue(d > 10 && d < 30);
    }

    @Test
    void haversineKm_symmetric() {
        double d1 = GeoUtils.haversineKm(118.0, 32.0, 119.0, 33.0);
        double d2 = GeoUtils.haversineKm(119.0, 33.0, 118.0, 32.0);
        assertEquals(d1, d2, 1e-10);
    }

    @Test
    void haversineKm_antipodal() {
        double d = GeoUtils.haversineKm(0, 0, 180, 0);
        assertTrue(d > 20000);
    }
}
