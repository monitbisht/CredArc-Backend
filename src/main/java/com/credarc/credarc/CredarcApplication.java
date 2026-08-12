package com.credarc.credarc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CredarcApplication {

	public static void main(String[] args) {

		SpringApplication.run(CredarcApplication.class, args);
	}

}
