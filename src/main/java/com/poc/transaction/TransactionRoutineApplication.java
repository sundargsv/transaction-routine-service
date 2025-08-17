package com.poc.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class TransactionRoutineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionRoutineApplication.class, args);
		log.info("Application started");
	}

}
