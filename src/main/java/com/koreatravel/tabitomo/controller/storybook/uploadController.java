package com.koreatravel.tabitomo.controller.storybook;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class uploadController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/tabitomo/uploadedImages/"; // 실제 이미지 저장 경로
    private MultipartFile file;

    /**
     * 이미지 업로드
     * @param file 업로드한 이미지 파일
     * @return json응답
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadImage(@RequestParam("upload") MultipartFile file) {
        this.file = file;
        Map<String, Object> response = new HashMap<>();
        System.out.println(System.getProperty("user.dir"));

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists() || !uploadDir.isDirectory()) {
                response.put("uploaded", false);
                response.put("error", "업로드 폴더가 존재하지 않습니다.");
                return response;
            }

            // 파일명 고유화 (중복 방지)
            String fileName = UUID.randomUUID().toString();
            File saveFile = new File(UPLOAD_DIR, fileName);

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