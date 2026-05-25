package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.service.PavilionService;
import com.tingchenggis.tingcheng.service.PavilionStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PavilionGISServiceImplTest {

    @Mock
    private PavilionRepository pavilionRepository;
    @Mock
    private PavilionService pavilionService;

    private PavilionGISServiceImpl service;
    private final GeometryFactory gf = new GeometryFactory();

    private Pavilion p1, p2;

    @BeforeEach
    void setUp() {
        service = new PavilionGISServiceImpl(pavilionRepository, pavilionService);

        p1 = new Pavilion("p1", "亭一", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        p2 = new Pavilion("p2", "亭二", null, null, 118.4, 32.4, "MODERN");
        p2.setId(2L);
    }

    @Test
    void calculateDistance() {
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p1));
        when(pavilionService.getPavilionById(2L)).thenReturn(Optional.of(p2));

        double d = service.calculateDistance(1L, 2L);

        assertTrue(d > 10000 && d < 15000);
    }

    @Test
    void calculateDistance_missingPavilion() {
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.empty());

        assertEquals(-1, service.calculateDistance(1L, 2L));
    }

    @Test
    void findNearestPavilions() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));
        Point center = gf.createPoint(new Coordinate(118.35, 32.35));

        List<Pavilion> nearest = service.findNearestPavilions(center, 2);

        assertEquals(2, nearest.size());
    }

    @Test
    void findNearestPavilions_nullGeom() {
        List<Pavilion> nearest = service.findNearestPavilions(null, 5);
        assertTrue(nearest.isEmpty());
    }

    @Test
    void findPavilionsInBuffer() {
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p1));
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Pavilion> result = service.findPavilionsInBuffer(1L, 50000);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void findPavilionsInBuffer_centerNotFound() {
        when(pavilionService.getPavilionById(99L)).thenReturn(Optional.empty());

        List<Pavilion> result = service.findPavilionsInBuffer(99L, 1000);

        assertTrue(result.isEmpty());
    }

    @Test
    void generateHeatmapData() {
        p1.setVisitorRating(4.5);
        p2.setVisitorRating(3.0);
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Object[]> data = service.generateHeatmapData();

        assertEquals(2, data.size());
        assertEquals(118.3, (Double) data.get(0)[0]);
        assertEquals(4.5, (Double) data.get(0)[2]);
    }

    @Test
    void getPavilionDensity_nullRegion() {
        assertEquals(0, service.getPavilionDensity(null), 1e-10);
    }

    @Test
    void getPavilionDensity_withRegion() {
        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));
        var envelope = gf.toGeometry(new Envelope(118.0, 119.0, 32.0, 33.0));

        double density = service.getPavilionDensity(envelope);

        assertTrue(density > 0);
    }

    @Test
    void generateShortestPath() {
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p1));
        when(pavilionService.getPavilionById(2L)).thenReturn(Optional.of(p2));

        var geom = service.generateShortestPath(1L, 2L);

        assertNotNull(geom);
        assertEquals(2, geom.getCoordinates().length);
    }

    @Test
    void generateShortestPath_missingPavilion() {
        when(pavilionService.getPavilionById(99L)).thenReturn(Optional.empty());

        assertNull(service.generateShortestPath(99L, 1L));
    }

    @Test
    void delegateMethods() {
        when(pavilionService.createPavilion(any())).thenReturn(p1);
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p1));
        when(pavilionService.updatePavilion(anyLong(), any())).thenReturn(p1);
        when(pavilionService.findByPavilionType("HISTORICAL")).thenReturn(List.of(p1));
        when(pavilionService.getStats()).thenReturn(new PavilionStats());

        assertNotNull(service.createPavilion(p1));
        assertTrue(service.getPavilionById(1L).isPresent());
        assertNotNull(service.updatePavilion(1L, p1));
        assertEquals(1, service.findByPavilionType("HISTORICAL").size());
        assertNotNull(service.getStats());

        service.deletePavilion(1L);
        verify(pavilionService).deletePavilion(1L);
    }
}
