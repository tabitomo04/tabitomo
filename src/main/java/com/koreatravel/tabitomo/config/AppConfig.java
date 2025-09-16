package com.koreatravel.tabitomo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable and validate application properties.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
    // This class enables the AppProperties configuration
    // and ensures that the properties are properly validated
}
