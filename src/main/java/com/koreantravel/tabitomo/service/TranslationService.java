package com.koreantravel.tabitomo.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Map;
import java.util.HashMap;

@Service
public class TranslationService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl = "https://translation.googleapis.com/language/translate/v2";

    public TranslationService(@Value("${google.cloud.translation.api-key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    public List<String> translateTexts(List<String> texts, String targetLanguage) {
        if (texts == null || texts.isEmpty()) {
            return texts;
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("q", texts);
        requestBody.put("target", targetLanguage);

        try {
            JsonNode response = restTemplate.postForObject(uri, requestBody, JsonNode.class);
            if (response != null && response.has("data")) {
                JsonNode data = response.get("data");
                if (data.has("translations")) {
                    JsonNode translations = data.get("translations");
                    if (translations.isArray()) {
                        return StreamSupport.stream(translations.spliterator(), false)
                                .map(t -> t.get("translatedText").asText())
                                .collect(Collectors.toList());
                    }
                }
            }
            return texts; // Return original texts if translation fails
        } catch (Exception e) {
            System.err.println("Translation API call failed: " + e.getMessage());
            return texts; // In case of an error, return the original texts
        }
    }
}
