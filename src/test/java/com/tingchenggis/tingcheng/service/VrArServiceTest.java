package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VrArServiceTest {

    @Mock
    private PavilionRepository pavilionRepository;

    @Mock
    private OverpassPoiService overpassPoiService;

    private VrArService vrArService;

    private Pavilion p1;

    @BeforeEach
    void setUp() {
        vrArService = new VrArService(pavilionRepository, overpassPoiService);

        p1 = new Pavilion("test", "醉翁亭", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        p1.setBuiltYear(1047);
        p1.setLocationDesc("琅琊山");
    }

    @Test
    void getVrExperience_found() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(overpassPoiService.queryNearbyPois(anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(Collections.emptyList());

        Map<String, Object> result = vrArService.getVrExperience(1L);

        assertEquals(1L, result.get("pavilionId"));
        assertEquals("醉翁亭", result.get("pavilionName"));
        assertEquals(true, result.get("hasVR"));
        assertNotNull(result.get("scenes"));
        assertNotNull(result.get("arMarkers"));
        assertNotNull(result.get("features"));
        List<?> scenes = (List<?>) result.get("scenes");
        assertEquals(2, scenes.size());
    }

    @Test
    void getVrExperience_notFound() {
        when(pavilionRepository.findById(99L)).thenReturn(Optional.empty());

        Map<String, Object> result = vrArService.getVrExperience(99L);

        assertNotNull(result.get("error"));
    }

    @Test
    void getArOverlayData() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(overpassPoiService.queryNearbyPois(anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(Collections.emptyList());

        Map<String, Object> result = vrArService.getArOverlayData(1L);

        assertEquals(1L, result.get("pavilionId"));
        assertNotNull(result.get("geoJson"));
        assertTrue(((String) result.get("geoJson")).startsWith("{\"type\":\"FeatureCollection\""));
    }

    @Test
    void get3dSceneData() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));

        Map<String, Object> result = vrArService.get3dSceneData(1L);

        assertEquals(1L, result.get("pavilionId"));
        assertNotNull(result.get("center"));
        assertNotNull(result.get("viewpoints"));
        assertNotNull(result.get("buildings"));
        List<?> viewpoints = (List<?>) result.get("viewpoints");
        assertEquals(3, viewpoints.size());
    }

    @Test
    void getVrExperience_withPoiMarkers() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));

        List<Map<String, Object>> pois = new ArrayList<>();
        Map<String, Object> poi = new LinkedHashMap<>();
        poi.put("name", "停车场"); poi.put("category", "parking");
        poi.put("latitude", 32.315); poi.put("longitude", 118.320);
        poi.put("distance", 0.3);
        pois.add(poi);
        when(overpassPoiService.queryNearbyPois(anyDouble(), anyDouble(), anyDouble()))
            .thenReturn(pois);

        Map<String, Object> result = vrArService.getVrExperience(1L);

        assertNotNull(result.get("arMarkers"));
        List<?> markers = (List<?>) result.get("arMarkers");
        assertTrue(markers.size() > 0);
    }
}
