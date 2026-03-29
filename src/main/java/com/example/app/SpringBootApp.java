package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * SpringBootApp - Main Spring Boot Application
 * Enables Spring Boot with REST endpoints for all microservices
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.app"})
public class SpringBootApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApp.class, args);
    }
}
