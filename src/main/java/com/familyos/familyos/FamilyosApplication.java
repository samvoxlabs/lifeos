package com.familyos.familyos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.familyos.familyos.config.properties")
public class FamilyosApplication {

	public static void main(String[] args) {
		SpringApplication.run(FamilyosApplication.class, args);
	}

}
