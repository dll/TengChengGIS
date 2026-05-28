package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.NavigationService;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import com.tingchenggis.tingcheng.service.PavilionExportService;
import com.tingchenggis.tingcheng.service.PavilionImportService;
import com.tingchenggis.tingcheng.service.PavilionService;
import com.tingchenggis.tingcheng.service.ThousandPavilionsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ThousandPavilionsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ThousandPavilionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PavilionService pavilionService;
    @MockBean
    private ThousandPavilionsService thousandPavilionsService;
    @MockBean
    private PavilionImportService pavilionImportService;
    @MockBean
    private PavilionExportService pavilionExportService;
    @MockBean
    private PavilionCollectorService collectorService;
    @MockBean
    private NavigationService navigationService;

    @Test
    void getAllPavilionLocations() throws Exception {
        Pavilion p = new Pavilion("test", "测试", null, null, 118.3, 32.3, "HISTORICAL");
        p.setId(1L);
        when(pavilionService.getAllPavilions()).thenReturn(List.of(p));

        mockMvc.perform(get("/thousand-pavilions/locations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].chineseName").value("测试"));
    }

    @Test
    void getAllPavilionLocations_filtered() throws Exception {
        Pavilion p = new Pavilion("test", "测试", null, null, 118.3, 32.3, "HISTORICAL");
        p.setId(1L);
        when(pavilionService.getAllPavilions()).thenReturn(List.of(p));

        mockMvc.perform(get("/thousand-pavilions/locations")
                .param("type", "HISTORICAL"))
            .andExpect(status().isOk());
    }

    @Test
    void getRouteBetweenPavilions() throws Exception {
        Pavilion p1 = new Pavilion("a", "亭A", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        Pavilion p2 = new Pavilion("b", "亭B", null, null, 118.4, 32.4, "MODERN");
        p2.setId(2L);
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p1));
        when(pavilionService.getPavilionById(2L)).thenReturn(Optional.of(p2));

        mockMvc.perform(get("/thousand-pavilions/route/1/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.from").value("亭A"))
            .andExpect(jsonPath("$.to").value("亭B"));
    }

    @Test
    void getTraverseAllRoute() throws Exception {
        Pavilion p1 = new Pavilion("a", "亭A", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        Pavilion p2 = new Pavilion("b", "亭B", null, null, 118.4, 32.4, "MODERN");
        p2.setId(2L);
        when(pavilionService.getAllPavilions()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/thousand-pavilions/traverse-all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPavilions").value(2));
    }

    @Test
    void getTraverseAllRoute_empty() throws Exception {
        when(pavilionService.getAllPavilions()).thenReturn(List.of());

        mockMvc.perform(get("/thousand-pavilions/traverse-all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalDistance").value(0.0));
    }

    @Test
    void getMultimedia() throws Exception {
        Pavilion p = new Pavilion("test", "测试", "desc", null, 118.3, 32.3, "HISTORICAL");
        p.setId(1L);
        p.setHistoricalSignificance("重要");
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/thousand-pavilions/multimedia/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("测试"));
    }

    @Test
    void getSmartTourPlan() throws Exception {
        when(thousandPavilionsService.getSmartTourPlan(any(), any(), anyInt(), any()))
            .thenReturn(Map.of("totalPavilions", 3, "route", List.of()));

        mockMvc.perform(get("/thousand-pavilions/smart-tour")
                .param("duration", "240"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPavilions").value(3));
    }

    @Test
    void getWeatherInfo() throws Exception {
        when(thousandPavilionsService.getWeatherInfo())
            .thenReturn(Map.of("temperature", 25, "condition", "晴朗"));

        mockMvc.perform(get("/thousand-pavilions/weather"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.temperature").value(25));
    }

    @Test
    void getOptimalRoute() throws Exception {
        when(thousandPavilionsService.getOptimalTraversalRoute()).thenReturn(List.of(1L, 2L));
        Pavilion p1 = new Pavilion("a", "亭A", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        Pavilion p2 = new Pavilion("b", "亭B", null, null, 118.4, 32.4, "MODERN");
        p2.setId(2L);
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p1));
        when(pavilionService.getPavilionById(2L)).thenReturn(Optional.of(p2));
        when(thousandPavilionsService.calculateDistance(1L, 2L)).thenReturn(12.34);

        mockMvc.perform(get("/thousand-pavilions/optimal-route"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalPavilions").value(2));
    }

    @Test
    void createPavilion() throws Exception {
        when(pavilionService.createPavilion(any())).thenAnswer(i -> i.getArgument(0));

        String json = "{\"name\":\"test\",\"chineseName\":\"测试亭\",\"longitude\":118.3,\"latitude\":32.3}";
        mockMvc.perform(post("/thousand-pavilions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deletePavilion() throws Exception {
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(new Pavilion()));
        doNothing().when(pavilionService).deletePavilion(1L);

        mockMvc.perform(delete("/thousand-pavilions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deletePavilion_notFound() throws Exception {
        when(pavilionService.getPavilionById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/thousand-pavilions/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void nearbyFacilities() throws Exception {
        when(thousandPavilionsService.getNearbyFacilities(1L, 1.0))
            .thenReturn(Map.of("facilities", List.of()));

        mockMvc.perform(get("/thousand-pavilions/nearby-facilities/1"))
            .andExpect(status().isOk());
    }

    @Test
    void getRealTimeNavigation() throws Exception {
        Pavilion p1 = new Pavilion("a", "亭A", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        Pavilion p2 = new Pavilion("b", "亭B", null, null, 118.4, 32.4, "MODERN");
        p2.setId(2L);
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p1));
        when(pavilionService.getPavilionById(2L)).thenReturn(Optional.of(p2));

        Map<String, Object> navResult = new java.util.LinkedHashMap<>();
        navResult.put("totalSteps", 5);
        navResult.put("steps", List.of());
        when(navigationService.getTurnByTurnNavigation(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString()))
            .thenReturn(navResult);

        mockMvc.perform(get("/thousand-pavilions/navigation/1/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSteps").value(5));
    }

    @Test
    void getRealTimeNavigation_notFound() throws Exception {
        when(pavilionService.getPavilionById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/thousand-pavilions/navigation/99/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void vrExperience() throws Exception {
        when(thousandPavilionsService.getVRExperience(1L))
            .thenReturn(Map.of("hasVR", true));

        mockMvc.perform(get("/thousand-pavilions/vr-experience/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasVR").value(true));
    }
}
