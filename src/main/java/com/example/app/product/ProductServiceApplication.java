package com.example.app.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * ProductServiceApplication - Spring Boot entry point for Product microservice
 * 
 * Run with: java -jar microservices.jar --spring.application.name=product-service --server.port=8082
 * Or: mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.app"})
public class ProductServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║    PRODUCT MICROSERVICE STARTED             ║");
        System.out.println("║    REST API: http://localhost:8082/api/products");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }
}
