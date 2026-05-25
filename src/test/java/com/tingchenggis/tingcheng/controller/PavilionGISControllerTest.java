package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.PavilionGISService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PavilionGISController.class)
class PavilionGISControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PavilionGISService pavilionGISService;

    @Test
    void calculateDistance() throws Exception {
        when(pavilionGISService.calculateDistance(1L, 2L)).thenReturn(12345.0);

        mockMvc.perform(get("/pavilions-gis/distance/1/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(12345.0));
    }

    @Test
    void findNearestPavilions() throws Exception {
        when(pavilionGISService.findNearestPavilions(any(), eq(5))).thenReturn(List.of(new Pavilion()));

        mockMvc.perform(get("/pavilions-gis/nearest")
                .param("longitude", "118.3")
                .param("latitude", "32.3")
                .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void findNearestPavilions_defaultLimit() throws Exception {
        when(pavilionGISService.findNearestPavilions(any(), eq(5))).thenReturn(List.of());

        mockMvc.perform(get("/pavilions-gis/nearest")
                .param("longitude", "118.3")
                .param("latitude", "32.3"))
            .andExpect(status().isOk());
    }

    @Test
    void findPavilionsInBuffer() throws Exception {
        when(pavilionGISService.findPavilionsInBuffer(1L, 1000)).thenReturn(List.of(new Pavilion()));

        mockMvc.perform(get("/pavilions-gis/1/buffer")
                .param("bufferRadius", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void findPavilionsInBuffer_defaultRadius() throws Exception {
        when(pavilionGISService.findPavilionsInBuffer(1L, 1000)).thenReturn(List.of());

        mockMvc.perform(get("/pavilions-gis/1/buffer"))
            .andExpect(status().isOk());
    }

    @Test
    void generateHeatmapData() throws Exception {
        java.util.List<Object[]> data = java.util.Collections.singletonList(new Object[]{118.3, 32.3, 4.5});
        when(pavilionGISService.generateHeatmapData()).thenReturn(data);

        mockMvc.perform(get("/pavilions-gis/heatmap"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void generateShortestPath() throws Exception {
        when(pavilionGISService.calculateDistance(1L, 2L)).thenReturn(5000.0);

        mockMvc.perform(get("/pavilions-gis/shortest-path/1/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.distance").value(5000.0));
    }

    @Test
    void generateOptimalPath() throws Exception {
        when(pavilionGISService.generateOptimalPath(anyList())).thenReturn(null);

        mockMvc.perform(get("/pavilions-gis/optimal-path")
                .param("pavilionIds", "1,2,3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void generateOptimalPath_insufficient() throws Exception {
        mockMvc.perform(get("/pavilions-gis/optimal-path")
                .param("pavilionIds", "1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSystemOverview() throws Exception {
        mockMvc.perform(get("/pavilions-gis/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
