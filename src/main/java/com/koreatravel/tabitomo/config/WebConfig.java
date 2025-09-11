package com.koreatravel.tabitomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = "file:///" + System.getProperty("user.dir") + "/uploadedImages/";

        registry.addResourceHandler("/uploadedImages/**")
                .addResourceLocations(uploadPath);
    }
}