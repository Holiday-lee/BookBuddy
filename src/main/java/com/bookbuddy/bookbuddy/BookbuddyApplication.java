package com.bookbuddy.bookbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BookbuddyApplication {

	private static final Logger logger = LoggerFactory.getLogger(BookbuddyApplication.class);

	public static void main(String[] args) {
		logger.info("Bookbuddy application is starting!");
		SpringApplication.run(BookbuddyApplication.class, args);
	}

}
