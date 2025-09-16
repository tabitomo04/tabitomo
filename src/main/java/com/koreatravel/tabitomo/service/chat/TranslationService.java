package com.koreatravel.tabitomo.service;

import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    private final Translator translator;

    public TranslationService(@Value("${deepl.api.key}") String apiKey) {
        // DeepL API 키를 사용하여 Translator 인스턴스 초기화
        this.translator = new Translator(apiKey);
    }

    public String translateText(String text, String targetLang) {
        // ✅ targetLang이 null이거나 비어있거나 한국어일 경우 번역을 시도하지 않습니다.
        if (targetLang == null || targetLang.isEmpty() || "ko".equalsIgnoreCase(targetLang)) {
            return text;
        }

        try {
            // 번역 API 호출
            // targetLang을 대문자로 변환하거나, DeepL이 허용하는 형식으로 변환
            TextResult result = translator.translateText(text, null, targetLang);
            return result.getText();
        } catch (Exception e) {
            System.err.println("번역 실패: " + e.getMessage());
            return text; // 실패 시 원본 텍스트 반환
        }
    }
}