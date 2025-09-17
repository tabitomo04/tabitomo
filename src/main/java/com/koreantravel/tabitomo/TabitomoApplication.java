package com.koreantravel.tabitomo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan("vio.tabitomo.config")
@EnableJpaRepositories
public class TabitomoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TabitomoApplication.class, args);
	}

}
