package com.surohi.backend.cric_scorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CricScorerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CricScorerApplication.class, args);
	}

}
