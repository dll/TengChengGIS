package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThousandPavilionsServiceImplTest {

    @Mock
    private PavilionRepository pavilionRepository;

    private ThousandPavilionsServiceImpl service;

    private Pavilion p1, p2, p3;

    @BeforeEach
    void setUp() {
        service = new ThousandPavilionsServiceImpl(pavilionRepository);

        p1 = new Pavilion("p1", "亭一", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        p1.setIsOpenToPublic(true);
        p2 = new Pavilion("p2", "亭二", null, null, 118.4, 32.4, "MODERN");
        p2.setId(2L);
        p2.setIsOpenToPublic(true);
        p3 = new Pavilion("p3", "亭三", null, null, 118.5, 32.5, "HISTORICAL");
        p3.setId(3L);
        p3.setIsOpenToPublic(true);
    }

    @Test
    void getAllPavilionsBasicInfo() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));
        assertEquals(2, service.getAllPavilionsBasicInfo().size());
    }

    @Test
    void calculateDistance() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(pavilionRepository.findById(2L)).thenReturn(Optional.of(p2));

        double d = service.calculateDistance(1L, 2L);

        assertTrue(d > 10 && d < 15);
    }

    @Test
    void calculateDistance_missingCoords() {
        Pavilion noCoord = new Pavilion();
        noCoord.setId(99L);
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(pavilionRepository.findById(99L)).thenReturn(Optional.of(noCoord));

        assertEquals(0.0, service.calculateDistance(1L, 99L));
    }

    @Test
    void getOptimalTraversalRoute_single() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1));
        List<Long> route = service.getOptimalTraversalRoute();
        assertEquals(List.of(1L), route);
    }

    @Test
    void getOptimalTraversalRoute_two() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));
        List<Long> route = service.getOptimalTraversalRoute();
        assertEquals(2, route.size());
        assertTrue(route.contains(1L));
        assertTrue(route.contains(2L));
    }

    @Test
    void getOptimalTraversalRoute_three() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2, p3));
        List<Long> route = service.getOptimalTraversalRoute();
        assertEquals(3, route.size());
    }

    @Test
    void getOptimalTraversalRoute_bruteForceLimit() {
        List<Pavilion> many = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Pavilion p = new Pavilion("p" + i, "t" + i, null, null, 118.0 + i * 0.1, 32.0 + i * 0.1, "HISTORICAL");
            p.setId((long) (i + 1));
            many.add(p);
        }
        when(pavilionRepository.findAll()).thenReturn(many);
        List<Long> route = service.getOptimalTraversalRoute();
        assertEquals(8, route.size());
    }

    @Test
    void getOptimalTraversalRoute_twoOpt() {
        List<Pavilion> many = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Pavilion p = new Pavilion("p" + i, "t" + i, null, null, 118.0 + i * 0.05, 32.0 + i * 0.05, "HISTORICAL");
            p.setId((long) (i + 1));
            many.add(p);
        }
        when(pavilionRepository.findAll()).thenReturn(many);
        List<Long> route = service.getOptimalTraversalRoute();
        assertEquals(15, route.size());
    }

    @Test
    void getRecommendedTourRoute_noFilter() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        List<Pavilion> result = service.getRecommendedTourRoute(null, 180);

        assertFalse(result.isEmpty());
    }

    @Test
    void getRecommendedTourRoute_filterType() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        List<Pavilion> result = service.getRecommendedTourRoute("HISTORICAL", 180);

        assertEquals(2, result.size());
    }

    @Test
    void estimateTravelTime() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(pavilionRepository.findById(2L)).thenReturn(Optional.of(p2));

        double time = service.estimateTravelTime(1L, 2L);

        assertTrue(time > 150 && time < 250);
    }

    @Test
    void getSmartTourPlan() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2, p3));
        when(pavilionRepository.findById(anyLong())).thenAnswer(i -> {
            long id = i.getArgument(0);
            return Optional.of(id == 1L ? p1 : id == 2L ? p2 : p3);
        });

        Map<String, Object> plan = service.getSmartTourPlan(null, null, 240, null);

        assertNotNull(plan);
        assertTrue((int) plan.get("totalPavilions") > 0);
        assertNotNull(plan.get("route"));
    }

    @Test
    void getWeatherInfo() {
        Map<String, Object> weather = service.getWeatherInfo();
        assertNotNull(weather);
        assertNotNull(weather.get("temperature"));
        assertNotNull(weather.get("condition"));
    }

    @Test
    void getVRExperience_found() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));

        Map<String, Object> vr = service.getVRExperience(1L);

        assertEquals(1L, vr.get("pavilionId"));
        assertEquals(true, vr.get("hasVR"));
    }

    @Test
    void getVRExperience_notFound() {
        when(pavilionRepository.findById(99L)).thenReturn(Optional.empty());

        Map<String, Object> vr = service.getVRExperience(99L);

        assertNotNull(vr.get("error"));
    }

    @Test
    void getAccessibilityMatrix() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(pavilionRepository.findById(2L)).thenReturn(Optional.of(p2));
        double[][] matrix = service.getAccessibilityMatrix();
        assertEquals(2, matrix.length);
        assertEquals(2, matrix[0].length);
        assertEquals(0.0, matrix[0][0]);
        assertTrue(matrix[0][1] > 0);
        assertEquals(matrix[0][1], matrix[1][0], 1e-10);
    }

    @Test
    void generateShareableRoute() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(pavilionRepository.findById(2L)).thenReturn(Optional.of(p2));

        Map<String, Object> share = service.generateShareableRoute(List.of(1L, 2L), "test");

        assertEquals("test", share.get("routeName"));
        assertNotNull(share.get("shareUrl"));
    }
}
