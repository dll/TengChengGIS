package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.AdminDivision;
import com.tingchenggis.tingcheng.repository.AdminDivisionRepository;
import com.tingchenggis.tingcheng.service.AdminDivisionCollectorService;
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
class AdminDivisionServiceImplTest {

    @Mock
    private AdminDivisionRepository adminDivisionRepository;
    @Mock
    private AdminDivisionCollectorService collectorService;

    private AdminDivisionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminDivisionServiceImpl(adminDivisionRepository, collectorService);
    }

    @Test
    void createAdminDivision() {
        AdminDivision input = new AdminDivision();
        input.setName("test");
        input.setChineseName("测试");
        input.setAdminLevel("province");
        when(adminDivisionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdminDivision result = service.createAdminDivision(input);

        assertNull(result.getId());
        assertEquals("test", result.getName());
        assertEquals("测试", result.getChineseName());
        assertEquals("province", result.getAdminLevel());
        verify(adminDivisionRepository).save(input);
    }

    @Test
    void getAdminDivisionById_found() {
        AdminDivision ad = new AdminDivision();
        ad.setId(1L);
        ad.setName("Beijing");
        when(adminDivisionRepository.findById(1L)).thenReturn(Optional.of(ad));

        Optional<AdminDivision> result = service.getAdminDivisionById(1L);

        assertTrue(result.isPresent());
        assertEquals("Beijing", result.get().getName());
    }

    @Test
    void getAdminDivisionById_notFound() {
        when(adminDivisionRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AdminDivision> result = service.getAdminDivisionById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllAdminDivisions_paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminDivision> page = new PageImpl<>(List.of(new AdminDivision()));
        when(adminDivisionRepository.findAll(pageable)).thenReturn(page);

        Page<AdminDivision> result = service.getAllAdminDivisions(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllAdminDivisions_unpaginated() {
        when(adminDivisionRepository.findAll()).thenReturn(List.of(new AdminDivision(), new AdminDivision()));

        List<AdminDivision> result = service.getAllAdminDivisions();

        assertEquals(2, result.size());
    }

    @Test
    void updateAdminDivision() {
        AdminDivision existing = new AdminDivision();
        existing.setId(1L);
        existing.setName("old");
        AdminDivision updated = new AdminDivision();
        updated.setName("new");
        updated.setChineseName("新名称");
        updated.setAdminLevel("city");
        updated.setParentId(10L);
        updated.setParentName("parent");
        updated.setGeomWkt("POINT(0 0)");
        updated.setAreaSize(100.0);
        updated.setPopulation(50000L);
        updated.setAdminCode("ABC");
        updated.setNotes("notes");

        when(adminDivisionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(adminDivisionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdminDivision result = service.updateAdminDivision(1L, updated);

        assertEquals("new", result.getName());
        assertEquals("新名称", result.getChineseName());
        assertEquals("city", result.getAdminLevel());
        assertEquals(10L, result.getParentId());
        assertEquals("parent", result.getParentName());
        assertEquals("POINT(0 0)", result.getGeomWkt());
        assertEquals(100.0, result.getAreaSize());
        assertEquals(50000L, result.getPopulation());
        assertEquals("ABC", result.getAdminCode());
        assertEquals("notes", result.getNotes());
    }

    @Test
    void updateAdminDivision_notFound() {
        when(adminDivisionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.updateAdminDivision(1L, new AdminDivision()));
    }

    @Test
    void deleteAdminDivision() {
        when(adminDivisionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(collectorService).deleteCollectorsByAdminDivisionId(1L);
        doNothing().when(adminDivisionRepository).deleteById(1L);

        service.deleteAdminDivision(1L);

        verify(adminDivisionRepository).deleteById(1L);
        verify(collectorService).deleteCollectorsByAdminDivisionId(1L);
    }

    @Test
    void deleteAdminDivision_notFound() {
        when(adminDivisionRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.deleteAdminDivision(1L));
    }

    @Test
    void findByAdminLevel() {
        AdminDivision ad = new AdminDivision();
        ad.setAdminLevel("province");
        when(adminDivisionRepository.findByAdminLevel("province")).thenReturn(List.of(ad));

        List<AdminDivision> result = service.findByAdminLevel("province");

        assertEquals(1, result.size());
        assertEquals("province", result.get(0).getAdminLevel());
    }

    @Test
    void findByParentId() {
        AdminDivision child = new AdminDivision();
        child.setId(2L);
        child.setParentId(1L);
        when(adminDivisionRepository.findByParentIdOrderByName(1L)).thenReturn(List.of(child));

        List<AdminDivision> result = service.findByParentId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getParentId());
    }

    @Test
    void findByNameContaining() {
        AdminDivision ad = new AdminDivision();
        ad.setName("Beijing");
        when(adminDivisionRepository.findByNameContainingIgnoreCase("bei")).thenReturn(List.of(ad));

        List<AdminDivision> result = service.findByNameContaining("bei");

        assertEquals(1, result.size());
        assertEquals("Beijing", result.get(0).getName());
    }

    @Test
    void getTree_withHierarchy() {
        AdminDivision province = new AdminDivision();
        province.setId(1L);
        province.setName("province");
        province.setChineseName("省");
        province.setAdminLevel("province");
        province.setParentId(null);

        AdminDivision city = new AdminDivision();
        city.setId(2L);
        city.setName("city");
        city.setChineseName("市");
        city.setAdminLevel("city");
        city.setParentId(1L);
        city.setParentName("province");

        AdminDivision district = new AdminDivision();
        district.setId(3L);
        district.setName("district");
        district.setChineseName("区");
        district.setAdminLevel("district");
        district.setParentId(2L);
        district.setParentName("city");

        when(adminDivisionRepository.findAll()).thenReturn(List.of(province, city, district));

        List<Map<String, Object>> tree = service.getTree();

        assertEquals(1, tree.size(), "should have one root");
        Map<String, Object> root = tree.get(0);
        assertEquals(1L, root.get("id"));
        assertEquals("province", root.get("name"));
        assertEquals("省", root.get("chineseName"));
        assertEquals("province", root.get("adminLevel"));

        List<Map<String, Object>> rootChildren = (List<Map<String, Object>>) root.get("children");
        assertNotNull(rootChildren);
        assertEquals(1, rootChildren.size());
        assertEquals(2L, rootChildren.get(0).get("id"));
        assertEquals("city", rootChildren.get(0).get("name"));

        List<Map<String, Object>> cityChildren = (List<Map<String, Object>>) rootChildren.get(0).get("children");
        assertNotNull(cityChildren);
        assertEquals(1, cityChildren.size());
        assertEquals(3L, cityChildren.get(0).get("id"));
        assertEquals("district", cityChildren.get(0).get("name"));
    }

    @Test
    void getTree_empty() {
        when(adminDivisionRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> tree = service.getTree();

        assertTrue(tree.isEmpty());
    }

    @Test
    void getTree_multipleRoots() {
        AdminDivision root1 = new AdminDivision();
        root1.setId(1L);
        root1.setName("A");
        root1.setParentId(null);
        AdminDivision root2 = new AdminDivision();
        root2.setId(2L);
        root2.setName("B");
        root2.setParentId(null);

        when(adminDivisionRepository.findAll()).thenReturn(List.of(root1, root2));

        List<Map<String, Object>> tree = service.getTree();

        assertEquals(2, tree.size());
    }

    @Test
    void getStats_empty() {
        when(adminDivisionRepository.findAll()).thenReturn(List.of());

        Map<String, Object> stats = service.getStats();

        assertEquals(0, stats.get("total"));
        assertEquals(0L, stats.get("withParent"));
        assertEquals(0L, stats.get("roots"));
        assertTrue(((Map<?, ?>) stats.get("byLevel")).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getStats_withData() {
        AdminDivision province = new AdminDivision();
        province.setId(1L);
        province.setAdminLevel("province");
        province.setParentId(null);

        AdminDivision city1 = new AdminDivision();
        city1.setId(2L);
        city1.setAdminLevel("city");
        city1.setParentId(1L);

        AdminDivision city2 = new AdminDivision();
        city2.setId(3L);
        city2.setAdminLevel("city");
        city2.setParentId(1L);

        AdminDivision district = new AdminDivision();
        district.setId(4L);
        district.setAdminLevel("district");
        district.setParentId(2L);

        AdminDivision noLevel = new AdminDivision();
        noLevel.setId(5L);
        noLevel.setAdminLevel(null);
        noLevel.setParentId(null);

        when(adminDivisionRepository.findAll()).thenReturn(List.of(province, city1, city2, district, noLevel));

        Map<String, Object> stats = service.getStats();

        assertEquals(5, stats.get("total"));
        assertEquals(3L, stats.get("withParent"));
        assertEquals(2L, stats.get("roots"));

        Map<String, Long> byLevel = (Map<String, Long>) stats.get("byLevel");
        assertEquals(1L, byLevel.get("province"));
        assertEquals(2L, byLevel.get("city"));
        assertEquals(1L, byLevel.get("district"));
        assertEquals(1L, byLevel.get("UNKNOWN"));
    }
}
