package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import com.tingchenggis.tingcheng.service.PavilionStats;
import com.tingchenggis.tingcheng.service.ThousandPavilionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PavilionServiceImplTest {

    @Mock
    private PavilionRepository pavilionRepository;
    @Mock
    private ThousandPavilionsService thousandPavilionsService;
    @Mock
    private PavilionCollectorService collectorService;

    private PavilionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PavilionServiceImpl(pavilionRepository, thousandPavilionsService, collectorService);
    }

    @Test
    void createPavilion() {
        Pavilion input = new Pavilion("test", "测试亭", "desc", null, 118.3, 32.3, "HISTORICAL");
        when(pavilionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Pavilion result = service.createPavilion(input);

        assertNotNull(result);
        assertEquals("测试亭", result.getChineseName());
        verify(pavilionRepository).save(input);
    }

    @Test
    void createPavilion_nullCoordinates() {
        Pavilion input = new Pavilion();
        input.setName("no-coord");
        when(pavilionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Pavilion result = service.createPavilion(input);

        assertEquals(0.0, result.getLatitude());
        assertEquals(0.0, result.getLongitude());
        assertEquals("POINT(0 0)", result.getGeomWkt());
    }

    @Test
    void getPavilionById_found() {
        Pavilion p = new Pavilion();
        p.setId(1L);
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Pavilion> result = service.getPavilionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getPavilionById_notFound() {
        when(pavilionRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Pavilion> result = service.getPavilionById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllPavilions_paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pavilion> page = new PageImpl<>(List.of(new Pavilion()));
        when(pavilionRepository.findAll(pageable)).thenReturn(page);

        Page<Pavilion> result = service.getAllPavilions(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllPavilions_unpaginated() {
        when(pavilionRepository.findAll()).thenReturn(List.of(new Pavilion(), new Pavilion()));

        List<Pavilion> result = service.getAllPavilions();

        assertEquals(2, result.size());
    }

    @Test
    void updatePavilion() {
        Pavilion existing = new Pavilion();
        existing.setId(1L);
        existing.setName("old");
        Pavilion updated = new Pavilion();
        updated.setName("new");
        updated.setChineseName("新名称");

        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pavilionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Pavilion result = service.updatePavilion(1L, updated);

        assertEquals("new", result.getName());
        assertEquals("新名称", result.getChineseName());
    }

    @Test
    void updatePavilion_notFound() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.updatePavilion(1L, new Pavilion()));
    }

    @Test
    void deletePavilion() {
        when(pavilionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(collectorService).deleteCollectorsByPavilionId(1L);
        doNothing().when(pavilionRepository).deleteById(1L);

        service.deletePavilion(1L);

        verify(pavilionRepository).deleteById(1L);
    }

    @Test
    void deletePavilion_notFound() {
        when(pavilionRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.deletePavilion(1L));
    }

    @Test
    void findByPavilionType() {
        when(pavilionRepository.findByPavilionType("HISTORICAL"))
            .thenReturn(List.of(new Pavilion()));

        List<Pavilion> result = service.findByPavilionType("HISTORICAL");

        assertEquals(1, result.size());
    }

    @Test
    void findByNameContaining() {
        when(pavilionRepository.findByNameContainingIgnoreCase("醉翁"))
            .thenReturn(List.of(new Pavilion()));

        List<Pavilion> result = service.findByNameContaining("醉翁");

        assertEquals(1, result.size());
    }

    @Test
    void findByBuiltYearBetween() {
        when(pavilionRepository.findByBuiltYearBetween(1000, 2000))
            .thenReturn(List.of(new Pavilion()));

        List<Pavilion> result = service.findByBuiltYearBetween(1000, 2000);

        assertEquals(1, result.size());
    }

    @Test
    void getStats_empty() {
        when(pavilionRepository.findAll()).thenReturn(List.of());

        PavilionStats stats = service.getStats();

        assertEquals(0, stats.getTotalPavilions());
        assertEquals(0.0, stats.getAverageRating());
    }

    @Test
    void getStats_withData() {
        Pavilion p1 = new Pavilion();
        p1.setPavilionType("HISTORICAL");
        p1.setVisitorRating(4.5);
        Pavilion p2 = new Pavilion();
        p2.setPavilionType("MODERN");
        p2.setVisitorRating(3.5);

        when(pavilionRepository.findAll()).thenReturn(List.of(p1, p2));

        PavilionStats stats = service.getStats();

        assertEquals(2, stats.getTotalPavilions());
        assertEquals(1, stats.getHistoricalPavilions());
        assertEquals(1, stats.getModernPavilions());
        assertEquals(0, stats.getCulturalPavilions());
        assertEquals(4.0, stats.getAverageRating());
    }

    @Test
    void recommendPavilions_historical() {
        when(pavilionRepository.findByPavilionType("HISTORICAL"))
            .thenReturn(List.of(new Pavilion()));

        List<Pavilion> result = service.recommendPavilions("u1", "historical");

        assertEquals(1, result.size());
    }

    @Test
    void recommendPavilions_modern() {
        when(pavilionRepository.findByPavilionType("MODERN"))
            .thenReturn(List.of(new Pavilion()));

        List<Pavilion> result = service.recommendPavilions("u1", "modern");

        assertEquals(1, result.size());
    }

    @Test
    void recommendPavilions_default() {
        when(pavilionRepository.findByVisitorRatingGreaterThanEqual(4.0))
            .thenReturn(List.of(new Pavilion()));

        List<Pavilion> result = service.recommendPavilions("u1", "unknown");

        assertEquals(1, result.size());
    }
}
