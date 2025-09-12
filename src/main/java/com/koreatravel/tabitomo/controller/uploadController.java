package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.PathConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(PathConstants.API)
@RequiredArgsConstructor
public class UploadController {

    @Value("${app.upload.dir:${user.dir}/uploadedImages}")
    private String uploadDirPath;
    
    private MultipartFile file;

    /**
     * 이미지 업로드
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadImage(@RequestParam("upload") MultipartFile file) {
        this.file = file;

        Map<String, Object> response = new HashMap<>();

        try {
            // 업로드 폴더가 없으면 생성
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 파일명 고유화 (중복 방지)
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            File saveFile = new File(uploadDir, fileName);

            // 파일을 지정된 경로에 저장
            file.transferTo(saveFile);

            // CKEditor에게 반환할 응답

            String imageUrl = "/uploadedImages/" + fileName;
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