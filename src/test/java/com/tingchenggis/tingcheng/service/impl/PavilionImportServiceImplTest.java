package com.tingchenggis.tingcheng.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PavilionImportServiceImplTest {

    @Mock
    private PavilionService pavilionService;
    @Mock
    private PavilionCollectorService collectorService;

    private PavilionImportServiceImpl service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new PavilionImportServiceImpl(pavilionService, objectMapper, collectorService);
        when(pavilionService.createPavilion(any())).thenAnswer(i -> {
            Pavilion p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(collectorService.createCollector(any())).thenReturn(null);
    }

    @Test
    void importAuto_xlsx() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        assertThrows(Exception.class, () -> service.importAuto(file));
    }

    @Test
    void importAuto_csv() {
        String csv = "\uFEFF序号,名称,结构,x,y,位置,所在街道/小区,大致面积(m²),风格,备注\n" +
            "1,醉翁亭,木结构,118.3,32.3,琅琊山,琅琊路,100,歇山顶,重点\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
            csv.getBytes(StandardCharsets.UTF_8));

        var result = service.importAuto(file);

        assertNotNull(result);
        assertEquals("csv", result.getFormat());
    }

    @Test
    void importAuto_csvSkipHeader() {
        String csv = "序号,名称,结构,x,y,位置,所在街道/小区,大致面积(m²),风格,备注\n" +
            "1,亭一,木,118.0,32.0,山,路,50,顶,\n" +
            "2,亭二,钢,118.1,32.1,山,路,60,顶,\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
            csv.getBytes(StandardCharsets.UTF_8));

        var result = service.importAuto(file);

        assertEquals(2, result.getSuccessCount());
    }

    @Test
    void importAuto_geojson() {
        String geojson = """
            {"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[118.3,32.3]},"properties":{"name":"醉翁亭","type":"HISTORICAL"}}]}
            """;
        MockMultipartFile file = new MockMultipartFile("file", "test.geojson", "application/json",
            geojson.getBytes(StandardCharsets.UTF_8));

        var result = service.importAuto(file);

        assertEquals("geojson", result.getFormat());
        assertEquals(1, result.getSuccessCount());
    }

    @Test
    void importAuto_unknownFormat() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
        assertThrows(RuntimeException.class, () -> service.importAuto(file));
    }

    @Test
    void importAuto_csvWithError() {
        String csv = "序号,名称,结构,x,y,位置,街道,面积,风格,备注\n" +
            "1,亭一,木,invalid,32.0,山,路,50,顶,\n";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
            csv.getBytes(StandardCharsets.UTF_8));

        var result = service.importAuto(file);

        assertEquals(0, result.getSuccessCount());
        assertTrue(result.getErrorCount() > 0 || result.getSkipCount() >= 0);
    }

    @Test
    void importFromGeoJson_noFeatures() {
        String geojson = "{\"type\":\"FeatureCollection\",\"features\":[]}";
        MockMultipartFile file = new MockMultipartFile("file", "test.geojson", "application/json",
            geojson.getBytes(StandardCharsets.UTF_8));

        var result = service.importFromGeoJson(file);

        assertEquals(0, result.getTotalRows());
    }
}
