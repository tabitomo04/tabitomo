package com.koreatravel.tabitomo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Primary
@SpringBootApplication(
    exclude = SecurityAutoConfiguration.class,
    scanBasePackages = "com.koreatravel.tabitomo"
)
@ComponentScan(basePackages = "com.koreatravel.tabitomo")
@EntityScan("com.koreatravel.tabitomo.domain.entity")
@EnableJpaRepositories("com.koreatravel.tabitomo.repository")
public class TabitomoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TabitomoApplication.class, args);
    }
}
