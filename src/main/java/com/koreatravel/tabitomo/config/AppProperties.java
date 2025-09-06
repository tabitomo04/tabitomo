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
     * List of supported languages in the application.
     * Example: ko, en, ja
     */
    private List<String> supportedLanguages;

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
}
