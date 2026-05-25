package com.tingchenggis.tingcheng.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 应用程序配置类
 * 
 * 配置Spring Boot应用程序的各种组件
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@Configuration
public class AppConfig {

    /**
     * 配置JTS几何工厂
     * 
     * @return GeometryFactory实例
     */
    @Bean
    public GeometryFactory geometryFactory() {
        return new GeometryFactory(new PrecisionModel(), 4326); // WGS84坐标系
    }

    /**
     * 配置跨域资源共享(CORS)
     * 
     * @return WebMvcConfigurer实例
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}