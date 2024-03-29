package com.nathanieldoe.santa;

import com.nathanieldoe.santa.db.PersonRepository;
import com.nathanieldoe.santa.model.Exclusion;
import com.nathanieldoe.santa.model.Person;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@EntityScan(basePackageClasses = { Person.class, Exclusion.class })
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
		ManagementWebSecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class})
@SecurityScheme(name = "Bearer-Token", scheme = "Bearer", type = SecuritySchemeType.HTTP)
public class SecretSantaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecretSantaApiApplication.class, args);
	}

	@Autowired
	PersonRepository repository;

	@Bean
	public OpenAPI secretSantaApi() {
		return new OpenAPI()
				.info(new Info().title("Secret Santa API")
						.description("Secret Santa generation API"));
	}

}
