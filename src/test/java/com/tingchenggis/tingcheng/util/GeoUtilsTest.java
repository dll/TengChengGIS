package com.tingchenggis.tingcheng.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GeoUtils 工具类边界条件和异常场景测试
 *
 * 包含：
 * 1. Haversine距离计算边界测试
 * 2. WKT解析边界测试
 * 3. 方位角计算边界测试
 * 4. 坐标转换边界测试
 */
class GeoUtilsTest {

    // ==================== Haversine距离计算测试 ====================

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

    @Test
    void haversineKm_samePoint() {
        // 测试同一点
        double d = GeoUtils.haversineKm(118.3, 32.3, 118.3, 32.3);
        assertEquals(0, d, 1e-10);
    }

    @Test
    void haversineKm_equator1DegreeLongitude() {
        // 赤道上经度差1度约111km
        double d = GeoUtils.haversineKm(0, 0, 1, 0);
        assertEquals(111.0, d, 1.0);
    }

    @Test
    void haversineKm_equator1DegreeLatitude() {
        // 赤道上纬度差1度约111km
        double d = GeoUtils.haversineKm(0, 0, 0, 1);
        assertEquals(111.0, d, 1.0);
    }

    @Test
    void haversineKm_negativeCoords() {
        // 测试负数坐标
        double d = GeoUtils.haversineKm(-118.3, -32.3, -118.4, -32.4);
        assertTrue(d > 0);
    }

    @Test
    void haversineKm_allPositive() {
        double d = GeoUtils.haversineKm(10.0, 20.0, 20.0, 30.0);
        assertTrue(d > 0);
    }

    @Test
    void haversineKm_crossHemisphere() {
        // 测试跨半球
        double d = GeoUtils.haversineKm(-10.0, -20.0, 10.0, 20.0);
        assertTrue(d > 0);
    }

    @Test
    void haversineKm_poleToEquator() {
        // 从北极到赤道的距离
        double d = GeoUtils.haversineKm(0, 90, 0, 0);
        assertEquals(10000.0, d, 100.0); // 约10000km
    }

    @Test
    void haversineKmLatLon_orderSwapped() {
        // 测试参数顺序
        double d1 = GeoUtils.haversineKm(118.0, 32.0, 119.0, 33.0);
        double d2 = GeoUtils.haversineKmLatLon(32.0, 118.0, 33.0, 119.0);
        assertEquals(d1, d2, 1e-10);
    }

    // ==================== round2 测试 ====================

    @Test
    void round2_oneDecimal() {
        // round2保留两位小数，1.234四舍五入为1.23
        assertEquals(1.23, GeoUtils.round2(1.234), 1e-10);
    }

    @Test
    void round2_twoDecimals() {
        assertEquals(1.23, GeoUtils.round2(1.234), 1e-10);
    }

    @Test
    void round2_threeDecimals() {
        assertEquals(1.24, GeoUtils.round2(1.235), 1e-10);
    }

    @Test
    void round2_zero() {
        assertEquals(0.0, GeoUtils.round2(0.0), 1e-10);
    }

    @Test
    void round2_negative() {
        assertEquals(-1.23, GeoUtils.round2(-1.234), 1e-10);
    }

    // ==================== WKT解析测试 ====================

    @Test
    void parseWktBbox_null() {
        assertNull(GeoUtils.parseWktBbox(null));
    }

    @Test
    void parseWktBbox_empty() {
        assertNull(GeoUtils.parseWktBbox(""));
    }

    @Test
    void parseWktBbox_whitespaceOnly() {
        assertNull(GeoUtils.parseWktBbox("   "));
    }

    @Test
    void parseWktBbox_singlePoint() {
        // 仅一个点，应该返回null（需要≥2个点）
        assertNull(GeoUtils.parseWktBbox("POINT(118.3 32.3)"));
    }

    @Test
    void parseWktBbox_twoPoints() {
        double[] bbox = GeoUtils.parseWktBbox("POINT(118.3 32.3) POINT(118.5 32.5)");
        assertNotNull(bbox);
        assertEquals(4, bbox.length);
        assertEquals(118.3, bbox[0], 1e-10);
        assertEquals(118.5, bbox[1], 1e-10);
        assertEquals(32.3, bbox[2], 1e-10);
        assertEquals(32.5, bbox[3], 1e-10);
    }

    @Test
    void parseWktBbox_multiplePoints() {
        String wkt = "POINT(118.0 32.0) POINT(118.5 32.5) POINT(119.0 33.0)";
        double[] bbox = GeoUtils.parseWktBbox(wkt);
        assertNotNull(bbox);
        assertEquals(118.0, bbox[0], 1e-10); // minLng
        assertEquals(119.0, bbox[1], 1e-10); // maxLng
        assertEquals(32.0, bbox[2], 1e-10); // minLat
        assertEquals(33.0, bbox[3], 1e-10); // maxLat
    }

    @Test
    void parseWktBbox_negativeCoords() {
        double[] bbox = GeoUtils.parseWktBbox("-118.3 -32.3 -118.5 -32.5");
        assertNotNull(bbox);
        assertEquals(-118.5, bbox[0], 1e-10);
        assertEquals(-118.3, bbox[1], 1e-10);
    }

    @Test
    void parseWktBbox_decimalCoords() {
        double[] bbox = GeoUtils.parseWktBbox("118.123456 32.654321 118.234567 32.765432");
        assertNotNull(bbox);
        assertEquals(118.123456, bbox[0], 1e-6);
    }

    @Test
    void parseWktBbox_invalidFormat() {
        // 无效格式应该返回null
        assertNull(GeoUtils.parseWktBbox("INVALID"));
    }

    @Test
    void parseWktBbox_onlyCoordinates() {
        // 仅坐标对也应该能解析
        double[] bbox = GeoUtils.parseWktBbox("118.3 32.3 118.5 32.5");
        assertNotNull(bbox);
        assertEquals(118.3, bbox[0], 1e-10);
        assertEquals(118.5, bbox[1], 1e-10);
    }

    @Test
    void parseWktBbox_minimalValid() {
        // 刚好两个点
        double[] bbox = GeoUtils.parseWktBbox("1 2 3 4");
        assertNotNull(bbox);
        assertEquals(1.0, bbox[0], 1e-10);
        assertEquals(3.0, bbox[1], 1e-10);
        assertEquals(2.0, bbox[2], 1e-10);
        assertEquals(4.0, bbox[3], 1e-10);
    }

    // ==================== 方位角计算测试 ====================

    @Test
    void computeBearing_north() {
        assertEquals("北", GeoUtils.computeBearing(32.3, 118.3, 33.3, 118.3));
    }

    @Test
    void computeBearing_east() {
        assertEquals("东", GeoUtils.computeBearing(32.3, 118.3, 32.3, 119.3));
    }

    @Test
    void computeBearing_south() {
        assertEquals("南", GeoUtils.computeBearing(32.3, 118.3, 31.3, 118.3));
    }

    @Test
    void computeBearing_west() {
        assertEquals("西", GeoUtils.computeBearing(32.3, 118.3, 32.3, 117.3));
    }

    @Test
    void computeBearing_northeast() {
        assertEquals("东北", GeoUtils.computeBearing(32.3, 118.3, 33.3, 119.3));
    }

    @Test
    void computeBearing_southeast() {
        assertEquals("东南", GeoUtils.computeBearing(32.3, 118.3, 31.3, 119.3));
    }

    @Test
    void computeBearing_southwest() {
        assertEquals("西南", GeoUtils.computeBearing(32.3, 118.3, 31.3, 117.3));
    }

    @Test
    void computeBearing_northwest() {
        assertEquals("西北", GeoUtils.computeBearing(32.3, 118.3, 33.3, 117.3));
    }

    @Test
    void computeBearing_samePoint() {
        // 相同点应该返回北
        String bearing = GeoUtils.computeBearing(32.3, 118.3, 32.3, 118.3);
        assertNotNull(bearing);
    }

    @Test
    void computeBearing_boundaryNorth() {
        // 边界角度测试
        String bearing = GeoUtils.computeBearing(0, 0, 1, 0);
        assertEquals("北", bearing);
    }

    @Test
    void computeBearing_boundaryEast() {
        String bearing = GeoUtils.computeBearing(0, 0, 0, 1);
        assertEquals("东", bearing);
    }

    @Test
    void computeBearing_boundarySouth() {
        String bearing = GeoUtils.computeBearing(1, 0, 0, 0);
        assertEquals("南", bearing);
    }

    @Test
    void computeBearing_boundaryWest() {
        String bearing = GeoUtils.computeBearing(0, 1, 0, 0);
        assertEquals("西", bearing);
    }
}
