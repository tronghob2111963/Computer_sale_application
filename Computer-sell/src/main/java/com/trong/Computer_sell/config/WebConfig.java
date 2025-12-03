package com.trong.Computer_sell.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối của thư mục uploads
        Path uploadPath = Paths.get("src/main/resources/static/uploads/").toAbsolutePath();
        String uploadLocation = "file:" + uploadPath.toString().replace("\\", "/") + "/";
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
        
        // Thêm resource handler cho thư mục uploads bên ngoài (nếu cần)
        Path externalUploadPath = Paths.get("uploads/").toAbsolutePath();
        String externalLocation = "file:" + externalUploadPath.toString().replace("\\", "/") + "/";
        
        registry.addResourceHandler("/external-uploads/**")
                .addResourceLocations(externalLocation);
    }
}

