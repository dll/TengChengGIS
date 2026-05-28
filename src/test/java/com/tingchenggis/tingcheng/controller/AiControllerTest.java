package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.ai.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(value = AiController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @Test
    void chat() throws Exception {
        when(aiService.chat(anyString())).thenReturn("AI reply");
        when(aiService.isAiAvailable()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/ai/chat").param("message", "test"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.reply").value("AI reply"));
    }

    @Test
    void getPavilionIntroduction() throws Exception {
        when(aiService.generatePavilionIntroduction(1L)).thenReturn("intro");
        when(aiService.isAiAvailable()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/ai/pavilion/1"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.introduction").value("intro"));
    }

    @Test
    void status() throws Exception {
        when(aiService.isAiAvailable()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/ai/status"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.aiAvailable").value(false));
    }
}
