package com.koreatravel.tabitomo.controller.storybook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class uploadController {

    @Value("${app.upload-dir}")
    private String uploadDir;
    
    @Value("${app.upload-path-pattern}")
    private String uploadPathPattern;
    
    private Path uploadPath;
    
    @PostConstruct
    public void init() {
        // Initialize upload path
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }
    /**
     * 이미지 업로드
     * @param file 업로드한 이미지 파일
     * @return json응답
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadImage(@RequestParam("upload") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Ensure upload directory exists
            File uploadDir = uploadPath.toFile();
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    response.put("uploaded", 0);
                    response.put("error", "Failed to create upload directory: " + uploadDir.getAbsolutePath());
                    return response;
                }
            }

            // Generate unique filename with original extension
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = uploadPath.resolve(fileName);
            
            // Save file to the specified path
            file.transferTo(targetLocation);
            
            // Get the URL path (remove the leading ** from the pattern)
            String urlPath = uploadPathPattern.replace("/**", "");
            String imageUrl = urlPath + "/" + fileName;
            response.put("uploaded", 1);
            response.put("url", imageUrl);

        } catch (Exception e) {
            response.put("uploaded", 0);
            response.put("error", Map.of("message", "이미지 업로드에 실패했습니다."));
            e.printStackTrace();
        }
        return response;
    }

}