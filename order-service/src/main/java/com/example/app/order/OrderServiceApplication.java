package com.example.app.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * OrderServiceApplication - Spring Boot entry point for Order microservice
 * Demonstrates cross-service communication via REST APIs
 * 
 * Run with: java -jar microservices.jar --spring.application.name=order-service --server.port=8083
 * Or: mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.app"})
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║    ORDER MICROSERVICE STARTED               ║");
        System.out.println("║    REST API: http://localhost:8083/api/orders");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }
}
