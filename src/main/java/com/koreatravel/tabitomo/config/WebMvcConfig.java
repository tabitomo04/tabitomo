package com.koreatravel.tabitomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images from the filesystem
        String uploadPath = System.getProperty("user.dir") + "/tabitomo/uploadedImages/";
        registry.addResourceHandler("/uploadedImages/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
