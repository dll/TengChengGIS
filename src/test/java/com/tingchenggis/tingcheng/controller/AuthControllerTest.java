package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.AppUser;
import com.tingchenggis.tingcheng.security.JwtUtil;
import com.tingchenggis.tingcheng.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AppUserService userService;

    @Test
    void login_success() throws Exception {
        AppUser admin = new AppUser("419116", "<hash>", "ADMIN", "系统管理员");
        admin.setId(1L);
        when(userService.authenticate("419116", "419116")).thenReturn(Optional.of(admin));
        when(jwtUtil.generateToken(any(), any())).thenReturn("fake-jwt-token");

        String body = "{\"username\":\"419116\",\"password\":\"419116\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("fake-jwt-token"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("419116"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_wrongPassword() throws Exception {
        when(userService.authenticate("419116", "wrong")).thenReturn(Optional.empty());

        String body = "{\"username\":\"419116\",\"password\":\"wrong\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void login_unknownUser() throws Exception {
        when(userService.authenticate("hacker", "419116")).thenReturn(Optional.empty());

        String body = "{\"username\":\"hacker\",\"password\":\"419116\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }
}
