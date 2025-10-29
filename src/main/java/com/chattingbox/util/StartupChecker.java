package com.chattingbox.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StartupChecker implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupChecker.class);

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Override
    public void run(String... args) throws Exception {
        log.info("StartupChecker: spring.data.mongodb.uri set? {}", (mongoUri != null && !mongoUri.isBlank()));
        if (mongoUri == null || mongoUri.isBlank()) {
            log.warn("MONGODB URI is not set. Authentication and DB operations will fail.");
        }
    }
}
