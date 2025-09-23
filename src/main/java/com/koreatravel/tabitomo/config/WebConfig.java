package com.koreatravel.tabitomo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploadedImages/";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/css/**",
                "/js/**",
                "/images/**",
                "/image/**",
                "/fonts/**",
                "/favicon.ico")
            .addResourceLocations(
                "classpath:/static/css/",
                "classpath:/static/js/",
                "classpath:/static/images/",
                "classpath:/static/image/",
                "classpath:/static/fonts/",
                "classpath:/static/"
            );

        // CKEditor 업로드 파일 매핑
        registry.addResourceHandler("/uploadedImages/**")
                .addResourceLocations("file:" + UPLOAD_DIR);
    }
}
