package com.tingchenggis.tingcheng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.RoutePlan;
import com.tingchenggis.tingcheng.repository.RoutePlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@WebMvcTest(value = RoutePlanController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class RoutePlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoutePlanRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private RoutePlan createPlan(Long id, String name, LocalDateTime createdAt) {
        RoutePlan p = new RoutePlan();
        p.setId(id);
        p.setPlanName(name);
        p.setTransportMode("driving");
        p.setObjective("最短路径");
        p.setNotes("备注");
        p.setTotalDistance(100.0);
        p.setTotalDuration(3600.0);
        p.setTotalFare(50.0);
        p.setTotalTicket(20.0);
        p.setTotalCost(70.0);
        p.setPavilionCount(2);
        p.setVisitOrderIds("1,2,3");
        p.setVisitOrderNames("A → B → C");
        p.setCreatedAt(createdAt);
        return p;
    }

    @Test
    void list() throws Exception {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/route-plans"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty());
    }

    @Test
    void listWithData() throws Exception {
        RoutePlan plan = createPlan(1L, "测试方案", LocalDateTime.now());
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(plan));

        mockMvc.perform(MockMvcRequestBuilders.get("/route-plans"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].planName").value("测试方案"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].transportMode").value("driving"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].pavilionCount").value(2));
    }

    @Test
    void get_found() throws Exception {
        RoutePlan plan = createPlan(1L, "方案A", LocalDateTime.now());
        plan.setPlanJson("{\"totalDistance\":100}");
        when(repository.findById(1L)).thenReturn(Optional.of(plan));

        mockMvc.perform(MockMvcRequestBuilders.get("/route-plans/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.planName").value("方案A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.plan.totalDistance").value(100));
    }

    @Test
    void get_notFound() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/route-plans/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("路线方案不存在"));
    }

    @Test
    void save_success() throws Exception {
        RoutePlan saved = createPlan(1L, "我的方案", LocalDateTime.now());
        when(repository.save(any(RoutePlan.class))).thenReturn(saved);

        String body = "{\"planName\":\"我的方案\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/route-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.planName").value("我的方案"));

        verify(repository).save(any(RoutePlan.class));
    }

    @Test
    void save_withPlan() throws Exception {
        RoutePlan saved = createPlan(2L, "带数据的方案", LocalDateTime.now());
        saved.setPlanJson("{\"totalDistance\":200.0,\"visitOrderIds\":[10,20,30]}");
        when(repository.save(any(RoutePlan.class))).thenReturn(saved);

        Map<String, Object> planMap = new LinkedHashMap<>();
        planMap.put("totalDistance", 200.0);
        planMap.put("totalDuration", 7200.0);
        planMap.put("totalFare", 100.0);
        planMap.put("totalTicket", 40.0);
        planMap.put("totalCost", 140.0);
        planMap.put("visitOrderIds", List.of(10, 20, 30));
        planMap.put("visitOrder", List.of("亭A", "亭B", "亭C"));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("planName", "带数据的方案");
        requestBody.put("mode", "walking");
        requestBody.put("objective", "最短时间");
        requestBody.put("notes", "测试备注");
        requestBody.put("plan", planMap);

        String body = objectMapper.writeValueAsString(requestBody);

        mockMvc.perform(MockMvcRequestBuilders.post("/route-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.planName").value("带数据的方案"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.transportMode").value("driving"));

        verify(repository).save(any(RoutePlan.class));
    }

    @Test
    void delete_success() throws Exception {
        RoutePlan plan = createPlan(1L, "待删除", LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(plan));

        mockMvc.perform(MockMvcRequestBuilders.delete("/route-plans/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));

        verify(repository).delete(plan);
    }

    @Test
    void delete_notFound() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/route-plans/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("不存在"));

        verify(repository, never()).delete(any());
    }

}
