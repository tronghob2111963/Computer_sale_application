package com.trong.Computer_sell.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cho phép truy cập qua URL /uploads/** trỏ vào thư mục uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
