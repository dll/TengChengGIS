package com.tingchenggis.tingcheng.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * 配置静态资源处理器
 * 
 * @author TingChengGIS
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String FORWARD_INDEX_HTML = "forward:/index.html";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源路径
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // 设置缓存时间为1小时
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将根路径映射到index.html，让Spring Boot直接返回静态文件
        registry.addViewController("/").setViewName(FORWARD_INDEX_HTML);
        registry.addViewController("/index").setViewName(FORWARD_INDEX_HTML);
        registry.addViewController("/home").setViewName(FORWARD_INDEX_HTML);
    }
}