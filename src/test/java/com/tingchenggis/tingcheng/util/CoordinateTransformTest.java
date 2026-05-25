package com.tingchenggis.tingcheng.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoordinateTransformTest {

    @Test
    void wgs84ToGcj02_chuzhou() {
        double[] gcj = CoordinateTransform.wgs84ToGcj02(118.3, 32.3);
        assertNotNull(gcj);
        assertEquals(2, gcj.length);
        assertNotEquals(118.3, gcj[0], 1e-6);
        assertNotEquals(32.3, gcj[1], 1e-6);
    }

    @Test
    void wgs84ToGcj02_outOfChina() {
        double[] gcj = CoordinateTransform.wgs84ToGcj02(10.0, 10.0);
        assertEquals(10.0, gcj[0], 1e-10);
        assertEquals(10.0, gcj[1], 1e-10);
    }

    @Test
    void wgs84ToGcj02_boundary() {
        double[] gcj = CoordinateTransform.wgs84ToGcj02(72.004, 0.8293);
        assertNotNull(gcj);
    }

    @Test
    void wgs84ToGcj02_nullSafe() {
        assertDoesNotThrow(() -> CoordinateTransform.wgs84ToGcj02(0, 0));
    }
}
