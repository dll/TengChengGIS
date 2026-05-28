package com.tingchenggis.tingcheng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NavigationServiceTest {

    @Mock
    private RoutingClient routingClient;

    private NavigationService navigationService;

    @BeforeEach
    void setUp() {
        navigationService = new NavigationService(routingClient);
    }

    @Test
    void getTurnByTurnNavigation_withSteps() {
        OsrmRoute osrm = new OsrmRoute();
        osrm.setDistance(3.5);
        osrm.setDuration(2520);
        osrm.setCoordinates(List.of(new double[]{118.3, 32.3}, new double[]{118.31, 32.31}));
        osrm.setGeometryWkt("LINESTRING(118.3 32.3, 118.31 32.31)");

        NavigationStep step1 = new NavigationStep();
        step1.setStepNumber(1);
        step1.setInstruction("出发");
        step1.setManeuverType("depart");
        step1.setDistanceKm(0.0);
        step1.setDurationSeconds(0);
        step1.setLatitude(32.3);
        step1.setLongitude(118.3);

        NavigationStep step2 = new NavigationStep();
        step2.setStepNumber(2);
        step2.setInstruction("左转，进入琅琊路");
        step2.setManeuverType("turn");
        step2.setManeuverModifier("left");
        step2.setStreetName("琅琊路");
        step2.setDistanceKm(0.5);
        step2.setDurationSeconds(360);
        step2.setLatitude(32.305);
        step2.setLongitude(118.305);

        NavigationStep step3 = new NavigationStep();
        step3.setStepNumber(3);
        step3.setInstruction("到达目的地");
        step3.setManeuverType("arrive");
        step3.setDistanceKm(0.0);
        step3.setDurationSeconds(0);
        step3.setLatitude(32.31);
        step3.setLongitude(118.31);

        osrm.setSteps(List.of(step1, step2, step3));

        when(routingClient.getRouteWithSteps(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString()))
            .thenReturn(osrm);

        Map<String, Object> result = navigationService.getTurnByTurnNavigation(118.3, 32.3, 118.31, 32.31, "WALKING");

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(3.5, (double) result.get("totalDistanceKm"), 1e-10);
        assertEquals(3, result.get("totalSteps"));
        assertNotNull(result.get("steps"));
        List<?> steps = (List<?>) result.get("steps");
        assertEquals(3, steps.size());
        assertNotNull(result.get("estimatedArrival"));
        assertNotNull(result.get("geometry"));
    }

    @Test
    void getTurnByTurnNavigation_fallbackWhenOsrmFails() {
        when(routingClient.getRouteWithSteps(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString()))
            .thenReturn(null);

        Map<String, Object> result = navigationService.getTurnByTurnNavigation(118.3, 32.3, 118.31, 32.31, "WALKING");

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("isFallback"));
        assertEquals(2, result.get("totalSteps"));
        assertNotNull(result.get("steps"));
    }

    @Test
    void getTurnByTurnNavigation_withDrivingMode() {
        OsrmRoute osrm = new OsrmRoute();
        osrm.setDistance(10.0);
        osrm.setDuration(600);
        osrm.setCoordinates(List.of(new double[]{118.3, 32.3}, new double[]{118.5, 32.5}));
        osrm.setSteps(List.of());

        when(routingClient.getRouteWithSteps(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq("driving")))
            .thenReturn(osrm);

        Map<String, Object> result = navigationService.getTurnByTurnNavigation(118.3, 32.3, 118.5, 32.5, "DRIVING");

        assertNotNull(result);
        assertEquals("DRIVING", result.get("mode"));
        assertEquals(10.0, (double) result.get("totalDistanceKm"), 1e-10);
    }

    @Test
    void getTurnByTurnNavigation_fallbackWithMode() {
        when(routingClient.getRouteWithSteps(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString()))
            .thenReturn(null);

        Map<String, Object> result = navigationService.getTurnByTurnNavigation(118.3, 32.3, 118.5, 32.5, "DRIVING");

        assertNotNull(result);
        assertEquals("DRIVING", result.get("mode"));
        assertTrue((double) result.get("totalDistanceKm") > 0);
    }
}
