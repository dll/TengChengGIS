package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.ScenicArea;
import com.tingchenggis.tingcheng.repository.ScenicAreaRepository;
import com.tingchenggis.tingcheng.service.ScenicAreaCollectorService;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScenicAreaServiceImplTest {

    @Mock
    private ScenicAreaRepository scenicAreaRepository;
    @Mock
    private ScenicAreaCollectorService collectorService;

    private ScenicAreaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ScenicAreaServiceImpl(scenicAreaRepository, collectorService);
    }

    @Test
    void createScenicArea() {
        ScenicArea input = new ScenicArea();
        input.setName("Langya Mountain");
        when(scenicAreaRepository.save(any())).thenAnswer(i -> {
            ScenicArea saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ScenicArea result = service.createScenicArea(input);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(scenicAreaRepository).save(input);
    }

    @Test
    void getScenicAreaById_found() {
        ScenicArea sa = new ScenicArea();
        sa.setId(1L);
        when(scenicAreaRepository.findById(1L)).thenReturn(Optional.of(sa));

        Optional<ScenicArea> result = service.getScenicAreaById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getScenicAreaById_notFound() {
        when(scenicAreaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ScenicArea> result = service.getScenicAreaById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllScenicAreas_paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ScenicArea> page = new PageImpl<>(List.of(new ScenicArea()));
        when(scenicAreaRepository.findAll(pageable)).thenReturn(page);

        Page<ScenicArea> result = service.getAllScenicAreas(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllScenicAreas_unpaginated() {
        when(scenicAreaRepository.findAll()).thenReturn(List.of(new ScenicArea(), new ScenicArea()));

        List<ScenicArea> result = service.getAllScenicAreas();

        assertEquals(2, result.size());
    }

    @Test
    void updateScenicArea() {
        ScenicArea existing = new ScenicArea();
        existing.setId(1L);
        existing.setName("old");
        ScenicArea updated = new ScenicArea();
        updated.setName("new");
        updated.setChineseName("新名称");
        updated.setDescription("Updated description");
        updated.setAreaType("NATURE");
        updated.setAreaSize(100.0);
        updated.setGeomWkt("POINT(118 32)");
        updated.setBoundaryWkt("POLYGON(...)");
        updated.setLongitude(118.3);
        updated.setLatitude(32.1);
        updated.setAddress("Chuzhou");
        updated.setOpeningHours("08:00-17:00");
        updated.setTicketPrice(50.0);
        updated.setVisitorRating(4.5);
        updated.setIsOpenToPublic(true);
        updated.setNotes("Popular site");

        when(scenicAreaRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(scenicAreaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ScenicArea result = service.updateScenicArea(1L, updated);

        assertEquals("new", result.getName());
        assertEquals("新名称", result.getChineseName());
        assertEquals("Updated description", result.getDescription());
        assertEquals("NATURE", result.getAreaType());
        assertEquals(100.0, result.getAreaSize());
        assertEquals("POINT(118 32)", result.getGeomWkt());
        assertEquals("POLYGON(...)", result.getBoundaryWkt());
        assertEquals(118.3, result.getLongitude());
        assertEquals(32.1, result.getLatitude());
        assertEquals("Chuzhou", result.getAddress());
        assertEquals("08:00-17:00", result.getOpeningHours());
        assertEquals(50.0, result.getTicketPrice());
        assertEquals(4.5, result.getVisitorRating());
        assertTrue(result.getIsOpenToPublic());
        assertEquals("Popular site", result.getNotes());
    }

    @Test
    void updateScenicArea_notFound() {
        when(scenicAreaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.updateScenicArea(1L, new ScenicArea()));
    }

    @Test
    void deleteScenicArea() {
        when(scenicAreaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(collectorService).deleteCollectorsByScenicAreaId(1L);
        doNothing().when(scenicAreaRepository).deleteById(1L);

        service.deleteScenicArea(1L);

        verify(collectorService).deleteCollectorsByScenicAreaId(1L);
        verify(scenicAreaRepository).deleteById(1L);
    }

    @Test
    void deleteScenicArea_notFound() {
        when(scenicAreaRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.deleteScenicArea(1L));
    }

    @Test
    void findByAreaType() {
        ScenicArea sa = new ScenicArea();
        sa.setAreaType("NATURE");
        when(scenicAreaRepository.findByAreaType("NATURE")).thenReturn(List.of(sa));

        List<ScenicArea> result = service.findByAreaType("NATURE");

        assertEquals(1, result.size());
        assertEquals("NATURE", result.get(0).getAreaType());
    }

    @Test
    void findByNameContaining() {
        ScenicArea sa = new ScenicArea();
        sa.setName("Langya Mountain");
        when(scenicAreaRepository.findByNameContainingIgnoreCase("langya"))
            .thenReturn(List.of(sa));

        List<ScenicArea> result = service.findByNameContaining("langya");

        assertEquals(1, result.size());
        assertEquals("Langya Mountain", result.get(0).getName());
    }

    @Test
    void findByNameContaining_noMatch() {
        when(scenicAreaRepository.findByNameContainingIgnoreCase("nonexistent"))
            .thenReturn(List.of());

        List<ScenicArea> result = service.findByNameContaining("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByGeographicRange() {
        ScenicArea sa = new ScenicArea();
        sa.setName("Langya Mountain");
        String wkt = "POLYGON((117 31, 119 31, 119 33, 117 33, 117 31))";
        when(scenicAreaRepository.findByGeographicRange(117.0, 119.0, 31.0, 33.0)).thenReturn(List.of(sa));

        List<ScenicArea> result = service.findByGeographicRange(wkt);

        assertEquals(1, result.size());
        verify(scenicAreaRepository).findByGeographicRange(117.0, 119.0, 31.0, 33.0);
    }

    @Test
    void getStats_empty() {
        when(scenicAreaRepository.findAll()).thenReturn(List.of());

        Map<String, Object> stats = service.getStats();

        assertEquals(0, stats.get("total"));
        assertEquals(0.0, stats.get("averageRating"));
        assertEquals(0L, stats.get("openToPublic"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getStats_withData() {
        ScenicArea p1 = new ScenicArea();
        p1.setAreaType("NATURE");
        p1.setVisitorRating(4.5);
        p1.setIsOpenToPublic(true);

        ScenicArea p2 = new ScenicArea();
        p2.setAreaType("NATURE");
        p2.setVisitorRating(3.5);
        p2.setIsOpenToPublic(true);

        ScenicArea p3 = new ScenicArea();
        p3.setAreaType("CULTURAL");
        p3.setVisitorRating(5.0);
        p3.setIsOpenToPublic(false);

        when(scenicAreaRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        Map<String, Object> stats = service.getStats();

        assertEquals(3, stats.get("total"));
        assertEquals(4.3, stats.get("averageRating"));
        assertEquals(2L, stats.get("openToPublic"));

        Map<String, Long> byType = (Map<String, Long>) stats.get("byType");
        assertEquals(2L, byType.get("NATURE"));
        assertEquals(1L, byType.get("CULTURAL"));
    }
}
