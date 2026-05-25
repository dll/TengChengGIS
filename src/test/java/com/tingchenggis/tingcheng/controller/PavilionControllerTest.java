package com.tingchenggis.tingcheng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.PavilionService;
import com.tingchenggis.tingcheng.service.PavilionStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PavilionController.class)
class PavilionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PavilionService pavilionService;

    @Test
    void createPavilion() throws Exception {
        Pavilion p = new Pavilion("test", "测试亭", "desc", null, 118.3, 32.3, "HISTORICAL");
        when(pavilionService.createPavilion(any())).thenReturn(p);

        mockMvc.perform(post("/pavilions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getPavilionById_found() throws Exception {
        Pavilion p = new Pavilion();
        p.setId(1L);
        p.setName("test");
        when(pavilionService.getPavilionById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/pavilions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getPavilionById_notFound() throws Exception {
        when(pavilionService.getPavilionById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pavilions/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllPavilions() throws Exception {
        Page<Pavilion> page = new PageImpl<>(List.of(new Pavilion()));
        when(pavilionService.getAllPavilions(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/pavilions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updatePavilion() throws Exception {
        Pavilion p = new Pavilion();
        p.setName("updated");
        when(pavilionService.updatePavilion(eq(1L), any())).thenReturn(p);

        mockMvc.perform(put("/pavilions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updatePavilion_notFound() throws Exception {
        when(pavilionService.updatePavilion(eq(99L), any())).thenThrow(new RuntimeException("Pavilion not found"));

        mockMvc.perform(put("/pavilions/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Pavilion())))
            .andExpect(status().isNotFound());
    }

    @Test
    void deletePavilion() throws Exception {
        doNothing().when(pavilionService).deletePavilion(1L);

        mockMvc.perform(delete("/pavilions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deletePavilion_notFound() throws Exception {
        doThrow(new RuntimeException("Pavilion not found")).when(pavilionService).deletePavilion(99L);

        mockMvc.perform(delete("/pavilions/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void findByPavilionType() throws Exception {
        when(pavilionService.findByPavilionType("HISTORICAL")).thenReturn(List.of(new Pavilion()));

        mockMvc.perform(get("/pavilions/type/HISTORICAL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void findByNameContaining() throws Exception {
        when(pavilionService.findByNameContaining("醉翁")).thenReturn(List.of(new Pavilion()));

        mockMvc.perform(get("/pavilions/search").param("name", "醉翁"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void findByBuiltYearBetween() throws Exception {
        when(pavilionService.findByBuiltYearBetween(1000, 2000)).thenReturn(List.of());

        mockMvc.perform(get("/pavilions/by-year-range")
                .param("startYear", "1000")
                .param("endYear", "2000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void findByVisitorRatingGreaterThanEqual() throws Exception {
        when(pavilionService.findByVisitorRatingGreaterThanEqual(4.0)).thenReturn(List.of(new Pavilion()));

        mockMvc.perform(get("/pavilions/popular").param("minRating", "4.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getPavilionStats() throws Exception {
        when(pavilionService.getStats()).thenReturn(new PavilionStats());

        mockMvc.perform(get("/pavilions/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void recommendPavilions() throws Exception {
        when(pavilionService.recommendPavilions("u1", "historical")).thenReturn(List.of());

        mockMvc.perform(post("/pavilions/recommendations")
                .param("userId", "u1")
                .param("preferences", "historical"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
