package com.koreatravel.tabitomo.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.baseline-on-migrate}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.baseline-version}")
    private String baselineVersion;

    @Value("${spring.flyway.baseline-description}")
    private String baselineDescription;

    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion)
                .baselineDescription(baselineDescription)
                .locations("classpath:db/migration")
                .load();
    }
}
