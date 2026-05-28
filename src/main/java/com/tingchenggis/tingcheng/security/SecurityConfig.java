package com.tingchenggis.tingcheng.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtil);
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 预检请求一律放行
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 登录、注册、当前用户查询（携带 token 才返回信息）
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                // H2 Console（dev profile 下 h2.console.enabled=true 时才生效；prod 下路径不存在，无害）
                .requestMatchers("/h2-console/**").permitAll()
                // 静态资源 / 首页 / 分享页
                .requestMatchers("/", "/index.html", "/share.html", "/share/**",
                                  "/api-test.html", "/favicon.ico",
                                  "/css/**", "/js/**", "/images/**", "/audio/**",
                                  "/webjars/**", "/*.js").permitAll()
                // 健康检查
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // 公开只读接口（演示用）
                .requestMatchers(HttpMethod.GET,
                    "/thousand-pavilions/locations",
                    "/thousand-pavilions/route/**",
                    "/thousand-pavilions/multimedia/**",
                    "/thousand-pavilions/traverse-all",
                    "/thousand-pavilions/optimal-route",
                    "/thousand-pavilions/smart-tour",
                    "/thousand-pavilions/tourism-services",
                    "/thousand-pavilions/weather",
                    "/thousand-pavilions/nearby-facilities/**",
                    "/thousand-pavilions/vr-experience/**",
                    "/thousand-pavilions/navigation/**",
                    "/thousand-pavilions/export/**",
                    "/scenic-areas/**",
                    "/admin-divisions/**",
                    "/transport-routes/**",
                    "/tourism-routes/**",
                    "/pavilions/**",
                    "/pavilions-gis/**",
                    "/poi/**",
                    "/route-plans/**",
                    "/travel-logs/**",
                    "/coordinate/**",
                    "/ai/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/thousand-pavilions/share-route").permitAll()
                .requestMatchers(HttpMethod.POST, "/ai/**").permitAll()
                .requestMatchers("/ogc/**").permitAll()
                // 只有管理员可以做的事：网络重建、坐标批量纠偏、OSM 导入
                .requestMatchers("/transport-routes/build-network",
                                  "/transport-routes/build-multi-modal",
                                  "/coordinate/correct-pavilions",
                                  "/osm/import/**").hasRole("ADMIN")
                // 写操作（增删改、导入、采集记录）需要登录
                .requestMatchers(HttpMethod.POST, "/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((req, res, ex) -> writeJson(res, HttpServletResponse.SC_UNAUTHORIZED,
                    "未登录或登录已过期"))
                .accessDeniedHandler((req, res, ex) -> writeJson(res, HttpServletResponse.SC_FORBIDDEN,
                    "权限不足"))
            )
            .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeJson(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("status", status);
        body.put("message", message);
        objectMapper.writeValue(res.getWriter(), body);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 教学项目：默认放开常见本地源；生产部署可通过配置覆盖
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://*.localhost:*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
