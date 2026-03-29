package com.example.app.user;

import com.example.app.core.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * UserServiceApplication - Spring Boot entry point for User microservice
 * 
 * Run with: java -jar microservices.jar --spring.application.name=user-service --server.port=8081
 * Or: mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.app"})
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║    USER MICROSERVICE STARTED                ║");
        System.out.println("║    REST API: http://localhost:8081/api/users");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }
}
