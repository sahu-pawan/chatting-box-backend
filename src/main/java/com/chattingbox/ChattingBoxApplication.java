package com.chattingbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories("com.chattingbox.repository")
public class ChattingBoxApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChattingBoxApplication.class, args);
    }
}
