package com.example.be_qltv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads/books}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Serve uploaded files as static resources
        // Convert to absolute path
        File uploadDirFile = new File(uploadDir);
        String absolutePath = uploadDirFile.getAbsolutePath();
        
        registry.addResourceHandler("/uploads/books/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
    }
}
