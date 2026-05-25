package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.entity.TransportRoute;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.repository.TransportRouteRepository;
import com.tingchenggis.tingcheng.service.RoutingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportRouteServiceImplTest {

    @Mock
    private TransportRouteRepository transportRouteRepository;
    @Mock
    private PavilionRepository pavilionRepository;
    @Mock
    private RoutingClient routingClient;

    private TransportRouteServiceImpl service;
    private TransportRoute route;

    @BeforeEach
    void setUp() {
        service = new TransportRouteServiceImpl(transportRouteRepository, pavilionRepository, routingClient);
        route = new TransportRoute();
        route.setId(1L);
        route.setRouteName("test-route");
        route.setFromPavilionId(1L);
        route.setToPavilionId(2L);
        route.setDistanceKm(10.0);
        route.setTravelTimeMinutes(15);
        route.setTransportMode("WALKING");
    }

    @Test
    void getAllRoutes() {
        when(transportRouteRepository.findAll()).thenReturn(List.of(route));
        assertEquals(1, service.getAllRoutes().size());
    }

    @Test
    void getRouteById_found() {
        when(transportRouteRepository.findById(1L)).thenReturn(Optional.of(route));
        assertNotNull(service.getRouteById(1L));
    }

    @Test
    void getRouteById_notFound() {
        when(transportRouteRepository.findById(99L)).thenReturn(Optional.empty());
        assertNull(service.getRouteById(99L));
    }

    @Test
    void getRoutesFromPavilion() {
        when(transportRouteRepository.findByFromPavilionId(1L)).thenReturn(List.of(route));
        assertEquals(1, service.getRoutesFromPavilion(1L).size());
    }

    @Test
    void getRouteBetweenPavilions_found() {
        when(transportRouteRepository.findRouteBetweenPavilions(1L, 2L)).thenReturn(List.of(route));
        assertNotNull(service.getRouteBetweenPavilions(1L, 2L));
    }

    @Test
    void getRouteBetweenPavilions_notFound() {
        when(transportRouteRepository.findRouteBetweenPavilions(1L, 99L)).thenReturn(List.of());
        assertNull(service.getRouteBetweenPavilions(1L, 99L));
    }

    @Test
    void getRoutesByType() {
        when(transportRouteRepository.findByRouteType("道路")).thenReturn(List.of(route));
        assertEquals(1, service.getRoutesByType("道路").size());
    }

    @Test
    void createRoute() {
        when(transportRouteRepository.save(any())).thenReturn(route);
        assertNotNull(service.createRoute(route));
    }

    @Test
    void deleteRoute() {
        doNothing().when(transportRouteRepository).deleteById(1L);
        service.deleteRoute(1L);
        verify(transportRouteRepository).deleteById(1L);
    }

    @Test
    void getAvailableTransportModes() {
        List<String> modes = service.getAvailableTransportModes();
        assertTrue(modes.contains("WALKING"));
        assertTrue(modes.contains("TAXI"));
        assertTrue(modes.contains("BUS"));
        assertEquals(5, modes.size());
    }

    @Test
    void buildRoadNetwork_notEnoughPavilions() {
        when(pavilionRepository.findAll()).thenReturn(List.of(new Pavilion()));
        var result = service.buildRoadNetwork();
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void getTspRoute_notEnough() {
        var result = service.getTspRoute(List.of(1L), "WALKING", "DISTANCE");
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void updateRoute_notFound() {
        when(transportRouteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateRoute(99L, new TransportRoute()));
    }

    @Test
    void getRouteStats() {
        when(transportRouteRepository.findAll()).thenReturn(List.of(route));
        when(transportRouteRepository.findByIsScenicRouteTrue()).thenReturn(List.of());
        when(transportRouteRepository.findByIsAccessibleTrue()).thenReturn(List.of(route));
        when(transportRouteRepository.findByTransportMode(anyString())).thenReturn(List.of());

        var stats = service.getRouteStats();
        assertEquals(1, stats.get("totalRoutes"));
    }

    @Test
    void buildMultiModalNetwork_notEnough() {
        when(pavilionRepository.findAll()).thenReturn(List.of(new Pavilion()));
        var result = service.buildMultiModalNetwork();
        assertFalse((Boolean) result.get("success"));
    }
}
