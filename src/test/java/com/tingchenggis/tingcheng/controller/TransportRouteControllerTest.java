package com.tingchenggis.tingcheng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.TransportRoute;
import com.tingchenggis.tingcheng.service.TransportRouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(value = TransportRouteController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class TransportRouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransportRouteService transportRouteService;

    private TransportRoute createTestRoute(Long id, String name) {
        TransportRoute r = new TransportRoute();
        r.setId(id);
        r.setRouteName(name);
        r.setFromPavilionId(1L);
        r.setToPavilionId(2L);
        r.setDistanceKm(10.5);
        r.setTransportMode("DRIVING");
        r.setRouteType("HIGHWAY");
        return r;
    }

    @Test
    void getAllRoutes() throws Exception {
        when(transportRouteService.getAllRoutes()).thenReturn(List.of(createTestRoute(1L, "R1")));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1));
    }

    @Test
    void getAllRoutes_error() throws Exception {
        when(transportRouteService.getAllRoutes()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getRouteStats() throws Exception {
        when(transportRouteService.getRouteStats()).thenReturn(Map.of("total", 10));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/stats"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(10));
    }

    @Test
    void getRouteStats_error() throws Exception {
        when(transportRouteService.getRouteStats()).thenThrow(new RuntimeException("Stats error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/stats"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getScenicRoutes() throws Exception {
        when(transportRouteService.getScenicRoutes()).thenReturn(List.of(createTestRoute(2L, "Scenic")));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/scenic"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1));
    }

    @Test
    void getScenicRoutes_error() throws Exception {
        when(transportRouteService.getScenicRoutes()).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/scenic"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getRoutesByMode() throws Exception {
        when(transportRouteService.getRoutesByTransportMode("CYCLING")).thenReturn(List.of(createTestRoute(3L, "Bike")));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/by-mode/cycling"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1));
    }

    @Test
    void getRoutesByMode_error() throws Exception {
        when(transportRouteService.getRoutesByTransportMode("DRIVING")).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/by-mode/DRIVING"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getAvailableModes() throws Exception {
        when(transportRouteService.getAvailableTransportModes()).thenReturn(List.of("DRIVING", "CYCLING", "WALKING"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/modes"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0]").value("DRIVING"));
    }

    @Test
    void getAvailableModes_error() throws Exception {
        when(transportRouteService.getAvailableTransportModes()).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/modes"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getRoutesFromPavilion() throws Exception {
        when(transportRouteService.getRoutesFromPavilion(5L)).thenReturn(List.of(createTestRoute(4L, "From5")));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/from/5"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1));
    }

    @Test
    void getRoutesFromPavilion_error() throws Exception {
        when(transportRouteService.getRoutesFromPavilion(99L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/from/99"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getRouteBetweenPavilions() throws Exception {
        when(transportRouteService.getRouteBetweenPavilions(1L, 2L)).thenReturn(createTestRoute(5L, "Between"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/between/1/2"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.routeName").value("Between"));
    }

    @Test
    void getRouteBetweenPavilions_error() throws Exception {
        when(transportRouteService.getRouteBetweenPavilions(1L, 2L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/between/1/2"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getRouteById_found() throws Exception {
        when(transportRouteService.getRouteById(1L)).thenReturn(createTestRoute(1L, "Route1"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.routeName").value("Route1"));
    }

    @Test
    void getRouteById_notFound() throws Exception {
        when(transportRouteService.getRouteById(99L)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/99"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void getRouteById_error() throws Exception {
        when(transportRouteService.getRouteById(1L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/transport-routes/1"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void createRoute() throws Exception {
        TransportRoute input = createTestRoute(null, "NewRoute");
        TransportRoute saved = createTestRoute(10L, "NewRoute");
        when(transportRouteService.createRoute(any())).thenReturn(saved);

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("交通线创建成功"));
    }

    @Test
    void createRoute_error() throws Exception {
        when(transportRouteService.createRoute(any())).thenThrow(new RuntimeException("Creation failed"));

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTestRoute(null, "Fail"))))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void updateRoute() throws Exception {
        TransportRoute updated = createTestRoute(1L, "UpdatedRoute");
        when(transportRouteService.updateRoute(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put("/transport-routes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("交通线更新成功"));
    }

    @Test
    void updateRoute_error() throws Exception {
        when(transportRouteService.updateRoute(eq(1L), any())).thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(MockMvcRequestBuilders.put("/transport-routes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTestRoute(1L, "Fail"))))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void deleteRoute() throws Exception {
        doNothing().when(transportRouteService).deleteRoute(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/transport-routes/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("交通线删除成功"));
    }

    @Test
    void deleteRoute_error() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(transportRouteService).deleteRoute(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/transport-routes/1"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void getTspPlan() throws Exception {
        Map<String, Object> plan = new HashMap<>();
        plan.put("order", List.of(1L, 2L, 3L));
        plan.put("distance", 45.0);
        when(transportRouteService.getTspRoute(List.of(1L, 2L), "DRIVING", "distance"))
            .thenReturn(plan);

        TransportRouteController.TspPlanRequest request = new TransportRouteController.TspPlanRequest();
        request.setPavilionIds(List.of(1L, 2L));
        request.setMode("DRIVING");
        request.setObjective("distance");

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes/tsp-plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    void getTspPlan_error() throws Exception {
        when(transportRouteService.getTspRoute(any(), any(), any()))
            .thenThrow(new RuntimeException("TSP failed"));

        TransportRouteController.TspPlanRequest request = new TransportRouteController.TspPlanRequest();
        request.setPavilionIds(List.of(1L));
        request.setMode("DRIVING");
        request.setObjective("distance");

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes/tsp-plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void buildMultiModal() throws Exception {
        when(transportRouteService.buildMultiModalNetwork()).thenReturn(Map.of("networks", "ok"));

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes/build-multi-modal"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    void buildMultiModal_error() throws Exception {
        when(transportRouteService.buildMultiModalNetwork()).thenThrow(new RuntimeException("Build failed"));

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes/build-multi-modal"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    @Test
    void buildRoadNetwork() throws Exception {
        when(transportRouteService.buildRoadNetwork()).thenReturn(Map.of("roads", "ok"));

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes/build-network"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    void buildRoadNetwork_error() throws Exception {
        when(transportRouteService.buildRoadNetwork()).thenThrow(new RuntimeException("Network failed"));

        mockMvc.perform(MockMvcRequestBuilders.post("/transport-routes/build-network"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }
}
