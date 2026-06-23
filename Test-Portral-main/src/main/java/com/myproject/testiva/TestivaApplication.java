package com.myproject.testiva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestivaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestivaApplication.class, args);
	}
 
}
