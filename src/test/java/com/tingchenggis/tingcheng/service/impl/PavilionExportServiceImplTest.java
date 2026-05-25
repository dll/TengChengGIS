package com.tingchenggis.tingcheng.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PavilionExportServiceImplTest {

    @Mock
    private PavilionService pavilionService;

    private PavilionExportServiceImpl service;
    private Pavilion p;

    @BeforeEach
    void setUp() {
        service = new PavilionExportServiceImpl(pavilionService, new ObjectMapper());
        p = new Pavilion("test", "测试", "desc", null, 118.3, 32.3, "HISTORICAL");
        p.setId(1L);
        p.setStructure("木结构");
        p.setTopStyle("歇山顶");
        p.setStreet("琅琊路");
        p.setLocationDesc("琅琊山");
        p.setNotes("重点保护");
        p.setAreaSize(100.0);
    }

    @Test
    void exportGeoJson() {
        when(pavilionService.getAllPavilions()).thenReturn(List.of(p));
        byte[] data = service.exportGeoJson();
        String json = new String(data, StandardCharsets.UTF_8);
        assertTrue(json.contains("FeatureCollection"));
        assertTrue(json.contains("Point"));
        assertTrue(json.contains("118.3"));
    }

    @Test
    void exportGeoJson_empty() {
        when(pavilionService.getAllPavilions()).thenReturn(List.of());
        byte[] data = service.exportGeoJson();
        String json = new String(data, StandardCharsets.UTF_8);
        assertTrue(json.contains("FeatureCollection"));
        assertTrue(json.contains("\"features\":[]"));
    }

    @Test
    void exportExcel() {
        when(pavilionService.getAllPavilions()).thenReturn(List.of(p));
        byte[] data = service.exportExcel();
        assertTrue(data.length > 0);
    }

    @Test
    void exportExcel_empty() {
        when(pavilionService.getAllPavilions()).thenReturn(List.of());
        byte[] data = service.exportExcel();
        assertTrue(data.length > 0);
    }

    @Test
    void exportCsv() {
        when(pavilionService.getAllPavilions()).thenReturn(List.of(p));
        byte[] data = service.exportCsv();
        String csv = new String(data, StandardCharsets.UTF_8);
        assertTrue(csv.contains("测试"));
        assertTrue(csv.contains("118.3"));
    }

    @Test
    void exportCsv_empty() {
        when(pavilionService.getAllPavilions()).thenReturn(List.of());
        byte[] data = service.exportCsv();
        String csv = new String(data, StandardCharsets.UTF_8);
        assertTrue(csv.contains("序号"));
    }

    @Test
    void exportExcelTemplate() {
        byte[] data = service.exportExcelTemplate();
        assertTrue(data.length > 0);
    }

    @Test
    void exportCsvTemplate() {
        byte[] data = service.exportCsvTemplate();
        String csv = new String(data, StandardCharsets.UTF_8);
        assertTrue(csv.contains("序号"));
    }

    @Test
    void exportGeoJson_nullFields() {
        Pavilion np = new Pavilion();
        np.setId(2L);
        np.setName(null);
        when(pavilionService.getAllPavilions()).thenReturn(List.of(np));

        byte[] data = service.exportGeoJson();
        assertTrue(data.length > 0);
        String json = new String(data, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"name\""));
    }

    @Test
    void exportCsv_specialChars() {
        p.setNotes("包含,逗号\"和引号\n换行");
        when(pavilionService.getAllPavilions()).thenReturn(List.of(p));

        byte[] data = service.exportCsv();
        String csv = new String(data, StandardCharsets.UTF_8);
        assertTrue(csv.contains("\""));
    }
}
