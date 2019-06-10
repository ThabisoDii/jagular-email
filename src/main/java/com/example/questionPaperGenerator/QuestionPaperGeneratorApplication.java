package com.example.questionPaperGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QuestionPaperGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuestionPaperGeneratorApplication.class, args);
	}

}
