package com.koreatravel.tabitomo.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable and validate application properties.
 */
@Configuration
@ConfigurationPropertiesScan("com.koreatravel.tabitomo.config")
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
    // This class enables the AppProperties configuration
    // and ensures that the properties are properly validated
}
