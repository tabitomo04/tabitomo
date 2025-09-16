package com.koreatravel.tabitomo.controller.trip;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TranslationController {

    @Value("${google.cloud.translation.api-key}")
    private String apiKey;

    @PostMapping("/translate")
    public ResponseEntity<?> translateText(
            @RequestParam String text,
            @RequestParam String target) {
        
        try {
            Translate translate = TranslateOptions.newBuilder()
                    .setApiKey(apiKey)
                    .build()
                    .getService();

            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(target)
            );

            return ResponseEntity.ok(
                    new TranslationResponse(translation.getTranslatedText())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Translation failed: " + e.getMessage());
        }
    }

    private static class TranslationResponse {
        private final String translatedText;

        public TranslationResponse(String translatedText) {
            this.translatedText = translatedText;
        }

        public String getTranslatedText() {
            return translatedText;
        }
    }
}
