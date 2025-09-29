package com.koreatravel.tabitomo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.upload-path-pattern}")
    private String uploadPathPattern;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convert the upload directory to absolute path
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toString();
        
        // Map URL pattern to file system location
        registry.addResourceHandler(uploadPathPattern)
                .addResourceLocations("file:" + absolutePath + "/");
    }

    @PostConstruct
    public void init() {
        // Ensure upload directory exists
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            boolean created = uploadDirFile.mkdirs();
            if (created) {
                System.out.println("Created upload directory: " + uploadDir);
            } else {
                System.err.println("Failed to create upload directory: " + uploadDir);
            }
        }
    }
}
