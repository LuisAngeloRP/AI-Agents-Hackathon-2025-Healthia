package com.healthia.functions.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "HealthiaSmartwatchPU"; // Updated PU name
    private static EntityManagerFactory factory;
    private static final Logger LOGGER = Logger.getLogger(JPAUtil.class.getName());

    static {
        try {
            LOGGER.info("Initializing EntityManagerFactory for Smartwatch PU...");
            Map<String, String> properties = new HashMap<>();
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                LOGGER.severe("Database connection properties (DB_URL, DB_USER, DB_PASSWORD) are not fully set.");
                throw new RuntimeException("Database configuration for Smartwatch PU is missing.");
            }

            properties.put("jakarta.persistence.jdbc.url", dbUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPassword);

            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
            LOGGER.info("EntityManagerFactory for Smartwatch PU initialized successfully.");
        } catch (Throwable ex) {
            LOGGER.log(Level.SEVERE, "Initial EntityManagerFactory (Smartwatch PU) creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManager getEntityManager() {
        if (factory == null) {
            LOGGER.severe("EntityManagerFactory (Smartwatch PU) is null. Cannot create EntityManager.");
            throw new IllegalStateException("EntityManagerFactory (Smartwatch PU) has not been initialized or failed.");
        }
        return factory.createEntityManager();
    }

    public static void close() {
        if (factory != null && factory.isOpen()) {
            factory.close();
            LOGGER.info("EntityManagerFactory (Smartwatch PU) closed.");
        }
    }
} 