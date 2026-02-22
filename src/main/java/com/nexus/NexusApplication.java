package com.nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Clase principal de la aplicación Nexus.
 *
 * @EnableAsync      → EmailService envía emails sin bloquear el hilo HTTP
 * @EnableScheduling → UpvoteRankingScheduler recalcula el ranking periódicamente
 * @EnableCaching    → Caché de Spring para optimizar consultas frecuentes
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class NexusApplication implements CommandLineRunner {

    @Autowired
    private PopulateDB populateDB;

    public static void main(String[] args) {
        SpringApplication.run(NexusApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        populateDB.popular();
    }

}