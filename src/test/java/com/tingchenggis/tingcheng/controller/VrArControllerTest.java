package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.service.VrArService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@WebMvcTest(value = VrArController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class VrArControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VrArService vrArService;

    @Test
    void getVrExperience() throws Exception {
        Map<String, Object> mock = new LinkedHashMap<>();
        mock.put("pavilionId", 1L);
        mock.put("hasVR", true);
        when(vrArService.getVrExperience(1L)).thenReturn(mock);

        mockMvc.perform(MockMvcRequestBuilders.get("/vr-ar/experience/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.pavilionId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hasVR").value(true));
    }

    @Test
    void getArOverlayData() throws Exception {
        Map<String, Object> mock = new LinkedHashMap<>();
        mock.put("pavilionId", 1L);
        mock.put("geoJson", "{\"type\":\"FeatureCollection\",\"features\":[]}");
        when(vrArService.getArOverlayData(1L)).thenReturn(mock);

        mockMvc.perform(MockMvcRequestBuilders.get("/vr-ar/ar-overlay/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.geoJson").exists());
    }

    @Test
    void get3dSceneData() throws Exception {
        Map<String, Object> mock = new LinkedHashMap<>();
        mock.put("pavilionId", 1L);
        when(vrArService.get3dSceneData(1L)).thenReturn(mock);

        mockMvc.perform(MockMvcRequestBuilders.get("/vr-ar/3d-scene/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.pavilionId").value(1));
    }
}
