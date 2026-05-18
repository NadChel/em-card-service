package com.example.em_card_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(info = @Info(version = "1.0", title = "Card Service"),
		security = @SecurityRequirement(name = "bearer-key"))
public class EmCardServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmCardServiceApplication.class, args);
	}

}
