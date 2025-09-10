package com.koreatravel.tabitomo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Application configuration properties.
 * Maps properties from application.properties with prefix 'app'.
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    /**
     * Base URL of the application (e.g., https://example.com)
     */
    private String baseUrl;
    
    /**
     * Expiration time for password reset tokens in hours
     */
    private int passwordResetTokenExpiryHours = 24;
    
    /**
     * Google Gemini API key
     */
    private String googleGeminiApiKey;
    
    /**
     * Gemini API URL
     */
    private String geminiApiUrl;
    
    /**
     * Kakao API configuration
     */
    private final Kakao kakao = new Kakao();
    
    /**
     * OpenWeatherMap API key
     */
    private String openweathermapApiKey;
    
    /**
     * Pixabay API key
     */
    private String pixabayApiKey;
    
    /**
     * Google Maps API key
     */
    private String googleMapsApiKey;
    
    /**
     * Google Cloud Translation API key
     */
    private String googleCloudTranslationApiKey;
    
    /**
     * List of supported languages in the application.
     * Example: ko, en, ja
     */
    private List<String> supportedLanguages = List.of("ko", "en", "ja");
    
    /**
     * Default language
     */
    private String defaultLanguage = "ko";

    @PostConstruct
    public void validate() {
        if (CollectionUtils.isEmpty(supportedLanguages)) {
            throw new IllegalStateException("At least one supported language must be configured");
        }
    }

    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    /**
     * Check if a language is supported by the application.
     *
     * @param languageCode The language code to check (e.g., "ko", "en", "ja")
     * @return true if the language is supported, false otherwise
     */
    public boolean isLanguageSupported(String languageCode) {
        return supportedLanguages != null && supportedLanguages.contains(languageCode);
    }
    
    // Getters and Setters
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getPasswordResetTokenExpiryHours() {
        return passwordResetTokenExpiryHours;
    }
    
    public void setPasswordResetTokenExpiryHours(int passwordResetTokenExpiryHours) {
        this.passwordResetTokenExpiryHours = passwordResetTokenExpiryHours;
    }
    
    public String getGoogleGeminiApiKey() {
        return googleGeminiApiKey;
    }
    
    public void setGoogleGeminiApiKey(String googleGeminiApiKey) {
        this.googleGeminiApiKey = googleGeminiApiKey;
    }
    
    public String getGeminiApiUrl() {
        return geminiApiUrl;
    }
    
    public void setGeminiApiUrl(String geminiApiUrl) {
        this.geminiApiUrl = geminiApiUrl;
    }
    
    public Kakao getKakao() {
        return kakao;
    }
    
    public String getOpenweathermapApiKey() {
        return openweathermapApiKey;
    }
    
    public void setOpenweathermapApiKey(String openweathermapApiKey) {
        this.openweathermapApiKey = openweathermapApiKey;
    }
    
    public String getPixabayApiKey() {
        return pixabayApiKey;
    }
    
    public void setPixabayApiKey(String pixabayApiKey) {
        this.pixabayApiKey = pixabayApiKey;
    }
    
    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }
    
    public void setGoogleMapsApiKey(String googleMapsApiKey) {
        this.googleMapsApiKey = googleMapsApiKey;
    }
    
    public String getGoogleCloudTranslationApiKey() {
        return googleCloudTranslationApiKey;
    }
    
    public void setGoogleCloudTranslationApiKey(String googleCloudTranslationApiKey) {
        this.googleCloudTranslationApiKey = googleCloudTranslationApiKey;
    }
    
    /**
     * Kakao API configuration properties
     */
    public static class Kakao {
        private String localApiKey;
        private String apiKey;
        private String jsKey;
        
        public String getLocalApiKey() {
            return localApiKey;
        }
        
        public void setLocalApiKey(String localApiKey) {
            this.localApiKey = localApiKey;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getJsKey() {
            return jsKey;
        }
        
        public void setJsKey(String jsKey) {
            this.jsKey = jsKey;
        }
    }
}
