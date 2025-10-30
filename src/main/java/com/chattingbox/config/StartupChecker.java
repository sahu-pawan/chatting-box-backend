package com.chattingbox.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Validates that required environment-backed properties are present at startup.
 * - Fails startup when a critical property (Mongo URI) is missing.
 * - Logs warnings for optional but recommended properties (Google OAuth client
 * id/secret).
 */
@Component
public class StartupChecker implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(StartupChecker.class);

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running StartupChecker: validating required configuration...");

        if (mongoUri == null || mongoUri.isBlank()) {
            logger.error("Missing required configuration: spring.data.mongodb.uri (MONGODB_URI).\n" +
                    "Set the MONGODB_URI environment variable or provide the property in configuration.\n" +
                    "Application will not start without a MongoDB connection.");
            // Fail fast
            throw new IllegalStateException("Missing required configuration: spring.data.mongodb.uri");
        }

        if (googleClientId == null || googleClientId.isBlank()) {
            logger.warn("Google OAuth client id (GOOGLE_CLIENT_ID) is not set. Google login will be disabled.");
        }

        if (googleClientSecret == null || googleClientSecret.isBlank()) {
            logger.warn("Google OAuth client secret (GOOGLE_CLIENT_SECRET) is not set. Google login will be disabled.");
        }

        logger.info("StartupChecker: configuration validation complete.");
    }
}
