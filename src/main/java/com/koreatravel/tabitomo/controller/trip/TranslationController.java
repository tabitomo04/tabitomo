package com.koreatravel.tabitomo.controller.trip;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.koreatravel.tabitomo.service.trip.TranslationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public ResponseEntity<?> translate(@RequestBody TranslationRequest request) {
        try {
            // Call the batch translation method in the service
            List<String> translatedTexts = translationService.translateTexts(request.getTexts(), request.getTargetLanguage());
            return ResponseEntity.ok(new TranslationResponse(translatedTexts));
        } catch (Exception e) {
            // Log the exception for debugging purposes
            return ResponseEntity.badRequest().body(Map.of("error", "번역 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // --- DTOs for the request and response ---

    static class TranslationRequest {
        private List<String> texts;
        private String targetLanguage;

        public List<String> getTexts() {
            return texts;
        }

        public void setTexts(List<String> texts) {
            this.texts = texts;
        }

        public String getTargetLanguage() {
            return targetLanguage;
        }

        public void setTargetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
        }
    }

    static class TranslationResponse {
        private final List<String> translatedTexts;

        public TranslationResponse(List<String> translatedTexts) {
            this.translatedTexts = translatedTexts;
        }

        public List<String> getTranslatedTexts() {
            return translatedTexts;
        }
    }
}
