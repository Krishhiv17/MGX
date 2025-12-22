package com.mgx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MgxBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MgxBackendApplication.class, args);
	}

}
