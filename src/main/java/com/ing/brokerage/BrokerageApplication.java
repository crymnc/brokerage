package com.ing.brokerage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class BrokerageApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokerageApplication.class, args);
	}

}
