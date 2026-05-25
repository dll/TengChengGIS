package com.tingchenggis.tingcheng.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SnapPointTest {

    @Test
    void compute_nullPath() {
        SnapPoint sp = SnapPoint.compute(118.3, 32.3, null);
        assertEquals(118.3, sp.lng(), 1e-10);
        assertEquals(32.3, sp.lat(), 1e-10);
        assertEquals(0.0, sp.offsetM(), 1e-10);
    }

    @Test
    void compute_emptyPath() {
        SnapPoint sp = SnapPoint.compute(118.3, 32.3, List.of());
        assertEquals(118.3, sp.lng(), 1e-10);
        assertEquals(32.3, sp.lat(), 1e-10);
        assertEquals(0.0, sp.offsetM(), 1e-10);
    }

    @Test
    void compute_onSegment() {
        List<double[]> path = List.of(
            new double[]{118.0, 32.0},
            new double[]{119.0, 33.0}
        );
        SnapPoint sp = SnapPoint.compute(118.5, 32.5, path);
        assertTrue(sp.offsetM() < 1);
    }

    @Test
    void compute_farFromSegment() {
        List<double[]> path = List.of(
            new double[]{118.0, 32.0},
            new double[]{118.0, 32.1}
        );
        SnapPoint sp = SnapPoint.compute(119.0, 33.0, path);
        assertTrue(sp.offsetM() > 10000);
    }

    @Test
    void record_construction() {
        SnapPoint sp = new SnapPoint(118.3, 32.3, 12.5);
        assertEquals(118.3, sp.lng());
        assertEquals(32.3, sp.lat());
        assertEquals(12.5, sp.offsetM());
    }
}
